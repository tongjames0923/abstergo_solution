package com.tbs.common.config;

import java.util.List;

import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
import com.tbs.common.provider.IRemoteTargetResolver;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.impl.DefaultObjectProvider;
import com.tbs.common.provider.impl.DefaultRegistryCenter;
import com.tbs.common.provider.impl.DubboInstanceRegistrationSource;
import com.tbs.common.provider.impl.DubboRemoteTargetResolver;
import com.tbs.common.provider.impl.ObjectResolverAsSource;
import com.tbs.common.provider.impl.RoutingProxyFactory;
import com.tbs.common.provider.impl.RoutingRefBeanPostProcessor;
import com.tbs.common.provider.impl.RoutingServiceRegistrar;
import com.tbs.common.provider.impl.SpringObjectResolver;

/**
 * 对象提供器自动装配
 * <p>
 * 装配统一对象提供器门面与多来源解析器链：Spring 来源解析器默认注册；
 * Dubbo 在注册中心中承担两个半边（均经 @ConditionalOnClass 守卫，Dubbo 依赖缺失时静默降级为仅 Spring 来源）：
 * 枚举半边由 DubboInstanceRegistrationSource 逐实例查注册中心并落为可枚举条目，让「列出某接口的全部远程实例」可用；
 * 路由半边由 DubboRemoteTargetResolver 按 business/version 直接构造 Dubbo 原生集群引用，发现与负载均衡交给 Dubbo 自身。
 * 同时装配统一注册中心：Spring 存量解析器经 ObjectResolverAsSource 桥接为贡献来源（静态贡献），
 * 装配期只登记贡献者不预扫，条目由中心首次读取按需填充。
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
     * 注册 Dubbo 远程来源目标解析器
     * <p>
     * Dubbo 依赖缺失时不装配，路由仅剩 Spring 枚举来源；装配后 Dubbo 不再逐实例枚举进中心，
     * 而由路由代理按偏好直接向本解析器索取绑定 business/version 的 Dubbo 原生集群引用。
     * </p>
     *
     * @param property 对象提供器配置
     * @return Dubbo 远程来源目标解析器
     */
    @Bean
    @ConditionalOnMissingBean(DubboRemoteTargetResolver.class)
    @ConditionalOnClass(ReferenceConfig.class)
    IRemoteTargetResolver dubboRemoteTargetResolver(ObjectProviderProperty property) {
        return new DubboRemoteTargetResolver(property);
    }

    /**
     * 注册 Dubbo 逐实例枚举来源
     * <p>
     * 使 {@code IObjectProvider.getObjects} 与枚举型解析能看到接口的全部远程实例：按接口查询注册中心的
     * 提供者地址，为每个实例建直连引用后落为可枚举条目；择一路由不经过本源（由 {@link DubboRemoteTargetResolver} 承担）。
     * 需要 provider 以接口级注册，仅应用级注册时枚举为空但路由仍可用。
     * </p>
     *
     * @param property 对象提供器配置
     * @return Dubbo 逐实例枚举来源
     */
    @Bean
    @ConditionalOnMissingBean(DubboInstanceRegistrationSource.class)
    @ConditionalOnClass(ReferenceConfig.class)
    IRegistrationSource dubboInstanceRegistrationSource(ObjectProviderProperty property) {
        return new DubboInstanceRegistrationSource(property);
    }

    /**
     * 注册统一对象提供器门面
     * <p>
     * 门面所有读取委派 {@link IRegistryCenter}，与路由代理共用同一份来源数据，保证两条读取路径结果一致；
     * 门面 API 签名不变，存量调用方无需改动。
     * </p>
     *
     * @param registryCenter 统一注册中心
     * @return 统一对象提供器门面
     */
    @Bean
    @ConditionalOnMissingBean(IObjectProvider.class)
    IObjectProvider objectProvider(IRegistryCenter registryCenter) {
        return new DefaultObjectProvider(registryCenter);
    }

    /**
     * 注册统一注册中心
     * <p>
     * 存量解析器（如 Spring）逐个桥接为贡献来源，已有同源原生 IRegistrationSource（如 Dubbo）时跳过桥接避免重复；
     * 贡献动作只登记懒贡献者，不在装配期扫容器/查注册中心。
     * 业务方自定义的 IRegistrationSource Bean 一并被收集贡献。
     * </p>
     *
     * @param resolvers       存量解析器集合
     * @param sources         显式声明的贡献来源集合（含桥接与业务自定义）
     * @param remoteResolvers 远程来源目标解析器集合（如 Dubbo），无则为空
     * @return 统一注册中心
     */
    @Bean
    @ConditionalOnMissingBean(IRegistryCenter.class)
    @SuppressWarnings("deprecation")
    public IRegistryCenter registryCenter(List<IObjectResolver> resolvers, List<IRegistrationSource> sources,
        List<IRemoteTargetResolver> remoteResolvers) {
        DefaultRegistryCenter center = new DefaultRegistryCenter();
        resolvers.stream()
            .filter(resolver -> sources.stream().noneMatch(source -> source.source().equals(resolver.getSource())))
            .map(this::bridgeResolver)
            .forEach(source -> source.contribute(center));
        sources.forEach(source -> source.contribute(center));
        center.attachRoutingProxyFactory(new RoutingProxyFactory(remoteResolvers));
        return center;
    }

    /**
     * 注册 {@code @RoutingRef} 字段装配器
     * <p>
     * 以 static Bean 方法暴露 {@link BeanPostProcessor}，保证容器在普通 Bean 实例化前完成装配器注册；
     * 装配器内部按需从容器取统一注册中心，不提前拉起依赖。
     * </p>
     *
     * @return RoutingRef 字段装配器
     */
    @Bean
    @ConditionalOnMissingBean(RoutingRefBeanPostProcessor.class)
    static BeanPostProcessor routingRefBeanPostProcessor() {
        return new RoutingRefBeanPostProcessor();
    }

    /**
     * 桥接存量解析器为贡献来源：动态来源（如 Dubbo）读即重贡献，其他来源静态一次性贡献
     *
     * @param resolver 存量解析器，不能为null
     * @return 桥接后的贡献来源
     */
    @SuppressWarnings("deprecation")
    private IRegistrationSource bridgeResolver(IObjectResolver resolver) {
        return new ObjectResolverAsSource(resolver, ObjectSource.DUBBO.equals(resolver.getSource()));
    }
}
