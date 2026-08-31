package com.tbs.app.remote.auth.impl;

import com.tbs.app.remote.auth.IAuthRemoteService;
import com.tbs.app.vo.RemoteUserAuthVO;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.user.IUserLoginInfoService;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * 默认的远程认证服务实现
 *
 * @author abstergo
 */
public class DefaultAuthRemoteServiceImpl implements IAuthRemoteService {

    @Resource
    ITokenIssuanceCenter center;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Override
    public RemoteUserAuthVO getAuthInfoByToken(String token) {
        var authInfo = new RemoteUserAuthVO();
        var tk = center.getTokenByToken(token);
        if (tk == null) {
            return null;
        }
        authInfo.setStatusCode(tk.getStatus().getCode());
        authInfo.setExpireTime(tk.getExpireTime());
        authInfo.setToken(tk.getToken());
        authInfo.setCreateTime(tk.getCreateTime());
        authInfo.setTokenTypeCode(tk.getTokenType().getCode());

        var userDetails = CollUtil.getFirst(loginInfoService.getUserDetails(token));
        if (userDetails == null) {
            return authInfo;
        }
        authInfo.setUserName(userDetails.getUsername());
        authInfo.setRoles(
            userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return authInfo;

    }
}
