# 消息推送（SSE）使用说明文档

> 适用版本：push-mod（SSE 传输）+ app-ui-vue 前端推送模块
> 读者：需要接入推送能力的前端/后端/第三方系统开发人员
> 相关文档：[前端推送模块契约](../app-ui-vue/src/push/README.md)、后端 `push-mod/README.md`（WebSocket 传输与协议详解）

---

## 一、功能整体介绍与适用场景

### 1.1 功能定位

消息推送功能基于 **SSE（Server-Sent Events）** 提供服务端到浏览器的实时单向下行通道，配套完整的连接管理、认证鉴权、主题订阅、心跳保活、断线重连、消息去重与 UI 呈现能力，可作为系统通知、任务提醒、审批催办等场景的**可复用基座**。

核心能力一览：

| 能力 | 说明 |
|------|------|
| 认证建连 | 登录令牌换取一次性 ticket（TTL 60s），携票据建立 SSE 流 |
| 主题订阅 | 支持点对点（per-user）与通配符（`*` 单级 / `#` 多级）主题 |
| 心跳保活 | 前端周期上行 PING（默认 30s），服务端空闲超时监控（可配置） |
| 断线重连 | 指数退避（1s→30s + 随机抖动），自动换票重连、重连后自动重订阅 |
| 消息去重 | 基于消息 id 的 Set 去重，杜绝重连/重发导致的重复弹窗 |
| 容错隔离 | 非法消息（缺 id/type、JSON 损坏）静默丢弃，不影响后续接收 |
| UI 呈现 | 右上角通知弹窗（按优先级控制时长）+ 顶栏铃铛 + 消息中心抽屉 |
| 管理发布 | 通知发布页（全体/指定用户）、结构化校验、发布历史本地留存 |

### 1.2 适用场景

- **系统通知**：向全体用户（`global.system.notify.*`）或指定用户（`global.system.notify.{用户名}`）发布通知/警告/提醒。
- **业务实时提醒**：订单状态、审批待办、监控告警等需要实时触达在线用户的场景（业务自定义 topic，如 `global.biz.order.{orderId}`）。
- **轻量实时看板**：单向数据下发场景（SSE 不支持客户端→服务端的流内上行，上行走独立 HTTP 端点）。

不适用：高频双向通信（请评估 WebSocket 传输，见 `push-mod/README.md`）、离线消息必达（见 5.3 消息丢失）。

---

## 二、Vue 前端接入与使用方式

### 2.1 本项目（app-ui-vue）——开箱即用

本项目已完成全部接线，**登录成功即自动建连、登出自动断开，无需任何额外配置**：

| 接线点 | 位置 | 作用 |
|--------|------|------|
| 自动建连 | `store/modules/user.js` 的 `getInfo` | 登录态就绪后 `dispatch('push/startConnection', null, { root: true })` |
| 自动断开 | `store/modules/user.js` 的 `logout` | 登出前 `dispatch('push/stopConnection', null, { root: true })` |
| 幂等兜底 | `App.vue` 的 `mounted` | 页面刷新后登录态仍在时补启动；`beforeunload` 关闭连接 |
| 状态展示 | `layout/components/NavBar.vue` | 铃铛 + 未读红点 + 连接状态点（i18n `push.status.*`） |
| 消息中心 | `components/PushNotificationCenter/index.vue` | 抽屉式历史消息列表（全部已读/清空） |
| 通知发布 | `views/push/notify-admin/index.vue` | 管理端发布页（全体/指定用户、预览、历史） |

> 注意：`user` 模块是 `namespaced: true`，其内部跨模块 dispatch **必须带 `{ root: true }`**，否则报 `unknown local action type`，推送链路静默失效。该坑已修复过两次，修改 `user.js` 时请留意。

### 2.2 新业务扩展接入（推荐方式）

业务代码**只应依赖门面 `@/push`**，不要直接触碰 `sse-client.js` / `notify.js`。

#### 2.2.1 初始化配置

本项目已由 store 的 `startConnection` 完成初始化，业务通常**无需再初始化**。若在新页面/新系统中独立接入：

