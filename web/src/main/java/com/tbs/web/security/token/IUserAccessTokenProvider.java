package com.tbs.web.security.token;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * 用户访问令牌提供者
 *
 * @author abstergo
 */
public interface IUserAccessTokenProvider<FORM extends Serializable> {
    /**
     * 请求访问令牌
     *
     * @param form
     * @return
     */
    ITokenModel requestToken(@NotNull FORM form);

}
