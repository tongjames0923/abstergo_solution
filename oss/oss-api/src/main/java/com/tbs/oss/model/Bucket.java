package com.tbs.oss.model;

import java.util.Date;

/**
 * 桶信息
 */
public class Bucket {

    /** 桶名 */
    private String name;

    /** 创建时间，实现不支持时为 null */
    private Date creationDate;

    /** 桶所在地域，实现不支持时为 null */
    private String region;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
