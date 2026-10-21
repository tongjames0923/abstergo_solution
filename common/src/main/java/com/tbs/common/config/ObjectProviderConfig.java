package com.tbs.common.config;

import java.util.List;

import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tbs.common.config.property.ObjectProviderProperty;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.IRegistrationSource;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.impl.DefaultObjectProvider;
import com.tbs.common.provider.impl.DefaultRegistryCenter;
import com.tbs.common.provider.impl.DubboInstanceResolver;
import com.tbs.common.provider.impl.ObjectResolverAsSource;
import com.tbs.common.provider.impl.RoutingProxyFactory;
import com.tbs.common.provider.impl.RoutingServiceRegistrar;
import com.tbs.common.provider.impl.SpringObjectResolver;

/**
 * 对象提供器自动装配
 * <p>
 * 装配统一对象提供器门面与多来源解析器链：Spring 来源解析器默认注册；
 * Dubbo 多实例发现解析器经 @ConditionalOnClass 守卫，Dubbo 依赖缺失时静默降级为仅 Spring 来源。
 * 同时装配统一注册中心：存量解析器经 ObjectResolverAsSource 桥接为贡献来源（Spring 静态贡献、
 * Dubbo 读即重贡献），装配期只登记贡献者不预扫，条目由中心首次读取按需填充。
 * 同时导入路由服务注册器，使标注 @RoutingService 的接口自动注册为路由代理 Bean。
 * 配置经 EnableCommonPack 扫描注册生效，属性由 @EnableConfigurationProperties 显式激活。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.common.object-provider", name = "enable", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(ObjectProviderProperty.class)
@Import(RoutingServiceRegistrar.class)
public class ObjectProviderConfig {

    /**
     * 注册 Hutool SpringUtil 容器持有器
     * <p>
     * SpringUtil 依赖 ApplicationContextAware 回调持有容器引用，项目内此前无注册点，
     * 容器不可用会导致 Spring 来源解析静默失败；提供兜底注册并允许业务方覆盖。
     * </p>
     *
     * @return Hutool SpringUtil 持有器
     */
    @Bean
    @ConditionalOnMissingBean(cn.hutool.extra.spring.SpringUtil.class)
    cn.hutool.extra.spring.SpringUtil hutoolSpringUtil() {
        return new cn.hutool.extra.spring.SpringUtil();
    }

    /**
     * 注册 Spring 容器来源解析器
     *
     * @return Spring 来源解析器
     */
    @Bean
    @ConditionalOnMissingBean(SpringObjectResolver.class)
    @SuppressWarnings("deprecation")
    IObjectResolver springObjectResolver() {
        return new SpringObjectResolver();
    }

    /**
     * 注册 Dubbo 多实例发现解析器
     * <p>
     * Dubbo 依赖缺失时不装配，解析链自动退化为仅 Spring 来源。
     * </p>
     *
     * @param property 对象提供器配置
     * @return Dubbo 多实例发现解析器
     */
    @Bean
    @ConditionalOnMissingBean(DubboInstanceResolver.class)
    @ConditionalOnClass(ReferenceConfig.class)
    @SuppressWarnings("deprecation")
    IObjectResolver dubboInstanceResolver(ObjectProviderProperty property) {
        return new DubboInstanceResolver(property);
    }

    /**
     * 注册统一对象提供器门面
     * <p>
     * 收集全部解析器构建有序解析链，允许业务方自定义 IObjectResolver 扩展来源。
     * 解析链为过渡期语义，阶段 3-δ 切换为中心统一解析分派后退出主线。
     * </p>
     *
     * @param resolvers 解析器集合
     * @return 统一对象提供器门面
     */
    @Bean
    @ConditionalOnMissingBean(IObjectProvider.class)
    @SuppressWarnings("deprecation")
    IObjectProvider objectProvider(List<IObjectResolver> resolvers, IRegistryCenter registryCenter) {
        return new DefaultObjectProvider(resolvers, registryCenter);
    }

    /**
     * 注册统一注册中心
     * <p>
     * 存量解析器逐个桥接为贡献来源：Dubbo 声明读即重贡献（新鲜度由解析器快照时效节流），
     * 其余来源按静态一次性贡献处理；贡献动作只登记懒贡献者，不在装配期扫容器/查注册中心。
     * 业务方自定义的 IRegistrationSource Bean 一并被收集贡献。
     * </p>
     *
     * @param resolvers 存量解析器集合
     * @param sources   显式声明的贡献来源集合（含桥接与业务自定义）
     * @return 统一注册中心
     */
    @Bean
    @ConditionalOnMissingBean(IRegistryCenter.class)
    @SuppressWarnings("deprecation")
    public IRegistryCenter registryCenter(List<IObjectResolver> resolvers, List<IRegistrationSource> sources) {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        resolvers.stream()
            .filter(resolver -> sources.stream().noneMatch(source -> source.source().equals(resolver.getSource())))
            .map(this::bridgeResolver)
            .forEach(source -> source.contribute(center));
        sources.forEach(source -> source.contribute(center));
        center.attachRoutingProxyFactory(new RoutingProxyFactory());
        return center;
    }

    /**
     * 桥接存量解析器为贡献来源：Dubbo 读即重贡献，其他来源静态一次性贡献
     *
     * @param resolver 存量解析器，不能为null
     * @return 桥接后的贡献来源
     */
    @SuppressWarnings("deprecation")
    private IRegistrationSource bridgeResolver(IObjectResolver resolver) {
        return new ObjectResolverAsSource(resolver, ObjectSource.DUBBO.equals(resolver.getSource()));
    }
}
