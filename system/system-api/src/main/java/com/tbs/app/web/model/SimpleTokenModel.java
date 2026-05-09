package com.tbs.app.web.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tbs.app.web.model.bridge.SimpleTokenDtoBridge;
import com.tbs.cache.serialize.annotation.CacheTransform;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.token.ITokenModel;
import lombok.Data;

import java.util.Date;

/**
 * 默认的token模型
 *
 * @author abstergo
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@CacheTransform(bridgeType = SimpleTokenDtoBridge.class)
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
    private TokenStatusEnum status;

    /**
     * token类型
     */
    private TokenTypeEnum tokenType;

    /**
     * sessionId
     */
    private String sessionId;

    /**
     * 过期时间
     */

    private Date expireTime;

    /**
     * 创建时间
     */

    private Date createTime;

    /**
     * 创建类型
     */
    private TokenCreateTypeEnum createType;

    /**
     * 客户端MAC地址
     */
    private String macAddr;

    /**
     * 客户端IP地址
     */
    private String ipAddr;

    public SimpleTokenModel() {
    }

    public SimpleTokenModel(String token, String userName, TokenStatusEnum status, TokenTypeEnum type, Date expireTime,
        Date createTime, TokenCreateTypeEnum createType, String sessionId, String macAddr, String ipAddr) {
        this.token = token;
        this.userName = userName;
        this.status = status;
        this.tokenType = type;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.createType = createType;
        this.sessionId = sessionId;
        this.macAddr = macAddr;
        this.ipAddr = ipAddr;
    }

}
