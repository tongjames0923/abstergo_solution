package com.tbs.sdk.model;

import java.util.Date;
import lombok.Data;

/**
 * 登录令牌模型
 * <p>
 * 对应服务端ITokenModel序列化结构：不透明令牌串（非JWT），
 * 登录成功后写入TokenManager供请求头自动注入。
 * 令牌类型/状态/创建方式以原始字符串承载，避免与服务端枚举演进耦合。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class TokenModel {

    /**
     * 令牌串（不透明随机串）
     */
    private String token;

    /**
     * 登录用户名
     */
    private String userName;

    /**
     * 令牌类型（WEB_ACCESS_TOKEN/REFRESH_TOKEN/APPLICATION_ACCESS_TOKEN）
     */
    private String tokenType;

    /**
     * 令牌状态（ACTIVE/EXPIRED/REVOKE等）
     */
    private String status;

    /**
     * 会话标识
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
     * 创建方式（LOGIN/REFRESH）
     */
    private String createType;
}
