package com.tbs.app.remote.auth;

import com.tbs.app.vo.RemoteUserAuthVO;

/**
 * 认证服务远程接口
 *
 * @author tbs
 */
public interface IAuthRemoteService {
    /**
     * 根据令牌获取认证信息
     *
     * @param token 令牌
     * @return 认证信息
     */
    RemoteUserAuthVO getAuthInfoByToken(String token);
}
