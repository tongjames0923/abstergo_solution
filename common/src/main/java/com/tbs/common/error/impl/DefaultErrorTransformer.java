package com.tbs.common.error.impl;

import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.stereotype.Component;

/**
 * 默认异常转换器
 *
 * @author tongj
 */

@Component
public class DefaultErrorTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        if (throwable instanceof FormattedRuntimeException) {
            return (FormattedRuntimeException)throwable;
        }
        var formattedRuntimeException = new FormattedRuntimeException(throwable, "");
        if (throwable instanceof IExceptionLevel) {
            formattedRuntimeException.errorLevel(((IExceptionLevel)throwable).getLevel());
        }
        return formattedRuntimeException;
    }
}
