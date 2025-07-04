package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.exception.FormattedRuntimeException;
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
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
            throw new FormattedRuntimeException(e, "写入Body异常");
        }
        writer.print(jsonResponse);
        writer.flush();
        writer.close();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        TokenModel tokenModel = null;
        UserDetails principal = null;
        try {
            HttpServletRequest serverRequest = request;
            tokenModel = tokenPicker.pick(serverRequest);
            if (tokenModel == null) {
                throw new NotFoundTokenException("未找到令牌.");
            }
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
