package com.tbs.app.service;

import com.tbs.app.pojo.entity.SystemUser;

import java.util.List;

/**
 * 用户服务
 *
 * @author abstergo
 */
public interface ISystemUserService extends ISaveableService<SystemUser, Long>, IDeleteableService<SystemUser, Long> {
    /**
     * 根据用户名密码查询用户
     *
     * @param username
     * @return
     */
    List<SystemUser> queryByUserName(String username);

    /**
     * 保存用户
     *
     * @param systemUser
     * @return
     */
    List<SystemUser> save(SystemUser systemUser);

}
