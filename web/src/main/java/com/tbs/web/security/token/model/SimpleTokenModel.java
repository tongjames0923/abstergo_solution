package com.tbs.web.security.token.model;

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

    public SimpleTokenModel() {
    }

    public SimpleTokenModel(String token, String userName, TokenStatusEnum status, TokenTypeEnum type, Date expireTime,
        Date createTime, TokenCreateTypeEnum createType, String sessionId) {
        this.token = token;
        this.userName = userName;
        this.status = status;
        this.tokenType = type;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.createType = createType;
        this.sessionId = sessionId;
    }

}
