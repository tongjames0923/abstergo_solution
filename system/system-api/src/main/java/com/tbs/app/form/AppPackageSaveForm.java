package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用安装包保存表单
 * <p>
 * 用于在指定应用版本下登记安装包产物，
 * 下载地址与存储信息（桶名/对象名）二选一提供，由服务层解析最终下载地址
 *
 * @author abstergo
 */
@Data
public class AppPackageSaveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 安装包ID，编辑时必传
     */
    private Long id;

    /**
     * 所属应用版本ID
     */
    private Long versionId;

    /**
     * 对象存储桶名
     */
    private String bucketName;

    /**
     * 对象存储对象名
     */
    private String objectName;

    /**
     * 安装包文件 md5 值，用于下载完整性校验
     */
    private String md5;

    /**
     * 直接下载地址
     */
    private String downloadUrl;
}
