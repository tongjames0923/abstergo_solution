package com.tbs.web.config;

import com.tbs.common.context.ContextPropagator;
import com.tbs.web.context.RequestContextPropagator;
import com.tbs.web.security.authentication.SecurityContextPropagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 上下文传播器装配
 * <p>
 * 单独装配本模块的 {@link ContextPropagator} 实现，供异步执行路径 （托管线程池、Spring {@code @Async}、Spring MVC 异步）统一收集使用。
 * </p>
 * <p>
 * <b>为何独立于 {@code WebConfig}</b>：{@code WebAsyncConfig} 作为
 * {@code WebMvcConfigurer} 会被 MVC 配置收集，而 MVC 配置又被 {@code WebConfig}（Spring Security 配置，依赖 MVC 的
 * {@code HandlerMappingIntrospector}）依赖——若传播器 Bean 定义在 {@code WebConfig} 中，将形成
 * {@code WebAsyncConfig → WebConfig → MVC 配置 → WebAsyncConfig} 的循环依赖。 传播器无外部依赖，独立装配可彻底断开该环。
 * </p>
 *
 * @author abstergo
 * @see ContextPropagator
 * @see WebAsyncConfig
 */
@Configuration
public class ContextPropagatorConfig {

    /**
     * 创建请求上下文传播器
     *
     * @return 请求上下文传播器，传播 {@code RequestDataContext}（traceId、业务数据等）
     */
    @Bean
    ContextPropagator requestContextPropagator() {
        return new RequestContextPropagator();
    }

    /**
     * 创建 Spring Security 认证上下文传播器
     *
     * @return 认证上下文传播器，传播安全上下文中的认证信息
     */
    @Bean
    ContextPropagator securityContextPropagator() {
        return new SecurityContextPropagator();
    }
}
