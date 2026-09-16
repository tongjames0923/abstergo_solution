package com.tbs.app.service.identity;

import org.springframework.core.Ordered;

/**
 * 外部身份自动关联策略 SPI
 * <p>
 * 按第三方用户信息自动搜索并匹配系统用户；Spring 容器收集全部实现并按
 * {@link Ordered} 顺序执行，首个命中即生效。新增匹配策略（如邮箱/手机号匹配）
 * 只需新增实现类，不改动绑定管理服务与登录链路。
 * </p>
 *
 * @author abstergo
 */
public interface IExternalIdentityResolver extends Ordered {

    /**
     * 当前策略是否适用于指定外部身份类型
     *
     * @param identityType 外部身份类型（如 wecom）
     * @return 适用返回 {@code true}
     */
    boolean supports(String identityType);

    /**
     * 按第三方用户信息匹配系统用户
     *
     * @param profile 第三方用户信息载体
     * @return 命中的系统用户 ID；未命中返回 {@code null}
     */
    Long resolve(ExternalUserProfile profile);
}
