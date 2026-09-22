package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 推送通知发送结果视图
 * <p>
 * 发送接口的返回契约：除消息ID外，回传实际投递主题、解析到的接收人数与信令发布数， 供调用方（管理端发布页）构建真实送达回执——HTTP 请求成功不等于送达，送达规模以 receiverCount 为准。
 * 接收人为空是合法状态（消息仅落库、不产生信令，receiverCount=0）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushSendResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（主表自增主键）
     */
    private Long messageId;

    /**
     * 实际投递主题（表单未传时为缺省 global.broadcast）
     */
    private String topic;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * SPI 解析并去重后的接收人数（fan-out 落库行数；0 表示未解析到任何接收人）
     */
    private Integer receiverCount;

    /**
     * 事务提交后成功发布的 SIGNAL 信令数（推送未启用或全部失败时为 0）
     */
    private Integer signalCount;

}
