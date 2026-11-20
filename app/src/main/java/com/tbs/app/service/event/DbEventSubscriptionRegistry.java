package com.tbs.app.service.event;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.orchestration.IEventSubscriptionRegistry;
import com.tbs.script.api.expressible.orchestration.vo.EventSubscription;
import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 持久化事件订阅仓库。
 *
 * <p>覆盖脚本模块默认的内存实现（标注 {@link Primary}），把订阅关系落到 {@code sys_event_subscription}，
 * 服务重启与多实例部署下配置不丢失。每次读取都实时查库，保证管理端修改后立即生效。</p>
 *
 * <p>写操作统一走 {@link IEventSubscriptionService#replaceByEvent} 的增量合并语义，
 * 避免逐条增删带来的中间态。</p>
 *
 * @author abstergo
 */
@Primary
@Service
@Slf4j
public class DbEventSubscriptionRegistry implements IEventSubscriptionRegistry {

    /**
     * 订阅服务
     */
    @Resource
    private IEventSubscriptionService eventSubscriptionService;

    /**
     * 绑定一个事件订阅，同一触发器已存在时覆盖原绑定。
     *
     * @param subscription 订阅关系
     */
    @Override
    public void bind(EventSubscription subscription) {
        assertSubscription(subscription);
        Map<String, EventSubscription> merged = new LinkedHashMap<>();
        listAll(subscription.getReporterId(), subscription.getEventCode())
            .forEach(item -> merged.put(item.getTriggerId(), item));
        merged.put(subscription.getTriggerId(), subscription);
        replace(subscription.getReporterId(), subscription.getEventCode(), merged.values());
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
        List<EventSubscription> remaining =
            listAll(reporterId, eventCode).stream().filter(item -> !Objects.equals(item.getTriggerId(), triggerId))
                .collect(Collectors.toList());
        replace(reporterId, eventCode, remaining);
    }

    /**
     * 整体替换指定事件下的全部订阅。
     *
     * @param reporterId    事件上报器ID
     * @param eventCode     事件编码
     * @param subscriptions 新的订阅集合，为空时清空该事件的全部订阅
     */
    @Override
    public void replace(String reporterId, String eventCode, Collection<EventSubscription> subscriptions) {
        AssertUtil.isTrue(StrUtil.isBlank(reporterId)).i18n("event.subscription.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(eventCode)).i18n("event.subscription.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        List<SystemEventSubscriptionEntity> entities = new ArrayList<>();
        if (CollUtil.isNotEmpty(subscriptions)) {
            for (EventSubscription subscription : subscriptions) {
                assertSubscription(subscription);
                entities.add(toEntity(subscription));
            }
        }
        eventSubscriptionService.replaceByEvent(reporterId, eventCode, entities);
        log.debug("持久化事件订阅替换完成, reporterId={}, eventCode={}, 订阅数量={}", reporterId, eventCode,
            entities.size());
    }

    /**
     * 查询指定事件下的全部启用订阅，按 execution 顺序升序。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅列表，无订阅时返回空列表
     */
    @Override
    public List<EventSubscription> find(String reporterId, String eventCode) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode)) {
            return List.of();
        }
        return eventSubscriptionService.listEnabled(reporterId, eventCode).stream().filter(Objects::nonNull)
            .map(this::toSubscription)
            .sorted(Comparator.comparing(EventSubscription::getOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 查询指定事件下的单个启用订阅。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     * @return 订阅关系；未绑定或已停用时返回 null
     */
    @Override
    public EventSubscription find(String reporterId, String eventCode, String triggerId) {
        if (StrUtil.isBlank(triggerId)) {
            return null;
        }
        SystemEventSubscriptionEntity entity =
            eventSubscriptionService.findEnabled(reporterId, eventCode, triggerId);
        return entity == null ? null : toSubscription(entity);
    }

    /**
     * 查询指定事件下的全部订阅（含停用），用于写操作前的合并。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅列表，无订阅时返回空列表
     */
    private List<EventSubscription> listAll(String reporterId, String eventCode) {
        return eventSubscriptionService.listByEvent(reporterId, eventCode).stream().filter(Objects::nonNull)
            .map(this::toSubscription)
            .sorted(Comparator.comparing(EventSubscription::getOrder, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 校验订阅关系必填字段。
     *
     * @param subscription 订阅关系
     */
    private void assertSubscription(EventSubscription subscription) {
        AssertUtil.isTrue(subscription == null).i18n("event.subscription.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getReporterId())).i18n("event.subscription.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getEventCode())).i18n("event.subscription.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(subscription.getTriggerId())).i18n("event.subscription.trigger.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 订阅关系转为持久化实体。
     *
     * @param subscription 订阅关系
     * @return 持久化实体
     */
    private SystemEventSubscriptionEntity toEntity(EventSubscription subscription) {
        SystemEventSubscriptionEntity entity = new SystemEventSubscriptionEntity();
        entity.setReporterId(subscription.getReporterId());
        entity.setEventCode(subscription.getEventCode());
        entity.setTriggerId(subscription.getTriggerId());
        entity.setRuleCode(subscription.getRuleCode());
        entity.setSortOrder(subscription.getOrder());
        return entity;
    }

    /**
     * 持久化实体转为订阅关系。
     *
     * @param entity 持久化实体
     * @return 订阅关系
     */
    private EventSubscription toSubscription(SystemEventSubscriptionEntity entity) {
        return EventSubscription.builder().reporterId(entity.getReporterId()).eventCode(entity.getEventCode())
            .triggerId(entity.getTriggerId()).ruleCode(entity.getRuleCode()).order(entity.getSortOrder()).build();
    }
}
