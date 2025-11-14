package com.tbs.web.result;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;

/**
 * 错误处理ajax结果提供者
 *
 * @author tongj
 */
public interface IOnErrorAjaxProvider<T extends Throwable> extends Ordered {
    /**
     * 错误处理ajax结果提供者
     *
     * @param throwable
     * @return
     */
    AjaxResult onError(T throwable);

    default boolean support(@NotNull Class<? extends Throwable> clazz) {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
