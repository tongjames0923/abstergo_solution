package com.tbs.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 动态配置能力自动装配：注册 {@link DynamicConfigBinder}，并在存在 Spring Cloud 时
 * 接入其配置刷新事件，使 {@code @DynamicConfig} 字段/setter 随 Nacos 变更自动热更新。
 *
 * <p>由 {@code META-INF/spring/...AutoConfiguration.imports} 自动装载，无需使用方显式开启。</p>
 *
 * <p>{@link DynamicConfigBinder} 是 {@code BeanPostProcessor}，以 {@code static} {@code @Bean} 注册，
 * 确保其在常规单例实例化之前就位，从而不漏掉任何带注解的 Bean。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class DynamicConfigAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    static DynamicConfigBinder dynamicConfigBinder() {
        return new DynamicConfigBinder();
    }

    /**
     * Spring Cloud 刷新事件桥接：仅当 classpath 存在 {@link EnvironmentChangeEvent}（引入了 spring-cloud-context）
     * 时装配。Nacos {@code refreshEnabled=true} 的配置变更经 {@code ContextRefresher} 更新 {@code Environment}
     * 后发布本事件，此处把变更的 key 集合转交 {@link DynamicConfigBinder} 完成热更新。
     *
     * <p>非 Spring Cloud 应用不装配本桥接，但仍可用 {@link DynamicConfigBinder#rebind(java.util.Collection)}
     * 的启动初绑能力；{@code common} 不因该可选依赖被强制耦合到下游模块。</p>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.cloud.context.environment.EnvironmentChangeEvent")
    static class SpringCloudRefreshBridgeConfiguration {

        @Bean
        ApplicationListener<EnvironmentChangeEvent> dynamicConfigRefreshListener(DynamicConfigBinder binder) {
            return event -> binder.rebind(event.getKeys());
        }
    }
}
