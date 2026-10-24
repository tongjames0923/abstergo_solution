# tbs_client（sdk-flutter）

纯 Dart 客户端 SDK：统一封装本平台后端 HTTP 协议（AjaxResult 信封 + 错误码分流 + Web 无感刷新）、多授权登录、SSE 推送订阅、消息中心、应用更新与动态视图协议。零 Flutter 依赖，任意 Dart 环境可用；能力对齐仓库既有 Java 客户端（`sdk/`）、C# 客户端（`sdk-csharp/`）与前端 `app-ui-vue` 的最新契约。

## 概述

| 能域 | 入口 | 对接服务端契约 |
|------|------|----------------|
| 统一 HTTP | `BaseHttpClient` / `DioHttpClient` | `AjaxResult{code,message,data}` 统一响应 + 错误码异常映射 + 会话失效事件 + `Accept-Language` 语言头 |
| 登录 | `AuthApi` | `POST /public/user/login/{grantType}`（password/wecom/third_party/refresh）+ `GET /private/user/info` + `GET /public/user/login-grants` + `GET /private/user/logout` + 部门切换 |
| 推送消息中心 | `PushMessageApi` | `/private/push/message/*`（list 游标分页 / unread-count / read 裸 id 数组 / operate / notify） |
| SSE 订阅 | `SsePushSubscriber` | `/private/push/auth` → `/private/push/sse/connect?push-ticket=` → `/private/push/sse/upload`；hello 握手 / signal 信令 / PING 心跳 / 换票退避重连 |
| 应用更新 | `UpdateApi` | `POST /public/app-update/check`（或管理端 `/private/admin/app-version-policy/check`）+ 多镜像下载（.part + 原子重命名） |
| 动态视图（迁移预留） | `ViewApi` / `PagedApi` | `GET /private/view/routes`（动态路由树）+ `GET /private/view/form/{name}`（表单配置）+ 泛化分页查询协议（`POST {url}?pageNum&pageSize`） |

分层对齐服务端"薄壳控制器 → 门面服务"：`TbsClient`（顶层门面）→ 能域 API → 基础设施（HTTP 契约 + dio 实现）。

**Token 策略**：登录成功后令牌自动写入 `TokenStore`（内存缺省实现，宿主注入平台持久化实现），由拦截器注入请求头（默认 `Authorization`，裸令牌不带 `Bearer`，可配置）；请求级可用 `TbsRequestOptions.explicitToken` 覆盖。

## 会话策略（SessionPolicy，重要）

后端规范（`docs/guideline/04`）与前端实际行为对 405/468 的处理语义存在矛盾（规范：405 重登、468 刷新；前端 `request.js` 实际：405 刷新重放、468 重登）。本 SDK 将映射做成可配置策略：

| 预设 | 业务码 → 行为 | 适用端 |
|------|--------------|--------|
| `SessionPolicy.native` | 401/405/468 → 会话失效事件，引导重登（不做刷新，对齐 Java/C# SDK） | Android/iOS/桌面 |
| `SessionPolicy.web` | 405 → Cookie 单飞刷新 + 请求重放；468/401 → 强制重登（对齐 Vue 实际行为） | Web |

> ⚠️ **待核对**：Web 预设的 405/468 语义以前端 `app-ui-vue/src/utils/request.js` 实际行为为准，与规范文档相反，落地前需与后端确认断言来源后固化两份预设。刷新令牌依赖 `REFRESH_TOKEN` HttpOnly Cookie（服务端 `CookieRefreshTokenService`），**仅 Web 可用**；跨域直连时须后端 CORS 开启 `allowCredentials`，否则应走同源反代（对齐 Vue `/api` 模式）。

## 快速开始

