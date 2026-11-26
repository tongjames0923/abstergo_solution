package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Getter;

import java.util.Date;
import java.util.List;

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
@LogicDeleteEntity(activeFlagValues = {1, -1, -2, -3})
public class CommonTokenQueryCriteria extends DefaultEntityCriteria {
    /**
     * 等值匹配的令牌字段 对应数据库中的token字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "token")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String tokenEquals;

    /**
     * 等值匹配的用户名字段 对应数据库中的user_name字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "user_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String userNameEquals;

    /**
     * 等值匹配的标识字段 对应数据库中的flag字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer flagEquals;

    /**
     * 等值匹配的令牌类型字段 对应数据库中的token_type字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "token_type")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer tokenTypeEquals;

    /**
     * 等值匹配的会话ID字段 对应数据库中的session_id字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "session_id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String sessionIdEquals;

    /**
     * 等值匹配的主键ID字段 对应数据库中的id字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 标识集合IN查询字段 对应数据库中的flag字段，使用IN比较条件
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagIn;

    /**
     * 令牌类型集合IN查询字段 对应数据库中的token_type字段，使用IN比较条件
     */
    @QueryMapping(value = "token_type")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> tokenTypeIn;

    /**
     * 等值匹配的登录设备MAC地址字段 对应数据库中的mac_address字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "mac_address")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String macAddressEquals;

    /**
     * 等值匹配的登录设备IP地址字段 对应数据库中的ip_address字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "ip_address")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String ipAddressEquals;

    /**
     * 左模糊匹配的登录设备IP地址字段 对应数据库中的ip_address字段，使用LLIKE比较条件
     */
    @QueryMapping(value = "ip_address")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String ipAddressLike;

    /**
     * 等值匹配的创建来源字段 对应数据库中的create_type字段，使用EQUAL比较条件
     */
    @QueryMapping(value = "create_type")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer createTypeEquals;

    /**
     * 更新时间范围查询的起始时间字段 对应数据库中的update_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("update_time")
    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Date updateTimeAfter;

    /**
     * 更新时间范围查询的结束时间字段 对应数据库中的update_time字段，使用LESS_EQUAL比较条件
     */
    @QueryMapping("update_time")
    @QueryField(contrast = ContrastEnum.LESS_EQUAL)
    private Date updateTimeBefore;

    /**
     * 创建时间范围查询的起始时间字段 对应数据库中的create_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("create_time")

    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Date createTimeAfter;

    /**
     * 创建时间范围查询的起始时间字段 对应数据库中的create_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("create_time")
    @QueryField(contrast = ContrastEnum.LESS_EQUAL)
    private Date createTimeBefore;

    /**
     * 过期时间范围查询的起始时间字段 对应数据库中的expire_time字段，使用GREATER_EQUAL比较条件
     */
    @QueryMapping("expire_time")

    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Date expireTimeAfter;

    /**
     * 过期时间范围查询的结束时间字段 对应数据库中的expire_time字段，使用LESS_EQUAL比较条件
     */
    @QueryMapping("expire_time")

    @QueryField(contrast = ContrastEnum.LESS_EQUAL)
    private Date expireTimeBefore;

    /**
     * 创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)

    private OrderedModel createTimeOrdered;

    /**
     * 过期时间排序
     */
    @QueryOrderField(mappedName = "expire_time", order = OrderConstants.ORDERED_MODEL)

    private OrderedModel expireTimeOrdered;

    /**
     * 构造函数，初始化实体类
     *
     * @param entityClass 实体类
     */
    public CommonTokenQueryCriteria() {
        super(SystemTokenEntity.class);
    }

    /**
     * 设置创建时间排序模型
     *
     * @param createTimeOrdered 创建时间排序模型
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setCreateTimeOrdered(OrderedModel createTimeOrdered) {
        this.createTimeOrdered = createTimeOrdered;
        return this;
    }

    /**
     * 设置过期时间排序模型
     *
     * @param expireTimeOrdered 过期时间排序模型
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setExpireTimeOrdered(OrderedModel expireTimeOrdered) {
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

    public CommonTokenQueryCriteria setSessionIdEquals(String sessionIdEquals) {
        this.sessionIdEquals = sessionIdEquals;
        return this;
    }

    public CommonTokenQueryCriteria setCreateTimeBefore(Date createTimeBefore) {
        this.createTimeBefore = createTimeBefore;
        return this;
    }

    /**
     * 设置等值匹配的主键ID字段
     *
     * @param idEquals 用于等值匹配的主键ID值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setIdEquals(Long idEquals) {
        this.idEquals = idEquals;
        return this;
    }

    /**
     * 设置标识集合IN查询字段
     *
     * @param flagIn 用于IN查询的标识集合
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setFlagIn(List<Integer> flagIn) {
        this.flagIn = flagIn;
        return this;
    }

    /**
     * 设置令牌类型集合IN查询字段
     *
     * @param tokenTypeIn 用于IN查询的令牌类型集合
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setTokenTypeIn(List<Integer> tokenTypeIn) {
        this.tokenTypeIn = tokenTypeIn;
        return this;
    }

    /**
     * 设置等值匹配的登录设备MAC地址字段
     *
     * @param macAddressEquals 用于等值匹配的MAC地址值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setMacAddressEquals(String macAddressEquals) {
        this.macAddressEquals = macAddressEquals;
        return this;
    }

    /**
     * 设置等值匹配的登录设备IP地址字段
     *
     * @param ipAddressEquals 用于等值匹配的IP地址值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setIpAddressEquals(String ipAddressEquals) {
        this.ipAddressEquals = ipAddressEquals;
        return this;
    }

    /**
     * 设置左模糊匹配的登录设备IP地址字段
     *
     * @param ipAddressLike 用于左模糊匹配的IP地址值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setIpAddressLike(String ipAddressLike) {
        this.ipAddressLike = ipAddressLike;
        return this;
    }

    /**
     * 设置等值匹配的创建来源字段
     *
     * @param createTypeEquals 用于等值匹配的创建来源值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setCreateTypeEquals(Integer createTypeEquals) {
        this.createTypeEquals = createTypeEquals;
        return this;
    }

    /**
     * 设置更新时间范围查询的起始时间字段
     *
     * @param updateTimeAfter 更新时间的最小值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setUpdateTimeAfter(Date updateTimeAfter) {
        this.updateTimeAfter = updateTimeAfter;
        return this;
    }

    /**
     * 设置更新时间范围查询的结束时间字段
     *
     * @param updateTimeBefore 更新时间的最大值
     * @return 当前查询条件实例，支持链式调用
     */
    public CommonTokenQueryCriteria setUpdateTimeBefore(Date updateTimeBefore) {
        this.updateTimeBefore = updateTimeBefore;
        return this;
    }
}
