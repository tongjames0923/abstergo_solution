package com.tbs.app.service.impl.event;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.event.IEventActionService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.event.EventActionMapper;
import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
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
 * 事件动作绑定服务实现。
 *
 * <p>保存时校验事件定位字段、动作必填、同一事件下动作不重复、参数载荷为合法参数列表 JSON；
 * 运行期查询只返回启用且未删除的绑定，并按顺序升序返回，保证动作执行顺序稳定。</p>
 *
 * @author abstergo
 */
@Slf4j
public class EventActionServiceImpl extends
    AbstractLogicDeleteFullFeatureServiceTemplate<SystemEventActionEntity, Long, EventActionMapper>
    implements IEventActionService {

    /**
     * 启用标记
     */
    private static final Integer ENABLED = 1;

    /**
     * 停用标记
     */
    private static final Integer DISABLED = 0;

    /**
     * 上报器ID / 事件编码 / 动作ID 的最大长度
     */
    private static final int MAX_CODE_LENGTH = 128;

    /**
     * 动作名称展示快照的最大长度
     */
    private static final int MAX_ACTION_NAME_LENGTH = 128;

    /**
     * 参数 JSON 的最大长度
     */
    private static final int MAX_JSON_LENGTH = 200_000;

    /**
     * 审计服务
     */
    private static final IAuditService<SystemEventActionEntity, Long, Long> AUDIT_SERVICE =
        new DefaultAuditService<>();

    /**
     * 动作绑定 Mapper
     */
    @Resource
    private EventActionMapper eventActionMapper;

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
    protected EventActionMapper getMapper() {
        return eventActionMapper;
    }

    @Override
    protected SystemEventActionEntity newEntity() {
        return new SystemEventActionEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected Optional<IAuditService<SystemEventActionEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(AUDIT_SERVICE);
    }

    @Override
    protected SystemEventActionEntity insertNew(SystemEventActionEntity entity) {
        validateForSave(entity);
        normalize(entity);
        entity.setEnabled(entity.getEnabled() == null ? ENABLED : entity.getEnabled());
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        log.info("事件动作绑定创建完成, reporterId={}, eventCode={}, actionId={}", entity.getReporterId(),
            entity.getEventCode(), entity.getActionId());
        return entity;
    }

    @Override
    protected SystemEventActionEntity compareAndUpdateValue(SystemEventActionEntity newOne,
        SystemEventActionEntity oldOne) {
        if (newOne.getActionName() != null) {
            validateActionName(newOne.getActionName());
            oldOne.setActionName(newOne.getActionName());
        }
        if (newOne.getParamJson() != null) {
            validateParamJson(newOne.getParamJson());
            oldOne.setParamJson(newOne.getParamJson());
        }
        if (newOne.getSortOrder() != null) {
            validateSortOrder(newOne.getSortOrder());
            oldOne.setSortOrder(newOne.getSortOrder());
        }
        if (newOne.getEnabled() != null) {
            validateEnabled(newOne.getEnabled());
            oldOne.setEnabled(newOne.getEnabled());
        }
        log.info("事件动作绑定更新完成, reporterId={}, eventCode={}, actionId={}, enabled={}", oldOne.getReporterId(),
            oldOne.getEventCode(), oldOne.getActionId(), oldOne.getEnabled());
        return oldOne;
    }

    @Override
    public List<SystemEventActionEntity> listByEvent(String reporterId, String eventCode) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode)) {
            return List.of();
        }
        return queryByEvent(reporterId.trim(), eventCode.trim(), false);
    }

    @Override
    public List<SystemEventActionEntity> listEnabled(String reporterId, String eventCode) {
        if (StrUtil.isBlank(reporterId) || StrUtil.isBlank(eventCode)) {
            return List.of();
        }
        return queryByEvent(reporterId.trim(), eventCode.trim(), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<SystemEventActionEntity> replaceByEvent(String reporterId, String eventCode,
        Collection<SystemEventActionEntity> actions) {
        AssertUtil.isTrue(StrUtil.isBlank(reporterId)).i18n("event.action.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(eventCode)).i18n("event.action.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String reporter = reporterId.trim();
        String event = eventCode.trim();
        validateCodeLength(reporter, "event.action.reporter.invalid");
        validateCodeLength(event, "event.action.event.invalid");

        Map<String, SystemEventActionEntity> incoming = new LinkedHashMap<>();
        if (actions != null) {
            for (SystemEventActionEntity action : actions) {
                AssertUtil.isTrue(action == null).i18n("event.action.required")
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
                action.setReporterId(reporter);
                action.setEventCode(event);
                validateForSave(action);
                normalize(action);
                AssertUtil.isTrue(incoming.containsKey(action.getActionId()))
                    .i18n("event.action.action.duplicate", action.getActionId())
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
                incoming.put(action.getActionId(), action);
            }
        }

        Map<String, SystemEventActionEntity> existing = new LinkedHashMap<>();
        queryByEvent(reporter, event, false).forEach(action -> existing.put(action.getActionId(), action));

        for (SystemEventActionEntity action : incoming.values()) {
            SystemEventActionEntity current = existing.remove(action.getActionId());
            if (current == null) {
                getInstance().save(action);
                continue;
            }
            current.setActionName(action.getActionName());
            current.setParamJson(action.getParamJson());
            current.setSortOrder(action.getSortOrder());
            if (action.getEnabled() != null) {
                current.setEnabled(action.getEnabled());
            }
            getInstance().save(current);
        }
        for (SystemEventActionEntity stale : existing.values()) {
            getInstance().deleteById(stale.getId());
        }
        log.info("事件动作绑定整体替换完成, reporterId={}, eventCode={}, 动作数量={}", reporter, event, incoming.size());
        return queryByEvent(reporter, event, false);
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        AssertUtil.isTrue(id == null).i18n("event.action.id.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemEventActionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n("event.action.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setEnabled(enabled ? ENABLED : DISABLED);
        getMapper().updateByPrimaryKey(entity);
        log.info("事件动作绑定状态变更, id={}, actionId={}, enabled={}", id, entity.getActionId(), enabled);
    }

    /**
     * 按事件查询动作绑定，可选只返回启用中的记录。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param onlyEnabled 是否只返回启用中的记录
     * @return 按顺序升序的动作绑定列表
     */
    private List<SystemEventActionEntity> queryByEvent(String reporterId, String eventCode, boolean onlyEnabled) {
        IWhereClause<SystemEventActionEntity> where =
            QueryEngine.getEntityQueryWrapper(SystemEventActionEntity.class).query().where()
                .equal(SystemEventActionEntity::getReporterId, reporterId)
                .equal(SystemEventActionEntity::getEventCode, eventCode)
                .equal(SystemEventActionEntity::getFlag, CommonFlagEnum.ACTIVE.getCode());
        if (onlyEnabled) {
            where.equal(SystemEventActionEntity::getEnabled, ENABLED);
        }
        var query = where.pour();
        query.getOrderSupport().ifPresent(order -> order.orderByAsc(SystemEventActionEntity::getSortOrder));
        List<SystemEventActionEntity> result = query.fetch();
        return result == null ? new ArrayList<>() : result;
    }

    /**
     * 保存前全量校验。
     *
     * @param entity 动作绑定实体
     */
    private void validateForSave(SystemEventActionEntity entity) {
        AssertUtil.isTrue(entity == null).i18n("event.action.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getReporterId())).i18n("event.action.reporter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getEventCode())).i18n("event.action.event.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(entity.getActionId())).i18n("event.action.action.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        validateCodeLength(entity.getReporterId(), "event.action.reporter.invalid");
        validateCodeLength(entity.getEventCode(), "event.action.event.invalid");
        validateCodeLength(entity.getActionId(), "event.action.action.invalid");
        validateActionName(entity.getActionName());
        validateParamJson(entity.getParamJson());
        validateSortOrder(entity.getSortOrder());
        validateEnabled(entity.getEnabled());
    }

    /**
     * 规范化字段：去空格。
     *
     * @param entity 动作绑定实体
     */
    private void normalize(SystemEventActionEntity entity) {
        entity.setReporterId(entity.getReporterId().trim());
        entity.setEventCode(entity.getEventCode().trim());
        entity.setActionId(entity.getActionId().trim());
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
     * 校验动作名称展示快照。
     *
     * @param actionName 动作名称
     */
    private void validateActionName(String actionName) {
        if (actionName == null) {
            return;
        }
        AssertUtil.isTrue(actionName.length() > MAX_ACTION_NAME_LENGTH).i18n("event.action.name.invalid")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验动作参数载荷为合法的参数列表 JSON。
     *
     * @param json 参数载荷
     */
    private void validateParamJson(String json) {
        if (StrUtil.isEmpty(json)) {
            return;
        }
        AssertUtil.isTrue(json.length() > MAX_JSON_LENGTH).i18n("event.action.json.too.long")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        Object parsed;
        try {
            parsed = JsonUtils.parseObject(json, Object.class);
        } catch (Exception e) {
            parsed = null;
        }
        AssertUtil.isTrue(!(parsed instanceof List)).i18n("event.action.param.invalid")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
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
        AssertUtil.isTrue(sortOrder < 0).i18n("event.action.order.invalid")
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
            .i18n("event.action.enabled.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }
}
