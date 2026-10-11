# tbs-client-sdk

纯 Java 客户端 SDK：基于 OkHttp 的统一 HTTP 工具，封装本平台的登录流程（多授权方式）、推送消息中心、SSE 信令订阅与应用更新（检查+下载）能力。无 Spring 依赖，任何 Java 应用可直接引入。能力对齐 C# 端 SDK 与前端 `app-ui-vue` 的最新契约。

## 概述

| 能域 | 入口 | 对接服务端契约 |
|------|------|----------------|
| 统一 HTTP | `IBaseHttpClient` / `OkHttpBaseHttpClient` | `AjaxResult{code,message,data}` 统一响应 + 错误码异常映射 + 泛化 `send`（任意动词/请求级选项）+ 会话失效事件 + `Accept-Language` 语言头 |
| 登录 | `ILoginApi` / `LoginApi` | `POST /public/user/login/{grantType}`（密码/企微/第三方）+ `GET /private/user/info` + `GET /public/user/login-grants` + `GET /private/user/logout` |
| 推送消息中心 | `IPushMessageApi` / `PushMessageApi` | `/private/push/message/*`（notify/list/unread-count/read/operate/chain） |
| SSE 订阅 | `ISsePushSubscriber` / `SsePushSubscriber` | `/private/push/auth` → `/private/push/sse/connect` → `/private/push/sse/upload`；状态/握手/信令/帧/异常事件 + 动态订阅 + 无限重连换票 |
| 应用更新 | `IUpdateApi` / `UpdateApi` | `POST /public/app-update/check`（或管理端 `/private/admin/app-version-policy/check`）+ 下载 `downloadUrls` |

设计遵循服务端"薄壳控制器 → 门面服务"分层：`TbsClient`（顶层门面）→ 能域 API → 基础设施（HTTP 契约 + OkHttp 实现）。

**Token 策略**：登录成功后令牌自动写入 `TokenManager`，由 OkHttp 拦截器注入请求头（默认 `Authorization`，裸令牌不带 `Bearer`，可配置）；请求级可用 `SdkRequestOptions.setExplicitToken(...)` 覆盖。SDK **不实现刷新令牌**（后端刷新依赖 HttpOnly Cookie，属 Web 专属）：业务码 405/468（会话失效/刷新失败）一律归为需重登，抛出 `SdkReloginRequiredException`（`SdkAuthException` 子类），并在抛出前先行回调 `SessionExpiredListener`；401 抛 `SdkAuthException`，其余非成功码抛 `SdkServerException`。

## 快速开始

```java
// 1. 构造客户端（try-with-resources 自动释放 SSE 连接与 OkHttp 连接池）
try (TbsClient client = TbsClient.create("https://api.example.com")) {

    // 2. 会话失效监听（收到 405/468 时先行回调，宿主据此引导重新登录）
    client.onSessionExpired((code, message) -> System.out.println("会话失效，请重新登录: " + message));

    // 3. 登录（App 模式：loginType=1，令牌自动注入后续请求）
    TokenModel token = client.login().loginByPassword("alice", "******");
    // 其他授权方式：loginByWecom(code, state) / loginByThirdParty(appId, appSecret[, timestamp])
    // 通用入口：login(grantType, credential)；获取信息：login().getUserInfo() / listEnabledGrants()

    // 4. 登录后启动推送长连接（缺省订阅 per-user 频道 topicPrefix + userId，userId 取用户信息主键）
    String userId = String.valueOf(client.login().getUserInfo().getId());
    client.startPush(userId, new SseFrameListener() {
        @Override public void onStatusChanged(ConnectionStatus status) { /* CONNECTING/CONNECTED/RECONNECTING/CLOSED */ }
        @Override public void onHello(HelloInfo hello) { /* 握手就绪，hello.getSessionId() */ }
        @Override public void onSignal() { /* 信令：触发 client.push().list(...) 拉取 */ }
        @Override public void onFrame(FrameType type, PushFrame frame) { /* DATA/PONG 等业务帧 */ }
    });

    // 5. 推送消息中心
    PushNotifySendForm form = new PushNotifySendForm();
    form.setMsgType("notice");
    form.setTitle("公告");
    form.setContent("内容");
    client.push().notify(form);
    Long unread = client.push().unreadCount();

    // 6. 应用更新：检查 + 下载
    AppUpdateCheckResult check = client.update().checkUpgrade("my-app", AppPlatform.WINDOWS, 100L);
    // 管理端检查同契约：update().checkUpgrade(SdkConstants.PATH_APP_UPDATE_CHECK_ADMIN, appKey, platform, code)
    if (Boolean.TRUE.equals(check.getNeedUpdate())) {
        String action = check.getActionName(); // force / recommend / notify
        client.update().downloadAll(check, new File("/tmp/update"),
            "my-app-" + check.getLatestVersionCode(), null);
    }

    // 7. 登出（调后端注销令牌 + 关闭推送连接 + 清理本地令牌，后端失败不阻断本地清理）
    client.logout();
}
```

