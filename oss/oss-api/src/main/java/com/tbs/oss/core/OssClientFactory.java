package com.tbs.oss.core;

import com.tbs.oss.model.OssProperties;

/**
 * OSS 客户端工厂 SPI。
 * <p>
 * 每种第三方 OSS 实现（aliyun/minio/aws-s3 等）提供一个工厂实现，
 * 由 oss-api 的装配逻辑根据 {@link OssProperties#getType()} 选择对应工厂创建客户端。
 */
public interface OssClientFactory {

    /** 工厂支持的实现类型标识，与 {@link OssProperties#getType()} 匹配 */
    String supportType();

    /** 根据配置创建客户端 */
    OssClient create(OssProperties properties);
}
