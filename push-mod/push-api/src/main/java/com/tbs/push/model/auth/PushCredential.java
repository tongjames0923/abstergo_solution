package com.tbs.push.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tbs.push.constants.PushSessionAttrConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 连接凭证
 * <p>
 * 认证通过后由凭证服务签发的一次性连接凭证快照： 服务端预分配的 sessionId 与 ticket 一一绑定，客户端携 ticket 建连时 经存储"取出即失效"地原子消费，绑定后建立以预分配 sessionId 为标识的会话。
 * </p>
 * <p>
 * 授权集（{@code grantedTopics}）为签票时点由链式主题授权器合并产出的可订阅主题模式清单，
 * 本字段是其唯一载体；建连时经{@link #getSessionAttributes()}派生为会话属性写入统一会话，
 * 供订阅校验点判定。
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
     * 订阅授权集（主题模式列表，支持订阅侧通配），签票时点计算的一次性快照
     * <p>
     * 空或null表示未授权任何主题（无鉴权部署语义），订阅校验侧对无授权集会话保持放行；
     * 长连接期间的授权变更不生效，升权需重新签票换连接。
     * </p>
     */
    private List<String> grantedTopics;

    /**
     * 凭证过期时间，过期后消费失败
     */
    private Date expireAt;

    /**
     * 派生建连时注入统一会话的属性集（认证属性 + 授权集约定键）
     * <p>
     * 会话属性Map是授权集跨层传递到订阅校验点的载体，授权集本身以{@code grantedTopics}
     * 字段为唯一真源，两者不重复维护；字段非空时覆盖属性中的同名键（以凭证为准）。
     * 本方法不参与JSON序列化，凭证响应面只暴露{@code grantedTopics}列表语义。
     * </p>
     *
     * @return 可变属性副本（不为null），授权集非空时追加逗号分隔的 {@code grantedTopics} 键
     */
    @JsonIgnore
    public Map<String, String> getSessionAttributes() {
        Map<String, String> sessionAttributes =
            this.attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(this.attributes);
        String granted = joinGrantedTopics();
        if (granted != null) {
            sessionAttributes.put(PushSessionAttrConstants.GRANTED_TOPICS, granted);
        }
        return sessionAttributes;
    }

    /**
     * 拼接授权集为会话属性字符串（逐项去空白、跳过空项）
     *
     * @return 逗号分隔的主题模式列表，授权集为空时返回null
     */
    private String joinGrantedTopics() {
        if (this.grantedTopics == null || this.grantedTopics.isEmpty()) {
            return null;
        }
        List<String> topics = new ArrayList<>(this.grantedTopics.size());
        for (String topic : this.grantedTopics) {
            if (topic != null && !topic.trim().isEmpty()) {
                topics.add(topic.trim());
            }
        }
        return topics.isEmpty() ? null : String.join(PushSessionAttrConstants.GRANT_SEPARATOR, topics);
    }

}
