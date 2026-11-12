# 推送消息中心 · 接入与开发指南（从 0 到 1 + 多客户端案例）

> 面向"要把推送/站内信接进来"的开发者。设计原理与决策见同目录 `推送消息中心设计方案.md`，本文只讲**怎么跑通、怎么接**。
> 全文端点、字段、帧格式均对齐当前代码仓库。约定服务地址用变量 `BASE`（如 `http://localhost:8083`），令牌用 `TOKEN`。

## 0. 读前须知

- **两个开关**：业务消息中心能力受 `tbs.system.enable=true` 控制；长连接与信令受 `tbs.push.enable=true` 控制。二者相互独立。
- **令牌头**：所有 `/private/**` 需登录令牌。浏览器 axios 与 SDK 均自动携带；手写请求时置于 `Authorization` 头（取值即登录令牌，前缀以项目认证体系为准）。
- **成功语义**：**入库即投递成功**。HTTP 发送成功 ≠ 前端收到；信令丢了也没关系，客户端建连/重连会主动拉第一页兜底。

## 1. 一分钟心智模型

一条消息的旅程 = **落库 → 空信令 → 拉取 → 回传**：

```
① 签票   POST /private/push/auth            带 token，换一次性 ticket(TTL60s) + sessionId
② 建连   GET  /private/push/sse/connect?push-ticket=…   下行事件流，首帧 event:hello 带回 sessionId
③ 订阅   POST /private/push/sse/upload       上行帧 type=SUBSCRIBE，订阅 tbs.push.channel.{userId}
④ 发送   POST /private/push/message/notify   落库(fan-out)，事务提交后逐人 publishSignal
⑤ 触达   在线客户端收到 event:signal         →（防抖）→ GET /private/push/message/list 拉正文
⑥ 回传   POST /private/push/message/read | /operate       写 READ/CLICK 日志，未读由日志派生
```

要点：**长连接只推空信令（`signal`，无载荷）**，正文一律走鉴权的分页拉取接口。这样天然获得持久化、历史、未读、翻页，且大消息不占用广播通道。

## 2. 端点与契约速查

### 2.1 传输层（3 个，建"管道"）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/private/push/auth` | body `{token, userId?, attributes?}` → `data:{ticket, sessionId, grantedTopics}` |
| GET | `/private/push/sse/connect?push-ticket=` | SSE 下行流；首帧 `hello`；`@NoWrapped`（非统一响应体） |
| POST | `/private/push/sse/upload` | body `{sessionId, frame:{version:1, type, topic?, payload?}}` |

**两套表达，勿混**：

- 上行帧 `type` = 后端 `FrameTypeEnum` **大写枚举名**：`SUBSCRIBE` / `UNSUBSCRIBE` / `PING`（`DATA` 编程用）。
- 下行 SSE **事件名** = 帧类型 **小写值**：`hello` / `signal` / `pong` / `data` / `error` / `shutdown`。
  - `error` 帧是服务端对某一上行帧的异步拒绝反馈（订阅未授权/上行被拒/topic 非法），**不断流**；`payload` 仅为 i18n 错误文案，不含错误码。
  - `shutdown` 是服务端优雅停机通知（宽限期默认 5s），`payload` 为 `{"reason":"server-shutdown","graceMillis":5000}`：收到后应停心跳，待宽限期结束再换票重连。

### 2.2 消息中心（6 个，基路径 `/private/push/message`，注意 message 单数）

| 方法 | 路径 | 入参 | data |
|------|------|------|------|
| POST | `/notify` | `PushNotifySendForm`（`msgType` 必填） | `{messageId, topic, msgType, receiverCount, signalCount}` |
| GET | `/list` | query：`cursor` / `limit`(≤100,缺省20) / `readFlag=all\|unread` | `{list:[…], cursor, hasMore}` |
| GET | `/unread-count` | — | `long` |
| POST | `/read` | **裸数组** `[10086,10087]`（≤100） | `int`（实际写入数，幂等） |
| POST | `/operate` | `{messageId, opType, extInfo?}` | `int`（CLICK 补记 READ） |
| GET | `/chain` | query：`?messageId=` | `{message, sendEvent, receivers[], operations[]}` |

