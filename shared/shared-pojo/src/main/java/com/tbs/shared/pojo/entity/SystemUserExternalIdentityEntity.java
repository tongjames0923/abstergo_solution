package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICommonUpdateAuditable;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 系统用户外部身份绑定
 * <p>
 * 承载第三方身份（如企业微信 userid）与系统用户的映射关系，
 * 供统一登录授权按外部身份解析系统用户；（identity_type, external_id）全局唯一。
 * </p>
 *
 * @author abstergo
 */
@Table(name = "sys_user_external_identity", indexes = {
	@Index(name = "idx_identity_type_external_id", columnList = "identity_type,external_id"),
	@Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_flag", columnList = "flag")})
@Entity
@Getter
@Setter
@ToString
@LogicDeleteEntity(activeFlagValues = { 1 })
public class SystemUserExternalIdentityEntity
	implements IBaseEntity<Long>, ICommonCreateAuditable<Long>, ICommonUpdateAuditable<Long> {
	private static final long serialVersionUID = 1L;

	/**
	 * 绑定ID
	 */
	@Setter
	@Column(name = "id")
	@Id
	@SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
	private Long id;

	/**
	 * 外部身份类型
	 *
	 * @see com.tbs.app.security.grant.wecom.WecomLoginGrantProvider#IDENTITY_TYPE
	 */
	@Column(name = "identity_type", length = 32, nullable = false)
	private String identityType;

	/**
	 * 外部身份标识
	 */
	@Column(name = "external_id", length = 128, nullable = false)
	private String externalId;

	/**
	 * 绑定的系统用户ID
	 */
	@Column(name = "user_id", nullable = false)
	private Long userId;

	/**
	 * 创建时间
	 */
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 修改时间
	 */
	@Column(name = "update_time")
	private Date updateTime;

	/**
	 * 绑定状态
	 */
	@Column(name = "flag")
	private Integer flag;

	/**
	 * 创建者用户ID
	 */
	@Column(name = "create_by")
	private Long createBy;

	/**
	 * 最后更新者用户ID
	 */
	@Column(name = "update_by")
	private Long updateBy;

}
