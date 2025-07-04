package com.tbs.common;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.StrUtil;

/**
 * 异常工具类
 *
 * @author tongj
 */
public class ExceptionUtil {
    /**
     * 创建一个新的格式化运行时异常对象 此方法允许通过指定消息和参数来创建一个FormattedRuntimeException对象，其中消息可以包含格式化占位符
     *
     * @param message 异常消息，可以包含格式化占位符
     * @param params  消息中的参数，将替换消息中的占位符
     * @return 新创建的FormattedRuntimeException对象
     */
    public static FormattedRuntimeException newException(String message, Object... params) {
        return new FormattedRuntimeException(message, params);
    }

    /**
     * 创建一个新的带有原因的格式化运行时异常对象 此方法允许通过指定原因、消息和参数来创建一个FormattedRuntimeException对象，其中消息可以包含格式化占位符
     *
     * @param cause   异常的原因，可以是另一个异常
     * @param message 异常消息，可以包含格式化占位符
     * @param params  消息中的参数，将替换消息中的占位符
     * @return 新创建的FormattedRuntimeException对象
     */
    public static FormattedRuntimeException newException(Throwable cause, String message, Object... params) {
        return new FormattedRuntimeException(cause, message, params);
    }

    /**
     * 创建一个新的带有原因的格式化运行时异常对象 当不需要指定消息时，此方法用于仅通过原因创建异常
     *
     * @param cause 异常的原因，可以是另一个异常
     * @return 新创建的FormattedRuntimeException对象
     */
    public static FormattedRuntimeException newException(Throwable cause) {
        return newException(cause, "");
    }

    /**
     * 创建一个新的带有上下文和原因的格式化运行时异常对象 此方法允许通过指定上下文类、原因、消息和参数来创建一个FormattedRuntimeException对象，其中消息可以包含格式化占位符
     * 异常消息将包括异常源信息，即提供的上下文类的名称
     *
     * @param context 异常发生的上下文类，可以为null
     * @param cause   异常的原因，可以是另一个异常
     * @param message 异常消息，可以包含格式化占位符
     * @param params  消息中的参数，将替换消息中的占位符
     * @return 新创建的FormattedRuntimeException对象
     */
    public static FormattedRuntimeException newException(Class<?> context, Throwable cause, String message,
        Object... params) {
        return newException(cause, "异常源:{} ,{}", context == null ? "未知" : context.getName(),
            StrUtil.format(message, params));
    }

    /**
     * 创建一个新的带有上下文和原因的格式化运行时异常对象 当不需要指定消息时，此方法用于通过上下文类和原因创建异常 异常消息将包括异常源信息，即提供的上下文类的名称
     *
     * @param context 异常发生的上下文类，可以为null
     * @param cause   异常的原因，可以是另一个异常
     * @return 新创建的FormattedRuntimeException对象
     */
    public static FormattedRuntimeException newException(Class<?> context, Throwable cause) {
        return newException(cause, "异常源:{}", context == null ? "未知" : context.getName());
    }

}
