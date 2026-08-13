package com.tbs.mq.api.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;

/**
 * 业务性MQ消费异常
 * <p>
 * 表示业务规则校验失败、数据非法等确定性错误，重试无法改变结果。 消费基类捕获后不再重试：按配置直接丢弃或转入死信队列留痕。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class MqBusinessException extends MqConsumeException {

    /**
     * 构造业务性消费异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public MqBusinessException(String message, Object... params) {
        super(message, params);
    }

    /**
     * 构造带原因异常的业务性消费异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> MqBusinessException(T cause, String message, Object... params) {
        super(cause, message, params);
    }

    /**
     * 设置异常级别（协变返回）
     *
     * @param errorLevel 异常级别
     * @return 当前异常对象
     */
    @Override
    public MqBusinessException errorLevel(ErrorLevelEnum errorLevel) {
        super.errorLevel(errorLevel);
        return this;
    }

    /**
     * 创建业务性消费异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 业务逻辑错误级别的异常
     */
    public static MqBusinessException business(String message, Object... params) {
        return new MqBusinessException(message, params).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    /**
     * 创建业务性消费异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 业务逻辑错误级别的异常
     */
    public static MqBusinessException businessI18n(String i18nKey, Object... params) {
        return new MqBusinessException(I18nUtil.get(i18nKey, params)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
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
