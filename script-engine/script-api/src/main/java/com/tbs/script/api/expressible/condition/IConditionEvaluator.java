package com.tbs.script.api.expressible.condition;

/**
 * 条件求值器。
 *
 * <p>供条件触发器判断「绑定的条件是否命中」。实现方负责把平台侧的条件规则能力接入触发器，
 * 并保证任何不可用场景（条件不存在、已停用、载荷非法、执行异常）返回 {@code false} 而不是抛出异常，
 * 避免单条条件问题中断事件链路。</p>
 */
public interface IConditionEvaluator {

    /**
     * 判断条件是否命中。
     *
     * @param conditionCode 条件编码
     * @return true 命中；条件不存在、不可用或执行异常时返回 false
     */
    boolean evaluate(String conditionCode);
}
