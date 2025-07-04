package com.tbs.common.util;

import cn.hutool.core.util.ArrayUtil;
import com.tbs.common.IErrorThrowEvent;
import com.tbs.common.exception.FormattedRuntimeException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
     * 链式断言构建器
     */
    public static class AssertBuilder {

        /**
         * 创建断言条件
         *
         * @param condition
         * @return
         */
        public static Condition that(Supplier<Boolean> condition) {
            return new Condition(condition);
        }

        /**
         * 链式断言条件
         */
        public static class Condition {
            private final Supplier<Boolean> condition;

            /**
             * 创建条件
             *
             * @param condition
             */
            public Condition(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            /**
             * 断言为真则抛出异常
             *
             * @param message
             * @param params
             */
            public void orElseThrow(String message, Object... params) {
                AssertUtil.isTrue(condition, FormattedRuntimeException::new, message, params);
            }
        }
    }

    @Getter
    private static List<IErrorThrowEvent> errorThrowEvents = new CopyOnWriteArrayList<>();

    @Resource
    private Map<String, IErrorThrowEvent> ets;

    @PostConstruct
    void init() {
        errorThrowEvents = ets.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    /**
     * 抛出异常
     *
     * @param exceptionSupplier
     */
    public static <T extends RuntimeException> void throwError(Supplier<T> exceptionSupplier) {

        var e = exceptionSupplier.get();
        if (errorThrowEvents.stream().anyMatch(event -> event.handle(e))) {
            return;
        }
        throw e;
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression
     * @param message
     * @param params
     */
    public static void isTrue(@NotNull boolean expression,
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
        Object... params) {
        if (expression) {
            throwError(() -> exceptionSupplier.apply(StrUtil.format(message, params)));
        }
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression
     * @param message
     * @param params
     */

    public static void isTrue(@NotNull Supplier<@NotNull Boolean> expression,
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
        Object... params) {
        if (expression.get()) {
            throwError(() -> exceptionSupplier.apply(StrUtil.format(message, params)));
        }
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression
     * @param exceptionSupplier
     */
    public static void isTrue(boolean expression, Supplier<? extends RuntimeException> exceptionSupplier) {
        isTrue(expression, (val) -> exceptionSupplier.get(), "");
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression
     * @param message
     * @param params
     */
    public static void isTrue(boolean expression, String message, Object... params) {
        isTrue(expression, FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言为真则抛出指定异常
     *
     * @param expression
     * @param message
     * @param params
     */
    public static void isTrue(@NotNull Supplier<@NotNull Boolean> expression, String message, Object... params) {
        isTrue(expression.get(), FormattedRuntimeException::new, message, params);
    }

    /**
     * 断言对象非空
     */
    public static void notNull(@Nullable Object object,
        @NotNull Function<String, ? extends RuntimeException> exceptionSupplier, @NotNull String message,
        Object... params) {
        isTrue(object == null, exceptionSupplier, message, params);
    }

    /**
     * 断言对象非空
     */
    public static void notNull(@Nullable Object object,
        @NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        notNull(object, (val) -> {
            return exceptionSupplier.get();
        }, "");
    }

    public static void notNull(@Nullable Object object, String msg, Object... params) {
        notNull(object, FormattedRuntimeException::new, msg, params);
    }

    /**
     * 断言字符串非空
     */
    public static void hasText(@Nullable String text, String message, Object... params) {
        isTrue(StrUtil.isEmpty(text), message, params);
    }

    /**
     * 迭代器非空
     */
    public static void notEmpty(@Nullable Iterator<?> iterator, String message, Object... params) {
        isTrue(CollUtil.isEmpty(iterator), FormattedRuntimeException::new, message, params);
    }

    /**
     * 集合非空
     */
    public static void notEmpty(Collection<?> collection, String message, Object... params) {
        isTrue(CollUtil.isEmpty(collection), FormattedRuntimeException::new, message, params);
    }

    /**
     * 数组非空
     */
    public static <T> void notEmpty(@Nullable T[] array, String message, Object... params) {
        isTrue(CollUtil.isEmpty(array), FormattedRuntimeException::new, message, params);
    }

    /**
     * map非空
     */
    public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... params) {
        isTrue(CollUtil.isEmpty(map), FormattedRuntimeException::new, message, params);
    }
}