```dart
// 1. 创建客户端（宿主注入平台令牌存储；Web 场景用 TbsClientConfig.forWeb）
final client = TbsClient.create(
  'https://api.example.com',
  config: TbsClientConfig.forNative('https://api.example.com'),
  tokenStore: MySecureTokenStore(), // 实现 TokenStore 接口
);

// 2. 会话失效监听（401/405/468 经 SessionPolicy 判定后触发，宿主引导重登）
client.onSessionExpired.listen((event) => showReloginDialog(event.message));
// 附加事件流：onNotice（成功提示/701 通知）、onRedirect（响应 redirect 字段）

// 3. 登录（令牌自动写入 TokenStore 并注入后续请求）
final token = await client.auth.loginByPassword('alice', '******');
final user = await client.auth.userInfo();      // GET /private/user/info
final grants = await client.auth.listEnabledGrants(); // 登录方式元数据

// 4. 启动推送长连接（缺省订阅 per-user 频道 tbs.push.channel.{userId}）
client.sse.setListener(MyListener()); // onStatusChanged/onHello/onSignal/onFrame/onError
client.sse.start('${user.id}');

// 5. 消息中心（游标分页）
final page = await client.push.listMessages(
  const PushMessageQueryForm(limit: 20, readFlag: 'unread'),
);
await client.push.markRead(['123456789012345678']);

// 6. 应用更新：检查 + 下载（桌面端 .part 原子落盘）
final check = await client.update.checkUpgrade('my-app', AppPlatform.windows, 1);
if (check.needUpdate) {
  await client.update.download(check.downloadUrls, '/tmp', 'my-app');
}

// 7. 登出（后端注销 + 关闭 SSE + 本地清理；后端失败不阻断本地清理）
await client.logout();
await client.dispose(); // 释放 SSE 与连接池（幂等）
```

## 配置说明

`TbsClientConfig`（不可变，`forNative`/`forWeb` 预设工厂）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `baseUrl` | 必填 | 服务端基础地址 |
| `tokenHeaderName` | `Authorization` | 令牌请求头字段名（裸令牌，需与服务端拾取配置一致） |
| `language` | `zh_CN` | `Accept-Language` 取值（zh_CN / en_US，服务端按此 i18n） |
| `connectTimeout` / `receiveTimeout` / `sendTimeout` | 10s / 30s / 30s | 超时（sendTimeout 仅带请求体的请求下发） |
| `sessionPolicy` | `SessionPolicy.native` | 错误码 → 会话行为映射（native/web 预设见上表） |
| `pushOptions` | 见 `PushOptions` | SSE 参数 |

`PushOptions`（缺省对齐前端 `settings.push`）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `topicPrefix` | `tbs.push.channel.` | per-user 信令频道缺省主题前缀 |
| `heartbeatInterval` | 200s | 心跳上行周期（须远小于服务端 600s 空闲超时；非正数关闭） |
| `heartbeatMaxFailures` | 3 | 心跳连续失败阈值，达到即判死换票重连 |
| `reconnectBaseDelay` / `reconnectMaxDelay` / `reconnectJitter` | 30s / 60s / 1s | 退避 `min(base×2^n, max) + [0, jitter)`，防多端惊群 |
| `reconnectMaxAttempts` | 3 | 最大自动重连次数（非正数不限次），超限转 `manualReconnect` |
| `protocolVersion` | 1 | SSE 协议版本 |

## 核心组件

```
lib/
├── tbs_client.dart        顶层导出桶（TbsClient 门面 static create）
└── src/
    ├── tbs_client.dart / tbs_client_impl.dart   门面：auth/push/sse/update/view/paged + 事件流 + logout/dispose
    ├── http/              BaseHttpClient 契约 + DioHttpClient 实现 + AjaxResponse 解析
    │                      + TbsRequestOptions（skipAuthHandling/explicitToken/isRefreshRequest）
    │                      + SessionRefreshCoordinator（单飞刷新，防"排队等自己"死锁）
    │                      + 客户端事件流（SessionExpired/Notice/Redirect）
    ├── auth/              AuthApi（多授权/用户信息/登出/部门切换）+ TokenStore 抽象 + ThirdPartyAuthCode
    ├── push/              PushMessageApi + SsePushSubscriber 状态机 + SseFrameCodec + 平台传输（native http 流 / web fetch 流）
    ├── update/            UpdateApi（公开/管理端检查 + 多镜像顺序回退下载）+ UpdateDownloadListener
    ├── view/              ViewApi（动态路由/表单配置）+ PagedApi（泛化分页协议，管理端迁移底座）
    ├── model/             与服务端契约一一对应的模型（字段名逐一对齐 Java SDK）
    ├── exception/         TbsException → Auth(401)/ReloginRequired(405,468)/Server(403/500/601/701)/Client
    ├── constants/         端点路径/授权类型/SSE 协议/错误码（对齐 SdkConstants）
    ├── config/            TbsClientConfig + PushOptions + SessionPolicy
    └── platform/          平台条件装配（HTTP 适配器 withCredentials / SSE 传输 / 文件下载）
```

