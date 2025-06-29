package com.tbs.web.security.impl;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.model.TokenModel;
import com.tbs.web.security.ITokenHandler;
import kotlin.NotImplementedError;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 默认的Token解析器，抛出异常
 * @author tongj
 */
public class NoImplTokenHandler implements ITokenHandler {

    public NoImplTokenHandler() {
        throw new NotImplementedError("请实现一个Token解析器");
    }

    @Override
    public UserDetails handle(TokenModel tokenModel) throws InvalidTokenException {
        throw new InvalidTokenException("请实现一个Token解析器");
    }
}
