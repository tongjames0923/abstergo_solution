package com.tbs.common.exception;

import com.tbs.common.util.StrUtil;

/**
 * 带格式的运行时异常
 *
 * @author abstergo
 */
public class FormattedRuntimeException extends RuntimeException {
    public FormattedRuntimeException(String message, Object... params) {
        super(StrUtil.format(message, params));
    }
}
