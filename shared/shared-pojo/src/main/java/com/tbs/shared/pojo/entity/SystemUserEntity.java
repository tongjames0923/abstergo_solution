package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.shared.pojo.IDeptBelongEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@LogicDeleteEntity(activeFlagValues = {1, 2, 0})
public class SystemUserEntity
	implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long>, IDeptBelongEntity<Long> {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@Setter
	@Column(name = "id")
	@Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	private Long id;

	/**
	 * 用户名
	 */
	@Column(name = "user_name", length = 72, nullable = false)
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
	 * 账号状态
	 *
	 * @see com.tbs.shared.enums.SystemUserStatusEnum
	 */
	@Column(name = "flag")
	private Integer flag;

	/**
	 * 创建者用户ID
	 */
	@Column(name = "create_by")
	private Long createBy;

	/**
	 * 最后更新者用户ID
	 */
	@Column(name = "update_by")
	private Long updateBy;

	/**
	 * 主要所属部门ID
	 */
	@Column(name = "dept_id")
	private Long deptId;

}
