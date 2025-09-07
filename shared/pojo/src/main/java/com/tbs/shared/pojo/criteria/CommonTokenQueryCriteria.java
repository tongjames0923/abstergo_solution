package com.tbs.shared.pojo.criteria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.query.impl.DefaultEntityClassBaseQueryCriteria;
import lombok.Getter;

import java.util.Date;

@Getter
public class CommonTokenQueryCriteria extends DefaultEntityClassBaseQueryCriteria {
    @QueryMapping(value = "token")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String tokenEquals;

    @QueryMapping(value = "user_name")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private String userNameEquals;

    @QueryMapping(value = "flag")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer flagEquals;

    @QueryMapping(value = "token_type")
    @QueryField(contrast = QueryContrastEnum.EQUAL)
    private Integer tokenTypeEquals;

    @QueryMapping("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date createTimeAfter;

    @QueryMapping("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date expireTimeAfter;

    @QueryMapping("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    private Date expireTimeBefore;

    public CommonTokenQueryCriteria setTokenEquals(String tokenEquals) {
        this.tokenEquals = tokenEquals;
        return this;
    }

    public CommonTokenQueryCriteria setUserNameEquals(String userNameEquals) {
        this.userNameEquals = userNameEquals;
        return this;
    }

    public CommonTokenQueryCriteria setFlagEquals(Integer flagEquals) {
        this.flagEquals = flagEquals;
        return this;
    }

    public CommonTokenQueryCriteria setTokenTypeEquals(Integer tokenTypeEquals) {
        this.tokenTypeEquals = tokenTypeEquals;
        return this;
    }

    public CommonTokenQueryCriteria setCreateTimeAfter(Date createTimeAfter) {
        this.createTimeAfter = createTimeAfter;
        return this;
    }

    public CommonTokenQueryCriteria setExpireTimeAfter(Date expireTimeAfter) {
        this.expireTimeAfter = expireTimeAfter;
        return this;
    }

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
}