## SSE 订阅流程（SsePushSubscriber 内部编排）

1. **签票**：`POST /private/push/auth`（携登录令牌）签发一次性 push-ticket（TTL 60s，禁止复用）；
2. **建连**：SSE 下行流携 `?push-ticket=` 与 `Authorization` 令牌头（native 经 `package:http` 流式、Web 经 `fetch_client` 流式，均无读超时限制）；
3. **握手**：`hello` 帧 payload 二次解析 `sessionId`（**以握手帧为准**），就绪后统一上行 SUBSCRIBE 全部已登记主题、启动心跳、回调 `onHello` 并触发一次 `onSignal` 兜底拉取；
4. **信令**：`signal` 事件回调 `onSignal`（无载荷，宿主据此拉取）；`pong` 重置心跳失败计数；`data` 帧解析 `PushFrame` 后经 `onFrame` 分发；
5. **动态订阅**：`subscribeTopics/unsubscribeTopics` 就绪时立即上行帧，未就绪仅登记，待 hello 后统一订阅（授权集以 `PushCredential.grantedTopics` 为准，越权订阅收到 `error` 帧 code 1008）；
6. **心跳**：按 `heartbeatInterval` 上行 PING，连续失败达阈值判定会话已死，主动关流换票重连；
7. **重连**：断线指数退避换票重连，超限转 `manualReconnect`；
8. **释放**：`close()` 幂等。

## Web 凭证模式说明

SSE 建连与 HTTP 主通道的凭证模式均跟随 `SessionPolicy`：仅 `web` 预设（Cookie 刷新流）携带跨域凭证（`credentials: include`）；`native` 预设为 `same-origin`。后端 CORS 为 `*` 通配时浏览器禁止凭证请求（`Access-Control-Allow-Origin` 不可为 `*`），跨域直连请保持 native 预设或后端开启 `allowCredentials` 并收敛来源；生产 Web 走同源反代后两种预设均可。

## 异常处理

| 异常 | 触发场景 | 调用方处置 |
|------|----------|------------|
| `TbsReloginRequiredException` | 业务码 405/468 命中重登策略，或刷新失败升级 | 引导用户重新登录（`TbsAuthException` 子类，捕获父类即可覆盖） |
| `TbsAuthException` | 业务码 401（未授权） | 清理本地状态并重新登录 |
| `TbsServerException` | 403/500/601/701 等其他非成功码 | 提示用户（message 为服务端 i18n 结果） |
| `TbsClientException` | 网络/超时/解析失败/下载失败/参数非法 | 可重试或本地排障 |

## 最佳实践

- `TbsClient` 全局单例使用（内置连接池复用），`TokenStore.peek()` 同步读供请求头注入。
- 会话失效统一经 `onSessionExpired` 处置，避免逐处判断业务码。
- SSE 回调在订阅器单线程事件循环中分发且异常被隔离；耗时处理请在回调内转交业务执行器。
- 第三方登录授权码 `ThirdPartyAuthCode.generate(appId, appSecret)`：`SHA-256(SALT:appSecret:timestamp:appId)` 小写 hex，时间窗 ±3 分钟，与 Java/Vue 实现逐字符一致。
- 第三方应用密钥仅用于本地生成授权码，不随请求传输。
- 依赖面最小：`dio` / `http` / `fetch_client` / `crypto` / `logging`。
