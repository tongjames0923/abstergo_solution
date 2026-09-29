# TbsSdk（C# 后端接入 SDK）

面向 .NET 客户端（.NET Framework 4.6.2+ / .NET 6/8 等）的后端快速接入 SDK，能力与前端
`app-ui-vue` 实现对齐：

- **完整 HTTP 请求封装**：token 头 / 语言头自动注入、统一 `AjaxResult` 解包、错误码异常体系
- **完整登录流程**：统一授权端点 `POST /public/user/login/{grantType}`，App 模式登录为主
- **无鉴权 + 有鉴权双通道**：`/public/**` 免鉴权与 `/private/**` 带令牌，另提供泛化请求方法覆盖全部业务模块
- **push-message SSE 兼容适配**：签票据建连、hello 握手、订阅/心跳上行帧、signal 信令、消息拉取、换票退避重连

> 令牌说明：后端令牌为不透明随机串，请求头 `Authorization: <裸token>`（无 Bearer 前缀）。
> 令牌刷新（refresh grant）依赖 HttpOnly Cookie 属 Web 专属；App 模式下收到 405/468
> 统一触发 `SessionExpired` 事件，由宿主重新调用登录接口。

## 项目结构

```
sdk-csharp/
├── TbsSdk.sln
├── src/TbsSdk/                     # netstandard2.0 类库
│   ├── TbsSdkClient.cs             # 组合根（推荐入口）
│   ├── Configuration/TbsSdkOptions.cs
│   ├── Core/                       # HTTP 核心 + AjaxResult + 异常体系
│   ├── Auth/                       # 登录授权 + 令牌存储 + 第三方授权码
│   ├── Push/                       # SSE 推送 + 消息中心
│   └── Models/                     # 消息模型
└── tests/TbsSdk.Tests/             # xUnit 单元测试
```

## 快速开始

### 1. 登录与用户信息（App 模式）

```csharp
using TbsSdk;
using TbsSdk.Configuration;

var options = new TbsSdkOptions
{
    BaseUrl = "http://localhost:8083",   // 后端基地址（绝对地址）
    Language = "zh_CN",                  // Accept-Language
    Timeout = TimeSpan.FromSeconds(30)
};

using var sdk = new TbsSdkClient(options);

// 会话失效（405 token 失效 / 468 刷新失败）：App 模式下应引导重新登录
sdk.SessionExpired += (sender, e) =>
    Console.WriteLine($"会话失效 code={e.Code}，请重新登录: {e.Message}");

// 账号密码登录（password grantType + loginType=1 客户端/App 模式）
var token = await sdk.LoginAsync("admin", "123456");

// 其他授权方式
// await sdk.Auth.LoginWecomAsync(code, state);                 // 企业微信 SSO
// await sdk.Auth.LoginThirdPartyAsync(appId, appSecret);       // 第三方应用（自动生成 SHA-256 授权码，窗口 ±3 分钟）
// await sdk.Auth.LoginByGrantAsync("custom_grant", new { });   // 任意自定义 grantType

var user = await sdk.Auth.GetUserInfoAsync();
Console.WriteLine($"欢迎 {user.NickName}，权限：{string.Join(",", user.FlattenPermissions())}");
```

### 2. 无鉴权与有鉴权请求（含泛化方法）

```csharp
// 公开端点（/public/**）：无需登录即可调用
var grants = await sdk.Http.GetAsync<List<LoginGrantDescriptor>>("/public/user/login-grants");

// 私有端点（/private/**）：自动从 ITokenStore 注入 Authorization 头
var result = await sdk.Http.GetAsync("/private/user/info");

// 泛化业务请求（覆盖用户/角色/部门/字典/文件等全部业务模块）
var page = await sdk.Http.PostAsync<PagedResult>("/private/role/query",
    new { roleNameLike = "管理员" },
    new Dictionary<string, object> { ["pageNum"] = 1, ["pageSize"] = 10 });

// 显式令牌（第三方连通性测试等场景）
await sdk.Http.GetAsync("/private/test", null,
    new TbsRequestOptions { ExplicitToken = "other-token" });

// 隔离型请求（对齐前端 _skipAuthHandling）：跳过统一登录态处理，返回原始响应体自行判定
var raw = await sdk.Http.GetAsync("/public/ping", null,
    new TbsRequestOptions { SkipAuthHandling = true });
```

### 3. SSE 推送与消息中心

```csharp
// 登录后启动推送：自动签票据 → SSE 建连 → hello 握手 → 订阅 per-user 频道 → 30s 心跳
sdk.StartPush(user.Id?.ToString());

sdk.Push.StatusChanged += status => Console.WriteLine("推送状态: " + status);
sdk.Push.ErrorOccurred += ex => Console.WriteLine("推送异常（将自动换票重连）: " + ex.Message);

// 信令仅提示"频道有新消息"，消息内容由消息中心拉取（对齐前端 onSignal）
sdk.Push.SignalReceived += async () =>
{
    var page = await sdk.Messages.FetchMessagesAsync(limit: 20);   // GET /private/push/message/list
    foreach (var message in page.Normalize())
    {
        Console.WriteLine($"[{message.Priority}] {message.Title}: {message.Content}");
        if (!message.Read)
        {
            await sdk.Messages.MarkReadAsync(new[] { long.Parse(message.Id) });
        }
    }
    var unread = await sdk.Messages.FetchUnreadCountAsync();
};

// 动态增删订阅主题（连接未就绪时仅登记，hello 握手后统一订阅）
sdk.Push.SubscribeTopics("global.broadcast", "user.name.zhangsan");
sdk.Push.UnsubscribeTopics("user.search.test");

// 主动关闭（登出/程序退出调用，不再重连）
sdk.Push.Close();

// 发送持久化通知（接收人由后端 INotifyReceiverResolver 解析）
var sendResult = await sdk.Messages.SendNotifyAsync(new PushNotifySendForm
{
    MsgType = "notify",
    Title = "系统通知",
    Content = "内容",
    Priority = "normal"
});
```

### 4. 登出

```csharp
await sdk.LogoutAsync();   // 调用后端登出 + 关闭推送 + 清理本地令牌
```

## 关键行为约定

| 行为 | 说明 |
| --- | --- |
| 令牌头 | `Authorization: <裸token>`，无 Bearer 前缀（后端 `tbs.web.auth.token-filed-name`） |
| 语言头 | `Accept-Language: zh_CN / en_US` |
| 成功码 | `code=200` 返回 `AjaxResult`；`GetData<T>()` 取业务体 |
| 会话失效 | `code=405/468` → 触发 `SessionExpired` 事件 + 抛 `TbsReloginRequiredException` |
| 业务失败 | 其余 code → 抛 `TbsApiException`（携带 code/message/redirect） |
| SSE 重连 | 一次性 ticket 被服务端原子消费，断线后自动换票，指数退避 1s~30s + 随机抖动 |
| 心跳 | 每 30s 上行 `{"version":1,"type":"PING"}` 帧（可配置） |
| 上行帧 | type 为大写枚举名（SUBSCRIBE/UNSUBSCRIBE/PING）；下行事件名为小写值（hello/signal/pong/data/error） |

## 构建

需要 .NET SDK 8+：

```bash
cd sdk-csharp
dotnet build TbsSdk.sln
dotnet test TbsSdk.sln
```

类库目标 `netstandard2.0`，唯一外部依赖为 `System.Text.Json`（NuGet 自动还原）。
宿主可通过 `TbsSdkClient(options, httpMessageHandler, tokenStore)` 注入自定义
Handler（代理/测试）与令牌持久化实现。
