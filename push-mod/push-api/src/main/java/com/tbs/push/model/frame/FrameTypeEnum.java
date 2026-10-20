package com.tbs.push.model.frame;

import com.tbs.common.BaseEnum;

/**
 * 协议帧类型枚举
 * <p>
 * 定义推送协议的全部帧类型，控制帧（HELLO/PING/PONG/SUBSCRIBE/UNSUBSCRIBE/ERROR/SHUTDOWN） 由协议层统一处理，业务数据帧为DATA。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum FrameTypeEnum implements BaseEnum<String> {

    /**
     * 握手帧：连接建立后由客户端发起（携带协议版本），服务端校验版本后回发握手确认帧
     */
    HELLO("hello"),

    /**
     * 心跳请求帧：客户端保活
     */
    PING("ping"),

    /**
     * 心跳应答帧：服务端响应
     */
    PONG("pong"),

    /**
     * 业务数据帧：双向均可发送，携带topic与payload
     */
    DATA("data"),

    /**
     * 信令帧：服务端单向通知目标频道存在新消息可拉取，不携带业务内容（payload为空），客户端不得上行
     */
    SIGNAL("signal"),

    /**
     * 订阅帧：声明对指定topic的接收意愿，服务端回发订阅确认帧
     */
    SUBSCRIBE("subscribe"),

    /**
     * 退订帧：取消对指定topic的订阅，服务端回发退订确认帧
     */
    UNSUBSCRIBE("unsubscribe"),

    /**
     * 错误帧：协议层校验失败（版本不兼容/帧损坏/topic非法等），可携带关闭意图
     */
    ERROR("error"),

    /**
     * 停机通知帧：服务端优雅停机前主动下发，告知客户端连接即将关闭（可在宽限期内完成上行后重连），客户端不得上行
     */
    SHUTDOWN("shutdown");

    /**
     * 帧类型小写值（同时用作SSE事件名）
     */
    private final String value;

    /**
     * 构造帧类型枚举
     *
     * @param value 帧类型小写值
     */
    FrameTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 获取帧类型小写值
     *
     * @return 帧类型小写值
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举编码（按声明顺序，从1开始）
     *
     * @return 枚举编码
     */
    @Override
    public Integer getCode() {
        return ordinal() + 1;
    }

}
