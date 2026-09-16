package com.tbs.web.security.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户信息服务, 用于获取用户信息
 * <p>
 * 独立于登录态，按唯一标识（用户名/手机号等）加载标准用户详情，供认证与授权链路复用。
 * </p>
 *
 * @author tongj
 * @see UserDetails Spring Security 用户详情契约
 */
public interface IUserInfoService {

    /**
     * 通过唯一标识获取标准用户信息
     * <p>
     * 该加载过程不依赖当前登录态，仅按标识查询用户本体；被禁用/锁定等状态由调用方或
     * {@link IUserValidator} 另行校验。
     * </p>
     *
     * @param uniqueKey 用户唯一标识（如用户名），不能为空
     * @return 标准用户详情；标识不存在时返回 {@code null}
     */
    UserDetails loadUserInfoByUniqueKey( String uniqueKey);

    /**
     * 通过外部身份标识获取标准用户信息
     * <p>
     * 按外部身份类型（如企业微信）与外部标识加载绑定的系统用户，供第三方单点登录换取本系统用户详情。
     * 默认未实现（返回 {@code null}），由业务模块按需覆写；未绑定场景由调用方断言提示。
     * 仅新增方法，不改变 {@link #loadUserInfoByUniqueKey(String)} 的既有契约。
     * </p>
     *
     * @param identityType 外部身份类型（如 wecom），不能为空
     * @param externalId   外部身份标识（如企微 userid），不能为空
     * @return 绑定的标准用户详情；未绑定或未实现时返回 {@code null}
     */
    default UserDetails loadUserInfoByExternalIdentity( String identityType,  String externalId) {
        return null;
    }
}
