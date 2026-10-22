package com.tbs.app.service.impl.user;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.IUserIdentityService;
import com.tbs.app.service.user.model.UserIdentity;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.app.vo.SystemRoleGroupVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.SystemRoleTypeEnum;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Objects;

/**
 * 默认用户身份聚合服务
 * <p>
 * 按用户标识一次性装配认证所需的身份视图：账号状态位、角色组名称、API 与菜单权限编码、部门归属。 内部依赖全部为同进程的本地服务，不引用任何远程服务，保证调用方的一次往返不会在实现内部被展开。
 * </p>
 * <p>
 * 不缓存装配结果：缓存只允许存在于拥有数据的源服务内部，由该服务在数据变更时自行失效； 本实现每次调用都新建 {@link UserIdentity}，不返回可被多方共享的实例。
 * </p>
 *
 * @author abstergo
 * @see IUserIdentityService 身份聚合契约
 */
public class DefaultUserIdentityService implements IUserIdentityService {

    @Resource
    private ISystemUserService systemUserService;

    @Resource
    private ISystemRoleGroupService userRoleRelationService;

    @Resource
    private IDepartmentUserRelationService departmentUserRelationService;

    @Override
    public UserIdentity loadUserIdentity(Long userId) {
        AssertUtil.notNull(userId, "装载用户身份的用户标识不能为空");
        SystemUserEntity user = systemUserService.queryById(userId);
        if (user == null) {
            return null;
        }
        UserIdentity identity = new UserIdentity();
        identity.setUserId(user.getId());
        identity.setUsername(user.getUserName());
        applyStatus(identity, user.getFlag());
        applyAuthorities(identity, userRoleRelationService.getUserRoleGroupVOList(userId));
        identity.setDepartments(
            DepartmentInfoVO.convertToDepartmentVOs(departmentUserRelationService.queryUserDepartments(userId)));
        return identity;
    }

    /**
     * 按用户状态位映射账号可用性
     * <p>
     * 与登录路径的 {@code DefaultUserInfoService#loadUserInfoByUniqueKey} 保持同一套语义， 避免两条路径对「禁用 / 锁定」的判定出现分歧。
     * </p>
     *
     * @param identity 待填充的身份视图，不能为空
     * @param flag     用户状态位，可以为空
     */
    private void applyStatus(UserIdentity identity, Integer flag) {
        if (Objects.equals(SystemUserStatusEnum.ACTIVE.getCode(), flag)) {
            identity.setAccountNonLocked(true);
            identity.setEnabled(true);
        } else if (Objects.equals(SystemUserStatusEnum.LOCKED.getCode(), flag)) {
            identity.setAccountNonLocked(false);
            identity.setEnabled(true);
        } else if (Objects.equals(SystemUserStatusEnum.INACTIVE.getCode(), flag)) {
            identity.setAccountNonLocked(true);
            identity.setEnabled(false);
        }
    }

    /**
     * 展开角色组，产出扁平的角色组名称与权限编码
     * <p>
     * 只保留鉴权需要用到的编码，权限项名称、描述、数据范围等展示字段不进入身份视图。
     * </p>
     *
     * @param identity 待填充的身份视图，不能为空
     * @param roles    用户关联的角色组，可以为空
     */
    private void applyAuthorities(UserIdentity identity, List<SystemRoleGroupVO> roles) {
        if (CollUtil.isEmpty(roles)) {
            return;
        }
        for (SystemRoleGroupVO role : roles) {
            if (role == null || role.getGroup() == null) {
                continue;
            }
            if (StrUtil.isNotEmpty(role.getGroup().getRoleGroupName())) {
                identity.getRoleGroupNames().add(role.getGroup().getRoleGroupName());
            }
            if (CollUtil.isEmpty(role.getPermissions())) {
                continue;
            }
            for (SystemRoleEntity permission : role.getPermissions()) {
                if (permission == null || StrUtil.isEmpty(permission.getRole())) {
                    continue;
                }
                if (Objects.equals(SystemRoleTypeEnum.API_ROLE.getCode(), permission.getType())) {
                    identity.getApiPermissions().add(permission.getRole());
                } else if (Objects.equals(SystemRoleTypeEnum.MENU_ROLE.getCode(), permission.getType())) {
                    identity.getMenuPermissions().add(permission.getRole());
                }
            }
        }
    }
}
