package com.tbs.shared.pojo.entity;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.pojo.IBaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author abstergo
 */
@Table(name = "sys_department", indexes = { @Index(name = "idx_parent_id", columnList = "parent_id"),
		@Index(name = "idx_create_time", columnList = "create_time"), @Index(name = "idx_flag", columnList = "flag") })
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemDepartmentEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;

	public static final Long NULL_PARENT_ID = 0L;

	/**
	 * 根部门
	 */
	public static SystemDepartmentEntity ROOT = new SystemDepartmentEntity(1L, "系统", NULL_PARENT_ID, "系统根部门", null,
			null, null, CommonConstant.FLAG_ON, null);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 部门名称
	 */
	@Column(name = "department_name", length = 128, nullable = false)
	private String departmentName;

	/**
	 * 父部门ID
	 */
	@Column(name = "parent_id")
	private Long parentId;

	/**
	 * 部门描述
	 */
	@Column(name = "description", columnDefinition = "text")
	private String description;

	/**
	 * 排序字段
	 */
	@Column(name = "sort_order")
	private Integer sortOrder;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "update_time")
	private Date updateTime;

	/**
	 * 状态标记：1-正常，0-删除
	 */
	@Column(name = "flag")
	private Integer flag = 1;

	/**
	 * 子部门数量
	 */
	@Transient
	private Integer childrenCount;

}
