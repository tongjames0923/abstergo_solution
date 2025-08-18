package com.tbs.app.service.business.user;

import java.util.List;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemUserEntity;

/**
 * 用户服务
 *
 * @author abstergo
 */
public interface ISystemUserService extends IServiceTemplate<SystemUserEntity, Long> {
    /**
     * 根据用户名密码查询用户
     *
     * @param username
     * @return
     */
    List<SystemUserEntity> queryByUserName(String username);

}
