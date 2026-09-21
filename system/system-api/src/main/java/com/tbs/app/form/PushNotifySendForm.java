package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 推送通知发送表单
 * <p>
 * 新链路（信令+拉取）的唯一发送载荷契约：服务端生成消息ID，接收人不随表单传递 （由 INotifyReceiverResolver SPI 解析）。HTTP 场景经 POST /private/push/notify 提交，
 * 程序化调用（RPC/Job）直接构造本表单调用 IPushNotifyService.send 并声明 source。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushNotifySendForm implements Serializable {

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
     * 优先级白名单
     */
    public static final java.util.Set<String> SUPPORTED_PRIORITIES =
        java.util.Set.of(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT);
    private static final long serialVersionUID = 1L;
    /**
     * 消息类型（业务可字典驱动扩展，服务端仅校验非空与长度）
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
     * 优先级（可选，白名单校验，缺省 normal）
     */
    private String priority;

    /**
     * 业务主题（可选，≤128字符，缺省 global.broadcast）
     */
    private String topic;

    /**
     * 发送者展示标识（可选，HTTP 场景缺省取当前登录用户）
     */
    private String sender;

    /**
     * 点击跳转地址（可选，≤512字符）
     */
    private String url;

    /**
     * 业务扩展字段（可选，原样序列化为 ext_info JSON）
     */
    private Object extInfo;

    /**
     * 来源通道（可选：http/rpc/job/system，缺省 http；SEND 操作日志 op_source 取此值）
     */
    private String source;

}
