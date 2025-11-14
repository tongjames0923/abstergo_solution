package com.tbs.common.error;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 断言工具类
 *
 * @author tbs
 */

@Component
public class AssertUtil {

    /**
     * 断言构建器内部类，用于构建格式化的运行时异常
     */
    public static final class AssertBuilder {

        private boolean isBuild = false;
        private String message;
        private List<Object> params = CollUtil.newArrayList();

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
        public FormattedRuntimeException build() {
            FormattedRuntimeException e = null;
            if (!this.isBuild) {
                return null;
            }
            // 根据是否有异常原因创建不同的FormattedRuntimeException实例
            if (this.cause != null) {
                e = new FormattedRuntimeException(this.cause, this.message, this.params.toArray());
            } else {
                e = new FormattedRuntimeException(this.message, this.params.toArray());
            }
            // 如果设置了错误等级，则设置异常的错误等级
            if (this.errorLevel != null) {
                e.errorLevel(this.errorLevel);
            }
            return e;
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
    private static List<IErrorThrowEvent> errorThrowEvents = new CopyOnWriteArrayList<>();

    @Resource
    private Map<String, IErrorThrowEvent> ets;

    /**
     * 初始化方法，在组件构造后执行，收集所有错误抛出事件处理器
     */
    @PostConstruct
    void init() {
        errorThrowEvents = ets.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

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
    public static void isTrue(@NotNull boolean expression,
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
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
    public static void isTrue(@NotNull Supplier<@NotNull Boolean> expression,
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
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
    public static void isTrue(@NotNull Supplier<@NotNull Boolean> expression, String message, Object... params) {
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
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
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
        @NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
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
        isTrue(CollUtil.isEmpty(array), FormattedRuntimeException::new, message, params);
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
}
