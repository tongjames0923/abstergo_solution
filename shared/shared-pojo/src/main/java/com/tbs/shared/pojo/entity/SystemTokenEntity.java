package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统令牌
 *
 * @author abstergo
 */
@Table(name = "sys_token", indexes = {@Index(name = "ix_user", columnList = "user_name"),
    @Index(name = "ix_time", columnList = "create_time,update_time"), @Index(name = "ix_flag", columnList = "flag"),
    @Index(name = "ix_expire", columnList = "expire_time"), @Index(name = "ix_token_type", columnList = "token_type"),
    @Index(name = "ix_session", columnList = "session_id")},
    uniqueConstraints = {@UniqueConstraint(name = "uk_token", columnNames = {"token"})})
@Entity
@ToString
@Data
@LogicDeleteEntity(activeFlagValues = {1, -1, -2, -3}, filedName = "flag")
public class SystemTokenEntity implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;
    /**
     * 令牌
     */
    @Column(nullable = false, length = 128, name = "token")
    private String token;
    /**
     * 用户名称
     */
    @Column(nullable = false, length = 128, name = "user_name")
    private String userName;
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
     * 标记
     *
     * @see TokenStatusEnum
     */
    private Integer flag;
    /**
     * 过期时间
     */
    @Column(name = "expire_time")

    private Date expireTime;

    /**
     * 登录设备的mac地址
     */
    @Column(name = "mac_address", length = 128)
    private String macAddress;

    /**
     * 登录设备的ip 地址
     */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    /**
     * 创建方式
     *
     * @see TokenCreateTypeEnum
     */
    @Column(name = "create_type")
    private Integer createType;

    /**
     * 令牌类型
     *
     * @see com.tbs.web.enums.TokenTypeEnum
     */
    @Column(name = "token_type", nullable = false)
    private Integer tokenType;

    /**
     * 会话id
     *
     */
    @Column(name = "session_id", length = 128)
    private String sessionId;
}
