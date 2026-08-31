package com.tbs.web.result;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;

/**
 * 错误处理 ajax 结果提供者
 * <p>
 * 将指定类型的异常转换为标准 {@link AjaxResult}，供全局异常处理器按 {@link Ordered} 顺序匹配使用。
 * </p>
 *
 * @param <T> 可处理的异常类型，需为 {@link Throwable} 子类
 * @author tongj
 */
public interface IOnErrorAjaxProvider<T extends Throwable> extends Ordered {
    /**
     * 将异常转换为 ajax 结果
     *
     * @param throwable 待处理的异常，不能为空
     * @return 转换后的统一响应结果
     */
    AjaxResult onError(T throwable);

    /**
     * 判断该提供者是否支持此异常类型
     *
     * @param clazz 异常类型，不能为空
     * @return 支持返回 {@code true}，否则 {@code false}
     */
    default boolean support( Class<? extends Throwable> clazz) {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
