package com.tbs.app.config;

import com.tbs.oss.downloadcode.OssDownloadCodeStorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 公开下载码配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.oss.download-code前缀的配置属性，
 * 集中管理下载码功能的配置项，存储实现类型由
 * {@link com.tbs.oss.downloadcode.OssDownloadCodeConfigKeys}，
 * 存储实现类型由实现层各配置类的{@code @ConditionalOnProperty}消费
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties("tbs.oss.download-code")
public class OssDownloadCodeProperty {

    /**
     * 下载码存储实现类型
     * <p>
     * 可选值：redis（Redis存储，缺省）、db（数据库存储），配置非法值时两个实现均不注册并在启动期报错
     * </p>
     */
    private OssDownloadCodeStorageType storage = OssDownloadCodeStorageType.REDIS;
}
