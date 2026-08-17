package com.tbs.oss.model;

import java.util.Date;

/**
 * 列表查询返回的对象摘要信息
 */
public class ObjectInfo {

    /** 所属桶名 */
    private String bucketName;

    /** 对象Key */
    private String objectKey;

    /** 内容长度（字节） */
    private long contentLength;

    /** 内容MD5/ETag */
    private String etag;

    /** 最后修改时间 */
    private Date lastModified;

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

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
