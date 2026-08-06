package com.tbs.app.service.impl.role;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.vo.RemoteUserInfoVO;
import com.tbs.app.vo.SystemRoleGroupVO;
import com.tbs.cache.expire.annotation.ExpireOn;
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
import com.tbs.shared.mapper.view.RoleGroupUserRelationMapper;
import com.tbs.shared.pojo.entity.*;
import com.tbs.sql.dsl.clause.JoinCondition;
import com.tbs.sql.service.QueryEngine;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private RoleGroupUserRelationMapper roleGroupUserRelationMapper;

    @Resource
    private AppSettingProperty property;

    /**
     * 获取角色组下的权限列表
     *
     * @param groupId
     * @return 权限列表
     */
    @Override
    @Cacheable(value = "roleGroup", key = "#groupId")
    @ExpireOn(cacheNames = "roleGroup", value = 10, timeUnit = TimeUnit.MINUTES)
    public List<SystemRoleEntity> getRoleList(Long groupId) {
        return QueryEngine.getEntityQueryWrapper(new SystemRoleEntity()).query().where().in(SystemRoleEntity::getFlag,
            Arrays.asList(SystemRoleFlagEnum.SYSTEMED.getCode(), SystemRoleFlagEnum.ACTIVE.getCode())).exists(
            QueryEngine.getEntityQueryWrapper(new SystemRoleGroupRelationEntity()).query().where()
                .equal(SystemRoleGroupRelationEntity::getGroupId, groupId).pour(),
            JoinCondition.of(SystemRoleGroupRelationEntity::getRoleId, SystemRoleEntity::getId)).pour().fetch();
    }

    /**
     * 获取角色组VO
     *
     * @param groupId
     * @return
     */
    @Override
    public SystemRoleGroupVO getRoleGroupVO(Long groupId) {
        var group = getInstance().queryById(groupId);
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.group.not.exists")
            .throwIt();
        return new SystemRoleGroupVO(group, ((ISystemRoleGroupService)getInstance()).getRoleList(groupId));
    }

    /**
     * 添加角色到角色组
     *
     * @param groupId
     * @param roleId
     */
    @Override
    @CacheEvict(value = "roleGroup", key = "#groupId")
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
    @CacheEvict(value = "roleGroup", key = "#groupId")
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
     * 添加用户到角色组
     *
     * @param groupId
     * @param userId
     */
    @Override
    public void applyGroup(Long groupId, Long userId) {
        var group = getInstance().queryById(groupId);
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("permission.group.not.exists")
            .throwIt();
        AssertUtil.isTrue(!Objects.equals(group.getFlag(), CommonFlagEnum.ACTIVE.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("permission.group.not.active").throwIt();
        var user = SpringUtil.getBean(ISystemUserService.class).queryById(userId);
        AssertUtil.isTrue(user == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("user.not.exists").throwIt();

        SystemRoleGroupUserRelationEntity relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        relation.setRoleGroupId(groupId);
        var ls = roleGroupUserRelationMapper.select(relation);
        AssertUtil.isTrue(CollUtil.isNotEmpty(ls)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("permission.group.user.exists").throwIt();
        roleGroupUserRelationMapper.insert(relation);
        AuthUtil.getInstance().notifyUserAuthStatusChanged(user.getUserName());
    }

    /**
     * 移除用户从角色组
     *
     * @param groupId
     * @param userId
     */
    @Override
    public void removeGroup(Long groupId, Long userId) {

        SystemUserEntity user = SpringUtil.getBean(ISystemUserService.class).queryById(userId);
        AssertUtil.isTrue(user == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("user.not.exists").throwIt();
        var relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        relation.setRoleGroupId(groupId);
        var ls = roleGroupUserRelationMapper.select(relation);
        AssertUtil.isTrue(CollUtil.isEmpty(ls)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("permission.group.user.not.exists").throwIt();
        roleGroupUserRelationMapper.delete(relation);
        AuthUtil.getInstance().notifyUserAuthStatusChanged(user.getUserName());
    }

    /**
     * 获取用户角色组VO列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<SystemRoleGroupVO> getUserRoleGroupVOList(Long userId) {
        var relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        return roleGroupUserRelationMapper.select(relation).stream().map(res -> {
            var group = getInstance().queryById(res.getRoleGroupId());
            if (group == null) {
                return null;
            }
            return new SystemRoleGroupVO(group,
                ((ISystemRoleGroupService)getInstance()).getRoleList(res.getRoleGroupId()));
        }).filter(val -> val.getGroup() != null).collect(Collectors.toList());

    }

    /**
     * 获取角色组下的用户列表
     *
     * @param groupId
     * @return
     */
    @Override
    public List<RemoteUserInfoVO> getUserList(Long groupId) {
        var relations = new SystemRoleGroupUserRelationEntity();
        relations.setRoleGroupId(groupId);

        return roleGroupUserRelationMapper.select(relations).stream().map(res -> {
            var user = SpringUtil.getBean(ISystemUserService.class).queryById(res.getUserId());
            if (user == null) {
                return null;
            }
            return new RemoteUserInfoVO(user.getUserName(), user.getNickName(), user.getAvatar(), user.getRemark(),
                user.getFlag(), user.getId());

        }).filter(val -> val != null).collect(Collectors.toList());
    }

    /**
     * 判断权限是否存在于任意角色组中
     *
     * @param roleId
     * @return
     */
    @Override
    public boolean isRoleInGroupAnyGroup(Long roleId) {
        SystemRoleGroupRelationEntity relation = new SystemRoleGroupRelationEntity();
        relation.setRoleId(roleId);
        return CollUtil.isNotEmpty(relationMapper.select(relation));
    }

    /**
     * 判断用户是否在任意角色组下
     *
     * @param userId
     * @return
     */
    @Override
    public boolean isUserInGroupAnyGroup(Long userId) {
        SystemRoleGroupUserRelationEntity relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        return CollUtil.isNotEmpty(roleGroupUserRelationMapper.select(relation));
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