```js
import { initPush, closePush, CONNECTION_STATUS } from '@/push'
import { getToken } from '@/utils/auth'

initPush({
  token: getToken(),                  // 当前登录令牌（必填）
  userName: this.userName,            // 用于缺省拼接 per-user 主题（必填）
  // topics: ['global.biz.order.1001'], // 可选：显式指定订阅主题，缺省为 global.system.notify.{userName}
  config: {                           // 可选：覆盖连接默认参数（见 PUSH_DEFAULTS）
    heartbeatInterval: 30_000,        // 心跳周期（毫秒）
    heartbeatMaxFailures: 3,         // 心跳连续失败阈值（达阈判定会话已死，换票重连）
    reconnectBaseDelay: 1_000,       // 重连退避基数
    reconnectMaxDelay: 30_000,       // 重连退避上限
    reconnectJitter: 1_000,           // 随机抖动上限（防惊群）
    topicPrefix: 'global.system.notify.'
  },
  onStatus: status => {              // 可选：连接状态回调
    // idle | connecting | connected | reconnecting | closed
  },
  onMessage: message => {            // 可选：消息拦截，返回 false 可终止弹窗与后续分发（本项用于去重）
    return true
  },
  onRead: message => {               // 可选：用户查看/关闭弹窗回调
    console.log('已读', message.id)
  }
})
```

`initPush` 幂等（连接层忽略重复调用），消息流水线为：**调用方拦截（去重）→ 渲染器（弹窗）→ 业务处理器分发**。

#### 2.2.2 订阅推送

```js
import { subscribeTopics, unsubscribeTopics } from '@/push'

// 动态追加业务主题（连接未就绪时先登记，握手后统一订阅；重复主题幂等）
subscribeTopics(['global.biz.order.1001', 'global.biz.task.*'])

// 动态退订（连接就绪时同步上行退订帧）
unsubscribeTopics(['global.biz.order.1001'])
```

缺省订阅主题为 `global.system.notify.{当前登录用户名}`（`TOPIC_PREFIX` 拼接），由 `startConnection` 自动完成，业务无感。

#### 2.2.3 接收消息

**方式一：注册消息处理器（推荐，与 UI 解耦）**

```js
import { registerMessageHandler } from '@/push'

// 按类型注册（notify | warning | reminder），返回取消注册函数
const off = registerMessageHandler('warning', message => {
  console.log('收到警告', message.id, message.title)
})

// '*' 通配：命中全部消息（精确类型处理器先于通配执行）
registerMessageHandler('*', message => {
  // 埋点、静默更新等
})
```

单个处理器抛出的异常会被捕获隔离，不影响其它处理器与 SSE 流。

**方式二：读取 Vuex 状态（UI 呈现）**

```js
import { mapGetters } from 'vuex'

export default {
  computed: {
    // push 模块是 namespaced，必须带命名空间！
    ...mapGetters('push', ['pushConnectionStatus', 'pushMessages', 'pushUnreadCount'])
  },
  methods: {
    markRead(id) { this.$store.dispatch('push/markRead', id) },
    markAll() { this.$store.dispatch('push/markAllRead') }
  }
}
```

| Getter | 说明 |
|--------|------|
| `pushConnectionStatus` | 连接状态（idle/connecting/connected/reconnecting/closed） |
| `pushMessages` | 消息列表（新消息在前，内存上限 200 条滚动淘汰） |
| `pushUnreadCount` | 未读数（派生值） |

**方式三：管理端发布（发通知）**

```js
import { publishNotify, validateNotifyForm } from '@/push/admin'

const form = {
  title: '系统维护通知',
  content: '今晚 22:00-24:00 系统升级维护',
  type: 'notify',        // notify | warning | reminder
  priority: 'high',      // low | normal | high | urgent
  url: '/notice/1',      // 可选：点击弹窗跳转
  scope: 'users',        // 'all' 全体（通配 global.system.notify.*）| 'users' 指定用户
  users: ['zhangsan']    // scope 为 users 时必填
}

const check = validateNotifyForm(form)   // 结构化校验，不抛异常
if (!check.valid) return

const result = await publishNotify(form)
// result.summary: { total, delivered, failed }
// result.results: [{ target, topic, ok, messageId?, onlineCount?, error? }]
// 单条失败不中断其余投递；发布历史自动写入 localStorage（上限 50 条）
```

#### 2.2.4 权限申请

