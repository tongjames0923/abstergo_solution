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
     * 路由策略名，取值为 {@code LOCAL_FIRST}/{@code REMOTE_FIRST}/{@code SPRING_ONLY}/{@code DUBBO_ONLY} 之一， 默认本地优先（Spring
     * 来源优先，Dubbo 来源兜底）；由注册器经 RoutingStrategy.byName 还原为策略值对象
     *
     * @return 路由策略名
     */
    String strategy() default "LOCAL_FIRST";

    /**
     * 代理 Bean 名称，为空时按接口简单名首字母小写并附加固定后缀 RoutingProxy 生成，避免与本地实现 Bean 同名
     *
     * @return Bean 名称
     */
    String beanName() default "";

    /**
     * 无提供者处理类
     *
     * @return 无提供者处理类
     */
    Class<? extends INoProviderHandler> noProvider() default LoggingNoProviderHandler.class;
}
