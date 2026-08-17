package com.tbs.oss.core;

import com.tbs.oss.model.OssProperties;

/**
 * OSS 统一门面，业务代码只依赖此接口。
 * <p>
 * 使用示例：
 * <pre>
 *     ossClient.putObject("report/2026/a.xlsx", file);
 *     String url = ossClient.generatePresignedUrl("report/2026/a.xlsx", 3600);
 * </pre>
 * @author abstergo
 */
public interface OssClient extends OssOperations, BucketOperations, AutoCloseable {

    /** 获取当前实现类型标识，如 aliyun / minio / aws-s3 / tencent-cos */
    String getType();

    /** 获取配置的默认桶名，未配置时返回 null */
    String getDefaultBucket();

    /** 获取连接配置属性 */
    OssProperties getProperties();

    /** 释放底层 SDK 客户端资源 */
    @Override
    void close();
}
