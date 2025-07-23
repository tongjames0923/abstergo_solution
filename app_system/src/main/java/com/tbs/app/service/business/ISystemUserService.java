package com.tbs.app.service.business;

import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.IServiceTemplate;

import java.util.List;

/**
 * 用户服务
 *
 * @author abstergo
 */
public interface ISystemUserService extends IServiceTemplate<SystemUser, Long> {
    /**
     * 根据用户名密码查询用户
     *
     * @param username
     * @return
     */
    List<SystemUser> queryByUserName(String username);

}
