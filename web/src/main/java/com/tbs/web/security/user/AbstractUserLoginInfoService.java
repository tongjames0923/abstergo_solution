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
 * <p>
 * 模板实现 {@link IUserLoginInfoService}：定义「令牌 ↔ 用户详情」增删改查的通用校验与流程，
 * 具体存储（库表/缓存）由子类实现 {@link #readUserDetails}/{@link #writeUserDetails}/{@link #removeUserDetails}。
 * 通过静态 {@code instance} 引用自身，使通用方法可调用被缓存实现覆盖的 {@code getUserDetails}。
 * </p>
 *
 * @author abstergo
 * @see IUserLoginInfoService 登录信息服务契约
 */
@Slf4j
public abstract class AbstractUserLoginInfoService implements IUserLoginInfoService {

    /**
     * 从存储读取用户详情
     *
     * @param token 登录令牌，不能为空
     * @return 用户详情；不存在时返回 {@code null}
     */
    protected abstract UserDetails readUserDetails(String token);

    /**
     * 写入用户详情到存储
     *
     * @param token       登录令牌，不能为空
     * @param userDetails 用户详情，不能为空
     */
    protected abstract void writeUserDetails(ITokenModel token, UserDetails userDetails);

    /**
     * 从存储删除用户详情
     *
     * @param token 登录令牌，不能为空
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
        // 防重复写入：同令牌已存在登录信息则拒绝，避免覆盖旧的会话关联
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
        // 安全约束：仅允许同一用户置换详情，防止越权替换他人会话信息
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
