package com.tbs.common.provider.annotation;

import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.IRouteCoordinator;
import com.tbs.common.provider.impl.LoggingNoProviderHandler;

import java.lang.annotation.*;

/**
 * 多来源路由引用注解（分类装配）
 * <p>
 * 标注在业务字段上，声明「按来源坐标定向装配一个路由代理」：字段类型即目标接口，
 * {@link #business()} 与 {@link #version()} 是业务分区与发布版本两个维度，
 * 由装配器经统一注册中心构建并注入绑定该坐标的路由代理，使同一接口可在不同字段按不同 business/version
 * 分类注入，达成「区分版本与业务分区的分类装配」。
 * </p>
 * <p>
 * 本注解不要求接口预先登记，直接对中心产出按坐标绑定的注入点代理。
 * business 与 version 支持 {@code ${...}} 占位符（经容器 Environment 解析），留空表示该维度不约束；
 * 需要叠加包前缀等额外策略时再声明 {@link #coordinator()}。
 * </p>
 * <p>
 * 装配发生在 Bean 初始化前，字段由代理覆盖写入，无需再叠加 {@code @Resource} / {@code @DubboReference}。
 * 代理不缓存实例，每次调用按坐标取当前可用实例；未命中时交由 {@link #noProvider()} 处理并返回 {@code null}
 * （基本类型返回默认值）。
 * </p>
 * <p>
 * business/version 的分类对本地 Spring Bean 生效依赖来源侧坐标推导：本地实现类可实现
 * {@link com.tbs.common.provider.IRouteMeta} 自声明其 business/version。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
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
     * 候选裁决协调器，缺省为全接受；声明后对本次寻址的每个候选执行
     * <p>
     * 装配时优先取容器内同类型 Bean，缺失则反射无参构造，因此实现类需登记为 Bean 或具备无参构造。
     * </p>
     *
     * @return 协调器类型
     */
    Class<? extends IRouteCoordinator> coordinator() default IRouteCoordinator.Default.class;

    /**
     * 无提供者处理类
     *
     * @return 无提供者处理类
     */
    Class<? extends INoProviderHandler> noProvider() default LoggingNoProviderHandler.class;
}
