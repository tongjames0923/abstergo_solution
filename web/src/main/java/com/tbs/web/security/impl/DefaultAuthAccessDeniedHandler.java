package com.tbs.web.security.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认「访问被拒绝」处理器
 * <p>
 * 实现 Spring Security {@link AccessDeniedHandler}：当已认证用户访问无权限资源时，
 * 统一返回 403 响应。响应体只携带 i18n 文案，不回传认证异常对象——异常对象会连同堆栈被
 * 序列化写入响应，认证失败的真实原因已由上面的 error 日志记录。
 * </p>
 *
 * @see AccessDeniedHandler Spring Security 访问拒绝契约
 */
public class DefaultAuthAccessDeniedHandler implements AccessDeniedHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAuthAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("权限异常", accessDeniedException);
        AjaxUtil.writeInfoResp(response, AjaxUtil.getInstance().custom(
            AjaxResult.DEFAULT_FORBIDDEN_CODE,
            AjaxResult.getDefaultForbiddenMessage(),
            null
        ));
    }
}
