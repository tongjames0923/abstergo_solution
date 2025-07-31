package com.tbs.web.security.impl;

import com.tbs.common.util.StrUtil;
import com.tbs.web.exception.NoLoginException;
import com.tbs.web.exception.ReLoginRequestException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.uitl.AuthUtil;
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

/**
 * 认证异常处理
 *
 * @author tongj
 */
@Slf4j
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        log.error("认证异常", authException);
        if (authException instanceof ReLoginRequestException) {
            AjaxUtil.writeInfoResp(response, AjaxUtil.getInstance().custom(AjaxResult.RELOGIN_CODE,
                StrUtil.isEmpty(authException.getLocalizedMessage()) ? AjaxResult.DEFAULT_RELOGIN_MESSAGE
                    : authException.getLocalizedMessage(), authException));
        }
        AjaxUtil.writeInfoResp(response, AjaxUtil.getInstance()
            .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, authException.getLocalizedMessage(), authException));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("权限异常", accessDeniedException);
        var res = AuthUtil.getInstance().getAuthentication().map(authentication -> {
            return AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE,
                StrUtil.format("授权信息错误:{}", accessDeniedException.getLocalizedMessage()), null);
        }).orElse(AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_FORBIDDEN_CODE,
            StrUtil.isEmpty(accessDeniedException.getLocalizedMessage()) ? "无权限访问"
                : accessDeniedException.getLocalizedMessage(), null));
        AjaxUtil.writeInfoResp(response, res);
        return;
    }
}
