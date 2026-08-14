package com.tbs.mq.impl;

import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.api.consumer.IMessageRepublisher;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 失败消息重投递分发器
 * <p>
 * 纯分发职责：一轮内从{@link IFailedMessageStore}取出到期消息，
 * 逐条交给{@link IMessageRepublisher}重投递。本类不持有任何调度线程，
 * 触发方式由外部决定且互斥选择其一，避免双调度：
 * <ul>
 *     <li>{@link EmbeddedRetryScheduler} — 内嵌守护线程驱动（默认，零配置）</li>
 *     <li>{@link RetryDispatchBackgroundJob} — 经IBackgroundJob桥接外部调度平台（如xxl-job）</li>
 * </ul>
 * 多实例部署下依赖失败存储"取出即锁定"的原子语义避免重复重投递。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RetryMessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RetryMessageDispatcher.class);

    /**
     * 单轮调度最大取出条数
     */
    private static final int FETCH_BATCH_SIZE = 50;

    /**
     * 失败消息存储，未装配时单轮直接跳过
     */
    private final IFailedMessageStore failedMessageStore;

    /**
     * 消息重投递器
     */
    private final IMessageRepublisher messageRepublisher;

    /**
     * 构造重投递分发器
     *
     * @param failedMessageStore 失败消息存储，允许为null（分发不生效）
     * @param messageRepublisher 消息重投递器，允许为null（分发不生效）
     */
    public RetryMessageDispatcher(IFailedMessageStore failedMessageStore, IMessageRepublisher messageRepublisher) {
        this.failedMessageStore = failedMessageStore;
        this.messageRepublisher = messageRepublisher;
    }

    /**
     * 重投递一轮到期消息
     * <p>
     * 公开入口，由且仅由一种调度驱动调用；存储或重投递器缺失时静默跳过。
     * </p>
     */
    public void dispatchDue() {
        if (failedMessageStore == null || messageRepublisher == null) {
            return;
        }
        List<FailedMessage> dueMessages = failedMessageStore.fetchDue(new Date(), FETCH_BATCH_SIZE);
        if (CollUtil.isEmpty(dueMessages)) {
            return;
        }
        log.debug("本轮取出到期失败消息{}条", dueMessages.size());
        for (FailedMessage failedMessage : dueMessages) {
            messageRepublisher.republish(failedMessage);
        }
    }

    /**
     * 重投递一轮到期消息（异常安全）
     * <p>
     * 供调度驱动方在长驻线程内调用，单轮异常仅记日志不外抛，保证调度线程存活。
     * </p>
     */
    public void dispatchDueSafely() {
        try {
            dispatchDue();
        } catch (Exception e) {
            log.error("失败消息重投递调度异常", e);
        }
    }

}
