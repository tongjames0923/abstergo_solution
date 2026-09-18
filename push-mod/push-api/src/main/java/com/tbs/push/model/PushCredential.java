package com.tbs.push.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 连接凭证
 * <p>
 * 认证通过后由凭证服务签发的一次性连接凭证快照：
 * 服务端预分配的 sessionId 与 ticket 一一绑定，客户端携 ticket 建连时
 * 经存储"取出即失效"地原子消费，绑定后建立以预分配 sessionId 为标识的会话。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 一次性连接凭证（UUID），建连时以 query param 携带
     */
    private String ticket;

    /**
     * 预分配的会话唯一标识（后续会话注册、上行定位、投递记录的唯一 key）
     */
    private String sessionId;

    /**
     * 认证通过后的会话属性（如 userId 等），建连时注入会话 attributes
     */
    private Map<String, String> attributes;

    /**
     * 凭证过期时间，过期后消费失败
     */
    private Date expireAt;

}
