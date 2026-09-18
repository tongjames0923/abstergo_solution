package com.tbs.push.api.exception;

import com.tbs.common.error.ErrorLevelEnum;

/**
 * 推送传输异常
 * <p>
 * 传输通道层面的失败时抛出：连接不可用、底层发送调用异常、会话关闭失败等。
 * 默认错误级别为系统错误，可通过errorLevel调整。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushTransportException extends PushException {

    /**
     * 构造传输异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public PushTransportException(String message, Object... params) {
        super(message, params);
        errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 构造带原因异常的传输异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> PushTransportException(T cause, String message, Object... params) {
        super(cause, message, params);
        errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 以i18n key创建传输异常
     *
     * @param i18nKey i18n资源key
     * @param params  消息参数
     * @return 传输异常
     */
    public static PushTransportException transportI18n(String i18nKey, Object... params) {
        return new PushTransportException(com.tbs.common.i18n.I18nUtil.get(i18nKey, params));
    }

}
