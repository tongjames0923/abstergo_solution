package com.tbs.sdk.model;

import lombok.Data;

/**
 * SSE hello握手帧载荷
 * <p>
 * 对齐后端hello帧payload（需从帧载荷JSON二次解析）：sessionId为后续上行帧必须携带的
 * 会话标识，version为协商的协议版本（服务端未回时为null）。连接以hello帧的sessionId为准，
 * 而非auth签票时预分配的sessionId。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class HelloInfo {

    /**
     * 会话标识（握手完成后可用，后续上行帧携带）
     */
    private String sessionId;

    /**
     * 协议版本（可空）
     */
    private Integer version;

    /**
     * 服务端推荐心跳周期（毫秒，可空；空表示服务端未下发，客户端沿用自身默认）
     */
    private Long heartbeatIntervalMillis;

    /**
     * 服务端最迟心跳期限（毫秒，可空；距上次心跳超过此值判定链路陈旧应主动重连）
     */
    private Long heartbeatDeadlineMillis;

    public HelloInfo() {
    }

    public HelloInfo(String sessionId, Integer version) {
        this.sessionId = sessionId;
        this.version = version;
    }

    public HelloInfo(String sessionId, Integer version, Long heartbeatIntervalMillis,
                     Long heartbeatDeadlineMillis) {
        this.sessionId = sessionId;
        this.version = version;
        this.heartbeatIntervalMillis = heartbeatIntervalMillis;
        this.heartbeatDeadlineMillis = heartbeatDeadlineMillis;
    }
}
