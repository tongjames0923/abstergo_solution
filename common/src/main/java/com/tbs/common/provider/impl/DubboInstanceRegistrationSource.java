package com.tbs.common.provider.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.DisposableBean;

import com.tbs.common.config.property.ObjectProviderProperty;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.ILazyEntriesContributor;
import com.tbs.common.provider.IRegistrationSource;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.SourceCoordinate;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Dubbo 逐实例枚举来源。
 * <p>
 * 恢复 Dubbo 在注册中心中的「枚举视图」半边：给定接口类型，经注册中心查出该接口的全部提供者实例，
 * 为每个实例构建独立直连引用（{@link ReferenceConfig} + 直连地址，非泛化调用），落为
 * {@code enumerable=true / routable=false} 的条目，使 {@code IObjectProvider.getObjects} 与
 * {@code resolve(..., ALL)} 能列出远程实例；择一路由仍由 {@link DubboRemoteTargetResolver} 承担，
 * 两者身份不同、同一接口桶内并存互不干扰。
 * </p>
 * <p>
 * 本来源为动态来源：中心渲染枚举时回调，按 {@code dubboDiscoveryRefreshMillis} 节流后重新发现，
 * 扩容与下线在一个刷新间隔内被后续读取发现；直连引用按「接口 + 坐标 + 地址」缓存复用，
 * 实例下线后其引用随即销毁，进程关闭时统一释放（{@link DisposableBean}）。
 * 发现失败（无 RegistryConfig、注册中心不可达）降级为空贡献并记录日志，不打断中心读取。
 * </p>
 * <p>
 * 依赖 provider 以接口级注册（{@code dubbo.application.register-mode} 含 interface），
 * 仅应用级注册时查询不到实例，条目为空——此时路由仍可用，只是枚举列表不含远程实例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see DubboRemoteTargetResolver 同一接口的择一路由半边
 */
@Slf4j
public class DubboInstanceRegistrationSource implements IRegistrationSource, ILazyEntriesContributor, DisposableBean {

    /**
     * 注册中心地址缺失协议时的默认协议
     */
    private static final String DEFAULT_REGISTRY_PROTOCOL = "dubbo";

    /**
     * 引用缓存键分隔符
     */
    private static final String KEY_DELIMITER = "#";

    /**
     * 接口全限定名到上次发现时刻的映射，用于按配置间隔节流重新发现
     */
    private final ConcurrentMap<String, Long> lastDiscoverAt = new ConcurrentHashMap<>();

    /**
     * 「接口 + 坐标 + 地址」到实例直连引用的缓存
     */
    private final ConcurrentMap<String, ReferenceConfig<?>> instanceReferences = new ConcurrentHashMap<>();

    /**
     * 对象提供器配置
     */
    private final ObjectProviderProperty property;

    /**
     * 构造 Dubbo 逐实例枚举来源
     *
     * @param property 对象提供器配置，不能为null
     */
    public DubboInstanceRegistrationSource(ObjectProviderProperty property) {
        this.property = property;
    }

    @Override
    public ObjectSource source() {
        return ObjectSource.DUBBO;
    }

    @Override
    public int getOrder() {
        return ProviderConstant.ORDER_DUBBO_RESOLVER;
    }

    @Override
    public boolean refreshOnRead() {
        return true;
    }

    /**
     * 登记为注册中心的懒贡献者
     * <p>
     * 装配期只登记不发现，实例查询由中心首次读取驱动，避免启动期依赖注册中心可达。
     * </p>
     *
     * @param center 注册中心，不能为null
     */
    @Override
    public void contribute(IRegistryCenter center) {
        if (center instanceof DefaultRegistryCenter defaultCenter) {
            defaultCenter.addLazyContributor(this);
            return;
        }
        log.warn("[DubboSource] 中心实现不支持懒贡献登记, 来源[{}]需在中心读取路径外自行刷新条目", source());
    }

