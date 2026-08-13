package com.tbs.mq.api.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * MQ消费异常基类
 * <p>
 * 消费端通过异常类型直接判定后续处理策略，替代单纯的错误级别判断：
 * <ul>
 *     <li>{@link MqRetryableException} — 瞬时系统故障，入库等待延迟重投递</li>
 *     <li>{@link MqBusinessException} — 业务性错误，重试无意义，不重试</li>
 *     <li>{@link MqDeadLetterException} — 跳过重试直接进入死信留痕</li>
 * </ul>
 * 未抛出本体系异常的，消费基类仍按{@link ErrorLevelEnum}级别兜底判定。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class MqConsumeException extends FormattedRuntimeException {

    /**
     * 构造MQ消费异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public MqConsumeException(String message, Object... params) {
        super(message, params);
    }

    /**
     * 构造带原因异常的MQ消费异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     */
    public <T extends Throwable> MqConsumeException(T cause, String message, Object... params) {
        super(cause, message, params);
    }

    /**
     * 是否允许重投递
     * <p>
     * true表示消费失败后进入失败存储等待延迟重投； false表示不再重试，按配置丢弃或转入死信留痕。
     * </p>
     *
     * @return true表示可重试
     */
    public abstract boolean retryable();

}
