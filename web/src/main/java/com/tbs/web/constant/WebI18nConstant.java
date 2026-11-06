package com.tbs.web.constant;

/**
 * Web 模块国际化常量
 * <p>
 * 集中收拢 web 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code web/src/main/resources/messages-web.properties}（中文）
 * 与 {@code messages-web_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class WebI18nConstant {

    private WebI18nConstant() {
    }

    /* ===================== 通用消息（定义在 common 模块，web 跨模块引用） ===================== */

    /**
     * 操作成功提示：操作成功
     */
    public static final String COMMON_SUCCESS = "common.success";
    /**
     * 操作失败提示：操作失败
     */
    public static final String COMMON_FAIL = "common.fail";
    /**
     * 操作错误提示：操作错误
     */
    public static final String COMMON_ERROR = "common.error";
    /**
     * 重新登录提示：请重新登录
     */
    public static final String COMMON_RELOGIN = "common.relogin";
    /**
     * 未授权提示：未授权
     */
    public static final String COMMON_UNAUTHORIZED = "common.unauthorized";
    /**
     * 无权限提示：无权限
     */
    public static final String COMMON_FORBIDDEN = "common.forbidden";

    /* ===================== 认证相关（auth） ===================== */

    /** 认证信息为空提示：认证信息为空 */
    public static final String AUTH_INFO_EMPTY = "auth.info.empty";
    /** 令牌信息为空提示：令牌信息为空 */
    public static final String AUTH_TOKEN_EMPTY = "auth.token.empty";
    /** 用户信息为空提示：用户信息为空 */
    public static final String AUTH_USER_EMPTY = "auth.user.empty";
    /** 用户校验失败提示：用户信息验证失败 */
    public static final String AUTH_USER_VERIFY_FAIL = "auth.user.verify.fail";
    /** 用户校验失败详情提示：用户信息验证失败。详情:{0} */
    public static final String AUTH_USER_VERIFY_FAIL_DETAIL = "auth.user.verify.fail.detail";
    /** 用户权限变更提示：用户权限发生变更，用户名:{0} */
    public static final String AUTH_PERMISSION_CHANGED = "auth.permission.changed";
    /** 无效认证提示：未获取到有效的Authentication */
    public static final String AUTH_INVALID_AUTHENTICATION = "auth.invalid.authentication";

    /* ===================== 令牌相关（token） ===================== */

    /** 令牌签发未实现提示：请实现一个Token签发中心 */
    public static final String TOKEN_ISSUANCE_NOT_IMPLEMENTED = "token.issuance.not.implemented";
    /** 令牌不能为空提示：令牌不能为空 */
    public static final String TOKEN_REQUIRED = "token.required";
    /** 令牌不存在提示：令牌不存在 */
    public static final String TOKEN_NOT_FOUND = "token.not.found";
    /** 令牌请求参数为空提示：令牌请求参数不能为空 */
    public static final String TOKEN_REQUEST_REQUIRED = "token.request.required";
    /** 令牌类型为空提示：令牌类型不能为空 */
    public static final String TOKEN_TYPE_REQUIRED = "token.type.required";
    /** 令牌创建类型为空提示：令牌创建类型不能为空 */
    public static final String TOKEN_CREATE_TYPE_REQUIRED = "token.create.type.required";
    /** 会话ID不能为空提示：会话ID不能为空 */
    public static final String TOKEN_SESSION_ID_REQUIRED = "token.session.id.required";
    /** 令牌刷新失败提示：Token失效且刷新Token失败 */
    public static final String TOKEN_REFRESH_FAIL = "token.refresh.fail";

    /* ===================== 用户相关（user） ===================== */

    /** 用户信息为空提示：用户信息不能为空 */
    public static final String USER_INFO_REQUIRED = "user.info.required";
    /** 用户名不能为空提示：用户名不能为空 */
    public static final String USER_NAME_REQUIRED = "user.name.required";
    /** 密码不能为空提示：密码不能为空 */
    public static final String USER_PASSWORD_REQUIRED = "user.password.required";
    /** 用户名对应的用户不存在提示：用户 {0} 不存在 */
    public static final String USER_NOT_FOUND_WITH_USERNAME = "user.not.found.with.username";
    /** 用户名密码错误提示：用户 {0} 密码错误 */
    public static final String USER_PASSWORD_ERROR_WITH_USERNAME = "user.password.error.with.username";

    /* ===================== 登录授权（login grant） ===================== */

    /** 不支持的登录授权类型提示：不支持的登录方式: {0} */
    public static final String LOGIN_GRANT_NOT_SUPPORTED = "login.grant.not.supported";
    /** 登录方式未启用提示：登录方式未启用: {0} */
    public static final String LOGIN_GRANT_DISABLED = "login.grant.disabled";
    /** 登录凭证格式错误提示：登录凭证格式错误: {0} */
    public static final String LOGIN_GRANT_PAYLOAD_INVALID = "login.grant.payload.invalid";
    /** 登录授权类型重复注册提示：登录授权类型重复注册: {0} */
    public static final String LOGIN_GRANT_TYPE_DUPLICATE = "login.grant.type.duplicate";
    /** 外部身份未绑定提示：当前外部身份未绑定系统用户，请联系管理员 */
    public static final String LOGIN_GRANT_EXTERNAL_NOT_BOUND = "login.grant.external.not.bound";

    /* ===================== 通用接口（Generic API） ===================== */

    /** 通用接口ID不存在提示：通用接口ID不存在 */
    public static final String GENERIC_API_ID_NOT_FOUND = "GENERIC.API.ID_NOT_FOUND";
    /** 调用需要登录提示：调用需要有效登录 */
    public static final String GENERIC_API_NOT_LOGIN = "GENERIC.API.NOT_LOGIN";
    /** 接口版本过低提示：接口版本过低 */
    public static final String GENERIC_API_API_VERSION_NOT_FOUND = "GENERIC.API.API_VERSION_NOT_FOUND";
    /** Dubbo泛化调用失败提示：不含底层异常原文 */
    public static final String GENERIC_API_DUBBO_INVOKE_FAILED = "GENERIC.API.DUBBO_INVOKE_FAILED";
    /** Triple HTTP调用失败提示：不含底层异常原文 */
    public static final String GENERIC_API_TRIPLE_INVOKE_FAILED = "GENERIC.API.TRIPLE_INVOKE_FAILED";

    /* -------- 新版泛化调用链路（/call/{apiId}、/meta/{apiId}） -------- */

    /** 泛化引用构建失败提示：不含底层异常原文 */
    public static final String GENERIC_API_REFERENCE_FAILED = "GENERIC.API.REFERENCE_FAILED";
    /** 旧编排入口误用提示：泛化API {0} 不支持该调用入口 */
    public static final String GENERIC_API_ENTRY_UNSUPPORTED = "GENERIC.API.ENTRY_UNSUPPORTED";
    /** 泛化调用返回值不可序列化提示：返回值类型 {0} 不可序列化 */
    public static final String GENERIC_API_RESULT_NOT_SERIALIZABLE = "GENERIC.API.RESULT_NOT_SERIALIZABLE";
    /** 入参未登记提示：未登记入参定义却传了请求体 */
    public static final String GENERIC_API_PARAM_DEF_MISSING = "GENERIC.API.PARAM_DEF_MISSING";
    /** 必填参数缺失提示：缺少必填参数 {0} */
    public static final String GENERIC_API_PARAM_REQUIRED = "GENERIC.API.PARAM_REQUIRED";
    /** 多余参数提示：未知参数 {0} */
    public static final String GENERIC_API_PARAM_UNKNOWN = "GENERIC.API.PARAM_UNKNOWN";
    /** 参数个数不匹配提示：登记 {0} 个，传入 {1} 个 */
    public static final String GENERIC_API_PARAM_COUNT_MISMATCH = "GENERIC.API.PARAM_COUNT_MISMATCH";
    /** 参数名未登记提示：参数 {0} 未登记名称，只能按位置传参 */
    public static final String GENERIC_API_PARAM_NAME_UNREGISTERED = "GENERIC.API.PARAM_NAME_UNREGISTERED";
    /** 参数类型未登记提示：参数 {0} 未登记类型 */
    public static final String GENERIC_API_PARAM_TYPE_UNREGISTERED = "GENERIC.API.PARAM_TYPE_UNREGISTERED";
    /** 请求体形态不支持提示：只接受JSON对象或JSON数组 */
    public static final String GENERIC_API_BODY_FORMAT_UNSUPPORTED = "GENERIC.API.BODY_FORMAT_UNSUPPORTED";

    /* ===================== 枚举文案（enum） ===================== */

    /** 枚举：令牌创建类型-登录 */
    public static final String ENUM_TOKEN_CREATE_LOGIN = "enum.token.create.login";
    /** 枚举：令牌创建类型-刷新 */
    public static final String ENUM_TOKEN_CREATE_REFRESH = "enum.token.create.refresh";
    /** 枚举：令牌类型-访问令牌 */
    public static final String ENUM_TOKEN_TYPE_WEB_ACCESS = "enum.token.type.web.access";
    /** 枚举：令牌类型-刷新令牌 */
    public static final String ENUM_TOKEN_TYPE_REFRESH = "enum.token.type.refresh";
    /** 枚举：令牌类型-应用访问令牌 */
    public static final String ENUM_TOKEN_TYPE_APPLICATION_ACCESS = "enum.token.type.application.access";
    /** 枚举：令牌来源-cookie */
    public static final String ENUM_TOKEN_SOURCE_COOKIE = "enum.token.source.cookie";
    /** 枚举：令牌来源-header */
    public static final String ENUM_TOKEN_SOURCE_HEADER = "enum.token.source.header";
    /** 枚举：令牌来源-param */
    public static final String ENUM_TOKEN_SOURCE_PARAM = "enum.token.source.param";

    /* ===================== 其它 ===================== */

    /** 操作成功提示：操作成功 */
    public static final String CONTROLLER_OPERATION_SUCCESS = "controller.operation.success";
    /** 设置请求语言日志提示：设置请求语言: {0} */
    public static final String LOG_I18N_SET_LOCALE = "log.i18n.set.locale";
}
