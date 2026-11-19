package com.tbs.app.service.impl.role;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.role.IPermissionChangeListener;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.vo.PermissionChangeContext;
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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tbs.app.service.impl.CacheNamesConstants.ROLE_GROUP;
import static com.tbs.app.service.impl.CacheNamesConstants.ROLE_GROUP_USER_ID;

/**
 * 角色组服务实现类
 * <p>
 * 缓存失效在本类统一收归为<b>编程式一处</b>，不与注解双轨并用：主失效（组权限列表按 groupId、 用户身份快照按 userId）由 {@link #evictCache(String, Object)}
 * 在收口方法内完成；组权限缓存被清除时， 它派生出的用户身份快照由 {@code PermissionUserSnapshotEvictLink} 向缓存框架声明的关联失效连带清除， 本类不在各写入点重复展开。
 * </p>
 * <p>
 * 不用 {@code @CacheEvict} 注解的理由是<b>时机</b>：注解在方法返回后才执行，而权限变更事件在方法体内派发， 挂在注解上会让监听器在派发瞬间读到旧权限；同处方法体内，失效与派发的顺序才是确定的。
 * </p>
 * <p>
 * 新增权限组写入点时必须经 {@link #invalidateGroupCache(Long)} 或 {@link #onUserGroupChanged} 收口， 不要另写一处 evict；变更事件需显式调用
 * {@link #firePermissionChanged}，因为它要方法上下文才能确定 动作方向与操作人。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class SimpleRoleGroupServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRoleGroupEntity, Long, SystemRoleGroupMapper>
    implements ISystemRoleGroupService {

    private static final IAuditService<SystemRoleGroupRelationEntity, Long, Long> audit =
        new DefaultAuditService<SystemRoleGroupRelationEntity, Long>();
    private static final IAuditService<SystemRoleGroupEntity, Long, ?> auditService = new DefaultAuditService<>();
    @Resource
    private SystemRoleGroupMapper groupMapper;
    @Resource
    private SystemRoleGroupRelationMapper relationMapper;
    @Resource
    private RoleGroupUserRelationMapper roleGroupUserRelationMapper;
    @Resource
    private AppSettingProperty property;
    @Resource
    private CacheManager cacheManager;
    /**
     * 权限变更监听器，容器内无实现时派发即为空操作
     */
    @Autowired
    private ObjectProvider<IPermissionChangeListener> permissionChangeListeners;

    /**
     * 获取角色组下的权限列表
     *
     * @param groupId
     * @return 权限列表
     */
    @Override
    @Cacheable(value = ROLE_GROUP, key = "#groupId")
    @ExpireOn(cacheNames = ROLE_GROUP, value = 10, timeUnit = TimeUnit.MINUTES)
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
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NOT_EXISTS).throwIt();
        return new SystemRoleGroupVO(group, ((ISystemRoleGroupService)getInstance()).getRoleList(groupId));
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
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NOT_EXISTS).throwIt();
        AssertUtil.isTrue(!Objects.equals(group.getFlag(), CommonFlagEnum.ACTIVE.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NOT_ACTIVE).throwIt();
        var role = SpringUtil.getBean(ISystemRoleService.class).queryById(roleId);
        AssertUtil.isTrue(role == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_NOT_EXISTS).throwIt();
        AssertUtil.isTrue(
                !CollUtil.isIn(role.getFlag(), SystemRoleFlagEnum.ACTIVE.getCode(), SystemRoleFlagEnum.SYSTEMED.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_NOT_ACTIVE).throwIt();

        // 绕过逻辑删除拦截器查询，避免已删除记录无法被重新激活
        var existing = relationMapper.selectByRoleIdAndGroupIdIncludeDeleted(roleId, groupId);
        if (existing != null) {
            existing.setFlag(CommonFlagEnum.ACTIVE.getCode());
            audit.onUpdate(existing);
            relationMapper.updateByPrimaryKey(existing);
        } else {
            var relation = new SystemRoleGroupRelationEntity();
            relation.setFlag(CommonFlagEnum.ACTIVE.getCode());
            relation.setRoleId(roleId);
            relation.setGroupId(groupId);
            audit.onCreate(relation);
            relationMapper.insert(relation);
        }
        onGroupPermissionChanged(IPermissionChangeListener.ChangeType.ROLE_APPENDED, groupId, List.of(roleId));
    }

    /**
     * 移除角色组下的角色
     *
     * @param groupId
     * @param roleId
     */
    @Override
    public void removeRole(Long groupId, Long roleId) {
        // 绕过逻辑删除拦截器查询，确保能检测到已删除的记录并给出准确提示
        var old = relationMapper.selectByRoleIdAndGroupIdIncludeDeleted(roleId, groupId);
        AssertUtil.isTrue(old == null || Objects.equals(old.getFlag(), CommonFlagEnum.DELETED.getCode()))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_ROLE_NOT_EXISTS).throwIt();
        old.setFlag(CommonFlagEnum.DELETED.getCode());
        audit.onDelete(old);
        relationMapper.updateByPrimaryKey(old);
        onGroupPermissionChanged(IPermissionChangeListener.ChangeType.ROLE_REMOVED, groupId, List.of(roleId));
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
        AssertUtil.isTrue(group == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NOT_EXISTS).throwIt();
        AssertUtil.isTrue(!Objects.equals(group.getFlag(), CommonFlagEnum.ACTIVE.getCode()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NOT_ACTIVE).throwIt();
        var user = SpringUtil.getBean(ISystemUserService.class).queryById(userId);
        AssertUtil.isTrue(user == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_EXISTS).throwIt();

        SystemRoleGroupUserRelationEntity relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        relation.setRoleGroupId(groupId);
        var ls = roleGroupUserRelationMapper.select(relation);
        AssertUtil.isTrue(CollUtil.isNotEmpty(ls)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_USER_EXISTS).throwIt();
        roleGroupUserRelationMapper.insert(relation);
        onUserGroupChanged(IPermissionChangeListener.ChangeType.USER_APPENDED, groupId, userId);
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
        AssertUtil.isTrue(user == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_EXISTS).throwIt();
        var relation = new SystemRoleGroupUserRelationEntity();
        relation.setUserId(userId);
        relation.setRoleGroupId(groupId);
        var ls = roleGroupUserRelationMapper.select(relation);
        AssertUtil.isTrue(CollUtil.isEmpty(ls)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_USER_NOT_EXISTS).throwIt();
        roleGroupUserRelationMapper.delete(relation);
        onUserGroupChanged(IPermissionChangeListener.ChangeType.USER_REMOVED, groupId, userId);
    }

    /**
     * 获取用户角色组VO列表
     * <p>
     * 缓存的是「权限组 + 权限项」组装快照，以 userId 为键；其失效由缓存框架的关联失效承担 （组权限缓存被清除时连带清除本缓存），本类写入点不再逐键处理。
     * </p>
     *
     * @param userId
     * @return
     */
    @Cacheable(value = ROLE_GROUP_USER_ID, key = "#userId")
    @ExpireOn(cacheNames = ROLE_GROUP_USER_ID, value = 10, timeUnit = TimeUnit.MINUTES)
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
        }).filter(Objects::nonNull).filter(val -> val.getGroup() != null).collect(Collectors.toList());

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
        return CollUtil.isNotEmpty(Arrays.asList(
            relationMapper.query().where().equal(SystemRoleGroupRelationEntity::getRoleId, roleId).pour().fetchOne()));
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
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_NAME_EMPTY).throwIt();
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        return entity;
    }

    /**
     * 保存权限组（新增或更新）
     * <p>
     * 组的名称与状态直接进入用户身份快照，变更后必须让快照重建；组内权限列表同样依赖组状态 （停用的组不应再产出权限），故一并失效。
     * </p>
     *
     * @param entity 权限组实体
     * @return 保存结果
     */
    @Override
    public List<SystemRoleGroupEntity> save(SystemRoleGroupEntity entity) {
        // 新建与更新的分界只能在落库前判断：插入会把雪花 id 回填到入参实体上
        var created = entity.getId() == null;
        var result = super.save(entity);
        if (entity.getId() == null) {
            return result;
        }
        var userIds = invalidateGroupCache(entity.getId());
        firePermissionChanged(created ? IPermissionChangeListener.ChangeType.GROUP_CREATED
                : IPermissionChangeListener.ChangeType.GROUP_UPDATED, entity.getId(),
            getInstance().queryById(entity.getId()), userIds, null);
        return result;
    }

    /**
     * 删除权限组（逻辑删除）
     * <p>
     * 删除组不会清理组与用户的关联行，若只删组不清缓存，已登录用户在本缓存 TTL 内仍能拿到 该组的权限。组与绑定用户须在删除前取到：删除后按逻辑删除规则已查不到组本身。
     * </p>
     *
     * @param id 权限组标识
     * @return 被删除的权限组
     */
    @Override
    public SystemRoleGroupEntity deleteById(Long id) {
        // 组与关联行即将不可读，先取组信息与绑定用户，再删、再失效与派发
        var group = getInstance().queryById(id);
        var userIds = boundUserIds(id);
        var deleted = super.deleteById(id);
        evictCache(ROLE_GROUP, id);
        firePermissionChanged(IPermissionChangeListener.ChangeType.GROUP_DELETED, id, group, userIds, null);
        return deleted;
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
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_GROUP_STATUS_INVALID).throwIt();
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

    /**
     * 权限组内权限项增删的收口动作：先失效缓存，再派发变更事件
     *
     * @param type    变更类型（区分新增与移除）
     * @param groupId 权限组标识
     * @param roleIds 本次变更涉及的权限项标识
     */
    private void onGroupPermissionChanged(IPermissionChangeListener.ChangeType type, Long groupId, List<Long> roleIds) {
        var userIds = invalidateGroupCache(groupId);
        firePermissionChanged(type, groupId, getInstance().queryById(groupId), userIds, roleIds);
    }

    /**
     * 用户与权限组归属关系变更的收口动作
     * <p>
     * 组内权限未变，不触碰组权限缓存；受影响范围只有这一个用户，直接按 userId 清除其身份快照。 权限变更采用静默生效：下一个请求重新认证即拿到新权限，不再吊销令牌强制下线。
     * </p>
     *
     * @param type    变更类型（区分加入与移出）
     * @param groupId 权限组标识
     * @param userId  受影响用户标识
     */
    private void onUserGroupChanged(IPermissionChangeListener.ChangeType type, Long groupId, Long userId) {
        evictCache(ROLE_GROUP_USER_ID, userId);
        firePermissionChanged(type, groupId, getInstance().queryById(groupId), List.of(userId), null);
    }

    /**
     * 清除组权限列表缓存，并返回受影响的用户列表
     * <p>
     * 只清组权限一层：用户身份快照由框架关联失效连带清除（见类注释）。清除必须发生在本方法内而不是 依赖方法上的注解：注解在方法返回后才执行，而紧随其后的事件派发需要看到「失效已发生」。
     * </p>
     *
     * @param groupId 权限组标识
     * @return 受影响的用户标识列表，供事件派发使用
     */
    private List<Long> invalidateGroupCache(Long groupId) {
        evictCache(ROLE_GROUP, groupId);
        return boundUserIds(groupId);
    }

    /**
     * 查询权限组绑定的用户标识
     * <p>
     * 只取关联列，一次索引查询。不复用 {@link #getUserList(Long)}：后者为拼装用户名片会对每个绑定 用户再打一条主键查询，失效与通知用不到那些字段。本方法同时供关联失效声明调用，
     * 避免两处各写一份反查逻辑。
     * </p>
     *
     * @param groupId 权限组标识
     * @return 去重后的用户标识列表
     */
    @Override
    public List<Long> boundUserIds(Long groupId) {
        var query = new SystemRoleGroupUserRelationEntity();
        query.setRoleGroupId(groupId);
        return roleGroupUserRelationMapper.select(query).stream().map(SystemRoleGroupUserRelationEntity::getUserId)
            .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * 按缓存名称与键清除一条缓存
     * <p>
     * 本类的失效一律走 {@link CacheManager} 显式清除，不用 {@code @CacheEvict} 注解：注解在方法返回后才 执行，无法为方法体内的后续逻辑（关联失效与变更事件）提供顺序保证 （同
     * {@code DefaultUserLoginInfoService#evictSessionCache}）。 同一缓存在本类内不混用两种失效机制；多级缓存的 evict 会逐层清除 L1（Caffeine）与
     * L2（Redis）， 无需另行处理本地层级残留。
     * </p>
     *
     * @param cacheName 缓存名称，取自 {@link com.tbs.app.service.impl.CacheNamesConstants}
     * @param key       缓存键
     */
    private void evictCache(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("缓存{}不存在，清除动作跳过，key:{}", cacheName, key);
            return;
        }
        cache.evict(key);
    }

    /**
     * 派发权限变更事件
     * <p>
     * 必须在缓存清除之后调用，保证监听器回读到的已是最新权限。单个监听器抛出的异常只记录不外抛， 一路监听器失败不影响其他监听器与业务主流程（同 {@code AbstractAuthManager} 对
     * {@code ITokenCleanner} 的容错处理）。派发为同步同线程，监听器内不得执行耗时操作。
     * </p>
     *
     * @param type    变更类型
     * @param groupId 权限组标识
     * @param group   权限组实体，组已删除时传删除前取到的实例
     * @param userIds 受影响用户标识
     * @param roleIds 涉及的权限项标识，可以为空
     */
    private void firePermissionChanged(IPermissionChangeListener.ChangeType type, Long groupId,
        SystemRoleGroupEntity group, List<Long> userIds, List<Long> roleIds) {
        var context = new PermissionChangeContext();
        context.setType(type);
        context.setGroupId(groupId);
        context.setGroupName(group == null ? null : group.getRoleGroupName());
        context.setUserIds(userIds);
        context.setRoleIds(roleIds);
        context.setOperateBy(currentOperator());
        permissionChangeListeners.forEach(listener -> {
            try {
                listener.onPermissionChanged(context);
            } catch (Exception e) {
                log.error("权限变更监听器[{}]处理失败，权限组:{} 变更类型:{}", listener.getName(), groupId, type, e);
            }
        });
    }

    /**
     * 获取当前操作人用户名
     * <p>
     * 权限变更也可能由启动任务或定时任务触发，此时没有登录会话，返回 {@code null} 而不是抛未登录异常。
     * </p>
     *
     * @return 操作人用户名，无登录会话时返回 {@code null}
     */
    private String currentOperator() {
        var authUtil = AuthUtil.getInstance();
        if (authUtil == null || !authUtil.isUserLoggedIn()) {
            return null;
        }
        return authUtil.getCurrentUser().getUsername();
    }

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
