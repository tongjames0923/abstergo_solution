package com.tbs.push.model.frame;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 推送协议帧
 * <p>
 * 推送协议的唯一传输单元，所有传输适配层（WebSocket/SSE）收发均以本帧为载体： 上层业务经编解码器{@code IPushCodec}把业务消息转为DATA帧，控制类帧
 * （HELLO/PING/PONG/SUBSCRIBE/UNSUBSCRIBE/ERROR）由协议层统一产生与处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushFrame implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 协议版本号，握手阶段用于版本协商
     */
    private int version;

    /**
     * 帧类型
     */
    private FrameTypeEnum type;

    /**
     * 帧唯一标识，服务端产生的DATA帧与投递记录的messageId一致，用于审计关联
     */
    private String messageId;

    /**
     * 业务主题，DATA/SUBSCRIBE/UNSUBSCRIBE帧必填，控制帧可为空； 类型化为{@link PushTopic}值对象，非空与长度约束由类型自身承载
     */
    private PushTopic topic;

    /**
     * 发送方序列号，可选，用于接收端做乱序检测
     */
    private Long seq;

    /**
     * 帧产生时间戳（毫秒）
     */
    private long timestamp;

    /**
     * 帧载荷
     * <p>
     * DATA帧为序列化后的业务数据文本；HELLO帧为服务端握手确认载荷（含sessionId与版本）； ERROR帧为错误描述文本。
     * </p>
     */
    private String payload;

    /**
     * 协议错误码（仅ERROR帧填充，取值见 {@link PushErrorCodeEnum#getCode()}）
     * <p>
     * payload为面向人的错误描述，会随语言与文案调整变化；code才是稳定的判定依据。
     * 非ERROR帧为空，序列化时省略，保持既有帧结构不变。
     * </p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer code;

    /**
     * 构造空帧（时间戳取当前时间）
     */
    public PushFrame() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造指定类型的帧
     *
     * @param type 帧类型
     */
    public PushFrame(FrameTypeEnum type) {
        this();
        this.type = type;
    }

}
