package com.tbs.sdk.model;

/**
 * 推送协议帧类型
 * <p>
 * 对应服务端FrameTypeEnum：枚举名与服务端一致（JSON序列化为大写名），
 * value为小写值（与SSE事件名对应）。SIGNAL为服务端下发信令帧，客户端禁止上行。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum FrameType {

    /**
     * 握手确认帧（服务端下发，载荷含sessionId与协议版本）
     */
    HELLO("hello"),

    /**
     * 心跳请求帧
     */
    PING("ping"),

    /**
     * 心跳应答帧（服务端下发）
     */
    PONG("pong"),

    /**
     * 数据帧（业务消息文本）
     */
    DATA("data"),

    /**
     * 信令帧（服务端下发，通知客户端拉取）
     */
    SIGNAL("signal"),

    /**
     * 订阅帧（客户端上行）
     */
    SUBSCRIBE("subscribe"),

    /**
     * 退订帧（客户端上行）
     */
    UNSUBSCRIBE("unsubscribe"),

    /**
     * 错误帧
     */
    ERROR("error"),

    /**
     * 停机通知帧（服务端优雅停机前广播，载荷含reason与graceMillis，客户端禁止上行）
     */
    SHUTDOWN("shutdown");

    /**
     * 帧类型小写值
     */
    private final String value;

    FrameType(String value) {
        this.value = value;
    }

    /**
     * 获取帧类型小写值
     *
     * @return 小写值（与SSE事件名对应）
     */
    public String getValue() {
        return value;
    }
}
