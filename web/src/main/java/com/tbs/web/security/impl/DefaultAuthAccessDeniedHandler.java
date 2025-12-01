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

public class DefaultAuthAccessDeniedHandler implements AccessDeniedHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAuthAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("权限异常", accessDeniedException);
        Optional<ITokenAuthentication> authentication = AuthUtil.getInstance().getAuthentication();
        AjaxResult res = authentication.map(new Function<ITokenAuthentication, AjaxResult>() {
                    @Override
                    public AjaxResult apply(ITokenAuthentication authentication) {
                        return AjaxUtil.getInstance().custom(
                                AjaxResult.DEFAULT_FORBIDDEN_CODE,
                                AjaxResult.DEFAULT_FORBIDDEN_MESSAGE,
                                authentication.getAuthException()
                        );
                    }
                }).orElse(
                        AjaxUtil.getInstance().custom(
                                AjaxResult.DEFAULT_FORBIDDEN_CODE,
                                "无权限访问", null
                        )
                );
        AjaxUtil.writeInfoResp(response, res);
    }
}
