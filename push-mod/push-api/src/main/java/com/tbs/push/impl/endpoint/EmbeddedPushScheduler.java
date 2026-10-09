package com.tbs.push.impl.endpoint;

import com.tbs.push.api.dispatch.IPushDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内嵌式推送分发调度器
 * <p>
 * 调度驱动之一：自持单线程守护调度线程周期性触发分发器， 不依赖应用全局的@EnableScheduling开关，零配置开箱即用。 与{@link PushDispatchBackgroundJob}（外部调度平台驱动）互斥，
 * 容器中装配了后台任务桥接时本调度器不装配，避免双调度。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class EmbeddedPushScheduler {

    /**
     * 默认调度间隔（毫秒）
     */
    public static final long DEFAULT_FIXED_DELAY_MILLIS = 5_000L;
    private static final Logger log = LoggerFactory.getLogger(EmbeddedPushScheduler.class);
    /**
     * 推送消息分发器
     */
    private final IPushDispatcher dispatcher;

    /**
     * 调度间隔（毫秒）
     */
    private final long fixedDelayMillis;

    /**
     * 内置调度线程
     */
    private ScheduledExecutorService scheduler;

    /**
     * 构造内嵌式推送调度器（默认调度间隔）
     *
     * @param dispatcher 推送消息分发器
     */
    public EmbeddedPushScheduler(IPushDispatcher dispatcher) {
        this(dispatcher, DEFAULT_FIXED_DELAY_MILLIS);
    }

    /**
     * 构造内嵌式推送调度器
     *
     * @param dispatcher       推送消息分发器
     * @param fixedDelayMillis 调度间隔（毫秒），非正数时按默认间隔处理
     */
    public EmbeddedPushScheduler(IPushDispatcher dispatcher, long fixedDelayMillis) {
        this.dispatcher = dispatcher;
        this.fixedDelayMillis = fixedDelayMillis > 0 ? fixedDelayMillis : DEFAULT_FIXED_DELAY_MILLIS;
    }

    /**
     * 启动内置调度
     */
    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "push-dispatcher");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(dispatcher::dispatchDueSafely, fixedDelayMillis, fixedDelayMillis,
            TimeUnit.MILLISECONDS);
        log.info("推送消息分发内嵌调度器已启动, 调度间隔={}ms", fixedDelayMillis);
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
