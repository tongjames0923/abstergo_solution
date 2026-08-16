package com.tbs.oss.model;

/**
 * OSS 连接配置属性，由 impl/starter 装配时注入
 */
public class OssProperties {

    /** 实现类型标识，如 aliyun / minio / aws-s3 / tencent-cos */
    private String type;

    /** 服务端点地址 */
    private String endpoint;

    /** 地域 */
    private String region;

    /** 访问密钥ID */
    private String accessKeyId;

    /** 访问密钥Secret */
    private String accessKeySecret;

    /** 默认桶名 */
    private String defaultBucket;

    /** 是否启用 path-style 访问（MinIO 等自建服务常用） */
    private boolean pathStyleAccess;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(String defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }
}
