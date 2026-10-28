package com.tbs.sdk.model;

import java.util.Date;
import java.util.List;
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
     * 订阅授权集（服务端签票时点计算的可订阅主题模式列表，支持订阅侧通配）
     * <p>
     * 不在集内的主题上行订阅会被服务端拒绝（回发ERROR帧，payload为i18n错误文案，不携带错误码）；
     * 字段缺失或为空表示服务端未启用订阅鉴权，订阅全放行。旧版本服务端不返回该字段，
     * 反序列化得null，客户端按需降级处理。
     * </p>
     */
    private List<String> grantedTopics;

    /**
     * 凭证过期时间
     */
    private Date expireAt;
}
