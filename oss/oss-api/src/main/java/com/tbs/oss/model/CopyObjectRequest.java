package com.tbs.oss.model;

/**
 * 复制对象请求，采用 Builder 构建
 */
public class CopyObjectRequest {

    /** 源桶名，为 null 时使用客户端默认桶 */
    private final String sourceBucketName;

    /** 源对象Key */
    private final String sourceObjectKey;

    /** 目标桶名，为 null 时使用客户端默认桶 */
    private final String targetBucketName;

    /** 目标对象Key */
    private final String targetObjectKey;

    private CopyObjectRequest(Builder builder) {
        this.sourceBucketName = builder.sourceBucketName;
        this.sourceObjectKey = builder.sourceObjectKey;
        this.targetBucketName = builder.targetBucketName;
        this.targetObjectKey = builder.targetObjectKey;
    }

    /**
     * 默认桶内的复制/移动
     */
    public static Builder builder(String sourceObjectKey, String targetObjectKey) {
        return new Builder(sourceObjectKey, targetObjectKey);
    }

    public String getSourceBucketName() {
        return sourceBucketName;
    }

    public String getSourceObjectKey() {
        return sourceObjectKey;
    }

    public String getTargetBucketName() {
        return targetBucketName;
    }

    public String getTargetObjectKey() {
        return targetObjectKey;
    }

    public static class Builder {

        private final String sourceObjectKey;
        private final String targetObjectKey;
        private String sourceBucketName;
        private String targetBucketName;

        private Builder(String sourceObjectKey, String targetObjectKey) {
            this.sourceObjectKey = sourceObjectKey;
            this.targetObjectKey = targetObjectKey;
        }

        public Builder sourceBucketName(String sourceBucketName) {
            this.sourceBucketName = sourceBucketName;
            return this;
        }

        public Builder targetBucketName(String targetBucketName) {
            this.targetBucketName = targetBucketName;
            return this;
        }

        public CopyObjectRequest build() {
            return new CopyObjectRequest(this);
        }
    }
}
