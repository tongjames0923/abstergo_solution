package com.tbs.app.service.impl.department;

import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemDepartmentFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author abstergo
 */
@Slf4j
public class SimpleDepartmentService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemDepartmentEntity, Long, SystemDepartmentMapper>
    implements ISystemDepartmentService, IStartup {

    private static final IAuditService<SystemDepartmentEntity, Long, Long> auditService = new DefaultAuditService<>();
    private static ISystemDepartmentService instance;
    @Resource
    @Lazy
    IDepartmentUserRelationService departmentUserRelationService;
    @Resource
    private SystemDepartmentMapper mapper;
    @Resource
    private DepartmentTreeStructService departmentTreeStructService;
    @Resource
    @Lazy
    private ISystemCompanyService companyService;

    @Override
    protected void deletePending(SystemDepartmentEntity entity) throws FormattedRuntimeException {
        AssertUtil.isTrue(CollUtil.isNotEmpty(departmentUserRelationService.queryDepartmentUsers(entity.getId())))
            .i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_HAS_USERS_NOT_DELETE).throwIt();
        AssertUtil.isTrue(!departmentTreeStructService.isLeaf(entity.getId()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_HAS_CHILDREN_NOT_DELETE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        super.deletePending(entity);
    }

    @Override
    public SystemDepartmentEntity deleteById(Long id) {
        var entity = super.deleteById(id);
        boolean deleted = departmentTreeStructService.deleteLeaf(id);
        AssertUtil.isTrue(!deleted).i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_DELETE_FAILED).throwIt();
        return entity;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemDepartmentEntity insertNew(SystemDepartmentEntity entity) {

        AssertUtil.isTrueUserInputError(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_EMPTY)
            .throwIt();
        AssertUtil.isTrueUserInputError(StrUtil.isEmpty(entity.getDepartmentName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_NAME_EMPTY).throwIt();
        AssertUtil.isTrueUserInputError(StrUtil.isEmpty(entity.getDepartmentCode()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_CODE_EMPTY).throwIt();
        AssertUtil.isTrueUserInputError(entity.getCompanyId() == null)
            .i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_COMPANY_EMPTY).throwIt();
        var company = SpringUtil.getBean(ISystemCompanyService.class).queryById(entity.getCompanyId());
        AssertUtil.isTrueUserInputError(company == null).i18n(com.tbs.app.constant.SystemI18nConstant.INVALID_COMPANY)
            .throwIt();

        entity.setFlag(SystemDepartmentFlagEnum.DISABLED.getCode());
        getMapper().insert(entity);
        departmentTreeStructService.setToTree(entity.getId(), null);
        return entity;
    }

    @Override
    protected SystemDepartmentEntity compareAndUpdateValue(SystemDepartmentEntity newOne,
        SystemDepartmentEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getDepartmentName())) {
            oldOne.setDepartmentName(newOne.getDepartmentName());
        }
        if (StrUtil.isNotEmpty(newOne.getDepartmentCode())) {
            oldOne.setDepartmentCode(newOne.getDepartmentCode());
        }
        if (newOne.getCompanyId() != null) {
            var newComp = companyService.queryById(newOne.getCompanyId());
            AssertUtil.isTrueUserInputError(newComp == null)
                .i18n(com.tbs.app.constant.SystemI18nConstant.INVALID_COMPANY).throwIt();
            oldOne.setCompanyId(newOne.getCompanyId());
        }
        if (StrUtil.isNotEmpty(newOne.getManager())) {
            oldOne.setManager(newOne.getManager());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (newOne.getParentId() != null) {
            var newParent = queryById(newOne.getParentId());
            AssertUtil.isTrueUserInputError(newParent == null)
                .i18n(com.tbs.app.constant.SystemI18nConstant.INVALID_DEPARTMENT).throwIt();
            oldOne.setParentId(newOne.getParentId());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrueUserInputError(!EnumUtil.isValidCode(SystemDepartmentFlagEnum.class, newOne.getFlag()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.INVALID_FLAG).throwIt();
            oldOne.setFlag(newOne.getFlag());
        }

        return oldOne;
    }

    @Override
    protected SystemDepartmentMapper getMapper() {
        return mapper;
    }

    /**
     * 获取禁用的标志
     *
     * @return
     */
    @Override
    protected Integer getFlagDeleted() {
        return SystemDepartmentFlagEnum.DELETED.getCode();
    }

    @Override
    protected SystemDepartmentEntity newEntity() {
        return new SystemDepartmentEntity();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void appendChildDepartment(Long parentId, Long childId) {
        departmentTreeStructService.setToTree(childId, parentId);
    }

    @Override
    public List<SystemDepartmentEntity> getChildrenDepartments(Long id) {
        return departmentTreeStructService.getChildren(id).stream().map(it -> getInstance().queryById(it.getEntityId()))
            .collect(Collectors.toList());
    }

    @Override
    public void startUp() throws RuntimeException {
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    protected Optional<IAuditService<SystemDepartmentEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
