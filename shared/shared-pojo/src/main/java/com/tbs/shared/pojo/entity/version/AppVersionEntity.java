package com.tbs.shared.pojo.entity.version;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonAuditableEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.Data;

import javax.persistence.*;
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
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * appKey
     */
    @Comment("应用唯一标识（如 backend-service、mobile-app）")
    @Column(name = "app_key", length = 64)
    private String appKey;

    /**
     * 内部版本号
     */
    @Comment("内部版本号（递增整数，用于版本比较）")
    @Column(name = "version_code")
    private Long versionCode;

    /**
     * 构建信息
     */
    @Comment("构建信息（JSON 格式的构建元数据）")
    @Column(name = "build_info", columnDefinition = "text")
    private String buildInfo;

    /**
     * 版本类型
     *
     * @see com.tbs.shared.enums.AppVersionTypeEnum
     */
    @Comment("版本类型（如正式版、测试版等）")
    @Column(name = "version_type")
    private Integer versionType;

    /**
     * 外部版本名称
     */
    @Comment("外部版本名称（用户可见的版本号，如 1.2.0）")
    @Column(name = "version_name", length = 64)
    private String versionName;

    /**
     * 平台
     */
    @Comment("目标平台（如 ANDROID、IOS、WEB）")
    @Column(name = "platform", length = 32)
    private String platform;

    /**
     * 更新日志
     */
    @Comment("版本更新日志（描述本次版本的变更内容）")
    @Column(name = "release_note", columnDefinition = "TEXT")
    private String releaseNote;

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
     * 标志
     *
     * @see com.tbs.shared.enums.AppVersionFlagEnum
     */
    @Comment("数据状态标志")
    @Column(name = "flag")
    private Integer flag;

    /**
     * 发布时间
     */
    @Comment("版本发布时间")
    @Column(name = "release_time")
    private Date releaseTime;

    /**
     * 下线时间
     */
    @Comment("版本下线时间")
    @Column(name = "offline_time")
    private Date offlineTime;

}
