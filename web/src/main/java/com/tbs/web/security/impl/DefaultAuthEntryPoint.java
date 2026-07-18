package com.tbs.web.security.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.web.exception.ReLoginRequestException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultAuthEntryPoint implements AuthenticationEntryPoint {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("认证异常", authException);
        if (authException instanceof ReLoginRequestException) {
            AjaxUtil.writeInfoResp(
                    response, AjaxUtil.getInstance().custom(
                            AjaxResult.RELOGIN_CODE,
                            StrUtil.isEmpty(authException.getLocalizedMessage()) ?
                                    AjaxResult.getDefaultReLoginMessage() :
                                    authException.getLocalizedMessage(), authException
                    )
            );
        }
        AjaxUtil.writeInfoResp(
                response, AjaxUtil.getInstance()
                        .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, authException.getLocalizedMessage(), authException)
        );
    }
}