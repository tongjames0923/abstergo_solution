package com.tbs.web.security.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.exception.ReLoginRequestException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.model.SimpleTokenModel;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 令牌过滤器
 *
 * @author tongj
 */
@Slf4j
@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Lazy
    @Resource
    private ITokenPicker tokenPicker;

    @Resource
    private IOnErrorAjaxProvider onErrorAjaxProvider;

    public TokenAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
        this.setAuthenticationFailureHandler((request, response, e) -> {
            AjaxUtil.writeInfoResp(response, onErrorAjaxProvider.onError(e));
        });
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {
        com.tbs.web.security.ITokenModel simpleTokenModel = null;
        UserDetails principal = null;
        HttpServletRequest serverRequest = request;
        simpleTokenModel = tokenPicker.pick(serverRequest);
        AssertUtil.notNull(simpleTokenModel, () -> new NotFoundTokenException("未找到令牌."));
        return getAuthenticationManager().authenticate(TokenAuthentication.create(simpleTokenModel));
    }

}
