package com.tbs.app.service.event.condition;

import com.tbs.app.service.event.ActionParamInterpolator;
import com.tbs.app.service.event.IEventActionService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.condition.AbstractActionConditionTriggerCallback;
import com.tbs.script.api.expressible.impl.TriggerActionQueryService;
import com.tbs.script.api.expressible.vo.ActionInvocation;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 条件命中后的动作执行回调。
 *
 * <p>一次查询取出该事件下启用中的全部动作绑定，再逐个解析动作实例与参数，
 * 避免在动作维度上产生 N+1 查询。单个动作的实例缺失或参数 JSON 非法时跳过该动作并记录告警，
 * 不影响其余动作执行。</p>
 *
 * @author abstergo
 */
@Component
@Slf4j
public class ConditionActionTriggerCallback extends AbstractActionConditionTriggerCallback {

    /**
     * 动作绑定服务
     */
    @Resource
    private IEventActionService eventActionService;

    /**
     * 动作元信息与实例查询服务
     */
    @Resource
    private TriggerActionQueryService triggerActionQueryService;

    /**
     * 批量解析本次事件需要执行的动作调用。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 动作调用列表，按绑定顺序排列
     */
    @Override
    protected List<ActionInvocation> resolveInvocations(String reporterId, String eventCode,
        Collection<ParamValue> paramValues) {
        List<SystemEventActionEntity> bindings = eventActionService.listEnabled(reporterId, eventCode);
        if (CollUtil.isEmpty(bindings)) {
            return List.of();
        }
        List<ActionInvocation> invocations = new ArrayList<>(bindings.size());
        for (SystemEventActionEntity binding : bindings) {
            ITriggerAction action = triggerActionQueryService.getActionInstance(binding.getActionId());
            if (action == null) {
                log.warn("事件动作未注册, 已跳过, reporterId={}, eventCode={}, actionId={}", reporterId, eventCode,
                    binding.getActionId());
                continue;
            }
            List<ParamValue> params = parseParams(binding);
            if (params == null) {
                continue;
            }
            invocations.add(new ActionInvocation(action, ActionParamInterpolator.interpolate(params, paramValues)));
        }
        return invocations;
    }

    /**
     * 解析动作列表，供直接调用基类模板方法的场景使用。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 动作列表
     */
    @Override
    protected List<ITriggerAction> resolveActions(String reporterId, String eventCode,
        Collection<ParamValue> paramValues) {
        return resolveInvocations(reporterId, eventCode, paramValues).stream().map(ActionInvocation::getAction)
            .toList();
    }

    /**
     * 解析指定动作的参数值，供直接调用基类模板方法的场景使用。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @param actionId    动作ID
     * @return 动作参数值；绑定缺失或载荷非法时返回空列表
     */
    @Override
    protected List<ParamValue> resolveActionParamValues(String reporterId, String eventCode,
        Collection<ParamValue> paramValues, String actionId) {
        if (StrUtil.isBlank(actionId)) {
            return List.of();
        }
        return eventActionService.listEnabled(reporterId, eventCode).stream()
            .filter(binding -> actionId.equals(binding.getActionId())).findFirst()
            .map(binding -> {
                List<ParamValue> params = parseParams(binding);
                return params == null ? List.<ParamValue>of()
                    : ActionParamInterpolator.interpolate(params, paramValues);
            }).orElse(List.of());
    }

    /**
     * 解析动作绑定的参数 JSON。
     *
     * @param binding 动作绑定
     * @return 参数值列表；载荷非法时返回 null 表示跳过该动作
     */
    private List<ParamValue> parseParams(SystemEventActionEntity binding) {
        if (StrUtil.isEmpty(binding.getParamJson())) {
            return List.of();
        }
        try {
            List<ParamValue> params = JsonUtils.parseArray(binding.getParamJson(), ParamValue.class);
            return params == null ? List.of() : params;
        } catch (Exception e) {
            log.warn("事件动作参数解析失败, 已跳过, actionId={}, 原因={}", binding.getActionId(), e.getMessage());
            return null;
        }
    }
}
