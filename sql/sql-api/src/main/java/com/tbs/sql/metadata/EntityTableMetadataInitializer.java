package com.tbs.sql.metadata;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 启动期实体表元数据初始化器：扫描 {@link EntityScan} 指定的包，校验实体注解并预热
 * {@link EntityMetadataExtractor} 的 {@code EntityTableMetadata} 缓存。
 *
 * <p>由 {@link EntityScanRegistrar} 注册为基础设施 Bean，在所有非懒加载单例初始化完成后执行，
 * 因此问题实体会在容器刷新阶段（启动期）就暴露，而不是等到第一次执行 INSERT / UPDATE。</p>
 *
 * <p>扫描基于 ASM 元数据（{@link CachingMetadataReaderFactory}），不加载业务类、不触发静态初始化；
 * 命中候选后才按类名加载并交给 {@link EntityMetadataExtractor#register(Class)} 完成校验与缓存写入。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see EntityScan
 * @see EntityScanRegistrar
 */
public class EntityTableMetadataInitializer implements SmartInitializingSingleton, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EntityTableMetadataInitializer.class);
    private static final String ENTITY_ANNOTATION = Entity.class.getName();
    private static final String TABLE_ANNOTATION = Table.class.getName();
    private static final String CLASS_PATTERN_SUFFIX = "/**/*.class";

    private final Collection<String> basePackages;
    private ApplicationContext applicationContext;

    public EntityTableMetadataInitializer(Collection<String> basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Set<String> packages = resolvePackages();
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(applicationContext);
        Set<String> candidates = new LinkedHashSet<>();
        for (String basePackage : packages) {
            collectCandidates(basePackage, readerFactory, candidates);
        }

        List<String> errors = new ArrayList<>();
        int initialized = 0;
        for (String className : candidates) {
            try {
                Class<?> entityClass = ClassUtils.forName(className, applicationContext.getClassLoader());
                EntityMetadataExtractor.register(entityClass);
                initialized++;
            } catch (ClassNotFoundException | LinkageError | RuntimeException e) {
                String message = e.getMessage();
                errors.add(className + "：" + (StringUtils.hasText(message) ? message : e.getClass().getName()));
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("实体表元数据启动期校验失败，共" + errors.size() + "个实体注解不完整：\n  - "
                + String.join("\n  - ", errors));
        }
        log.info("@EntityScan 启动期已校验并预热 {} 个 EntityTableMetadata，扫描包：{}", initialized, packages);
    }

    /**
     * 解析包配置中的 {@code ${...}} 占位符，并拆分逗号/分号分隔的多个包名。
     *
     * @return 去重后的扫描包
     */
    private Set<String> resolvePackages() {
        Environment environment = applicationContext.getEnvironment();
        Set<String> resolved = new LinkedHashSet<>();
        for (String declared : basePackages) {
            String value = environment.resolveRequiredPlaceholders(declared);
            for (String basePackage : StringUtils.tokenizeToStringArray(value, ",; \t\n")) {
                resolved.add(basePackage);
            }
        }
        return resolved;
    }

    /**
     * 扫描单个包下所有 class 的 ASM 元数据，收集标注了 {@code @Entity} 或 {@code @Table} 的类名。
     *
     * <p>只认直接标注：与运行期 {@code SqlUtils} 的 {@code isAnnotationPresent} 口径一致，
     * 避免组合注解被扫描命中却在生成 SQL 时表名回退。只标其中一个注解的类也会进入候选，
     * 由 {@link EntityMetadataExtractor#register(Class)} 统一报"注解不完整"。</p>
     *
     * @param basePackage   待扫描包
     * @param readerFactory ASM 元数据读取器
     * @param candidates    候选类名集合
     */
    private void collectCandidates(String basePackage, CachingMetadataReaderFactory readerFactory,
        Set<String> candidates) {
        String location = "classpath*:" + basePackage.replace('.', '/') + CLASS_PATTERN_SUFFIX;
        Resource[] resources;
        try {
            resources = applicationContext.getResources(location);
        } catch (IOException e) {
            throw new IllegalStateException("@EntityScan 扫描包[" + basePackage + "]失败", e);
        }
        if (resources.length == 0) {
            log.warn("@EntityScan 扫描包[{}]未找到任何 class 资源，请确认包名是否正确", basePackage);
        }
        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            MetadataReader metadataReader;
            try {
                metadataReader = readerFactory.getMetadataReader(resource);
            } catch (IOException e) {
                throw new IllegalStateException("@EntityScan 读取实体类元数据失败：" + resource, e);
            }
            AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
            if (metadata.hasAnnotation(ENTITY_ANNOTATION) || metadata.hasAnnotation(TABLE_ANNOTATION)) {
                candidates.add(metadata.getClassName());
            }
        }
    }
}
