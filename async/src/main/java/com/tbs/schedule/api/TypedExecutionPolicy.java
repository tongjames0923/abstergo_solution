package com.tbs.schedule.api;

/**
 * 按任务类型绑定的执行策略标记接口
 * <p>
 * 业务方若需为某任务类型提供专属 {@link ExecutionPolicy}，令其实现本接口并声明 {@link #getTaskType()}，
 * 解析器 {@link ExecutionPolicyResolver} 会自动按类型归集，覆盖全局默认。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TypedExecutionPolicy extends ExecutionPolicy {

    /**
     * 绑定的任务类型
     *
     * @return 任务类型标识
     */
    String getTaskType();
}
