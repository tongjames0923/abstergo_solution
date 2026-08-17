package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用安装包视图
 * <p>
 * 面向管理端展示的安装包信息，包含下载地址与完整性校验信息
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppPackageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 安装包ID
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
     * 安装包文件 md5 值
     */
    private String md5;

    /**
     * 下载地址
     */
    private String downloadUrl;

    /**
     * 创建时间
     */
    private Date createTime;
}
