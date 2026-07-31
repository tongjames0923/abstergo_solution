package com.tbs.web.context;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 请求上下文数据载体
 * <p>
 * 用于在整个请求生命周期中携带和传递请求相关的运行时数据。 支持序列化，可通过Dubbo RpcContext attachments等方式在分布式服务间传播。
 * <p>
 * 包含两类数据：
 * <ul>
 *   <li>基础运行数据：traceId、startTime、clientIp、requestMethod、requestUri等</li>
 *   <li>业务数据：通过 {@link #businessData} Map动态扩展，各处理步骤可按需塞入业务数据</li>
 * </ul>
 * <p>
 * 生命周期：一个HTTP请求（或一次Dubbo RPC调用）
 * <p>
 * 典型使用场景：
 * <pre>{@code
 * // 在Controller或Service中获取上下文
 * RequestDataContext ctx = RequestContextHolder.current();
 * ctx.put("orderNo", "ORDER_001");
 *
 * // 跨服务调用后，在下游服务中获取
 * String orderNo = RequestContextHolder.current().get("orderNo", String.class).orElse(null);
 * }</pre>
 *
 * @author abstergo
 * @see RequestContextHolder
 * @see com.tbs.web.gate.intercept.RequestContextInterceptor
 */
@Data
@NoArgsConstructor
public class RequestDataContext implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础运行数据 ====================

    /**
     * 链路追踪ID，全局唯一，用于分布式链路追踪
     */
    private String traceId;

    /**
     * 请求开始时间戳（毫秒）
     */
    private long startTime;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * HTTP请求方法（GET/POST/PUT/DELETE等）
     */
    private String requestMethod;

    /**
     * 请求URI路径
     */
    private String requestUri;

    /**
     * 控制器类名
     */
    private String controllerName;

    /**
     * 被调用的方法名
     */
    private String methodName;

    /**
     * 服务名称（应用标识），分布式调用时用于标识来源服务
     */
    private String serviceName;

    // ==================== 业务数据 ====================

    /**
     * 业务数据Map，各处理步骤可按需塞入业务数据
     * <p>
     * key为业务标识字符串，value为任意类型业务对象
     */
    private Map<String, Object> businessData;

    // ==================== 构造与工厂方法 ====================

    /**
     * 创建一个新的请求上下文，自动生成traceId并记录开始时间
     *
     * @return 新的RequestDataContext实例
     */
    public static RequestDataContext create() {
        RequestDataContext context = new RequestDataContext();
        context.traceId = generateTraceId();
        context.startTime = System.currentTimeMillis();
        context.businessData = new HashMap<>();
        return context;
    }

    /**
     * 从已有traceId创建请求上下文（用于分布式传播时恢复上下文）
     *
     * @param traceId 链路追踪ID
     * @return 新的RequestDataContext实例
     */
    public static RequestDataContext create(String traceId) {
        RequestDataContext context = create();
        if (traceId != null && !traceId.isEmpty()) {
            context.traceId = traceId;
        }
        return context;
    }

    // ==================== 业务数据操作方法 ====================

    /**
     * 向业务数据Map中放入一个键值对
     *
     * @param key   业务标识
     * @param value 业务数据
     */
    public void put(String key, Object value) {
        if (businessData == null) {
            businessData = new HashMap<>();
        }
        businessData.put(key, value);
    }

    /**
     * 从业务数据Map中获取指定key的值
     *
     * @param key 业务标识
     * @return 值的Optional包装
     */
    public Optional<Object> get(String key) {
        if (businessData == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(businessData.get(key));
    }

    /**
     * 从业务数据Map中获取指定key的值，并转换为指定类型
     *
     * @param key  业务标识
     * @param type 期望的值类型
     * @param <T>  泛型类型
     * @return 类型安全的值的Optional包装
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        return get(key).map(v -> type.isInstance(v) ? (T)v : null);
    }

    /**
     * 从业务数据Map中获取指定key的值，不存在则返回默认值
     *
     * @param key          业务标识
     * @param defaultValue 默认值
     * @param <T>          泛型类型
     * @return 值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        if (businessData == null) {
            return defaultValue;
        }
        Object value = businessData.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T)value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * 从业务数据Map中移除指定key的值
     *
     * @param key 业务标识
     * @return 被移除的值
     */
    public Object remove(String key) {
        if (businessData == null) {
            return null;
        }
        return businessData.remove(key);
    }

    /**
     * 检查业务数据Map中是否包含指定key
     *
     * @param key 业务标识
     * @return 是否包含
     */
    public boolean containsKey(String key) {
        return businessData != null && businessData.containsKey(key);
    }

    /**
     * 计算请求已消耗的时间（毫秒）
     *
     * @return 从请求开始到当前的时间差
     */
    public long elapsed() {
        return System.currentTimeMillis() - startTime;
    }

    // ==================== 内部方法 ====================

    /**
     * 生成唯一的链路追踪ID
     * <p>
     * 格式：timestamp-random，确保全局唯一性
     *
     * @return 链路追踪ID字符串
     */
    private static String generateTraceId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public String toString() {
        return "RequestDataContext{" +
            "traceId='" +
            traceId +
            '\'' +
            ", startTime=" +
            startTime +
            ", clientIp='" +
            clientIp +
            '\'' +
            ", requestMethod='" +
            requestMethod +
            '\'' +
            ", requestUri='" +
            requestUri +
            '\'' +
            ", controllerName='" +
            controllerName +
            '\'' +
            ", methodName='" +
            methodName +
            '\'' +
            ", serviceName='" +
            serviceName +
            '\'' +
            ", businessDataKeys=" +
            (businessData != null ? businessData.keySet() : "null") +
            '}';
    }
}
