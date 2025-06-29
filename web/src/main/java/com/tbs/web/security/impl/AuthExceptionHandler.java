package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.util.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.model.TokenModel;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 认证异常处理
 * @author tongj
 */
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private void writeIntoBody(HttpServletResponse response, AjaxResult custom) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/json; charset=utf-8");
        String jsonResponse = JSON.toJSONString(custom.getMap());
        PrintWriter writer = null;

        writer = httpResponse.getWriter();
        writer.print(jsonResponse);
        writer.flush();
        writer.close();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {

        writeIntoBody(response, AjaxUtil.getInstance()
            .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, authException.getLocalizedMessage(), authException));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        TokenModel token = null;
        UserDetails principal = null;
        if (SecurityContextHolder.getContext().getAuthentication().getCredentials() != null) {
            token = (TokenModel)SecurityContextHolder.getContext().getAuthentication().getCredentials();
        }
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            principal = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        if (token == null) {
            AjaxResult result = null;
            result = AjaxUtil.getInstance()
                .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, AjaxResult.DEFAULT_UNAUTHORIZED_MESSAGE, null);
            writeIntoBody(response, result);
            return;
        }
        if (principal == null) {
            AjaxResult result = null;
            result = AjaxUtil.getInstance()
                .custom(AjaxResult.DEFAULT_FORBIDDEN_CODE, AjaxResult.DEFAULT_FORBIDDEN_MESSAGE, null);
            writeIntoBody(response, result);
            return;
        }

        AjaxResult result = null;
        result = AjaxUtil.getInstance()
            .custom(AjaxResult.DEFAULT_ERROR_CODE, StrUtil.format("{} 不存在所需权限", principal.getUsername()), null);
        writeIntoBody(response, result);

        return;
    }
}