## 配置说明

`SdkConfig`（Builder 模式，不可变对象）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `baseUrl` | 必填 | 服务端基础地址 |
| `connectTimeout` | 10 秒 | 连接超时 |
| `readTimeout` | 30 秒 | 读取超时 |
| `writeTimeout` | 30 秒 | 写入超时 |
| `tokenHeaderName` | `Authorization` | 令牌请求头字段名（需与服务端令牌拾取配置一致） |
| `language` | `zh_CN` | `Accept-Language` 请求头取值（需与服务端 i18n 支持一致） |
| `push(PushOptions)` | 见下 | SSE 推送参数（主题前缀/心跳/重连退避/协议版本） |

`PushOptions`（可变配置，随 `SdkConfig` 提供）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `topicPrefix` | `tbs.push.channel.` | per-user 频道缺省主题前缀（拼接 userId） |
| `heartbeatIntervalSeconds` | 30 | 心跳上行周期（秒，须远小于服务端空闲超时；<=0 关闭心跳） |
| `reconnectEnabled` | true | 断线自动重连开关（默认无限重连，直到主动 `close()`） |
| `reconnectBaseDelaySeconds` | 1 | 重连退避基数（实际间隔 base × 2^n，累加封顶后叠加抖动） |
| `reconnectMaxDelaySeconds` | 30 | 重连退避上限（秒） |
| `reconnectJitterSeconds` | 1 | 重连随机抖动上限（秒，防惊群；<=0 不叠加抖动） |
| `protocolVersion` | 1 | SSE 协议版本（与服务端 `PROTOCOL_VERSION` 一致） |

## 核心组件

```
com.tbs.sdk/
├── TbsClient            顶层门面（Closeable）：login/push/sse/update 能域 + onSessionExpired/startPush/logout 便捷编排
├── SdkSelfCheck         可运行 main 自检：第三方授权码 SHA-256 算法 + SSE 重连指数退避
├── http/                IBaseHttpClient 契约（send/全动词/请求选项/会话失效监听）+ OkHttp 实现 + AjaxResponse 解析/异常映射 + 流式下载 + SdkRequestOptions/SessionExpiredListener
├── auth/                ILoginApi/LoginApi（多授权/用户信息/登出）+ TokenManager（volatile 无锁读取）+ ThirdPartyAuthCode（SHA-256 授权码）
├── push/                IPushMessageApi/PushMessageApi + ISsePushSubscriber/SsePushSubscriber + SseFrameListener（状态/握手/信令/帧/异常）+ ConnectionStatus
├── update/              IUpdateApi/UpdateApi（公开/管理端检查重载 + 多镜像下载）+ UpdateDownloadListener
├── model/               与服务端契约一一对应的模型（TokenModel/UserInfoModel/PushFrame/HelloInfo/AppUpdateCheckResult/AppUpdateAction/AppPlatform 等）
├── exception/           SdkException + SdkAuthException(401) + SdkReloginRequiredException(405/468) + SdkServerException + SdkClientException
├── constants/           端点路径、授权类型、SSE 事件名/主题前缀、统一错误码（含 601/701）
└── config/              SdkConfig（Builder）+ PushOptions
```

### SSE 订阅流程（SsePushSubscriber 内部编排）

