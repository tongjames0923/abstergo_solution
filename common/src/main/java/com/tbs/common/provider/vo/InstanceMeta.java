package com.tbs.common.provider.vo;

import lombok.Getter;

import java.io.Serializable;

/**
 * 实例元数据
 */
@Getter
public class InstanceMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务线
     */
    private String business;

    /**
     * 版本
     */
    private String version;

    /**
     * 类全限定名
     */
    private String classFullName;

    public InstanceMeta setBusiness(String business) {
        this.business = business;
        return this;
    }

    public InstanceMeta setVersion(String version) {
        this.version = version;
        return this;
    }

    public InstanceMeta setClassFullName(String classFullName) {
        this.classFullName = classFullName;
        return this;
    }
}
