package com.tbs.schedule.api;

import java.util.Collection;
import java.util.Optional;

/**
 * 任务持久化存储（SPI，可选能力）
 * <p>
 * 为需要可靠性的场景（如外部 API 提交的延迟任务不能因进程重启丢失）提供持久化扩展点。 默认实现 {@link NoopTaskStore} 为纯内存，不落盘，保持框架轻量；启用持久化只需注册 DB/Redis 实现。
 * 存储仅负责任务的登记、查询与完成清理，不介入调度与执行（与 mq-mod 的 {@code IFailedMessageStore} 职责边界一致）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskStore {

    /**
     * 登记任务
     *
     * @param submission 任务
     */
    void save(TaskSubmission submission);

    /**
     * 按标识查询任务
     *
     * @param taskId 任务标识
     * @return 任务（不存在返回空）
     */
    Optional<TaskSubmission> find(String taskId);

    /**
     * 标记任务已终结并移除
     *
     * @param taskId 任务标识
     */
    void remove(String taskId);

    /**
     * 取出所有未终结任务（用于启动时恢复）
     *
     * @return 未终结任务集合
     */
    Collection<TaskSubmission> loadUnfinished();
}
