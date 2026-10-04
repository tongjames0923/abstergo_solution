package com.tbs.push.constants;

/**
 * 会话属性键常量
 * <p>
 * 统一收拢凭证签发→建连→会话全程传递的属性键约定：
 * 认证器写入的身份属性与授权器写入的授权集均以会话属性为载体，
 * 经{@code PushCredential.attributes}透传至连接会话，供订阅校验与拦截器消费。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushSessionAttrConstants {

    /**
     * 属性键：认证用户ID（服务端解析的数字主键字符串形式，认证器权威写入）
     */
    public static final String USER_ID = "userId";

    /**
     * 属性键：认证用户名（网关认证主体，供建连一致性与上行归属比对）
     */
    public static final String USERNAME = "username";

    /**
     * 属性键：订阅授权集（主题模式列表，逗号分隔；空缺失时订阅校验放行）
     */
    public static final String GRANTED_TOPICS = "grantedTopics";

    /**
     * 授权集分隔符（topic字符集不含逗号，分隔安全）
     */
    public static final String GRANT_SEPARATOR = ",";

    private PushSessionAttrConstants() {
    }

}
