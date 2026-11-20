package com.tbs.script.api.expressible.condition;

import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * 抽象动作触发器回调。
 *
 * <p>提供动作解析与执行逻辑。</p>
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

    @Override
    public void onTriggered(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        List<ITriggerAction> actions = resolveActions(reporterId, eventCode, paramValues);
        actions.forEach(action -> {
            try {
                List<ParamValue> actionParamValues =
                    resolveActionParamValues(reporterId, eventCode, paramValues, action.getActionId());
                action.execute(actionParamValues);
            }catch (Exception e){
                log.warn("动作执行异常, actionId={}, 原因={}", action.getActionId(), e.getMessage());
            }
        });

    }
}
