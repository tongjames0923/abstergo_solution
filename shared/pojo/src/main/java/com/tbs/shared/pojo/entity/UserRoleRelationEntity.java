package com.tbs.shared.pojo.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.IBaseEntity;

import lombok.Data;

/**
 * 用户角色关系
 *
 * @author tongj
 */
@Table(name = "user_role_relation", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
		"role_id" }), indexes = { @Index(name = "idx_flag", columnList = "flag"),
				@Index(name = "idx_time", columnList = "create_time,update_time") })
@Entity
@Data
public class UserRoleRelationEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	private Long id;
	@javax.persistence.Column(name = "user_id", nullable = false)
	private Long userId;
	@javax.persistence.Column(name = "role_id", nullable = false)
	private Long roleId;
	@javax.persistence.Column(name = "create_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;
	@javax.persistence.Column(name = "update_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	@javax.persistence.Column(name = "flag")
	private Integer flag;
}
