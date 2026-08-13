package com.tbs.mq.api.consumer;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.mq.api.exception.MqConsumeException;
import com.tbs.mq.api.exception.MqDeadLetterException;
import com.tbs.mq.api.idempotent.IIdempotentStore;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.model.DispatchOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * 消息处理分派基类
 * <p>
 * 将消费链路的通用编排固化为模板方法{@link #execute(Object)}：
 * <ol>
 *     <li>幂等占位：解析幂等键并经IIdempotentStore互斥占位，重复消息直接拦截</li>
 *     <li>业务分派：调用{@link #dispatch(Object)}</li>
 *     <li>结果固化：成功则标记完成；失败则释放占位、触发{@link #onFailure(Object, Throwable)}回调、
 *         并按异常类型分类出{@link DispatchOutcome}</li>
 * </ol>
 * 具体MQ厂商的消费基类只需对接容器（ACK/原始消息字节），
 * 并按返回的{@link DispatchOutcome}决定持久化重试或死信留痕，无需重复编排上述流程。
 * </p>
 *
 * @param <T> 消息体类型
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractMessageDispatcher<T> implements IMessageDispatcher<T> {

    protected static final Logger log = LoggerFactory.getLogger(AbstractMessageDispatcher.class);

    /**
     * 默认幂等键TTL：24小时，实现方可按最大重试窗口调整
     */
    private static final Duration DEFAULT_IDEMPOTENT_TTL = Duration.ofHours(24);

    /**
     * 幂等存储，未注入时不参与幂等控制
     */
    private IIdempotentStore idempotentStore;

    /**
     * 失败消息存储，未注入时不支持重试持久化
     */
    private IFailedMessageStore failedMessageStore;

    /**
     * 重试延迟策略
     */
    private IRetryDelayPolicy retryDelayPolicy;

    /**
     * 幂等键TTL
     */
    private Duration idempotentTtl = DEFAULT_IDEMPOTENT_TTL;

    /**
     * 分派编排模板
     * <p>
     * 幂等占位→业务分派→结果固化的完整流程，返回分派终态。 本方法不抛出异常，所有失败均收敛为{@link DispatchOutcome}。
     * </p>
     *
     * @param message 反序列化后的消息体
     * @return 分派结果
     */
    public final DispatchOutcome execute(T message) {
        if (!tryAcquireIdempotent(message)) {
            log.debug("幂等拦截重复消息, key:{}", resolveIdempotentKey(message));
            return DispatchOutcome.DUPLICATE_SKIPPED;
        }
        try {
            dispatch(message);
            markIdempotentDone(message);
            return DispatchOutcome.SUCCESS;
        } catch (Throwable error) {
            releaseIdempotent(message);
            safeInvokeOnFailure(message, error);
            return classify(error);
        }
    }

    /**
     * 解析幂等键（默认返回null，即不参与幂等控制）
     * <p>
     * 子类覆盖以提供业务幂等键；消息messageId与内容摘要的兜底逻辑 由具体消费基类在覆盖时组合。
     * </p>
     *
     * @param message 消息体
     * @return 幂等键，null表示跳过幂等
     */
    @Override
    public String resolveIdempotentKey(T message) {
        return null;
    }

    /**
     * 失败回调（默认仅记录日志）
     * <p>
     * 在重试/死信决策之前执行，子类可覆盖为告警、留痕等自定义逻辑。
     * </p>
     *
     * @param message 处理失败的消息体
     * @param error   异常信息
     */
    @Override
    public void onFailure(T message, Throwable error) {
        log.error("消息处理失败", error);
    }

    /**
     * 获取失败消息存储
     *
     * @return 失败消息存储，未注入时为null
     */
    @Override
    public IFailedMessageStore getFailedMessageStore() {
        return failedMessageStore;
    }

    /**
     * 是否启用幂等控制
     *
     * @return true表示已注入幂等存储
     */
    protected boolean idempotentEnabled() {
        return idempotentStore != null;
    }

    /**
     * 幂等占位封装
     * <p>
     * 幂等未启用或幂等键为空时直接放行。
     * </p>
     *
     * @param message 消息体
     * @return true表示占位成功或无需占位，可以继续处理
     */
    protected final boolean tryAcquireIdempotent(T message) {
        String key = resolveIdempotentKey(message);
        if (key == null || idempotentStore == null) {
            return true;
        }
        return idempotentStore.tryAcquire(key, idempotentTtl);
    }

    /**
     * 幂等完成态固化封装
     *
     * @param message 处理成功的消息体
     */
    protected final void markIdempotentDone(T message) {
        String key = resolveIdempotentKey(message);
        if (key == null || idempotentStore == null) {
            return;
        }
        idempotentStore.markDone(key, idempotentTtl);
    }

    /**
     * 幂等占位释放封装
     *
     * @param message 处理失败的消息体
     */
    protected final void releaseIdempotent(T message) {
        String key = resolveIdempotentKey(message);
        if (key == null || idempotentStore == null) {
            return;
        }
        idempotentStore.release(key);
    }

    /**
     * 异常分类
     * <p>
     * 判定顺序：
     * <ol>
     *     <li>{@link MqDeadLetterException} → 死信</li>
     *     <li>{@link MqConsumeException}其他子类 → 按retryable()判定</li>
     *     <li>存量{@link FormattedRuntimeException} → 业务逻辑错误及以下级别视为业务性失败，
     *         更高级别视为可重试</li>
     *     <li>其他未知异常 → 视为可重试</li>
     * </ol>
     * </p>
     *
     * @param error 分派过程中抛出的异常
     * @return 对应的分派结果（仅失败态）
     */
    protected DispatchOutcome classify(Throwable error) {
        if (error instanceof MqDeadLetterException) {
            return DispatchOutcome.DEAD_LETTER;
        }
        if (error instanceof MqConsumeException) {
            return ((MqConsumeException)error).retryable() ? DispatchOutcome.RETRY_SCHEDULED
                : DispatchOutcome.BUSINESS_DISCARDED;
        }
        return isLegacyBusinessException(error) ? DispatchOutcome.BUSINESS_DISCARDED : DispatchOutcome.RETRY_SCHEDULED;
    }

    /**
     * 存量异常的业务性判定
     * <p>
     * 未接入MQ异常体系的{@link FormattedRuntimeException}， 按错误级别兜底：业务逻辑错误及以下级别重试无意义。
     * </p>
     *
     * @param error 异常
     * @return true表示属于业务性失败
     */
    protected boolean isLegacyBusinessException(Throwable error) {
        if (error instanceof FormattedRuntimeException) {
            ErrorLevelEnum level = ((FormattedRuntimeException)error).getLevel();
            return level != null && level.getCode() <= ErrorLevelEnum.BUSINESS_LOGIC_ERROR.getCode();
        }
        return false;
    }

    /**
     * 异常安全地触发失败回调
     * <p>
     * 回调自身的异常不得影响主流程的分派结果判定。
     * </p>
     *
     * @param message 处理失败的消息体
     * @param error   异常信息
     */
    private void safeInvokeOnFailure(T message, Throwable error) {
        try {
            onFailure(message, error);
        } catch (Throwable callbackError) {
            log.warn("onFailure回调执行异常", callbackError);
        }
    }

    /**
     * 注入幂等存储
     *
     * @param idempotentStore 幂等存储
     * @return 当前对象
     */
    public AbstractMessageDispatcher<T> idempotentStore(IIdempotentStore idempotentStore) {
        this.idempotentStore = idempotentStore;
        return this;
    }

    /**
     * 注入失败消息存储
     *
     * @param failedMessageStore 失败消息存储
     * @return 当前对象
     */
    public AbstractMessageDispatcher<T> failedMessageStore(IFailedMessageStore failedMessageStore) {
        this.failedMessageStore = failedMessageStore;
        return this;
    }

    /**
     * 注入重试延迟策略
     *
     * @param retryDelayPolicy 重试延迟策略
     * @return 当前对象
     */
    public AbstractMessageDispatcher<T> retryDelayPolicy(IRetryDelayPolicy retryDelayPolicy) {
        this.retryDelayPolicy = retryDelayPolicy;
        return this;
    }

    /**
     * 注入幂等键TTL
     *
     * @param idempotentTtl 幂等键有效期
     * @return 当前对象
     */
    public AbstractMessageDispatcher<T> idempotentTtl(Duration idempotentTtl) {
        if (idempotentTtl != null) {
            this.idempotentTtl = idempotentTtl;
        }
        return this;
    }

    /**
     * 获取重试延迟策略
     *
     * @return 重试延迟策略，未注入时为null
     */
    protected IRetryDelayPolicy getRetryDelayPolicy() {
        return retryDelayPolicy;
    }

}
