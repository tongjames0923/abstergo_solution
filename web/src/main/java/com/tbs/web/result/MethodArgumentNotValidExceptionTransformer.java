package com.tbs.web.result;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * 参数校验异常转换器
 * <p>
 * 将 Spring MVC 的 {@link MethodArgumentNotValidException}（@Valid 校验失败）转换为用户级错误异常，
 * 使校验失败以友好提示返回前端，而非框架默认的内部错误。
 * </p>
 *
 * @author abstergo
 * @see IErrorTransformer 错误转换契约
 */
@Component
public class MethodArgumentNotValidExceptionTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && throwable instanceof MethodArgumentNotValidException;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        MethodArgumentNotValidException e = (MethodArgumentNotValidException)throwable;
        var fieldError = e.getBindingResult().getFieldError();
        // 优先取字段级校验消息（面向用户）；无字段错误时不得回退到框架异常原文（含方法签名与参数快照），改用通用提示
        String message = fieldError != null && StrUtil.isNotEmpty(fieldError.getDefaultMessage())
            ? fieldError.getDefaultMessage() : I18nUtil.get(CommonI18nConstant.COMMON_INVALID_PARAMETER);
        return new FormattedRuntimeException(message).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
