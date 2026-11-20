package com.tbs.app.service.impl.event;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.event.IEventSubscriptionService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.event.EventSubscriptionMapper;
import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 事件订阅服务实现。
 *
 * <p>保存时校验事件定位字段、触发器必填、同一事件下触发器不重复、顺序与启停合法；
 * 运行期查询只返回启用且未删除的绑定，并按顺序升序返回，保证分发顺序稳定。</p>
 *
 * @author abstergo
 */
@Slf4j
public class EventSubscriptionServiceImpl extends
    AbstractLogicDeleteFullFeatureServiceTemplate<SystemEventSubscriptionEntity, Long, EventSubscriptionMapper>
    implements IEventSubscriptionService {

    /**
     * 启用标记
     */
    private static final Integer ENABLED = 1;

    /**
     * 停用标记
     */
    private static final Integer DISABLED = 0;

    /**
     * 上报器ID / 事件编码 / 触发器ID 的最大长度
     */
    private static final int MAX_CODE_LENGTH = 128;

    /**
     * 条件规则编码的最大长度
     */
    private static final int MAX_RULE_CODE_LENGTH = 64;

    /**
     * 审计服务
     */
    private static final IAuditService<SystemEventSubscriptionEntity, Long, Long> AUDIT_SERVICE =
        new DefaultAuditService<>();

    /**
     * 订阅 Mapper
     */
    @Resource
    private EventSubscriptionMapper eventSubscriptionMapper;

    /**
     * 系统设置，提供 PageVO 构建委派
     */
    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected EventSubscriptionMapper getMapper() {
        return eventSubscriptionMapper;
    }

    @Override
    protected SystemEventSubscriptionEntity newEntity() {
        return new SystemEventSubscriptionEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected Optional<IAuditService<SystemEventSubscriptionEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(AUDIT_SERVICE);
    }

    @Override
    protected SystemEventSubscriptionEntity insertNew(SystemEventSubscriptionEntity entity) {
        validateForSave(entity);
        normalize(entity);
        entity.setEnabled(entity.getEnabled() == null ? ENABLED : entity.getEnabled());
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        log.info("事件订阅创建完成, reporterId={}, eventCode={}, triggerId={}, ruleCode={}", entity.getReporterId(),
            entity.getEventCode(), entity.getTriggerId(), entity.getRuleCode());
        return entity;
    }

    @Override
    protected SystemEventSubscriptionEntity compareAndUpdateValue(SystemEventSubscriptionEntity newOne,
        SystemEventSubscriptionEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getRuleCode())) {
            validateRuleCode(newOne.getRuleCode());
            oldOne.setRuleCode(newOne.getRuleCode().trim());
        }
        if (newOne.getSortOrder() != null) {
            validateSortOrder(newOne.getSortOrder());
            oldOne.setSortOrder(newOne.getSortOrder());
        }
        if (newOne.getEnabled() != null) {
            validateEnabled(newOne.getEnabled());
            oldOne.setEnabled(newOne.getEnabled());
        }
        log.info("事件订阅更新完成, reporterId={}, eventCode={}, triggerId={}, enabled={}", oldOne.getReporterId(),
            oldOne.getEventCode(), oldOne.getTriggerId(), oldOne.getEnabled());
        return oldOne;
    }

    @Override
    public List<SystemEventSubscriptionEntity> listByEvent(String reporterId, String eventCode) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode)) {
            return List.of();
        }
        return queryByEvent(reporterId.trim(), eventCode.trim(), false);
    }

    @Override
    public List<SystemEventSubscriptionEntity> listEnabled(String reporterId, String eventCode) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode)) {
            return List.of();
        }
        return queryByEvent(reporterId.trim(), eventCode.trim(), true);
    }

    @Override
    public SystemEventSubscriptionEntity findEnabled(String reporterId, String eventCode, String triggerId) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode) || StrUtil.isBlank(triggerId)) {
            return null;
        }
        IWhereClause<SystemEventSubscriptionEntity> where =
            QueryEngine.getEntityQueryWrapper(SystemEventSubscriptionEntity.class).query().where()
                .equal(SystemEventSubscriptionEntity::getReporterId, reporterId.trim())
                .equal(SystemEventSubscriptionEntity::getEventCode, eventCode.trim())
                .equal(SystemEventSubscriptionEntity::getTriggerId, triggerId.trim())
                .equal(SystemEventSubscriptionEntity::getEnabled, ENABLED)
                .equal(SystemEventSubscriptionEntity::getFlag, CommonFlagEnum.ACTIVE.getCode());
        List<SystemEventSubscriptionEntity> result = where.pour().fetch();
        return CollUtil.isEmpty(result) ? null : result.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<SystemEventSubscriptionEntity> replaceByEvent(String reporterId, String eventCode,
        Collection<SystemEventSubscriptionEntity> subscriptions) {
        AssertUtil.isTrue(StrUtil.isBlank(reporterId)).i18n("event.subscription.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(eventCode)).i18n("event.subscription.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String reporter = reporterId.trim();
        String event = eventCode.trim();
        validateCodeLength(reporter, "event.subscription.reporter.invalid");
        validateCodeLength(event, "event.subscription.event.invalid");

        Map<String, SystemEventSubscriptionEntity> incoming = new LinkedHashMap<>();
        if (subscriptions != null) {
            for (SystemEventSubscriptionEntity subscription : subscriptions) {
                AssertUtil.isTrue(subscription == null).i18n("event.subscription.required")
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
                subscription.setReporterId(reporter);
                subscription.setEventCode(event);
                validateForSave(subscription);
                normalize(subscription);
                AssertUtil.isTrue(incoming.containsKey(subscription.getTriggerId()))
                    .i18n("event.subscription.trigger.duplicate", subscription.getTriggerId())
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
                incoming.put(subscription.getTriggerId(), subscription);
            }
        }

        Map<String, SystemEventSubscriptionEntity> existing = new LinkedHashMap<>();
        queryByEvent(reporter, event, false)
            .forEach(subscription -> existing.put(subscription.getTriggerId(), subscription));

        for (SystemEventSubscriptionEntity subscription : incoming.values()) {
            SystemEventSubscriptionEntity current = existing.remove(subscription.getTriggerId());
            if (current == null) {
                getInstance().save(subscription);
                continue;
            }
            current.setRuleCode(subscription.getRuleCode());
            current.setSortOrder(subscription.getSortOrder());
            if (subscription.getEnabled() != null) {
                current.setEnabled(subscription.getEnabled());
            }
            getInstance().save(current);
        }
        for (SystemEventSubscriptionEntity stale : existing.values()) {
            getInstance().deleteById(stale.getId());
        }
        log.info("事件订阅整体替换完成, reporterId={}, eventCode={}, 订阅数量={}", reporter, event, incoming.size());
        return queryByEvent(reporter, event, false);
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        AssertUtil.isTrue(id == null).i18n("event.subscription.id.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemEventSubscriptionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n("event.subscription.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setEnabled(enabled ? ENABLED : DISABLED);
        getMapper().updateByPrimaryKey(entity);
        log.info("事件订阅状态变更, id={}, triggerId={}, enabled={}", id, entity.getTriggerId(), enabled);
    }

    /**
     * 按事件查询订阅，可选只返回启用中的记录。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param onlyEnabled 是否只返回启用中的记录
     * @return 按顺序升序的订阅列表
     */
    private List<SystemEventSubscriptionEntity> queryByEvent(String reporterId, String eventCode,
        boolean onlyEnabled) {
        IWhereClause<SystemEventSubscriptionEntity> where =
            QueryEngine.getEntityQueryWrapper(SystemEventSubscriptionEntity.class).query().where()
                .equal(SystemEventSubscriptionEntity::getReporterId, reporterId)
                .equal(SystemEventSubscriptionEntity::getEventCode, eventCode)
                .equal(SystemEventSubscriptionEntity::getFlag, CommonFlagEnum.ACTIVE.getCode());
        if (onlyEnabled) {
            where.equal(SystemEventSubscriptionEntity::getEnabled, ENABLED);
        }
        var query = where.pour();
        query.getOrderSupport().ifPresent(order -> order.orderByAsc(SystemEventSubscriptionEntity::getSortOrder));
        List<SystemEventSubscriptionEntity> result = query.fetch();
        return result == null ? new ArrayList<>() : result;
    }

    /**
     * 保存前全量校验。
     *
     * @param entity 订阅实体
     */
    private void validateForSave(SystemEventSubscriptionEntity entity) {
        AssertUtil.isTrue(entity == null).i18n("event.subscription.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getReporterId())).i18n("event.subscription.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getEventCode())).i18n("event.subscription.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getTriggerId())).i18n("event.subscription.trigger.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        validateCodeLength(entity.getReporterId(), "event.subscription.reporter.invalid");
        validateCodeLength(entity.getEventCode(), "event.subscription.event.invalid");
        validateCodeLength(entity.getTriggerId(), "event.subscription.trigger.invalid");
        validateRuleCode(entity.getRuleCode());
        validateSortOrder(entity.getSortOrder());
        validateEnabled(entity.getEnabled());
    }

    /**
     * 规范化字段：去空格并补默认值。
     *
     * @param entity 订阅实体
     */
    private void normalize(SystemEventSubscriptionEntity entity) {
        entity.setReporterId(entity.getReporterId().trim());
        entity.setEventCode(entity.getEventCode().trim());
        entity.setTriggerId(entity.getTriggerId().trim());
        if (StrUtil.isNotEmpty(entity.getRuleCode())) {
            entity.setRuleCode(entity.getRuleCode().trim());
        }
    }

    /**
     * 校验标识字段长度。
     *
     * @param value 字段值
     * @param key   校验失败文案 key
     */
    private void validateCodeLength(String value, String key) {
        AssertUtil.isTrue(value != null && value.trim().length() > MAX_CODE_LENGTH).i18n(key)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验条件规则编码。
     *
     * @param ruleCode 条件规则编码
     */
    private void validateRuleCode(String ruleCode) {
        if (StrUtil.isEmpty(ruleCode)) {
            return;
        }
        AssertUtil.isTrue(ruleCode.trim().length() > MAX_RULE_CODE_LENGTH)
            .i18n("event.subscription.rule.code.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验执行顺序。
     *
     * @param sortOrder 执行顺序
     */
    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return;
        }
        AssertUtil.isTrue(sortOrder < 0).i18n("event.subscription.order.invalid")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验启用标记。
     *
     * @param enabled 启用标记
     */
    private void validateEnabled(Integer enabled) {
        if (enabled == null) {
            return;
        }
        AssertUtil.isTrue(!ENABLED.equals(enabled) && !DISABLED.equals(enabled))
            .i18n("event.subscription.enabled.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }
}
