package com.tbs.common.provider;

/**
 * 注册条目契约。
 * <p>
 * 一次「某来源在某接口下、某 business/version 上提供了某实例」的登记。唯一身份 = (key, coordinate, instanceKey)， 供注册中心去重与按来源增量替换。{@link #resolve()}
 * 允许返回实际实例或懒解析（首次命中构建并缓存）， 用于承接 Dubbo 直连引用、代理等「命中才建」的昂贵对象。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface RegistryEntry {

    /**
     * 接口类型（注册桶键）
     *
     * @return 目标接口 Class，不能为null
     */
    Class<?> key();

    /**
     * 来源坐标（source + business + version）
     *
     * @return 来源坐标，不能为null
     */
    SourceCoordinate coordinate();

    /**
     * 实例物理定位：Dubbo 为 host:port（或 Dubbo3 instanceId），Spring 为 beanName，整体负载均衡引用固定为 cluster 标识
     *
     * @return 实例标识，不能为null
     */
    String instanceKey();

    /**
     * 同坐标内优先级，值越大越优先
     *
     * @return 优先级
     */
    int order();

    /**
     * 解析为可调用实例，懒解析条目首次调用时构建并缓存
     *
     * @return 实例本体，不能为null
     */
    Object resolve();

    /**
     * 是否作为「枚举视图」条目呈现（逐实例直连引用、本地 Bean 等物理实现）。
     * <p>
     * 与「路由目标」正交：一个条目可仅用于枚举、仅用于路由，或二者皆是（本地 Bean 既可枚举也可直接调用）。
     * 默认视为可枚举，保持存量条目语义不变。
     * </p>
     *
     * @return true 表示该条目应出现在多实现枚举结果中
     */
    default boolean enumerable() {
        return true;
    }

    /**
     * 是否作为「路由目标」条目参与择一解析（{@code resolveFirst}）。
     * <p>
     * Dubbo 来源以按 business/version 聚合的整体集群引用作为路由目标（保留集群负载均衡与 failover），
     * 逐实例直连引用只用于枚举与精确直连，不作为默认路由目标。默认视为可路由，保持存量条目语义不变。
     * </p>
     *
     * @return true 表示该条目可被择一路由命中
     */
    default boolean routable() {
        return true;
    }
}