列表行 `list[i]` 字段：`messageId, topic, msgType, title, content, priority, sender, url, createTime, readFlag, readTime`。

### 2.3 发送表单字段（`/notify`）

| 字段 | 必填 | 约束 | 缺省 |
|------|------|------|------|
| `msgType` | ✅ | 非空、≤32 | — |
| `title` | | ≤255 | — |
| `content` | | — | — |
| `priority` | | `low`/`normal`/`high`/`urgent` | `normal` |
| `topic` | | ≤128，见 §2.4 | `global.broadcast` |
| `sender` | | ≤64 | HTTP 下取当前登录用户 |
| `url` | | ≤512 | — |
| `extInfo` | | 任意 JSON | — |
| `source` | | `http`/`rpc`/`job`/`system` | `http` |

> **接收人不随表单传递**，由后端按 `topic` 解析（见 §2.4）。

### 2.4 topic 决定"发给谁"（默认解析器规则）

| topic 写法 | 投递对象 |
|------------|----------|
| 不传 / `global.broadcast` | 全体激活用户 |
| `user.name.{userName}` | 按用户名精确匹配 |
| `user.search.{keyword}` | 按用户名前缀搜索 |
| `tbs.push.channel.*` | **保留命名空间**，业务禁用（防伪装信令） |
| 其他未知 topic | 无人（仅落库，`receiverCount=0`） |

`receiverCount>0` 才算真有人收到；`=0` 表示没解析到接收人（如用户名不存在）。

---

## 3. 从 0 到 1 跑通

### 3.1 后端就绪

```yaml
# application-tbs.yml
tbs:
  system:
    enable: true
  push:
    enable: true
    transport: sse          # 或 websocket
```

- 启动类标注 `@EnableTbsPush` + `@EnableTbsPushSse`（WebSocket 用对应启用注解）；
- 引入 `push-sse-impl`（或 `push-websocket-impl`）与 `default-system-impl`；
- 三张表建好：`push_message` / `push_message_receiver` / `push_message_operation_log`；
- `PushMessageScanConfig` 已 `@MapperScan` 指向 shared 的 push Mapper 包（引入即生效）；push 实体由 `jkit.sql.auto.packages` 扫描。

### 3.2 验证判据（跑通即达标）

1. `POST /notify` 返回 `receiverCount>0`；
2. 在线客户端收到 `event:signal`；
3. `GET /list` 能拉到这条消息；
4. `GET /unread-count` 计入它；
5. `POST /read` 后未读减 1、`readFlag` 变 `true`；
6. `GET /chain?messageId=` 能看到 SEND + 接收人 + READ 三段。

### 3.3 选客户端

| 场景 | 走 | 见 |
|------|----|----|
| 命令行验证 / 网关联调 | curl | §4 |
| 非 Vue 的纯前端页 | 浏览器原生 SSE | §5 |
| 本项目 `app-ui-vue` | 复用 `src/push` | §6 |
| Java 后端/桌面 | Java SDK | §7 |
| .NET 应用 | C# SDK | §8 |
| 脚本/服务集成 | Python / Go | §9 / §10 |
| 系统内部发通知（免 HTTP） | 程序化 Service | §11 |

---

## 4. 案例 A：curl 全流程

> 一个终端挂着 SSE 流，另一个终端发通知，观察信令与拉取。

```bash
BASE=http://localhost:8083

# 0) 登录取 token（grantType/响应字段以项目实际启用的登录方式为准）
TOKEN=$(curl -s -X POST "$BASE/public/user/login/password" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' | jq -r '.data.token')

# 1) 签票：换一次性 ticket + sessionId
AUTH=$(curl -s -X POST "$BASE/private/push/auth" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"token\":\"$TOKEN\",\"userId\":\"1\"}")
TICKET=$(echo "$AUTH" | jq -r '.data.ticket')
SID=$(echo "$AUTH" | jq -r '.data.sessionId')

# 2) 建连（-N 关缓冲，持续读事件流；另开一个终端继续 3~6）
curl -N -H "Authorization: $TOKEN" \
  "$BASE/private/push/sse/connect?push-ticket=$TICKET"
#   预期先收到： event:hello  data:{...,"sessionId":"..."}

# 3) 订阅本人信令频道（大写 type）
curl -s -X POST "$BASE/private/push/sse/upload" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"sessionId\":\"$SID\",\"frame\":{\"version\":1,\"type\":\"SUBSCRIBE\",\"topic\":\"tbs.push.channel.1\"}}"

# 3b) 心跳保活（周期发 PING）
curl -s -X POST "$BASE/private/push/sse/upload" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"sessionId\":\"$SID\",\"frame\":{\"version\":1,\"type\":\"PING\"}}"
```

