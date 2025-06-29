package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.model.TokenModel;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author tongj
 */
public class AuthExceptionHandler implements Customizer<ExceptionHandlingConfigurer<HttpSecurity>> {
    @Override
    public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {

        httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new AccessDeniedHandler() {
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
                AjaxResult result = null;
                if (token == null) {
                    result =
                        AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, "未授权,请传递Token", null);
                    writeIntoBody(response, result);
                    return;
                }
                if (principal == null) {
                    result =
                        AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, "未授权,Token无效", null);
                    writeIntoBody(response, result);
                    return;
                }

                result =
                    AjaxUtil.getInstance().error(accessDeniedException.getLocalizedMessage(), accessDeniedException);
                writeIntoBody(response, result);
                return;
            }
        });
    }

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
}
