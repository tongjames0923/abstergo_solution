package com.tbs.app.error.transformer;

import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@Component
public class AccessDeniedTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && throwable instanceof AccessDeniedException;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        return FormattedRuntimeException.businessErrorI18n("auth.user.access.denied");
    }
}