- **接收**：订阅权限以 **auth 签票时下发的授权集** 为准。服务端签票时点经链式 SPI `IPushTopicGranter` 计算可订阅主题集，写入凭证一等字段 `grantedTopics`（字符串数组，主题模式清单）随 auth 响应返回，前端可据此按需订阅；订阅时服务端按授权集实时校验，**越权订阅被拒**（ERROR 帧，见 2.2.5）。授权集三层来源：
  - **硬编码本人频道**：`tbs.push.channel.{userId}` 由服务端认证身份推导，建连时服务端自动订阅，无需申请也不依赖客户端自报；
  - **配置白名单**：`tbs.push.grant-public-topics`（默认 `global.broadcast`）中的公共主题对全体用户放行；
  - **业务 SPI 扩展**：实现 `IPushTopicGranter` 注册为 Spring Bean 即可按角色/业务规则追加主题（支持订阅侧通配模式），多个授权器按注册顺序链式合并去重，单个异常仅跳过不阻断签票。

  保留前缀 `tbs.push.channel.*` **禁止客户端订制任何形态**（含通配窃听写法），只放行精确本人频道，杜绝跨用户信令元数据窃听。自定义业务主题建议约定命名空间（`global.biz.{业务}.{标识}`）并经上述白名单或 SPI 纳入授权。
- **发布**：`POST /private/push/send` 为私有接口，须持有有效登录令牌（请求头 `Authorization`）。**当前未做细粒度权限注解**，任何登录用户均可调用；如需收敛到管理员，请在业务层自行加权限校验（见 5.1）。
- **页面**：管理端通知发布页（`views/push/notify-admin`）按动态路由权限分配，与其它菜单权限一致。

#### 2.2.5 异常处理

模块自身已内置完整容错，业务侧只需感知：

| 异常 | 模块行为 | 业务侧建议 |
|------|----------|------------|
| 认证签票失败 | 打 warn 日志，进入退避重连；登录态失效（刷新失败 / 本地已无 token）时停止重连置 closed | 监听状态变 closed 且非登出触发时引导重新登录 |
| SSE 流断开 | 换票自动重连（指数退避，每次建连实时取最新 token），重连后自动重订阅 | 监听 `onStatus`/`pushConnectionStatus` 展示状态即可 |
| 消息 JSON 非法 / 缺 id 或 type | 丢弃该条（warn 日志），不影响后续消息 | 发送端务必遵守消息契约（见 3.3） |
| 处理器抛异常 | 捕获隔离，不影响其它处理器 | 处理器内部尽量自兜底 |
| 订阅/退订/心跳上行失败 | 订阅失败 warn 日志（重连后全量重订阅自愈）；心跳连续失败达 `heartbeatMaxFailures`（默认 3）判定会话已死，关流换票重连 | 关注 `reconnecting` 状态超过 1 分钟未恢复时告警 |
| 越权订阅（主题不在授权集） | 服务端回 ERROR 帧（code 1008 `SUBSCRIBE_DENIED`，含 i18n 文案），**仅拒绝该次订阅不断流**，其余订阅与心跳不受影响 | 校验主题是否在签票返回的 `grantedTopics` 内；升权需重新 auth 换票重连（授权集是签票时点快照） |

控制台统一以 `[push]` 前缀输出告警，可作为排障入口。

---

## 三、其他语言 / 技术栈接入方式

SSE 本质是 `text/event-stream` 格式的 HTTP 长连接，**任何支持 HTTP 流式读取的语言都能接入**（curl、Java、Python、Go 等）。前端仓库使用 `event-source-polyfill`，纯技术栈直接用原生 `EventSource` 或流式 HTTP 客户端即可。

### 3.1 通用接口清单

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 签发凭证 | POST | `/private/push/auth` | 换取一次性 ticket |
| 建立下行流 | GET | `/private/push/sse/connect?push-ticket={ticket}` | SSE 事件流 |
| 上行帧 | POST | `/private/push/sse/upload` | 订阅/退订/心跳 |
| 发布消息 | POST | `/private/push/send` | 发布到指定 topic |
| 在线会话数 | GET | `/private/push/online` | 当前全部在线会话数 |

### 3.2 鉴权方式