1. **认证**：`POST /private/push/auth`（携登录令牌）签发一次性 push-ticket（TTL 60 秒，每次建连重新签发，禁止复用）；
2. **建连**：OkHttp EventSource 携 `?push-ticket=` 建立 SSE 下行流，并携带与 HTTP 同源的 `Authorization` 令牌头（专用客户端读取超时为 0，避免空闲长连接被切断）；
3. **握手**：收到 `hello` 帧从其 payload 二次解析 `sessionId`（**以握手帧为准**，非签票预分配值），就绪后统一上行 SUBSCRIBE 订阅全部已登记主题、启动心跳、回调 `onHello`，并触发一次 `onSignal` 兜底拉取；
4. **信令**：收到 `signal` 事件回调 `onSignal`（无载荷，提示频道有新消息，宿主据此拉取）；`data`/`pong` 等业务帧经 `onFrame` 分发；
5. **动态订阅**：`subscribeTopics(...)`/`unsubscribeTopics(...)` 连接就绪时立即上行帧，未就绪时仅登记，待 hello 后随全量主题统一订阅；
6. **心跳**：后台按 `heartbeatIntervalSeconds` 上行 `PING` 帧；
7. **重连**：断线（含服务端 `error` 帧/关闭流）**换票重连**，退避为 `min(max, base × 2^n)` 叠加随机抖动（防惊群），默认无限重连直到主动 `close()`；每次重连重新走 auth 换票（ticket 一次性）；
8. **释放**：`close()` 幂等，取消事件流与心跳并停止重连（守护线程随实例回收，实例可再次 `subscribe` 复用）。

**授权集（可订清单）**：auth 响应里的 `PushCredential.getGrantedTopics()` 为服务端签票时点算出的可订阅主题模式列表（支持订阅侧通配）；不在清单内的主题上行 SUBSCRIBE 会被服务端拒绝（收到 `error` 帧，code 1008）。字段为 `null`/空表示服务端未启用订阅鉴权（全放行）；授权变更需重新签票重连才生效。

## 异常处理

| 异常 | 触发场景 | 调用方处置 |
|------|----------|------------|
| `SdkReloginRequiredException` | 业务码 405/468（令牌失效/刷新失败），并先行回调 `SessionExpiredListener` | 引导用户重新登录（`SdkAuthException` 子类，捕获父类即可覆盖） |
| `SdkAuthException` | 业务码 401（未授权） | 清理本地状态并重新登录 |
| `SdkServerException` | 业务码 403/500/601/701 等其他非成功码 | 提示用户（消息为服务端 i18n 结果） |
| `SdkClientException` | 网络/超时/解析失败/下载失败/参数非法 | 可重试或本地排障 |

> `601`（调试）/`701`（通知）在前端为特殊展示，SDK 层不作为特例处理，归入普通 `SdkServerException`，仅暴露常量供调用方按需判定。

## 最佳实践

- `TbsClient` 全局单例使用（内置连接池复用）；多线程调用安全，`TokenManager` 为 volatile 无锁读取。
- 会话失效统一经 `onSessionExpired` 监听处置，避免在每处调用重复判断 405/468。
- SSE 回调在 SDK 单线程执行器中分发且回调异常被隔离；帧上行走独立线程池 fire-and-forget，耗时处理请在回调内转交业务线程池。
- 第三方登录授权码为 `SHA-256(SALT:appSecret:timestamp:appId)` 小写十六进制（SALT 与后端固定一致，窗口 ±3 分钟），由 `ThirdPartyAuthCode.generate(...)` 生成；可运行 `java -cp target/classes:<依赖> com.tbs.sdk.SdkSelfCheck` 校验算法与退避逻辑。
- 下载先写 `.part` 临时文件，成功后原子 rename，半包文件不会被误用。
- 模块依赖面最小：仅 `okhttp` / `okhttp-sse` / `jackson-databind` / `slf4j-api`，版本全部由父 POM 管控。

## 与 updater 模块的职责边界

本 SDK 的 `update()` 仅覆盖**升级检查与安装包下载**；部署脚本执行、本地版本持久化等安装编排由 `updater` 模块（`AutoUpdater`）承担，二者不重复实现。
