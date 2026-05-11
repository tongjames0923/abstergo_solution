package com.tbs.app.remote.user;

import com.tbs.app.vo.RemoteUserInfoVO;

import java.util.List;

/**
 * 远程用户服务接口
 *
 * @author abstergo
 */
public interface IUserRemoteService {

    /**
     * 获取用户信息列表
     *
     * @param userName 用户名
     * @return 用户信息列表
     */
    List<RemoteUserInfoVO> getUserInfoByUserName(String userName);
}
