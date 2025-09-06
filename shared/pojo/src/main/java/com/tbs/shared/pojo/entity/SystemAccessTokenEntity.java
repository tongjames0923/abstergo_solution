package com.tbs.shared.pojo.entity;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.IBaseEntity;

import com.tbs.web.enums.AccessTokenCreateTypeEnum;
import lombok.Data;
import lombok.ToString;

@Table(name = "sys_access_token", indexes = { @Index(name = "ix_user", columnList = "user_name"),
		@Index(name = "ix_time", columnList = "create_time,update_time"), @Index(name = "ix_flag", columnList = "flag"),
		@Index(name = "ix_expire", columnList = "expire_time") }, uniqueConstraints = {
				@UniqueConstraint(name = "uk_token", columnNames = { "token" }) })
@Entity
@ToString
@Data
public class SystemAccessTokenEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;
	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	/**
	 * 标记
	 *
	 * @see com.tbs.shared.enums.AccessTokenFlagEnum
	 */
	private Integer flag;
	/**
	 * 过期时间
	 */
	@Column(name = "expire_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
	 * @see AccessTokenCreateTypeEnum
	 */
	@Column(name = "create_type")
	private Integer createType;
}
