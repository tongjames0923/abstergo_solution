package com.tbs.common.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 国际化配置属性
 * <p>
 * 配置示例：
 * <pre>
 * tbs:
 *   i18n:
 *     enabled: true
 *     basenames:
 *       - messages-common
 *       - messages-system
 *       - messages
 *     encoding: UTF-8
 *     default-locale: zh_CN
 *     cache-seconds: 3600
 *     use-code-as-default-message: true
 * </pre>
 *
 * @author abstergo
 */
@ConfigurationProperties(prefix = "tbs.i18n")
@Component
@Data
public class I18nProperty {

    /**
     * 是否启用国际化
     */
    private boolean enabled = true;

    /**
     * 消息资源文件基础名称列表
     * <p>
     * 每个模块使用独立的basename命名资源文件，避免冲突
     */
    private List<String> basenames = new ArrayList<>();

    /**
     * 消息资源文件编码
     */
    private String encoding = "UTF-8";

    /**
     * 默认语言区域（格式：language_COUNTRY，如 zh_CN）
     */
    private String defaultLocale = "zh_CN";

    /**
     * 消息缓存时间（秒），0表示不缓存，-1表示永久缓存
     */
    private int cacheSeconds = 3600;

    /**
     * 是否使用代码作为默认消息
     * <p>
     * 当找不到消息时，如果返回true则返回代码本身
     */
    private boolean useCodeAsDefaultMessage = true;
}