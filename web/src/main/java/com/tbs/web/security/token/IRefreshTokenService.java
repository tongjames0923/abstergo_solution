package com.tbs.web.security.token;

/**
 * 刷新令牌服务
 * <p>
 * 管理刷新令牌的完整生命周期：创建、禁用与使用（换取新的访问令牌）。
 * 刷新令牌用于在无用户再次登录的情况下延续访问令牌的有效性。
 * </p>
 *
 * @author jiangnan
 * @date 2020/01/07
 * @see ITokenModel 令牌模型
 */
public interface IRefreshTokenService {

    /**
     * 创建刷新令牌
     *
     * @return 新创建的刷新令牌模型
     */
    ITokenModel createRefreshToken();

    /**
     * 禁用当前刷新令牌
     * <p>
     * 将刷新令牌置为失效状态，使其无法再换取访问令牌（典型的登出/撤销场景）。
     * </p>
     */
    void inactiveRefreshToken();

    /**
     * 使用刷新令牌换取访问令牌
     * <p>
     * 校验刷新令牌有效性后签发新的访问令牌；刷新令牌非法或已失效时由实现方抛异常。
     * </p>
     *
     * @param refreshToken 待使用的刷新令牌模型，不能为空
     * @return 新签发的访问令牌模型
     */
    ITokenModel useRefreshToken();
}
