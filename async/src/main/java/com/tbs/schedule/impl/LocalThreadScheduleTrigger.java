package com.tbs.schedule.impl;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.common.IStartup;
import com.tbs.schedule.api.ScheduleTrigger;
import com.tbs.schedule.api.Tickable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 本地线程触发器
 * <p>
 * 进程内常驻线程阻塞轮询：持信号量实现惰性唤醒，循环驱动 {@link Tickable#tick()}。
 * 等价于原内核 {@code handle()} 主循环语义，但与内核解耦——内核不再感知线程与信号量。
 * 作为 {@link IStartup} 在应用启动后自启循环（仅 LOCAL 模式装配）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class LocalThreadScheduleTrigger implements ScheduleTrigger, IStartup {

    /**
     * 轮询等待时间
     */
    private final long pollTime;

    /**
     * 轮询等待时间单位
     */
    private final TimeUnit pollTimeUnit;

    /**
     * 运行标志
     */
    private volatile boolean running = false;

    /**
     * 惰性唤醒信号量
     */
    private final Semaphore semaphore = new Semaphore(0);

    /**
     * 被驱动的内核视图（start 时注入）
     */
    private Tickable tickable;

    /**
     * 构造本地线程触发器
     *
     * @param pollTime     轮询等待时间
     * @param pollTimeUnit 轮询等待时间单位
     */
    public LocalThreadScheduleTrigger(long pollTime, TimeUnit pollTimeUnit) {
        this.pollTime = pollTime;
        this.pollTimeUnit = pollTimeUnit;
    }

    @Override
    public void start(Tickable tickable) {
        this.tickable = tickable;
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        semaphore.release();
    }

    @Override
    public void wakeup() {
        semaphore.release();
    }

    @Override
    @LongTermAsync(value = "本地调度触发器")
    public void startUp() {
        while (running) {
            try {
                semaphore.tryAcquire(pollTime, pollTimeUnit);
            } catch (InterruptedException e) {
                log.warn("schedule trigger interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
            if (tickable != null) {
                tickable.tick();
            }
        }
    }
}