- **认证体系**：所有 `/private/**` 接口受登录网关保护，令牌默认经 **HTTP Header `Authorization`** 携带（可配置 `tbs.web.auth.token-source` 为 header/cookie/param，字段名 `tbs.web.auth.token-filed-name`）。
- **推送双凭证机制**：登录令牌 ≠ 建连凭证。流程是「登录令牌 → auth 端点换一次性 ticket → ticket 建连」。ticket：
  - **一次性**：服务端原子消费，防重放；连接断开后必须**重新** auth 换新票，复用旧票必然失败；
  - **短时效**：默认 TTL 60s（`tbs.push.auth.ticket-ttl-millis`），签发后须立即建连；
  - 通过 query 参数 `push-ticket` 携带（EventSource 无法自定义 Header 的折中设计）。
- **身份绑定**：auth 请求体中的 `userId`/`token` 仅是客户端声明，服务端以**网关认证主体**（登录令牌解析出的用户）为唯一权威反查数字 ID 后覆盖写入凭证属性，声明与解析不一致时仅记审计日志不拒绝（兼容存量客户端）；ticket 被旁路获取时，他人携该票建连会因建连时点身份一致性校验被关闭。
- **授权集机制**：签票时点链式 `IPushTopicGranter` 计算授权主题集，写入凭证字段 `PushCredential.grantedTopics`（**唯一真源**，见 2.2.4）；建连时由凭证派生为会话属性（逗号分隔字符串只是跨层载体，不存在双写），订阅时点由 `ISubscriptionAuthorizer` 按会话属性判定。授权集为**签票时点快照**：长连接期间业务侧降权不生效、提权需断线重连换票。旧客户端（未读该字段的 Java / C# SDK 与前端）因 Jackson / System.Text.Json 忽略未知属性而不受影响；需消费可订清单时，两个 SDK 的 `PushCredential` 镜像模型已同步 `grantedTopics` / `GrantedTopics` 字段。
- **发布接口**：直接用登录令牌走 Header 鉴权，与 ticket 无关。

### 3.3 消息格式

**发布请求体**（`POST /private/push/send`）：

```json
{
  "topic": "global.system.notify.zhangsan",
  "payload": {
    "id": "M1705123456789",
    "type": "notify",
    "title": "系统通知",
    "content": "您有一条新的审批待处理，请及时处理。",
    "timestamp": 1705123456789,
    "priority": "normal",
    "sender": "system",
    "url": "/approve/123"
  }
}
```

`topic` 可省略（缺省 `global.broadcast`）；支持通配符 `*`（单级）、`#`（多级）。

> **payload 强约束**：发布端点对载荷做前置契约校验（`PushNotifyMessage`）——`id`/`type` 必填、`type`/`priority` 白名单校验（type: notify/warning/reminder；priority: low/normal/high/urgent）、`timestamp`/`priority` 缺省自动补齐（当前毫秒/normal）。非法载荷在**发布时即被拒绝**（返回 i18n 错误文案 `push.notify.*`），不再依赖接收端容错丢弃。

**业务消息契约**（payload 字段）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | **是** | 消息唯一标识（接收端据此去重，缺失则整条被丢弃） |
| `type` | string | **是** | `notify`（通知）/ `warning`（警告）/ `reminder`（提醒），白名单外丢弃 |
| `title` | string | 否 | 标题，缺省空串 |
| `content` | string | 否 | 正文，缺省空串 |
| `timestamp` | number | 否 | 毫秒时间戳，缺省取接收时刻 |
| `priority` | string | 否 | `urgent` / `high` / `normal` / `low`，缺省 normal（影响弹窗时长：urgent/high 常驻，normal 4.5s，low 3s） |
| `sender` | string | 否 | 发送者标识 |
| `url` | string | 否 | 点击弹窗跳转地址（相对路由或完整 URL） |

**上行帧**（`POST /private/push/sse/upload`）——`type` 为帧类型**大写枚举名**：

```json
{ "sessionId": "uuid", "frame": { "version": 1, "type": "SUBSCRIBE", "topic": "global.system.notify.zhangsan" } }
```

```json
{ "sessionId": "uuid", "frame": { "version": 1, "type": "UNSUBSCRIBE", "topic": "global.system.notify.zhangsan" } }
```

```json
{ "sessionId": "uuid", "frame": { "version": 1, "type": "PING" } }
```

