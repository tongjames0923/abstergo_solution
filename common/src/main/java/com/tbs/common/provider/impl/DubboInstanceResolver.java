package com.tbs.common.provider.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import com.tbs.common.config.property.ObjectProviderProperty;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.ObjectResolveContext;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.SourceCoordinate;
import com.tbs.common.provider.impl.support.InterfaceProviderState;
import com.tbs.common.util.framework.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Dubbo 多实例发现解析器
 * <p>
 * 多来源解析链中的 Dubbo 来源实现：给定接口类型，经注册中心查出该接口在集群中的全部服务提供者实例，
 * 并为每个实例构建独立的类型安全直连引用（{@link ReferenceConfig} + setUrl 直连，全程不使用泛化调用），
 * 每个实例均可被独立调用；同时维护一个不带地址的整体引用，供单实例解析返回，由 Dubbo 在实例间负载均衡。
 * 实例发现采用首次解析时懒加载 lookup，快照带时效：空快照（发现早于服务上线）按固定回退间隔重新 lookup，
 * 非空快照按配置间隔过期，使扩容与重启能被后续解析发现；引用缓存复用避免重复订阅建连，
 * 实例下线后其直连引用随即销毁，进程关闭时统一释放全部引用（DisposableBean）。
 * 按实例标识（host:port）解析时只返回匹配实例，不会降级到其他实例或整体引用。
 * 阶段 3 增量：提供者条目的来源坐标携带 group→business（未声明 group 兜底 default）与 version，
 * 直连引用按「坐标+地址」去重缓存，修复同一 host:port 上不同 business/version  provider 被合并吐掉的碰撞。
 * 阶段 3 起经 {@link ObjectResolverAsSource} 桥接为注册中心的读即重贡献来源，本类作为存量实现保留。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see InterfaceProviderState 接口级实例状态
 */
@Slf4j
@Deprecated
public class DubboInstanceResolver implements IObjectResolver, DisposableBean {

    /**
     * 注册中心地址缺失协议时的默认协议
     */
    private static final String DEFAULT_REGISTRY_PROTOCOL = "dubbo";

    /**
     * 接口全限定名到该接口实例状态的缓存
     */
    private final ConcurrentMap<String, InterfaceProviderState<?>> interfaceStates = new ConcurrentHashMap<>();

    /**
     * 对象提供器配置
     */
    private final ObjectProviderProperty property;

    /**
     * 构造 Dubbo 实例发现解析器
     *
     * @param property 对象提供器配置，不能为null
     */
    public DubboInstanceResolver(ObjectProviderProperty property) {
        this.property = property;
    }

    /**
     * 获取 Dubbo 应用配置
     * <p>
     * 优先复用容器内已有的 ApplicationConfig Bean，避免脱离当前应用配置；存在多个时取第一个。
     * </p>
     *
     * @return 应用配置，容器内不存在时为 empty
     */
    private Optional<ApplicationConfig> findApplicationConfig() {
        try {
            Map<String, ApplicationConfig> configs = SpringUtil.getBeansOfType(ApplicationConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Optional.empty();
            }
            return configs.values().stream().findFirst();
        } catch (Exception e) {
            log.debug("获取ApplicationConfig失败", e);
            return Optional.empty();
        }
    }

    /**
     * 获取 Dubbo 注册中心配置
     * <p>
     * 容器内可能存在多个 RegistryConfig Bean，取第一个；不存在时为 empty。
     * </p>
     *
     * @return 注册中心配置，容器内不存在时为 empty
     */
    private Optional<RegistryConfig> findRegistryConfig() {
        try {
            Map<String, RegistryConfig> configs = SpringUtil.getBeansOfType(RegistryConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Optional.empty();
            }
            return configs.values().stream().findFirst();
        } catch (Exception e) {
            log.debug("获取RegistryConfig失败", e);
            return Optional.empty();
        }
    }

    /**
     * 构建注册中心 URL
     * <p>
     * RegistryConfig 地址未携带协议前缀时补充默认协议。
     * </p>
     *
     * @param registryConfig 注册中心配置，不能为null
     * @return 注册中心 URL
     */
    private URL buildRegistryUrl(RegistryConfig registryConfig) {
        var address = registryConfig.getAddress();
        if (address != null && !address.contains("://")) {
            address = DEFAULT_REGISTRY_PROTOCOL + "://" + address;
        }
        return URL.valueOf(address);
    }

