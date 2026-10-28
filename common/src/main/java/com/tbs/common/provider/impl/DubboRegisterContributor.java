package com.tbs.common.provider.impl;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.AbstractRegisterContributor;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Dubbo 注册贡献者
 * <p>
 * 以 Dubbo 注册中心为实例来源：按查询类型向注册中心拉取该接口的全部提供者，把提供者 URL 上的 group/version 折算为注册坐标（group 即 business，version
 * 即发布版本，未声明为空串表示该维度不声明）， 每个坐标产出一个集群引用作为该坐标下的实例，坐标内的多实例由 Dubbo 自身负载均衡选取。
 * </p>
 * <p>
 * 未发现到任何提供者时贡献为空列表并记录日志，不打断中心读取；注册中心地址缺失、不可达、接口按 应用级注册导致查询不到实例，均落到这条降级路径。
 * </p>
 * <p>
 * 优先级取 {@link ProviderConstant#ORDER_DUBBO_RESOLVER}，在贡献者链中后于本地 Spring 来源遍历， 使本地实现优先命中、远端仅作兜底。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX,
    name = {ProviderConstant.ENABLE_PROPERTY_NAME, ProviderConstant.DUBBO_DISCOVERY_ENABLED_PROPERTY_NAME},
    matchIfMissing = true)
public class DubboRegisterContributor extends AbstractRegisterContributor implements Ordered, DisposableBean {

    /**
     * 贡献者标识：远端 Dubbo 来源
     */
    private static final String CONTRIBUTOR_ID = "dubbo";

    /**
     * 缓存键分隔符，用于拼装「接口 + 坐标」的引用键
     */
    private static final String KEY_DELIMITER = "#";

    /**
     * 注册中心地址未携带协议时的默认协议
     */
    private static final String DEFAULT_REGISTRY_PROTOCOL = "dubbo";

    /**
     * 容器 Bean 工厂，用于取 Dubbo 应用配置
     */
    private final ListableBeanFactory beanFactory;

    /**
     * 「接口 + 坐标」到集群引用的缓存，同一坐标复用同一引用
     */
    private final ConcurrentMap<String, ReferenceConfig<Object>> references = new ConcurrentHashMap<>();

    /**
     * 接口全限定名到坐标快照的缓存，按发现间隔重新拉取
     */
    private final ConcurrentMap<String, CoordinateSnapshot> snapshots = new ConcurrentHashMap<>();

    /**
     * 构造 Dubbo 注册贡献者
     *
     * @param beanFactory 容器 Bean 工厂，由容器注入
     */
    public DubboRegisterContributor(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 获取注册贡献者ID
     *
     * @return 贡献者标识
     */
    @Override
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }

    /**
     * 获取贡献者优先级
     *
     * @return 远端来源优先级，数值大于本地来源
     */
    @Override
    public int getOrder() {
        return ProviderConstant.ORDER_DUBBO_RESOLVER;
    }

    /**
     * 按类型枚举注册中心内的提供者坐标
     * <p>
     * 每个坐标产出一个集群引用；引用构建失败的坐标跳过，不影响其余坐标。
     * </p>
     *
     * @param type 类型，不能为空
     * @param <T>  类型
     * @return 携带注册坐标的实例列表，无匹配时返回空列表
     */
    @Override
    protected <T> List<InstanceProxy> getInstanceByType(Class<T> type) {
        List<DubboCoordinate> coordinates = coordinatesOf(type);
        if (CollUtil.isEmpty(coordinates)) {
            return CollUtil.emptyList();
        }
        List<InstanceProxy> instanceProxies = new ArrayList<>(coordinates.size());
        for (DubboCoordinate coordinate : coordinates) {
            T instance = referenceOf(type, coordinate);
            if (instance == null) {
                continue;
            }
            InstanceProxy proxy = new InstanceProxy();
            proxy.setInstance(instance);
            proxy.setBusiness(coordinate.getBusiness());
            proxy.setVersion(coordinate.getVersion());
            proxy.setContributor(this);
            instanceProxies.add(proxy);
        }
        return instanceProxies;
    }

    /**
     * 取指定类型的坐标快照，未到刷新时刻时沿用缓存
     *
     * @param type 类型，不能为空
     * @return 去重后的坐标列表，发现失败为空列表
     */
    private List<DubboCoordinate> coordinatesOf(Class<?> type) {
        long now = System.currentTimeMillis();
        CoordinateSnapshot cached = snapshots.get(type.getName());
        if (cached != null && now - cached.getDiscoverAt() < refreshIntervalOf(cached)) {
            return cached.getCoordinates();
        }
        List<DubboCoordinate> coordinates = discover(type);
        snapshots.put(type.getName(), new CoordinateSnapshot(now, coordinates));
        return coordinates;
    }

