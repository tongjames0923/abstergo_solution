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

/**
 * 默认认证入口点
 * <p>
 * 实现 Spring Security {@link AuthenticationEntryPoint}：处理匿名/未认证请求触发的认证异常。
 * 若异常为 {@link ReLoginRequestException}，返回「需重新登录」语义码；否则统一返回 401 未认证响应。
 * </p>
 *
 * @see AuthenticationEntryPoint Spring Security 认证入口契约
 */
public class DefaultAuthEntryPoint implements AuthenticationEntryPoint {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("认证异常", authException);
        // 区分「需重新登录」与通用「未认证」两类语义，前端据此决定跳转登录还是提示失效
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