package com.tbs.web.result.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * 默认错误处理
 *
 * @author tongj
 */
public class SimpleErrorAjaxProvider implements IOnErrorAjaxProvider {
    @Override
    public AjaxResult onError(Throwable throwable) {
        if (throwable instanceof AuthenticationException) {
            return AjaxUtil.getInstance()
                .custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE, "未授权。" + throwable.getMessage(), null);
        } else if (throwable instanceof AccessDeniedException) {
            return AjaxUtil.getInstance()
                .custom(AjaxResult.DEFAULT_FORBIDDEN_CODE, "无访问权限。" + throwable.getMessage(), null);
        } else if (throwable instanceof MethodArgumentNotValidException) {
            return AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_FAIL_CODE,
                ((MethodArgumentNotValidException)throwable).getBindingResult().getFieldError().getDefaultMessage(),
                null);
        }
        return AjaxUtil.getInstance().error("系统异常。" + throwable.getMessage(), null);
    }
}
