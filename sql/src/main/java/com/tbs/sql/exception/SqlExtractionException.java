package com.tbs.sql.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;

/**
 * SQL提取异常
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SqlExtractionException extends FormattedRuntimeException {

    public SqlExtractionException(String message, Object from, Object... args) {
        super("from:{} ,details:" + message, from, args);
        errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}
