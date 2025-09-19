package com.tbs.web.security.token.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.token.ITokenModel;

import java.util.Date;

/**
 * 默认的token模型
 *
 * @author abstergo
 */
public class SimpleTokenModel implements ITokenModel {
    private static final long serialVersionUID = 1L;
    /**
     * token
     */
    private String token;

    /**
     * 用户名
     */
    private String userName;

    /**
     * token状态
     */
    private TokenStatusEnum statusEnum;

    private TokenTypeEnum typeEnum;

    private String sessionId;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private TokenCreateTypeEnum createType;

    public SimpleTokenModel() {
    }

    public SimpleTokenModel(String token, String userName, TokenStatusEnum status, TokenTypeEnum type, Date expireTime,
        Date createTime, TokenCreateTypeEnum createType, String sessionId) {
        this.token = token;
        this.userName = userName;
        this.statusEnum = status;
        this.typeEnum = type;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.createType = createType;
        this.sessionId = sessionId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public Date getExpireTime() {
        return expireTime;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public TokenCreateTypeEnum getCreateType() {
        return createType;
    }

    @Override
    public TokenStatusEnum getStatus() {
        return statusEnum;
    }

    @Override
    public void setStatus(TokenStatusEnum status) {
        this.statusEnum = status;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public TokenTypeEnum getTokenType() {
        return typeEnum;
    }
}
