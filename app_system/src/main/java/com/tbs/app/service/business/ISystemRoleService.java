package com.tbs.app.service.business;

import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.service.IServiceTemplate;
import org.jetbrains.annotations.NotNull;

/**
 * 角色服务
 *
 * @author tongj
 */
public interface ISystemRoleService extends IServiceTemplate<SystemRole, Long> {
    /**
     * 获取角色
     *
     * @param roleName
     * @return
     */
    SystemRole getRoleByName(@NotNull String roleName);
}