**另一个终端**（管理员发通知给 `admin`）：

```bash
BASE=http://localhost:8083
TOKEN=<上一步的 token>

# 4) 发送：发给用户名精确匹配的接收人
curl -s -X POST "$BASE/private/push/message/notify" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"msgType":"notify","title":"工单已分派","content":"#2048 已分派给您","priority":"high","topic":"user.name.admin","url":"/ticket/2048"}'
#   → data.receiverCount 应 >0；此时第一个终端应打印 event:signal

# 5) 拉正文
curl -s "$BASE/private/push/message/list?limit=20&readFlag=unread" -H "Authorization: $TOKEN" | jq '.data'

# 5b) 未读数
curl -s "$BASE/private/push/message/unread-count" -H "Authorization: $TOKEN"

# 6) 已读（裸数组）
curl -s -X POST "$BASE/private/push/message/read" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' -d '[10086]'

# 6b) 点击回传
curl -s -X POST "$BASE/private/push/message/operate" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"messageId":10086,"opType":"CLICK","extInfo":"curl-demo"}'
```

---

## 5. 案例 B：浏览器原生 SSE（不依赖框架）

> 原生 `EventSource` 不能带 `Authorization` 头，而 `/private/**` 要求该头。两种解法：① 用 `eventsource-polyfill`（XHR 流实现，可带 Header，本项目即用它）；② 网关放宽 connect 端点鉴权（不推荐）。

```js
import { EventSourcePolyfill } from 'event-source-polyfill'

async function connect({ token, userId, onNewMessage }) {
  const base = ''
  // 1) 签票
  const { data: cred } = await fetch(base + '/private/push/auth', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: token },
    body: JSON.stringify({ token, userId })
  }).then(r => r.json())

  // 2) 建连（携带与 axios 同源的 Authorization 头）
  const es = new EventSourcePolyfill(
    `${base}/private/push/sse/connect?push-ticket=${encodeURIComponent(cred.ticket)}`,
    { headers: { Authorization: token } })

  const upload = frame => fetch(base + '/private/push/sse/upload', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: token },
    body: JSON.stringify({ sessionId: cred.sessionId, frame })
  })

  es.addEventListener('hello', () => {
    // 3) 订阅本人信令频道
    upload({ version: 1, type: 'SUBSCRIBE', topic: `tbs.push.channel.${userId}` })
    setInterval(() => upload({ version: 1, type: 'PING' }), 30000)   // 心跳
    pull()   // hello 兜底先拉一遍
  })

  es.addEventListener('signal', () => pull())   // 4) 收到信令→拉正文（建议 300ms 防抖）

  async function pull() {
    const { data: page } = await fetch(base + '/private/push/message/list?limit=20&readFlag=all',
      { headers: { Authorization: token } }).then(r => r.json())
    page.list.forEach(onNewMessage)
  }
  return es   // 关闭：es.close()
}
```

---

## 6. 案例 C：在本项目 `app-ui-vue` 里接入（推荐路径）

本项目已封装 `src/push`（连接层）+ `src/store/modules/push`（Vuex 单一数据源）+ `PushNotificationCenter`（UI）。**业务通常不用碰 SSE 细节**，只做三件事：

### 6.1 登录后启动连接

在用户登录成功（拿到 token 与 userId）处派发一次：

```js
this.$store.dispatch('push/startConnection')
```

`startConnection` 内部：取 `getToken()` 与 `rootState.user.userId` → `initPush({ topics:[tbs.push.channel.{userId}], onSignal })`；`onSignal` 对 `/list` 拉取做 **300ms 防抖**；并"登录即先拉一遍"兜底（不等握手）。登出/卸载调 `this.$store.dispatch('push/stopConnection')`。

