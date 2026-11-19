package com.tbs.app.service.impl.genericapi;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.genericapi.ISystemGenericApiParamService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.SystemGenericApiParamMapper;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.tbs.app.constant.SystemI18nConstant.*;
import static com.tbs.app.service.impl.CacheNamesConstants.GENERIC_API_PARAM;

/**
 * 泛化API入参定义服务实现
 * <p>
 * 元信息只在服务端登记一次，调用方与网关都不需要目标接口的 jar。校验规则全部在此以断言表达 （不依赖数据库的唯一索引或非空约束）：归属的泛化API必须存在、顺序必填且同API内不重复、类型必填。
 * </p>
 * <p>
 * 顺序只需<em>相对正确</em>，因此不校验「从 0 连续」；调用侧按 {@code paramOrder} 升序装配实参即可。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class GenericApiParamServiceImpl extends
    AbstractLogicDeleteFullFeatureServiceTemplate<SystemGenericApiParamEntity, Long, SystemGenericApiParamMapper>
    implements ISystemGenericApiParamService {

    @Resource
    private SystemGenericApiParamMapper genericApiParamMapper;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected SystemGenericApiParamMapper getMapper() {
        return genericApiParamMapper;
    }

    @Override
    protected SystemGenericApiParamEntity newEntity() {
        return new SystemGenericApiParamEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected SystemGenericApiParamEntity insertNew(SystemGenericApiParamEntity entity) {
        validate(entity);
        assertOrderNotDuplicated(entity, null);
        if (entity.getFlag() == null) {
            entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        }
        if (entity.getRequiredFlag() == null) {
            entity.setRequiredFlag(false);
        }
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected SystemGenericApiParamEntity compareAndUpdateValue(SystemGenericApiParamEntity newOne,
        SystemGenericApiParamEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getParamName())) {
            oldOne.setParamName(newOne.getParamName().trim());
        }
        if (StrUtil.isNotEmpty(newOne.getParamType())) {
            oldOne.setParamType(newOne.getParamType().trim());
        }
        if (newOne.getParamOrder() != null) {
            oldOne.setParamOrder(newOne.getParamOrder());
        }
        if (newOne.getRequiredFlag() != null) {
            oldOne.setRequiredFlag(newOne.getRequiredFlag());
        }
        if (newOne.getDescription() != null) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (newOne.getExample() != null) {
            oldOne.setExample(newOne.getExample());
        }
        if (newOne.getFlag() != null) {
            oldOne.setFlag(newOne.getFlag());
        }
        validate(oldOne);
        assertOrderNotDuplicated(oldOne, oldOne.getId());
        return oldOne;
    }

    /**
     * 按业务标识读取入参定义
     * <p>
     * 泛化调用每次请求都会读取，因此整组缓存；写路径（保存/删除）通过同名缓存失效，不额外维护派生缓存。
     * </p>
     *
     * @param apiId API唯一标识，不能为空
     * @return 按 {@code paramOrder} 升序排列的参数定义，无登记时为空列表
     */
    @Cacheable(cacheNames = GENERIC_API_PARAM, key = "#apiId", unless = "#result==null")
    @Override
    public List<SystemGenericApiParamEntity> listByApiId(String apiId) {
        List<SystemGenericApiParamEntity> params =
            QueryEngine.getEntityQueryWrapper(new SystemGenericApiParamEntity()).query().where()
                .equal(SystemGenericApiParamEntity::getApiId, apiId).pour().fetch();
        if (CollUtil.isEmpty(params)) {
            return List.of();
        }
        return params.stream().sorted(Comparator.comparing(SystemGenericApiParamEntity::getParamOrder,
            Comparator.nullsLast(Comparator.naturalOrder()))).toList();
    }

    @CacheEvict(cacheNames = GENERIC_API_PARAM, allEntries = true)
    @Override
    public List<SystemGenericApiParamEntity> save(SystemGenericApiParamEntity entity) {
        return super.save(entity);
    }

    @CacheEvict(cacheNames = GENERIC_API_PARAM, allEntries = true)
    @Override
    public SystemGenericApiParamEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    /**
     * 校验单条入参定义的必填项与归属
     * <p>
     * 归属存在性按主表业务键直查而不注入主表服务：主表服务的整组保存又要调用本服务， 反向注入会构成 Bean 循环依赖（Boot 3 默认不允许）；本校验只在登记时发生一次，不依赖主表缓存。
     * </p>
     *
     * @param entity 待校验的入参定义
     */
    private void validate(SystemGenericApiParamEntity entity) {
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getApiId())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(GENERIC_API_PARAM_API_ID_REQUIRED).throwIt();
        SystemGenericApiEntity apiConfig =
            QueryEngine.getEntityQueryWrapper(new SystemGenericApiEntity()).query().where()
                .equal(SystemGenericApiEntity::getApiId, entity.getApiId()).pour().fetchOne();
        AssertUtil.isTrue(apiConfig == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(GENERIC_API_PARAM_API_NOT_EXISTS).params(entity.getApiId()).throwIt();
        AssertUtil.isTrue(entity.getParamOrder() == null || entity.getParamOrder() < 0)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(GENERIC_API_PARAM_ORDER_REQUIRED).throwIt();
        String paramLabel =
            StrUtil.isEmpty(entity.getParamName()) ? "#" + entity.getParamOrder() : entity.getParamName();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getParamType())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(GENERIC_API_PARAM_TYPE_REQUIRED).params(paramLabel).throwIt();
        if (StrUtil.isNotEmpty(entity.getParamName())) {
            entity.setParamName(entity.getParamName().trim());
        }
        entity.setParamType(entity.getParamType().trim());
    }

    /**
     * 校验同一 API 下参数顺序不重复
     * <p>
     * 判重取启用态同序行中是否存在非自身的主键：新增时自身主键尚未生成，任何同序行都算重复。 唯一性由本断言保证，不依赖数据库唯一索引（总纲铁律 14）。
     * </p>
     *
     * @param entity    待校验的入参定义
     * @param excludeId 更新时排除自身主键，新增时传 {@code null}
     */
    private void assertOrderNotDuplicated(SystemGenericApiParamEntity entity, Long excludeId) {
        List<SystemGenericApiParamEntity> siblings =
            QueryEngine.getEntityQueryWrapper(new SystemGenericApiParamEntity()).query().where()
                .equal(SystemGenericApiParamEntity::getApiId, entity.getApiId())
                .equal(SystemGenericApiParamEntity::getParamOrder, entity.getParamOrder()).pour().fetch();
        if (CollUtil.isEmpty(siblings)) {
            return;
        }
        boolean duplicated = siblings.stream().anyMatch(sibling -> !Objects.equals(sibling.getId(), excludeId));
        AssertUtil.isTrue(duplicated).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(GENERIC_API_PARAM_ORDER_DUPLICATE).params(entity.getParamOrder()).throwIt();
    }
}
