package com.tbs.app.service.business.role;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.tbs.shared.pojo.entity.SystemRoleEntity;

/**
 * 用户角色关系服务接口
 *
 * @author tongj
 */
public interface IUserRoleRelationService {

    /**
     * 启用角色
     *
     * @param userId
     * @param roleId
     */
    void enableRole(@NotNull Long userId, @NotNull String roleName);

    /**
     * 禁用角色
     *
     * @param userId
     * @param roleId
     */
    void disableRole(@NotNull Long userId, @NotNull String roleName);

    /**
     * 查询用户角色
     *
     * @param userId
     * @return
     */
    List<SystemRoleEntity> queryUserRoles(@NotNull Long userId);
}
