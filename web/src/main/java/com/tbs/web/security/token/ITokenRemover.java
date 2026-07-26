package com.tbs.web.security.token;

import org.jetbrains.annotations.NotNull;

public interface ITokenRemover {

    /**
     * 撤销访问令牌
     *
     * @param token
     */
    void inactiveToken( ITokenModel token);

    /**
     * 撤销访问令牌
     *
     * @param token
     */
    void revokeToken( ITokenModel token);
}
