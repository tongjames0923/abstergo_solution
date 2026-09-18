package com.tbs.push.constants;

/**
 * 推送模块i18n资源key常量
 * <p>
 * 统一收拢推送模块的国际化消息key，key风格为 push.{domain}.{desc} 全小写点分，
 * 与资源文件 messages-push.properties / messages-push_en_US.properties 一一对应。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushI18nConstant {

    /**
     * 帧（协议帧）不能为空
     */
    public static final String FRAME_REQUIRED = "push.frame.required";

    /**
     * 帧类型不能为空
     */
    public static final String FRAME_TYPE_REQUIRED = "push.frame.type.required";

    /**
     * 协议版本不兼容, 收到:{0}, 要求:{1}
     */
    public static final String PROTOCOL_VERSION_MISMATCH = "push.protocol.version.mismatch";

    /**
     * 帧解析失败: {0}
     */
    public static final String FRAME_DECODE_FAILED = "push.frame.decode.failed";

    /**
     * 消息记录不能为空
     */
    public static final String MESSAGE_REQUIRED = "push.message.required";

    /**
     * 消息记录缺少唯一标识
     */
    public static final String MESSAGE_ID_REQUIRED = "push.message.id.required";

    /**
     * 消息记录缺少主题
     */
    public static final String MESSAGE_TOPIC_REQUIRED = "push.message.topic.required";

    /**
     * 主题不合法: {0}
     */
    public static final String TOPIC_INVALID = "push.topic.invalid";

    /**
     * 会话不能为空
     */
    public static final String SESSION_REQUIRED = "push.session.required";

    /**
     * 会话标识不能为空
     */
    public static final String SESSION_ID_REQUIRED = "push.session.id.required";

    /**
     * 会话不存在或已断开, sessionId:{0}
     */
    public static final String SESSION_NOT_FOUND = "push.session.not.found";

    /**
     * 会话未就绪（未完成握手）, sessionId:{0}
     */
    public static final String SESSION_NOT_CONNECTED = "push.session.not.connected";

    /**
     * 不支持的帧类型: {0}
     */
    public static final String FRAME_TYPE_UNSUPPORTED = "push.frame.type.unsupported";

    /**
     * 服务端内部错误
     */
    public static final String INTERNAL_ERROR = "push.internal.error";

    /**
     * 投递记录不能为空
     */
    public static final String DELIVERY_RECORD_REQUIRED = "push.delivery.record.required";

    /**
     * 投递记录缺少结果状态
     */
    public static final String DELIVERY_RECORD_RESULT_REQUIRED = "push.delivery.record.result.required";

    /**
     * 编解码器不能为空
     */
    public static final String CODEC_REQUIRED = "push.codec.required";

    /**
     * 会话注册表不能为空
     */
    public static final String SESSION_REGISTRY_REQUIRED = "push.session.registry.required";

    /**
     * 订阅注册表不能为空
     */
    public static final String TOPIC_REGISTRY_REQUIRED = "push.topic.registry.required";

    /**
     * 投递协调器不能为空
     */
    public static final String ROUTE_COORDINATOR_REQUIRED = "push.route.coordinator.required";

    /**
     * 消息源不能为空
     */
    public static final String MESSAGE_STORE_REQUIRED = "push.message.store.required";

    /**
     * 传输适配器不支持的能力: {0}
     */
    public static final String CAPABILITY_NOT_SUPPORTED = "push.capability.not.supported";

    /**
     * 认证表单不能为空
     */
    public static final String AUTH_FORM_REQUIRED = "push.auth.form.required";

    /**
     * 认证失败: {0}
     */
    public static final String AUTH_FAILED = "push.auth.failed";

    /**
     * 连接凭证缺失, 请先调用认证端点获取
     */
    public static final String TICKET_REQUIRED = "push.ticket.required";

    /**
     * 连接凭证无效或已被使用: {0}
     */
    public static final String TICKET_INVALID = "push.ticket.invalid";

    /**
     * 连接凭证已过期, 请重新认证: {0}
     */
    public static final String TICKET_EXPIRED = "push.ticket.expired";

    /**
     * 连接凭证不能为空
     */
    public static final String CREDENTIAL_REQUIRED = "push.credential.required";

    /**
     * 连接凭证缺少过期时间
     */
    public static final String CREDENTIAL_EXPIRE_REQUIRED = "push.credential.expire.required";

    /**
     * 内存凭证存储已满, 拒绝签发新凭证
     */
    public static final String CREDENTIAL_STORE_FULL = "push.credential.store.full";

    private PushI18nConstant() {
    }

}
