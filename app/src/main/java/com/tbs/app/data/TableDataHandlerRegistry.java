package com.tbs.app.data;

import com.tbs.common.data.ITableDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表格数据处理器注册中心
 * <p>
 * 支持双模式查找：
 * <ul>
 *   <li><b>本地模式</b>（默认）：从 Spring 容器注入 {@code List<ITableDataHandler>}，
 *       按 {@link ITableDataHandler#getId()} 构建 Map，适用于单体架构</li>
 *   <li><b>Dubbo 远程模式</b>：通过 {@link ReferenceConfig} 动态创建 Dubbo 引用，
 *       以 handlerId 作为 group 从注册中心查找远程服务，适用于微服务架构</li>
 * </ul>
 * <p>
 * 查找顺序：本地优先 → Dubbo 远程（若启用）。Dubbo 代理创建后缓存于
 * {@link ConcurrentHashMap}，避免重复创建 {@link ReferenceConfig}。
 * </p>
 * <p>
 * 配置项：
 * <pre>
 * tbs.data.handler.remote.enable=true   # 启用 Dubbo 远程查找
 * tbs.data.handler.remote.timeout=10000 # Dubbo 引用超时（毫秒）
 * </pre>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class TableDataHandlerRegistry {

    /**
     * 本地处理器列表（Spring 容器注入，单体架构下生效）
     */
    @Autowired(required = false)
    private List<ITableDataHandler> localHandlers;

    /**
     * Dubbo 应用配置（远程模式必需）
     */
    @Autowired(required = false)
    private ApplicationConfig applicationConfig;

    /**
     * Dubbo 注册中心配置（远程模式必需）
     */
    @Autowired(required = false)
    private RegistryConfig registryConfig;

    /**
     * 是否启用 Dubbo 远程查找
     */
    @Value("${tbs.data.handler.remote.enable:false}")
    private boolean remoteEnabled;

    /**
     * Dubbo 引用超时时间（毫秒）
     */
    @Value("${tbs.data.handler.remote.timeout:10000}")
    private int remoteTimeout;

    /**
     * 本地处理器映射（只读，线程安全）
     */
    private Map<String, ITableDataHandler> localHandlerMap;

    /**
     * Dubbo 远程处理器代理缓存
     */
    private final Map<String, ITableDataHandler> remoteHandlerCache = new ConcurrentHashMap<>();

    /**
     * 初始化本地处理器映射
     */
    @PostConstruct
    private void init() {
        Map<String, ITableDataHandler> map = new HashMap<>();
        if (localHandlers != null) {
            for (ITableDataHandler handler : localHandlers) {
                map.put(handler.getId(), handler);
            }
        }
        this.localHandlerMap = Collections.unmodifiableMap(map);
        log.info("TableDataHandlerRegistry 初始化完成，本地处理器数量：{}，远程模式：{}",
            map.size(), remoteEnabled);
    }

    /**
     * 按ID查找处理器
     * <p>
     * 查找顺序：本地 Map → Dubbo 远程（若启用且本地未命中）。
     * Dubbo 代理首次创建后缓存，后续直接返回缓存实例。
     * </p>
     *
     * @param id 处理器ID
     * @return 处理器实例，未找到返回 null
     */
    public ITableDataHandler getHandler(String id) {
        ITableDataHandler local = localHandlerMap.get(id);
        if (local != null) {
            return local;
        }
        if (remoteEnabled && applicationConfig != null && registryConfig != null) {
            return remoteHandlerCache.computeIfAbsent(id, this::createDubboReference);
        }
        return null;
    }

    /**
     * 获取所有本地处理器
     * <p>
     * 仅返回本地 Spring 容器中的处理器，Dubbo 远程处理器为按需发现，不在此列。
     * </p>
     *
     * @return 不可变处理器集合
     */
    public Collection<ITableDataHandler> getAllHandlers() {
        return localHandlerMap.values();
    }

    /**
     * 创建 Dubbo 远程引用
     * <p>
     * 以 handlerId 作为 Dubbo group，创建 {@link ReferenceConfig} 并获取代理。
     * 创建失败时记录警告日志并返回 null，不抛异常（由调用方处理 null）。
     * </p>
     *
     * @param handlerId 处理器ID（作为 Dubbo group）
     * @return Dubbo 代理实例，创建失败返回 null
     */
    private ITableDataHandler createDubboReference(String handlerId) {
        try {
            ReferenceConfig<ITableDataHandler> reference = new ReferenceConfig<>();
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setInterface(ITableDataHandler.class);
            reference.setGroup(handlerId);
            reference.setTimeout(remoteTimeout);
            ITableDataHandler handler = reference.get();
            log.info("Dubbo 远程处理器引用创建成功，handlerId：{}", handlerId);
            return handler;
        } catch (Exception e) {
            log.warn("Dubbo 远程处理器引用创建失败，handlerId：{}，原因：{}", handlerId, e.getMessage());
            return null;
        }
    }
}
