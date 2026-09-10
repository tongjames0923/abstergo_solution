package com.tbs.logging.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 默认日志上下文实现——可变快照载体
 * <p>
 * 创建器按来源条件填充标准字段与扩展属性后交付使用；
 * {@link #empty()} 产出无来源数据的空快照，作为全链路降级兜底。
 * 生命周期遵循 {@link com.tbs.common.context.IContext}：销毁后的写操作被拒绝。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContext
 */
public class DefaultLoggingContext implements ILoggingContext {

    /**
     * 默认（无来源数据）来源标识
     */
    public static final String SOURCE_DEFAULT = "default";

    /**
     * 来源标识
     */
    private final String source;

    /**
     * 采集开始时间戳（毫秒）
     */
    private final long startTime;

    /**
     * 扩展属性容器
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 生命周期有效标志
     */
    private volatile boolean valid = true;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    /**
     * 操作用户名
     */
    private String operateUser;

    /**
     * 操作来源 IP
     */
    private String operateIp;

    /**
     * 请求 URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 构造默认来源的快照
     */
    public DefaultLoggingContext() {
        this(SOURCE_DEFAULT);
    }

    /**
     * 构造指定来源的快照，采集开始时间取当前时刻
     *
     * @param source 来源标识，传入 null 按 "default" 处理
     */
    public DefaultLoggingContext(String source) {
        this(source, System.currentTimeMillis());
    }

    /**
     * 构造指定来源与采集开始时间的快照
     *
     * @param source    来源标识，传入 null 按 "default" 处理
     * @param startTime 采集开始时间戳（毫秒）
     */
    public DefaultLoggingContext(String source, long startTime) {
        this.source = source == null ? SOURCE_DEFAULT : source;
        this.startTime = startTime;
    }

    /**
     * 产出无来源数据的空快照
     *
     * @return 空快照实例
     */
    public static DefaultLoggingContext empty() {
        return new DefaultLoggingContext(SOURCE_DEFAULT);
    }

    @Override
    public Optional<String> getTraceId() {
        return Optional.ofNullable(traceId);
    }

    @Override
    public Optional<String> getOperateUser() {
        return Optional.ofNullable(operateUser);
    }

    @Override
    public Optional<String> getOperateIp() {
        return Optional.ofNullable(operateIp);
    }

    @Override
    public Optional<String> getRequestUrl() {
        return Optional.ofNullable(requestUrl);
    }

    @Override
    public Optional<String> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public void setAttribute(String key, Object value) {
        checkValid();
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null || !type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of((T)value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * 设置链路追踪 ID
     *
     * @param traceId 链路追踪 ID
     */
    public void setTraceId(String traceId) {
        checkValid();
        this.traceId = traceId;
    }

    /**
     * 设置操作用户名
     *
     * @param operateUser 操作用户名
     */
    public void setOperateUser(String operateUser) {
        checkValid();
        this.operateUser = operateUser;
    }

    /**
     * 设置操作来源 IP
     *
     * @param operateIp 操作来源 IP
     */
    public void setOperateIp(String operateIp) {
        checkValid();
        this.operateIp = operateIp;
    }

    /**
     * 设置请求 URL
     *
     * @param requestUrl 请求 URL
     */
    public void setRequestUrl(String requestUrl) {
        checkValid();
        this.requestUrl = requestUrl;
    }

    /**
     * 设置请求方法
     *
     * @param requestMethod 请求方法
     */
    public void setRequestMethod(String requestMethod) {
        checkValid();
        this.requestMethod = requestMethod;
    }

    @Override
    public void destroy() {
        this.valid = false;
    }

    @Override
    public void rebuild() {
        this.valid = true;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}
