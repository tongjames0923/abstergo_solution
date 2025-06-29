package com.tbs.web.security;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.security.model.TokenModel;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 令牌处理器
 *
 * @author tongj
 */
public interface ITokenHandler {
    /**
     * 处理令牌
     *
     * @param tokenModel 令牌模型
     * @return 用户详情
     * @throws InvalidTokenException 无效令牌异常
     */
    UserDetails handle(TokenModel tokenModel) throws InvalidTokenException;
}