> 注意两套表达勿混用：**上行帧 type 用大写枚举名**（SUBSCRIBE/PING），**下行 SSE 事件名是小写值**（hello/data/pong/error）。

**下行 SSE 事件**：每个事件的 `data` 为帧对象 JSON 字符串，其 `payload` 字段为内容 JSON 字符串（**需二次 JSON 解析**）：

| 事件名 | 触发时机 | payload 内容 |
|--------|----------|--------------|
| `hello` | 建连成功握手帧 | `{"sessionId":"...","version":1}`（字符串，二次解析取 sessionId） |
| `subscribe` | 订阅确认 | 订阅结果信息 |
| `unsubscribe` | 退订确认 | 退订结果信息 |
| `data` | 业务消息下发 | 业务消息 JSON（见上契约） |
| `ping` | 服务端心跳探测 | — |
| `pong` | 对上行 PING 的应答 | —（作为链路活性观测点） |
| `error` | 协议/凭证错误 | 错误信息 |

### 3.4 通用交互流程（curl 演示）

```bash
# 0. 登录取令牌（项目现有登录接口，略） → 得到 TOKEN

# 1. 换取一次性凭证（60 秒内用于建连）
curl -X POST http://host:8080/private/push/auth \
  -H "Authorization: ${TOKEN}" -H "Content-Type: application/json" \
  -d '{"token":"${TOKEN}","userId":"zhangsan"}'
# 响应 data: { "ticket": "...", "sessionId": "...", "expireAt": ..., "grantedTopics": ["tbs.push.channel.1001","global.broadcast"] }

# 2. 建立 SSE 下行流（-N 禁用缓冲；必须带 Authorization 头）
curl -N "http://host:8080/private/push/sse/connect?push-ticket=${TICKET}" \
  -H "Authorization: ${TOKEN}"
# 首事件: event: hello → 二次解析 payload 取 sessionId

# 3. 订阅主题（用 hello 下发的 sessionId）
curl -X POST http://host:8080/private/push/sse/upload \
  -H "Authorization: ${TOKEN}" -H "Content-Type: application/json" \
  -d '{"sessionId":"${SESSION_ID}","frame":{"version":1,"type":"SUBSCRIBE","topic":"global.system.notify.zhangsan"}}'

# 4. 发布消息（另一终端，任意已登录客户端）
curl -X POST http://host:8080/private/push/send \
  -H "Authorization: ${TOKEN}" -H "Content-Type: application/json" \
  -d '{"topic":"global.system.notify.zhangsan","payload":{"id":"M-001","type":"notify","title":"测试","content":"hello","priority":"normal"}}'
# 响应 data: { "messageId": "...", "topic": "...", "onlineCount": 1 }

# 5. 步骤 2 的连接收到 event: data，二次解析 payload 得业务消息

# 6. 心跳保活（周期性，默认前端 30s 一次）
curl -X POST http://host:8080/private/push/sse/upload \
  -H "Authorization: ${TOKEN}" -H "Content-Type: application/json" \
  -d '{"sessionId":"${SESSION_ID}","frame":{"version":1,"type":"PING"}}'
```

其它语言要点：Java 用 `OkHttp`/`HttpClient` 的流式响应或 `okhttp-sse`；Python 用 `requests(stream=True)` 逐行读 `event:`/`data:` 行。WebSocket 技术栈可直接切后端传输（`tbs.push.transport=websocket`，`ws://host/push/ws?push-ticket=...`，帧语义相同，详见 `push-mod/README.md`）。

---

## 四、端到端完整流程说明

### 4.1 主链路（注册 → 授权 → 订阅 → 发送 → 接收）

```text
用户登录(取令牌 TOKEN)
   │
   ├─ 授权: POST /private/push/auth {token}
   │        ← data: {ticket, sessionId, expireAt, grantedTopics}  (ticket 一次性, TTL 60s)
   │
   ├─ 建连: GET /private/push/sse/connect?push-ticket=xxx   (Header: Authorization)
   │        ← event: hello (二次解析 payload 取 sessionId)
   │
   ├─ 订阅: POST /sse/upload {sessionId, frame: SUBSCRIBE topic}   ← 事件: subscribe 确认
   │
   ├─ 保活: 每 30s POST /sse/upload {frame: PING}                 ← 事件: pong
   │
发送方: POST /private/push/send {topic, payload}  → 投递给订阅该 topic 的全部在线会话
   │
   └─ 接收: event: data → 二次解析 payload → 契约校验(缺 id/type 则丢弃)
            → id 去重(Set) → 入列(上限200) → 弹窗(按优先级定时) → 业务处理器
```

