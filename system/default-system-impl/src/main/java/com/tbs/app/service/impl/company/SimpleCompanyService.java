package com.tbs.app.service.impl.company;

import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemCompanyFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.tbs.app.service.impl.CacheNamesConstants.COMPANY_QUERY_ID;

/**
 * 系统公司服务实现类
 *
 * @author abstergo
 */
@Slf4j
public class SimpleCompanyService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemCompanyEntity, Long, SystemCompanyMapper>
    implements ISystemCompanyService, IStartup {

    @Resource
    @Lazy
    private ISystemDepartmentService departmentService;

    /**
     * 系统公司
     */
    public static final SystemCompanyEntity SYSTEM_COMPANY =
        new SystemCompanyEntity(1L, "系统公司", "系统公司", "系统公司", null, null, null,
            SystemCompanyFlagEnum.SYSTEMED.getCode(), 0L, null, null);

    @Resource
    private SystemCompanyMapper mapper;

    @Resource
    CompanyTreeStructService companyTreeStructService;

    @Override
    public SystemCompanyEntity deleteById(Long id) {
        CommonSystemDepartmentQueryCriteria criteria = new CommonSystemDepartmentQueryCriteria();
        criteria.setEqualsCompanyId(id);
        var ls = departmentService.query(criteria, null);
        AssertUtil.isTrueBusinessError(CollUtil.isNotEmpty(ls)).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_HAS_DEPARTMENTS_NOT_DELETE).throwIt();
        var entity = super.deleteById(id);
        boolean deleted = companyTreeStructService.deleteLeaf(id);
        AssertUtil.isTrueBusinessError(!deleted).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_DELETE_FAILED).throwIt();
        return entity;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemCompanyEntity insertNew(SystemCompanyEntity entity) {
        AssertUtil.isTrue(entity == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_EMPTY).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getCompanyName())).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_NAME_EMPTY).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getTaxNumber())).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_TAX_NUMBER_EMPTY).throwIt();
        var oldOne =
            getQueryWrapper().query().where().equal(SystemCompanyEntity::getTaxNumber, entity.getTaxNumber()).pour()
                .fetchOne();
        AssertUtil.isTrue(oldOne != null).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_TAX_NUMBER_EXISTS).throwIt();

        entity.setFlag(SystemCompanyFlagEnum.DISABLED.getCode());
        getMapper().insert(entity);
        companyTreeStructService.setToTree(entity.getId(), null);
        return entity;
    }

    @Override
    protected List<SystemCompanyEntity> updateBy(SystemCompanyEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemCompanyEntity compareAndUpdateValue(SystemCompanyEntity newOne, SystemCompanyEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getCompanyName())) {
            oldOne.setCompanyName(newOne.getCompanyName());
        }
        if (StrUtil.isNotEmpty(newOne.getLogoImage())) {
            oldOne.setLogoImage(newOne.getLogoImage());
        }
        if (StrUtil.isNotEmpty(newOne.getTaxNumber()) &&
            !Objects.equals(newOne.getTaxNumber(), oldOne.getTaxNumber())) {
            SystemCompanyEntity that =
                getQueryWrapper().query().where().equal(SystemCompanyEntity::getTaxNumber, newOne.getTaxNumber()).pour()
                    .fetchOne();
            AssertUtil.isTrueBusinessError(that != null).i18n(com.tbs.app.constant.SystemI18nConstant.COMPANY_TAX_NUMBER_EXISTS).throwIt();
            oldOne.setTaxNumber(newOne.getTaxNumber());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getLogoImage())) {
            oldOne.setLogoImage(newOne.getLogoImage());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrueUserInputError(SystemCompanyFlagEnum.DELETED.getCode().equals(newOne.getFlag()) ||
                    !EnumUtil.isValidCode(SystemCompanyFlagEnum.class, newOne.getFlag()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.INVALID_FLAG).throwIt();
            oldOne.setFlag(newOne.getFlag());
        }

        return oldOne;
    }

    @Override
    protected SystemCompanyMapper getMapper() {
        return mapper;
    }

    @Override
    protected SystemCompanyEntity newEntity() {
        return new SystemCompanyEntity();
    }

    @Override
    public void startUp() throws RuntimeException {
        log.info("启动系统公司服务");
        mapper.deleteByPrimaryKey(SYSTEM_COMPANY.getId());
        mapper.insert(SYSTEM_COMPANY);
        log.info("初始化系统公司成功");
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    private static final IAuditService<SystemCompanyEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<SystemCompanyEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }

    /**
     * @param id
     * @return
     */

    @Cacheable(value = COMPANY_QUERY_ID, key = "#id")
    @ExpireOn(cacheNames = COMPANY_QUERY_ID, value = 10, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemCompanyEntity queryById(Long id) {
        return super.queryById(id);
    }

    /**
     * 获取禁用的标志
     *
     * @return
     */
    @Override
    protected Integer getFlagDeleted() {
        return SystemCompanyFlagEnum.DELETED.getCode();
    }
}
