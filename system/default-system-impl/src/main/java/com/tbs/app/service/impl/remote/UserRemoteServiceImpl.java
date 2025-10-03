package com.tbs.app.service.impl.remote;

import com.tbs.app.form.IRemoteRequestForm;
import com.tbs.app.service.remote.IUserRemoteService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.Collection;

@DubboService
public class UserRemoteServiceImpl implements IUserRemoteService {
    @Resource
    ISystemUserService userService;

    @Override
    public SystemUserEntity queryUserById(IRemoteRequestForm<Long> id) {

        return userService.queryById(id.getForm());
    }

    @Override
    public Collection<SystemUserEntity> queryUserByUsername(IRemoteRequestForm<String> username) {
        return userService.queryByUserName(username.getForm());
    }

}
