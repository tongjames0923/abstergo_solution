package com.tbs.mq.api.consumer;

import com.tbs.common.error.AssertUtil;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.constants.MqI18nConstant;
import com.tbs.mq.model.FailedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 失败消息重投递基类
 * <p>
 * 将重投递链路的通用记账固化为模板方法{@link #republish(FailedMessage)}：
 * <ol>
 *     <li>递增重试状态：retryCount+1并同步写入消息头（x-retry-count），首次失败时间头兜底补齐</li>
 *     <li>委托{@link #doRepublish(FailedMessage)}执行实际发送</li>
 *     <li>成功则从失败存储移除记录；失败则按延迟策略计算下次重试时间重新入库</li>
 * </ol>
 * 具体MQ厂商的实现只需覆盖{@link #doRepublish(FailedMessage)}完成消息还原与发送，
 * 无需关心重试计数与存储记账。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractMessageRepublisher implements IMessageRepublisher {

    protected static final Logger log = LoggerFactory.getLogger(AbstractMessageRepublisher.class);

    /**
     * 失败消息存储
     */
    private final IFailedMessageStore failedMessageStore;

    /**
     * 重试延迟策略
     */
    private final IRetryDelayPolicy retryDelayPolicy;

    /**
     * 构造重投递基类
     *
     * @param failedMessageStore 失败消息存储，不能为空
     * @param retryDelayPolicy   重试延迟策略，不能为空
     */
    public AbstractMessageRepublisher(IFailedMessageStore failedMessageStore, IRetryDelayPolicy retryDelayPolicy) {
        AssertUtil.isTrue(failedMessageStore == null).i18n(MqI18nConstant.REPUBLISHER_FAILED_STORE_REQUIRED).throwIt();
        AssertUtil.isTrue(retryDelayPolicy == null).i18n(MqI18nConstant.REPUBLISHER_DELAY_POLICY_REQUIRED).throwIt();
        this.failedMessageStore = failedMessageStore;
        this.retryDelayPolicy = retryDelayPolicy;
    }

    /**
     * 重投递编排模板
     * <p>
     * 递增重试状态→实际发送→按结果记账，不抛出异常。
     * </p>
     *
     * @param failedMessage 从失败存储中取出的消息记录
     * @return true表示重投递成功（记录已移除）；false表示发送失败（记录已重新排期入库）
     */
    @Override
    public final boolean republish(FailedMessage failedMessage) {
        incrementRetryState(failedMessage);
        boolean success;
        try {
            success = doRepublish(failedMessage);
        } catch (Exception error) {
            log.error("消息重投递异常, id:{}", failedMessage.getId(), error);
            success = false;
        }
        if (success) {
            failedMessageStore.remove(failedMessage.getId());
        } else {
            scheduleNextRetry(failedMessage);
        }
        return success;
    }

    /**
     * 实际重投递动作
     * <p>
     * 实现方负责将{@link FailedMessage}还原为原始消息（保留messageId与消息头）并发送。
     * </p>
     *
     * @param failedMessage 已递增重试状态的消息记录
     * @return true表示发送成功
     */
    protected abstract boolean doRepublish(FailedMessage failedMessage);

    /**
     * 递增重试状态封装
     * <p>
     * retryCount加1，并同步到消息头x-retry-count； 首次失败时间头不存在时按记录的firstFailTime补齐。
     * </p>
     *
     * @param failedMessage 消息记录
     */
    protected final void incrementRetryState(FailedMessage failedMessage) {
        failedMessage.setRetryCount(failedMessage.getRetryCount() + 1);
        Map<String, Object> headers = failedMessage.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
            failedMessage.setHeaders(headers);
        }
        headers.put(MqHeaderConstants.RETRY_COUNT, failedMessage.getRetryCount());
        if (failedMessage.getFirstFailTime() != null) {
            headers.putIfAbsent(MqHeaderConstants.FIRST_FAIL_TIME, failedMessage.getFirstFailTime().getTime());
        }
    }

    /**
     * 失败重新排期封装
     * <p>
     * 按延迟策略计算下次重试时间并重新入库，等待下一轮调度。
     * </p>
     *
     * @param failedMessage 重投递失败的消息记录
     */
    protected final void scheduleNextRetry(FailedMessage failedMessage) {
        Duration delay = retryDelayPolicy.nextDelay(failedMessage.getRetryCount());
        failedMessage.setNextRetryTime(Date.from(Instant.now().plus(delay)));
        failedMessageStore.save(failedMessage);
        log.warn("消息重投递失败, 已重新排期, id:{}, retryCount:{}, nextRetryTime:{}", failedMessage.getId(),
            failedMessage.getRetryCount(), failedMessage.getNextRetryTime());
    }

    /**
     * 获取失败消息存储
     *
     * @return 失败消息存储
     */
    protected IFailedMessageStore getFailedMessageStore() {
        return failedMessageStore;
    }

    /**
     * 获取重试延迟策略
     *
     * @return 重试延迟策略
     */
    protected IRetryDelayPolicy getRetryDelayPolicy() {
        return retryDelayPolicy;
    }

}
