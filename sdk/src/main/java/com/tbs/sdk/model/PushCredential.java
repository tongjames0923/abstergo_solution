package com.tbs.sdk.model;

import java.util.Date;
import java.util.Map;
import lombok.Data;

/**
 * 推送连接凭证
 * <p>
 * 对应服务端PushCredential：POST /private/push/auth签发，
 * ticket为一次性凭证（TTL默认60秒，禁止复用），建连成功即消费。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushCredential {

    /**
     * 一次性连接凭证（UUID）
     */
    private String ticket;

    /**
     * 预分配会话标识（后续上行帧携带）
     */
    private String sessionId;

    /**
     * 会话属性（认证表单透传，服务端可能追加）
     */
    private Map<String, String> attributes;

    /**
     * 凭证过期时间
     */
    private Date expireAt;
}
