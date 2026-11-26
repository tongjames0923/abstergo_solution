package com.tbs.shared.pojo.entity.oss;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.AutoSetDate;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * OSS公开下载码实体
 * <p>
 * 对应表oss_download_code；下载码按code全局唯一，
 * 删除语义走flag逻辑删除，查询由{@code com.tbs.sql.interceptor.SqlLogicDeleteIntercepter}自动过滤已删除记录
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Entity
@Table(name = "oss_download_code",
    indexes = {@Index(name = "idx_code", columnList = "code"), @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(filedName = "flag", activeFlagValues = {1})
public class OssDownloadCodeEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法自动生成）
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 下载唯一码（4~64位URL安全Base64字符，全局唯一）
     */
    @Column(name = "code", length = 64, nullable = false)
    private String code;

    /**
     * 存储桶名
     */
    @Column(name = "bucket", length = 255)
    private String bucket;

    /**
     * 存储对象键
     */
    @Column(name = "object_key", length = 512, nullable = false)
    private String objectKey;

    /**
     * 过期时间戳（毫秒），小于等于0表示永不过期
     */
    @Column(name = "expire_at")
    private Long expireAt;

    /**
     * 备注说明
     */
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    @AutoSetDate
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    @AutoSetDate
    private Date updateTime;

    /**
     * 逻辑删除标志
     *
     * @see com.tbs.shared.enums.OssDownloadCodeFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;
}
