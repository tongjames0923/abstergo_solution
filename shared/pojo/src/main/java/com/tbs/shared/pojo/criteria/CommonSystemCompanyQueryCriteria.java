package com.tbs.shared.pojo.criteria;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.shared.serialize.QueryOrderedModelDeserializer;
import com.tbs.shared.serialize.QueryOrderedModelSerializer;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.order.impl.QueryOrderedModel;

import lombok.Data;

/**
 * 系统公司实体查询条件类 用于构建针对SystemCompanyEntity实体的查询条件，支持多种查询方式和排序
 *
 * @author tongj
 */
@Data
public class CommonSystemCompanyQueryCriteria extends ClassBaseQueryCriteria<SystemCompanyEntity> {
	/**
	 * 按ID精确查询
	 */
	@QueryField
	@QueryMapping(value = "id")
	private Long equalsId;

	/**
	 * 按公司名称模糊查询（左模糊）
	 */
	@QueryField(contrast = QueryContrastEnum.LLIKE)
	@QueryMapping(value = "company_name")
	private String likeName;

	/**
	 * 按税号精确查询
	 */
	@QueryField
	@QueryMapping(value = "tax_number")
	private String equalsTaxNumber;

	/**
	 * 按状态标记IN查询（支持多个状态）
	 */
	@QueryField(contrast = QueryContrastEnum.IN)
	@QueryMapping(value = "flag")
	private List<Integer> flagEquals;

	/**
	 * 按创建时间范围查询（起始时间 >=）
	 */
	@QueryMapping("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
	private Date createTimeStart;

	/**
	 * 按创建时间范围查询（结束时间 <=）
	 */
	@QueryMapping("create_time")
	@QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTimeEnd;

	/**
	 * 按更新时间范围查询（起始时间 >=）
	 */
	@QueryMapping("update_time")
	@QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTimeStart;

	/**
	 * 按更新时间范围查询（结束时间 <=）
	 */
	@QueryMapping("update_time")
	@QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTimeEnd;

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
}