    /**
     * 构建接口 providers 目录的查询 URL
     * <p>
     * 订阅地址形如 dubbo://consumerHost/接口全限定名?interface=接口全限定名&category=providers。
     * </p>
     *
     * @param interfaceName 接口全限定名，不能为null
     * @return 查询 URL
     */
    private URL buildLookupUrl(String interfaceName) {
        var localHost = org.apache.dubbo.common.utils.NetUtils.getLocalHost();
        return URL.valueOf("dubbo://" + localHost + "/" + interfaceName)
            .addParameter(CommonConstants.INTERFACE_KEY, interfaceName)
            .addParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.DEFAULT_CATEGORY)
            .addParameter(CommonConstants.CHECK_KEY, String.valueOf(false));
    }

    /**
     * 从注册中心拉取接口的全部服务提供者实例
     * <p>
     * 任何失败（配置缺失、注册中心不可达）均降级为空列表并记录 warn 日志，不影响其他来源解析。
     * </p>
     *
     * @param interfaceType 接口类型，不能为null
     * @param <T>           接口类型
     * @return 提供者实例 URL 列表，不会为null
     */
    private <T> List<URL> lookupProviderUrls(Class<T> interfaceType) {
        var registryConfigOptional = findRegistryConfig();
        if (registryConfigOptional.isEmpty()) {
            log.warn("未发现Dubbo注册中心配置(RegistryConfig)，跳过接口[{}]的实例发现", interfaceType.getName());
            return Collections.emptyList();
        }
        try {
            var registryFactory = ApplicationModel.defaultModel().getExtensionLoader(org.apache.dubbo.registry.RegistryFactory.class)
                .getAdaptiveExtension();
            var registry = registryFactory.getRegistry(buildRegistryUrl(registryConfigOptional.get()));
            var providers = registry.lookup(buildLookupUrl(interfaceType.getName()));
            if (providers == null) {
                return Collections.emptyList();
            }
            return providers.stream()
                .filter(url -> !RegistryConstants.EMPTY_PROTOCOL.equals(url.getProtocol()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Dubbo接口[{}]实例发现失败, 原因: {}", interfaceType.getName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 计算提供者实例 URL 的实例标识
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 实例标识（host:port）
     */
    private String instanceKeyOf(URL providerUrl) {
        return String.format(ProviderConstant.DUBBO_INSTANCE_KEY_FORMAT, providerUrl.getHost(), providerUrl.getPort());
    }

    /**
     * 构建提供者实例的来源坐标
     * <p>
     * group 承载 business，未声明 group 时兜底 default（避免同机默认 provider 与显式业务 provider 碰撞）；
     * URL 未声明 version 时为空串表示不约束。
     * </p>
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 来源坐标（DUBBO + business + version）
     */
    private SourceCoordinate coordinateOf(URL providerUrl) {
        var group = providerUrl.getParameter(CommonConstants.GROUP_KEY);
        var business = StringUtils.hasText(group) ? group : ProviderConstant.DEFAULT_BUSINESS;
        var version = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        return SourceCoordinate.of(ObjectSource.DUBBO, business, version == null ? "" : version);
    }

    /**
     * 计算直连引用的去重键
     * <p>
     * 同一物理地址上不同 business/version 的 provider 必须各自持有独立引用，去重键 = 坐标 + 实例标识，
     * 仅用 host:port 会把同机多业务 provider 合并成一个引用（历史碰撞 bug 的根因）。
     * </p>
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 引用去重键
     */
    private String referenceDedupeKeyOf(URL providerUrl) {
        var coordinate = coordinateOf(providerUrl);
        return coordinate.getBusiness() + '#' + coordinate.getVersion() + '#' + instanceKeyOf(providerUrl);
    }

    /**
     * 获取或初始化接口的实例状态，并在快照过期时重新发现实例
     * <p>
     * 空快照按固定回退间隔重查，因此服务后上线场景下不会被首次发现结果永久锁死，
     * 无可用 provider 时也不会每次解析都打注册中心；刷新后同步回收已下线实例的直连引用。
     * </p>
     *
     * @param interfaceType 接口类型，不能为null
     * @param <T>           接口类型
     * @return 接口实例状态
     */
    @SuppressWarnings("unchecked")
    private <T> InterfaceProviderState<T> getState(Class<T> interfaceType) {
        var refreshMillis = property.getDubboDiscoveryRefreshMillis();
        var state = (InterfaceProviderState<T>) interfaceStates.computeIfAbsent(interfaceType.getName(),
            name -> new InterfaceProviderState<>(interfaceType));
        if (!state.isStale(refreshMillis)) {
            return state;
        }
        state.refresh(lookupProviderUrls(interfaceType), refreshMillis);
        var aliveKeys = state.getProviderUrls().stream().map(this::referenceDedupeKeyOf).collect(Collectors.toSet());
        state.retainInstanceReferences(aliveKeys);
        log.debug("[DubboResolver] 接口[{}]实例发现刷新完成, 提供者实例数: {}", interfaceType.getName(),
            state.getProviderUrls().size());
        return state;
    }

    /**
     * 构建指向指定实例的直连引用
     * <p>
     * 直连协议取自提供者实例 URL 自身（如 dubbo/tri），不能硬编码：
     * 提供方可能以 Triple 协议导出，协议不匹配时直连引用构建成功但调用必然失败。
     * </p>
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @param interfaceType 接口类型，不能为null
     * @param <T>         接口类型
     * @return 直连引用
     */
    private <T> ReferenceConfig<T> buildInstanceReference(URL providerUrl, Class<T> interfaceType) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceType);
        reference.setUrl(providerUrl.getProtocol() + "://" + providerUrl.getHost() + ":" + providerUrl.getPort());
        reference.setTimeout(property.getDubboTimeout());
        reference.setCheck(false);
        var group = providerUrl.getParameter(CommonConstants.GROUP_KEY);
        if (StringUtils.hasText(group)) {
            reference.setGroup(group);
        }
        var version = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        if (StringUtils.hasText(version)) {
            reference.setVersion(version);
        }
        findApplicationConfig().ifPresent(reference::setApplication);
        return reference;
    }

    /**
     * 构建整体负载均衡引用
     * <p>
     * 不指定实例地址，由 Dubbo 依据注册中心在全部实例间负载均衡。
     * 传入坐标时按 business/version 限定该引用（group=非默认 business，version=坐标版本），使路由可按维度定向；
     * 传 null 表示不限定（匹配无分组、无版本的默认 provider）。
     * </p>
     *
     * @param interfaceType 接口类型，不能为null
     * @param coordinate    来源坐标，可为null表示不限定
     * @param <T>           接口类型
     * @return 整体引用
     */
    private <T> ReferenceConfig<T> buildClusterReference(Class<T> interfaceType, SourceCoordinate coordinate) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceType);
        reference.setTimeout(property.getDubboTimeout());
        reference.setCheck(false);
        if (coordinate != null) {
            var business = coordinate.getBusiness();
            if (StringUtils.hasText(business) && !ProviderConstant.DEFAULT_BUSINESS.equals(business)) {
                reference.setGroup(business);
            }
            if (StringUtils.hasText(coordinate.getVersion())) {
                reference.setVersion(coordinate.getVersion());
            }
        }
        findApplicationConfig().ifPresent(reference::setApplication);
        findRegistryConfig().ifPresent(reference::setRegistry);
        return reference;
    }

    @Override
    public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
        if (!property.isDubboDiscoveryEnabled()) {
            return Collections.emptyList();
        }
        var state = getState(context.getTargetType());
        var providerUrls = state.getProviderUrls();
        if (providerUrls.isEmpty()) {
            return Collections.emptyList();
        }
        List<ResolvedObject<T>> result = new ArrayList<>(providerUrls.size());
        for (URL providerUrl : providerUrls) {
            var instanceKey = instanceKeyOf(providerUrl);
            if (!context.matchesInstanceKey(instanceKey)) {
                continue;
            }
            var coordinate = coordinateOf(providerUrl);
            try {
                var reference = state.getOrCreateInstanceReference(referenceDedupeKeyOf(providerUrl),
                    () -> buildInstanceReference(providerUrl, context.getTargetType()));
                result.add(ResolvedObject.of(reference.get(), coordinate, instanceKey));
            } catch (Exception e) {
                log.warn("构建Dubbo实例[{}]直连引用失败, 接口: {}, 原因: {}", instanceKey, context.getTargetType().getName(),
                    e.getMessage());
            }
        }
        log.debug("[DubboResolver] 接口[{}]枚举Dubbo实例 {} 个", context.getTargetType().getName(), result.size());
        return result;
    }

    /**
     * 解析单个实例（整体负载均衡引用）
     * <p>
     * 覆写默认实现：未指定实例标识时返回由 Dubbo 负载均衡的整体引用而非钉死首个实例，
     * 避免流量全部打到发现列表第一个节点且失去 failover 能力；
     * 指定实例标识时只返回该实例；整体引用构建失败时降级为实例列表首个直连实例。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 单个可用实例
     */
    @Override
    public <T> Optional<ResolvedObject<T>> resolveOne(ObjectResolveContext<T> context) {
        if (!property.isDubboDiscoveryEnabled() || context.hasBeanName()) {
            return IObjectResolver.super.resolveOne(context);
        }
        var state = getState(context.getTargetType());
        if (state.getProviderUrls().isEmpty()) {
            log.debug("[DubboResolver] 接口[{}]当前无可用Dubbo提供者", context.getTargetType().getName());
            return Optional.empty();
        }
        try {
            var reference = state.getOrCreateClusterReference(ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY,
                () -> buildClusterReference(context.getTargetType(), null));
            return Optional.of(ResolvedObject.of(reference.get(), ObjectSource.DUBBO,
                ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY));
        } catch (Exception e) {
            log.warn("构建Dubbo整体负载均衡引用失败, 接口: {}, 原因: {}", context.getTargetType().getName(), e.getMessage());
            return IObjectResolver.super.resolveOne(context);
        }
    }

    /**
     * 解析路由目标：按 business/version 坐标聚合的整体集群引用
     * <p>
     * 同一坐标下的多个物理实例收敛为一个限定到该坐标的集群引用（不设地址，由 Dubbo 在该坐标的实例间负载均衡），
     * 使择一路由既不钉死单机、又保留 failover；business 为兜底 default 时不附加 group（匹配无分组 provider）。
     * </p>
     *
     * @param context 解析上下文，不能为null
     * @param <T>     目标实例类型
     * @return 每个存活坐标一个集群引用结果
     */
    @Override
    public <T> Collection<ResolvedObject<T>> resolveRoutableTargets(ObjectResolveContext<T> context) {
        if (!property.isDubboDiscoveryEnabled()) {
            return Collections.emptyList();
        }
        var state = getState(context.getTargetType());
        var providers = state.getProviderUrls();
        if (providers.isEmpty()) {
            return Collections.emptyList();
        }
        Map<SourceCoordinate, URL> firstUrlByCoordinate = new java.util.LinkedHashMap<>();
        for (URL providerUrl : providers) {
            firstUrlByCoordinate.putIfAbsent(coordinateOf(providerUrl), providerUrl);
        }
        List<ResolvedObject<T>> result = new ArrayList<>(firstUrlByCoordinate.size());
        firstUrlByCoordinate.forEach((coordinate, providerUrl) -> {
            try {
                var reference = state.getOrCreateClusterReference(clusterKeyOf(coordinate),
                    () -> buildClusterReference(context.getTargetType(), coordinate));
                result.add(ResolvedObject.of(reference.get(), coordinate, ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY));
            } catch (Exception e) {
                log.warn("构建Dubbo坐标[{}]集群引用失败, 接口: {}, 原因: {}", coordinate, context.getTargetType().getName(),
                    e.getMessage());
            }
        });
        return result;
    }

    /**
     * 计算集群引用的坐标去重缓存键
     *
     * @param coordinate 来源坐标，不能为null
     * @return 坐标键
     */
    private String clusterKeyOf(SourceCoordinate coordinate) {
        return coordinate.getBusiness() + '#' + coordinate.getVersion();
    }

    @Override
    public ObjectSource getSource() {
        return ObjectSource.DUBBO;
    }

    @Override
    public int getOrder() {
        return ProviderConstant.ORDER_DUBBO_RESOLVER;
    }

    /**
     * 释放全部 Dubbo 引用
     * <p>
     * 进程关闭时销毁全部整体引用与实例直连引用，取消注册中心订阅占用的连接资源。
     * </p>
     */
    @Override
    public void destroy() {
        interfaceStates.values().forEach(state -> {
            try {
                state.destroy();
            } catch (Exception e) {
                log.warn("销毁Dubbo引用失败, 接口: {}", state.getInterfaceType().getName(), e);
            }
        });
        interfaceStates.clear();
    }
}
