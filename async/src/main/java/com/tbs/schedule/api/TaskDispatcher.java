package com.tbs.schedule.api;

import com.tbs.schedule.dispatcher.LocalTaskDispatcher;

/**
 * 任务派发器（SPI）
 * <p>
 * 决定到期任务"在哪里、以何种方式执行"。调度内核在任务到期后调用本接口，将执行职责彻底交出去， 内核本身不执行任何业务逻辑。
 * </p>
 * <ul>
 *     <li>默认实现 {@link LocalTaskDispatcher}：提交到本地线程池执行；</li>
 *     <li>未来扩展：{@code BroadcastTaskDispatcher} 将任务广播出去，由收到的执行器抢到即执行
 *     （呼应"广播理念"，内核零改动，仅新增一个实现 Bean）。</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskDispatcher {

    /**
     * 派发一个到期任务
     *
     * @param submission 到期任务提交描述
     */
    void dispatch(TaskSubmission submission);
}
