package com.tbs.script.api.expressible.condition.impl;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.script.api.expressible.condition.AbstractConditionTrigger;
import com.tbs.script.api.expressible.condition.IConditionTriggerCallback;
import com.tbs.script.api.expressible.constant.ScriptI18nConstant;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 通用条件规则触发器。
 *
 * <p>触发器本体保持通用：条件编码来自事件订阅关系上的 {@code ruleCode}，同一实例可被多个事件复用。
 * 命中后遍历容器内的 {@link IConditionTriggerCallback} 执行具体业务动作；无回调时仅记录日志，
 * 表示条件命中但无附加动作。</p>
 */
@Slf4j
@Component
public class ConditionRuleTrigger extends AbstractConditionTrigger {

    /**
     * 触发器ID
     */
    public static final String TRIGGER_ID = "ConditionRuleTrigger";

    /**
     * 命中回调提供者，允许业务方声明零到多个回调
     */
    private final ObjectProvider<IConditionTriggerCallback> callbackProvider;

    /**
     * 构造通用条件规则触发器。
     *
     * @param callbackProvider 命中回调提供者
     */
    public ConditionRuleTrigger(ObjectProvider<IConditionTriggerCallback> callbackProvider) {
        this.callbackProvider = callbackProvider;
    }

    /**
     * 获取触发器ID。
     *
     * @return 触发器ID
     */
    @Override
    public String getTriggerId() {
        return TRIGGER_ID;
    }

    /**
     * 获取触发器名称。
     *
     * @return 国际化后的触发器名称
     */
    @Override
    public String getTriggerName() {
        return text(ScriptI18nConstant.TRIGGER_CONDITION_NAME);
    }

    /**
     * 获取触发器描述。
     *
     * @return 国际化后的触发器描述
     */
    @Override
    public String getDescription() {
        return text(ScriptI18nConstant.TRIGGER_CONDITION_DESCRIPTION);
    }

    /**
     * 条件命中后遍历执行回调，单个回调异常被隔离。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     */
    @Override
    protected void onTriggered(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        boolean invoked = false;
        for (IConditionTriggerCallback callback : callbackProvider.orderedStream().toList()) {
            invoked = true;
            try {
                callback.onTriggered(reporterId, eventCode, paramValues);
            } catch (Exception e) {
                log.warn("条件触发器命中回调失败, triggerId={}, reporterId={}, eventCode={}, 回调={}, 原因={}",
                    TRIGGER_ID, reporterId, eventCode, callback.getClass().getName(), e.getMessage(), e);
            }
        }
        if (!invoked) {
            log.debug("条件触发器命中但未声明回调, triggerId={}, reporterId={}, eventCode={}", TRIGGER_ID, reporterId,
                eventCode);
        }
    }

    /**
     * 解析国际化文案。
     *
     * @param key 资源 key
     * @return 文案内容；无可用资源时回退为 key 本身
     */
    private String text(String key) {
        String value = I18nUtil.get(key);
        return value == null || value.isBlank() ? key : value;
    }
}
