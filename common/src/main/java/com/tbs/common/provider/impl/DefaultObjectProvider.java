package com.tbs.common.provider.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.registry.DispatchPolicy;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一对象提供器默认实现
 * <p>
 * 门面委派统一注册中心 {@link IRegistryCenter}：单实例/枚举/按来源解析全部读中心条目表，与路由代理共用同一份
 * 来源数据，消除「门面只看到解析链、中心看到全部来源」的过渡期不一致。门面 API 签名保持不变，存量调用方无需改动。
 * 枚举取 {@code enumerable} 条目（按本地优先排序，Spring 在前、Dubbo 逐实例在后）；择一取 {@code routable} 条目
 * （本地 Bean 优先，仅远程时命中 Dubbo 按 business/version 聚合的整体集群引用），与旧解析链 {@code resolveOne} 语义一致。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class DefaultObjectProvider implements IObjectProvider {

    /**
     * 统一注册中心：门面所有读取的底层数据源
     */
    private final IRegistryCenter registryCenter;

    /**
     * 构造统一对象提供器
     *
     * @param registryCenter 统一注册中心，不能为null
     */
    public DefaultObjectProvider(IRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @Override
    public <T> T getObject(Class<T> type) {
        return getObject(type, null);
    }

    /**
     * 解析单个实例（委派中心）
     * <p>
     * beanName 为空时按本地优先取首个 {@code routable} 条目：本地 Bean 优先，仅远程时命中 Dubbo 整体集群引用；
     * beanName 非空时在 {@code enumerable} 条目中按实例标识精确定位。无命中返回 null。
     * </p>
     *
     * @param type     目标类型，不能为null
     * @param beanName 实例标识，可为null
     * @param <T>      目标类型
     * @return 命中的实例，未命中返回 null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> type, String beanName) {
        if (beanName == null || beanName.isEmpty()) {
            return (T) registryCenter.resolveFirst(type, null, null, RoutingStrategy.LOCAL_FIRST)
                .map(RegistryEntry::resolve).orElse(null);
        }
        return (T) enumerableEntries(type).stream()
            .filter(entry -> beanName.equals(entry.instanceKey()))
            .map(RegistryEntry::resolve).findFirst().orElse(null);
    }

    @Override
    public <T> Optional<T> getObjectOptional(Class<T> type) {
        return Optional.ofNullable(getObject(type));
    }

    @Override
    public <T> Optional<T> getObjectOptional(Class<T> type, String beanName) {
        return Optional.ofNullable(getObject(type, beanName));
    }

    @Override
    public <T> Collection<ResolvedObject<T>> getObjects(Class<T> type) {
        return toResolvedObjects(enumerableEntries(type));
    }

    /**
     * 按来源定向解析：取该来源的首个 {@code routable} 条目
     * <p>
     * 以单来源偏好策略定向到指定来源；Spring 返回本地 Bean，Dubbo 返回按 business/version 聚合的整体集群引用，
     * 与旧解析链 {@code resolveOne} 语义一致。
     * </p>
     *
     * @param type   目标类型，不能为null
     * @param source 目标来源，不能为null
     * @param <T>    目标类型
     * @return 该来源的最优解析结果，无命中返回 empty
     */
    @Override
    public <T> Optional<ResolvedObject<T>> getResolvedObject(Class<T> type, ObjectSource source) {
        if (source == null) {
            return Optional.empty();
        }
        return registryCenter.resolveFirst(type, null, null, RoutingStrategy.of(source))
            .map(entry -> this.<T>toResolved(entry));
    }

    /**
     * 读取该接口全部 {@code enumerable} 条目，按本地优先排序（Spring 在前、Dubbo 逐实例在后）
     *
     * @param type 目标类型，不能为null
     * @return 可枚举条目列表
     */
    private List<RegistryEntry> enumerableEntries(Class<?> type) {
        return registryCenter.resolve(type, null, null, DispatchPolicy.ALL, RoutingStrategy.LOCAL_FIRST);
    }

    /**
     * 条目列表映射为解析结果列表
     *
     * @param entries 条目列表
     * @param <T>     目标类型
     * @return 解析结果列表
     */
    private <T> List<ResolvedObject<T>> toResolvedObjects(List<RegistryEntry> entries) {
        return entries.stream().map(entry -> this.<T>toResolved(entry)).collect(Collectors.toList());
    }

    /**
     * 单条目映射为解析结果载体
     *
     * @param entry 注册条目
     * @param <T>   目标类型
     * @return 解析结果
     */
    @SuppressWarnings("unchecked")
    private <T> ResolvedObject<T> toResolved(RegistryEntry entry) {
        return ResolvedObject.of((T) entry.resolve(), entry.coordinate(), entry.instanceKey());
    }
}
