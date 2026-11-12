package com.tbs.shared.pojo.entity.version;

import com.tbs.shared.pojo.ICommonAuditableEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * 应用安装包实体
 *
 * <p>存储应用版本的安装包文件信息，包括 OSS 存储位置和下载地址。</p>
 *
 * @author abstergo
 * @see AppVersionEntity
 */
@Entity
@Data
@Table(name = "app_package", indexes = {@jakarta.persistence.Index(name = "idx_version_id", columnList = "version_id")})
public class AppPackageEntity implements ICommonAuditableEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 应用版本id
     */
    @jakarta.persistence.Column(name = "version_id")
    private Long versionId;

    /**
     * 桶名
     */
    @Column(name = "bucket_name")
    private String bucketName;

    /**
     * 对象名
     */
    @Column(name = "object_name")
    private String objectName;

    /**
     * md5值
     */
    @Column(name = "md5")
    private String md5;

    /**
     * 下载地址
     */
    @Column(name = "download_url")
    private String downloadUrl;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 标记位
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;
}
