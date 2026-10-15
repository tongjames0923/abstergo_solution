package com.tbs.sdk.model;

import java.util.Date;
import lombok.Data;

/**
 * 推送消息视图
 * <p>
 * 对应服务端PushNotifyMessageVO：messageId等Long字段在JSON中为字符串
 * （服务端防雪花ID精度丢失），Jackson反序列化兼容字符串与数字两种形态。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushNotifyMessage {

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
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String content;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 发送者展示标识
     */
    private String sender;

    /**
     * 点击跳转地址
     */
    private String url;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 当前用户是否已读
     */
    private Boolean readFlag;

    /**
     * 已读时间
     */
    private Date readTime;

    /**
     * 扩展信息
     */
    private String extraInfo;
}
