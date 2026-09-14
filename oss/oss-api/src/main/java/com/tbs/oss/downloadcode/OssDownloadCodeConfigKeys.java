package com.tbs.oss.downloadcode;

/**
 * 下载码配置键常量
 * <p>
 * 集中收拢下载码功能的配置键，供{@code @ConditionalOnProperty}与
 * {@code @ConfigurationProperties}引用，避免魔法字符串散落
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class OssDownloadCodeConfigKeys {

    /**
     * 配置前缀：tbs.oss.download-code
     */
    public static final String CONFIG_PREFIX = "tbs.oss.download-code";

    /**
     * 存储实现类型配置项：storage
     */
    public static final String STORAGE_PROPERTY = "storage";

    private OssDownloadCodeConfigKeys() {
    }
}
