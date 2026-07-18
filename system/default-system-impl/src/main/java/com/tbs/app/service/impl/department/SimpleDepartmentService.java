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
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemDepartmentFlagEnum;
import com.tbs.shared.interfaces.service.AbstractPhysicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    extends AbstractPhysicDeleteFullFeatureServiceTemplate<SystemDepartmentEntity, Long, SystemDepartmentMapper>
    implements ISystemDepartmentService, IStartup {

    @Resource
    private SystemDepartmentMapper mapper;

    @Resource
    private DepartmentTreeStructService departmentTreeStructService;

    private static ISystemDepartmentService instance;

    @Resource
    @Lazy
    IDepartmentUserRelationService departmentUserRelationService;

    @Resource
    @Lazy
    private ISystemCompanyService companyService;

    @Override
    protected void deletePending(SystemDepartmentEntity entity) throws FormattedRuntimeException {
        AssertUtil.isTrue(CollUtil.isNotEmpty(departmentUserRelationService.queryDepartmentUsers(entity.getId())),
            I18nUtil.get("department.has.users.not.delete"));
        AssertUtil.isTrue(!departmentTreeStructService.isLeaf(entity.getId())).message(I18nUtil.get("department.has.children.not.delete"))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        super.deletePending(entity);
    }

    @Override
    public SystemDepartmentEntity deleteById(Long id) {
        var entity = super.deleteById(id);
        boolean deleted = departmentTreeStructService.deleteLeaf(id);
        AssertUtil.isTrue(!deleted, I18nUtil.get("department.delete.failed"));
        return entity;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemDepartmentEntity insertNew(SystemDepartmentEntity entity) {
        entity.setFlag(SystemDepartmentFlagEnum.DISABLED.getCode());
        getMapper().insert(entity);
        departmentTreeStructService.setToTree(entity.getId(), null);
        return entity;
    }

    @Override
    protected List<SystemDepartmentEntity> updateBy(SystemDepartmentEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemDepartmentEntity compareAndUpdateValue(SystemDepartmentEntity newOne, SystemDepartmentEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getDepartmentName())) {
            oldOne.setDepartmentName(newOne.getDepartmentName());
        }
        if (StrUtil.isNotEmpty(newOne.getDepartmentCode())) {
            oldOne.setDepartmentCode(newOne.getDepartmentCode());
        }
        if (newOne.getCompanyId() != null) {
            var newComp = companyService.queryById(newOne.getCompanyId());
            AssertUtil.isTrueUserInputError(newComp == null).message(I18nUtil.get("invalid.company")).throwIt();
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
            AssertUtil.isTrueUserInputError(newParent == null).message(I18nUtil.get("invalid.department")).throwIt();
            oldOne.setParentId(newOne.getParentId());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrueUserInputError(!EnumUtil.isValidCode(SystemDepartmentFlagEnum.class, newOne.getFlag()))
                .message(I18nUtil.get("invalid.flag")).throwIt();
            oldOne.setFlag(newOne.getFlag());
        }

        return oldOne;
    }

    @Override
    protected SystemDepartmentMapper getMapper() {
        return mapper;
    }

    @Override
    protected SystemDepartmentEntity newEntity() {
        return new SystemDepartmentEntity();
    }

    private static final String createTreeGroupName(SystemDepartmentEntity entity) {
        return DEPARTMENT_TREE_GROUP + entity.getId();
    }

    private static final String createTreeNodeId(SystemDepartmentEntity entity) {
        return DEPARTMENT_TREE_GROUP + entity.getId().toString();
    }

    private static final String DEPARTMENT_TREE_GROUP = "DET:";

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void appendChildDepartment(Long parentId, Long childId) {
        departmentTreeStructService.setToTree(childId, parentId);
    }

    @Override
    public List<SystemDepartmentEntity> getChildrenDepartments(Long id) {
        return departmentTreeStructService.getChildren(id).stream()
            .map(it -> getInstance().queryById(it.getEntityId()))
            .collect(Collectors.toList());
    }

    @Override
    public void startUp() throws RuntimeException {
        if (instance == null) {
            instance = this;
        }
    }

    private static final IAuditService<SystemDepartmentEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<SystemDepartmentEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
