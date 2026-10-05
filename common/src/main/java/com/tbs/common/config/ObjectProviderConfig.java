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
import com.tbs.common.provider.impl.DefaultObjectProvider;
import com.tbs.common.provider.impl.DubboInstanceResolver;
import com.tbs.common.provider.impl.RoutingServiceRegistrar;
import com.tbs.common.provider.impl.SpringObjectResolver;

/**
 * 对象提供器自动装配
 * <p>
 * 装配统一对象提供器门面与多来源解析器链：Spring 来源解析器默认注册；
 * Dubbo 多实例发现解析器经 @ConditionalOnClass 守卫，Dubbo 依赖缺失时静默降级为仅 Spring 来源。
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
    IObjectResolver dubboInstanceResolver(ObjectProviderProperty property) {
        return new DubboInstanceResolver(property);
    }

    /**
     * 注册统一对象提供器门面
     * <p>
     * 收集全部解析器构建有序解析链，允许业务方自定义 IObjectResolver 扩展来源。
     * </p>
     *
     * @param resolvers 解析器集合
     * @return 统一对象提供器门面
     */
    @Bean
    @ConditionalOnMissingBean(IObjectProvider.class)
    IObjectProvider objectProvider(List<IObjectResolver> resolvers) {
        return new DefaultObjectProvider(resolvers);
    }
}
