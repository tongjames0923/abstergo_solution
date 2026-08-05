package com.tbs.app.service.impl.role;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemRoleGroupMapper;
import com.tbs.shared.mapper.SystemRoleGroupRelationMapper;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;
import com.tbs.shared.pojo.entity.SystemRoleGroupRelationEntity;
import com.tbs.sql.dsl.clause.JoinCondition;
import com.tbs.sql.service.QueryEngine;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 角色组服务实现类
 *
 * @author abstergo
 */
public class SimpleRoleGroupServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRoleGroupEntity, Long, SystemRoleGroupMapper>
    implements ISystemRoleGroupService {

    @Resource
    private SystemRoleGroupMapper groupMapper;

    @Resource
    private SystemRoleGroupRelationMapper relationMapper;

    @Resource
    private AppSettingProperty property;

    /**
     * 获取角色组下的权限列表
     *
     * @param groupId
     * @return 权限列表
     */
    @Override
    public List<SystemRoleEntity> getRoleList(Long groupId) {
        return QueryEngine.getEntityQueryWrapper(new SystemRoleEntity()).query().where().in(SystemRoleEntity::getFlag,
            Arrays.asList(SystemRoleFlagEnum.SYSTEMED.getCode(), SystemRoleFlagEnum.ACTIVE.getCode())).exists(
            QueryEngine.getEntityQueryWrapper(new SystemRoleGroupRelationEntity()).query().where()
                .equal(SystemRoleGroupRelationEntity::getGroupId, groupId).pour(),
            JoinCondition.of(SystemRoleGroupRelationEntity::getRoleId, SystemRoleEntity::getId)).pour().fetch();
    }

    /**
     * 添加角色到角色组
     *
     * @param groupId
     * @param roleId
     */
    @Override
    public void appendRole(Long groupId, Long roleId) {

        var group = getInstance().queryById(groupId);
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.group.not.exists")
            .throwIt();
        AssertUtil.isTrue(!Objects.equals(group.getFlag(), CommonFlagEnum.ACTIVE.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("permission.group.not.active").throwIt();
        var role = SpringUtil.getBean(ISystemRoleService.class).queryById(roleId);
        AssertUtil.isTrue(role == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.not.exists")
            .throwIt();
        AssertUtil.isTrue(
                !CollUtil.isIn(role.getFlag(), SystemRoleFlagEnum.ACTIVE.getCode(), SystemRoleFlagEnum.SYSTEMED.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("permission.not.active").throwIt();

        SystemRoleGroupRelationEntity relation = new SystemRoleGroupRelationEntity();
        relation.setRoleId(roleId);
        relation.setGroupId(groupId);
        var old = CollUtil.getFirst(relationMapper.select(relation));
        AssertUtil.isTrue(old != null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.group.role.exists")
            .throwIt();
        relationMapper.insert(relation);
    }

    /**
     * 移除角色组下的角色
     *
     * @param groupId
     * @param roleId
     */
    @Override
    public void removeRole(Long groupId, Long roleId) {
        var relation = new SystemRoleGroupRelationEntity();
        relation.setRoleId(roleId);
        relation.setGroupId(groupId);
        var old = CollUtil.getFirst(relationMapper.select(relation));
        AssertUtil.isTrue(old == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("permission.group.role.not.exists").throwIt();
        relationMapper.delete(relation);
    }

    /**
     * 获取禁用的标志
     *
     * @return
     */
    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    /**
     * 获取PageVO构建类
     *
     * @return
     */
    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    /**
     * 新增逻辑处理
     *
     * @param entity
     * @return
     */
    @Override
    protected SystemRoleGroupEntity insertNew(SystemRoleGroupEntity entity) {
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getRoleGroupName())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n("permission.group.name.empty").throwIt();
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        return entity;
    }

    /**
     * 更新逻辑处理
     *
     * @param entity
     * @return
     */
    @Override
    protected List<SystemRoleGroupEntity> updateBy(SystemRoleGroupEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    /**
     * 对比新值与旧值的区别后返回最终的值
     *
     * @param newOne
     * @param oldOne
     * @return
     */
    @Override
    protected SystemRoleGroupEntity compareAndUpdateValue(SystemRoleGroupEntity newOne, SystemRoleGroupEntity oldOne) {
        if (newOne.getFlag() != null && !Objects.equals(newOne.getFlag(), oldOne.getFlag())) {
            AssertUtil.isTrue(
                    !CollUtil.isIn(newOne.getFlag(), CommonFlagEnum.ACTIVE.getCode(), CommonFlagEnum.INACTIVE.getCode()))
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.group.status.invalid").throwIt();
            oldOne.setFlag(newOne.getFlag());
        }
        if (StrUtil.isNotEmpty(newOne.getRoleGroupName())) {
            oldOne.setRoleGroupName(newOne.getRoleGroupName());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        return oldOne;
    }

    /**
     * 获取Mapper对象
     *
     * @return
     */
    @Override
    protected SystemRoleGroupMapper getMapper() {
        return groupMapper;
    }

    /**
     * 创建一个新的实体对象
     *
     * @return
     */
    @Override
    protected SystemRoleGroupEntity newEntity() {
        return new SystemRoleGroupEntity();
    }

    private static final IAuditService<SystemRoleGroupEntity, Long, ?> auditService = new DefaultAuditService<>();

    /**
     * 获取审计服务支持
     *
     * @return
     */
    @Override
    protected Optional<IAuditService<SystemRoleGroupEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
