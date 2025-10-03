package com.tbs.app.service.user;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemUserEntity;

import java.util.List;

/**
 * 用户服务
 *
 * @author abstergo
 */
public interface ISystemUserService extends IServiceTemplate<SystemUserEntity, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    List<SystemUserEntity> queryByUserName(String username);

    /**
     * 根据用户名查询用户ID
     *
     * @param username
     * @return
     */
    List<Long> queryUserIdsByUserName(String username);
}
