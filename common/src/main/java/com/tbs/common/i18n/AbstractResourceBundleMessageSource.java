package com.tbs.common.i18n;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于ResourceBundle的消息源抽象实现
 * <p>
 * 提供基础的资源文件加载和消息格式化能力，不依赖Spring框架
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractResourceBundleMessageSource implements IMessageSource {

    /**
     * 资源文件缓存：key为basename_locale，value为ResourceBundle
     */
    private final Map<String, ResourceBundle> bundleCache = new ConcurrentHashMap<>();

    /**
     * 消息格式化缓存：key为code_locale，value为格式化后的消息
     */
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();

    /**
     * 获取资源文件的基础名称列表
     * <p>
     * 例如：["messages", "errors"]
     *
     * @return 基础名称数组
     */
    
    protected abstract String[] getBasenames();

    /**
     * 获取默认语言区域
     *
     * @return 默认Locale
     */
    
    protected abstract Locale getDefaultLocale();

    /**
     * 是否使用代码作为默认消息
     * <p>
     * 当找不到消息时，如果返回true则返回code本身，否则返回null
     *
     * @return 是否使用代码作为默认消息
     */
    protected abstract boolean isUseCodeAsDefaultMessage();

    
    @Override
    public String getMessage( String code) {
        return getMessage(code, null);
    }

    
    @Override
    public String getMessage( String code, @Nullable Object[] args) {
        return getMessage(code, args, (String) null);
    }

    
    @Override
    public String getMessage( String code, @Nullable Object[] args, @Nullable String defaultMessage) {
        return getMessage(code, args, defaultMessage, getDefaultLocale());
    }

    
    @Override
    public String getMessage( String code, @Nullable Object[] args,  Locale locale) {
        return getMessage(code, args, null, locale);
    }

    
    @Override
    public String getMessage( String code, @Nullable Object[] args, @Nullable String defaultMessage,  Locale locale) {
        // 尝试从缓存获取
        String cacheKey = code + "_" + locale.toString();
        if (args == null || args.length == 0) {
            String cached = messageCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        // 遍历所有basename查找消息
        String pattern = resolveMessage(code, locale);

        // 如果找不到消息，使用默认消息或代码
        if (pattern == null) {
            if (defaultMessage != null) {
                return defaultMessage;
            }
            return isUseCodeAsDefaultMessage() ? code : "";
        }

        // 格式化消息
        String result = formatMessage(pattern, args);

        // 缓存无参数的消息
        if (args == null || args.length == 0) {
            messageCache.put(cacheKey, result);
        }

        return result;
    }

    @Override
    public boolean hasMessage( String code) {
        return hasMessage(code, getDefaultLocale());
    }

    @Override
    public boolean hasMessage( String code,  Locale locale) {
        return resolveMessage(code, locale) != null;
    }

    /**
     * 解析消息内容
     *
     * @param code   消息代码
     * @param locale 语言区域
     * @return 消息模式，如果找不到返回null
     */
    @Nullable
    protected String resolveMessage( String code,  Locale locale) {
        for (String basename : getBasenames()) {
            ResourceBundle bundle = getResourceBundle(basename, locale);
            if (bundle != null) {
                try {
                    return bundle.getString(code);
                } catch (MissingResourceException ignored) {
                    // 继续查找下一个basename
                }
            }
        }
        return null;
    }

    /**
     * 获取ResourceBundle，带缓存
     *
     * @param basename 基础名称
     * @param locale   语言区域
     * @return ResourceBundle实例，如果找不到返回null
     */
    @Nullable
    protected ResourceBundle getResourceBundle( String basename,  Locale locale) {
        String cacheKey = basename + "_" + locale.toString();
        return bundleCache.computeIfAbsent(cacheKey, k -> loadResourceBundle(basename, locale));
    }

    /**
     * 加载ResourceBundle
     *
     * @param basename 基础名称
     * @param locale   语言区域
     * @return ResourceBundle实例，如果找不到返回null
     */
    @Nullable
    protected ResourceBundle loadResourceBundle( String basename,  Locale locale) {
        String resourceName = getResourceName(basename, locale);
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        } catch (IOException e) {
            log.warn("加载资源文件失败: {}", resourceName, e);
        }

        // 尝试不带国家代码的文件
        if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            String fallbackResourceName = getResourceName(basename, new Locale(locale.getLanguage()));
            try {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fallbackResourceName);
                if (is != null) {
                    try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        return new PropertyResourceBundle(reader);
                    }
                }
            } catch (IOException e) {
                log.warn("加载资源文件失败: {}", fallbackResourceName, e);
            }
        }

        return null;
    }

    /**
     * 获取资源文件名
     *
     * @param basename 基础名称
     * @param locale   语言区域
     * @return 资源文件名
     */
    
    protected String getResourceName( String basename,  Locale locale) {
        StringBuilder sb = new StringBuilder(basename.replace('.', '/'));
        if (!locale.getLanguage().isEmpty()) {
            sb.append('_').append(locale.getLanguage());
        }
        if (!locale.getCountry().isEmpty()) {
            sb.append('_').append(locale.getCountry());
        }
        sb.append(".properties");
        return sb.toString();
    }

    /**
     * 格式化消息
     *
     * @param pattern 消息模式
     * @param args    参数
     * @return 格式化后的消息
     */
    
    protected String formatMessage( String pattern, @Nullable Object[] args) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        bundleCache.clear();
        messageCache.clear();
    }
}
