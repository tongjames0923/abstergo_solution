package com.tbs.common.provider.impl.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ReferenceConfig;

import com.tbs.common.constant.ProviderConstant;

import lombok.Getter;

/**
 * 接口级服务提供者实例状态
 * <p>
 * 缓存单个 Dubbo 接口在注册中心发现的提供者实例快照，以及对应的实例直连引用与整体负载均衡引用。
 * 快照带时效：空快照（首次发现早于服务上线）按固定回退间隔重新 lookup，
 * 非空快照按配置间隔过期，使服务扩容/重启能被后续解析发现，避免发现结果被永久固化；
 * 回退间隔同时避免了无可用 provider 时每次解析都打注册中心。
 * 引用按需构建并缓存复用，避免重复建连与注册中心订阅开销，实例下线后其直连引用随之销毁。
 * 统一提供销毁入口用于进程关闭时释放全部引用资源。
 * </p>
 *
 * @param <T> 接口类型
 * @author abstergo
 * @since 1.0.0
 */
public class InterfaceProviderState<T> {

    /**
     * 空快照的重新发现回退间隔（毫秒），与配置间隔无关
     */
    private static final long EMPTY_SNAPSHOT_RETRY_MILLIS = ProviderConstant.EMPTY_DISCOVERY_RETRY_MILLIS;

    /**
     * 接口类型
     */
    @Getter
    private final Class<T> interfaceType;

    /**
     * 提供者实例 URL 快照，随注册中心发现结果刷新
     */
    @Getter
    private volatile List<URL> providerUrls = Collections.emptyList();

    /**
     * 最近一次发现尝试的时间戳（毫秒），0 表示尚未尝试过
     */
    private volatile long lastRefreshAt;

    /**
     * 整体负载均衡引用缓存，键为坐标去重键（同一接口下不同 business/version 各持一个限定的集群引用）
     */
    private final ConcurrentMap<String, ReferenceConfig<T>> clusterReferences = new ConcurrentHashMap<>();

    /**
     * 实例直连引用缓存，键为「坐标+地址」去重键（同机不同 business/version 各自独立引用）
     */
    private final ConcurrentMap<String, ReferenceConfig<T>> instanceReferences = new ConcurrentHashMap<>();

    /**
     * 构造接口实例状态
     *
     * @param interfaceType 接口类型，不能为null
     */
    public InterfaceProviderState(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    /**
     * 判断实例快照是否已过期需要重查
     * <p>
     * 空快照按固定回退间隔重查：首次发现早于服务上线或注册中心抖动返回空时，
     * 若固化空结果该接口来源将永久不可用，但若也不加间隔则会在无 provider 环境下每次解析都打注册中心；
     * 固定间隔不受配置间隔影响，即使配置为「不主动过期」仍能发现后上线的服务。
     * </p>
     * <p>
     * ponytail: 这里用「按间隔主动 lookup」代替注册中心 NotifyListener 订阅，
     * 上限是实例变化最迟在一个间隔后被发现；升级路径是改为定向订阅并回调刷新快照。
     * </p>
     *
     * @param refreshMillis 非空快照的刷新间隔（毫秒），非正数表示非空快照不再主动过期
     * @return true 表示应重新向注册中心发现实例
     */
    public boolean isStale(long refreshMillis) {
        var elapsed = System.currentTimeMillis() - lastRefreshAt;
        if (providerUrls.isEmpty()) {
            return elapsed >= EMPTY_SNAPSHOT_RETRY_MILLIS;
        }
        return refreshMillis > 0 && elapsed >= refreshMillis;
    }

    /**
     * 应用一次新的实例发现结果
     * <p>
     * 二次校验过期状态，避免并发下用较旧的发现结果覆盖新结果；未过期时不做任何变更。
     * 无论是否发现到实例都记录尝试时间，作为空快照的回退基准。
     * </p>
     *
     * @param providerUrls  本次发现的实例 URL 列表，允许为null或空
     * @param refreshMillis 刷新间隔（毫秒）
     */
    public synchronized void refresh(List<URL> providerUrls, long refreshMillis) {
        if (!isStale(refreshMillis)) {
            return;
        }
        this.providerUrls = providerUrls == null ? Collections.emptyList() : List.copyOf(providerUrls);
        lastRefreshAt = System.currentTimeMillis();
    }

    /**
     * 获取或创建指定实例的直连引用
     *
     * @param dedupeKey 实例引用去重键（坐标+地址），不能为null
     * @param factory   引用构建工厂，不能为null
     * @return 直连引用
     */
    public ReferenceConfig<T> getOrCreateInstanceReference(String dedupeKey, Supplier<ReferenceConfig<T>> factory) {
        return instanceReferences.computeIfAbsent(dedupeKey, key -> factory.get());
    }

    /**
     * 获取或创建指定坐标的整体负载均衡引用
     * <p>
     * 按坐标去重键缓存，使同一接口的不同 business/version 各自持有限定的集群引用，路由时按维度定向命中。
     * </p>
     *
     * @param coordinateKey 集群引用坐标去重键，不能为null
     * @param factory       引用构建工厂，不能为null
     * @return 整体引用
     */
    public ReferenceConfig<T> getOrCreateClusterReference(String coordinateKey, Supplier<ReferenceConfig<T>> factory) {
        return clusterReferences.computeIfAbsent(coordinateKey, key -> factory.get());
    }

    /**
     * 回收已下线实例的直连引用
     * <p>
     * 快照刷新后调用，销毁不再存在的实例引用，避免服务重启换端口后引用与连接无限堆积。
     * </p>
     *
     * @param aliveDedupeKeys 当前存活实例的引用去重键集合，不能为null
     */
    public void retainInstanceReferences(Collection<String> aliveDedupeKeys) {
        var deadKeys = new ArrayList<>(instanceReferences.keySet());
        deadKeys.removeAll(aliveDedupeKeys);
        deadKeys.forEach(key -> {
            var reference = instanceReferences.remove(key);
            if (reference != null) {
                destroyQuietly(reference);
            }
        });
    }

    /**
     * 销毁单条引用，失败不影响其余引用释放
     *
     * @param reference 待销毁引用，不能为null
     */
    private void destroyQuietly(ReferenceConfig<T> reference) {
        try {
            reference.destroy();
        } catch (Exception ignored) {
            // 引用销毁失败不阻断其余引用释放
        }
    }

    /**
     * 销毁全部引用
     * <p>
     * 依次销毁整体引用与全部实例直连引用，单条销毁失败不影响其余引用释放。
     * </p>
     */
    public void destroy() {
        clusterReferences.values().forEach(this::destroyQuietly);
        clusterReferences.clear();
        instanceReferences.values().forEach(this::destroyQuietly);
        instanceReferences.clear();
    }
}
