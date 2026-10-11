package com.tbs.mq.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内嵌式重试调度器
 * <p>
 * 调度驱动之一：自持单线程守护调度线程周期性触发分发器，
 * 不依赖应用全局的@EnableScheduling开关，零配置开箱即用。
 * 与{@link RetryDispatchBackgroundJob}（外部调度平台驱动）互斥，
 * 容器中装配了后台任务桥接时本调度器不装配，避免双调度。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class EmbeddedRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedRetryScheduler.class);

    /**
     * 默认调度间隔（毫秒）
     */
    public static final long DEFAULT_FIXED_DELAY_MILLIS = 10_000L;

    /**
     * 重投递分发器
     */
    private final RetryMessageDispatcher dispatcher;

    /**
     * 调度间隔（毫秒）
     */
    private final long fixedDelayMillis;

    /**
     * 内置调度线程
     */
    private ScheduledExecutorService scheduler;

    /**
     * 构造内嵌式重试调度器（默认调度间隔）
     *
     * @param dispatcher 重投递分发器
     */
    public EmbeddedRetryScheduler(RetryMessageDispatcher dispatcher) {
        this(dispatcher, DEFAULT_FIXED_DELAY_MILLIS);
    }

    /**
     * 构造内嵌式重试调度器
     *
     * @param dispatcher       重投递分发器
     * @param fixedDelayMillis 调度间隔（毫秒），非正数时按默认间隔处理
     */
    public EmbeddedRetryScheduler(RetryMessageDispatcher dispatcher, long fixedDelayMillis) {
        this.dispatcher = dispatcher;
        this.fixedDelayMillis = fixedDelayMillis > 0 ? fixedDelayMillis : DEFAULT_FIXED_DELAY_MILLIS;
    }

    /**
     * 启动内置调度
     */
    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "mq-retry-dispatcher");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(dispatcher::dispatchDueSafely, fixedDelayMillis, fixedDelayMillis,
            TimeUnit.MILLISECONDS);
        log.info("失败消息重投递内嵌调度器已启动, 调度间隔={}ms", fixedDelayMillis);
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

}
