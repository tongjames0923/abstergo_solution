package com.tbs.redis.oss;

import com.tbs.oss.downloadcode.OssDownloadCodeConfigKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 下载码Redis存储装配配置（Spring默认Bean名取短类名，故与DB存储配置类区分命名）
 * <p>
 * 按{@code tbs.oss.download-code.storage=redis}（或缺省）条件注册Redis存储实现；
 * 条件经ConfigurationClassParser评估，注册与评估语义由Spring官方机制保证
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
public class RedisDownloadCodeStorageConfig {

    /**
     * 注册下载码Redis存储实现
     *
     * @return 下载码Redis存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = OssDownloadCodeConfigKeys.CONFIG_PREFIX,
        name = OssDownloadCodeConfigKeys.STORAGE_PROPERTY, havingValue = "redis", matchIfMissing = true)
    public RedisDownloadCodeStorage redisDownloadCodeStorage() {
        return new RedisDownloadCodeStorage();
    }
}
