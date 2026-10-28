package com.tbs.common.provider.annotation;

import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.impl.LoggingNoProviderHandler;

import java.lang.annotation.*;

/**
 * 多来源路由引用注解（分类装配）
 * <p>
 * 标注在业务字段上，声明「按来源坐标定向装配一个路由代理」：字段类型即目标接口，
 * {@link #business()} 与 {@link #version()} 对应 {@code SourceCoordinate} 的业务分区与发布版本两个维度，
 * 由装配器经统一注册中心构建并注入绑定该坐标的路由代理，使同一接口可在不同字段按不同 business/version
 * 分类注入，达成「区分版本与业务分区的分类装配」。
 * </p>
 * <p>
 * 与 {@link RoutingService} 正交：{@code @RoutingService} 为接口注册一个不绑定维度的主代理 Bean（按类型注入命中它），
 * 本注解不要求接口预先标注 {@code @RoutingService}，直接对中心产出按坐标绑定的注入点代理。
 * 三个属性均支持 {@code ${...}} 占位符（经容器 Environment 解析），留空表示该维度不约束。
 * </p>
 * <p>
 * business/version 的分类对本地 Spring Bean 生效依赖来源侧坐标推导：本地实现类可实现
 * {@code IRouteMeta} 自声明其 business/version，未声明时框架以实现类简单名兜底 business（详见注册中心实现文档 §3.2）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see RoutingService 接口的路由代理声明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RoutingRef {

    /**
     * 业务分区（Dubbo group / Spring 实现类自声明或类名兜底），空串表示不约束；支持 {@code ${...}} 占位符
     *
     * @return 业务分区
     */
    String business() default "";

    /**
     * 发布/灰度版本（Dubbo version / Spring 自声明），空串表示不约束；支持 {@code ${...}} 占位符
     *
     * @return 发布版本
     */
    String version() default "";

    /**
     * 无提供者处理类
     *
     * @return 无提供者处理类
     */
    Class<? extends INoProviderHandler> noProvider() default LoggingNoProviderHandler.class;
}
