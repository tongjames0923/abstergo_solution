package com.tbs.push.constants;

/**
 * 推送协议帧常量
 * <p>
 * 固化协议层的全局常量：协议版本号、传输类型标识、SSE事件名约定与元数据键约定。
 * 协议演进时仅允许新增常量或递增版本号，不得修改已有常量语义。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class PushFrameConstants {

    /**
     * 当前协议版本号
     * <p>
     * 握手阶段客户端与服务的版本不一致时拒绝连接（返回ERROR帧后关闭）。
     * </p>
     */
    public static final int PROTOCOL_VERSION = 1;

    /**
     * 主题最大长度（字符数），超出视为非法topic
     */
    public static final int TOPIC_MAX_LENGTH = 128;

    /**
     * 传输类型标识：WebSocket
     */
    public static final String TRANSPORT_WEBSOCKET = "websocket";

    /**
     * 传输类型标识：SSE（SseEmitter下行 + HTTP POST上行组合通道）
     */
    public static final String TRANSPORT_SSE = "sse";

    /**
     * SSE事件名前缀，事件名为帧类型小写值
     */
    public static final String SSE_EVENT_PREFIX = "push-";

    /**
     * 元数据键：链路追踪ID，投递记录与帧透传时保留
     */
    public static final String METADATA_KEY_TRACE_ID = "x-trace-id";

    /**
     * 元数据键：会话属性过滤键，供自定义协调器按会话属性解析投递目标
     */
    public static final String METADATA_KEY_TARGET_ATTRIBUTE = "x-target-attribute";

    /**
     * 会话关闭原因：服务端优雅停机
     */
    public static final String CLOSE_REASON_SERVER_SHUTDOWN = "server-shutdown";

    /**
     * 会话关闭原因：心跳空闲超时
     */
    public static final String CLOSE_REASON_HEARTBEAT_TIMEOUT = "heartbeat-timeout";

    /**
     * 会话关闭原因：协议违规（版本不兼容/帧格式损坏）
     */
    public static final String CLOSE_REASON_PROTOCOL_VIOLATION = "protocol-violation";

    /**
     * 会话关闭原因：连接凭证校验失败
     */
    public static final String CLOSE_REASON_AUTH_FAILED = "auth-failed";

    /**
     * 会话关闭原因：建连主体与凭证绑定身份不一致
     */
    public static final String CLOSE_REASON_IDENTITY_MISMATCH = "identity-mismatch";

    /**
     * 会话关闭原因：登录态（token）实际失效，服务端主动踢线（业务侧触发）
     */
    public static final String CLOSE_REASON_TOKEN_EXPIRED = "token-expired";

    /**
     * 会话关闭原因：物理通道关闭事件回传
     */
    public static final String CLOSE_REASON_CHANNEL_CLOSED = "channel-closed";

    /**
     * 会话关闭原因：建连编排中被拦截器拒绝
     */
    public static final String CLOSE_REASON_CONNECT_REJECTED = "connect-rejected";

    /**
     * HTTP POST 认证端点路径（薄壳控制器引用，改路径只动本常量）
     */
    public static final String AUTH_PATH = "/private/push/auth";

    /**
     * 建连时携带连接凭证的 query param 参数名
     */
    public static final String TICKET_PARAM = "push-ticket";

    /**
     * HELLO 握手载荷键：预分配的会话标识
     */
    public static final String HELLO_KEY_SESSION_ID = "sessionId";

    /**
     * HELLO 握手载荷键：协议版本号
     */
    public static final String HELLO_KEY_VERSION = "version";

    /**
     * HELLO 握手载荷键：服务端推荐心跳周期（毫秒）
     */
    public static final String HELLO_KEY_HEARTBEAT_INTERVAL = "heartbeatInterval";

    /**
     * HELLO 握手载荷键：服务端最迟心跳期限（毫秒）
     */
    public static final String HELLO_KEY_HEARTBEAT_DEADLINE = "heartbeatDeadline";

    private PushFrameConstants() {
    }

}
