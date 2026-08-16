package com.tbs.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象列表查询结果，采用 marker 游标分页模型（兼容 S3/阿里云/MinIO 等主流实现）
 */
public class ObjectListing {

    /** 本页对象列表 */
    private List<ObjectInfo> objects = new ArrayList<>();

    /** 本页公共前缀（按 delimiter 查询时的"目录"） */
    private List<String> commonPrefixes = new ArrayList<>();

    /** 是否被截断（还有下一页） */
    private boolean truncated;

    /** 下一页游标，truncated 为 true 时有效 */
    private String nextMarker;

    public List<ObjectInfo> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectInfo> objects) {
        this.objects = objects;
    }

    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<String> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }
}
