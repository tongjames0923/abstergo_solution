package com.tbs.push.model.frame;

import com.tbs.common.BaseEnum;
import com.tbs.push.constants.PushI18nConstant;

/**
 * 协议错误码枚举
 * <p>
 * 定义ERROR帧携带的协议错误码，每个错误码绑定一个i18n key用于生成面向用户的错误消息。 错误码段预留：1xxx协议错误，2xxx传输错误，3xxx会话错误。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum PushErrorCodeEnum implements BaseEnum<String> {

    /**
     * 协议版本不兼容：握手阶段版本协商失败
     */
    PROTOCOL_VERSION_MISMATCH(1001, PushI18nConstant.PROTOCOL_VERSION_MISMATCH),

    /**
     * 帧格式损坏：无法反序列化或必填字段缺失
     */
    FRAME_MALFORMED(1002, PushI18nConstant.FRAME_DECODE_FAILED),

    /**
     * 主题不合法：空、超长或包含非法字符
     */
    TOPIC_INVALID(1003, PushI18nConstant.TOPIC_INVALID),

    /**
     * 不支持的帧类型
     */
    FRAME_TYPE_UNSUPPORTED(1004, PushI18nConstant.FRAME_TYPE_UNSUPPORTED),

    /**
     * 会话不存在（SSE组合通道上行时sessionId失效）
     */
    SESSION_NOT_FOUND(1005, PushI18nConstant.SESSION_NOT_FOUND),

    /**
     * 会话未就绪：未完成握手即发送业务数据
     */
    NOT_CONNECTED(1006, PushI18nConstant.SESSION_NOT_CONNECTED),

    /**
     * 服务端内部错误
     */
    INTERNAL_ERROR(1007, PushI18nConstant.INTERNAL_ERROR),

    /**
     * 订阅未获授权：SUBSCRIBE帧主题不在会话授权集内（回发ERROR帧，不关闭连接）
     */
    SUBSCRIBE_DENIED(1008, PushI18nConstant.SUBSCRIBE_DENIED),

    /**
     * 上行被拒：会话未通过上行守卫校验（如归属身份不符），回发ERROR帧丢弃该帧不关闭连接
     */
    UPLOAD_DENIED(1009, PushI18nConstant.UPLOAD_DENIED);

    /**
     * 错误码
     */
    private final int code;

    /**
     * 绑定的i18n资源key
     */
    private final String i18nKey;

    /**
     * 构造协议错误码枚举
     *
     * @param code    错误码
     * @param i18nKey 绑定的i18n资源key
     */
    PushErrorCodeEnum(int code, String i18nKey) {
        this.code = code;
        this.i18nKey = i18nKey;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取绑定的i18n资源key
     *
     * @return i18n资源key
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * 获取枚举详情值（返回错误码的字符串形式）
     *
     * @return 错误码字符串
     */
    @Override
    public String getValue() {
        return String.valueOf(code);
    }

}
