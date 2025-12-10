package com.tbs.knife4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * knife4j配置属性类
 * <p>
 * 该类定义了knife4j的基本配置属性，如是否启用knife4j、是否显示knife4j的url、文档标题、描述、版本号、许可证等
 * </p>
 *
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties(prefix = "tbs.knife4j")
public class Knife4jProperty {
    /**
     * 是否启用knife4j
     */
    private boolean enable = true;

    /**
     * 是否显示knife4j的url
     */
    private boolean showUrl = true;

    /**
     * 文档标题
     */
    private String title = "TBS 后端服务 API 文档";

    /**
     * 文档描述
     */
    private String description = "TBS 后端服务 API 文档";

    /**
     * 版本号
     */
    private String version = "v1.0.0";

    /**
     * 许可证名称
     */
    private String license = "Apache 2.0";

    /**
     * 许可证URL
     */
    private String licenseUrl = "http://springdoc.org";

    /**
     * 外部文档描述
     */
    private String externalDocsDescription = "TBS 解决方案文档";

    /**
     * 外部文档URL
     */
    private String externalDocsUrl = "https://github.com/abstergo/abstergo-solution";

    /**
     * 服务URL列表
     */
    private List<String> servers;

}
