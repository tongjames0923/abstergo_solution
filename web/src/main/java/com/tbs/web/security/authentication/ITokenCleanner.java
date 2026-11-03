package com.tbs.web.security.authentication;

import com.tbs.web.security.token.ITokenModel;

public interface ITokenCleanner {

    /**
     * 获取清理器名称
     *
     * @return
     */
    String getName();

    /**
     * 清理token
     *
     * @param token
     */
    void clean(ITokenModel token);
}
