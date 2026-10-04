# tbs-client-sdk

纯 Java 客户端 SDK：基于 OkHttp 的统一 HTTP 工具，封装本平台的登录流程、推送消息中心、SSE 信令订阅与应用更新（检查+下载）能力。无 Spring 依赖，任何 Java 应用可直接引入。

## 概述

| 能域 | 入口 | 对接服务端契约 |
|------|------|----------------|
| 统一 HTTP | `IBaseHttpClient` / `OkHttpBaseHttpClient` | `AjaxResult{code,message,data}` 统一响应 + 错误码异常映射 |
| 登录 | `ILoginApi` / `LoginApi` | `POST /public/user/login/{grantType}` 统一登录门面 |
| 推送消息中心 | `IPushMessageApi` / `PushMessageApi` | `/private/push/message/*`（notify/list/unread-count/read/operate/chain） |
| SSE 订阅 | `ISsePushSubscriber` / `SsePushSubscriber` | `/private/push/auth` → `/private/push/sse/connect` → `/private/push/sse/upload` |
| 应用更新 | `IUpdateApi` / `UpdateApi` | `POST /public/app-update/check` + 下载 `downloadUrls` |

设计遵循服务端"薄壳控制器 → 门面服务"分层：`TbsClient`（顶层门面）→ 能域 API → 基础设施（HTTP 契约 + OkHttp 实现）。

**Token 策略**：登录成功后令牌自动写入 `TokenManager`，由 OkHttp 拦截器注入请求头（默认 `Authorization`，可配置）。SDK **不实现刷新令牌**（后端刷新机制仅适用 web 端）；收到 401/405/468 时抛出 `SdkAuthException`，调用方捕获后重新登录。

## 快速开始

```java
// 1. 构造客户端（try-with-resources 自动释放 SSE 连接与 OkHttp 连接池）
try (TbsClient client = TbsClient.create("https://api.example.com")) {

    // 2. 登录（令牌自动注入后续请求）
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", "alice");
    payload.put("password", "******");
    TokenModel token = client.login().login("password", payload);

    // 3. 推送消息中心
    PushNotifySendForm form = new PushNotifySendForm();
    form.setMsgType("notice");
    form.setTitle("公告");
    form.setContent("内容");
    PushSendResult result = client.push().notify(form);
    Long unread = client.push().unreadCount();

    // 4. SSE 订阅信令（异步建连，回调通知结果）
    client.sse().subscribe(List.of("global.broadcast"), token.getUserName(), null,
        new SseFrameListener() {
            @Override
            public void onConnected(String sessionId) { /* 握手成功 */ }

            @Override
            public void onFrame(FrameType type, PushFrame frame) { /* DATA/SIGNAL 业务帧 */ }
        });

    // 5. 应用更新：检查 + 下载
    AppUpdateCheckResult check = client.update().checkUpgrade("my-app", "WINDOWS", 100L);
    if (Boolean.TRUE.equals(check.getNeedUpdate())) {
        List<DownloadedFile> files = client.update().downloadAll(
            check, new File("/tmp/update"), "my-app-" + check.getLatestVersionCode(),
            new UpdateDownloadListener() {
                @Override
                public void onProgress(String url, long downloaded, long total) { /* 进度 */ }
            });
    }
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
| `sseReconnect(maxAttempts, baseInterval)` | 关闭 | SSE 断线重连开关（指数退避：base × 2^(n-1)） |

## 核心组件

```
com.tbs.sdk/
├── TbsClient            顶层门面（Closeable）
├── http/                IBaseHttpClient 契约 + OkHttp 实现 + AjaxResult 解析/异常映射 + 流式下载
├── auth/                ILoginApi/LoginApi + TokenManager（volatile 无锁读取）
├── push/                IPushMessageApi/PushMessageApi + ISsePushSubscriber/SsePushSubscriber + 回调监听器
├── update/              IUpdateApi/UpdateApi + UpdateDownloadListener
├── model/               与服务端契约一一对应的轻量模型（TokenModel/PushFrame/AppUpdateCheckResult 等）
├── exception/           SdkException + SdkAuthException(401/405/468) + SdkServerException + SdkClientException
├── constants/           端点路径、SSE 协议常量、统一错误码
└── config/              SdkConfig（Builder）
```

### SSE 订阅流程（SsePushSubscriber 内部编排）

1. **认证**：`POST /private/push/auth`（携登录令牌）签发一次性 push-ticket（TTL 60 秒，每次建连重新签发，禁止复用）；
2. **建连**：OkHttp EventSource 携 `?push-ticket=` 建立 SSE 下行流（专用客户端读取超时为 0，避免空闲长连接被切断）；
3. **握手**：收到 `hello` 帧即会话就绪，回调 `onConnected`，自动 SUBSCRIBE 初始主题；
4. **心跳**：后台每 30 秒上行 `PING` 帧；
5. **重连**：断线后按指数退避重新走 auth 流程（默认关闭，经 `sseReconnect(...)` 开启）；
6. **释放**：`close()` 幂等，取消事件流、心跳与执行器。

**授权集（可订清单）**：auth 响应里的 `PushCredential.getGrantedTopics()` 为服务端签票时点算出的可订阅主题模式列表（支持订阅侧通配）；不在清单内的主题上行 SUBSCRIBE 会被服务端拒绝（回调收到 ERROR 帧，code 1008，仅拒该次订阅不断流）。字段为 `null`/空表示服务端未启用订阅鉴权（全放行）；授权变更需重新签票重连才生效。

## 异常处理

| 异常 | 触发场景 | 调用方处置 |
|------|----------|------------|
| `SdkAuthException` | 业务码 401/405/468（未授权/需重新登录/刷新失败） | 清理本地状态并重新登录 |
| `SdkServerException` | 业务码 403/500 等其他非成功码 | 提示用户（消息为服务端 i18n 结果） |
| `SdkClientException` | 网络/超时/解析失败/下载失败/参数非法 | 可重试或本地排障 |

## 最佳实践

- `TbsClient` 全局单例使用（内置连接池复用）；多线程调用安全，`TokenManager` 为 volatile 无锁读取。
- SSE 回调在 SDK 单线程执行器中分发且回调异常被隔离；耗时处理请在回调内转交业务线程池。
- 下载先写 `.part` 临时文件，成功后原子 rename，半包文件不会被误用。
- 模块依赖面最小：仅 `okhttp` / `okhttp-sse` / `jackson-databind` / `slf4j-api`，版本全部由父 POM 管控。

## 与 updater 模块的职责边界

本 SDK 的 `update()` 仅覆盖**升级检查与安装包下载**；部署脚本执行、本地版本持久化等安装编排由 `updater` 模块（`AutoUpdater`）承担，二者不重复实现。
