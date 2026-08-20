package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * 权限表
 * @author tongj
 */
@Entity
@Table(name = "sys_role", indexes = {@Index(name = "idx_role_type", columnList = "role,type"),
		@Index(name = "idx_flag", columnList = "flag"),
	@Index(name = "idx_time", columnList = "create_time,update_time"), @Index(name = "idx_name", columnList = "name")})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, 2, 0})
public class SystemRoleEntity implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@Comment("主键ID")
	@Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	private Long id;

	/**
	 * 权限编码
	 */
	@Comment("权限编码（唯一标识）")
	@Column(name = "role", length = 128, nullable = false)
	private String role;

	/**
	 * 权限名称
	 */
	@Comment("权限名称")
	@Column(name = "name")
	private String name;

	/**
	 * 权限类型
	 *
	 * @see com.tbs.shared.enums.SystemRoleTypeEnum
	 */
	@Comment("权限类型")
	@Column(name = "type")
	private Integer type;

	/**
	 * 描述信息
	 */
	@Comment("描述信息")
	@Column(name = "description")
	private String description;

	/**
	 * 创建时间
	 */
	@Comment("创建时间")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 修改时间
	 */
	@Comment("修改时间")
	@Column(name = "update_time")
	private Date updateTime;

	/**
	 * 状态标识
	 *
	 * @see com.tbs.shared.enums.SystemRoleFlagEnum
	 */
	@Comment("状态标识")
	@Column(name = "flag")
	private Integer flag;

	/**
	 * 创建者用户ID
	 */
	@Comment("创建者用户ID")
	@Column(name = "create_by")
	private Long createBy;

	/**
	 * 更新者用户ID
	 */
	@Comment("更新者用户ID")
	@Column(name = "update_by")
	private Long updateBy;

	/**
	 * 数据权限范围
	 *
	 * @see com.tbs.shared.enums.SystemDataScopeEnum
	 */
	@Comment("数据权限范围")
	@Column(name = "data_scope")
	private Integer dataScope;

}
