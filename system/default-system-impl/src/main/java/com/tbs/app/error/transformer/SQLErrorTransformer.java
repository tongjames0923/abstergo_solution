package com.tbs.app.error.transformer;

import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 数据库异常转换器
 *
 * @author abstergo
 */
@Component
public class SQLErrorTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && (throwable instanceof SQLException || throwable instanceof DataAccessException);
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        return FormattedRuntimeException.dataAccessError(throwable.getMessage());
    }
}
