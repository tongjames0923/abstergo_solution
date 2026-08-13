package com.tbs.rabbitmq.retry;

import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.api.consumer.IMessageRepublisher;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 失败消息重投递调度器
 * <p>
 * 周期性从{@link IFailedMessageStore}取出到期消息，逐条交给{@link IMessageRepublisher}重投递。
 * 调度器自持单线程守护调度线程，不依赖应用全局的@EnableScheduling开关；
 * 多实例部署下依赖失败存储"取出即锁定"的原子语义避免重复重投递。
 * </p>
 * <p>
 * {@link #dispatchDue()}为公开入口，除内置定时调度外，也可由外部调度平台（如xxl-job）触发，
 * 以便在集群规模较大时改为集中式调度。
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
     * 调度间隔（毫秒）
     */
    private static final long FIXED_DELAY_MILLIS = 10_000L;

    /**
     * 失败消息存储，未装配真实实现时调度器不启动
     */
    private final IFailedMessageStore failedMessageStore;

    /**
     * 消息重投递器
     */
    private final IMessageRepublisher messageRepublisher;

    /**
     * 内置调度线程
     */
    private ScheduledExecutorService scheduler;

    /**
     * 构造重投递调度器
     *
     * @param failedMessageStore 失败消息存储，允许为null（调度器不启动）
     * @param messageRepublisher 消息重投递器，允许为null（调度器不启动）
     */
    public RetryMessageDispatcher(IFailedMessageStore failedMessageStore, IMessageRepublisher messageRepublisher) {
        this.failedMessageStore = failedMessageStore;
        this.messageRepublisher = messageRepublisher;
    }

    /**
     * 启动内置调度
     * <p>
     * 失败存储或重投递器缺失时跳过启动（重试能力未装配完整）。
     * </p>
     */
    @PostConstruct
    public void start() {
        if (failedMessageStore == null || messageRepublisher == null) {
            log.info("失败消息存储或重投递器未装配, 失败消息重投递调度器不启动");
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "mq-retry-dispatcher");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::dispatchDueSafely, FIXED_DELAY_MILLIS, FIXED_DELAY_MILLIS,
            TimeUnit.MILLISECONDS);
        log.info("失败消息重投递调度器已启动, 调度间隔={}ms", FIXED_DELAY_MILLIS);
    }

    /**
     * 停止内置调度
     */
    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * 重投递一轮到期消息
     * <p>
     * 公开入口，可由外部调度平台（如xxl-job）直接调用；单轮异常不外抛，保证调度线程存活。
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
     * 调度线程内的异常安全包装
     */
    private void dispatchDueSafely() {
        try {
            dispatchDue();
        } catch (Exception e) {
            log.error("失败消息重投递调度异常", e);
        }
    }

}
