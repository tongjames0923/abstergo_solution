package com.tbs.sdk.model;

import lombok.Data;

/**
 * 推送通知发送表单
 * <p>
 * 对应服务端PushNotifySendForm契约：msgType必填，接收人不随表单传递
 * （由服务端SPI解析），source缺省由服务端按http通道处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushNotifySendForm {

    /**
     * 优先级：低
     */
    public static final String PRIORITY_LOW = "low";

    /**
     * 优先级：普通
     */
    public static final String PRIORITY_NORMAL = "normal";

    /**
     * 优先级：高
     */
    public static final String PRIORITY_HIGH = "high";

    /**
     * 优先级：紧急
     */
    public static final String PRIORITY_URGENT = "urgent";

    /**
     * 消息类型（必填，业务可字典驱动扩展）
     */
    private String msgType;

    /**
     * 标题（可选，≤255字符）
     */
    private String title;

    /**
     * 正文（可选）
     */
    private String content;

    /**
     * 优先级（可选白名单：low/normal/high/urgent，缺省normal）
     */
    private String priority;

    /**
     * 业务主题（可选，≤128字符，缺省global.broadcast）
     */
    private String topic;

    /**
     * 发送者展示标识（可选，HTTP场景缺省取当前登录用户）
     */
    private String sender;

    /**
     * 点击跳转地址（可选，≤512字符）
     */
    private String url;

    /**
     * 业务扩展字段（可选，原样序列化为ext_info JSON）
     */
    private Object extInfo;

    /**
     * 来源通道（可选：http/rpc/job/system，缺省http）
     */
    private String source;
}
