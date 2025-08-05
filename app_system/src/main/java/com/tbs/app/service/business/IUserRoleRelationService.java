package com.tbs.app.service.business;

import com.tbs.app.pojo.entity.SystemRole;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    void enableRole(@NotNull Long userId, @NotNull String roleId);

    /**
     * 禁用角色
     *
     * @param userId
     * @param roleId
     */
    void disableRole(@NotNull Long userId, @NotNull String roleId);

    /**
     * 查询用户角色
     *
     * @param userId
     * @return
     */
    List<SystemRole> queryUserRoles(@NotNull Long userId);
}
