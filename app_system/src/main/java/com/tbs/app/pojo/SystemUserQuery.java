package com.tbs.app.pojo;

import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.pojo.model.ClassQueryObject;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.enums.QueryContrastEnum;
import lombok.Getter;

import java.util.Date;

/**
 * @author abstergo
 */
@Getter
public class SystemUserQuery extends ClassQueryObject<SystemUser> {
    @QueryMapping(value = "id")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String idEquals;
    @QueryMapping(value = "username")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String userNameEquals;
    @QueryMapping(value = "nickname")
    @QueryField(contrast = QueryContrastEnum.LIKE)
    private String nickNameLike;
    @QueryMapping(value = "flag")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer flagEquals;
    @QueryMapping(value = "create_time")
    @QueryField(contrast = QueryContrastEnum.GREATER)
    private Date createTimeGreaterThan;
    @QueryMapping(value = "create_time")
    @QueryField(contrast = QueryContrastEnum.LESS)
    private Date createTimeLessThan;
    @QueryMapping(value = "update_time")
    @QueryField(contrast = QueryContrastEnum.GREATER)
    private Date updateTimeGreaterThan;
    @QueryMapping(value = "update_time")
    @QueryField(contrast = QueryContrastEnum.LESS)
    private Date updateTimeLessThan;

    public SystemUserQuery setIdEquals(String idEquals) {
        this.idEquals = idEquals;
        return this;
    }

    public SystemUserQuery setUserNameEquals(String userNameEquals) {
        this.userNameEquals = userNameEquals;
        return this;
    }

    public SystemUserQuery setNickNameLike(String nickNameLike) {
        this.nickNameLike = nickNameLike;
        return this;
    }

    public SystemUserQuery setFlagEquals(Integer flagEquals) {
        this.flagEquals = flagEquals;
        return this;
    }

    public SystemUserQuery setCreateTimeGreaterThan(Date createTimeGreaterThan) {
        this.createTimeGreaterThan = createTimeGreaterThan;
        return this;
    }

    public SystemUserQuery setCreateTimeLessThan(Date createTimeLessThan) {
        this.createTimeLessThan = createTimeLessThan;
        return this;
    }

    public SystemUserQuery setUpdateTimeGreaterThan(Date updateTimeGreaterThan) {
        this.updateTimeGreaterThan = updateTimeGreaterThan;
        return this;
    }

    public SystemUserQuery setUpdateTimeLessThan(Date updateTimeLessThan) {
        this.updateTimeLessThan = updateTimeLessThan;
        return this;
    }
}
