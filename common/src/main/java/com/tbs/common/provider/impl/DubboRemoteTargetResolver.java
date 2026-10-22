package com.tbs.common.provider.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import com.tbs.common.config.property.ObjectProviderProperty;
import com.tbs.common.provider.IRemoteTargetResolver;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.util.framework.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Dubbo 远程来源目标解析器。
 * <p>
 * Dubbo 3 应用级发现下的路由实现：给定接口类型与定向维度（business/version），直接构造限定该维度的
 * Dubbo 原生集群引用（{@link ReferenceConfig} 不指定实例地址，check=false），把服务发现、迁移与负载均衡
 * 全权交由 Dubbo 依注册中心完成，因此不依赖 provider 的注册模式（接口级/应用级/双注册均可路由）。
 * business 映射为 Dubbo 分组：business 非空即作为 group（含默认业务 default，对应 provider 侧显式
 * {@code @DubboService(group = "default")} 的普遍约定）；business 为空表示不限定分组。
 * </p>
 * <p>
 * 引用按「接口 + 坐标」缓存复用，避免重复订阅建连；进程关闭时统一销毁全部引用（{@link DisposableBean}）。
 * 目标的存在性由 Dubbo 在调用期保障：无可用提供者时本方法仍返回引用，调用时抛 Dubbo 框架级异常，
 * 而非在解析期判空——这是 Dubbo 原生发现形态相对逐实例枚举的既定取舍。来源未配置（无 RegistryConfig）
 * 或发现开关关闭时返回 null，交由路由代理降级或走无提供者处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class DubboRemoteTargetResolver implements IRemoteTargetResolver, DisposableBean {

    /**
     * 引用缓存键分隔符
     */
    private static final String KEY_DELIMITER = "#";

    /**
     * 「接口 + 坐标」到集群引用的缓存
     */
    private final ConcurrentMap<String, ReferenceConfig<?>> referenceCache = new ConcurrentHashMap<>();

    /**
     * 对象提供器配置
     */
    private final ObjectProviderProperty property;

    /**
     * 构造 Dubbo 远程来源目标解析器
     *
     * @param property 对象提供器配置，不能为null
     */
    public DubboRemoteTargetResolver(ObjectProviderProperty property) {
        this.property = property;
    }

    @Override
    public ObjectSource source() {
        return ObjectSource.DUBBO;
    }

    /**
     * 按接口与定向维度解析 Dubbo 原生集群引用
     *
     * @param interfaceType 目标接口类型，不能为null
     * @param business      定向业务分区，null/空/默认表示不指定分组
     * @param version       定向发布版本，null/空表示不指定版本
     * @param <T>           目标接口类型
     * @return Dubbo 集群引用代理；来源未配置或发现开关关闭时为 null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolveTarget(Class<T> interfaceType, String business, String version) {
        if (!property.isDubboDiscoveryEnabled()) {
            return null;
        }
        if (findRegistryConfig().isEmpty()) {
            log.debug("[DubboRemote] 未发现Dubbo注册中心配置(RegistryConfig), 跳过接口[{}]的远程目标解析",
                interfaceType.getName());
            return null;
        }
        var key = referenceCacheKey(interfaceType, business, version);
        var reference = referenceCache.computeIfAbsent(key,
            k -> buildClusterReference(interfaceType, business, version));
        try {
            return (T) reference.get();
        } catch (Exception e) {
            log.warn("[DubboRemote] 接口[{}]构建Dubbo集群引用失败, business: {}, version: {}, 原因: {}",
                interfaceType.getName(), business, version, e.getMessage());
            return null;
        }
    }

    /**
     * 计算引用缓存键：接口全限定名 + business + version
     *
     * @param interfaceType 接口类型，不能为null
     * @param business      业务分区，可为null
     * @param version       发布版本，可为null
     * @return 缓存键
     */
    private String referenceCacheKey(Class<?> interfaceType, String business, String version) {
        return interfaceType.getName() + KEY_DELIMITER + nullToEmpty(business) + KEY_DELIMITER + nullToEmpty(version);
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
     * 构建限定坐标的整体负载均衡集群引用
     * <p>
     * 不指定实例地址，由 Dubbo 依据注册中心在全部实例间负载均衡。business 非空时映射为分组
     * （含默认业务 default，与全库 provider 显式 {@code @DubboService(group = "default")} 的发布坐标一致），
     * version 非空时映射为版本，均缺省时构造不限定引用（匹配无分组、无版本的提供者）。
     * </p>
     *
     * @param interfaceType 接口类型，不能为null
     * @param business      定向业务分区，可为null
     * @param version       定向发布版本，可为null
     * @param <T>           接口类型
     * @return 集群引用
     */
    private <T> ReferenceConfig<T> buildClusterReference(Class<T> interfaceType, String business, String version) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceType);
        reference.setTimeout(property.getDubboTimeout());
        reference.setCheck(false);
        if (StringUtils.hasText(business)) {
            reference.setGroup(business);
        }
        if (StringUtils.hasText(version)) {
            reference.setVersion(version);
        }
        findApplicationConfig().ifPresent(reference::setApplication);
        findRegistryConfig().ifPresent(reference::setRegistry);
        return reference;
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
     * 销毁全部 Dubbo 集群引用
     * <p>
     * 进程关闭时取消注册中心订阅并释放连接资源。
     * </p>
     */
    @Override
    public void destroy() {
        referenceCache.values().forEach(reference -> {
            try {
                reference.destroy();
            } catch (Exception e) {
                log.warn("销毁Dubbo集群引用失败, 接口: {}", reference.getInterface(), e);
            }
        });
        referenceCache.clear();
    }
}
