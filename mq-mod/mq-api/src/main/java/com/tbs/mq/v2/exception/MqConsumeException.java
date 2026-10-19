package com.tbs.mq.v2.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;

/**
 * MQ消费异常（v2 记录驱动模型）
 * <p>
 * 消费失败时抛出，携带三要素供上下文与记录层分派重试策略：
 * <ul>
 *     <li>{@code consumeId} — 失败发生在那个消费者上，重投记录按此粒度追踪进度；</li>
 *     <li>{@code businessId} — 业务主键，用于幂等去重与人工排查定位；</li>
 *     <li>{@code retryFlag} — 是否允许按记录重试，false 表示不再重投（留痕后终态）。</li>
 * </ul>
 * 使用方式：
 * <pre>
 * // 可重试的瞬时故障（默认系统错误级别，retryFlag=true）
 * throw MqConsumeException.retryable("下游RPC超时").consumeId(consumeId).businessId(businessId);
 *
 * // 带原因、不可重试，直接终态留痕
 * throw MqConsumeException.discard(cause, "订单{}状态非法", orderId).consumeId(consumeId);
 *
 * // i18n 消息
 * throw MqConsumeException.retryableI18n("mq.consume.rpc.timeout").consumeId(consumeId);
 * </pre>
 * 身份键类型与消费台账对齐：{@code consumeId} 取 {@code IMqConsumer.consumeId()}（Integer），
 * {@code businessId} 为业务主键字符串。
 *
 * @author tongj
 * @since 2.0.0
 */
public class MqConsumeException extends FormattedRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认允许重试：未显式声明策略的失败按瞬时故障对待，重试上限由记录层控制
     */
    private static final boolean DEFAULT_RETRY_FLAG = true;

    /**
     * 消费者id，与 {@code IMqConsumer.consumeId()}、{@code MqConsumeVO.consumeId} 同型
     */
    private Integer consumeId;

    /**
     * 消费业务id
     */
    private String businessId;

    /**
     * 是否允许按记录重试
     */
    private boolean retryFlag = DEFAULT_RETRY_FLAG;

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
     * 设置异常级别（协变返回）
     *
     * @param errorLevel 异常级别
     * @return 当前异常对象
     */
    @Override
    public MqConsumeException errorLevel(ErrorLevelEnum errorLevel) {
        super.errorLevel(errorLevel);
        return this;
    }

    /**
     * 设置消费者id
     *
     * @param consumeId 失败发生的消费者标识
     * @return 当前异常对象
     */
    public MqConsumeException consumeId(Integer consumeId) {
        this.consumeId = consumeId;
        return this;
    }

    /**
     * 设置消费业务id
     *
     * @param businessId 业务主键
     * @return 当前异常对象
     */
    public MqConsumeException businessId(String businessId) {
        this.businessId = businessId;
        return this;
    }

    /**
     * 设置是否允许按记录重试
     *
     * @param retryFlag true表示可重试，false表示终态留痕不再重投
     * @return 当前异常对象
     */
    public MqConsumeException retryFlag(boolean retryFlag) {
        this.retryFlag = retryFlag;
        return this;
    }

    /**
     * 获取消费者id
     *
     * @return 失败发生的那个消费者标识，可能为空
     */
    public Integer getConsumeId() {
        return consumeId;
    }

    /**
     * 获取消费业务id
     *
     * @return 业务主键，可能为空
     */
    public String getBusinessId() {
        return businessId;
    }

    /**
     * 是否允许按记录重试
     *
     * @return true表示记录层应安排重投，false表示留痕后进入终态
     */
    public boolean retryable() {
        return retryFlag;
    }

    // ======================== 静态工厂方法（直接消息）========================

    /**
     * 创建可重试的消费异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 允许按记录重试、系统错误级别的异常
     */
    public static MqConsumeException retryable(String message, Object... params) {
        return new MqConsumeException(message, params).retryFlag(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 创建不可重试的消费异常
     * <p>
     * 业务性错误等重试无意义的失败使用，记录层留痕后不再重投。
     * </p>
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 禁止重试、业务逻辑错误级别的异常
     */
    public static MqConsumeException discard(String message, Object... params) {
        return new MqConsumeException(message, params).retryFlag(false).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    // ======================== 静态工厂方法（带原因异常）========================

    /**
     * 创建可重试的消费异常（带原因）
     *
     * @param cause   原因异常
     * @param message 错误消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     * @return 允许按记录重试、系统错误级别的异常
     */
    public static <T extends Throwable> MqConsumeException retryable(T cause, String message, Object... params) {
        return new MqConsumeException(cause, message, params).retryFlag(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 创建不可重试的消费异常（带原因）
     *
     * @param cause   原因异常
     * @param message 错误消息模板
     * @param params  消息参数
     * @param <T>     原因异常类型
     * @return 禁止重试、业务逻辑错误级别的异常
     */
    public static <T extends Throwable> MqConsumeException discard(T cause, String message, Object... params) {
        return new MqConsumeException(cause, message, params).retryFlag(false)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    // ======================== 静态工厂方法（i18n支持）========================

    /**
     * 创建可重试的消费异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 允许按记录重试、系统错误级别的异常
     */
    public static MqConsumeException retryableI18n(String i18nKey, Object... params) {
        return retryable(I18nUtil.get(i18nKey, params));
    }

    /**
     * 创建不可重试的消费异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 禁止重试、业务逻辑错误级别的异常
     */
    public static MqConsumeException discardI18n(String i18nKey, Object... params) {
        return discard(I18nUtil.get(i18nKey, params));
    }
}
