package com.tbs.web.security.user;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.token.ITokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Objects;

/**
 * 抽象用户登录信息服务
 * <p>
 * 模板实现 {@link IUserLoginInfoService}：定义「令牌 ↔ 用户详情」增删改查的通用校验与流程，
 * 具体存储（库表/缓存）由子类实现 {@link #readUserDetails}/{@link #writeUserDetails}/{@link #removeUserDetails}。
 * 通用流程一律直接调用 {@link #readUserDetails} 读取真源，不经过子类的缓存增强，
 * 避免「写前校验」被缓存中的陈旧或空值误导。
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

    @Override
    public void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel) {
        AssertUtil.notNull(userDetails, "登录用户信息不能为空");
        AssertUtil.notNull(tokenModel, "登录令牌不能为空");
        AssertUtil.hasText(tokenModel.getToken(), "登录令牌内容不能为空");
        AssertUtil.hasText(userDetails.getUsername(), "登录用户名不能为空");
        // 防重复写入：同令牌已存在登录信息则拒绝，避免覆盖旧的会话关联（直接读真源，不走缓存）
        UserDetails details = readUserDetails(tokenModel.getToken());
        AssertUtil.isTrue(details != null, "令牌{}，登录用户信息已存在", tokenModel.getToken());
        writeUserDetails(tokenModel, userDetails);
    }

    @Override
    public void removeUserLoginInfo(String token) {
        AssertUtil.hasText(token, "删除登录用户信息令牌不能为空");
        UserDetails details = readUserDetails(token);
        if (details == null) {
            log.info("令牌{}，登录用户信息不存在", token);
            return;
        }
        removeUserDetails(token);
    }

    @Override
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        UserDetails details = readUserDetails(token.getToken());
        AssertUtil.notNull(details, "令牌{}，登录用户信息不存在", token);
        // 安全约束：仅允许同一用户置换详情，防止越权替换他人会话信息
        AssertUtil.isTrue(!Objects.equals(details.getUsername(), userDetails.getUsername()),
            "令牌{}，非法的用户信息置换，仅能针对同一用户进行用户信息置换。", token);
        writeUserDetails(token, userDetails);
    }

    /**
     * 根据令牌获取用户详情
     * <p>
     * 返回值恒为 0 或 1 个元素的列表：无匹配时为空集合，有匹配时为单元素集合，不会出现 null 元素。
     * 空集合是合法的缓存值，用于抵御无效令牌的重复回源；因此统一经 {@link CollUtil#singletonList}
     * 包装为可序列化的 {@link java.util.ArrayList}，避免 {@code Collections.emptyList()} 等
     * JDK 内部集合实现写入缓存后无法反序列化。
     * </p>
     *
     * @param token 令牌串，不能为空
     * @return 令牌对应的用户详情集合，无匹配时返回空集合
     */
    @Override
    public List<UserDetails> getUserDetails(String token) {
        AssertUtil.hasText(token, "查询登录时用户数据令牌不能为空");
        return CollUtil.singletonList(readUserDetails(token));
    }

}
