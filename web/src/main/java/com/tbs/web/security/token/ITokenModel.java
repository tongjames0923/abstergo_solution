package com.tbs.web.security.token;

import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * 令牌模型
 *
 * @author abstergo
 */
public interface ITokenModel extends Serializable {
    /**
     * 获取令牌
     *
     * @return
     */
    String getToken();

    /**
     * 获取令牌过期时间
     *
     * @return
     */
    Date getExpireTime();

    /**
     * 获取令牌创建时间
     *
     * @return
     */
    Date getCreateTime();

    /**
     * 获取用户名
     *
     * @return
     */
    String getUserName();

    /**
     * 获取令牌类型
     *
     * @return
     */
    TokenCreateTypeEnum getCreateType();

    /**
     * 获取令牌状态
     *
     * @return
     */
    TokenStatusEnum getStatus();

    /**
     * 设置令牌状态
     *
     * @return
     */
    void setStatus(TokenStatusEnum status);

    /**
     * 获取令牌类型
     *
     * @return
     */
    TokenTypeEnum getTokenType();

    /**
     * 获取sessionId
     *
     * @return
     */
    String getSessionId();

}
