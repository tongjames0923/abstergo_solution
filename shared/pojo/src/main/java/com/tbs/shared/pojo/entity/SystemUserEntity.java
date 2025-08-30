package com.tbs.shared.pojo.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.tbs.shared.pojo.IBaseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统用户
 *
 * @author abstergo
 */
@Table(name = "sys_user", indexes = { @Index(name = "idx_username_password", columnList = "user_name,password"),
		@Index(name = "idx_nickname", columnList = "nick_name"), @Index(name = "idx_flag", columnList = "flag"),
		@Index(name = "idx_department_id", columnList = "department_id"),
		@Index(name = "idx_time", columnList = "create_time,update_time") })
@Entity
@Getter
@Setter
@ToString
public class SystemUserEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;
	@Setter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	/**
	 * 部门id
	 */
	@Column(name = "department_id")
	private Long departmentId;
	/**
	 * 角色
	 */
	@Transient
	private List<SystemRoleEntity> roles;

}
