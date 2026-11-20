package com.tbs.script.api.expressible.condition;

import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.vo.ActionInvocation;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 抽象动作触发器回调。
 *
 * <p>提供动作解析与执行逻辑：实现方只需声明「本次事件要执行哪些动作」与「每个动作的参数值」，
 * 执行顺序、异常隔离与日志由本基类统一处理。</p>
 *
 * <p>子类若需要批量解析（如一次性查询动作绑定配置，避免逐动作查询），
 * 可覆写 {@link #resolveInvocations(String, String, Collection)}，原有两个抽象方法保持兼容。</p>
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractActionConditionTriggerCallback implements IConditionTriggerCallback {

    /**
     * 解析动作。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 动作列表
     */
    protected abstract List<ITriggerAction> resolveActions(String reporterId, String eventCode,
        Collection<ParamValue> paramValues);

    /**
     * 解析动作参数值。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @param actionId    动作ID
     * @return 动作参数值
     */
    protected abstract List<ParamValue> resolveActionParamValues(String reporterId, String eventCode,
        Collection<ParamValue> paramValues, String actionId);

    /**
     * 批量解析本次事件需要执行的动作调用。
     *
     * <p>默认实现委托 {@link #resolveActions} 与 {@link #resolveActionParamValues}，保持既有子类零改动；
     * 需要减少查询次数的实现可整体覆写本方法。</p>
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 动作调用列表，按执行顺序排列；无动作时返回空列表
     */
    protected List<ActionInvocation> resolveInvocations(String reporterId, String eventCode,
        Collection<ParamValue> paramValues) {
        List<ITriggerAction> actions = resolveActions(reporterId, eventCode, paramValues);
        if (actions == null || actions.isEmpty()) {
            return List.of();
        }
        List<ActionInvocation> invocations = new ArrayList<>(actions.size());
        for (ITriggerAction action : actions) {
            if (action == null) {
                continue;
            }
            List<ParamValue> actionParamValues =
                resolveActionParamValues(reporterId, eventCode, paramValues, action.getActionId());
            invocations.add(new ActionInvocation(action, actionParamValues));
        }
        return invocations;
    }

    /**
     * 条件命中后逐个执行动作，单个动作异常被隔离。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     */
    @Override
    public void onTriggered(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        List<ActionInvocation> invocations = resolveInvocations(reporterId, eventCode, paramValues);
        if (invocations == null || invocations.isEmpty()) {
            log.debug("条件触发器命中但未配置动作, reporterId={}, eventCode={}", reporterId, eventCode);
            return;
        }
        for (ActionInvocation invocation : invocations) {
            ITriggerAction action = invocation.getAction();
            if (action == null) {
                continue;
            }
            try {
                action.execute(invocation.getParamValues());
            } catch (Exception e) {
                log.warn("动作执行异常, actionId={}, reporterId={}, eventCode={}, 原因={}", action.getActionId(),
                    reporterId, eventCode, e.getMessage(), e);
            }
        }
    }
}
