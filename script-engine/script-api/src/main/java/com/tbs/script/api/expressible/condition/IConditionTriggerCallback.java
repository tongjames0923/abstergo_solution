package com.tbs.script.api.expressible.condition;

import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.Collection;

/**
 * 条件触发器命中回调。
 *
 * <p>条件规则命中后由通用条件触发器遍历调用，承载具体业务动作。实现方声明为 Spring Bean 即可自动生效，
 * 单个回调异常由触发器隔离，不影响其余回调。</p>
 */
@FunctionalInterface
public interface IConditionTriggerCallback {

    /**
     * 条件命中后执行回调。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     */
    void onTriggered(String reporterId, String eventCode, Collection<ParamValue> paramValues);
}
