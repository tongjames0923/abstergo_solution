package com.tbs.common.error;

import com.tbs.common.IStartup;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.Comparators;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 断言工具类
 * <p>
 * 提供一系列断言方法，用于验证业务逻辑条件和参数有效性。
 * 断言失败时会抛出格式化的运行时异常，便于统一错误处理和日志记录。
 * 该类实现了IStartup接口，在应用启动时初始化错误转换器和错误抛出事件。
 * </p>
 *
 * @author tbs
 * @since 1.0.0
 */
@Component
public class AssertUtil implements IStartup {

    private static Collection<IErrorTransformer> errorTransformers;

    @Override
    public void startUp() throws RuntimeException {
        errorTransformers = SpringUtil.getBeansByTypeSafe(IErrorTransformer.class).stream().sorted(Comparators::asc)
            .collect(Collectors.toList());
        errorThrowEvents = SpringUtil.getBeansByTypeSafe(IErrorThrowEvent.class).stream().collect(Collectors.toList());
    }

    /**
     * 断言构建器内部类，用于构建格式化的运行时异常
     */
    public static final class AssertBuilder {

        private boolean isBuild = false;
        private String message;
        private String i18nKey;
        private List<Object> params = CollUtil.newArrayList();

        private Function<FormattedRuntimeException, RuntimeException> convertor;

        private ErrorLevelEnum errorLevel;

        private Throwable cause;

        private AssertBuilder() {
        }

        public static AssertBuilder createNotAssert() {
            return new AssertBuilder();
        }

        public static AssertBuilder create() {
            var builder = new AssertBuilder();
            builder.isBuild = true;
            return builder;
        }

        /**
         * 设置错误消息
         *
         * @param message 错误消息
         * @return 当前构建器实例
         */
        public AssertBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置错误消息
         *
         * @param message 错误消息
         * @return 当前构建器实例
         */
        public AssertBuilder message(String message, Object... params) {
            this.message = message;
            this.params = CollUtil.newArrayList(params);
            return this;
        }

        /**
         * 设置i18n消息key
         *
         * @param i18nKey i18n消息key
         * @return 当前构建器实例
         */
        public AssertBuilder i18n(String i18nKey) {
            this.i18nKey = i18nKey;
            return this;
        }

        /**
         * 设置i18n消息key和参数
         *
         * @param i18nKey i18n消息key
         * @param params  消息参数
         * @return 当前构建器实例
         */
        public AssertBuilder i18n(String i18nKey, Object... params) {
            this.i18nKey = i18nKey;
            this.params = CollUtil.newArrayList(params);
            return this;
        }


        /**
         * 设置错误消息参数
         *
         * @param params 错误消息参数列表
         * @return 当前构建器实例
         */
        public AssertBuilder params(Object... params) {
            this.params = CollUtil.newArrayList(params);
            return this;
        }

        /**
         * 设置错误消息参数
         *
         * @param params 错误消息参数列表
         * @return 当前构建器实例
         */
        public AssertBuilder params(List<Object> params) {
            this.params = params;
            return this;
        }

        /**
         * 设置错误等级
         *
         * @param errorLevel 错误等级枚举
         * @return 当前构建器实例
         */
        public AssertBuilder errorLevel(ErrorLevelEnum errorLevel) {
            this.errorLevel = errorLevel;
            return this;
        }

