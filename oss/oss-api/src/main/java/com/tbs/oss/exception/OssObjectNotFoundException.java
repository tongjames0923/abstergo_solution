package com.tbs.oss.exception;

/**
 * 对象不存在异常
 */
public class OssObjectNotFoundException extends OssException {

    public OssObjectNotFoundException(String bucketName, String objectKey) {
        super("oss object not found: " + objectKey, bucketName, objectKey, null);
    }

    public OssObjectNotFoundException(String bucketName, String objectKey, Throwable cause) {
        super("oss object not found: " + objectKey, bucketName, objectKey, cause);
    }
}
