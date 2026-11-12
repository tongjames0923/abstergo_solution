package com.tbs.shared.pojo.entity.version;

import com.tbs.shared.pojo.ICommonAuditableEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * app版本信息
 *
 * @author abstergo
 */
@Data
@Entity
@Table(name = "app_version_info",
    indexes = {@Index(columnList = "app_key,version_code"), @Index(columnList = "version_name"),
        @Index(columnList = "version_type,platform"), @Index(columnList = "flag")})
public class AppVersionEntity implements ICommonAuditableEntity<Long> {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * appKey
     */
    @Column(name = "app_key", length = 64)
    private String appKey;

    /**
     * 内部版本号
     */
    @Column(name = "version_code")
    private Long versionCode;

    /**
     * 构建信息
     */
    @Column(name = "build_info", columnDefinition = "text")
    private String buildInfo;

    /**
     * 版本类型
     *
     * @see com.tbs.shared.enums.AppVersionTypeEnum
     */
    @Column(name = "version_type")
    private Integer versionType;

    /**
     * 外部版本名称
     */
    @Column(name = "version_name", length = 64)
    private String versionName;

    /**
     * 平台
     */
    @Column(name = "platform", length = 32)
    private String platform;

    /**
     * 更新日志
     */
    @Column(name = "release_note", columnDefinition = "TEXT")
    private String releaseNote;

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
     * 标志
     *
     * @see com.tbs.shared.enums.AppVersionFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

    /**
     * 发布时间
     */
    @Column(name = "release_time")
    private Date releaseTime;

    /**
     * 下线时间
     */
    @Column(name = "offline_time")
    private Date offlineTime;

}
