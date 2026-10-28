package com.tbs.common.provider.annotation;

import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.impl.LoggingNoProviderHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多来源路由服务注册注解
 * <p>
 * 与 Spring 的 {@code @Bean}、Dubbo 的 {@code @DubboService} 同级：标注在服务实现类或 {@code @Bean} 方法上，
 * 声明该实现作为可路由服务登记进注册中心，由注册贡献者按坐标对外供给实例；未标注的实现不登记，
 * 等价于 {@code @DubboService} 不标注即不暴露。
 * </p>
 * <p>
 * {@link #business()} 与 {@link #version()} 是该实例的注册坐标，对应 {@code @DubboService} 的 group/version，
 * 消费侧按同一坐标定向寻址；两处都留空表示不约束该维度，可被任意坐标的查询命中。
 * {@link #interfaceClass()} 用于实现类实现多个接口时指定注册身份。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RoutingService {

    /**
     * 业务分区
     *
     * @return 业务分区，空串表示不分区
     */
    String business() default "";

    /**
     * 发布版本
     *
     * @return 发布版本，空串表示不区分版本
     */
    String version() default "";

    /**
     * 注册接口
     * <p>
     * 实现类实现多个接口时，声明以哪个接口身份登记；留空表示不限定，按查询类型直接匹配。
     * </p>
     *
     * @return 注册接口，留空为 {@code void.class}
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 无提供者处理类
     *
     * @return 无提供者处理类
     */
    Class<? extends INoProviderHandler> noProvider() default LoggingNoProviderHandler.class;
}
