package com.tbs.common.provider.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.tbs.common.annotation.EnableTbsRoutingService;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.annotation.RoutingService;

import lombok.extern.slf4j.Slf4j;

/**
 * 多来源路由服务注册器
 * <p>
 * 扫描标注了 {@link RoutingService} 的业务接口，为每个接口注册对应的 {@link RoutingServiceFactoryBean} Bean 定义，
 * 使该接口以路由代理形式进入 Spring 依赖注入体系。
 * 接口无法直接实例化，故不能复用按组件类注册的扫描基类，此处基于
 * ClassPathScanningCandidateComponentProvider 自定义「接口为候选组件」的扫描规则；
 * Environment 与 ResourceLoader 由 Spring 对 ImportBeanDefinitionRegistrar 的 Aware 回调注入，
 * 扫描基包可经 {@link EnableTbsRoutingService} 缩小，重复导入时按 Bean 名称防重。
 * 路由代理与本地实现同属一个接口类型，因此代理 Bean 必须标为 primary：
 * 否则按类型注入会命中两个候选 Bean 而抛出 NoUniqueBeanDefinitionException。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RoutingServiceRegistrar
    implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware, ImportAware {

    /**
     * 默认扫描基础包
     */
    private static final String DEFAULT_BASE_PACKAGE = "com.tbs";

    /**
     * 扫描基础包集合
     */
    private final Set<String> basePackages = new LinkedHashSet<>();

    /**
     * 环境对象
     */
    private Environment environment;

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;

    /**
     * 构造路由服务注册器（默认扫描 com.tbs 包）
     * <p>
     * @Import 无参导入时使用本构造器。
     * </p>
     */
    public RoutingServiceRegistrar() {
        this.basePackages.add(DEFAULT_BASE_PACKAGE);
    }

    /**
     * 构造路由服务注册器
     *
     * @param basePackages 扫描基础包集合，为空时使用默认包 com.tbs
     */
    public RoutingServiceRegistrar(String... basePackages) {
        if (basePackages != null && basePackages.length > 0) {
            this.basePackages.addAll(Set.of(basePackages));
        } else {
            this.basePackages.add(DEFAULT_BASE_PACKAGE);
        }
    }

    /**
     * 注入环境对象
     *
     * @param environment 环境对象
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 注入资源加载器
     *
     * @param resourceLoader 资源加载器
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 读取导入类的 {@link EnableTbsRoutingService} 属性，按需缩小扫描范围
     * <p>
     * 直接 @Import 本注册器（如经 ObjectProviderConfig 自动装配）时导入类无该注解，
     * 保持默认全包扫描；显式声明了基包时以声明为准，避免大型仓库下不必要的 classpath 扫描。
     * </p>
     *
     * @param importMetadata 导入类元数据，不能为null
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(EnableTbsRoutingService.class.getName());
        if (attributes == null) {
            return;
        }
        var packages = (String[]) attributes.get("basePackages");
        if (packages != null && packages.length > 0) {
            this.basePackages.clear();
            this.basePackages.addAll(Arrays.asList(packages));
        }
    }

    /**
     * 构建「仅接受标注注解的独立接口」的扫描器
     *
     * @return 候选组件扫描器
     */
    private ClassPathScanningCandidateComponentProvider buildScanner() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false, environment) {
            @Override
            protected boolean isCandidateComponent(MetadataReader metadataReader) {
                return metadataReader.getClassMetadata().isInterface()
                    && metadataReader.getClassMetadata().isIndependent();
            }

            /**
             * 接口非具体类，默认校验会拒绝接口候选，需放行为独立接口
             */
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface()
                    && beanDefinition.getMetadata().isIndependent();
            }
        };
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RoutingService.class));
        return scanner;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        var packages = basePackages.toArray(new String[0]);
        var scanner = buildScanner();
        log.info("开始扫描@RoutingService标注接口, 包: {}", String.join(",", packages));
        var count = 0;
        for (var basePackage : packages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                var interfaceName = candidate.getBeanClassName();
                try {
                    var interfaceType = (Class<?>) ClassUtils.forName(interfaceName, resourceLoader.getClassLoader());
                    var annotation = interfaceType.getAnnotation(RoutingService.class);
                    if (annotation == null) {
                        continue;
                    }
                    var strategy = annotation.strategy();
                    var beanName = resolveBeanName(interfaceType, annotation.beanName());
                    if (registry.containsBeanDefinition(beanName)) {
                        log.warn("路由代理Bean[{}]与已有BeanDefinition同名, 接口: {}, 跳过注册",
                            beanName, interfaceName);
                        continue;
                    }
                    registerFactoryBean(registry, beanName, interfaceType, strategy);
                    count++;
                } catch (ClassNotFoundException e) {
                    log.warn("@RoutingService接口类加载失败: {}", interfaceName);
                }
            }
        }
        log.info("扫描@RoutingService标注接口完成, 共注册 {} 个路由代理Bean", count);
    }

    /**
     * 推导路由代理的 Bean 名称
     * <p>
     * 未显式指定时附加固定后缀，避免与业务侧同名的本地实现 Bean 相互顶掉（顶掉后代理静默不注册或启动报覆盖异常）；
     * 显式指定时尊重业务侧命名，不额外附加后缀。
     * </p>
     *
     * @param interfaceType    目标接口类型，不能为null
     * @param declaredBeanName 注解声明的 Bean 名称，允许为空
     * @return 最终 Bean 名称
     */
    private String resolveBeanName(Class<?> interfaceType, String declaredBeanName) {
        if (declaredBeanName != null && !declaredBeanName.isEmpty()) {
            return declaredBeanName;
        }
        var simpleName = interfaceType.getSimpleName();
        var decapitalized = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        return decapitalized + ProviderConstant.ROUTING_PROXY_BEAN_NAME_SUFFIX;
    }

    /**
     * 注册单个路由代理工厂 Bean 定义
     * <p>
     * 注入接口类型、路由策略与 IObjectProvider 的运行时 Bean 引用；
     * 标为 primary 使按类型注入在「本地实现 + 路由代理」两个候选中确定性地命中代理。
     * </p>
     *
     * @param registry      Bean 定义注册器，不能为null
     * @param beanName      Bean 名称，不能为null
     * @param interfaceType 目标接口类型，不能为null
     * @param strategy      路由策略，不能为null
     */
    private void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Class<?> interfaceType,
        RoutingStrategy strategy) {
        var definition = BeanDefinitionBuilder.genericBeanDefinition(RoutingServiceFactoryBean.class);
        definition.addConstructorArgValue(interfaceType);
        definition.addConstructorArgValue(strategy);
        definition.addConstructorArgValue(new RuntimeBeanReference(IObjectProvider.class));
        var beanDefinition = definition.getBeanDefinition();
        // 声明FactoryBean产物类型，使按接口类型的依赖注入可在实例化前完成类型匹配
        beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, interfaceType);
        beanDefinition.setPrimary(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
