package com.tbs.oss.model;

/**
 * 对象列表查询请求，采用 Builder 构建
 */
public class ListObjectsRequest {

    /** 桶名，为 null 时使用客户端默认桶 */
    private final String bucketName;

    /** Key前缀过滤，可为 null */
    private final String prefix;

    /** 分页游标，首页传 null */
    private final String marker;

    /** 单页最大条数 */
    private final int maxKeys;

    /** 分隔符（模拟目录层级时传 "/"），可为 null */
    private final String delimiter;

    private ListObjectsRequest(Builder builder) {
        this.bucketName = builder.bucketName;
        this.prefix = builder.prefix;
        this.marker = builder.marker;
        this.maxKeys = builder.maxKeys;
        this.delimiter = builder.delimiter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMarker() {
        return marker;
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public static class Builder {

        private String bucketName;
        private String prefix;
        private String marker;
        private int maxKeys = 100;
        private String delimiter;

        public Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder marker(String marker) {
            this.marker = marker;
            return this;
        }

        public Builder maxKeys(int maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        public Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public ListObjectsRequest build() {
            return new ListObjectsRequest(this);
        }
    }
}
