package com.tbs.app.service.business;

import com.tbs.app.pojo.entity.SystemRoleEntity;
import com.tbs.app.service.IServiceTemplate;
import org.jetbrains.annotations.NotNull;

/**
 * 角色服务
 *
 * @author tongj
 */
public interface ISystemRoleService extends IServiceTemplate<SystemRoleEntity, Long> {
    /**
     * 获取角色
     *
     * @param roleName
     * @return
     */
    SystemRoleEntity getRoleByName(@NotNull String roleName);
}