### 4.2 失败与重试机制

| 阶段 | 失败表现 | 处理机制 |
|------|----------|----------|
| auth 签票 | 请求失败/401 | 退避后整体重试（重新签票，每次实时取最新 token）；登录态失效（拦截器标记 authExpired）或本地已无 token 时停止重连，置 closed 等待重新登录 |
| SSE 建连 | error 事件 | 关流 → **重新 auth 换新票**（旧票已消费不可复用）→ 退避重连 |
| 订阅上行 | upload 请求失败 | warn 日志；重连成功后随全量主题统一重订阅（自然自愈） |
| 越权订阅 | 下行 error 事件（帧 code 1008） | 该次订阅被拒不断流；按授权集（2.2.4）核对主题，必要时重新 auth 换票重连刷新授权快照 |
| 上行守卫拒绝 | 下行 error 事件（帧 code 1009） | 携他人 sessionId 上行或会话绑定身份与当前主体不符，该帧被丢弃不断流；前端正常链路不会触发，出现即按归属异常排查 |
| 心跳上行 | PING 请求失败 | warn 日志，下一周期（30s）重试；连续失败达 `heartbeatMaxFailures`（默认 3）判定会话已死（下行流可能仍开着但上行全死），主动关流转换票重连自愈 |
| 链路空闲 | 服务端超 `tbs.push.heartbeat-timeout-millis` 无活动 | 服务端主动关闭会话（完整清理链：物理拆除→注销→订阅清理→断开拦截器，不依赖半开连接的事件回传）→ 客户端 error → 自动重连 |
| 发布投递 | send 请求失败 | `publishNotify` 按 allSettled 语义单条重试不中断其余；调用方按 `results[].ok` 决定是否人工重发 |
| 消息非法 | 缺 id/type、JSON 损坏 | 接收端容错丢弃（warn 日志），不影响后续消息 |

**退避算法**：`delay = min(30s, 1s × 2^attempts) + random(0~1s)`，首次失败约 1s 重试，持续失败逐步拉长到 30s 封顶，随机抖动防多客户端惊群。重连成功后 `attempts` 归零。

**登出**：主动关闭（`close()`），置 `manualClose` 标记阻止一切自动重连；再次登录重新走完整链路。

---

## 五、注意事项

### 5.1 权限与安全

- **身份绑定三层模型**：身份在签票点绑定（网关认证主体为唯一权威，表单声明仅被覆盖与审计）；一致性在建连时点校验（凭证绑定身份≠当前请求主体 → 直接关闭会话，close 原因 `identity-mismatch`）；归属在上行时点校验（协议层上行守卫链 `IUploadGuard` 在帧处理器内对**全部上行帧**判定：会话绑定身份≠当前登录主体则回发 ERROR 帧（code 1009 `UPLOAD_DENIED`）丢弃该帧不断流，SSE 与 WebSocket 两种传输统一覆盖；Controller 不做任何校验编排）。业务侧可注册自定义 `IUploadGuard` Bean 叠加或替换默认守卫（未注册任何守卫时上行全放行）。
- **订阅鉴权**：订阅点按授权集判定（见 2.2.4/3.2），未注册 `ISubscriptionAuthorizer` 时全放行（向后兼容无鉴权部署）；保留前缀 `tbs.push.channel.*` 无论授权集如何配置都拒绝客户端通配订阅。
- **服务端主动关闭能力**：传输适配器契约 `ITransportAdapter` 覆盖「建连→上下行→主动/被动关闭」全链路，`closeSession(sessionId, reason)` 一次调用完成物理拆除与清理链（注销→订阅清理→断开拦截器），不依赖事件回传——适用于 token 实际失效、无心跳场景踢线、管理踢线等业务场景（关闭原因常量见 `PushFrameConstants`，如 `token-expired`）；心跳超时监控已走本链路。清理链幂等门禁为注册表注销的原子唯一性，主动关闭与通道事件回传竞态时仅一方执行清理，业务侧可放心双入口调用。
- **数据面独立安全**：消息拉取接口（`/private/push/message/list` 等）按认证主体过滤，只返回本人收件箱消息——即使信令面配置不当，消息内容也不会串号。
- 发布接口 `/private/push/send` 仅要求登录，**无细粒度权限校验**——任何登录用户都能向任意 topic 发消息（接收人由后端按 topic 解析）。生产环境若要收敛，建议在业务层加管理员权限校验，或拆分出独立发布端点。
- ticket 为一次性 + 60s 短时效 + 服务端原子消费，具备防重放能力；不要在日志中输出 ticket/token 原文（后端已做脱敏）。
- payload 经消息源与投递链路，**不要放置敏感明文**（密码、密钥等）。
- 自定义 topic 建议遵循命名空间约定（`tbs.push.channel.*` 信令保留、`global.system.*` 系统保留、`global.biz.{业务}.*` 业务自用），并通过授权白名单/SPI 显式授权后才可订阅。

