package com.tbs.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 推送协议帧
 * <p>
 * 对应服务端PushFrame：SSE下行data载荷与HTTP上行frame字段的统一结构。
 * topic为纯字符串（非空且≤128字符，DATA/SUBSCRIBE/UNSUBSCRIBE帧必填）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushFrame {

    /**
     * 协议版本（当前为1，握手阶段版本不一致服务端拒绝连接）
     */
    private int version;

    /**
     * 帧类型（必填）
     */
    private FrameType type;

    /**
     * 消息ID（DATA帧携带）
     */
    private String messageId;

    /**
     * 订阅主题（按帧类型要求必填）
     */
    private String topic;

    /**
     * 序号（可选）
     */
    private Long seq;

    /**
     * 时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 帧载荷（DATA为业务文本，HELLO含sessionId与版本，ERROR为错误描述）
     */
    private String payload;

    /**
     * 协议错误码（仅ERROR帧携带：1001版本不兼容 / 1002帧损坏 / 1003主题非法 / 1008订阅未授权 / 1009上行被拒等）
     * <p>
     * payload为面向人的描述，会随语言变化；按 code 分支处置更稳定。
     * </p>
     */
    private Integer code;
}
