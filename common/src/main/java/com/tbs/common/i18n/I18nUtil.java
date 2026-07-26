package com.tbs.common.i18n;

import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

/**
 * 国际化工具类
 * <p>
 * 提供静态方法获取国际化消息，自动集成Spring MessageSource（如果可用）
 * <p>
 * 使用方式：
 * <pre>
 * // 简单消息
 * String msg = I18nUtil.get("user.not.found");
 *
 * // 带参数的消息
 * String msg = I18nUtil.get("validation.min.length", new Object[]{8});
 *
 * // 指定语言
 * String msg = I18nUtil.get("user.not.found", Locale.ENGLISH);
 * </pre>
 *
 * @author abstergo
 */
@Slf4j
public final class I18nUtil {

    private static final String DEFAULT_MESSAGE_SOURCE_BEAN_NAME = "messageSource";

    /**
     * Spring MessageSource实例（延迟加载）
     */
    private static volatile MessageSource springMessageSource;

    /**
     * 自定义消息源实例（可通过setCustomMessageSource设置）
     */
    private static volatile IMessageSource customMessageSource;

    private I18nUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息代码
     * @return 消息内容
     */
    
    public static String get( String code) {
        return get(code, (Object[])null);
    }

    /**
     * 获取国际化消息（带参数）
     *
     * @param code 消息代码
     * @param args 消息参数
     * @return 消息内容
     */
    
    public static String get( String code, @Nullable Object... args) {
        return get(code, args, I18nContextHolder.getLocale());
    }

    /**
     * 获取国际化消息（指定语言）
     *
     * @param code   消息代码
     * @param locale 语言区域
     * @return 消息内容
     */
    
    public static String get( String code,  Locale locale) {
        return get(code, null, locale);
    }

    /**
     * 获取国际化消息（带参数，指定语言）
     *
     * @param code   消息代码
     * @param args   消息参数
     * @param locale 语言区域
     * @return 消息内容
     */
    
    public static String get( String code, @Nullable Object[] args,  Locale locale) {
        // 优先使用自定义消息源
        IMessageSource custom = customMessageSource;
        if (custom != null) {
            return custom.getMessage(code, args, code, locale);
        }

        // 尝试使用Spring MessageSource
        MessageSource spring = getSpringMessageSource();
        if (spring != null) {
            return spring.getMessage(code, args, code, locale);
        }

        // 都不可用时返回代码本身
        log.debug("没有可用的MessageSource，返回消息代码: {}", code);
        return code;
    }

    /**
     * 获取国际化消息（带默认消息）
     *
     * @param code           消息代码
     * @param args           消息参数
     * @param defaultMessage 默认消息
     * @return 消息内容
     */
    
    public static String get( String code, @Nullable Object[] args, @Nullable String defaultMessage) {
        // 优先使用自定义消息源
        IMessageSource custom = customMessageSource;
        if (custom != null) {
            return custom.getMessage(code, args, defaultMessage, I18nContextHolder.getLocale());
        }

        // 尝试使用Spring MessageSource
        MessageSource spring = getSpringMessageSource();
        if (spring != null) {
            return spring.getMessage(code, args, defaultMessage, I18nContextHolder.getLocale());
        }

        // 都不可用时返回默认消息或代码
        return defaultMessage != null ? defaultMessage : code;
    }

    /**
     * 判断是否存在指定代码的消息
     *
     * @param code 消息代码
     * @return 是否存在
     */
    public static boolean has( String code) {
        IMessageSource custom = customMessageSource;
        if (custom != null) {
            return custom.hasMessage(code, I18nContextHolder.getLocale());
        }

        try {
            String message = get(code);
            return !message.equals(code);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Spring MessageSource实例（延迟加载）
     *
     * @return MessageSource实例，如果不存在返回null
     */
    @Nullable
    private static MessageSource getSpringMessageSource() {
        if (springMessageSource == null) {
            synchronized (I18nUtil.class) {
                if (springMessageSource == null) {
                    try {
                        springMessageSource = SpringUtil.getBean(MessageSource.class);
                    } catch (Exception e) {
                        log.debug("Spring MessageSource不可用", e);
                    }
                }
            }
        }
        return springMessageSource;
    }

    /**
     * 设置自定义消息源
     * <p>
     * 如果不使用Spring，可以通过此方法设置自定义的IMessageSource实现
     *
     * @param messageSource 自定义消息源
     */
    public static void setCustomMessageSource( IMessageSource messageSource) {
        customMessageSource = messageSource;
        log.info("设置自定义MessageSource: {}", messageSource.getClass().getName());
    }

    /**
     * 获取当前使用的自定义消息源
     *
     * @return 自定义消息源，如果未设置返回Optional.empty()
     */
    
    public static Optional<IMessageSource> getCustomMessageSource() {
        return Optional.ofNullable(customMessageSource);
    }

    /**
     * 清除Spring MessageSource缓存
     * <p>
     * 用于测试或重新初始化场景
     */
    public static void clearSpringMessageSourceCache() {
        springMessageSource = null;
    }

    /**
     * 获取当前Locale
     *
     * @return 当前Locale
     */
    
    public static Locale getCurrentLocale() {
        return I18nContextHolder.getLocale();
    }

    /**
     * 设置当前Locale
     *
     * @param locale 语言区域
     */
    public static void setCurrentLocale( Locale locale) {
        I18nContextHolder.setLocale(locale);
    }
}
