package com.tbs.sdk.constants;

/**
 * SDK全局常量
 * <p>
 * 固化客户端对接所需的端点路径、SSE协议约定与统一响应错误码，
 * 字段取值与服务端契约保持一致，服务端协议演进时在此同步修改。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SdkConstants {

    /**
     * 统一登录端点前缀，完整路径为 前缀 + grantType
     */
    public static final String PATH_LOGIN = "/public/user/login/";

    /**
     * 查询启用登录方式元数据端点
     */
    public static final String PATH_LOGIN_GRANTS = "/public/user/login-grants";

    /**
     * 发送持久化通知端点
     */
    public static final String PATH_PUSH_NOTIFY = "/private/push/message/notify";

    /**
     * 游标分页拉取消息列表端点
     */
    public static final String PATH_PUSH_LIST = "/private/push/message/list";

    /**
     * 统计未读数端点
     */
    public static final String PATH_PUSH_UNREAD_COUNT = "/private/push/message/unread-count";

    /**
     * 批量标记已读端点
     */
    public static final String PATH_PUSH_READ = "/private/push/message/read";

    /**
     * 通用操作回传端点
     */
    public static final String PATH_PUSH_OPERATE = "/private/push/message/operate";

    /**
     * 单消息全链路查询端点
     */
    public static final String PATH_PUSH_CHAIN = "/private/push/message/chain";

    /**
     * 推送连接认证端点（签发一次性push-ticket）
     */
    public static final String PATH_PUSH_AUTH = "/private/push/auth";

    /**
     * SSE下行流建立端点
     */
    public static final String PATH_PUSH_SSE_CONNECT = "/private/push/sse/connect";

    /**
     * SSE结构化帧上行端点
     */
    public static final String PATH_PUSH_SSE_UPLOAD = "/private/push/sse/upload";

    /**
     * 应用升级检查端点（公开接口，无需认证）
     */
    public static final String PATH_APP_UPDATE_CHECK = "/public/app-update/check";

    /**
     * SSE建连时携带连接凭证的query param参数名
     */
    public static final String PARAM_PUSH_TICKET = "push-ticket";

    /**
     * SSE协议版本号
     */
    public static final int SSE_PROTOCOL_VERSION = 1;

    /**
     * SSE事件名前缀（兼容部分服务端实现携带前缀的事件名）
     */
    public static final String SSE_EVENT_PREFIX = "push-";

    /**
     * 主题最大长度（字符数）
     */
    public static final int TOPIC_MAX_LENGTH = 128;

    /**
     * 批量标记已读单次上限
     */
    public static final int READ_BATCH_MAX = 100;

    /**
     * 消息拉取单页上限
     */
    public static final int LIST_PAGE_MAX = 100;

    /**
     * 统一响应错误码：成功
     */
    public static final int CODE_OK = 200;

    /**
     * 统一响应错误码：未授权
     */
    public static final int CODE_UNAUTHORIZED = 401;

    /**
     * 统一响应错误码：无权限
     */
    public static final int CODE_FORBIDDEN = 403;

    /**
     * 统一响应错误码：需要重新登录
     */
    public static final int CODE_RELOGIN = 405;

    /**
     * 统一响应错误码：刷新令牌失败
     */
    public static final int CODE_REFRESH_FAIL = 468;

    /**
     * 统一响应错误码：服务端错误
     */
    public static final int CODE_SERVER_ERROR = 500;

    private SdkConstants() {
    }
}
