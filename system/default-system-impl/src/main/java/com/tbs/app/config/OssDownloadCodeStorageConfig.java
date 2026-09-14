package com.tbs.app.config;

import com.tbs.app.service.impl.oss.downloadcode.DbDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeConfigKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 下载码数据库存储装配配置
 * <p>
 * 按{@code tbs.oss.download-code.storage=db}条件注册数据库存储实现；
 * 条件经ConfigurationClassParser评估，注册与评估语义由Spring官方机制保证
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
public class OssDownloadCodeStorageConfig {

    /**
     * 注册下载码数据库存储实现
     *
     * @return 下载码数据库存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = OssDownloadCodeConfigKeys.CONFIG_PREFIX,
        name = OssDownloadCodeConfigKeys.STORAGE_PROPERTY, havingValue = "db")
    public DbDownloadCodeStorage dbDownloadCodeStorage() {
        return new DbDownloadCodeStorage();
    }
}
