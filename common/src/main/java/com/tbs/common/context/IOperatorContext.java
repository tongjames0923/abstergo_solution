package com.tbs.common.context;

import java.util.Optional;

/**
 * 当前操作人上下文
 * <p>
 * 基础设施模块（MQ、缓存、日志等）需要知道「谁发起了这次动作」，但不能反向依赖业务模块，
 * 故以本接口做依赖倒置，由业务模块提供实现，基础设施按类型可选注入：
 * </p>
 * <ul>
 *     <li>{@link #getCurrentOperatorId()} —— 读取当前操作人，用于消息头等需要携带发起人的场景；</li>
 *     <li>{@link #runAs(Long, Runnable)} —— 声明本次执行的操作人，用于消息消费、定时任务等无登录态场景。</li>
 * </ul>
 * <p>
 * 实现缺失时基础设施跳过操作人相关逻辑，不阻断主流程。
 * </p>
 *
 * @author abstergo
 */
public interface IOperatorContext {

    /**
     * 获取当前操作人ID
     *
     * @return 操作人ID；无法确定操作人时返回空
     */
    Optional<Long> getCurrentOperatorId();

    /**
     * 以指定操作人身份执行任务，执行结束后恢复原上下文
     *
     * @param operatorId 操作人ID，不能为空
     * @param action     待执行任务，不能为空
     */
    void runAs(Long operatorId, Runnable action);
}
