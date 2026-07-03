package com.tbs.common.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.StrUtil;

/**
 * 带格式的运行时异常
 * <p>
 * 支持消息格式化的运行时异常类，实现了IExceptionLevel接口，可以设置异常级别
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class FormattedRuntimeException extends RuntimeException implements IExceptionLevel {
    private ErrorLevelEnum level = ErrorLevelEnum.SYSTEM_ERROR;

    /**
     * 构建由原因异常引起的异常消息
     * <p>
     * 将原因异常信息和自定义消息格式化为统一的异常消息
     * </p>
     *
     * @param cause   原因异常
     * @param message 自定义消息模板
     * @param params  消息参数
     * @param <T>     异常类型
     * @return 格式化后的异常消息
     */
    private static <T extends Throwable> String causeByExceptionMessage(T cause, String message, Object... params) {
        StringBuilder sb = new StringBuilder();
        if (cause != null) {
            sb.append(
                StrUtil.format("异常类型:{},异常信息:{}.", cause.getClass().getName(), cause.getLocalizedMessage()));
        }
        if (StrUtil.isNotEmpty(message)) {
            sb.append(" 更多信息:").append(StrUtil.format(message, params)).append(".");
        }
        return sb.toString();
    }

    /**
     * 构造带格式的运行时异常
     *
     * @param message 消息模板
     * @param params  消息参数
     */
    public FormattedRuntimeException(String message, Object... params) {
        super(StrUtil.format(message, params));
    }

    /**
     * 构造带格式和原因的运行时异常
     *
     * @param cause   原因异常
     * @param message 消息模板
     * @param params  消息参数
     * @param <T>     异常类型
     */
    public <T extends Throwable> FormattedRuntimeException(T cause, String message, Object... params) {
        super(causeByExceptionMessage(cause, message, params));
    }

    /**
     * 获取异常级别
     * <p>
     * 实现IExceptionLevel接口的方法
     * </p>
     *
     * @return 异常级别
     */
    @Override
    public ErrorLevelEnum getLevel() {
        return level;
    }

    /**
     * 设置异常级别
     * <p>
     * 支持链式调用
     * </p>
     *
     * @param errorLevel 异常级别
     * @return 当前异常对象
     */
    public FormattedRuntimeException errorLevel(ErrorLevelEnum errorLevel) {
        if (errorLevel == null) {
            return this;
        }
        this.level = errorLevel;
        return this;
    }

    // ======================== 静态工厂方法 ========================

    /**
     * 创建用户输入错误异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 用户输入错误级别的异常
     */
    public static FormattedRuntimeException userInputError(String message, Object... params) {
        return new FormattedRuntimeException(message, params)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 创建业务逻辑错误异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 业务逻辑错误级别的异常
     */
    public static FormattedRuntimeException businessError(String message, Object... params) {
        return new FormattedRuntimeException(message, params)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    /**
     * 创建数据访问错误异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 数据访问错误级别的异常
     */
    public static FormattedRuntimeException dataAccessError(String message, Object... params) {
        return new FormattedRuntimeException(message, params)
            .errorLevel(ErrorLevelEnum.DATA_ACCESS_ERROR);
    }

    /**
     * 创建系统错误异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 系统错误级别的异常
     */
    public static FormattedRuntimeException systemError(String message, Object... params) {
        return new FormattedRuntimeException(message, params)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 创建严重系统错误异常
     *
     * @param message 错误消息模板
     * @param params  消息参数
     * @return 严重系统错误级别的异常
     */
    public static FormattedRuntimeException criticalSystemError(String message, Object... params) {
        return new FormattedRuntimeException(message, params)
            .errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}
