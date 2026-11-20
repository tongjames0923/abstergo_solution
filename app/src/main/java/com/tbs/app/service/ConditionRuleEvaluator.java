package com.tbs.app.service;

import com.tbs.app.service.condition.IConditionRuleService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.impl.IValueGetterService;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.script.api.expressible.vo.result.ResultValue;
import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 条件规则求值器。
 *
 * <p>放在接入层是因为求值需要同时访问规则存储与值获取器注册中心，而 system 层不依赖脚本引擎模块。
 * 求值只信任规则表中的 {@code getterId + invocationJson}，规则不存在、已停用、载荷非法或执行异常
 * 一律返回 false / null，不中断上层脚本或事件链路。</p>
 *
 * @author abstergo
 */
@Slf4j
@Component
public class ConditionRuleEvaluator {

    /**
     * 启用标记
     */
    private static final Integer ENABLED = 1;

    /**
     * 条件规则服务
     */
    @Resource
    private IConditionRuleService conditionRuleService;

    /**
     * 值获取器服务
     */
    @Resource
    private IValueGetterService valueGetterService;

    /**
     * 按编码执行规则并返回布尔结果。
     *
     * @param ruleCode 规则编码
     * @return 规则不可用时返回 false
     */
    public boolean evaluate(String ruleCode) {
        ResultValue result = evaluateResult(ruleCode);
        if (result == null || result.getValue() == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(result.getValue()));
    }

    /**
     * 按编码执行规则并返回原始结果值。
     *
     * @param ruleCode 规则编码
     * @return 结果值；规则不可用或执行异常时返回 null
     */
    public ResultValue evaluateResult(String ruleCode) {
        if (StrUtil.isEmpty(ruleCode)) {
            return null;
        }
        long start = System.currentTimeMillis();
        try {
            SystemConditionRuleEntity rule = conditionRuleService.queryByCode(ruleCode);
            if (rule == null) {
                log.warn("条件规则不存在或已删除, ruleCode={}", ruleCode);
                return null;
            }
            if (!ENABLED.equals(rule.getEnabled())) {
                log.debug("条件规则已停用, ruleCode={}", ruleCode);
                return null;
            }
            List<ParamValue> params = JsonUtils.parseArray(rule.getInvocationJson(), ParamValue.class);
            if (params == null || params.isEmpty()) {
                log.warn("条件规则调用载荷不可解析, ruleCode={}", ruleCode);
                return null;
            }
            ResultValue result = valueGetterService.invokeValueGetter(rule.getGetterId(), params);
            log.debug("条件规则求值完成, ruleCode={}, getterId={}, 结果={}, 耗时={}ms", ruleCode, rule.getGetterId(),
                result == null ? null : result.getValue(), System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.warn("条件规则求值失败, ruleCode={}, 耗时={}ms, 原因={}", ruleCode,
                System.currentTimeMillis() - start, e.getMessage());
            return null;
        }
    }

    /**
     * 校验取值器是否已注册，供保存规则前预检。
     *
     * @param getterId 取值器 ID
     */
    public void assertGetterExists(String getterId) {
        AssertUtil.isTrue(StrUtil.isEmpty(getterId) || valueGetterService.getValueGetter(getterId) == null)
            .i18n("condition.rule.getter.not.exists").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }
}
