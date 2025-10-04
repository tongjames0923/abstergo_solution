package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 登录会话信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginSessionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 登录时间
     */

    private Date createTime;

    /**
     * 登录令牌列表
     */
    private List<ITokenModel> accessTokens;

    /**
     * 是否在线（至少有一个有效的刷新令牌）
     */
    private boolean online;

    /**
     * 会话中的令牌数量
     */
    private int tokenCount;

    /**
     * 有效的刷新令牌数量
     */
    private int activeRefreshTokenCount;

    /**
     * 最后更新时间
     */
    //
    private Date lastActiveTime;
}
