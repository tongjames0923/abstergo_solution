package com.tbs.shared.pojo.criteria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.shared.serialize.QueryOrderedModelDeserializer;
import com.tbs.shared.serialize.QueryOrderedModelSerializer;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.order.impl.QueryOrderedModel;
import com.tbs.sql.query.impl.DefaultEntityClassBaseQueryCriteria;
import lombok.Getter;

import java.util.Date;

/**
 * 通用令牌查询条件类
 * <p>
 * 该类用于构建针对系统令牌(SystemTokenEntity)的查询条件，支持基于令牌、用户名、标识、令牌类型等字段的精确查询， 以及基于创建时间和过期时间的范围查询。同时支持按创建时间和过期时间进行排序。
 * </p>
 * <p>
 * 该类使用了基于注解的查询映射机制，通过@QueryMapping和@QueryField定义查询字段与数据库字段的映射关系及比较条件， 通过@QueryOrderField定义排序字段。
 * </p>
 *
 * @author abstergo
 */
@Getter
public class CommonTokenQueryCriteria extends DefaultEntityClassBaseQueryCriteria {
    /**
     * 等值匹配的令牌字段 对应数据库中的token字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "token")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String tokenEquals;

    /**
     * 等值匹配的用户名字段 对应数据库中的user_name字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "user_name")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String userNameEquals;

    /**
     * 等值匹配的标识字段 对应数据库中的flag字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer flagEquals;

    /**
     * 等值匹配的令牌类型字段 对应数据库中的token_type字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "token_type")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer tokenTypeEquals;

    /**
     * 等值匹配的会话ID字段 对应数据库中的session_id字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "session_id")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String sessionIdEquals;

    /**
     * 创建时间范围查询的起始时间字段 对应数据库中的create_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("create_time")
    
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date createTimeAfter;

    /**
     * 过期时间范围查询的起始时间字段 对应数据库中的expire_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("expire_time")
    
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date expireTimeAfter;

    /**
     * 过期时间范围查询的结束时间字段 对应数据库中的expire_time字段，使用LESS_EQUAL比较条件
     */
    @QueryMapping("expire_time")
    
    @QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    private Date expireTimeBefore;

    /**
     * 创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstant.ORDERED_MODEL)
    @JsonSerialize(using = QueryOrderedModelSerializer.class)
    @JsonDeserialize(using = QueryOrderedModelDeserializer.class)
    private QueryOrderedModel createTimeOrdered;

    /**
     * 过期时间排序
     */
    @QueryOrderField(mappedName = "expire_time", order = OrderConstant.ORDERED_MODEL)
    @JsonSerialize(using = QueryOrderedModelSerializer.class)
    @JsonDeserialize(using = QueryOrderedModelDeserializer.class)
    private QueryOrderedModel expireTimeOrdered;

    /**
     * 设置创建时间排序模型
     *
     * @param createTimeOrdered 创建时间排序模型
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setCreateTimeOrdered(QueryOrderedModel createTimeOrdered) {
        this.createTimeOrdered = createTimeOrdered;
        return this;
    }

    /**
     * 设置过期时间排序模型
     *
     * @param expireTimeOrdered 过期时间排序模型
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setExpireTimeOrdered(QueryOrderedModel expireTimeOrdered) {
        this.expireTimeOrdered = expireTimeOrdered;
        return this;
    }

    /**
     * 设置等值匹配的令牌字段
     *
     * @param tokenEquals 用于等值匹配的令牌值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setTokenEquals(String tokenEquals) {
        this.tokenEquals = tokenEquals;
        return this;
    }

    /**
     * 设置等值匹配的用户名字段
     *
     * @param userNameEquals 用于等值匹配的用户名值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setUserNameEquals(String userNameEquals) {
        this.userNameEquals = userNameEquals;
        return this;
    }

    /**
     * 设置等值匹配的标识字段
     *
     * @param flagEquals 用于等值匹配的标识值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setFlagEquals(Integer flagEquals) {
        this.flagEquals = flagEquals;
        return this;
    }

    /**
     * 设置等值匹配的令牌类型字段
     *
     * @param tokenTypeEquals 用于等值匹配的令牌类型值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setTokenTypeEquals(Integer tokenTypeEquals) {
        this.tokenTypeEquals = tokenTypeEquals;
        return this;
    }

    /**
     * 设置创建时间范围查询的起始时间字段
     *
     * @param createTimeAfter 创建时间的最小值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setCreateTimeAfter(Date createTimeAfter) {
        this.createTimeAfter = createTimeAfter;
        return this;
    }

    /**
     * 设置过期时间范围查询的起始时间字段
     *
     * @param expireTimeAfter 过期时间的最小值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setExpireTimeAfter(Date expireTimeAfter) {
        this.expireTimeAfter = expireTimeAfter;
        return this;
    }

    /**
     * 设置过期时间范围查询的结束时间字段
     *
     * @param expireTimeBefore 过期时间的最大值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setExpireTimeBefore(Date expireTimeBefore) {
        this.expireTimeBefore = expireTimeBefore;
        return this;
    }

    /**
     * 构造函数，初始化实体类
     *
     * @param entityClass 实体类
     */
    public CommonTokenQueryCriteria() {
        super(SystemTokenEntity.class);
    }

    public CommonTokenQueryCriteria setSessionIdEquals(String sessionIdEquals) {
        this.sessionIdEquals = sessionIdEquals;
        return this;
    }
}