        /**
         * 设置异常原因
         *
         * @param cause 异常原因
         * @return 当前构建器实例
         */
        public AssertBuilder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * 构建格式化运行时异常
         *
         * @return 格式化运行时异常实例
         */
        public RuntimeException build() {

            if (!this.isBuild) {
                return null;
            }
            FormattedRuntimeException e1;
            // 确定最终的消息内容
            String finalMessage;
            if (this.i18nKey != null) {
                // 优先使用i18n key
                finalMessage = I18nUtil.get(this.i18nKey, this.params.toArray());
            } else if (this.message != null) {
                finalMessage = this.message;
            } else {
                finalMessage = "";
            }
            // 根据是否有异常原因创建不同的FormattedRuntimeException实例
            if (this.cause != null) {
                if (this.cause instanceof FormattedRuntimeException) {
                    e1 = new FormattedRuntimeException(this.cause, cause.getMessage()).errorLevel(
                        ((FormattedRuntimeException)this.cause).getLevel());
                } else {
                    e1 = new FormattedRuntimeException(this.cause, finalMessage, this.params.toArray());
                }
            } else {
                e1 = new FormattedRuntimeException(finalMessage, this.params.toArray());
            }
            // 如果设置了错误等级，则设置异常的错误等级
            if (this.errorLevel != null) {
                e1.errorLevel(this.errorLevel);
            }

            return e1;
        }

        /**
         * 设置异常转换器
         *
         * @param convertor 异常转换器
         * @return 当前构建器实例
         */
        public <T extends RuntimeException> AssertBuilder as(Function<FormattedRuntimeException, T> convertor) {
            this.convertor = (f) -> convertor.apply(f);
            return this;
        }


        /**
         * 抛出构建的异常
         *
         * @throws FormattedRuntimeException 格式化运行时异常
         */
        public void throwIt() {
            if (!this.isBuild) {
                return;
            }
            AssertUtil.throwError(() -> {
                return AssertBuilder.this.build();
            });
        }

    }

    @Getter
    private static List<IErrorThrowEvent> errorThrowEvents;

    /**
     * 抛出异常
     *
     * @param exceptionSupplier 异常提供者
     * @param <T>               异常类型
     */
    public static <T extends RuntimeException> void throwError(Supplier<T> exceptionSupplier) {

        T e = exceptionSupplier.get();
        // 检查是否有事件处理器处理该异常，如果有则返回，否则抛出异常
        if (errorThrowEvents.stream().anyMatch(event -> event.onAssertUtilThrow(e))) {
            return;
        }
        throw e;
    }

    /**
     * 转换异常为格式化运行时异常
     *
     * @param e 异常
     * @return 格式化运行时异常
     */
    public static <T extends Throwable> Optional<Throwable> toFormattedRuntimeException(T e) {
        if (e == null) {
            return Optional.empty();
        }
        if (e instanceof FormattedRuntimeException) {
            return Optional.of(e);
        }
        if (CollUtil.isNotEmpty(errorTransformers)) {
            for (IErrorTransformer transformer : errorTransformers) {
                if (transformer.support(e)) {
                    return Optional.of(transformer.toFormattedRuntimeException(e));
                }
            }
        }
        return Optional.ofNullable(e);
    }

    /**
     * 根据条件创建可选的断言构建器
     *
     * @param condition 条件表达式
     * @return 如果条件为true返回包含构建器的Optional，否则返回空Optional
     */
    public static AssertBuilder toAssert(boolean condition) {
        if (!condition) {
            return AssertBuilder.createNotAssert();
        }
        return AssertBuilder.create();
    }

