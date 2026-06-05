package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author tongj
 */
@Entity
@Table(name = "sys_role", indexes = {@Index(name = "idx_role", columnList = "role"),
		@Index(name = "idx_flag", columnList = "flag"),
		@Index(name = "idx_time", columnList = "create_time,update_time") })
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, 2, 0})
public class SystemRoleEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;

	/**
	 * 角色
	 */
	@Column(name = "role", length = 128, nullable = false)
	private String role;
	/**
	 * 描述
	 */
	@Column(name = "description")
	private String description;
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
	 * @see com.tbs.shared.enums.SystemRoleFlagEnum
	 */
	@Column(name = "flag")
	private Integer flag;
}