### 5.2 浏览器与网络兼容性

- 原生 `EventSource` 无法携带自定义 Header，而网关对 `/private/**` 要求 `Authorization`——**必须使用 `event-source-polyfill`**（XHR 流实现，本项目已采用）或等价方案。
- SSE 要求响应**不被代理缓冲**：Nginx 需 `proxy_buffering off;`（或后端输出 `X-Accel-Buffering: no`），否则消息被攒批、延迟明显；开发环境 vue devServer proxy 已配置可用。
- 网关/负载均衡的**读超时须大于心跳周期**（前端 30s PING、服务端默认空闲超时 90s、本项目配置 600s），否则空闲连接被中间层掐断会引发频繁重连。
- HTTP/1.1 同域浏览器长连接上限约 6 条，多标签页同开会占满连接池，导致其它请求排队——重场景请评估 HTTP/2 或合并订阅。
- IE/老浏览器依赖 polyfill 的 XHR 流式能力，兼容目标包含 IE 时需回归验证。

### 5.3 消息丢失（重要）

- **SSE 是"在线投递"语义**：断线期间发布的消息不会补发给重连后的客户端（内存消息源，不落库）。重要业务**必须提供拉取兜底**（页面加载/重连成功后主动查询一次），推送只作加速。
- 消息列表为**内存态**：刷新页面/重登后历史与已读状态重置（已读持久化为预留扩展点 `persistRead`，当前空实现）。
- 内存消息源容量上限 `tbs.push.memory-message-capacity`（默认 10000），满时淘汰最早消息。

### 5.4 重复推送

- 通配符主题重叠（如同时订阅 `global.system.notify.*` 与 `global.system.notify.zhangsan`）会收到同一消息多次——前端已按 `id` 去重兜底，但**自研接收端必须实现 id 去重**，业务处理器也应幂等。
- 发送端对同一 id 重复发布属正常重试语义，接收端天然去重，可放心失败重发（同 id）。

### 5.5 性能

- 每用户一条长连接，会话数 = 在线用户数，评估服务器连接数与网关并发。
- 发布是"发布即投递给在线订阅者"，`onlineCount` 可判断触达面；不在线用户收不到（见 5.3）。
- 前端消息列表上限 200 条滚动淘汰，发布历史本地 50 条，均有硬上限防内存膨胀。
- payload 控制在合理大小（建议 KB 级以内），避免阻塞事件流。

---

## 六、常见问题与排查建议

