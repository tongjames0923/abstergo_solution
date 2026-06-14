package com.tbs.sql.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * SQL构建异常
 *
 * @author abstergo
 * @since 2.0.0
 */
public class SqlBuildException extends FormattedRuntimeException {

    public SqlBuildException(String message, Object... args) {
        super(message, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }

    public SqlBuildException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}