    /**
     * 计算坐标快照的有效期
     * <p>
     * 空快照按固定回退间隔重试，避免提供者尚未上线时长期沿用空结果； 非空快照按刷新间隔过期，刷新间隔非正数表示不再主动过期。
     * </p>
     *
     * @param snapshot 坐标快照，不能为null
     * @return 有效期（毫秒）
     */
    private long refreshIntervalOf(CoordinateSnapshot snapshot) {
        if (snapshot.getCoordinates().isEmpty()) {
            return ProviderConstant.EMPTY_DISCOVERY_RETRY_MILLIS;
        }
        return ProviderConstant.DEFAULT_DISCOVERY_REFRESH_MILLIS;
    }

    /**
     * 从注册中心拉取提供者并按坐标去重
     * <p>
     * 任何失败（配置缺失、注册中心不可达）均降级为空列表并记录日志，不影响其他来源。
     * </p>
     *
     * @param type 类型，不能为空
     * @return 去重后的坐标列表，不会为null
     */
    private List<DubboCoordinate> discover(Class<?> type) {
        Optional<RegistryConfig> registryConfig = findRegistryConfig();
        if (registryConfig.isEmpty() || StrUtil.isBlank(registryConfig.get().getAddress())) {
            log.debug("[DubboContributor] 未发现Dubbo注册中心配置, 跳过接口[{}]的实例枚举", type.getName());
            return CollUtil.emptyList();
        }
        try {
            RegistryFactory registryFactory =
                ApplicationModel.defaultModel().getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
            Registry registry = registryFactory.getRegistry(buildRegistryUrl(registryConfig.get()));
            List<URL> providers = registry.lookup(buildLookupUrl(type.getName()));
            if (CollUtil.isEmpty(providers)) {
                return CollUtil.emptyList();
            }
            return providers.stream().filter(url -> !RegistryConstants.EMPTY_PROTOCOL.equals(url.getProtocol()))
                .map(this::coordinateOf).distinct().collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[DubboContributor] 接口[{}]实例发现失败, 原因: {}", type.getName(), e.getMessage());
            return CollUtil.emptyList();
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
        String address = registryConfig.getAddress();
        if (!address.contains("://")) {
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
        String localHost = NetUtils.getLocalHost();
        return URL.valueOf("dubbo://" + localHost + "/" + interfaceName)
            .addParameter(CommonConstants.INTERFACE_KEY, interfaceName)
            .addParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.DEFAULT_CATEGORY)
            .addParameter(CommonConstants.CHECK_KEY, String.valueOf(false));
    }

    /**
     * 折算提供者实例的注册坐标
     * <p>
     * group 承载 business，version 承载发布版本，未声明时为空串表示该维度不声明。
     * </p>
     *
     * @param providerUrl 提供者实例 URL，不能为null
     * @return 注册坐标
     */
    private DubboCoordinate coordinateOf(URL providerUrl) {
        return new DubboCoordinate(StrUtil.nullToEmpty(providerUrl.getParameter(CommonConstants.GROUP_KEY)),
            StrUtil.nullToEmpty(providerUrl.getParameter(CommonConstants.VERSION_KEY)));
    }

    /**
     * 取或建指定坐标的集群引用
     *
     * @param type       类型，不能为null
     * @param coordinate 注册坐标，不能为null
     * @param <T>        类型
     * @return 远程实例，构建失败为 null
     */
    private <T> T referenceOf(Class<T> type, DubboCoordinate coordinate) {
        String cacheKey = referenceKeyOf(type, coordinate);
        ReferenceConfig<Object> reference =
            references.computeIfAbsent(cacheKey, key -> buildReference(type, coordinate));
        try {
            return type.cast(reference.get());
        } catch (Exception e) {
            references.remove(cacheKey, reference);
            log.warn("[DubboContributor] 接口[{}]坐标[{}]的远程引用构建失败, 原因: {}", type.getName(),
                coordinate.describe(), e.getMessage());
            return null;
        }
    }

    /**
     * 构建指向指定坐标的集群引用
     * <p>
     * 不指定直连地址，由注册中心发现同坐标的多个提供者并交给 Dubbo 负载均衡； check 关闭使提供者缺失时启动不受影响，窗口期内调用失败由上层降级处理。
     * </p>
     *
     * @param type       类型，不能为null
     * @param coordinate 注册坐标，不能为null
     * @return 集群引用
     */
    private ReferenceConfig<Object> buildReference(Class<?> type, DubboCoordinate coordinate) {
        ReferenceConfig<Object> reference = new ReferenceConfig<>();
        reference.setInterface(type);
        reference.setTimeout(ProviderConstant.DEFAULT_DUBBO_TIMEOUT);
        reference.setCheck(false);
        if (StrUtil.isNotBlank(coordinate.getBusiness())) {
            reference.setGroup(coordinate.getBusiness());
        }
        if (StrUtil.isNotBlank(coordinate.getVersion())) {
            reference.setVersion(coordinate.getVersion());
        }
        findApplicationConfig().ifPresent(reference::setApplication);
        return reference;
    }

    /**
     * 计算集群引用缓存键：接口 + 坐标
     * <p>
     * 同一接口不同 business/version 的提供者必须各自持有独立引用，仅用接口名会把多坐标合并成一个引用。
     * </p>
     *
     * @param type       类型，不能为null
     * @param coordinate 注册坐标，不能为null
     * @return 引用缓存键
     */
    private String referenceKeyOf(Class<?> type, DubboCoordinate coordinate) {
        return type.getName() + KEY_DELIMITER + coordinate.describe();
    }

    /**
     * 获取 Dubbo 应用配置
     * <p>
     * 优先复用容器内已有的 ApplicationConfig Bean，脱离容器构建引用时仍沿用当前应用配置。
     * </p>
     *
     * @return 应用配置，容器内不存在时为 empty
     */
    private Optional<ApplicationConfig> findApplicationConfig() {
        try {
            Map<String, ApplicationConfig> configs = beanFactory.getBeansOfType(ApplicationConfig.class);
            return configs == null ? Optional.empty() : configs.values().stream().findFirst();
        } catch (Exception e) {
            log.debug("[DubboContributor] 获取ApplicationConfig失败", e);
            return Optional.empty();
        }
    }

    /**
     * 获取 Dubbo 注册中心配置
     * <p>
     * 容器内可能存在多个 RegistryConfig Bean，取第一个。
     * </p>
     *
     * @return 注册中心配置，容器内不存在时为 empty
     */
    private Optional<RegistryConfig> findRegistryConfig() {
        try {
            Map<String, RegistryConfig> configs = beanFactory.getBeansOfType(RegistryConfig.class);
            return configs == null ? Optional.empty() : configs.values().stream().findFirst();
        } catch (Exception e) {
            log.debug("[DubboContributor] 获取RegistryConfig失败", e);
            return Optional.empty();
        }
    }

    /**
     * 销毁全部集群引用
     * <p>
     * 进程关闭时取消订阅并释放连接资源。坐标下线后其引用保留至停机统一释放： ponytail: 已知上限为「旧坐标引用驻留内存与连接直到停机」，升级路径为发现后按接口比对存活坐标并即时释放。
     * </p>
     */
    @Override
    public void destroy() {
        references.forEach(this::destroyReference);
        references.clear();
        snapshots.clear();
    }

    /**
     * 销毁单个引用，异常只记录日志
     *
     * @param cacheKey  引用缓存键，用于日志定位
     * @param reference 待销毁引用，不能为null
     */
    private void destroyReference(String cacheKey, ReferenceConfig<?> reference) {
        try {
            reference.destroy();
        } catch (Exception e) {
            log.warn("[DubboContributor] 销毁远程引用失败, 引用键: {}", cacheKey, e);
        }
    }

    /**
     * 提供者注册坐标：group 承载 business，version 即发布版本
     */
    @Getter
    @RequiredArgsConstructor
    private static final class DubboCoordinate {

        /**
         * 业务分区，未声明为空串
         */
        private final String business;

        /**
         * 发布版本，未声明为空串
         */
        private final String version;

        /**
         * 输出用于缓存键与日志的坐标描述
         *
         * @return 坐标描述
         */
        private String describe() {
            return business + KEY_DELIMITER + version;
        }
    }

    /**
     * 坐标快照：记录发现时刻与去重后的坐标集合
     */
    @Getter
    @RequiredArgsConstructor
    private static final class CoordinateSnapshot {

        /**
         * 本次发现时刻（毫秒）
         */
        private final long discoverAt;

        /**
         * 去重后的坐标集合
         */
        private final List<DubboCoordinate> coordinates;
    }
}
