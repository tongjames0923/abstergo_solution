package com.tbs.web.security.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * 默认「访问被拒绝」处理器
 * <p>
 * 实现 Spring Security {@link AccessDeniedHandler}：当已认证用户访问无权限资源时，
 * 统一返回 403 响应。若当前认证上下文携带了认证异常（如 {@code @PreAuthorize} 校验失败原因），
 * 则一并透传给前端，便于定位。
 * </p>
 *
 * @see AccessDeniedHandler Spring Security 访问拒绝契约
 */
public class DefaultAuthAccessDeniedHandler implements AccessDeniedHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAuthAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("权限异常", accessDeniedException);
        // 优先使用认证上下文中记录的异常信息，否则仅返回标准 403
        Optional<ITokenAuthentication> authentication = AuthUtil.getInstance().getAuthentication();
        AjaxResult res = authentication.map(new Function<ITokenAuthentication, AjaxResult>() {
                    @Override
                    public AjaxResult apply(ITokenAuthentication authentication) {
                        return AjaxUtil.getInstance().custom(
                                AjaxResult.DEFAULT_FORBIDDEN_CODE,
                                AjaxResult.getDefaultForbiddenMessage(),
                                authentication.getAuthException()
                        );
                    }
                }).orElse(
                        AjaxUtil.getInstance().custom(
                                AjaxResult.DEFAULT_FORBIDDEN_CODE,
                                AjaxResult.getDefaultForbiddenMessage(), null
                        )
                );
        AjaxUtil.writeInfoResp(response, res);
    }
}