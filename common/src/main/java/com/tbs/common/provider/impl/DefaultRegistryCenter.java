package com.tbs.common.provider.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.provider.ILazyEntriesContributor;
import com.tbs.common.provider.IRegistrationSource;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.IRegistryChangeListener;
import com.tbs.common.provider.IRoutingProxyFactory;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.SourceCoordinate;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;
import com.tbs.common.registry.DispatchPolicy;
import com.tbs.common.registry.KeyedRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一注册中心默认实现。
 * <p>
 * 坐落通用 {@link KeyedRegistry}（键=接口类型，多值桶）之上，实现「注册 + 解析 + 分派 + 生命周期」：
 * 读多写少——{@link #resolve} 读取时先触发已登记的 {@link ILazyEntriesContributor} 按需懒填充
 * （静态来源每接口首读贡献一次，动态来源读即重贡献并在同集幂等时跳过换位），动态变更经
 * {@link #replaceEntriesBySource} 事件增量刷新；路由代理作为「通道」而非实现，{@link #register} 予以拒绝，防解析自递归。条目按唯一坐标
 * (接口, {@link SourceCoordinate}, instanceKey) 区分，使同一 host:port 上不同 business/version 的实例各自并存。
 * </p>
 * <p>
 * 解析按条目角色分层：{@link DispatchPolicy#FIRST}（择一路由）只在 {@code routable} 条目中选择，
 * {@link DispatchPolicy#ALL}（枚举/广播）只在 {@code enumerable} 条目中选择；来源偏好与优先级排序统一委派
 * {@link RoutingStrategy#reorder}。代理构建委派 {@link IRoutingProxyFactory}，装配期经 {@link #attachRoutingProxyFactory} 挂接，未挂接时快速失败。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class DefaultRegistryCenter implements IRegistryCenter {

    /**
     * 条目存储：键=接口类型，值=同键下的多个条目（多值），底层进程内并发安全存储
     */
    private final KeyedRegistry<Class<?>, RegistryEntry> table = KeyedRegistry.multiInMemory();

    /**
     * 条目变更监听器集合，迭代无并发修改异常
     */
    private final List<IRegistryChangeListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * 路由代理缓存：按 (接口, 策略, business, version) 复用，避免重复构建
     */
    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    /**
     * 懒条目贡献者集合，装配期登记，读取命中空桶时回调
     */
    private final List<ILazyEntriesContributor> lazyContributors = new CopyOnWriteArrayList<>();

    /**
     * 已完成一次性贡献的 (贡献者, 接口) 对，静态贡献者据此免重复触发
     */
    private final Map<ILazyEntriesContributor, Set<Class<?>>> contributedKeys = new ConcurrentHashMap<>();

    /**
     * 路由代理工厂，装配期经 {@link #attachRoutingProxyFactory} 挂接，未挂接时构建代理快速失败
     */
    private IRoutingProxyFactory proxyFactory;

    /**
     * 构造未装配代理工厂的注册中心，解析与登记可用，代理构建需阶段 3 装配工厂
     */
    public DefaultRegistryCenter() {
        this(null);
    }

    /**
     * 构造注册中心
     *
     * @param proxyFactory 路由代理工厂，可为 null（此时构建代理将快速失败）
     */
    public DefaultRegistryCenter(IRoutingProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 装配期挂接路由代理工厂
     * <p>
     * 供依赖方向与构造顺序解耦：中心 Bean 先创建并完成来源贡献登记，再挂接工厂，避免中心与工厂/代理之间的构造期循环。
     * </p>
     *
     * @param proxyFactory 路由代理工厂，不能为null
     */
    public void attachRoutingProxyFactory(IRoutingProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 登记一条条目
     * <p>
     * 快速失败校验条目非空；命中路由代理实例则拒绝登记（通道≠实现，防自递归），其余直接入桶。
     * </p>
     *
     * @param entry 条目，不能为null
     */
    @Override
    public void register(RegistryEntry entry) {
        AssertUtil.isTrue(entry == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        if (RoutingServiceProxy.isRoutingProxy(entry.resolve())) {
            log.debug("[RegistryCenter] 拒绝登记路由代理条目(通道非实现): {}", entry);
            return;
        }
        table.register(entry.key(), entry);
    }

    /**
     * 登记一个懒条目贡献者
     * <p>
     * 由 {@link IRegistrationSource} 装配链路在容器就绪前调用，重复登记同一实例无效果。
     * </p>
     *
     * @param contributor 懒贡献者，不能为null
     */
    public void addLazyContributor(ILazyEntriesContributor contributor) {
        AssertUtil.isTrue(contributor == null).i18n(CommonI18nConstant.REGISTRY_CONTRIBUTOR_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        if (!lazyContributors.contains(contributor)) {
            lazyContributors.add(contributor);
        }
    }

    /**
     * 整段替换某接口下、某来源的条目
     * <p>
     * 先摘除该接口下属于该来源的旧条目，再登记新条目集，其他来源条目不受影响；随后按变化坐标回调监听器。
     * 新条目中同机不同 business/version 的实例各自并存（身份三元组不同）。
     * 与现存同源条目完全同集时直接跳过（读即重贡献的动态来源不产生无谓换位与监听噪音）。
     * </p>
     *
     * @param source       来源标识，不能为null
     * @param key          接口类型，不能为null
     * @param freshEntries 最新条目集，不能为null
     */
    @Override
    public void replaceEntriesBySource(ObjectSource source, Class<?> key, List<RegistryEntry> freshEntries) {
        AssertUtil.isTrue(source == null).i18n(CommonI18nConstant.REGISTRY_COORDINATE_SOURCE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        List<RegistryEntry> current = table.getAll(key);
        if (sameEntrySet(current, source, freshEntries)) {
            return;
        }
        for (RegistryEntry existing : current) {
            if (source.equals(existing.coordinate().getSource())) {
                table.unregister(key, existing);
            }
        }
        freshEntries.forEach(this::register);
        freshEntries.stream().map(RegistryEntry::coordinate).distinct()
            .forEach(coordinate -> notifyChanged(coordinate, key));
        log.debug("[RegistryCenter] 来源[{}]接口[{}]条目刷新完成, 现存条目数: {}", source.id(), key.getName(),
            table.getAll(key).size());
    }

    /**
     * 解析候选条目并按分派策略返回
     * <p>
     * 读取前先触发按需懒填充：一次性贡献者每接口只回调一次，读即重贡献者每次读取都回调，
     * 同集幂等跳过保证重复读取不产生换位与监听噪音；随后纯内存读。
     * 按角色分层：FIRST（择一路由）取 {@code routable} 条目，ALL（枚举/广播）取 {@code enumerable} 条目；
     * 来源偏好与优先级排序委派 {@link RoutingStrategy#reorder}。
     * </p>
     *
     * @param key      接口类型，不能为null
     * @param business 业务过滤，null/空表示不约束
     * @param version  版本过滤，null/空表示不约束
     * @param policy   分派策略，不能为null
     * @param strategy 来源偏好，不能为null
     * @return 命中条目列表，FIRST 时最多一条，无命中为空列表
     */
    @Override
    public List<RegistryEntry> resolve(Class<?> key, String business, String version, DispatchPolicy policy,
        RoutingStrategy strategy) {
        lazyFill(key);
        var routing = policy == DispatchPolicy.FIRST;
        List<RegistryEntry> candidates = table.getAll(key).stream()
            .filter(entry -> routing ? entry.routable() : entry.enumerable())
            .filter(entry -> dimensionMatches(business, entry.coordinate().getBusiness()))
            .filter(entry -> dimensionMatches(version, entry.coordinate().getVersion()))
            .collect(Collectors.toList());
        List<RegistryEntry> ordered = strategy.reorder(candidates);
        if (routing && ordered.size() > 1) {
            return new ArrayList<>(ordered.subList(0, 1));
        }
        return ordered;
    }

    /**
     * 取首个命中条目
     *
     * @param key      接口类型，不能为null
     * @param business 业务过滤，null/空表示不约束
     * @param version  版本过滤，null/空表示不约束
     * @param strategy 来源偏好，不能为null
     * @return 首个命中条目，无命中为 {@link Optional#empty()}
     */
    @Override
    public Optional<RegistryEntry> resolveFirst(Class<?> key, String business, String version, RoutingStrategy strategy) {
        return resolve(key, business, version, DispatchPolicy.FIRST, strategy).stream().findFirst();
    }

    /**
     * 构建绑定到指定 business/version 的路由代理
     * <p>
     * 委派 {@link IRoutingProxyFactory} 并按 (接口, 策略, business, version) 缓存复用；工厂未装配时快速失败。
     * </p>
     *
     * @param interfaceType 目标接口类型，必须为接口，不能为null
     * @param strategy      来源偏好，不能为null
     * @param business      绑定业务，null/空表示不约束
     * @param version       绑定版本，null/空表示不约束
     * @param <T>           目标接口类型
     * @return 路由代理实例
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRoutingProxy(Class<T> interfaceType, RoutingStrategy strategy, String business, String version) {
        AssertUtil.isTrue(proxyFactory == null).i18n(CommonI18nConstant.REGISTRY_PROXY_FACTORY_ABSENT)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        var cacheKey = buildProxyCacheKey(interfaceType, strategy, business, version);
        return (T) proxyCache.computeIfAbsent(cacheKey,
            key -> proxyFactory.create(interfaceType, this, strategy, business, version));
    }

    /**
     * 注册条目变更监听器
     *
     * @param listener 监听器，不能为null
     */
    @Override
    public void addListener(IRegistryChangeListener listener) {
        AssertUtil.isTrue(listener == null).i18n(CommonI18nConstant.REGISTRY_ENTRY_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        listeners.add(listener);
    }

    /**
     * 当前已登记的全部接口类型
     *
     * @return 键集合，不会为null
     */
    @Override
    public Set<Class<?>> keys() {
        return table.keys();
    }

    /**
     * 触发某接口的按需懒填充
     * <p>
     * 逐个回调已登记的贡献者：一次性贡献者（{@code refreshOnRead()} 为 false）每接口只贡献一次，
     * 由 {@code contributedKeys} 记录免重复；读即重贡献者（{@code refreshOnRead()} 为 true）每次读取都回调，
     * 新鲜度由贡献者自身节流保障，同集幂等由 {@link #replaceEntriesBySource} 跳过换位。
     * 不再以「桶内已有条目」整体短路，使启动后才实例化的懒 Bean 仍能首读命中。
     * 贡献者异常在桥接层吞掉降级为空贡献，不影响读取主线。
     * </p>
     *
     * @param key 接口类型，不能为null
     */
    private void lazyFill(Class<?> key) {
        if (lazyContributors.isEmpty()) {
            return;
        }
        for (ILazyEntriesContributor contributor : lazyContributors) {
            var doneKeys = contributedKeys.computeIfAbsent(contributor,
                c -> ConcurrentHashMap.newKeySet());
            if (!contributor.refreshOnRead() && doneKeys.contains(key)) {
                continue;
            }
            contributor.contributeEntries(this, key);
            doneKeys.add(key);
        }
    }

    /**
     * 判断新条目集与现存同源条目是否同集（基于条目身份三元组等值）
     * <p>
     * 同源现存条目与新集互相包含即视为无变化；同源条目的身份三元组含来源，
     * 新集必须全部属于本源才能与同源旧集相等，无需额外过滤新集。
     * </p>
     *
     * @param current      现存全部条目
     * @param source       替换目标来源
     * @param freshEntries 新条目集
     * @return true 表示集合未变化可跳过替换
     */
    private boolean sameEntrySet(List<RegistryEntry> current, ObjectSource source, List<RegistryEntry> freshEntries) {
        Set<RegistryEntry> currentSameSource = new HashSet<>();
        for (RegistryEntry existing : current) {
            if (source.equals(existing.coordinate().getSource())) {
                currentSameSource.add(existing);
            }
        }
        return currentSameSource.size() == freshEntries.size()
            && currentSameSource.containsAll(freshEntries);
    }

    /**
     * 维度过滤判定：过滤条件为空表示不约束，否则要求逐字相等
     *
     * @param filter 查询侧过滤值，可为null/空
     * @param value  条目侧维度值
     * @return true 表示该维度命中
     */
    private boolean dimensionMatches(String filter, String value) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        return filter.equals(value);
    }

    /**
     * 构建代理缓存键，null business/version 归一为空串
     *
     * @param interfaceType 接口类型，不能为null
     * @param strategy      来源偏好，不能为null
     * @param business      业务，可为null
     * @param version       版本，可为null
     * @return 缓存键
     */
    private String buildProxyCacheKey(Class<?> interfaceType, RoutingStrategy strategy, String business, String version) {
        return interfaceType.getName() + '#' + strategy + '#' + nullToEmpty(business) + '#' + nullToEmpty(version);
    }

    /**
     * null 归一为空串
     *
     * @param value 原值，可为null
     * @return 非null字符串
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 通知监听器条目变更，吞掉监听器异常避免影响刷新主流程
     *
     * @param coordinate 变化坐标，不能为null
     * @param key        变化接口类型，不能为null
     */
    private void notifyChanged(SourceCoordinate coordinate, Class<?> key) {
        for (IRegistryChangeListener listener : listeners) {
            try {
                listener.onEntriesChanged(coordinate, key);
            } catch (Exception e) {
                log.warn("[RegistryCenter] 条目变更监听器执行异常, 坐标: {}, 接口: {}", coordinate, key.getName(), e);
            }
        }
    }
}
