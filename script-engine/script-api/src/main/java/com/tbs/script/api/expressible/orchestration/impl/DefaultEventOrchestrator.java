package com.tbs.script.api.expressible.orchestration.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.IEventReporter;
import com.tbs.script.api.expressible.ITrigger;
import com.tbs.script.api.expressible.constant.ScriptI18nConstant;
import com.tbs.script.api.expressible.orchestration.IEventOrchestrator;
import com.tbs.script.api.expressible.orchestration.IEventSubscriptionRegistry;
import com.tbs.script.api.expressible.orchestration.vo.*;
import com.tbs.script.api.expressible.vo.EventMeta;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认事件编排实现。
 *
 * <p>上报流程：校验信封与上报器/事件声明 → 校验参数数量 → 调用上报器上报 → 按订阅关系分发；
 * 上报异常时直接返回失败结果且不再分发。分发流程：一次性加载触发器实例，按订阅顺序逐个调用
 * {@link ITrigger#trigger(String, String, Collection)}，单个触发器异常被隔离并继续后续触发器。</p>
 *
 * <p>每次调用实时从注册中心取实例，不做缓存，保证运行时注册/注销的触发器立即生效。</p>
 */
@DubboService
@Slf4j
@Service
public class DefaultEventOrchestrator implements IEventOrchestrator {

    /**
     * 日志参数摘要最大长度
     */
    private static final int SUMMARY_MAX_LENGTH = 100;

    /**
     * 注册中心，用于定位上报器与触发器实例
     */
    @Resource
    private IRegistryCenter registryCenter;

    /**
     * 事件订阅仓库
     */
    @Resource
    private IEventSubscriptionRegistry subscriptionRegistry;

    /**
     * 上报事件并按订阅关系分发。
     *
     * @param envelope 事件上报信封
     * @return 上报与分发汇总结果
     */
    @Override
    public EventReportResult report(EventEnvelope envelope) {
        assertEnvelope(envelope);
        long start = System.currentTimeMillis();
        IEventReporter reporter = findReporter(envelope.getReporterId());
        AssertUtil.isTrue(reporter == null)
            .i18n(ScriptI18nConstant.ORCHESTRATION_REPORTER_NOT_FOUND, envelope.getReporterId())
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();

        EventMeta eventMeta = reporter.getEventMeta(envelope.getEventCode());
        AssertUtil.isTrue(eventMeta == null)
            .i18n(ScriptI18nConstant.ORCHESTRATION_EVENT_NOT_DECLARED, envelope.getReporterId(),
                envelope.getEventCode())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        assertParamCount(eventMeta, envelope);

        try {
            reporter.report(envelope.getEventCode(), envelope.getParamValues());
        } catch (Exception e) {
            log.warn("事件上报失败, reporterId={}, eventCode={}, 参数摘要={}, 耗时={}ms, 原因={}",
                envelope.getReporterId(), envelope.getEventCode(), summarizeParams(envelope.getParamValues()),
                System.currentTimeMillis() - start, e.getMessage(), e);
            return EventReportResult.builder().reported(false).errorMsg(e.getMessage()).success(false)
                .elapsedMs(System.currentTimeMillis() - start).build();
        }

        EventDispatchResult dispatch = doDispatch(envelope, resolveSubscribedTriggerIds(envelope));
        long elapsed = System.currentTimeMillis() - start;
        log.debug("事件上报编排完成, reporterId={}, eventCode={}, 触发器数量={}, 命中数量={}, 耗时={}ms",
            envelope.getReporterId(), envelope.getEventCode(), dispatch.getTriggerResults().size(),
            dispatch.getTriggeredCount(), elapsed);
        return EventReportResult.builder().reported(true).dispatch(dispatch).success(dispatch.isSuccess())
            .elapsedMs(elapsed).build();
    }

    /**
     * 按订阅关系分发事件，不调用上报器。
     *
     * @param envelope 事件信封
     * @return 分发汇总结果
     */
    @Override
    public EventDispatchResult dispatch(EventEnvelope envelope) {
        assertEnvelope(envelope);
        return doDispatch(envelope, resolveSubscribedTriggerIds(envelope));
    }

    /**
     * 按指定触发器列表分发事件。
     *
     * <p>条件触发器仍需从订阅关系上取条件编码，未绑定订阅时不会触发。</p>
     *
     * @param envelope   事件信封
     * @param triggerIds 触发器ID集合
     * @return 分发汇总结果
     */
    @Override
    public EventDispatchResult dispatch(EventEnvelope envelope, Collection<String> triggerIds) {
        assertEnvelope(envelope);
        return doDispatch(envelope, distinctTriggerIds(triggerIds));
    }

    /**
     * 绑定一个事件订阅。
     *
     * @param subscription 订阅关系
     */
    @Override
    public void bind(EventSubscription subscription) {
        subscriptionRegistry.bind(subscription);
    }

    /**
     * 解绑指定事件下的一个触发器。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     */
    @Override
    public void unbind(String reporterId, String eventCode, String triggerId) {
        subscriptionRegistry.unbind(reporterId, eventCode, triggerId);
    }

    /**
     * 整体替换指定事件下的全部订阅。
     *
     * @param reporterId    事件上报器ID
     * @param eventCode     事件编码
     * @param subscriptions 新的订阅集合
     */
    @Override
    public void replace(String reporterId, String eventCode, Collection<EventSubscription> subscriptions) {
        subscriptionRegistry.replace(reporterId, eventCode, subscriptions);
    }

    /**
     * 查询指定事件下的订阅列表。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 按 order 升序的订阅列表
     */
    @Override
    public List<EventSubscription> listSubscriptions(String reporterId, String eventCode) {
        return subscriptionRegistry.find(reporterId, eventCode);
    }

    /**
     * 执行分发：按触发器ID顺序调用触发器并汇总结果。
     *
     * @param envelope   事件信封
     * @param triggerIds 触发器ID集合
     * @return 分发汇总结果
     */
    private EventDispatchResult doDispatch(EventEnvelope envelope, Collection<String> triggerIds) {
        long start = System.currentTimeMillis();
        if (CollUtil.isEmpty(triggerIds)) {
            log.debug("事件分发明细为空, reporterId={}, eventCode={}", envelope.getReporterId(), envelope.getEventCode());
            return EventDispatchResult.builder().triggerResults(List.of()).triggeredCount(0).success(true).elapsedMs(0)
                .build();
        }
        Map<String, ITrigger> triggers = loadTriggers();
        List<TriggerResult> results = new ArrayList<>(triggerIds.size());
        int triggeredCount = 0;
        boolean success = true;
        for (String triggerId : triggerIds) {
            long triggerStart = System.currentTimeMillis();
            ITrigger trigger = triggers.get(triggerId);
            if (trigger == null) {
                success = false;
                String errorMsg = I18nUtil.get(ScriptI18nConstant.ORCHESTRATION_TRIGGER_NOT_FOUND, triggerId);
                log.warn("事件分发未找到触发器, reporterId={}, eventCode={}, triggerId={}", envelope.getReporterId(),
                    envelope.getEventCode(), triggerId);
                results.add(TriggerResult.builder().triggerId(triggerId).triggered(false).success(false)
                    .errorMsg(errorMsg).elapsedMs(System.currentTimeMillis() - triggerStart).build());
                continue;
            }
            try {
                boolean triggered = trigger.trigger(envelope.getReporterId(), envelope.getEventCode(),
                    envelope.getParamValues());
                if (triggered) {
                    triggeredCount++;
                }
                results.add(TriggerResult.builder().triggerId(triggerId).triggerName(safeTriggerName(trigger))
                    .triggered(triggered).success(true).elapsedMs(System.currentTimeMillis() - triggerStart).build());
            } catch (Exception e) {
                success = false;
                log.warn("触发器执行失败, reporterId={}, eventCode={}, triggerId={}, 耗时={}ms, 原因={}",
                    envelope.getReporterId(), envelope.getEventCode(), triggerId,
                    System.currentTimeMillis() - triggerStart, e.getMessage(), e);
                results.add(TriggerResult.builder().triggerId(triggerId).triggerName(safeTriggerName(trigger))
                    .triggered(false).success(false).errorMsg(e.getMessage())
                    .elapsedMs(System.currentTimeMillis() - triggerStart).build());
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        log.debug("事件分发完成, reporterId={}, eventCode={}, 触发器数量={}, 命中数量={}, 耗时={}ms",
            envelope.getReporterId(), envelope.getEventCode(), results.size(), triggeredCount, elapsed);
        return EventDispatchResult.builder().triggerResults(results).triggeredCount(triggeredCount).success(success)
            .elapsedMs(elapsed).build();
    }

    /**
     * 校验信封必填字段。
     *
     * @param envelope 事件信封
     */
    private void assertEnvelope(EventEnvelope envelope) {
        AssertUtil.isTrue(envelope == null).i18n(ScriptI18nConstant.ORCHESTRATION_ENVELOPE_NULL)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(envelope.getReporterId()))
            .i18n(ScriptI18nConstant.ORCHESTRATION_REPORTER_ID_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(envelope.getEventCode()))
            .i18n(ScriptI18nConstant.ORCHESTRATION_EVENT_CODE_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
    }

    /**
     * 校验参数数量不超过事件声明数量；事件未声明参数时忽略传入参数。
     *
     * @param eventMeta 事件元信息
     * @param envelope  事件信封
     */
    private void assertParamCount(EventMeta eventMeta, EventEnvelope envelope) {
        List<ParamType> declared = eventMeta.getParamTypes();
        if (CollUtil.isEmpty(declared)) {
            return;
        }
        int actual = envelope.getParamValues() == null ? 0
            : (int)envelope.getParamValues().stream().filter(Objects::nonNull).count();
        AssertUtil.isTrue(actual > declared.size())
            .i18n(ScriptI18nConstant.ORCHESTRATION_PARAM_OVERFLOW, envelope.getEventCode(), declared.size(), actual)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 解析事件订阅的触发器ID列表，按订阅顺序去重。
     *
     * @param envelope 事件信封
     * @return 有序触发器ID列表
     */
    private Collection<String> resolveSubscribedTriggerIds(EventEnvelope envelope) {
        List<EventSubscription> subscriptions = subscriptionRegistry.find(envelope.getReporterId(),
            envelope.getEventCode());
        if (CollUtil.isEmpty(subscriptions)) {
            return List.of();
        }
        return subscriptions.stream().filter(Objects::nonNull).map(EventSubscription::getTriggerId)
            .filter(StrUtil::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 过滤空值并去重触发器ID。
     *
     * @param triggerIds 触发器ID集合
     * @return 有序触发器ID集合
     */
    private static Collection<String> distinctTriggerIds(Collection<String> triggerIds) {
        if (CollUtil.isEmpty(triggerIds)) {
            return List.of();
        }
        return triggerIds.stream().filter(StrUtil::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 定位事件上报器。
     *
     * @param reporterId 事件上报器ID
     * @return 上报器实例；未找到时返回 null
     */
    private IEventReporter findReporter(String reporterId) {
        return registryCenter.getAllContributors().stream()
            .flatMap(contributor -> contributor
                .getAllInstanceByTypeAndBusinessAndVersion(IEventReporter.class, null, null).stream())
            .map(instance -> (IEventReporter)instance.getInstance()).filter(Objects::nonNull)
            .filter(reporter -> reporterId.equals(reporter.getEventReporterId())).findFirst().orElse(null);
    }

    /**
     * 加载全部触发器实例。
     *
     * @return 触发器ID到实例的映射
     */
    private Map<String, ITrigger> loadTriggers() {
        Map<String, ITrigger> triggers = new LinkedHashMap<>();
        registryCenter.getAllContributors().stream()
            .flatMap(contributor -> contributor
                .getAllInstanceByTypeAndBusinessAndVersion(ITrigger.class, null, null).stream())
            .map(instance -> (ITrigger)instance.getInstance()).filter(Objects::nonNull)
            .filter(trigger -> StrUtil.isNotBlank(trigger.getTriggerId()))
            .forEach(trigger -> triggers.putIfAbsent(trigger.getTriggerId(), trigger));
        return triggers;
    }

    /**
     * 安全读取触发器名称，读取失败不影响分发结果。
     *
     * @param trigger 触发器实例
     * @return 触发器名称；读取失败返回 null
     */
    private static String safeTriggerName(ITrigger trigger) {
        try {
            return trigger.getTriggerName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成参数摘要，避免完整大对象进入日志。
     *
     * @param paramValues 事件参数值
     * @return 参数摘要
     */
    private static String summarizeParams(Collection<ParamValue> paramValues) {
        if (CollUtil.isEmpty(paramValues)) {
            return "[]";
        }
        return paramValues.stream().filter(Objects::nonNull)
            .map(param -> summarize(param.getName()) + "=" + summarize(param.getValue()))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 截断单个日志片段。
     *
     * @param value 日志片段
     * @return 截断后的片段
     */
    private static String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        String text = String.valueOf(value);
        return text.length() > SUMMARY_MAX_LENGTH ? text.substring(0, SUMMARY_MAX_LENGTH) + "..." : text;
    }
}
