package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.SystemDepartmentFlagEnum;
import com.tbs.shared.interfaces.service.AbstractPhysicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.ITimeAuditService;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 部门服务实现
 *
 * @author abstergo
 */
@Slf4j
public class SimpleDepartmentService
    extends AbstractPhysicDeleteFullFeatureServiceTemplate<SystemDepartmentEntity, Long, SystemDepartmentMapper>
    implements ISystemDepartmentService, IStartup, ITimeAuditService<SystemDepartmentEntity, Long> {

    @Resource
    private SystemDepartmentMapper mapper;

    @Resource
    private ITreeStore treeStore;

    private static ISystemDepartmentService instance;


    @Override
    public SystemDepartmentEntity deleteById(Long id) {
        var entity = super.deleteById(id);
        if (StrUtil.isNotEmpty(entity.getTreeNodeGroup())) {
            treeStore.delete(entity.getTreeNodeId(), entity.getTreeNodeGroup());
        }
        return entity;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemDepartmentEntity insertNew(SystemDepartmentEntity entity) {
        entity.setFlag(SystemDepartmentFlagEnum.DISABLED.getCode());
        entity.setTreeRootFlag(CommonConstant.FLAG_ON);
        getMapper().insert(entity);
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
            oldOne.setCompanyId(newOne.getCompanyId());
        }
        if (StrUtil.isNotEmpty(newOne.getManager())) {
            oldOne.setManager(newOne.getManager());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(SystemDepartmentFlagEnum.class, newOne.getFlag()), "无效标志");
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

    @Override
    protected Optional<ITimeAuditService<SystemDepartmentEntity, Long>> getTimeAuditServiceSupport() {
        return Optional.of(this);
    }

    @Override
    public SystemDepartmentEntity triggerCreateTime(SystemDepartmentEntity entity, Date createTime) {
        entity.setCreateTime(createTime);
        return entity;
    }

    @Override
    public SystemDepartmentEntity triggerUpdateTime(SystemDepartmentEntity entity, Date updateTime) {
        entity.setUpdateTime(updateTime);
        return entity;
    }

    @Override
    public SystemDepartmentEntity triggerDeleteTime(SystemDepartmentEntity entity, Date deleteTime) {
        entity.setUpdateTime(deleteTime);
        return entity;
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

    }

    @Override
    public List<SystemDepartmentEntity> getChildrenDepartments(Long id) {
        //        var p = getInstance().queryById(id);
        //        return treeStore.getDirectChildren(p.getTreeNodeId(), p.getTreeNodeGroup()).stream()
        //            .map(it -> getInstance().queryById(Long.parseLong(it.getNodeId().substring(4, it.getNodeId().length()))))
        //            .collect(Collectors.toList());
        return null;
    }

    @Override
    public void startUp() throws RuntimeException {
        if (instance == null) {
            instance = this;
        }
    }
}
