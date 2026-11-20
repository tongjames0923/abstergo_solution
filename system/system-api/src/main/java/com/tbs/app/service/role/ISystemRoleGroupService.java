package com.tbs.app.service.role;

import com.tbs.app.vo.RemoteUserInfoVO;
import com.tbs.app.vo.SystemRoleGroupVO;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;

import java.util.List;

/**
 * 角色组服务
 *
 * @author abstergo
 */
public interface ISystemRoleGroupService extends IServiceTemplate<SystemRoleGroupEntity, Long> {
    /**
     * 获取角色组下的权限列表
     *
     * @param groupId
     * @return 权限列表
     */
    List<SystemRoleEntity> getRoleList(Long groupId);

    /**
     * 获取绑定了指定权限的角色组列表
     *
     * @param roleId 权限标识
     * @return 绑定该权限的角色组列表，无绑定时返回空列表
     */
    List<SystemRoleGroupEntity> getGroupListByRoleId(Long roleId);

    /**
     * 获取角色组VO
     *
     * @param groupId
     * @return
     */
    SystemRoleGroupVO getRoleGroupVO(Long groupId);

    /**
     * 添加角色到角色组
     *
     * @param groupId
     * @param roleId
     */
    void appendRole(Long groupId, Long roleId);

    /**
     * 移除角色组下的角色
     *
     * @param groupId
     * @param roleId
     */
    void removeRole(Long groupId, Long roleId);

    /**
     * 添加用户到角色组
     *
     * @param groupId
     * @param userId
     */
    void applyGroup(Long groupId, Long userId);

    /**
     * 移除用户从角色组
     *
     * @param groupId
     * @param userId
     */
    void removeGroup(Long groupId, Long userId);

    /**
     * 获取用户角色组VO列表
     *
     * @param userId
     * @return
     */
    List<SystemRoleGroupVO> getUserRoleGroupVOList(Long userId);

    /**
     * 获取角色组下的用户列表
     *
     * @param groupId
     * @return
     */
    List<RemoteUserInfoVO> getUserList(Long groupId);

    /**
     * 获取角色组绑定的用户标识列表
     * <p>
     * 只返回关联列，供失效与变更通知反查受影响范围使用；不拼装用户名片字段，与
     * {@link #getUserList(Long)} 的适用场景区分：需要展示用户信息用后者，只需要范围用本方法。
     * </p>
     *
     * @param groupId 角色组标识
     * @return 去重后的用户标识列表，组下无用户时返回空列表
     */
    List<Long> boundUserIds(Long groupId);

    /**
     * 判断权限是否存在于任意角色组中
     *
     * @param roleId
     * @param groupId
     * @return
     */
    boolean isRoleInGroupAnyGroup(Long roleId);

    /**
     * 判断用户是否在任意角色组下
     *
     * @param userId
     * @param groupId
     * @return
     */
    boolean isUserInGroupAnyGroup(Long userId);


}