| 现象 | 排查步骤 |
|------|----------|
| 状态一直停在「连接中」 | ① Network 看 `POST /private/push/auth` 是否 200（401 → 登录过期，重新登录）；② 看 `GET /private/push/sse/connect` 的 HTTP 状态与响应头 `Content-Type: text/event-stream`（401 → 未带 Authorization 头，检查是否用了原生 EventSource；404/404* → 后端 `tbs.push.enable=true` 且 `transport=sse`）；③ 控制台搜 `[push]` 告警 |
| auth 成功但收不到 hello | 确认 SSE 响应未被代理缓冲（见 5.2）；确认 ticket 在 60s 内使用且为首次使用 |
| 收不到业务消息 | ① 发送方响应是否成功——payload 违反强约束契约（缺 id/type、type/priority 非白名单值）会在**发布时即报错**（错误文案 `push.notify.*`）；② 发送成功的响应里 `onlineCount` 是否 ≥1（=0 → 目标不在线，属正常丢失，见 5.3）；③ 订阅与发布 topic 是否匹配（注意通配符级别）；④ 接收端控制台是否有 `[push] 业务消息缺少必填字段 id/type`——仅历史/旁路消息可能出现，正常发布已被强约束拦截 |
| 订阅返回 code 1008 被拒 | 主题不在签票下发的 `grantedTopics` 授权集内：检查 `tbs.push.grant-public-topics` 配置或业务 `IPushTopicGranter` 扩展是否覆盖该主题；授权变更前签发的旧票不含新授权，需重新 auth 换票重连；`tbs.push.channel.*` 前缀主题只允许精确本人频道（且服务端已自动订阅） |
| 消息弹了两次 | ① 是否绕过 store 的去重直接调用了 `initPush`（onMessage 没做去重且返回 true）；② 自研处理器是否重复注册；③ 前端已有 id 去重，检查是否多页面各自建连 |
| 反复「重连中」不恢复 | ① 后端是否重启（恢复后应自动连上）；② auth 是否持续 401（令牌过期）；③ 退避已到 30s 封顶，看服务端会话与网关超时配置（见 5.2） |
| 登出后仍在收消息 | 确认 `logout` 中 `dispatch('push/stopConnection', null, { root: true })` 未被删改（namespaced 跨模块必须带 root） |
| 弹窗点击跳转无效 | payload 的 `url` 是否为有效前端路由（相对路径走 `router.push`，完整 URL 走跳转） |
| 已读状态刷新后丢失 | 当前为内存态设计（见 5.3），如需持久化在 `store/modules/push.js` 的 `persistRead` 扩展点接入 |
| 控制台 `unknown getter: pushMessages` | `mapGetters` 未带 `'push'` 命名空间：`mapGetters('push', [...])` |
| 控制台 `unknown local action type: push/startConnection` | namespaced 模块内跨模块 dispatch 漏了 `{ root: true }` |

### 快速自检清单（联调新接入方）

1. 登录后 Network 出现：`POST /private/push/auth` 200 → `GET .../sse/connect?push-ticket=...` pending（EventStream）；
2. 收到 hello 事件且能解析出 sessionId；
3. `POST /private/push/send`（topic 命中订阅）返回 `onlineCount ≥ 1`；
4. SSE 流收到 `data` 事件，二次解析 payload 通过契约校验；
5. 断开后端 30s 内出现自动重连日志，恢复后自动重订阅并继续收消息；
6. 同 id 重发一次，接收端不重复入列/弹窗。

---

## 附录：相关配置项速查

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `tbs.push.enable` | `false` | 推送功能总开关（项目须显式置 true） |
| `tbs.push.transport` | `websocket` | 传输选择（`sse` / `websocket`，互斥装配） |
| `tbs.push.heartbeat-timeout-millis` | `90000` | 服务端会话空闲超时（本项目配置 600000） |
| `tbs.push.auth.ticket-ttl-millis` | `60000` | 一次性建连凭证有效期 |
| `tbs.push.grant-public-topics` | `global.broadcast` | 签票授权集的公共主题白名单（支持订阅侧通配模式，业务 `IPushTopicGranter` 可另行扩展） |
| `tbs.push.memory-message-capacity` | `10000` | 内存消息源容量上限 |
| `tbs.push.memory-record-capacity` | `10000` | 内存投递记录容量上限 |
| `tbs.web.auth.token-source` | `HEADER` | 登录令牌来源（HEADER/COOKIE/PARAM/BODY） |
| `tbs.web.auth.token-filed-name` | `Authorization` | 登录令牌字段名 |
| `tbs.web.secured-paths` | `/api/**` | 网关受保护路径（本项目覆盖为 `/private/**` 等） |

前端连接默认参数见 `app-ui-vue/src/push/push-message.js` 的 `PUSH_DEFAULTS`（心跳 30s、心跳连续失败阈值 3、退避 1s→30s+抖动 1s），可在 `initPush({ config })` 逐项覆盖。
