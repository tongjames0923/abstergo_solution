package com.tbs.common.provider;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tbs.common.registry.DispatchPolicy;

/**
 * 统一注册中心门面。
 * <p>
 * 收敛「注册 + 解析 + 分派 + 生命周期」：条目按唯一坐标 (接口 key, {@link SourceCoordinate}, instanceKey) 登记，
 * 内部坐落通用键式注册表。读多写少——调用期 {@link #resolve} 为纯内存读，动态来源经
 * {@link #replaceEntriesBySource} 事件增量刷新；路由代理作为「通道」而非实现，不作为条目登记（防解析自递归）。
 * </p>
 * <p>
 * business/version 为可选过滤维度：传 null 或空串表示不约束该维度。本接口为阶段 1 契约声明，实现与接线在后续阶段落地。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRegistryCenter {

    /**
     * 登记一条条目，路由代理类实例将被拒绝
     *
     * @param entry 条目，不能为null
     */
    void register(RegistryEntry entry);

    /**
     * 整段替换某接口下、某来源的条目（动态增量刷新）
     * <p>
     * 先摘除该接口下属于该来源的全部旧条目，再登记新条目集，使同机不同 business/version 的实例各自并存、互不覆盖。
     * </p>
     *
     * @param source       来源标识，不能为null
     * @param key          接口类型，不能为null
     * @param freshEntries 最新条目集，不能为null
     */
    void replaceEntriesBySource(ObjectSource source, Class<?> key, List<RegistryEntry> freshEntries);

    /**
     * 解析候选条目并按分派策略返回，结果已按来源偏好与条目优先级排序
     *
     * @param key      接口类型，不能为null
     * @param business 业务过滤，null/空表示不约束
     * @param version  版本过滤，null/空表示不约束
     * @param policy   分派策略，不能为null
     * @param strategy 来源偏好，不能为null
     * @return 命中条目列表，FIRST 时最多一条，无命中为空列表，不会为null
     */
    List<RegistryEntry> resolve(Class<?> key, String business, String version, DispatchPolicy policy,
        RoutingStrategy strategy);

    /**
     * 取首个命中条目，为 FIRST 分派的便捷方法
     *
     * @param key      接口类型，不能为null
     * @param business 业务过滤，null/空表示不约束
     * @param version  版本过滤，null/空表示不约束
     * @param strategy 来源偏好，不能为null
     * @return 首个命中条目，无命中为 {@link Optional#empty()}
     */
    Optional<RegistryEntry> resolveFirst(Class<?> key, String business, String version, RoutingStrategy strategy);

    /**
     * 构建绑定到指定 business/version 的路由代理，按 (接口, 策略, business, version) 缓存
     *
     * @param interfaceType 目标接口类型，必须为接口，不能为null
     * @param strategy      来源偏好，不能为null
     * @param business      绑定业务，null/空表示不约束
     * @param version       绑定版本，null/空表示不约束
     * @param <T>           目标接口类型
     * @return 路由代理实例
     */
    <T> T getRoutingProxy(Class<T> interfaceType, RoutingStrategy strategy, String business, String version);

    /**
     * 注册条目变更监听器
     *
     * @param listener 监听器，不能为null
     */
    void addListener(IRegistryChangeListener listener);

    /**
     * 当前已登记的全部接口类型
     *
     * @return 键集合，不会为null
     */
    Set<Class<?>> keys();
}
