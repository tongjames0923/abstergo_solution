package com.tbs.mq.impl;

import com.tbs.common.delegate.IBackgroundJob;

/**
 * 失败消息重投递后台任务桥接
 * <p>
 * 调度驱动之一：将分发器注册为{@link IBackgroundJob}， 交由外部调度平台（如xxl-job的后台任务执行器）集中触发， 适用于集群规模较大、希望由调度中心统一编排的场景。
 * 与{@link EmbeddedRetryScheduler}（内嵌线程驱动）互斥， 容器中装配了本桥接时内嵌调度器不装配，避免双调度。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class RetryDispatchBackgroundJob implements IBackgroundJob {

    /**
     * 重投递分发器
     */
    private final RetryMessageDispatcher dispatcher;

    /**
     * 构造后台任务桥接
     *
     * @param dispatcher 重投递分发器
     */
    public RetryDispatchBackgroundJob(RetryMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * 任务名称
     *
     * @return 任务名称
     */
    @Override
    public String name() {
        return "MqRetryDispatchJob";
    }

    /**
     * 任务描述
     *
     * @return 任务描述
     */
    @Override
    public String description() {
        return "MQ失败消息到期重投递分发";
    }

    /**
     * 执行方法：触发一轮到期消息分发
     */
    @Override
    public void execute() {
        dispatcher.dispatchDueSafely();
    }

}
