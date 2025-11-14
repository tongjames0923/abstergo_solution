package com.tbs.web.result.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * 默认错误处理
 *
 * @author tongj
 */
public class DefaultErrorAjaxProvider implements IOnErrorAjaxProvider {
    @Override
    public AjaxResult onError(Throwable throwable) {
        if (throwable instanceof AccessDeniedException) {
            return AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_FORBIDDEN_CODE,
                StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.DEFAULT_FORBIDDEN_MESSAGE), null);
        } else if (throwable instanceof AuthenticationException) {
            return AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_UNAUTHORIZED_CODE,
                StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.DEFAULT_UNAUTHORIZED_MESSAGE), null);
        } else if (throwable instanceof MethodArgumentNotValidException) {
            return AjaxUtil.getInstance().custom(AjaxResult.DEFAULT_FAIL_CODE,
                ((MethodArgumentNotValidException)throwable).getBindingResult().getFieldError().getDefaultMessage(),
                null);
        }
        return AjaxUtil.getInstance()
            .error(StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.DEFAULT_ERROR_MESSAGE), null);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
