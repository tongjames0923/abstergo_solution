
package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
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
	@Index(name = "idx_update_time", columnList = "update_time")})
@ToString
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, 2, 5})
public class SystemCompanyEntity
	implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {

	/**
	 * 公司ID
	 */
	@Comment("公司ID（雪花算法生成）")
	@jakarta.persistence.Id
	@SqlAutoValue(insertOnly = true,value = IdAutoProvider.class)
	private Long id;

	/**
	 * 公司名称
	 */
	@Comment("公司名称")
	@Column(name = "company_name", length = 128, nullable = false)
	private String companyName;

	/**
	 * 公司logo url
	 */
	@Comment("公司Logo图片URL")
	@Column(name = "logo_image", length = 256)
	private String logoImage;

	/**
	 * 公司税号
	 */
	@Comment("公司税号")
	@Column(name = "tax_number", length = 64, nullable = false)
	private String taxNumber;

	/**
	 * 公司描述
	 */
	@Comment("公司描述")
	@Column(name = "description", columnDefinition = "text")
	private String description;

	/**
	 * 公司创建时间
	 */
	@Comment("创建时间")
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 公司更新时间
	 */
	@Comment("更新时间")
	@Column(name = "update_time")
    private Date updateTime;

	/**
	 * 数据状态
	 *
	 * @see com.tbs.shared.enums.SystemCompanyFlagEnum
	 */
	@Comment("数据状态")
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
