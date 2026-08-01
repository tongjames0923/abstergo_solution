package com.tbs.app.service.role;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemRoleEntity;

/**
 * 角色服务
 *
 * @author tongj
 */
public interface ISystemRoleService extends IServiceTemplate<SystemRoleEntity, Long> {

    /**
     * 根据角色查询
     *
     * @param role
     * @return
     */
    SystemRoleEntity queryByRole(String role);

}