### 6.2 挂消息中心组件 + 角标

```vue
<template>
  <PushNotificationCenter
    :messages="pushMessages"
    :unread="pushUnreadCount"
    :status="pushConnectionStatus" />
</template>
<script>
import { mapGetters } from 'vuex'
import PushNotificationCenter from '@/components/PushNotificationCenter'
export default {
  components: { PushNotificationCenter },
  computed: mapGetters('push', ['pushMessages', 'pushUnreadCount', 'pushConnectionStatus'])
}
</script>
```

关键行为（无需改）：`seenIds` 去重防重复弹窗；**首轮拉取只入列不弹窗**（`firstPullDone`）避免登录弹一堆历史；点击条目自动回传 `read`/`operate`；未读角标展示以服务端 `/unread-count` 为准更精确。

### 6.3 按业务类型定制处理（可选）

```js
import { registerMessageHandler } from '@/push'
const off = registerMessageHandler('notify', msg => {
  // msg: { id, type, title, content, timestamp, priority, sender, url, read }
  if (msg.url) routerPush(msg.url)
})
// 组件销毁时 off() 注销；'*' 可订阅全部类型
```

管理端发布用 `src/push/admin.js` 的 `publishNotify(form)`（`scope:'all'`→`global.broadcast` 单次；`scope:'users'`→逐人 `user.name.{username}`）。

---
## 7. 案例 D：Java SDK（`sdk` 模块）

> 门面 `TbsClient` 聚合 `login()`/`push()`/`sse()`/`update()`，登录令牌由 HTTP 层自动注入；`TbsClient` 实现 `Closeable`，用毕释放。

```java
import com.tbs.sdk.TbsClient;
import com.tbs.sdk.model.*;
import com.tbs.sdk.push.*;
import java.util.*;

try (TbsClient client = TbsClient.create("http://localhost:8083")) {
    // 1) 登录（grantType + 对应表单），令牌自动写入后续请求
    client.login().login("password", Map.of("username", "admin", "password", "123456"));

    // 2) 发通知（落库 + fan-out + 信令均由后端完成）
    PushNotifySendForm form = new PushNotifySendForm();
    form.setMsgType("notify");
    form.setTitle("工单已分派");
    form.setPriority("high");
    form.setTopic("user.name.admin");
    PushSendResult r = client.push().notify(form);
    System.out.println("接收人=" + r.getReceiverCount() + ", 信令=" + r.getSignalCount());

    // 3) 订阅本人信令频道；hello 握手后 SDK 会兜底触发一次 onSignal
    client.sse().subscribe(List.of("tbs.push.channel.1"), "1", null, new SseFrameListener() {
        @Override public void onHello(HelloInfo hello) {
            System.out.println("推送已就绪 sessionId=" + hello.getSessionId());
        }
        @Override public void onSignal() { pull(client); }                     // 兜底/新信令都在此拉取（建议 300ms 防抖）
        @Override public void onShutdown(String reason, long graceMillis) {    // 停机通知：SDK 已停心跳并待宽限期后重连
            System.out.println("服务端停机 reason=" + reason + ", " + graceMillis + "ms 后自动重连");
        }
        @Override public void onError(Throwable cause) { cause.printStackTrace(); }
    });

    Thread.sleep(60_000);
}

static void pull(TbsClient client) {
    PushMessageQueryForm q = new PushMessageQueryForm();
    q.setLimit(20);
    q.setReadFlag("all");
    PushMessagePage page = client.push().list(q);
    page.getList().forEach(m -> System.out.println(m.getTitle() + " read=" + m.getReadFlag()));
    // 未读数： client.push().unreadCount();
    // 已读：   client.push().markRead(List.of(10086L));
}
```

---

## 8. 案例 E：C# SDK（`sdk-csharp` 模块）

> 组合根 `TbsSdkClient` 暴露 `Auth`/`Messages`（CRUD）/`Push`（`SsePushClient`）。`SignalReceived` 在 `hello` 兑底与 `signal` 两路都触发。

