package com.tbs.web.result;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
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
        // 优先取字段级校验消息，无则回退到异常消息
        String message = fieldError != null ? fieldError.getDefaultMessage() : e.getMessage();
        return new FormattedRuntimeException(message).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
