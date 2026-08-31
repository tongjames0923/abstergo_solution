package com.tbs.shared.pojo.entity.version;

import com.tbs.shared.pojo.ICommonAuditableEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.util.Date;

/**
 * 应用版本兼容策略表 用于定义源应用版本对目标应用的最低版本要求，实现跨应用联动更新
 *
 * @author abstergo
 */
@Data
@Entity
@Table(name = "app_version_compatibility_policy",
    indexes = {@Index(name = "idx_source", columnList = "source_app_key, source_version_code, source_platform"),
        @Index(name = "idx_target", columnList = "target_app_key, target_platform, flag"),
        // 可选：唯一索引防止重复策略，但需要注意 source_version_code 为 NULL 时唯一约束的数据库兼容性
        // @Index(name = "uk_source_target", columnList = "source_app_key, source_version_code, source_platform, target_app_key, target_platform", unique = true)
    })
public class AppVersionCompatibilityPolicyEntity implements ICommonAuditableEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 源应用标识（如 backend-service、web-console、mobile-app） 表示哪个应用发布的版本触发了该策略
     */
    @Comment("源应用标识（如 backend-service、web-console），表示哪个应用发布版本触发该策略")
    @Column(name = "source_app_key", length = 64, nullable = false)
    private String sourceAppKey;

    /**
     * 源应用版本号（可空） 为空表示该源应用所有版本均适用此策略； 不为空则只针对特定源版本生效
     */
    @Comment("源应用版本号（可空，为空表示所有版本均适用）")
    @Column(name = "source_version_code")
    private Long sourceVersionCode;

    /**
     * 源平台（可选） 如 ANDROID、IOS、WEB、BACKEND 等
     */
    @Comment("源平台（如 ANDROID、IOS、WEB、BACKEND）")
    @Column(name = "source_platform", length = 32)
    private String sourcePlatform;

    /**
     * 目标应用标识，表示对哪个应用提出要求
     */
    @Comment("目标应用标识（表示对哪个应用提出版本要求）")
    @Column(name = "target_app_key", length = 64, nullable = false)
    private String targetAppKey;

    /**
     * 目标平台（可选） 如 ANDROID、IOS、WEB 等
     */
    @Comment("目标平台（如 ANDROID、IOS、WEB）")
    @Column(name = "target_platform", length = 32)
    private String targetPlatform;

    /**
     * 目标应用必须满足的最低版本号 低于此版本将触发更新操作
     */
    @Comment("目标应用最低兼容版本号（低于此版本将触发更新）")
    @Column(name = "min_compat_version", nullable = false)
    private Long minCompatVersion;

    /**
     * 动作类型
     * @see  com.tbs.shared.enums.AppVersionCompatibilityActionEnum
     */
    @Comment("动作类型（不兼容时的处理策略）")
    @Column(name = "action", nullable = false)
    private Integer action;

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
     * 标志,排除删除标志，进行物理删除。
     *
     * @see com.tbs.shared.enums.CommonFlagEnum
     */
    @Comment("数据状态标志")
    @Column(name = "flag")
    private Integer flag;
}
