package com.tbs.oss.exception;

/**
 * 桶不存在异常
 */
public class OssBucketNotFoundException extends OssException {

    public OssBucketNotFoundException(String bucketName) {
        super("oss bucket not found: " + bucketName, bucketName, null, null);
    }

    public OssBucketNotFoundException(String bucketName, Throwable cause) {
        super("oss bucket not found: " + bucketName, bucketName, null, cause);
    }
}
