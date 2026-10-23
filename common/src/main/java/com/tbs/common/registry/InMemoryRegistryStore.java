package com.tbs.common.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;

/**
 * 注册表存储的进程内默认实现
 * <p>
 * 以 {@link ConcurrentHashMap} 承载「键 → 值列表」路由表，键下值列表用 {@link CopyOnWriteArrayList}，
 * 使读取（分发遍历）与写入（注册/注销）并发互不干扰、迭代无并发修改异常，语义与 MQ v2 消费者注册表一致。
 * 键下值清空后回收该键，避免空桶堆积。
 * </p>
 * <p>
 * 定位为单机/开发开箱即用实现，不含任何外部存储依赖；集群或需持久化时应替换为其他 {@link IRegistryStore} 实现。
 * </p>
 *
 * @param <K> 路由键类型
 * @param <V> 注册值类型
 * @author abstergo
 * @since 1.0.0
 */
public class InMemoryRegistryStore<K, V> implements IRegistryStore<K, V> {

    /**
     * 键到值列表的路由桶表
     */
    private final Map<K, List<V>> buckets = new ConcurrentHashMap<>();

    /**
     * 在指定键下追加一个值，键与值均不可为null（快速失败）
     *
     * @param key   路由键，不能为null
     * @param value 注册值，不能为null
     */
    @Override
    public void add(K key, V value) {
        AssertUtil.isTrue(key == null).i18n(CommonI18nConstant.REGISTRY_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(value == null).i18n(CommonI18nConstant.REGISTRY_VALUE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        buckets.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(value);
    }

    /**
     * 从指定键下移除一个值，桶空则回收该键
     *
     * @param key   路由键，不能为null
     * @param value 待移除的注册值，不能为null
     */
    @Override
    public void remove(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        List<V> bucket = buckets.get(key);
        if (bucket == null) {
            return;
        }
        bucket.remove(value);
        if (bucket.isEmpty()) {
            buckets.remove(key, bucket);
        }
    }

    /**
     * 移除指定键及其下的全部值
     *
     * @param key 路由键，不能为null
     */
    @Override
    public void removeAll(K key) {
        if (key != null) {
            buckets.remove(key);
        }
    }

    /**
     * 回查指定键下的全部值
     *
     * @param key 路由键，允许为null（视作未命中）
     * @return 该键下值的只读视图，未注册时返回空集合
     */
    @Override
    public Collection<V> get(K key) {
        if (key == null) {
            return Collections.emptyList();
        }
        List<V> bucket = buckets.get(key);
        return bucket == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(bucket));
    }

    /**
     * 判断指定键是否已注册
     *
     * @param key 路由键，允许为null
     * @return true 表示该键下至少有一个值
     */
    @Override
    public boolean containsKey(K key) {
        return key != null && buckets.containsKey(key);
    }

    /**
     * 列出全部已注册的键
     *
     * @return 键集合快照，无注册时返回空集合
     */
    @Override
    public Set<K> keys() {
        return new LinkedHashSet<>(buckets.keySet());
    }

    /**
     * 列出全部键下挂载的值（跨键扁平化）
     *
     * @return 值集合快照，无注册时返回空集合
     */
    @Override
    public Collection<V> values() {
        List<V> all = new ArrayList<>();
        buckets.values().forEach(all::addAll);
        return all;
    }

    /**
     * 清空全部注册数据
     */
    @Override
    public void clear() {
        buckets.clear();
    }
}
