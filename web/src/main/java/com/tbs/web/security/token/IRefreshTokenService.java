package com.tbs.web.security.token;

/**
 * 刷新令牌服务
 *
 * @author jiangnan
 * @date 2020/01/07
 */
public interface IRefreshTokenService {

    /**
     * 创建刷新令牌
     *
     * @return 刷新令牌
     */
    ITokenModel createRefreshToken();

    /**
     * 禁用刷新令牌
     */
    void inactiveRefreshToken();

    /**
     * 使用刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 访问令牌
     */
    ITokenModel useRefreshToken();
}
