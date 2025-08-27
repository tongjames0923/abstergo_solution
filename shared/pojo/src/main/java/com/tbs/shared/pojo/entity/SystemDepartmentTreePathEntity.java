package com.tbs.shared.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author abstergo
 */
@Data
@Entity
@Table(name = "department_tree_path")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SystemDepartmentTreePathEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 根节点
	 */
	public static final SystemDepartmentTreePathEntity ROOT_PATH = new SystemDepartmentTreePathEntity(1L, 0L, "/");

	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 父级ID
	 */
	@Column(name = "parent_id", nullable = false)
	private Long parentId;

	/**
	 * 路径
	 */
	@Column(name = "tree_path", nullable = false)
	private String treePath;
}
