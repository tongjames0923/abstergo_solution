package com.tbs.web.security.user.vo;

import com.tbs.web.security.token.ITokenModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 登录会话视图对象
 * <p>
 * 聚合单个会话维度的令牌信息，供前端展示「我的会话」列表。由 {@code IUserLoginInfoService.listUserSessions} 组装。
 * </p>
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
     * 登录（会话创建）时间
     */
    private Date createTime;

    /**
     * 会话内的访问令牌列表
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
     * 最后活跃时间（最近一次访问令牌创建时间）
     */
    private Date lastActiveTime;
}