    /**
     * 断言条件为真
     *
     * @param condition 待判断的条件
     * @return 如果条件为真返回可构建的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isTrue(boolean condition) {
        return toAssert(condition);
    }

    /**
     * 断言条件为真并指定错误级别
     *
     * @param condition  待判断的条件
     * @param errorLevel 错误级别枚举
     * @return 如果条件为真返回带有指定错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isTrueWithLevel(boolean condition, ErrorLevelEnum errorLevel) {
        return toAssert(condition).errorLevel(errorLevel);
    }

    /**
     * 断言条件为假并指定错误级别
     *
     * @param condition  待判断的条件
     * @param errorLevel 错误级别枚举
     * @return 如果条件为假返回带有指定错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isFalseWithLevel(boolean condition, ErrorLevelEnum errorLevel) {
        return toAssert(!condition).errorLevel(errorLevel);
    }

    /**
     * 断言条件为真并指定错误级别为用户输入错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为真返回带有用户输入错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isTrueUserInputError(boolean condition) {
        return toAssert(condition).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 断言条件为假并指定错误级别为用户输入错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为假返回带有用户输入错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isFalseUserInputError(boolean condition) {
        return toAssert(!condition).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
    }

    /**
     * 断言条件为真并指定错误级别为系统错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为真返回带有系统错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isTrueSystemError(boolean condition) {
        return toAssert(condition).errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 断言条件为假并指定错误级别为系统错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为假返回带有系统错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isFalseSystemError(boolean condition) {
        return toAssert(!condition).errorLevel(ErrorLevelEnum.SYSTEM_ERROR);
    }

    /**
     * 断言条件为真并指定错误级别为业务逻辑错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为真返回带有业务逻辑错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isTrueBusinessError(boolean condition) {
        return toAssert(condition).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    /**
     * 断言条件为假并指定错误级别为业务逻辑错误
     *
     * @param condition 待判断的条件
     * @return 如果条件为假返回带有业务逻辑错误级别的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isFalseBusinessError(boolean condition) {
        return toAssert(!condition).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR);
    }

    /**
     * 断言条件为假
     *
     * @param condition 待判断的条件
     * @return 如果条件为假返回可构建的断言构建器，否则返回不可构建的断言构建器
     */
    public static AssertBuilder isFalse(boolean condition) {
        return toAssert(!condition);
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression        表达式布尔值
     * @param exceptionSupplier 异常提供者函数
     * @param message           错误消息模板
     * @param params            错误消息参数
     */
    public static void isTrue( boolean expression,
         Function<String, ? extends RuntimeException> exceptionSupplier,  String message,
        Object... params) {
        // 如果表达式为true，则抛出异常
        if (expression) {
            throwError(() -> exceptionSupplier.apply(StrUtil.format(message, params)));
        }
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression        布尔值提供者
     * @param exceptionSupplier 异常提供者函数
     * @param message           错误消息模板
     * @param params            错误消息参数
     */
    public static void isTrue( Supplier< Boolean> expression,
         Function<String, ? extends RuntimeException> exceptionSupplier,  String message,
        Object... params) {
        // 获取表达式的值，如果为true则抛出异常
        if (expression.get()) {
            throwError(() -> exceptionSupplier.apply(StrUtil.format(message, params)));
        }
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression        布尔表达式
     * @param exceptionSupplier 异常提供者
     */
    public static void isTrue(boolean expression, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(expression, (val) -> exceptionSupplier.get(), "");
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression 布尔表达式
     * @param message    错误消息模板
     * @param params     错误消息参数
     */
    public static void isTrue(boolean expression, String message, Object... params) {
        isTrue(expression, FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression 布尔值提供者
     * @param message    错误消息模板
     * @param params     错误消息参数
     */
    public static void isTrue( Supplier< Boolean> expression, String message, Object... params) {
        isTrue(expression.get(), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言对象非空，如果为空则抛出异常
     *
     * @param object            待检查的对象
     * @param exceptionSupplier 异常提供者函数
     * @param message           错误消息模板
     * @param params            错误消息参数
     */
    public static void notNull(@Nullable Object object,
         Function<String, ? extends RuntimeException> exceptionSupplier,  String message,
        Object... params) {
        // 如果对象为null，则抛出异常
        isTrue(object == null, exceptionSupplier, message, params);
    }

    /**
     * 断言对象非空，如果为空则抛出异常
     *
     * @param object            待检查的对象
     * @param exceptionSupplier 异常提供者
     */
    public static void notNull(@Nullable Object object,
         Supplier<? extends RuntimeException> exceptionSupplier) {
        notNull(object, (val) -> {
            return exceptionSupplier.get();
        }, "");
    }

    /**
     * 断言对象非空，如果为空则抛出异常
     *
     * @param object 待检查的对象
     * @param msg    错误消息
     * @param params 错误消息参数
     */
    public static void notNull(@Nullable Object object, String msg, Object... params) {
        notNull(object, FormattedRuntimeException::new, msg, params);
    }

    /**
     * 断言字符串非空且包含文本，如果为空或仅包含空白字符则抛出异常
     *
     * @param text    待检查的字符串
     * @param message 错误消息模板
     * @param params  错误消息参数
     */
    public static void hasText(@Nullable String text, String message, Object... params) {
        // 使用StrUtil.isEmpty判断字符串是否为空，如果为空则抛出异常
        isTrue(StrUtil.isEmpty(text), message, params);
    }

    /**
     * 断言迭代器非空，如果为空则抛出异常
     *
     * @param iterator 待检查的迭代器
     * @param message  错误消息模板
     * @param params   错误消息参数
     */
    public static void notEmpty(@Nullable Iterator<?> iterator, String message, Object... params) {
        // 使用CollUtil.isEmpty判断迭代器是否为空，如果为空则抛出异常
        isTrue(CollUtil.isEmpty(iterator), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言集合非空，如果为空则抛出异常
     *
     * @param collection 待检查的集合
     * @param message    错误消息模板
     * @param params     错误消息参数
     */
    public static void notEmpty(Collection<?> collection, String message, Object... params) {
        // 使用CollUtil.isEmpty判断集合是否为空，如果为空则抛出异常
        isTrue(CollUtil.isEmpty(collection), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言数组非空，如果为空则抛出异常
     *
     * @param array   待检查的数组
     * @param message 错误消息模板
     * @param params  错误消息参数
     * @param <T>     数组元素类型
     */
    public static <T> void notEmpty(@Nullable T[] array, String message, Object... params) {
        // 使用CollUtil.isEmpty判断数组是否为空，如果为空则抛出异常
        isTrue(CollUtil.of(array).isEmpty(), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言Map非空，如果为空则抛出异常
     *
     * @param map     待检查的Map
     * @param message 错误消息模板
     * @param params  错误消息参数
     */
    public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... params) {
        // 使用CollUtil.isEmpty判断Map是否为空，如果为空则抛出异常
        isTrue(CollUtil.isEmpty(map), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言对象非空并指定错误级别
     *
     * @param object     待检查的对象
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果对象非空返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder notNullWithLevel(@Nullable Object object, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(object != null).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言对象为空并指定错误级别
     *
     * @param object     待检查的对象
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果对象为空返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder isNullWithLevel(@Nullable Object object, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(object == null).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言字符串非空且包含文本并指定错误级别
     *
     * @param text       待检查的字符串
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果字符串非空且包含文本返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder hasTextWithLevel(@Nullable String text, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(!StrUtil.isEmpty(text)).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言集合非空并指定错误级别
     *
     * @param collection 待检查的集合
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果集合非空返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder notEmptyWithLevel(@Nullable Collection<?> collection, ErrorLevelEnum errorLevel,
        String message, Object... params) {
        return toAssert(!CollUtil.isEmpty(collection)).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言数组非空并指定错误级别
     *
     * @param array      待检查的数组
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @param <T>        数组元素类型
     * @return 如果数组非空返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static <T> AssertBuilder notEmptyWithLevel(@Nullable T[] array, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(!CollUtil.of(array).isEmpty()).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言对象相等并指定错误级别
     *
     * @param obj1       第一个对象
     * @param obj2       第二个对象
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果对象相等返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder equalsWithLevel(Object obj1, Object obj2, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(Objects.equals(obj1, obj2)).errorLevel(errorLevel).message(message).params(params);
    }

    /**
     * 断言对象不相等并指定错误级别
     *
     * @param obj1       第一个对象
     * @param obj2       第二个对象
     * @param errorLevel 错误级别枚举
     * @param message    错误消息模板
     * @param params     错误消息参数
     * @return 如果对象不相等返回不可构建的断言构建器，否则返回可构建的断言构建器
     */
    public static AssertBuilder notEqualsWithLevel(Object obj1, Object obj2, ErrorLevelEnum errorLevel, String message,
        Object... params) {
        return toAssert(!Objects.equals(obj1, obj2)).errorLevel(errorLevel).message(message).params(params);
    }

}
