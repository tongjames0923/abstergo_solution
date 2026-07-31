package com.tbs.common.i18n;

import com.tbs.common.context.ContextPropagator;

import java.util.Locale;

/**
 * 国际化上下文传播器
 * <p>
 * 将父线程的 {@link Locale} 信息传播到线程池的工作线程中。
 * <p>
 * 解决的问题：
 * 在异步线程池场景下，{@link I18nContextHolder} 使用的是普通 ThreadLocal，
 * 工作线程无法继承父线程的 Locale 设置。本传播器通过在提交任务时快照 Locale，
 * 在工作线程执行前恢复、执行后清理，确保异步任务能正确使用请求的语言设置。
 *
 * @author abstergo
 * @see ContextPropagator
 * @see I18nContextHolder
 */
public class I18nContextPropagator implements ContextPropagator {

    @Override
    public Runnable wrap(Runnable task) {
        // 在调用线程（请求线程）中快照 Locale
        Locale locale = I18nContextHolder.getLocaleNullable();
        return () -> {
            try {
                // 在工作线程中恢复 Locale
                if (locale != null) {
                    I18nContextHolder.setLocale(locale);
                }
                task.run();
            } finally {
                // 清理，防止线程池复用时残留上一个请求的 Locale
                I18nContextHolder.clear();
            }
        };
    }
}
