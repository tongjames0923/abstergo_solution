package com.tbs.oss.model;

/**
 * 上传结果
 */
public class PutObjectResult {

    /** 对象所在桶名 */
    private String bucketName;

    /** 对象Key */
    private String objectKey;

    /** 内容MD5/ETag */
    private String etag;

    /** 对象访问URL（实现支持时返回，否则为 null） */
    private String url;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
