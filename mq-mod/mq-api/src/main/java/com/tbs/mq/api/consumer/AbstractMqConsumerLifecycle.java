package com.tbs.mq.api.consumer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQ消费者生命周期基类
 * <p>
 * 封装启动/停止的状态守卫，保证：
 * <ul>
 *     <li>重复start/stop调用幂等，不会重复触发底层容器操作</li>
 *     <li>并发调用下的状态一致性</li>
 * </ul>
 * 具体实现只需覆盖{@link #doStart()}与{@link #doStop()}对接具体的消费容器。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractMqConsumerLifecycle implements IMqConsumerLifecycle {

    /**
     * 运行状态标记
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 启动消费者（幂等）
     */
    @Override
    public final void start() {
        if (running.compareAndSet(false, true)) {
            doStart();
        }
    }

    /**
     * 停止消费者（幂等）
     */
    @Override
    public final void stop() {
        if (running.compareAndSet(true, false)) {
            doStop();
        }
    }

    /**
     * 检查消费者是否正在运行
     *
     * @return true表示正在运行
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 实际的启动动作
     */
    protected abstract void doStart();

    /**
     * 实际的停止动作
     */
    protected abstract void doStop();

}
