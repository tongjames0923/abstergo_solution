package com.tbs.push.api.exception;

import com.tbs.common.error.ErrorLevelEnum;

/**
 * 推送协议异常
 * <p>
 * 协议层校验失败时抛出：版本不兼容、帧格式损坏、topic非法、不支持的帧类型等。
 * 默认错误级别为用户输入错误（连接方输入不合法），可通过errorLevel调整。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushProtocolException extends PushException {

    /**
     * 构造协议异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public PushProtocolException(String message, Object... params) {
        super(message, params);
        errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 构造带原因异常的协议异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> PushProtocolException(T cause, String message, Object... params) {
        super(cause, message, params);
        errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 以i18n key创建协议异常
     *
     * @param i18nKey i18n资源key
     * @param params  消息参数
     * @return 协议异常
     */
    public static PushProtocolException protocolI18n(String i18nKey, Object... params) {
        return new PushProtocolException(com.tbs.common.i18n.I18nUtil.get(i18nKey, params));
    }

}
