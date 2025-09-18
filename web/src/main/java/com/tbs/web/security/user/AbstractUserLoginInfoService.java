package com.tbs.web.security.user;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class AbstractUserLoginInfoService implements IUserLoginInfoService {

    /**
     * 读取用户信息
     *
     * @param token 登录令牌
     * @return 用户信息
     */
    protected abstract UserDetails readUserDetails(String token);

    /**
     * 写入用户信息
     *
     * @param token       登录令牌
     * @param userDetails 用户信息
     */
    protected abstract void writeUserDetails(String token, UserDetails userDetails);

    /**
     * 删除用户信息
     *
     * @param token 登录令牌
     */
    protected abstract void removeUserDetails(String token);

    protected static AbstractUserLoginInfoService instance;

    public AbstractUserLoginInfoService() {
        instance = this;
    }

    @Override
    public void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel) {
        AssertUtil.notNull(userDetails, "登录用户信息不能为空");
        AssertUtil.notNull(tokenModel, "登录令牌不能为空");
        AssertUtil.hasText(tokenModel.getToken(), "登录令牌内容不能为空");
        AssertUtil.hasText(userDetails.getUsername(), "登录用户名不能为空");
        UserDetails details = instance.getUserDetails(tokenModel.getToken());
        AssertUtil.isTrue(details != null, "令牌{}，登录用户信息已存在", tokenModel.getToken());
        writeUserDetails(tokenModel.getToken(), userDetails);
    }

    @Override
    public void removeUserLoginInfo(String token) {
        AssertUtil.hasText(token, "删除登录用户信息令牌不能为空");
        UserDetails details = instance.getUserDetails(token);
        AssertUtil.notNull(details, "令牌{}，登录用户信息不存在", token);
        removeUserDetails(token);
    }

    @Override
    public void replaceUserDetails(UserDetails userDetails, String token) {

        writeUserDetails(token, userDetails);
    }

    @Override
    public UserDetails getUserDetails(String token) {
        AssertUtil.hasText(token, "查询登录时用户数据令牌不能为空");
        return readUserDetails(token);
    }
}
