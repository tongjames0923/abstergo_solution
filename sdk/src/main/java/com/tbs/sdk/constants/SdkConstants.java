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
     * 当前登录用户信息端点（需登录令牌）
     */
    public static final String PATH_USER_INFO = "/private/user/info";

    /**
     * 登出端点（后端注销令牌与刷新令牌，需登录令牌）
     */
    public static final String PATH_LOGOUT = "/private/user/logout";

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
     * 应用升级检查端点（管理端，与公开检查同契约，需登录与权限）
     */
    public static final String PATH_APP_UPDATE_CHECK_ADMIN = "/private/admin/app-version-policy/check";

    /**
     * 授权类型：账号密码
     */
    public static final String GRANT_PASSWORD = "password";

    /**
     * 授权类型：企业微信 SSO
     */
    public static final String GRANT_WECOM = "wecom";

    /**
     * 授权类型：第三方应用
     */
    public static final String GRANT_THIRD_PARTY = "third_party";

    /**
     * 授权类型：刷新令牌（Web 专属，依赖 HttpOnly Cookie，App/SDK 模式不可用）
     */
    public static final String GRANT_REFRESH = "refresh";

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
     * 下行SSE事件名/帧类型小写值：握手确认
     */
    public static final String EVENT_HELLO = "hello";

    /**
     * 下行SSE事件名/帧类型小写值：信令
     */
    public static final String EVENT_SIGNAL = "signal";

    /**
     * 下行SSE事件名/帧类型小写值：心跳应答
     */
    public static final String EVENT_PONG = "pong";

    /**
     * 下行SSE事件名/帧类型小写值：业务数据
     */
    public static final String EVENT_DATA = "data";

    /**
     * 下行SSE事件名/帧类型小写值：错误
     */
    public static final String EVENT_ERROR = "error";

    /**
     * per-user SIGNAL 频道主题前缀（拼接用户数字id得到缺省频道，与后端 CHANNEL_TOPIC_PREFIX 一致）
     */
    public static final String TOPIC_PREFIX = "tbs.push.channel.";

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

    /**
     * 统一响应错误码：调试信息（前端仅控制台展示，SDK层归入普通失败）
     */
    public static final int CODE_DEBUG = 601;

    /**
     * 统一响应错误码：通知信息（前端仅通知栏展示，SDK层归入普通失败）
     */
    public static final int CODE_NOTICE = 701;

    /**
     * 消息读取范围：全部（含已读）
     */
    public static final String READ_FLAG_ALL = "all";

    /**
     * 消息读取范围：仅未读
     */
    public static final String READ_FLAG_UNREAD = "unread";

    private SdkConstants() {
    }
}
