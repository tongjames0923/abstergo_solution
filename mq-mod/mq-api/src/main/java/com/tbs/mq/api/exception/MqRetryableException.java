package com.tbs.mq.api.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;

/**
 * 可重试MQ消费异常
 * <p>
 * 表示瞬时性系统故障（如下游服务不可用、数据库连接抖动等），
 * 重试后有可能成功。消费基类捕获后将消息快照入库，
 * 由调度器按延迟策略重新投递，直至成功或耗尽最大重试次数。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class MqRetryableException extends MqConsumeException {

    /**
     * 构造可重试消费异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public MqRetryableException(String message, Object... params) {
        super(message, params);
    }

    /**
     * 构造带原因异常的可重试消费异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> MqRetryableException(T cause, String message, Object... params) {
        super(cause, message, params);
    }

    /**
     * 设置异常级别（协变返回）
     *
     * @param errorLevel 异常级别
     * @return 当前异常对象
     */
    @Override
    public MqRetryableException errorLevel(ErrorLevelEnum errorLevel) {
        super.errorLevel(errorLevel);
        return this;
    }

    /**
     * 创建可重试消费异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 可重试级别（默认系统错误）的异常
     */
    public static MqRetryableException retryable(String message, Object... params) {
        return new MqRetryableException(message, params)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 创建可重试消费异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 可重试级别（默认系统错误）的异常
     */
    public static MqRetryableException retryableI18n(String i18nKey, Object... params) {
        return new MqRetryableException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 是否允许重投递
     *
     * @return 恒为true
     */
    @Override
    public boolean retryable() {
        return true;
    }

}
