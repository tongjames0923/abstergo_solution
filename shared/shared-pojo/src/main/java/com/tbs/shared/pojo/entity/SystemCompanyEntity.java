
package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 公司信息
 *
 * @author abstergo
 */
@Data
@Entity
@Table(name = "sys_company", indexes = { @Index(name = "idx_company_name", columnList = "company_name"),
		@Index(name = "idx_flag", columnList = "flag"), @Index(name = "idx_create_time", columnList = "create_time"),
	@Index(name = "idx_update_time", columnList = "update_time"),})
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SystemCompanyEntity
	implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

	/**
	 * 公司ID
	 */
	@javax.persistence.Id
	@SqlAutoValue(insertOnly = true,value = IdAutoProvider.class)
	private Long id;

	/**
	 * 公司名称
	 */
	@Column(name = "company_name", length = 128, nullable = false)
	private String companyName;

	/**
	 * 公司logo url
	 */
	@Column(name = "logo_image", length = 256)
	private String logoImage;

	/**
	 * 公司税号
	 */
	@Column(name = "tax_number", length = 64, unique = true, nullable = false)
	private String taxNumber;

	/**
	 * 公司描述
	 */
	@Column(name = "description", columnDefinition = "text")
	private String description;

	/**
	 * 公司创建时间
	 */
	@Column(name = "create_time")

	private Date createTime;

	/**
	 * 公司更新时间
	 */
	@Column(name = "update_time")

    private Date updateTime;

	/**
	 * 数据状态
	 *
	 * @see com.tbs.shared.enums.SystemCompanyFlagEnum
	 */
	@Column(name = "flag")
	private Integer flag;

	/**
     * 子公司数量
     */
    @Transient
    private Long childrenCount;

	/**
	 * 创建者
	 */
	@Column(name = "create_by")
	private Long createBy;

	/**
	 * 更新者
	 */
	@Column(name = "update_by")
	private Long updateBy;

}
