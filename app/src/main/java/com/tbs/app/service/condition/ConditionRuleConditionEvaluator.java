package com.tbs.app.service.condition;

import com.tbs.app.service.ConditionRuleEvaluator;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.condition.IConditionEvaluator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 条件规则求值适配器。
 *
 * <p>把平台已有的条件规则能力接入脚本模块的条件触发器 SPI：条件编码即条件规则编码，
 * 求值委托 {@link ConditionRuleEvaluator}。规则不存在、已停用、载荷非法或执行异常统一返回 false，
 * 不中断事件链路。</p>
 */
@Slf4j
@Component
public class ConditionRuleConditionEvaluator implements IConditionEvaluator {

    /**
     * 条件规则求值器
     */
    @Resource
    private ConditionRuleEvaluator conditionRuleEvaluator;

    /**
     * 判断条件规则是否命中。
     *
     * @param conditionCode 条件规则编码
     * @return true 命中；规则不可用或执行异常时返回 false
     */
    @Override
    public boolean evaluate(String conditionCode) {
        if (StrUtil.isBlank(conditionCode)) {
            return false;
        }
        try {
            return conditionRuleEvaluator.evaluate(conditionCode);
        } catch (Exception e) {
            log.warn("条件规则求值适配失败, ruleCode={}, 原因={}", conditionCode, e.getMessage());
            return false;
        }
    }
}
