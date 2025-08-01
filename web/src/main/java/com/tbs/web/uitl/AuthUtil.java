package com.tbs.web.uitl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.exception.NoLoginException;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.impl.TokenAuthentication;
import lombok.Getter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 认证工具类
 *
 * @author tongj
 */
@Component
public class AuthUtil {
    /**
     * -- GETTER -- 获取单例
     *
     * @return
     */
    private static AuthUtil instance;

    public AuthUtil() {
        instance = this;
    }

    @Resource
    private AuthenticationManager manager;

    public static AuthUtil getInstance() {
        return instance;
    }

    /**
     * 获取当前用户的TokenModel
     *
     * @return
     */
    public ITokenModel getCurrentTokenModel() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return getAuthentication().map(ITokenAuthentication::getTokenModel)
            .orElseThrow(() -> new FormattedRuntimeException("用户Token获取失败"));
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    public UserDetails getCurrentUser() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return getAuthentication().map(ITokenAuthentication::getUserDetails)
            .orElseThrow(() -> new FormattedRuntimeException("用户数据获取失败"));
    }

    /**
     * 判断当前用户是否已认证（已登录）
     *
     * @return
     */
    public boolean isUserLoggedIn() {
        return getAuthentication().map(authentication -> authentication.hasToken() && authentication.isAuthenticated())
            .orElse(false);
    }

    /**
     * 获取当前用户的认证信息
     *
     * @return
     */
    public Optional<ITokenAuthentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (!(authentication instanceof TokenAuthentication)) {
            return Optional.empty();
        }
        return Optional.of((ITokenAuthentication)authentication);
    }

    /**
     * 设置当前用户的认证信息
     *
     * @param authentication
     */
    public void setAuthentication(ITokenAuthentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
