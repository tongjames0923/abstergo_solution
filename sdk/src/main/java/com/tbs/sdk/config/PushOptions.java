package com.tbs.sdk.config;

import com.tbs.sdk.constants.SdkConstants;

/**
 * SSE推送默认参数
 * <p>
 * 集中承载推送连接的主题派生、心跳与重连退避参数，与服务端 PushFrameConstants
 * 及前端 PUSH_DEFAULTS 对齐。作为可变配置随 SdkConfig 提供，宿主可按需覆盖；
 * 一次性 ticket 被服务端原子消费，断线后必须换票重连，退避为指数退避叠加随机抖动（防惊群）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushOptions {

    /**
     * per-user SIGNAL 频道主题前缀（缺省据此拼接 topicPrefix + userId）
     */
    private String topicPrefix = SdkConstants.TOPIC_PREFIX;

    /**
     * 心跳上行周期（秒，须远小于服务端空闲超时；<=0 表示关闭心跳）
     */
    private int heartbeatIntervalSeconds = 30;

    /**
     * 是否启用断线自动重连（默认开启；收到 hello 后连接被服务端关闭亦触发重连）
     */
    private boolean reconnectEnabled = true;

    /**
     * 重连退避基数（秒），实际间隔为 base × 2^(尝试次数)，累加上限封顶后再叠加随机抖动
     */
    private int reconnectBaseDelaySeconds = 1;

    /**
     * 重连退避上限（秒）
     */
    private int reconnectMaxDelaySeconds = 30;

    /**
     * 重连退避随机抖动上限（秒，防惊群；<=0 表示不叠加抖动）
     */
    private int reconnectJitterSeconds = 1;

    /**
     * 协议版本号（与服务端 PROTOCOL_VERSION 一致）
     */
    private int protocolVersion = SdkConstants.SSE_PROTOCOL_VERSION;

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public PushOptions setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
        return this;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public PushOptions setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        return this;
    }

    public boolean isReconnectEnabled() {
        return reconnectEnabled;
    }

    public PushOptions setReconnectEnabled(boolean reconnectEnabled) {
        this.reconnectEnabled = reconnectEnabled;
        return this;
    }

    public int getReconnectBaseDelaySeconds() {
        return reconnectBaseDelaySeconds;
    }

    public PushOptions setReconnectBaseDelaySeconds(int reconnectBaseDelaySeconds) {
        this.reconnectBaseDelaySeconds = reconnectBaseDelaySeconds;
        return this;
    }

    public int getReconnectMaxDelaySeconds() {
        return reconnectMaxDelaySeconds;
    }

    public PushOptions setReconnectMaxDelaySeconds(int reconnectMaxDelaySeconds) {
        this.reconnectMaxDelaySeconds = reconnectMaxDelaySeconds;
        return this;
    }

    public int getReconnectJitterSeconds() {
        return reconnectJitterSeconds;
    }

    public PushOptions setReconnectJitterSeconds(int reconnectJitterSeconds) {
        this.reconnectJitterSeconds = reconnectJitterSeconds;
        return this;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public PushOptions setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }
}
