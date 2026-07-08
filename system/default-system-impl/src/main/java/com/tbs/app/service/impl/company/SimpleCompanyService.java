package com.tbs.app.service.impl.company;

import com.tbs.app.account.annotation.OnlyActiveAccountUse;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemCompanyFlagEnum;
import com.tbs.shared.interfaces.service.AbstractPhysicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.ITimeAuditService;
import com.tbs.shared.interfaces.service.SimpleTimeAuditServiceImpl;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 系统公司服务实现类
 *
 * @author abstergo
 */
@Slf4j
public class SimpleCompanyService
    extends AbstractPhysicDeleteFullFeatureServiceTemplate<SystemCompanyEntity, Long, SystemCompanyMapper>
    implements ISystemCompanyService, IStartup {

    @Resource
    @Lazy
    private ISystemDepartmentService departmentService;

    /**
     * 系统公司
     */
    public static final SystemCompanyEntity SYSTEM_COMPANY =
        new SystemCompanyEntity(1L, "系统公司", "系统公司", "系统公司", null, null, null,
            SystemCompanyFlagEnum.SYSTEMED.getCode(), 0L);

    @Resource
    private SystemCompanyMapper mapper;

    @Resource
    CompanyTreeStructService companyTreeStructService;

    @Override
    @OnlyActiveAccountUse
    public SystemCompanyEntity deleteById(Long id) {
        CommonSystemDepartmentQueryCriteria criteria = new CommonSystemDepartmentQueryCriteria();
        criteria.setEqualsCompanyId(id);
        var ls = departmentService.query(criteria, null);
        AssertUtil.isTrueBusinessError(CollUtil.isNotEmpty(ls)).message("公司下有部门，请先删除部门").throwIt();
        var entity = super.deleteById(id);
        boolean deleted = companyTreeStructService.deleteLeaf(id);
        AssertUtil.isTrueBusinessError(!deleted).message("公司删除失败,可能存在子部门情况").throwIt();
        return entity;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemCompanyEntity insertNew(SystemCompanyEntity entity) {
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
            AssertUtil.isTrueBusinessError(that != null).message("公司税号已存在").throwIt();
            oldOne.setTaxNumber(newOne.getTaxNumber());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getLogoImage())) {
            oldOne.setLogoImage(newOne.getLogoImage());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrueUserInputError(!EnumUtil.isValidCode(SystemCompanyFlagEnum.class, newOne.getFlag()))
                .message("无效标志").throwIt();
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

    @Override
    protected Optional<ITimeAuditService<SystemCompanyEntity, Long>> getTimeAuditServiceSupport() {
        return Optional.of(new SimpleTimeAuditServiceImpl<SystemCompanyEntity>());
    }
}
