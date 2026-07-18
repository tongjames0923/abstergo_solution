package com.tbs.common.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * 国际化上下文持有者
 * <p>
 * 用于在当前线程中保存和获取Locale信息
 * <p>
 * 典型使用场景：
 * 1. 在请求入口处设置Locale（如从HTTP Header中获取）
 * 2. 在业务代码中获取当前Locale
 * 3. 在请求结束时清除Locale
 *
 * @author abstergo
 */
public final class I18nContextHolder {

    private static final ThreadLocal<Locale> LOCALE_HOLDER = new ThreadLocal<>();

    /**
     * 默认语言区域
     */
    private static Locale defaultLocale = Locale.CHINA;

    private I18nContextHolder() {
        // 工具类，禁止实例化
    }

    /**
     * 设置当前线程的语言区域
     *
     * @param locale 语言区域，如果为null则清除当前设置
     */
    public static void setLocale(Locale locale) {
        if (locale == null) {
            LOCALE_HOLDER.remove();
        } else {
            LOCALE_HOLDER.set(locale);
        }
    }

    /**
     * 获取当前线程的语言区域
     * <p>
     * 如果当前线程没有设置，则返回默认语言区域
     *
     * @return 当前语言区域
     */
    @NotNull
    public static Locale getLocale() {
        Locale locale = LOCALE_HOLDER.get();
        return locale != null ? locale : defaultLocale;
    }

    /**
     * 获取当前线程的语言区域（可能为null）
     *
     * @return 当前语言区域，如果未设置则返回null
     */
    public static Locale getLocaleNullable() {
        return LOCALE_HOLDER.get();
    }

    /**
     * 清除当前线程的语言区域设置
     */
    public static void clear() {
        LOCALE_HOLDER.remove();
    }

    /**
     * 设置默认语言区域
     *
     * @param locale 默认语言区域
     */
    public static void setDefaultLocale(@NotNull Locale locale) {
        defaultLocale = locale;
    }

    /**
     * 获取默认语言区域
     *
     * @return 默认语言区域
     */
    @NotNull
    public static Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * 判断当前线程是否设置了语言区域
     *
     * @return 是否设置了语言区域
     */
    public static boolean hasLocale() {
        return LOCALE_HOLDER.get() != null;
    }
}