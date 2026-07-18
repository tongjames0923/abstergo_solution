package com.tbs.common.config;

import com.tbs.common.config.property.I18nProperty;
import com.tbs.common.i18n.I18nContextHolder;
import com.tbs.common.i18n.IMessageSourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 国际化自动配置
 * <p>
 * 支持两种方式配置 basename：
 * 1. 配置文件方式：在 tbs.i18n.basenames 中配置
 * 2. Bean注册方式：实现 IMessageSourceProvider 接口并注册为 Spring Bean
 * <p>
 * 两种方式可以同时使用，最终会合并所有 basename。
 * <p>
 * 配置示例：
 * <pre>
 * tbs:
 *   i18n:
 *     enabled: true
 *     # 可选：通过配置文件添加额外的 basename
 *     basenames:
 *       - messages-app
 * </pre>
 * <p>
 * Bean注册示例：
 * <pre>
 * &#64;Component
 * &#64;Order(100)
 * public class SystemModuleMessageSourceProvider extends AbstractMessageSourceProvider {
 *     &#64;Override
 *     public String getBasename() {
 *         return "messages-system";
 *     }
 * }
 * </pre>
 *
 * @author abstergo
 * @see IMessageSourceProvider
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "tbs.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class I18nAutoConfiguration {

    private final I18nProperty i18nProperty;

    private List<IMessageSourceProvider> providers = new ArrayList<>();

    public I18nAutoConfiguration(I18nProperty i18nProperty) {
        this.i18nProperty = i18nProperty;
        // 初始化默认Locale
        initDefaultLocale();
    }

    /**
     * 注入所有 IMessageSourceProvider 实现
     */
    @Autowired(required = false)
    public void setProviders(List<IMessageSourceProvider> providers) {
        this.providers = providers != null ? providers : new ArrayList<>();
    }

    /**
     * 配置消息源
     *
     * @return MessageSource 实例
     */
    @Bean
    @ConditionalOnMissingBean(MessageSource.class)
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        // 合并所有 basename（配置文件 + Bean注册）
        List<String> allBasenames = resolveAllBasenames();

        if (allBasenames.isEmpty()) {
            // 如果未配置basename，使用默认值
            allBasenames = new ArrayList<>();
            allBasenames.add("messages");
            log.warn("未配置 tbs.i18n.basenames 且无 IMessageSourceProvider，使用默认值: {}", allBasenames);
        }

        // 设置basename
        String[] basenameArray = allBasenames.toArray(new String[0]);
        messageSource.setBasenames(basenameArray);
        log.info("配置国际化消息源，basenames: {}", allBasenames);

        // 设置编码
        messageSource.setDefaultEncoding(i18nProperty.getEncoding());

        // 设置默认语言
        Locale defaultLocale = parseLocale(i18nProperty.getDefaultLocale());
        messageSource.setDefaultLocale(defaultLocale);

        // 设置是否使用代码作为默认消息
        messageSource.setUseCodeAsDefaultMessage(i18nProperty.isUseCodeAsDefaultMessage());

        // 设置缓存时间
        messageSource.setCacheSeconds(i18nProperty.getCacheSeconds());

        return messageSource;
    }

    /**
     * 解析所有 basename（合并配置文件和 Bean 注册）
     * <p>
     * 合并规则：
     * 1. 先添加 Bean 注册的 basename（按优先级排序）
     * 2. 再添加配置文件中的 basename
     * 3. 去重（保留第一次出现的位置）
     *
     * @return 所有 basename 列表
     */
    private List<String> resolveAllBasenames() {
        Set<String> seen = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();

        // 1. 收集 Bean 注册的 basename（按优先级排序）
        List<IMessageSourceProvider> enabledProviders = providers.stream()
            .filter(IMessageSourceProvider::isEnabled)
            .sorted(Comparator.comparingInt(IMessageSourceProvider::getOrder))
            .collect(Collectors.toList());

        for (IMessageSourceProvider provider : enabledProviders) {
            String basename = provider.getBasename();
            if (basename != null && !basename.isEmpty() && seen.add(basename)) {
                result.add(basename);
                log.debug("从 {} 注册 basename: {}", provider.getClass().getSimpleName(), basename);
            }
        }

        // 2. 添加配置文件中的 basename
        List<String> configBasenames = i18nProperty.getBasenames();
        if (configBasenames != null) {
            for (String basename : configBasenames) {
                if (basename != null && !basename.isEmpty() && seen.add(basename)) {
                    result.add(basename);
                    log.debug("从配置文件注册 basename: {}", basename);
                }
            }
        }

        return result;
    }

    /**
     * 初始化默认Locale
     */
    private void initDefaultLocale() {
        String defaultLocaleStr = i18nProperty.getDefaultLocale();
        if (defaultLocaleStr != null && !defaultLocaleStr.isEmpty()) {
            Locale locale = parseLocale(defaultLocaleStr);
            I18nContextHolder.setDefaultLocale(locale);
            log.info("设置默认语言: {}", locale);
        }
    }

    /**
     * 解析Locale字符串
     *
     * @param localeStr Locale字符串，格式：language_COUNTRY（如 zh_CN）
     * @return Locale对象
     */
    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.CHINA;
        }

        String[] parts = localeStr.replace('-', '_').split("_");
        if (parts.length >= 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return new Locale(parts[0]);
        }

        return Locale.CHINA;
    }
}
