package com.tbs.oss.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象元数据，与具体 OSS 厂商 SDK 解耦的统一模型
 */
public class ObjectMetadata {

    /** 内容长度（字节） */
    private long contentLength;

    /** 内容类型 */
    private String contentType;

    /** 内容MD5/ETag */
    private String etag;

    /** 最后修改时间 */
    private Date lastModified;

    /** 扩展用户自定义元数据 */
    private Map<String, String> userMetadata = new HashMap<>();

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, String> userMetadata) {
        this.userMetadata = userMetadata;
    }
}
