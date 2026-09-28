package com.tbs.push.model.session;

import com.tbs.common.BaseEnum;

/**
 * 连接状态枚举
 * <p>
 * 会话状态机的三个状态，全部状态变更经会话注册表与会话实现单点维护： CONNECTING（已注册未握手）→ CONNECTED（握手完成可用）→ CLOSED（已关闭，终态）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum ConnectionStateEnum implements BaseEnum<String> {

    /**
     * 传输已建立但尚未完成协议握手（HELLO），此时不允许投递业务数据
     */
    CONNECTING("connecting"),

    /**
     * 握手完成，会话可用
     */
    CONNECTED("connected"),

    /**
     * 会话已关闭（终态），订阅关系已清理
     */
    CLOSED("closed");

    /**
     * 状态小写值
     */
    private final String value;

    /**
     * 构造连接状态枚举
     *
     * @param value 状态小写值
     */
    ConnectionStateEnum(String value) {
        this.value = value;
    }

    /**
     * 获取状态小写值
     *
     * @return 状态小写值
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
