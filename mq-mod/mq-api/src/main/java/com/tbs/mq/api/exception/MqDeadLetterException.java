package com.tbs.mq.api.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;

/**
 * 死信MQ消费异常
 * <p>
 * 表示消息已确定不可消费（如毒消息、格式彻底损坏、人工判定终止等）。 消费基类捕获后跳过重试存储，直接将消息转入死信队列留痕， 并触发handleDeadLetter钩子。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class MqDeadLetterException extends MqConsumeException {

    /**
     * 构造死信消费异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public MqDeadLetterException(String message, Object... params) {
        super(message, params);
    }

    /**
     * 构造带原因异常的死信消费异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> MqDeadLetterException(T cause, String message, Object... params) {
        super(cause, message, params);
    }

    /**
     * 设置异常级别（协变返回）
     *
     * @param errorLevel 异常级别
     * @return 当前异常对象
     */
    @Override
    public MqDeadLetterException errorLevel(ErrorLevelEnum errorLevel) {
        super.errorLevel(errorLevel);
        return this;
    }

    /**
     * 创建死信消费异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 严重系统错误级别的异常
     */
    public static MqDeadLetterException deadLetter(String message, Object... params) {
        return new MqDeadLetterException(message, params).errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }

    /**
     * 创建死信消费异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 严重系统错误级别的异常
     */
    public static MqDeadLetterException deadLetterI18n(String i18nKey, Object... params) {
        return new MqDeadLetterException(I18nUtil.get(i18nKey, params)).errorLevel(
            ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }

    /**
     * 是否允许重投递
     *
     * @return 恒为false
     */
    @Override
    public boolean retryable() {
        return false;
    }

}
