package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.util.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 认证异常处理
 *
 * @author tongj
 */
@Slf4j
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
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        log.error("认证异常", authException);
        writeIntoBody(response, AjaxUtil.getInstance()
            .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, authException.getLocalizedMessage(), authException));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("权限异常", accessDeniedException);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            writeIntoBody(response, AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE, "空授权信息", null));
            return;
        }
        if (!(authentication instanceof TokenAuthentication)) {
            writeIntoBody(response, AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE,
                StrUtil.format("授权信息错误:{}", accessDeniedException.getLocalizedMessage()), null));
        }
        TokenAuthentication tokenAuthentication = (TokenAuthentication)authentication;
        AjaxResult result = AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_FORBIDDEN_CODE,
            StrUtil.isEmpty(tokenAuthentication.getMessage()) ? "无权限访问" : tokenAuthentication.getMessage(), null);
        writeIntoBody(response, result);

        return;
    }
}
