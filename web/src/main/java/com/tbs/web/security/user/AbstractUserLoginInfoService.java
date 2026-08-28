package com.tbs.web.security.user;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.token.ITokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 抽象用户登录信息服务
 *
 * @author abstergo
 */
@Slf4j
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
    protected abstract void writeUserDetails(ITokenModel token, UserDetails userDetails);

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
        UserDetails details = CollUtil.getFirst(instance.getUserDetails(tokenModel.getToken()));
        AssertUtil.isTrue(details != null, "令牌{}，登录用户信息已存在", tokenModel.getToken());
        writeUserDetails(tokenModel, userDetails);
    }

    @Override
    public void removeUserLoginInfo(String token) {
        AssertUtil.hasText(token, "删除登录用户信息令牌不能为空");
        UserDetails details = CollUtil.getFirst(instance.getUserDetails(token));
        if (details == null) {
            log.info("令牌{}，登录用户信息不存在", token);
            return;
        }
        removeUserDetails(token);
    }

    @Override
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        UserDetails details = CollUtil.getFirst(instance.getUserDetails(token.getToken()));
        AssertUtil.notNull(details, "令牌{}，登录用户信息不存在", token);
        AssertUtil.isTrue(!Objects.equals(details.getUsername(), userDetails.getUsername()),
            "令牌{}，非法的用户信息置换，仅能针对同一用户进行用户信息置换。", token);
        writeUserDetails(token, userDetails);
    }

    @Override
    public List<UserDetails> getUserDetails(String token) {
        AssertUtil.hasText(token, "查询登录时用户数据令牌不能为空");
        return Arrays.asList((UserDetails)readUserDetails(token));
    }

}
