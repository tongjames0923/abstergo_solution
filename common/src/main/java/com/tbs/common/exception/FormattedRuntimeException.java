package com.tbs.common.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;

/**
 * 带格式的运行时异常
 * <p>
 * 支持消息格式化和国际化(i18n)的运行时异常类，实现了IExceptionLevel接口，可以设置异常级别。
 * <p>
 * 使用方式：
 * <pre>
 * // 直接使用消息模板
 * throw new FormattedRuntimeException("用户{}不存在", userId);
 *
 * // 使用i18n key
 * throw FormattedRuntimeException.userInputErrorI18n("user.not.found");
 *
 * // 使用i18n key并带参数
 * throw FormattedRuntimeException.userInputErrorI18n("user.status.invalid", userStatus);
 * </pre>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class FormattedRuntimeException extends RuntimeException implements IExceptionLevel {
    private ErrorLevelEnum level = ErrorLevelEnum.SYSTEM_ERROR;

    /**
     * 是否使用i18n key
     */
    private boolean useI18n = false;

    /**
     * i18n消息key
     */
    private String i18nKey;

    /**
     * 构建由原因异常引起的异常消息
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

    public FormattedRuntimeException() {
        super();
    }

    /**
     * 获取异常级别
     *
     * @return 异常级别
     */
    @Override
    public ErrorLevelEnum getLevel() {
        return level;
    }

    /**
     * 设置异常级别
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

    // ======================== 静态工厂方法（直接消息）========================

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

    // ======================== 静态工厂方法（i18n支持）========================

    /**
     * 创建用户输入错误异常（i18n）
     * <p>
     * 使用i18n key获取国际化消息，如果找不到则使用key本身
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 用户输入错误级别的异常
     */
    public static FormattedRuntimeException userInputErrorI18n(String i18nKey, Object... params) {
        return new FormattedRuntimeException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 创建业务逻辑错误异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 业务逻辑错误级别的异常
     */
    public static FormattedRuntimeException businessErrorI18n(String i18nKey, Object... params) {
        return new FormattedRuntimeException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    /**
     * 创建数据访问错误异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 数据访问错误级别的异常
     */
    public static FormattedRuntimeException dataAccessErrorI18n(String i18nKey, Object... params) {
        return new FormattedRuntimeException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.DATA_ACCESS_ERROR);
    }

    /**
     * 创建系统错误异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 系统错误级别的异常
     */
    public static FormattedRuntimeException systemErrorI18n(String i18nKey, Object... params) {
        return new FormattedRuntimeException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 创建严重系统错误异常（i18n）
     *
     * @param i18nKey i18n消息key
     * @param params  消息参数
     * @return 严重系统错误级别的异常
     */
    public static FormattedRuntimeException criticalSystemErrorI18n(String i18nKey, Object... params) {
        return new FormattedRuntimeException(I18nUtil.get(i18nKey, params))
            .errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
    }
}