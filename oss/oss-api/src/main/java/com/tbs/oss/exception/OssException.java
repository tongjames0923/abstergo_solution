package com.tbs.oss.exception;

/**
 * OSS 统一异常，所有实现需将第三方 SDK 异常转换为此异常抛出
 */
public class OssException extends RuntimeException {

    /** 操作涉及的桶名，可能为 null */
    private final String bucketName;

    /** 操作涉及的对象Key，可能为 null */
    private final String objectKey;

    public OssException(String message) {
        this(message, null, null, null);
    }

    public OssException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public OssException(String message, String bucketName, String objectKey, Throwable cause) {
        super(message, cause);
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
