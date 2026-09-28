package com.tbs.sdk.model;

import lombok.Data;

/**
 * 推送发送结果视图
 * <p>
 * 对应服务端PushSendResultVO：接收人数为真实送达回执依据，
 * 信令数为信令通道发布数量（tbs.push.enable关闭时为0）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushSendResult {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 业务主题
     */
    private String topic;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 接收人数
     */
    private Integer receiverCount;

    /**
     * 信令发布数量
     */
    private Integer signalCount;
}
