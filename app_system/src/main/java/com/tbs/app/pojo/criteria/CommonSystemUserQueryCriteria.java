package com.tbs.app.pojo.criteria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.model.QueryOrderedModel;
import lombok.Getter;

import java.util.Date;

/**
 * @author abstergo
 */
@Getter
public class CommonSystemUserQueryCriteria extends ClassBaseQueryCriteria<SystemUser> {
    @QueryMapping(value = "id")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String idEquals;
    @QueryMapping(value = "username")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String userNameEquals;
    @QueryMapping(value = "nickname")
    @QueryField(contrast = QueryContrastEnum.LLIKE)
    private String nickNameLike;
    @QueryMapping(value = "flag")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer flagEquals;
    @QueryMapping(value = "create_time")
    @QueryField(contrast = QueryContrastEnum.GREATER)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeGreaterThan;
    @QueryMapping(value = "create_time")
    @QueryField(contrast = QueryContrastEnum.LESS)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeLessThan;
    @QueryMapping(value = "update_time")
    @QueryField(contrast = QueryContrastEnum.GREATER)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTimeGreaterThan;
    @QueryMapping(value = "update_time")
    @QueryField(contrast = QueryContrastEnum.LESS)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTimeLessThan;

    public CommonSystemUserQueryCriteria setIdEquals(String idEquals) {
        this.idEquals = idEquals;
        return this;
    }

    public CommonSystemUserQueryCriteria setUserNameEquals(String userNameEquals) {
        this.userNameEquals = userNameEquals;
        return this;
    }

    public CommonSystemUserQueryCriteria setNickNameLike(String nickNameLike) {
        this.nickNameLike = nickNameLike;
        return this;
    }

    public CommonSystemUserQueryCriteria setFlagEquals(Integer flagEquals) {
        this.flagEquals = flagEquals;
        return this;
    }

    public CommonSystemUserQueryCriteria setCreateTimeGreaterThan(Date createTimeGreaterThan) {
        this.createTimeGreaterThan = createTimeGreaterThan;
        return this;
    }

    public CommonSystemUserQueryCriteria setCreateTimeLessThan(Date createTimeLessThan) {
        this.createTimeLessThan = createTimeLessThan;
        return this;
    }

    public CommonSystemUserQueryCriteria setUpdateTimeGreaterThan(Date updateTimeGreaterThan) {
        this.updateTimeGreaterThan = updateTimeGreaterThan;
        return this;
    }

    public CommonSystemUserQueryCriteria setUpdateTimeLessThan(Date updateTimeLessThan) {
        this.updateTimeLessThan = updateTimeLessThan;
        return this;
    }

    @QueryOrderField(mappedName = "create_time", order = OrderConstant.ORDERED_MODEL)
    private QueryOrderedModel createTimeOrder;

    @QueryOrderField(mappedName = "update_time", order = OrderConstant.ORDERED_MODEL)
    private QueryOrderedModel updateTimeOrder;

    public CommonSystemUserQueryCriteria setCreateTimeOrder(QueryOrderedModel createTimeOrder) {
        this.createTimeOrder = createTimeOrder;
        return this;
    }

    public CommonSystemUserQueryCriteria setUpdateTimeOrder(QueryOrderedModel updateTimeOrder) {
        this.updateTimeOrder = updateTimeOrder;
        return this;
    }
}