```csharp
using TbsSdk;
using TbsSdk.Configuration;
using TbsSdk.Models;
using TbsSdk.Push;

var options = new TbsSdkOptions { BaseUrl = "http://localhost:8083" };
using var sdk = new TbsSdkClient(options);
sdk.SessionExpired += (s, e) => Console.WriteLine("会话失效，请重新登录: " + e.Message);

// 1) 登录（账号密码），令牌自动存入 TokenStore
var token = await sdk.LoginAsync("admin", "123456");

// 2) 发通知
var send = await sdk.Messages.SendNotifyAsync(new PushNotifySendForm {
    MsgType = "notify", Title = "工单已分派", Priority = PushMessagePriority.High,
    Topic = "user.name.admin", Url = "/ticket/2048"
});
Console.WriteLine($"接收人={send.ReceiverCount}, 信令={send.SignalCount}");

// 3) 收信令→拉取
sdk.Push.SignalReceived += async () => {
    var page = await sdk.Messages.FetchMessagesAsync(limit: 20, readFlag: PushConstants.ReadFlagAll);
    foreach (var m in page.List) Console.WriteLine($"{m.Title} read={m.ReadFlag}");
};

// 4) 启动推送（内部 Configure+Connect，缺省订阅 tbs.push.channel.{userId}）
sdk.StartPush(userId: token.Id?.ToString());
// 动态增订：sdk.Push.SubscribeTopics("global.broadcast");
// 已读：  await sdk.Messages.MarkReadAsync(new long[]{10086});
```

---

## 9. 案例 F：Python（`requests` + 手动读 SSE）

> 无专用 SDK，直接用 REST + SSE。关键点：`Authorization` 头、`signal` 事件触拉取、`stream=True` 逐行。

```python
import requests, threading, time

BASE = "http://localhost:8083"
s = requests.Session()

# 0) 登录（grantType / 字段以实际启用方式为准）
tok = s.post(f"{BASE}/public/user/login/password",
             json={"username": "admin", "password": "123456"}).json()["data"]["token"]
H = {"Authorization": tok}

# 1) 签票
cred = s.post(f"{BASE}/private/push/auth", headers=H,
              json={"token": tok, "userId": "1"}).json()["data"]
sid = cred["sessionId"]

# 2) 订阅本人频道（上行帧 type 大写）
s.post(f"{BASE}/private/push/sse/upload", headers=H, json={
    "sessionId": sid, "frame": {"version": 1, "type": "SUBSCRIBE",
                                 "topic": "tbs.push.channel.1"}})

def pull():
    page = s.get(f"{BASE}/private/push/message/list", headers=H,
                 params={"limit": 20, "readFlag": "all"}).json()["data"]
    for m in page["list"]:
        print(m["title"], m["readFlag"])

def listen():
    with s.get(f"{BASE}/private/push/sse/connect", headers=H,
               params={"push-ticket": cred["ticket"]}, stream=True) as r:
        event = None
        for line in r.iter_lines(decode_unicode=True):
            if line and line.startswith("event:"):
                event = line.split(":", 1)[1].strip()
            elif event == "signal" and line.startswith("data:"):
                pull()   # 建议此处加 300ms 防抖

def heartbeat():
    while True:
        s.post(f"{BASE}/private/push/sse/upload", headers=H, json={
            "sessionId": sid, "frame": {"version": 1, "type": "PING"}})
        time.sleep(30)

threading.Thread(target=listen, daemon=True).start()
threading.Thread(target=heartbeat, daemon=True).start()
# 发通知：s.post(f"{BASE}/private/push/message/notify", headers=H, json={"msgType":"notify", "topic":"user.name.admin", "title":"x"})
```

---

## 10. 案例 G：Go（`net/http` + `bufio`）

> 同理：签票 → `GET .../sse/connect?push-ticket=` 流式读 → `event:signal` 行触发 `GET /list`。错误处理略。

```go
base := "http://localhost:8083"

// 1) 签票（POST /private/push/auth，头 Authorization: tok），取 data.Ticket / data.SessionId
// 2) 订阅（POST /private/push/sse/upload，frame.type="SUBSCRIBE", topic="tbs.push.channel.1"）

req, _ := http.NewRequest("GET", base+"/private/push/sse/connect?push-ticket="+ticket, nil)
req.Header.Set("Authorization", tok)
stream, _ := http.DefaultClient.Do(req)
defer stream.Body.Close()

sc := bufio.NewScanner(stream.Body)
var event string
for sc.Scan() {
    line := sc.Text()
    switch {
    case strings.HasPrefix(line, "event:"):
        event = strings.TrimPrefix(line, "event:")
    case event == "signal" && strings.HasPrefix(line, "data:"):
        pullMessages(base, tok) // GET /private/push/message/list
    }
}
```

