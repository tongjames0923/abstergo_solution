package com.tbs.web.security.impl;

import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 令牌过滤器
 *
 * @author tongj
 */
@Slf4j
@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private AuthenticationManager manager;

    RequestMatcher matcher;

    @Resource
    private IOnErrorAjaxProvider onErrorAjaxProvider;

    public TokenAuthenticationFilter(RequestMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !matcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        try {
            ITokenAuthentication tokenAuthentication =
                AuthUtil.getInstance().getAuthentication().orElseThrow(() -> new NotFoundTokenException("未发现令牌"));
            SecurityContextHolder.getContext().setAuthentication(manager.authenticate(tokenAuthentication));
        } catch (Throwable e) {
            AjaxUtil.writeInfoResp(response, onErrorAjaxProvider.onError(e));
            return;
        }

        filterChain.doFilter(request, response);
    }

}
