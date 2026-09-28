package com.tbs.push.impl.endpoint;

import com.tbs.common.delegate.IBackgroundJob;
import com.tbs.push.api.dispatch.IPushDispatcher;

/**
 * 推送消息分发后台任务桥接
 * <p>
 * 调度驱动之一：将分发器注册为{@link IBackgroundJob}， 交由外部调度平台（如xxl-job）集中触发， 适用于集群规模较大、希望由调度中心统一编排的场景。
 * 与{@link EmbeddedPushScheduler}（内嵌线程驱动）互斥， 避免双调度。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushDispatchBackgroundJob implements IBackgroundJob {

    /**
     * 推送消息分发器
     */
    private final IPushDispatcher dispatcher;

    /**
     * 构造后台任务桥接
     *
     * @param dispatcher 推送消息分发器
     */
    public PushDispatchBackgroundJob(IPushDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * 任务名称
     *
     * @return 任务名称
     */
    @Override
    public String name() {
        return "PushDispatchJob";
    }

    /**
     * 任务描述
     *
     * @return 任务描述
     */
    @Override
    public String description() {
        return "推送消息分发";
    }

    /**
     * 执行方法：触发一轮消息分发
     */
    @Override
    public void execute() {
        dispatcher.dispatchDueSafely();
    }

}
