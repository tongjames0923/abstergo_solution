package com.tbs.web.result;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
public class MethodArgumentNotValidExceptionTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && throwable instanceof MethodArgumentNotValidException;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        MethodArgumentNotValidException e = (MethodArgumentNotValidException)throwable;
        return new FormattedRuntimeException(e.getBindingResult().getFieldError().getDefaultMessage()).errorLevel(
            ErrorLevelEnum.USER_INPUT_ERROR);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
