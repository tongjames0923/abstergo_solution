package com.tbs.script.api.expressible.orchestration.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.constant.ScriptI18nConstant;
import com.tbs.script.api.expressible.orchestration.IEventSubscriptionRegistry;
import com.tbs.script.api.expressible.orchestration.vo.EventSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存事件订阅仓库。
 *
 * <p>以 {@code reporterId#eventCode} 为键保存订阅列表，使用 {@link ConcurrentHashMap} 保证并发安全。
 * 订阅按触发器ID去重：同一事件下重复绑定同一触发器时覆盖原绑定，避免重复触发。</p>
 *
 * <p>订阅关系仅存于当前进程内存，重启后丢失；需要跨实例共享时替换为持久化实现。</p>
 */
@Slf4j
@Service
public class InMemoryEventSubscriptionRegistry implements IEventSubscriptionRegistry {

    /**
     * 订阅键分隔符
     */
    private static final String KEY_DELIMITER = "#";

    /**
     * 事件订阅存储，key 为 reporterId#eventCode
     */
    private final Map<String, List<EventSubscription>> store = new ConcurrentHashMap<>();

    /**
     * 绑定订阅，同一触发器已存在时覆盖原绑定。
     *
     * @param subscription 订阅关系
     */
    @Override
    public void bind(EventSubscription subscription) {
        assertSubscription(subscription);
        String key = keyOf(subscription.getReporterId(), subscription.getEventCode());
        store.compute(key, (k, existing) -> {
            List<EventSubscription> next = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            next.removeIf(item -> Objects.equals(item.getTriggerId(), subscription.getTriggerId()));
            next.add(subscription);
            return Collections.unmodifiableList(next);
        });
        log.debug("绑定事件订阅完成, reporterId={}, eventCode={}, triggerId={}, ruleCode={}",
            subscription.getReporterId(), subscription.getEventCode(), subscription.getTriggerId(),
            subscription.getRuleCode());
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
        if (StrUtil.isBlank(triggerId)) {
            return;
        }
        String key = keyOf(reporterId, eventCode);
        store.computeIfPresent(key, (k, existing) -> {
            List<EventSubscription> next = existing.stream()
                .filter(item -> !Objects.equals(item.getTriggerId(), triggerId)).collect(Collectors.toList());
            return next.isEmpty() ? null : Collections.unmodifiableList(next);
        });
        log.debug("解绑事件订阅完成, reporterId={}, eventCode={}, triggerId={}", reporterId, eventCode, triggerId);
    }

    /**
     * 整体替换指定事件下的全部订阅，按触发器ID去重（后者覆盖前者）。
     *
     * @param reporterId    事件上报器ID
     * @param eventCode     事件编码
     * @param subscriptions 新的订阅集合，为空时清空该事件的全部订阅
     */
    @Override
    public void replace(String reporterId, String eventCode, Collection<EventSubscription> subscriptions) {
        AssertUtil.isTrue(StrUtil.isBlank(reporterId)).i18n(ScriptI18nConstant.ORCHESTRATION_REPORTER_ID_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(eventCode)).i18n(ScriptI18nConstant.ORCHESTRATION_EVENT_CODE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String key = keyOf(reporterId, eventCode);
        if (subscriptions == null || subscriptions.isEmpty()) {
            store.remove(key);
            log.debug("清空事件订阅, reporterId={}, eventCode={}", reporterId, eventCode);
            return;
        }
        Map<String, EventSubscription> merged = new LinkedHashMap<>();
        for (EventSubscription subscription : subscriptions) {
            assertSubscription(subscription);
            merged.put(subscription.getTriggerId(), subscription);
        }
        store.put(key, Collections.unmodifiableList(new ArrayList<>(merged.values())));
        log.debug("替换事件订阅完成, reporterId={}, eventCode={}, 订阅数量={}", reporterId, eventCode, merged.size());
    }

    /**
     * 查询指定事件下的全部订阅，按 order 升序（为空排最后）。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅列表，无订阅时返回空列表
     */
    @Override
    public List<EventSubscription> find(String reporterId, String eventCode) {
        List<EventSubscription> subscriptions = store.get(keyOf(reporterId, eventCode));
        if (subscriptions == null || subscriptions.isEmpty()) {
            return List.of();
        }
        return subscriptions.stream().filter(Objects::nonNull)
            .sorted(Comparator.comparing(EventSubscription::getOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 查询指定事件下的单个订阅。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     * @return 订阅关系；未绑定时返回 null
     */
    @Override
    public EventSubscription find(String reporterId, String eventCode, String triggerId) {
        if (StrUtil.isBlank(triggerId)) {
            return null;
        }
        List<EventSubscription> subscriptions = store.get(keyOf(reporterId, eventCode));
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.stream().filter(Objects::nonNull)
            .filter(item -> Objects.equals(item.getTriggerId(), triggerId)).findFirst().orElse(null);
    }

    /**
     * 计算订阅键。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅键
     */
    private String keyOf(String reporterId, String eventCode) {
        return String.valueOf(reporterId) + KEY_DELIMITER + eventCode;
    }

    /**
     * 校验订阅关系必填字段。
     *
     * @param subscription 订阅关系
     */
    private void assertSubscription(EventSubscription subscription) {
        AssertUtil.isTrue(subscription == null).i18n(ScriptI18nConstant.ORCHESTRATION_SUBSCRIPTION_NULL)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getReporterId()))
            .i18n(ScriptI18nConstant.ORCHESTRATION_REPORTER_ID_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getEventCode()))
            .i18n(ScriptI18nConstant.ORCHESTRATION_EVENT_CODE_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getTriggerId()))
            .i18n(ScriptI18nConstant.ORCHESTRATION_SUBSCRIPTION_TRIGGER_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }
}
