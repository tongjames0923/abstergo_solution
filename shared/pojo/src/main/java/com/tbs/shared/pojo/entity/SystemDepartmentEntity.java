package com.tbs.shared.pojo.entity;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.IBaseEntity;

import lombok.Data;

@Table(name = "system_department", indexes = { @Index(name = "idx_parent_id", columnList = "parent_id"),
		@Index(name = "idx_path", columnList = "tree_path"), @Index(name = "idx_create_time", columnList = "create_time"),
		@Index(name = "idx_flag", columnList = "flag") })
@Entity
@Data
public class SystemDepartmentEntity implements IBaseEntity<Long> {
	private static final long serialVersionUID = 1L;

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
	 * 物化路径，存储从根节点到当前节点的完整路径 格式：/root_id/child_id/grandchild_id/ 例如：/1/5/12/
	 * 表示根部门ID为1，子部门ID为5，孙部门ID为12
	 */
	@Column(name = "tree_path", length = 512)
	private String treePath;

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

}
