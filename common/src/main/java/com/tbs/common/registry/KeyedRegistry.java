package com.tbs.common.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;

/**
 * 框架级键式注册表门面
 * <p>
 * 收敛各模块手写「键 → 处理器集合」的注册/查找/分派样板（如 MQ 消费者路由、登录授权提供者、负载转换器），
 * 底层委托 {@link IRegistryStore} 存储扩展位，本身只负责编排两层语义：
 * </p>
 * <ul>
 *     <li><b>唯一性约束</b>：{@code unique=true} 时同一键只允许挂载一个值，重复注册快速失败（fail-fast），
 *     对应「按类型精确路由到唯一实现」的场景（登录授权、转换器）；{@code unique=false} 时同键多值，
 *     对应「一对多广播分派」的场景（消费者路由）。</li>
 *     <li><b>取键方式</b>：支持显式传键注册（{@link #register(Object, Object)}），
 *     也支持由值反推导键注册（{@link #register(Object, Function)}），后者适配容器收集一批实现后按各自声明的键建表。</li>
 * </ul>
 * <p>
 * 默认进程内运行，通过静态工厂 {@link #multiInMemory()}/{@link #uniqueInMemory()}/{@link #uniqueFrom} 开箱即用；
 * 如需换存储，构造时注入自定义 {@link IRegistryStore} 即可，本门面逻辑不变。
 * </p>
 *
 * @param <K> 路由键类型
 * @param <V> 注册值类型
 * @author abstergo
 * @since 1.0.0
 */
public class KeyedRegistry<K, V> {

    /**
     * 底层存储扩展位
     */
    private final IRegistryStore<K, V> store;

    /**
     * 是否要求同一键下值唯一
     */
    private final boolean unique;

    /**
     * 构造注册表门面
     *
     * @param store  底层存储实现，不能为null
     * @param unique true 表示同一键仅允许一个值，重复注册快速失败
     */
    public KeyedRegistry(IRegistryStore<K, V> store, boolean unique) {
        AssertUtil.isTrue(store == null).i18n(CommonI18nConstant.REGISTRY_STORE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        this.store = store;
        this.unique = unique;
    }

    /**
     * 以显式键注册一个值
     *
     * @param key   路由键，不能为null
     * @param value 注册值，不能为null
     */
    public void register(K key, V value) {
        AssertUtil.isTrue(unique && store.containsKey(key)).i18n(CommonI18nConstant.REGISTRY_DUPLICATE_KEY)
            .params(key).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        store.add(key, value);
    }

    /**
     * 由值反推导键并注册一个值
     *
     * @param value       注册值，不能为null
     * @param keyExtractor 从值提取路由键的函数，不能为null
     */
    public void register(V value, Function<V, K> keyExtractor) {
        AssertUtil.isTrue(keyExtractor == null).i18n(CommonI18nConstant.REGISTRY_KEY_EXTRACTOR_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        if (value == null) {
            return;
        }
        register(keyExtractor.apply(value), value);
    }

    /**
     * 注销指定键下的一个值
     *
     * @param key   路由键
     * @param value 待注销的注册值
     */
    public void unregister(K key, V value) {
        store.remove(key, value);
    }

    /**
     * 由值反推导键并注销该值
     *
     * @param value        待注销的注册值
     * @param keyExtractor 从值提取路由键的函数，不能为null
     */
    public void unregister(V value, Function<V, K> keyExtractor) {
        AssertUtil.isTrue(keyExtractor == null).i18n(CommonI18nConstant.REGISTRY_KEY_EXTRACTOR_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        if (value == null) {
            return;
        }
        store.remove(keyExtractor.apply(value), value);
    }

    /**
     * 按键取第一个值（单值语义）
     *
     * @param key 路由键
     * @return 命中的值，未注册时为 {@link Optional#empty()}
     */
    public Optional<V> getFirst(K key) {
        Collection<V> values = store.get(key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }

    /**
     * 按键取全部值（多值语义）
     *
     * @param key 路由键
     * @return 该键下的值列表，未注册时返回空列表
     */
    public List<V> getAll(K key) {
        return new ArrayList<>(store.get(key));
    }

    /**
     * 判断指定键是否已注册
     *
     * @param key 路由键
     * @return true 表示该键下至少有一个值
     */
    public boolean supports(K key) {
        return store.containsKey(key);
    }

    /**
     * 列出全部已注册的键
     *
     * @return 键集合快照
     */
    public Set<K> keys() {
        return store.keys();
    }

    /**
     * 列出全部已注册的值（跨键扁平化）
     *
     * @return 值集合快照
     */
    public Collection<V> values() {
        return store.values();
    }

    /**
     * 是否要求同一键下值唯一
     *
     * @return true 表示唯一模式
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * 构建进程内多值注册表（一对多广播分派场景）
     *
     * @param <K> 路由键类型
     * @param <V> 注册值类型
     * @return 允许同键多值的注册表
     */
    public static <K, V> KeyedRegistry<K, V> multiInMemory() {
        return new KeyedRegistry<>(new InMemoryRegistryStore<>(), false);
    }

    /**
     * 构建进程内单值注册表（按键精确路由到唯一实现场景）
     *
     * @param <K> 路由键类型
     * @param <V> 注册值类型
     * @return 同一键重复注册即快速失败的注册表
     */
    public static <K, V> KeyedRegistry<K, V> uniqueInMemory() {
        return new KeyedRegistry<>(new InMemoryRegistryStore<>(), true);
    }

    /**
     * 由一批值构建进程内单值注册表，各自按提取器声明的键入表（适配容器收集实现后建表）
     *
     * @param values       待注册的值集合，可为空（此时返回空表）
     * @param keyExtractor 从值提取路由键的函数，不能为null
     * @param <K>          路由键类型
     * @param <V>          注册值类型
     * @return 构建完成的单值注册表，键冲突时快速失败
     */
    public static <K, V> KeyedRegistry<K, V> uniqueFrom(Collection<V> values, Function<V, K> keyExtractor) {
        KeyedRegistry<K, V> registry = uniqueInMemory();
        if (CollUtil.isEmpty(values)) {
            return registry;
        }
        for (V value : values) {
            registry.register(value, keyExtractor);
        }
        return registry;
    }
}
