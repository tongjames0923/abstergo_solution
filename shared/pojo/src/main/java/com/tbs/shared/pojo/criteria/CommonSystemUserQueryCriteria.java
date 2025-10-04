package com.tbs.shared.pojo.criteria;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.shared.serialize.QueryOrderedModelDeserializer;
import com.tbs.shared.serialize.QueryOrderedModelSerializer;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.order.impl.QueryOrderedModel;

import lombok.Getter;

/**
 * 系统用户通用查询条件
 * <p>
 * 提供对SystemUserEntity实体的各种查询条件支持，包括相等查询、模糊查询、范围查询和排序功能。
 *
 * @author abstergo
 */
@Getter
public class CommonSystemUserQueryCriteria extends ClassBaseQueryCriteria<SystemUserEntity> {
	/**
	 * 根据ID精确查询
	 */
	@QueryMapping(value = "id")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private String idEquals;

	/**
	 * 根据用户名精确查询
	 */
	@QueryMapping(value = "user_name")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private String userNameEquals;

	/**
	 * 根据昵称左模糊查询
	 */
	@QueryMapping(value = "nick_name")
	@QueryField(contrast = QueryContrastEnum.LLIKE)
	private String nickNameLike;

	/**
	 * 根据状态标志精确查询
	 */
	@QueryMapping(value = "flag")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private Integer flagEquals;

	/**
	 * 根据创建时间大于查询
	 */
	@QueryMapping(value = "create_time")
	@QueryField(contrast = QueryContrastEnum.GREATER)
	
	private Date createTimeGreaterThan;

	/**
	 * 根据创建时间小于查询
	 */
	@QueryMapping(value = "create_time")
	@QueryField(contrast = QueryContrastEnum.LESS)
	
	private Date createTimeLessThan;

	/**
	 * 根据更新时间大于查询
	 */
	@QueryMapping(value = "update_time")
	@QueryField(contrast = QueryContrastEnum.GREATER)
	
	private Date updateTimeGreaterThan;

	/**
	 * 根据更新时间小于查询
	 */
	@QueryMapping(value = "update_time")
	@QueryField(contrast = QueryContrastEnum.LESS)
	
	private Date updateTimeLessThan;

	/**
	 * 创建时间排序字段
	 */
	@QueryOrderField(mappedName = "create_time", order = OrderConstant.ORDERED_MODEL)
	@JsonSerialize(using = QueryOrderedModelSerializer.class)
	@JsonDeserialize(using = QueryOrderedModelDeserializer.class)
	private QueryOrderedModel createTimeOrder;

	/**
	 * 更新时间排序字段
	 */
	@QueryOrderField(mappedName = "update_time", order = OrderConstant.ORDERED_MODEL)
	@JsonSerialize(using = QueryOrderedModelSerializer.class)
	@JsonDeserialize(using = QueryOrderedModelDeserializer.class)
	private QueryOrderedModel updateTimeOrder;

	/**
	 * 设置ID精确查询条件
	 *
	 * @param idEquals ID值
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setIdEquals(String idEquals) {
		this.idEquals = idEquals;
		return this;
	}

	/**
	 * 设置用户名精确查询条件
	 *
	 * @param userNameEquals 用户名
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setUserNameEquals(String userNameEquals) {
		this.userNameEquals = userNameEquals;
		return this;
	}

	/**
	 * 设置昵称左模糊查询条件
	 *
	 * @param nickNameLike 昵称
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setNickNameLike(String nickNameLike) {
		this.nickNameLike = nickNameLike;
		return this;
	}

	/**
	 * 设置状态标志精确查询条件
	 *
	 * @param flagEquals 状态标志值
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setFlagEquals(Integer flagEquals) {
		this.flagEquals = flagEquals;
		return this;
	}

	/**
	 * 设置创建时间大于查询条件
	 *
	 * @param createTimeGreaterThan 创建时间下限
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setCreateTimeGreaterThan(Date createTimeGreaterThan) {
		this.createTimeGreaterThan = createTimeGreaterThan;
		return this;
	}

	/**
	 * 设置创建时间小于查询条件
	 *
	 * @param createTimeLessThan 创建时间上限
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setCreateTimeLessThan(Date createTimeLessThan) {
		this.createTimeLessThan = createTimeLessThan;
		return this;
	}

	/**
	 * 设置更新时间大于查询条件
	 *
	 * @param updateTimeGreaterThan 更新时间下限
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setUpdateTimeGreaterThan(Date updateTimeGreaterThan) {
		this.updateTimeGreaterThan = updateTimeGreaterThan;
		return this;
	}

	/**
	 * 设置更新时间小于查询条件
	 *
	 * @param updateTimeLessThan 更新时间上限
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setUpdateTimeLessThan(Date updateTimeLessThan) {
		this.updateTimeLessThan = updateTimeLessThan;
		return this;
	}

	/**
	 * 设置创建时间排序条件
	 *
	 * @param createTimeOrder 创建时间排序模型
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setCreateTimeOrder(QueryOrderedModel createTimeOrder) {
		this.createTimeOrder = createTimeOrder;
		return this;
	}

	/**
	 * 设置更新时间排序条件
	 *
	 * @param updateTimeOrder 更新时间排序模型
	 * @return 当前查询条件对象，用于链式调用
	 */
	public CommonSystemUserQueryCriteria setUpdateTimeOrder(QueryOrderedModel updateTimeOrder) {
		this.updateTimeOrder = updateTimeOrder;
		return this;
	}
}
