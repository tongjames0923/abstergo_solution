package com.tbs.app.config;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.AssertUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.model.TokenModel;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author abstergo
 */
public class UserAuthServiceImpl implements IUserAuthService {

    @Resource
    WebAuthProperty authProperty;

    @Override
    public UserDetails loadUserInfoByUniqueKey(String uniqueKey) {
        if ("1".equals(uniqueKey)) {
            return User.builder().username("JACK").password("123456").authorities("ADMIN").build();
        } else if ("2".equals(uniqueKey)) {
            return User.builder().username("TOM").password("123456").authorities("USER").build();
        }
        AssertUtil.throwError(() -> new SimpleAuthException("唯一Key用户不存在"));
        return null;
    }

    @Override
    public UserDetails loadUserInfoByToken(TokenModel token) {
        if ("JACK".equals(token.getToken())) {
            return User.builder().username("JACK").password("123456").authorities("ADMIN").build();
        } else if ("TOM".equals(token.getToken())) {
            return User.builder().username("TOM").password("123456").authorities("USER").build();
        }
        AssertUtil.throwError(() -> new SimpleAuthException("Token用户不存在"));
        return null;
    }

    @Override
    public TokenModel login(String username, String password) {
        UserDetails details = loadUserInfoByUniqueKey(username);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new SimpleAuthException("用户名或密码错误");
        });
        return TokenModel.create(details.getUsername(), authProperty.getTokenSource(), authProperty.getTokenFiledName(),
            details.getUsername(), null);
    }

    @Override
    public void authToken(TokenModel tokenModel) throws AuthenticationException {
        if (tokenModel.getToken() != null) {
            if (loadUserInfoByToken(tokenModel) != null) {
                return;
            }
        }
        AssertUtil.throwError(() -> new SimpleAuthException("Token错误"));
    }

    @Override
    public boolean logout(TokenModel tokenModel) {
        return true;
    }
}