    /**
     * 重新发现指定接口的提供者实例并整段替换本源条目
     * <p>
     * 发现间隔未到或来源开关关闭时直接返回，沿用中心现有条目；发现异常降级为空贡献。
     * </p>
     *
     * @param center 注册中心，不能为null
     * @param key    目标接口类型，不能为null
     */
    @Override
    public void contributeEntries(IRegistryCenter center, Class<?> key) {
        if (!property.isDubboDiscoveryEnabled() || !shouldDiscover(key)) {
            return;
        }
        List<RegistryEntry> entries;
        try {
            entries = buildEntries(key);
        } catch (Exception e) {
            log.warn("[DubboSource] 接口[{}]实例枚举失败, 原因: {}", key.getName(), e.getMessage());
            entries = Collections.emptyList();
        }
        center.replaceEntriesBySource(source(), key, entries);
    }

    /**
     * 判断是否已到该接口的重新发现时刻，并记下本次发现时刻
     *
     * @param key 接口类型，不能为null
     * @return true 表示本次应当重新发现
     */
    private boolean shouldDiscover(Class<?> key) {
        var interval = property.getDubboDiscoveryRefreshMillis();
        if (interval <= 0) {
            return true;
        }
        var now = System.currentTimeMillis();
        var last = lastDiscoverAt.get(key.getName());
        if (last != null && now - last < interval) {
            return false;
        }
        lastDiscoverAt.put(key.getName(), now);
        return true;
    }

    /**
     * 构建指定接口的逐实例枚举条目
     * <p>
     * 逐个实例建引用，建引用失败的实例跳过（列表只呈现真正可达的实例）；随后释放已下线实例的直连引用。
     * </p>
     *
     * @param key 接口类型，不能为null
     * @return 枚举条目列表，无实例时为空列表
     */
    private List<RegistryEntry> buildEntries(Class<?> key) {
        List<URL> providerUrls = lookupProviderUrls(key);
        Set<String> aliveKeys = providerUrls.stream()
            .map(url -> referenceKeyOf(key, url))
            .collect(Collectors.toCollection(HashSet::new));
        List<RegistryEntry> entries = new ArrayList<>(providerUrls.size());
        for (URL providerUrl : providerUrls) {
            var instance = resolveInstance(key, providerUrl);
            if (instance == null) {
                continue;
            }
            entries.add(DefaultRegistryEntry.ofInstance(key, coordinateOf(providerUrl), instanceKeyOf(providerUrl),
                ProviderConstant.ORDER_DUBBO_RESOLVER, instance, true, false));
        }
        releaseStaleReferences(key, aliveKeys);
        log.debug("[DubboSource] 接口[{}]枚举远程实例 {} 个, 提供者地址 {} 个", key.getName(), entries.size(),
            providerUrls.size());
        return entries;
    }

