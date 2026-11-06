package com.tbs.web.genric;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.constant.WebI18nConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 泛化引用持有器（新版泛化调用链路专用）
 * <p>
 * 以「接口全限定名 + 业务分区 + 版本」为键复用 {@code generic=true} 的 {@link ReferenceConfig}：
 * 同一坐标只建一次引用，之后的调用复用其订阅与负载均衡，服务发现、实例选择、集群容错全部交给 Dubbo 原生能力，
 * 本类只负责引用的<b>创建、复用与停机释放</b>。
 * </p>
 * <p>
 * 引用获取失败时逐出该键并抛出系统级异常，下次调用重建，避免把瞬时故障固化进缓存。
 * </p>
 * <p>
 * {@code ponytail:} 已知上限——本类与 {@code com.tbs.common.provider.impl.DubboRegisterContributor} 内部的类型化引用缓存
 * 是两份同构实现（一个建 {@code generic=true} 引用、一个建类型化引用，键互不重叠），为保持既有 provider 框架不动而暂时并存；
 * 升级路径见 docs/generic-api-architecture-rethink.md 第二节：抽出统一 {@code DubboReferenceHolder}，
 * 由 contributor 与本类共同委托，届时一并收敛键口径、超时与释放时机。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class GenericReferenceHolder implements DisposableBean {

    /**
     * 缓存键分隔符
     */
    private static final String KEY_DELIMITER = "#";

    /**
     * Dubbo 泛化标记
     */
    private static final String GENERIC_FLAG = "true";

    /**
     * 应用配置提供器，容器内缺失时沿用 Dubbo 全局配置
     */
    private final ObjectProvider<ApplicationConfig> applicationConfigProvider;

    /**
     * 注册中心配置提供器，容器内缺失时沿用 Dubbo 全局配置
     */
    private final ObjectProvider<RegistryConfig> registryConfigProvider;

    /**
     * 「接口 + 坐标」到泛化引用的缓存
     */
    private final ConcurrentMap<String, ReferenceConfig<GenericService>> references = new ConcurrentHashMap<>();

    public GenericReferenceHolder(ObjectProvider<ApplicationConfig> applicationConfigProvider,
        ObjectProvider<RegistryConfig> registryConfigProvider) {
        this.applicationConfigProvider = applicationConfigProvider;
        this.registryConfigProvider = registryConfigProvider;
    }

    /**
     * 取指定接口在指定坐标下的泛化引用
     *
     * @param interfaceName 接口全限定名，不能为空
     * @param business      业务分区（Dubbo group），空表示不约束
     * @param version       发布版本，{@code null} 表示不约束
     * @return 泛化服务引用
     */
    public GenericService genericService(String interfaceName, String business, Integer version) {
        String key = referenceKeyOf(interfaceName, business, version);
        ReferenceConfig<GenericService> reference =
            references.computeIfAbsent(key, k -> buildReference(interfaceName, business, version));
        try {
            return reference.get();
        } catch (Exception e) {
            references.remove(key, reference);
            log.warn("[GenericReferenceHolder] 接口[{}]坐标[{}]泛化引用获取失败, 原因: {}", interfaceName, key,
                e.getMessage());
            throw AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_REFERENCE_FAILED).cause(e).build();
        }
    }

    /**
     * 构建指向指定坐标的泛化引用
     * <p>
     * 不填直连地址，由注册中心发现同坐标的提供者并交给 Dubbo 负载均衡；{@code check=false} 使提供者缺失时
     * 调用侧得到明确错误而非阻塞启动。
     * </p>
     *
     * @param interfaceName 接口全限定名，不能为空
     * @param business      业务分区，可以为空
     * @param version       发布版本，可以为 {@code null}
     * @return 泛化引用
     */
    private ReferenceConfig<GenericService> buildReference(String interfaceName, String business, Integer version) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface(interfaceName);
        reference.setGeneric(GENERIC_FLAG);
        reference.setCheck(false);
        reference.setTimeout(ProviderConstant.DEFAULT_DUBBO_TIMEOUT);
        if (StrUtil.isNotBlank(business)) {
            reference.setGroup(business);
        }
        if (version != null) {
            reference.setVersion(String.valueOf(version));
        }
        ApplicationConfig applicationConfig = applicationConfigProvider.getIfAvailable();
        if (applicationConfig != null) {
            reference.setApplication(applicationConfig);
        }
        RegistryConfig registryConfig = registryConfigProvider.getIfAvailable();
        if (registryConfig != null) {
            reference.setRegistry(registryConfig);
        }
        return reference;
    }

    /**
     * 计算引用缓存键：接口 + 坐标
     *
     * @param interfaceName 接口全限定名，不能为空
     * @param business      业务分区，可以为空
     * @param version       发布版本，可以为 {@code null}
     * @return 引用缓存键
     */
    private String referenceKeyOf(String interfaceName, String business, Integer version) {
        return interfaceName + KEY_DELIMITER + StrUtil.nullToEmpty(business) + KEY_DELIMITER + (version == null
            ? StrUtil.EMPTY : version);
    }

    /**
     * 销毁全部泛化引用，取消订阅并释放连接资源
     */
    @Override
    public void destroy() {
        references.forEach(this::destroyReference);
        references.clear();
    }

    /**
     * 销毁单个引用，异常只记录日志
     *
     * @param cacheKey  引用缓存键，用于日志定位
     * @param reference 待销毁引用，不能为 null
     */
    private void destroyReference(String cacheKey, ReferenceConfig<GenericService> reference) {
        try {
            reference.destroy();
        } catch (Exception e) {
            log.warn("[GenericReferenceHolder] 销毁泛化引用失败, 引用键: {}", cacheKey, e);
        }
    }
}
