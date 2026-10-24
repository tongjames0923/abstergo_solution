/// SDK 全局常量
///
/// 固化客户端对接所需的端点路径、授权类型、SSE 协议约定与统一响应错误码，
/// 取值与仓库既有 Java 客户端（sdk/src/main/java/com/tbs/sdk/constants/SdkConstants.java）
/// 及前端 app-ui-vue 契约保持一致，服务端协议演进时在此同步修改。
library;

/// 统一登录端点前缀，完整路径为 前缀 + grantType
const String pathLogin = '/public/user/login/';

/// 查询启用登录方式元数据端点
const String pathLoginGrants = '/public/user/login-grants';

/// 当前登录用户信息端点（需登录令牌）
const String pathUserInfo = '/private/user/info';

/// 登出端点（后端注销令牌，需登录令牌）
const String pathLogout = '/private/user/logout';

/// 获取刷新令牌端点（服务端种 HttpOnly Cookie，Web 专属）
const String pathGetRefreshToken = '/private/user/getRefreshToken';

/// 设置当前选中部门端点前缀，完整路径为 前缀 + departmentId
const String pathSetCurrentDepartment = '/private/user/setCurrentDepartment/';

/// 网关版本端点（免认证）
const String pathGatewayVersion = '/public/gateway/version';

/// 发送持久化通知端点
const String pathPushNotify = '/private/push/message/notify';

/// 游标分页拉取消息列表端点
const String pathPushList = '/private/push/message/list';

/// 统计未读数端点（响应 data 为裸数字）
const String pathPushUnreadCount = '/private/push/message/unread-count';

/// 批量标记已读端点（请求体为裸 id 数组）
const String pathPushRead = '/private/push/message/read';

/// 通用操作回传端点
const String pathPushOperate = '/private/push/message/operate';

/// 推送连接认证端点（签发一次性 push-ticket）
const String pathPushAuth = '/private/push/auth';

/// SSE 下行流建立端点
const String pathPushSseConnect = '/private/push/sse/connect';

/// SSE 结构化帧上行端点
const String pathPushSseUpload = '/private/push/sse/upload';

/// 应用升级检查端点（公开接口，无需认证）
const String pathAppUpdateCheck = '/public/app-update/check';

/// 应用升级检查端点（管理端，与公开检查同契约，需登录与权限）
const String pathAppUpdateCheckAdmin = '/private/admin/app-version-policy/check';

/// 动态视图路由端点（管理端迁移预留：菜单树下发）
const String pathViewRoutes = '/private/view/routes';

/// 动态视图表单配置端点前缀，完整路径为 前缀 + formName
const String pathViewForm = '/private/view/form/';

/// 授权类型：账号密码
const String grantPassword = 'password';

/// 授权类型：企业微信 SSO
const String grantWecom = 'wecom';

/// 授权类型：第三方应用
const String grantThirdParty = 'third_party';

/// 授权类型：刷新令牌（Web 专属，依赖 HttpOnly Cookie）
const String grantRefresh = 'refresh';

/// SSE 建连时携带连接凭证的 query 参数名
const String paramPushTicket = 'push-ticket';

/// SSE 协议版本号
const int sseProtocolVersion = 1;

/// SSE 事件名前缀（兼容部分服务端实现携带前缀的事件名）
const String sseEventPrefix = 'push-';

/// 下行 SSE 事件名/帧类型小写值：握手确认
const String eventHello = 'hello';

/// 下行 SSE 事件名/帧类型小写值：信令
const String eventSignal = 'signal';

/// 下行 SSE 事件名/帧类型小写值：心跳应答
const String eventPong = 'pong';

/// 下行 SSE 事件名/帧类型小写值：业务数据
const String eventData = 'data';

/// 下行 SSE 事件名/帧类型小写值：错误
const String eventError = 'error';

/// per-user 信令频道主题前缀（拼接用户数字 id 得到缺省频道）
const String topicPrefix = 'tbs.push.channel.';

/// 广播频道主题
const String topicGlobalBroadcast = 'global.broadcast';

/// 主题最大长度（字符数）
const int topicMaxLength = 128;

/// 批量标记已读单次上限
const int readBatchMax = 100;

/// 消息拉取单页上限
const int listPageMax = 100;

/// 消息已读筛选：全部（含已读）
const String readFlagAll = 'all';

/// 消息已读筛选：仅未读
const String readFlagUnread = 'unread';

/// 统一响应错误码：成功
const int codeOk = 200;

/// 统一响应错误码：未授权
const int codeUnauthorized = 401;

/// 统一响应错误码：无权限
const int codeForbidden = 403;

/// 统一响应错误码：需要重新登录
const int codeRelogin = 405;

/// 统一响应错误码：刷新令牌失败
const int codeRefreshFail = 468;

/// 统一响应错误码：服务端错误
const int codeServerError = 500;

/// 统一响应错误码：调试信息（前端仅控制台展示）
const int codeDebug = 601;

/// 统一响应错误码：通知信息（前端仅通知栏展示）
const int codeNotice = 701;

/// 第三方应用授权码 SALT（与后端固定一致）
const String thirdPartyAuthSalt = 'cad39065478d1e81fc528f9a6be211a5';
