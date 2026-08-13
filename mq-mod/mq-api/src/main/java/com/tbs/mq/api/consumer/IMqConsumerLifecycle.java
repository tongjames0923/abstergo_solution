package com.tbs.mq.api.consumer;

/**
 * MQ消费者生命周期契约
 * <p>
 * 定义消费者的运行状态管理能力，与具体MQ厂商的容器实现解耦。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IMqConsumerLifecycle {

    /**
     * 启动消费者
     */
    void start();

    /**
     * 停止消费者
     */
    void stop();

    /**
     * 检查消费者是否正在运行
     *
     * @return true表示正在运行
     */
    boolean isRunning();

}
