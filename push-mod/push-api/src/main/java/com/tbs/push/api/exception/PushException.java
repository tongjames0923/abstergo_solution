package com.tbs.push.api.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 推送模块异常基类
 * <p>
 * 推送链路的统一异常根类型，继承框架格式化运行时异常以获得i18n与错误级别能力：
 * <ul>
 *     <li>{@link PushProtocolException} — 协议违规（版本不兼容/帧损坏/topic非法等）</li>
 *     <li>{@link PushTransportException} — 传输通道异常（连接不可用/发送失败等）</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class PushException extends FormattedRuntimeException {

    /**
     * 构造推送异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public PushException(String message, Object... params) {
        super(message, params);
    }

    /**
     * 构造带原因异常的推送异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> PushException(T cause, String message, Object... params) {
        super(cause, message, params);
    }

}
