package com.tbs.common.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.StrUtil;

/**
 * 带格式的运行时异常
 *
 * @author abstergo
 */
public class FormattedRuntimeException extends RuntimeException implements IExceptionLevel {
    private ErrorLevelEnum level = ErrorLevelEnum.SYSTEM_ERROR;

    private static <T extends Throwable> String causeByExceptionMessage(T cause, String message, Object... params) {
        StringBuilder sb = new StringBuilder();
        if (cause != null) {
            sb.append(
                StrUtil.format("异常类型:{},异常信息:{}.", cause.getClass().getName(), cause.getLocalizedMessage()));
        }
        if (StrUtil.isNotEmpty(message)) {
            sb.append(StrUtil.format(" 更多信息:{} .", StrUtil.format(message, params)));
        }
        return sb.toString();
    }

    /**
     * 带格式的运行时异常
     */
    public FormattedRuntimeException(String message, Object... params) {
        super(StrUtil.format(message, params));
    }

    /**
     * 带格式的运行时异常
     */
    public <T extends Throwable> FormattedRuntimeException(T cause, String message, Object... params) {
        super(causeByExceptionMessage(cause, message, params));
    }

    @Override
    public ErrorLevelEnum getLevel() {
        return level;
    }

    public FormattedRuntimeException errorLevel(ErrorLevelEnum errorLevel) {
        if (errorLevel == null) {
            return this;
        }
        this.level = errorLevel;
        return this;
    }
}
