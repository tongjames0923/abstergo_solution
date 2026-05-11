
package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_department", indexes = { @Index(name = "idx_department_name", columnList = "department_name"),
		@Index(name = "idx_flag", columnList = "flag"), @Index(name = "idx_create_time", columnList = "create_time"),
    @Index(name = "idx_update_time", columnList = "update_time"),
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "tree_idx", columnList = "tree_node_group,tree_node_id,tree_root_flag")})
@ToString
@LogicDeleteEntity(filedName = "flag", activeFlagValues = {1, 2, 5})
@AllArgsConstructor
@NoArgsConstructor
/**
 * 系统部门实体类 用于表示系统中的部门信息，包含部门基本信息、状态及创建更新时间等字段
 */
public class SystemDepartmentEntity implements IBaseEntity<Long> {

	/**
	 * 部门ID
	 */
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 部门名称
	 */
	@Column(name = "department_name", length = 128, nullable = false)
	private String departmentName;

	/**
	 * 部门编码
	 */
	@Column(name = "department_code", length = 64, unique = true)
	private String departmentCode;

	/**
	 * 所属公司ID（一对一关联）
	 */
	@Column(name = "company_id", nullable = false)
	private Long companyId;

	/**
	 * 部门负责人
	 */
	@Column(name = "manager", length = 64)
	private String manager;

	/**
	 * 部门描述
	 */
	@Column(name = "description", columnDefinition = "text")
	private String description;

	/**
	 * 部门创建时间
	 */
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 部门更新时间
	 */
	@Column(name = "update_time")
    private Date updateTime;

	/**
	 * 数据状态
	 *
	 * @see com.tbs.shared.enums.SystemDepartmentFlagEnum
	 */
	@Column(name = "flag")
	private Integer flag;

    /**
     * 部门树节点分组
     */
    @Column(name = "tree_node_group", length = 64)
    private String treeNodeGroup;

    /**
     * 部门树节点ID
     */
    @Column(name = "tree_node_id", length = 64)
    private String treeNodeId;

    /**
     * 是否是树根节点
     */
    @Column(name = "tree_root_flag")
    private Integer treeRootFlag;

    /**
     * 子部门数量
     */
    @Transient
    private Long childrenCount;

}
