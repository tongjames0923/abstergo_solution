package com.tbs.oss.downloadcode;

/**
 * 下载码存储实现类型枚举
 * <p>
 * 用于指定公开下载码的持久化实现方式，取值经
 * {@code @ConditionalOnProperty}（配置键见 {@link OssDownloadCodeConfigKeys}）消费
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum OssDownloadCodeStorageType {

    /**
     * Redis存储（Redisson，缺省实现，需注意淘汰策略对永不过期码的影响）
     */
    REDIS,

    /**
     * 数据库存储（表oss_download_code，逻辑删除，持久化最稳妥）
     */
    DB
}
