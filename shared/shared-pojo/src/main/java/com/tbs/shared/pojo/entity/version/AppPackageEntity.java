package com.tbs.shared.pojo.entity.version;

import com.tbs.shared.pojo.ICommonAuditableEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Comment("应用安装包表")
public class AppPackageEntity implements ICommonAuditableEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 应用版本id
     */
    @Comment("关联的应用版本ID（援引 app_version_info.id）")
    @jakarta.persistence.Column(name = "version_id")
    private Long versionId;

    /**
     * 桶名
     */
    @Comment("OSS 存储桶名称")
    @Column(name = "bucket_name")
    private String bucketName;

    /**
     * 对象名
     */
    @Comment("OSS 对象名称（文件路径）")
    @Column(name = "object_name")
    private String objectName;

    /**
     * md5值
     */
    @Comment("安装包文件 MD5 校验值")
    @Column(name = "md5")
    private String md5;

    /**
     * 下载地址
     */
    @Comment("安装包下载地址 URL")
    @Column(name = "download_url")
    private String downloadUrl;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Comment("更新时间")
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @Comment("创建者用户ID")
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @Comment("更新者用户ID")
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 标记位
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("数据状态标志：1-启用，-1-禁用，0-删除")
    @Column(name = "flag")
    private Integer flag;
}