    /**
     * 从注册中心拉取接口的全部提供者实例
     * <p>
     * 任何失败（配置缺失、注册中心不可达）均降级为空列表并记录日志，不影响其他来源。
     * </p>
     *
     * @param key 接口类型，不能为null
     * @return 提供者实例 URL 列表，不会为null
     */
    private List<URL> lookupProviderUrls(Class<?> key) {
        var registryConfig = findRegistryConfig();
        if (registryConfig.isEmpty()) {
            log.debug("[DubboSource] 未发现Dubbo注册中心配置(RegistryConfig), 跳过接口[{}]的实例枚举", key.getName());
            return Collections.emptyList();
        }
        try {
            var registryFactory = ApplicationModel.defaultModel()
                .getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
            var registry = registryFactory.getRegistry(buildRegistryUrl(registryConfig.get()));
            var providers = registry.lookup(buildLookupUrl(key.getName()));
            if (providers == null) {
                return Collections.emptyList();
            }
            return providers.stream()
                .filter(url -> !RegistryConstants.EMPTY_PROTOCOL.equals(url.getProtocol()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[DubboSource] 接口[{}]实例发现失败, 原因: {}", key.getName(), e.getMessage());
            return Collections.emptyList();
        }
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
     * 取或建指定实例的直连引用，失败时降级为 null
     *
     * @param key         接口类型，不能为null
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 实例引用，构建失败为 null
     */
    private Object resolveInstance(Class<?> key, URL providerUrl) {
        var reference = instanceReferences.computeIfAbsent(referenceKeyOf(key, providerUrl),
            cacheKey -> buildInstanceReference(key, providerUrl));
        try {
            return reference.get();
        } catch (Exception e) {
            log.warn("[DubboSource] 构建Dubbo实例[{}]直连引用失败, 接口: {}, 原因: {}", instanceKeyOf(providerUrl),
                key.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 构建指向指定实例的直连引用
     * <p>
     * 直连协议取自提供者实例 URL 自身（如 dubbo/tri），不能硬编码：
     * 提供方可能以 Triple 协议导出，协议不匹配时引用构建成功但调用必然失败。
     * </p>
     *
     * @param key         接口类型，不能为null
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 直连引用
     */
    private ReferenceConfig<?> buildInstanceReference(Class<?> key, URL providerUrl) {
        ReferenceConfig<Object> reference = new ReferenceConfig<>();
        reference.setInterface(key);
        reference.setUrl(providerUrl.getProtocol() + "://" + providerUrl.getHost() + ":" + providerUrl.getPort());
        reference.setTimeout(property.getDubboTimeout());
        reference.setCheck(false);
        var group = providerUrl.getParameter(CommonConstants.GROUP_KEY);
        if (StrUtil.isNotBlank(group)) {
            reference.setGroup(group);
        }
        var version = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        if (StrUtil.isNotBlank(version)) {
            reference.setVersion(version);
        }
        findApplicationConfig().ifPresent(reference::setApplication);
        return reference;
    }

    /**
     * 释放已下线实例的直连引用
     *
     * @param key       接口类型，不能为null
     * @param aliveKeys 本次发现的存活引用键集合，不能为null
     */
    private void releaseStaleReferences(Class<?> key, Set<String> aliveKeys) {
        var interfacePrefix = key.getName() + KEY_DELIMITER;
        List<String> staleKeys = instanceReferences.keySet().stream()
            .filter(cacheKey -> cacheKey.startsWith(interfacePrefix) && !aliveKeys.contains(cacheKey))
            .collect(Collectors.toList());
        for (String staleKey : staleKeys) {
            var reference = instanceReferences.remove(staleKey);
            if (reference != null) {
                destroyReference(reference, staleKey);
            }
        }
    }

    /**
     * 销毁单个引用，异常只记录日志
     *
     * @param reference 待销毁引用，不能为null
     * @param cacheKey  引用缓存键，用于日志定位
     */
    private void destroyReference(ReferenceConfig<?> reference, String cacheKey) {
        try {
            reference.destroy();
        } catch (Exception e) {
            log.warn("销毁Dubbo实例直连引用失败, 引用键: {}", cacheKey, e);
        }
    }

    /**
     * 计算直连引用缓存键：接口 + 坐标 + 实例标识
     * <p>
     * 同一物理地址上不同 business/version 的 provider 必须各自持有独立引用，
     * 仅用 host:port 会把同机多业务 provider 合并成一个引用。
     * </p>
     *
     * @param key         接口类型，不能为null
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 引用缓存键
     */
    private String referenceKeyOf(Class<?> key, URL providerUrl) {
        var coordinate = coordinateOf(providerUrl);
        return key.getName() + KEY_DELIMITER + coordinate.getBusiness() + KEY_DELIMITER + coordinate.getVersion()
            + KEY_DELIMITER + instanceKeyOf(providerUrl);
    }

    /**
     * 计算实例物理标识：host:port
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 实例标识
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
     * @return 来源坐标
     */
    private SourceCoordinate coordinateOf(URL providerUrl) {
        var group = providerUrl.getParameter(CommonConstants.GROUP_KEY);
        var business = StrUtil.isNotBlank(group) ? group : ProviderConstant.DEFAULT_BUSINESS;
        var version = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        return SourceCoordinate.of(ObjectSource.DUBBO, business, version == null ? "" : version);
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
     * 销毁全部实例直连引用
     * <p>
     * 进程关闭时取消订阅并释放连接资源。
     * </p>
     */
    @Override
    public void destroy() {
        instanceReferences.forEach((cacheKey, reference) -> destroyReference(reference, cacheKey));
        instanceReferences.clear();
        lastDiscoverAt.clear();
    }
}
