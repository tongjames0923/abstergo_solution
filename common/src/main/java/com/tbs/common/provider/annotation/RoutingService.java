package com.tbs.common.provider.annotation;

import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.impl.LoggingNoProviderHandler;

import java.lang.annotation.*;

/**
 * 多来源路由服务注解
 * <p>
 * 标注在业务接口上，声明该接口需要注册为多来源路由代理 Bean： 由 RoutingServiceRegistrar 扫描识别后，为该接口注册 RoutingServiceFactoryBean， 产出可在 Spring Bean 与
 * Dubbo 实例间按策略动态路由的代理，业务侧按普通 Bean 方式注入使用。
 * </p>
 *
 * @author abstergo
 * @see com.tbs.common.provider.RoutingStrategy 路由策略
 * @since 1.0.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RoutingService {

    /**
     * 业务线
     * @return
     */
    String business() default "";

    /**
     * 版本
     * @return
     */
    String version() default "";


    /**
     * 无提供者处理类
     *
     * @return 无提供者处理类
     */
    Class<? extends INoProviderHandler> noProvider() default LoggingNoProviderHandler.class;
}
