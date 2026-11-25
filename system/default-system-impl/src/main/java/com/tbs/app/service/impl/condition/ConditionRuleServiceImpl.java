package com.tbs.app.service.impl.condition;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.condition.IConditionRuleService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.condition.ConditionRuleMapper;
import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.tbs.app.service.impl.CacheNamesConstants.CONDITION_RULE;
import static com.tbs.app.service.impl.CacheNamesConstants.CONDITION_RULE_CODE;

/**
 * 条件规则服务实现。
 *
 * <p>保存时校验编码格式、唯一性、取值器存在性与调用载荷合法性；求值时只信任
 * {@code getterId + invocationJson}，任何异常都降级为 false，避免中断脚本或事件链路。</p>
 *
 * @author abstergo
 */
@Slf4j
public class ConditionRuleServiceImpl extends
    AbstractLogicDeleteFullFeatureServiceTemplate<SystemConditionRuleEntity, Long, ConditionRuleMapper>
    implements IConditionRuleService {

    /**
     * 规则编码格式：字母、数字、下划线、中划线、点，长度 2-64
     */
    private static final Pattern RULE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{2,64}$");

    /**
     * 单个 JSON 字段的最大长度
     */
    private static final int MAX_JSON_LENGTH = 200_000;

    /**
     * 启用标记
     */
    private static final Integer ENABLED = 1;

    /**
     * 停用标记
     */
    private static final Integer DISABLED = 0;

    /**
     * 审计服务
     */
    private static final IAuditService<SystemConditionRuleEntity, Long, Long> AUDIT_SERVICE =
        new DefaultAuditService<>();

    /**
     * 规则 Mapper
     */
    @Resource
    private ConditionRuleMapper conditionRuleMapper;

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
    protected ConditionRuleMapper getMapper() {
        return conditionRuleMapper;
    }

    @Override
    protected SystemConditionRuleEntity newEntity() {
        return new SystemConditionRuleEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected Optional<IAuditService<SystemConditionRuleEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(AUDIT_SERVICE);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CONDITION_RULE, key = "#entity.id", condition = "#entity!=null&&#entity.id!=null"),
        @CacheEvict(cacheNames = CONDITION_RULE_CODE, allEntries = true)})
    @Override
    public List<SystemConditionRuleEntity> save(SystemConditionRuleEntity entity) {
        return super.save(entity);
    }

    @Cacheable(cacheNames = CONDITION_RULE, key = "#id", unless = "#result==null")
    @ExpireOn(cacheNames = CONDITION_RULE, value = 10, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemConditionRuleEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Caching(evict = {@CacheEvict(cacheNames = CONDITION_RULE, key = "#id"),
        @CacheEvict(cacheNames = CONDITION_RULE_CODE, allEntries = true)})
    @Override
    public SystemConditionRuleEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    @Caching(evict = {@CacheEvict(cacheNames = CONDITION_RULE, allEntries = true),
        @CacheEvict(cacheNames = CONDITION_RULE_CODE, allEntries = true)})
    @Override
    public int deleteByIds(Collection<Long> ids) {
        return super.deleteByIds(ids);
    }

    @Override
    protected SystemConditionRuleEntity insertNew(SystemConditionRuleEntity entity) {
        validateForSave(entity, true);
        entity.setRuleCode(entity.getRuleCode().trim());
        entity.setEnabled(entity.getEnabled() == null ? ENABLED : entity.getEnabled());
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        log.info("条件规则创建完成, ruleCode={}, getterId={}", entity.getRuleCode(), entity.getGetterId());
        return entity;
    }

    @Override
    protected SystemConditionRuleEntity compareAndUpdateValue(SystemConditionRuleEntity newOne,
        SystemConditionRuleEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getRuleCode()) && !newOne.getRuleCode().equals(oldOne.getRuleCode())) {
            validateRuleCode(newOne.getRuleCode());
            AssertUtil.isTrue(queryByCode(newOne.getRuleCode().trim()) != null)
                .i18n("condition.rule.code.exists").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            oldOne.setRuleCode(newOne.getRuleCode().trim());
        }
        if (StrUtil.isNotEmpty(newOne.getRuleName())) {
            oldOne.setRuleName(newOne.getRuleName());
        }
        if (newOne.getDescription() != null) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getGetterId()) && !newOne.getGetterId().equals(oldOne.getGetterId())) {
            validateGetter(newOne.getGetterId());
            oldOne.setGetterId(newOne.getGetterId());
        }
        if (StrUtil.isNotEmpty(newOne.getInvocationJson())) {
            validateInvocation(newOne.getInvocationJson());
            oldOne.setInvocationJson(newOne.getInvocationJson());
        }
        if (newOne.getEditorJson() != null) {
            validateEditor(newOne.getEditorJson());
            oldOne.setEditorJson(newOne.getEditorJson());
        }
        if (newOne.getEnabled() != null) {
            validateEnabled(newOne.getEnabled());
            oldOne.setEnabled(newOne.getEnabled());
        }
        log.info("条件规则更新完成, ruleCode={}, enabled={}", oldOne.getRuleCode(), oldOne.getEnabled());
        return oldOne;
    }

    @Cacheable(cacheNames = CONDITION_RULE_CODE, key = "#ruleCode", unless = "#result==null")
    @ExpireOn(cacheNames = CONDITION_RULE_CODE, value = 10, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemConditionRuleEntity queryByCode(String ruleCode) {
        if (StrUtil.isEmpty(ruleCode)) {
            return null;
        }
        List<SystemConditionRuleEntity> rules = QueryEngine.getEntityQueryWrapper(new SystemConditionRuleEntity())
            .query().where()
            .equal(SystemConditionRuleEntity::getRuleCode, ruleCode.trim())
            .equal(SystemConditionRuleEntity::getFlag, CommonFlagEnum.ACTIVE.getCode())
            .pour().fetch();
        return CollUtil.getFirst(rules);
    }

    @Caching(evict = {@CacheEvict(cacheNames = CONDITION_RULE, key = "#id"),
        @CacheEvict(cacheNames = CONDITION_RULE_CODE, allEntries = true)})
    @Override
    public void setEnabled(Long id, boolean enabled) {
        AssertUtil.isTrue(id == null).i18n("condition.rule.id.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemConditionRuleEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n("condition.rule.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setEnabled(enabled ? ENABLED : DISABLED);
        getMapper().updateByPrimaryKey(entity);
        log.info("条件规则状态变更, ruleCode={}, enabled={}", entity.getRuleCode(), enabled);
    }

    @Caching(evict = {@CacheEvict(cacheNames = CONDITION_RULE, allEntries = true),
        @CacheEvict(cacheNames = CONDITION_RULE_CODE, key = "#ruleCode")})
    @Override
    public void deleteByCode(String ruleCode) {
        SystemConditionRuleEntity entity = queryByCode(ruleCode);
        AssertUtil.isTrue(entity == null).i18n("condition.rule.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        getInstance().deleteById(entity.getId());
        log.info("条件规则已删除, ruleCode={}", ruleCode);
    }

    /**
     * 保存前校验。
     *
     * @param entity     规则实体
     * @param checkExists 是否校验编码唯一
     */
    private void validateForSave(SystemConditionRuleEntity entity, boolean checkExists) {
        AssertUtil.isTrue(entity == null).i18n("condition.rule.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        validateRuleCode(entity.getRuleCode());
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getRuleName())).i18n("condition.rule.name.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        validateGetter(entity.getGetterId());
        validateInvocation(entity.getInvocationJson());
        validateEditor(entity.getEditorJson());
        validateEnabled(entity.getEnabled());
        if (checkExists) {
            AssertUtil.isTrue(queryByCode(entity.getRuleCode()) != null).i18n("condition.rule.code.exists")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
    }

    /**
     * 校验规则编码格式。
     *
     * @param ruleCode 规则编码
     */
    private void validateRuleCode(String ruleCode) {
        AssertUtil.isTrue(StrUtil.isEmpty(ruleCode)).i18n("condition.rule.code.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!RULE_CODE_PATTERN.matcher(ruleCode.trim()).matches())
            .i18n("condition.rule.code.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验取值器存在。
     *
     * @param getterId 取值器 ID
     */
    private void validateGetter(String getterId) {
        AssertUtil.isTrue(StrUtil.isEmpty(getterId)).i18n("condition.rule.getter.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(getterId.length() > 128).i18n("condition.rule.getter.invalid")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验调用载荷为合法的参数列表 JSON。
     *
     * @param json 调用载荷
     */
    private void validateInvocation(String json) {
        AssertUtil.isTrue(StrUtil.isEmpty(json)).i18n("condition.rule.invocation.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(json.length() > MAX_JSON_LENGTH).i18n("condition.rule.json.too.long")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        Object parsed;
        try {
            parsed = JsonUtils.parseObject(json, Object.class);
        } catch (Exception e) {
            parsed = null;
        }
        AssertUtil.isTrue(!(parsed instanceof List) || ((List<?>) parsed).isEmpty())
            .i18n("condition.rule.invocation.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验编辑器快照长度。
     *
     * @param json 编辑器快照
     */
    private void validateEditor(String json) {
        if (StrUtil.isEmpty(json)) {
            return;
        }
        AssertUtil.isTrue(json.length() > MAX_JSON_LENGTH).i18n("condition.rule.json.too.long")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        try {
            JsonUtils.parseObject(json, Object.class);
        } catch (Exception e) {
            AssertUtil.isTrue(true).i18n("condition.rule.editor.invalid")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
    }

    /**
     * 校验启用标记取值。
     *
     * @param enabled 启用标记
     */
    private void validateEnabled(Integer enabled) {
        if (enabled == null) {
            return;
        }
        AssertUtil.isTrue(!ENABLED.equals(enabled) && !DISABLED.equals(enabled))
            .i18n("condition.rule.enabled.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

}
