package com.tbs.oss.model;

import com.tbs.oss.core.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 上传请求参数，通过 Builder 构建
 */
public class PutObjectRequest {

    /** 桶名，为 null 时使用客户端默认桶 */
    private final String bucketName;

    /** 对象Key */
    private final String objectKey;

    /** 上传内容流，与 file/content 三选一 */
    private final InputStream inputStream;

    /** 上传本地文件，与 inputStream/content 三选一 */
    private final File file;

    /** 上传字节内容，与 inputStream/file 三选一 */
    private final byte[] content;

    /** 内容长度（字节），流上传时建议显式指定，-1 表示未知 */
    private final long contentLength;

    /** 内容类型 */
    private final String contentType;

    /** 用户自定义元数据 */
    private final Map<String, String> userMetadata;

    /** 进度监听器，可为 null */
    private final ProgressListener progressListener;

    private PutObjectRequest(Builder builder) {
        this.bucketName = builder.bucketName;
        this.objectKey = builder.objectKey;
        this.inputStream = builder.inputStream;
        this.file = builder.file;
        this.content = builder.content;
        this.contentLength = builder.contentLength;
        this.contentType = builder.contentType;
        this.userMetadata = builder.userMetadata;
        this.progressListener = builder.progressListener;
    }

    public static Builder builder(String objectKey) {
        return new Builder(objectKey);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public File getFile() {
        return file;
    }

    public byte[] getContent() {
        return content;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public static class Builder {

        private final String objectKey;
        private String bucketName;
        private InputStream inputStream;
        private File file;
        private byte[] content;
        private long contentLength = -1;
        private String contentType;
        private Map<String, String> userMetadata = new HashMap<>();
        private ProgressListener progressListener;

        private Builder(String objectKey) {
            this.objectKey = objectKey;
        }

        public Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder file(File file) {
            this.file = file;
            return this;
        }

        public Builder content(byte[] content) {
            this.content = content;
            return this;
        }

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder userMetadata(Map<String, String> userMetadata) {
            this.userMetadata = userMetadata;
            return this;
        }

        public Builder progressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        public PutObjectRequest build() {
            return new PutObjectRequest(this);
        }
    }
}
