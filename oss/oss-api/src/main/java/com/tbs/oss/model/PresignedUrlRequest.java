package com.tbs.oss.model;

/**
 * 预签名URL请求，采用 Builder 构建
 */
public class PresignedUrlRequest {

    /** 桶名，为 null 时使用客户端默认桶 */
    private final String bucketName;

    /** 对象Key */
    private final String objectKey;

    /** 类型：下载或上传 */
    private final PresignedUrlType type;

    /** 有效期（秒） */
    private final long expirationSeconds;

    /** 内容类型，上传预签名时建议指定，可为 null */
    private final String contentType;

    private PresignedUrlRequest(Builder builder) {
        this.bucketName = builder.bucketName;
        this.objectKey = builder.objectKey;
        this.type = builder.type;
        this.expirationSeconds = builder.expirationSeconds;
        this.contentType = builder.contentType;
    }

    /**
     * 下载预签名URL
     */
    public static Builder download(String objectKey) {
        return new Builder(objectKey, PresignedUrlType.DOWNLOAD);
    }

    /**
     * 上传预签名URL（客户端直传）
     */
    public static Builder upload(String objectKey) {
        return new Builder(objectKey, PresignedUrlType.UPLOAD);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public PresignedUrlType getType() {
        return type;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String getContentType() {
        return contentType;
    }

    public static class Builder {

        private final String objectKey;
        private final PresignedUrlType type;
        private String bucketName;
        private long expirationSeconds = 3600;
        private String contentType;

        private Builder(String objectKey, PresignedUrlType type) {
            this.objectKey = objectKey;
            this.type = type;
        }

        public Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder expirationSeconds(long expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public PresignedUrlRequest build() {
            return new PresignedUrlRequest(this);
        }
    }
}
