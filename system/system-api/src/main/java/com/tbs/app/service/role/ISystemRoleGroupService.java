package com.tbs.app.service.role;

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

}
