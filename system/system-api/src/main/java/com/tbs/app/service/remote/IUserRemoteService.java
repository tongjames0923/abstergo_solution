package com.tbs.app.service.remote;

import com.tbs.app.form.IRemoteRequestForm;
import com.tbs.shared.pojo.entity.SystemUserEntity;

import java.util.Collection;

public interface IUserRemoteService {
    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    SystemUserEntity queryUserById(IRemoteRequestForm<Long> id);

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    Collection<SystemUserEntity> queryUserByUsername(IRemoteRequestForm<String> username);

}
