package com.tbs.app.remote.user.impl;

import com.tbs.app.remote.user.IUserRemoteService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.vo.RemoteUserInfoVO;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的用户远程服务实现
 *
 * @author abstergo
 */
public class DefaultUserRemoteServiceImpl implements IUserRemoteService {
    @Resource
    private ISystemUserService userService;

    @Override
    public List<RemoteUserInfoVO> getUserInfoByUserName(String userName) {
        return userService.queryUserIdsByUserName(userName).stream().map(id -> {
            var user = userService.queryById(id);
            RemoteUserInfoVO vo = new RemoteUserInfoVO();
            vo.setUserName(user.getUserName());
            vo.setNickName(user.getNickName());
            vo.setAvatar(user.getAvatar());
            vo.setRemark(user.getRemark());
            vo.setFlag(user.getFlag());
            vo.setUserId(user.getId());
            return vo;
        }).collect(Collectors.toList());
    }
}
