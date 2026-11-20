package com.tbs.script.api.expressible.condition;

import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.ITrigger;
import com.tbs.script.api.expressible.orchestration.IEventSubscriptionRegistry;
import com.tbs.script.api.expressible.orchestration.vo.EventSubscription;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * 条件触发器抽象基类。
 *
 * <p>把「条件是否命中」与「命中后做什么」解耦：条件编码优先取订阅关系上的 {@code ruleCode}
 * （通过 {@link IEventSubscriptionRegistry#find(String, String, String)} 反查），未配置时回退 {@link #defaultConditionCode()}；命中判断委托
 * {@link IConditionEvaluator}，命中后执行 {@link #onTriggered(String, String, Collection)}。</p>
 *
 * <p>{@code isTrigger} 与 {@code trigger} 走同一套判断逻辑，且 {@code isTrigger} 不产生业务副作用，
 * 只在 {@code trigger} 命中后执行回调，避免「查询 + 执行」双调用带来的重复计算。</p>
 *
 * <p>任何依赖缺失、条件编码为空或求值异常均视为未命中，不向外抛出；命中后的业务回调异常由调用方
 * （事件编排层）隔离，不在本层吞掉，避免把业务失败伪装成成功。</p>
 *
 * <p>实现类必须是 Spring Bean（{@code @Component} 或 {@code @Bean}），否则无法注入条件求值器与订阅仓库。</p>
 */
@Slf4j
public abstract class AbstractConditionTrigger implements ITrigger {

    /**
     * 条件求值器
     */
    @Resource
    private IConditionEvaluator conditionEvaluator;

    /**
     * 事件订阅仓库，用于反查本触发器绑定的条件编码
     */
    @Resource
    private IEventSubscriptionRegistry subscriptionRegistry;

    /**
     * 尝试触发：条件命中时执行业务回调并返回 true，未命中返回 false 且无副作用。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 是否真正触发
     */
    @Override
    public final boolean trigger(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        if (!isTrigger(reporterId, eventCode, paramValues)) {
            return false;
        }
        try {
            onTriggered(reporterId, eventCode, paramValues);
            return true;
        } catch (Exception e) {
            log.error("条件触发器回调异常, 视为未触发, triggerId={}, reporterId={}, eventCode={}, 原因={}",
                getTriggerId(), reporterId, eventCode, e.getMessage());
            return false;
        }
    }

    /**
     * 仅查询条件是否命中，不执行业务回调。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 是否命中
     */
    @Override
    public boolean isTrigger(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        if (conditionEvaluator == null || subscriptionRegistry == null) {
            log.debug("条件触发器依赖缺失, 视为未命中, triggerId={}", getTriggerId());
            return false;
        }
        try {
            String conditionCode = resolveConditionCode(reporterId, eventCode);
            if (StrUtil.isBlank(conditionCode)) {
                log.debug("条件触发器未绑定条件编码, 视为未命中, triggerId={}, reporterId={}, eventCode={}",
                    getTriggerId(), reporterId, eventCode);
                return false;
            }
            boolean matched = conditionEvaluator.evaluate(conditionCode);
            log.debug("条件触发器求值完成, triggerId={}, reporterId={}, eventCode={}, conditionCode={}, 命中={}",
                getTriggerId(), reporterId, eventCode, conditionCode, matched);
            return matched;
        } catch (Exception e) {
            log.warn("条件触发器求值异常, 视为未命中, triggerId={}, reporterId={}, eventCode={}, 原因={}",
                getTriggerId(), reporterId, eventCode, e.getMessage());
            return false;
        }
    }

    /**
     * 声明参数定义，条件触发器不额外约束参数。
     *
     * @return 空参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of();
    }

    /**
     * 解析本次触发使用的条件编码：订阅关系上的 ruleCode 优先，未配置时回退默认条件编码。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 条件编码；均未配置时返回 null
     */
    protected String resolveConditionCode(String reporterId, String eventCode) {
        EventSubscription subscription = subscriptionRegistry.find(reporterId, eventCode, getTriggerId());
        if (subscription != null && StrUtil.isNotBlank(subscription.getRuleCode())) {
            return subscription.getRuleCode();
        }
        return defaultConditionCode();
    }

    /**
     * 默认条件编码，订阅关系未配置 ruleCode 时使用。
     *
     * @return 默认条件编码，默认为空
     */
    protected String defaultConditionCode() {
        return null;
    }

    /**
     * 条件命中后的业务动作。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     */
    protected abstract void onTriggered(String reporterId, String eventCode, Collection<ParamValue> paramValues);
}
