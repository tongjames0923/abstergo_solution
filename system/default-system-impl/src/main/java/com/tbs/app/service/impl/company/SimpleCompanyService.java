package com.tbs.app.service.impl.company;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.SystemCompanyFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.ITimeAuditService;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.model.TreeNodeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SimpleCompanyService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemCompanyEntity, Long, SystemCompanyMapper>
    implements ISystemCompanyService, IStartup, ITimeAuditService<SystemCompanyEntity, Long> {
    /**
     * 系统公司
     */
    public static final SystemCompanyEntity SYSTEM_COMPANY =
        new SystemCompanyEntity(1L, "系统公司", "系统公司", "系统公司", null, null, null,
            SystemCompanyFlagEnum.SYSTEMED.getCode(), null, null, CommonConstant.FLAG_ON, 0L);

    @Resource
    private SystemCompanyMapper mapper;

    @Resource
    private ITreeStore treeStore;

    @Override
    protected Integer getFlagDisable() {
        return SystemCompanyFlagEnum.DELETED.getCode();
    }

    @Override
    public SystemCompanyEntity deleteById(Long id) {
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
    protected SystemCompanyEntity insertNew(SystemCompanyEntity entity) {
        entity.setFlag(SystemCompanyFlagEnum.DISABLED.getCode());
        entity.setTreeRootFlag(CommonConstant.FLAG_ON);
        entity.setChildrenCount(0L);
        getMapper().insert(entity);
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
            AssertUtil.isTrue(that != null, "公司税号已存在");
            oldOne.setTaxNumber(newOne.getTaxNumber());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getLogoImage())) {
            oldOne.setLogoImage(newOne.getLogoImage());
        }
        if (newOne.getFlag() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(SystemCompanyFlagEnum.class, newOne.getFlag()), "无效标志");
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
    protected Optional<ITimeAuditService<SystemCompanyEntity, Long>> getTimeAuditServiceSupport() {
        return Optional.of(this);
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
    public SystemCompanyEntity triggerCreateTime(SystemCompanyEntity entity, Date createTime) {
        entity.setCreateTime(createTime);
        return entity;
    }

    @Override
    public SystemCompanyEntity triggerUpdateTime(SystemCompanyEntity entity, Date updateTime) {
        entity.setUpdateTime(updateTime);
        return entity;
    }

    @Override
    public SystemCompanyEntity triggerDeleteTime(SystemCompanyEntity entity, Date deleteTime) {
        entity.setUpdateTime(deleteTime);
        return entity;
    }

    private static final String createTreeGroupName(SystemCompanyEntity entity) {
        return "company_tree:" + entity.getId();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void appendChildCompany(Long parentId, Long childId) {
        var parent = getInstance().queryById(parentId);
        var child = getInstance().queryById(childId);
        AssertUtil.isTrue(parent == null || Objects.equals(parent.getFlag(), SystemCompanyFlagEnum.DELETED.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("目标父公司不存在").throwIt();
        AssertUtil.isTrue(child == null || Objects.equals(parent.getFlag(), SystemCompanyFlagEnum.DELETED.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("目标子公司不存在").throwIt();
        if (StrUtil.isEmpty(parent.getTreeNodeGroup())) {
            parent.setTreeNodeGroup(createTreeGroupName(parent));
            parent.setTreeNodeId(parent.getId().toString());
            var tree = new TreeNodeModel();
            tree.setTreeGroup(parent.getTreeNodeGroup());
            tree.setNodeId(parent.getTreeNodeId());
            treeStore.store(tree, null);
        }
        parent.setChildrenCount(parent.getChildrenCount() + 1L);
        getMapper().updateByPrimaryKey(parent);
        AssertUtil.isTrue(StrUtil.isNotEmpty(child.getTreeNodeGroup()) &&
                !Objects.equals(child.getTreeNodeGroup(), parent.getTreeNodeGroup()))
            .message("不能将子公司添加到其他公司下").errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();

        if (StrUtil.isEmpty(child.getTreeNodeGroup())) {
            child.setTreeNodeGroup(parent.getTreeNodeGroup());
            child.setTreeNodeId(child.getId().toString());
            child.setTreeRootFlag(CommonConstant.FLAG_OFF);
            getMapper().updateByPrimaryKey(child);
        }
        treeStore.store(new TreeNodeModel(child.getTreeNodeId(), child.getTreeNodeGroup()), parent.getTreeNodeId());
    }

    @Override
    public List<SystemCompanyEntity> getChildrenCompanies(Long id) {
        var p = getInstance().queryById(id);
        return treeStore.getChildren(p.getTreeNodeId()).stream()
            .map(it -> getInstance().queryById(Long.parseLong(it.getNodeId()))).collect(Collectors.toList());
    }
}