---

## 11. 后端程序化发送（免 HTTP）

同一 JVM 内的业务代码直接依赖服务，走与 `/notify` 完全一致的链路：

```java
@Resource
private IPushNotifyService pushNotifyService;   // 推荐：落库 + fan-out + 信令

public void fireOrderAssigned(long assigneeUserId, String assigneeName) {
    PushNotifySendForm form = new PushNotifySendForm();
    form.setMsgType("notify");
    form.setTitle("工单已分派");
    form.setPriority(PushNotifySendForm.PRIORITY_HIGH);
    form.setTopic("user.name." + assigneeName);   // 接收人由解析器按 topic 解析
    PushSendResultVO r = pushNotifyService.send(form);
    // r.getReceiverCount()==0 时自行降级（如改发部门广播 topic）
}

// 定时清理（由 xxl-job 等调度调用，非内置）
// pushNotifyService.cleanupBefore(Date.from(Instant.now().minus(Duration.ofDays(28))));
```

> 低层 `IPushEndpoint` 只走协议管道、**不写消息中心库**，供自定义推送：
> `publish(topic, payload)` = DATA 全量帧；`publishSignal("tbs.push.channel.1", null)` = 空信令。消息中心发送请优先用 `IPushNotifyService.send`。

---

## 12. 常见问题排查

| 现象 | 排查方向 |
|------|----------|
| 404 / 连不上 | 路径拼写：基路径是 `message`（单数）；`transport` 与启用注解匹配 |
| 建连报 ticket 错 | ticket 一次性且 TTL≈60s；**重连必须重新签票**，不能复用 |
| 收不到 signal | 未订阅本人频道 `tbs.push.channel.{userId}`；`tbs.push.enable≠true`；userId 与登录用户不符 |
| 订阅被拒（1008） | 主题不在授权集；`grant-public-topics` 未含该主题；保留前缀不可订 |
| `receiverCount=0` | topic 未解析到人（用户名不存在/未知 topic）；自定义 SPI 返回空 |
| 前端不弹窗但列表有消息 | **首轮拉取只入列不弹**（设计如此，避免登录弹一堆历史） |
| 已读没变化 | 非本人接收关系被静默跳过；`/read` 用**裸数组** `[id]` 不是 `{ids}` |
| signal 来了拉到旧数据 | 拉取有 300ms 防抖；确认 `/list` 分页游标；重连会拉第一页兑底 |
| 心跳后仍被关 | 心跳上行连续失败达阈值→自动换票重连；检查令牌是否过期 |
| 消息发不出去（服务端） | `tbs.system.enable` 未开→Service Bean 未装配；未建表；`msgType` 未传 |

---

## 13. 配置速查

| 键 | 缺省 | 作用 |
|----|------|------|
| `tbs.system.enable` | — | 业务 Service / Resolver 装配 |
| `tbs.push.enable` | 字段 true，但需**显式配** | 协议层 + 安全 SPI 装配 |
| `tbs.push.transport` | `sse`（非 PushProperty 字段，仅 WS 侧条件读） | 传输实现选择 |
| `tbs.push.heartbeat-timeout-millis` | 90000（本项目 600000） | 服务端死连接判定空闲时长 |
| `tbs.push.grant-public-topics` | `[global.broadcast]` | 公开可订阅白名单 |
| `tbs.push.memory-message-capacity` | 10000 | 内存信令源容量 |
| `tbs.push.dispatch.scheduler` | — | 分发调度方式（`job` 走 PushDispatchJob） |

> 客户端侧：心跳周期/退避上限、拉取防抖（300ms）、内存保留上限（200）等在前端 `src/push/push-message.js` 的 `PUSH_DEFAULTS` 与 SDK `TbsSdkOptions.Push` 中配置。
