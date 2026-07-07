package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * 用户角色关系实体类
 *
 * <p>该实体映射 user_role_relation 表，用于维护用户与角色之间的多对多关联关系。</p>
 * <p>通过 user_id 和 role_id 的唯一约束，确保同一用户-角色组合不会重复。
 * 表上建立了 flag 索引和 create_time/update_time 复合索引以优化查询性能。</p>
 *
 * @author tongj
 */
@Table(name = "user_role_relation", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
		"role_id" }), indexes = { @Index(name = "idx_flag", columnList = "flag"),
				@Index(name = "idx_time", columnList = "create_time,update_time") })
@Entity
@Data
@ToString
public class UserRoleRelationEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;
	@javax.persistence.Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	/**
	 * 主键ID，由系统自动生成，插入时自动填充
	 */
	private Long id;
	@javax.persistence.Column(name = "user_id", nullable = false)
	/**
	 * 用户ID，关联用户表的主键，不可为空
	 */
	private Long userId;
	@javax.persistence.Column(name = "role_id", nullable = false)
	/**
	 * 角色ID，关联角色表的主键，不可为空
	 */
	private Long roleId;
	@javax.persistence.Column(name = "create_time")
	/**
	 * 记录创建时间
	 */
	private Date createTime;
	@javax.persistence.Column(name = "update_time")
	/**
	 * 记录最后更新时间
	 */
	private Date updateTime;
	@javax.persistence.Column(name = "flag")
	/**
	 * 状态标记位，用于标识该关系记录的有效性或逻辑删除状态
	 */
	private Integer flag;
}
