package com.tbs.common.provider;

import org.springframework.core.Ordered;

/**
 * 注册来源 SPI。
 * <p>
 * 多来源实例贡献的扩展契约，是 {@link IObjectResolver} 的继任：一个来源代表「某接口在这些 business/version 下
 * 提供了哪些实例」的提供方。注册中心装配时按 {@link Ordered} 收集并回调全部实现，静态来源（Spring）一次贡献，
 * 动态来源（Dubbo）在此注册后续刷新回调，经 {@link IRegistryCenter#replaceEntriesBySource} 增量刷新条目。
 * </p>
 * <p>
 * 来源扩展只需新增实现并声明其 {@link #source()}，无需改动中心与 {@code @RoutingService}；同一来源存在多个实现时
 * 按 {@link Ordered} 优先级取先者。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRegistrationSource extends Ordered {

    /**
     * 本实现代表的来源标识
     *
     * @return 来源标识，不能为null
     */
    ObjectSource source();

    /**
     * 向注册中心贡献条目
     * <p>
     * 静态来源一次性 {@code register}；动态来源应做首快照并注册变更监听。贡献过程中来源不可用应记录日志并贡献空集，
     * 不得抛出异常打断整体装配。
     * </p>
     *
     * @param center 注册中心，不能为null
     */
    void contribute(IRegistryCenter center);
}
