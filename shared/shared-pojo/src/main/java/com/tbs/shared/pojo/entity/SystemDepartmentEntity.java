
package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统部门实体类 用于表示系统中的部门信息，包含部门基本信息、状态及创建更新时间等字段
 */
@Data
@Entity
@Table(name = "sys_department",
	indexes = {@Index(name = "idx_name_code", columnList = "department_name,department_code"),
		@Index(name = "idx_flag", columnList = "flag"), @Index(name = "idx_create_time", columnList = "create_time"),
    @Index(name = "idx_update_time", columnList = "update_time"),
    @Index(name = "idx_company_id", columnList = "company_id"),
		@Index(name = "idx_parent_id", columnList = "parent_id")})
@ToString
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, 2})
public class SystemDepartmentEntity
	implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

	/**
	 * 部门ID
	 */
	@Comment("部门ID（雪花算法生成）")
	@javax.persistence.Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	private Long id;

	/**
	 * 部门名称
	 */
	@Comment("部门名称")
	@Column(name = "department_name", length = 128, nullable = false)
	private String departmentName;

	/**
	 * 部门编码
	 */
	@Comment("部门编码")
	@Column(name = "department_code", length = 64, unique = false)
	private String departmentCode;

	/**
	 * 所属公司ID
	 */
	@Comment("所属公司ID")
	@Column(name = "company_id", nullable = false)
	private Long companyId;

	/**
	 * 部门负责人
	 */
	@Comment("部门负责人姓名")
	@Column(name = "manager", length = 64)
	private String manager;

	/**
	 * 部门描述
	 */
	@Comment("部门描述")
	@Column(name = "description", columnDefinition = "text")
	private String description;

	/**
	 * 部门创建时间
	 */
	@Comment("创建时间")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 部门更新时间
	 */
	@Comment("更新时间")
	@Column(name = "update_time")
    private Date updateTime;

	/**
	 * 数据状态
	 *
	 * @see com.tbs.shared.enums.SystemDepartmentFlagEnum
	 */
	@Comment("数据状态")
	@Column(name = "flag")
	private Integer flag;


	/**
     * 是否是树根节点
     */
	@Transient
    private Integer treeRootFlag;

	/**
	 * 父部门ID
	 */
	@Comment("父部门ID（用于构建树形结构）")
	@Column(name = "parent_id")
	private Long parentId;

    /**
     * 子部门数量
     */
    @Transient
    private Long childrenCount;

	/**
	 * 创建者
	 */
	@Comment("创建者用户ID")
	@Column(name = "create_by")
	private Long createBy;

	/**
	 * 更新者
	 */
	@Comment("更新者用户ID")
	@Column(name = "update_by")
	private Long updateBy;

}
