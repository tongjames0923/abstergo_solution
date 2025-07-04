package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.ExceptionUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.config.property.WebProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.model.TokenModel;
import com.tbs.web.uitl.AjaxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 令牌过滤器
 *
 * @author tongj
 */
@Slf4j
@ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Lazy
    @Resource
    private ITokenPicker tokenPicker;

    @Resource
    private AuthenticationManager manager;

    RequestMatcher matcher;

    @PostConstruct
    void init() {
        WebProperty property = SpringUtil.getBean(WebProperty.class);
        List<RequestMatcher> matchers = new ArrayList<>(property.getSecuredPaths().size());
        property.getSecuredPaths().stream().forEach(path -> matchers.add(new AntPathRequestMatcher(path)));
        matcher = new AndRequestMatcher(matchers);
    }

    private void writeIntoBody(HttpServletResponse response, AjaxResult ajaxResult) {
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/json; charset=utf-8");

        // 返回JSON响应
        String jsonResponse = JSON.toJSONString(ajaxResult.getMap());
        PrintWriter writer = null;
        try {
            writer = httpResponse.getWriter();
        } catch (IOException e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e));
        }
        writer.print(jsonResponse);
        writer.flush();
        writer.close();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !matcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        TokenModel tokenModel = null;
        UserDetails principal = null;
        try {
            HttpServletRequest serverRequest = request;
            tokenModel = tokenPicker.pick(serverRequest);
            AssertUtil.notNull(tokenModel, () -> new NotFoundTokenException("未找到令牌."));
            auth(manager.authenticate(TokenAuthentication.create(tokenModel)));

        } catch (NotFoundTokenException | InvalidTokenException e) {
            auth(TokenAuthentication.badTokenCreate(e, ""));
        } catch (Exception e) {
            AjaxResult result = AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE, e.getMessage(), null);
            writeIntoBody(response, result);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void auth(Authentication a) {
        SecurityContextHolder.getContext().setAuthentication(a);
    }
}
