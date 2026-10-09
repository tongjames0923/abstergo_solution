package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICreateTimeAuditable;
import com.tbs.shared.pojo.IUpdateTimeAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.ToString;

import jakarta.persistence.*;
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
@Comment("系统令牌表")
public class SystemTokenEntity implements IBaseEntity<Long>, ICreateTimeAuditable, IUpdateTimeAuditable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Comment("主键ID")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;
    /**
     * 令牌
     */
    @Comment("令牌字符串")
    @Column(nullable = false, length = 128, name = "token")
    private String token;
    /**
     * 用户名称
     */
    @Comment("用户名称")
    @Column(nullable = false, length = 128, name = "user_name")
    private String userName;
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
     * 标记
     */
    @Comment("令牌状态：1-有效，0-已删除，-1-已撤销，-2-已过期，-3-无效")
    private Integer flag;
    /**
     * 过期时间
     */
    @Comment("过期时间")
    @Column(name = "expire_time")
    private Date expireTime;

    /**
     * 登录设备的mac地址
     */
    @Comment("登录设备MAC地址")
    @Column(name = "mac_address", length = 128)
    private String macAddress;

    /**
     * 登录设备的ip 地址
     */
    @Comment("登录设备IP地址")
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Comment("令牌创建方式：1-登录，2-刷新替换，3-第三方登录")
    @Column(name = "create_type")
    private Integer createType;

    /**
     * 令牌类型
     *
     * @see com.tbs.web.enums.TokenTypeEnum
     */
    @Comment("令牌类型：1-访问令牌，2-刷新令牌，3-应用访问令牌，4-第三方访问令牌")
    @Column(name = "token_type", nullable = false)
    private Integer tokenType;

    /**
     * 会话id
     *
     */
    @Comment("会话ID")
    @Column(name = "session_id", length = 128)
    private String sessionId;
}
