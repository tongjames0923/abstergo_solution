package com.tbs.sql.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * SQL提取异常
 *
 * @author abstergo
 */
public class SqlExtractionException extends FormattedRuntimeException {


    public SqlExtractionException(String message, Object from, Object... args) {
        super("from:{} ,details:" + message, from, args);
    }
}
