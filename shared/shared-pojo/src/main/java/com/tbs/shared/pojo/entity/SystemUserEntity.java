package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统用户
 *
 * @author abstergo
 */
@Table(name = "sys_user", indexes = { @Index(name = "idx_username_password", columnList = "user_name,password"),
		@Index(name = "idx_nickname", columnList = "nick_name"), @Index(name = "idx_flag", columnList = "flag"),
		@Index(name = "idx_time", columnList = "create_time,update_time") })
@Entity
@Getter
@Setter
@ToString
public class SystemUserEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;
	@Setter
	@Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	private Long id;
	/**
	 * 用户名
	 */
	@Column(name = "user_name", length = 72, unique = true, nullable = false)
	private String userName;
	/**
	 * 密码
	 */
	@Column(name = "password", length = 72, nullable = false)
	private String password;
	/**
	 * 昵称
	 */
	@Column(name = "nick_name", length = 32, nullable = false)
	private String nickName;
	/**
	 * 头像
	 */
	@Column(name = "avatar")
	private String avatar;

	/**
	 * 备注
	 */
	@Column(name = "remark")
	private String remark;

	/**
	 * 创建时间
	 */
	@Column(name = "create_time")
	private Date createTime;
	/**
	 * 修改时间
	 */
	@Column(name = "update_time")
	private Date updateTime;
	/**
	 * 状态
	 */
	@Column(name = "flag")
	private Integer flag;

}
