package com.tbs.app.service.business.impl;

import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.ITokenManager;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.IUserInfoService;
import com.tbs.web.security.model.SimpleTokenModel;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

/**
 * @author abstergo
 */
public class SimpleUserAuthServiceImpl implements IUserAuthService {

    @Resource
    WebAuthProperty authProperty;

    @Resource
    private IPasswordEncrypt passwordEncrypt;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private ITokenManager tokenManager;

    @Override
    public com.tbs.web.security.ITokenModel login(String username, String password) {
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(username);
        password = passwordEncrypt.encrypt(password);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new SimpleAuthException("用户名或密码错误");
        });
        checkUserDetails(details);
        var exp =
            new Date(System.currentTimeMillis() + Duration.ofSeconds(authProperty.getTokenExpireTime()).toMillis());
        com.tbs.web.security.ITokenModel model =
            SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(), authProperty.getTokenSource(),
                authProperty.getTokenFiledName(), details.getUsername(), exp);
        tokenManager.submitToken(model, details);
        return model;
    }

    private void checkUserDetails(UserDetails details) {
        AssertUtil.notNull(details, () -> new SimpleAuthException("未获得有效的用户数据"));
        AssertUtil.isTrue(!details.isEnabled(), () -> new SimpleAuthException("用户被禁用"));

    }

    @Override
    public void authToken(com.tbs.web.security.ITokenModel iTokenModel) throws AuthenticationException {
        AssertUtil.notNull(iTokenModel, () -> new InvalidTokenException("未接收到有效Token"));
        AssertUtil.isTrue(StrUtil.isEmpty(iTokenModel.getToken()), () -> new InvalidTokenException("Token值为空"));
        AssertUtil.isTrue(
            iTokenModel.getExpireTime() != null && iTokenModel.getExpireTime().getTime() < System.currentTimeMillis(),
            () -> new InvalidTokenException("Token已过期"));
        UserDetails details = tokenManager.loadUserInfoByToken(iTokenModel);
        AssertUtil.isTrue(details == null, () -> new InvalidTokenException("Token已失效"));
        checkUserDetails(details);
    }

    @Override
    public boolean logout(com.tbs.web.security.ITokenModel iTokenModel) {
        AssertUtil.notNull(iTokenModel, () -> new InvalidTokenException("未接收到有效Token"));
        tokenManager.removeToken(iTokenModel);
        return true;
    }

}
