package com.tbs.web.security.token;

import org.jetbrains.annotations.NotNull;

public interface ITokenRemover {

    /**
     * 撤销访问令牌
     *
     * @param token
     */
    void inactiveToken(@NotNull ITokenModel token);

    /**
     * 撤销访问令牌
     *
     * @param token
     */
    void revokeToken(@NotNull ITokenModel token);
}
