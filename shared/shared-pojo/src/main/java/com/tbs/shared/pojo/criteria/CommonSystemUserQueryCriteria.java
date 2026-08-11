package com.tbs.shared.pojo.criteria;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
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
 * 系统用户通用查询条件
 * <p>
 * 提供对SystemUserEntity实体的各种查询条件支持，包括相等查询、模糊查询、范围查询和排序功能。
 *
 * @author abstergo
 */
@Getter
@LogicDeleteEntity(activeFlagValues = {1, 2, 0})
public class CommonSystemUserQueryCriteria extends DefaultEntityCriteria {
    /**
     * 根据ID精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String idEquals;

    /**
     * 根据用户名精确查询
     */
    @QueryMapping(value = "user_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String userNameEquals;

    /**
     * 根据昵称左模糊查询
     */
    @QueryMapping(value = "nick_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String nickNameLike;

    /**
     * 根据状态标志精确查询
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagEquals;

    /**
     * 根据创建时间大于查询
     */
    @QueryMapping(value = "create_time")
    @QueryField(contrast = ContrastEnum.GREATER)

    private Date createTimeGreaterThan;

    /**
     * 根据创建时间小于查询
     */
    @QueryMapping(value = "create_time")
    @QueryField(contrast = ContrastEnum.LESS)

    private Date createTimeLessThan;

    /**
     * 根据更新时间大于查询
     */
    @QueryMapping(value = "update_time")
    @QueryField(contrast = ContrastEnum.GREATER)

    private Date updateTimeGreaterThan;

    /**
     * 根据更新时间小于查询
     */
    @QueryMapping(value = "update_time")
    @QueryField(contrast = ContrastEnum.LESS)
    private Date updateTimeLessThan;

    /**
     * 创建时间排序字段
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;

    /**
     * 更新时间排序字段
     */
    @QueryOrderField(mappedName = "update_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel updateTimeOrder;

    public CommonSystemUserQueryCriteria() {
        super(SystemUserEntity.class);
    }

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
    public CommonSystemUserQueryCriteria setFlagEquals(List<Integer> flagEquals) {
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
    public CommonSystemUserQueryCriteria setCreateTimeOrder(OrderedModel createTimeOrder) {
        this.createTimeOrder = createTimeOrder;
        return this;
    }

    /**
     * 设置更新时间排序条件
     *
     * @param updateTimeOrder 更新时间排序模型
     * @return 当前查询条件对象，用于链式调用
     */
    public CommonSystemUserQueryCriteria setUpdateTimeOrder(OrderedModel updateTimeOrder) {
        this.updateTimeOrder = updateTimeOrder;
        return this;
    }

}
