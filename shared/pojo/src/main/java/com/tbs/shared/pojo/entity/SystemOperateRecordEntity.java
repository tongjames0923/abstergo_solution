package com.tbs.shared.pojo.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.IIdentifiable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tongj
 */
@Table(name = "sys_operate_record", indexes = { @Index(name = "idx_operate_time", columnList = "operate_time DESC"),
		@Index(name = "idx_user_time", columnList = "operate_user_name, operate_time DESC"),
		@Index(name = "idx_type_time", columnList = "operate_type, operate_time DESC"),
		@Index(name = "idx_operate_ip", columnList = "operate_ip"),
		@Index(name = "idx_time_user_type", columnList = "operate_time DESC, operate_user_name, operate_type") })
@Entity
@Getter
@Setter
@ToString
public class SystemOperateRecordEntity implements IIdentifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键
	 */
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	private Long id;
	/**
	 * 操作类型
	 */
	@javax.persistence.Column(name = "operate_type", length = 128)
	private String operateType;

	/**
	 * 操作内容,即调用的方法
	 */
	@javax.persistence.Column(name = "operate_content", length = 512)
	private String operateContent;
	/**
	 * 操作时间
	 */
	@javax.persistence.Column(name = "operate_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date operateTime;

	/**
	 * 操作用户名称
	 */
	@javax.persistence.Column(name = "operate_user_name", length = 64)
	private String operateUserName;
	/**
	 * 操作ip
	 */
	@javax.persistence.Column(name = "operate_ip", length = 64)
	private String operateIp;

	/**
	 * 操作方法 GET,PUT等操作方法类型
	 */
	@javax.persistence.Column(name = "method_type", length = 64)
	private String method;

	/**
	 * 操作url
	 */
	@javax.persistence.Column(name = "operate_url", length = 512)
	private String operateUrl;

	/**
	 * 操作参数json
	 */
	@javax.persistence.Column(name = "operate_param_json", columnDefinition = "text")
	private String operateParamJson;
	/**
	 * 操作结果json
	 */
	@javax.persistence.Column(name = "operate_result_json", columnDefinition = "text")
	private String operateResultJson;

	/**
	 * 操作耗时
	 */
	@javax.persistence.Column(name = "cost_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Long costTime;

	/**
	 * 操作异常信息
	 */
	@javax.persistence.Column(name = "operate_error", columnDefinition = "text")
	private String operateError;

}
