# push 推送通知模块

## 概述

`src/push` 是基于 SSE 的消息订阅与通知基础工具模块，负责与服务端推送通道的连接管理、
协议帧解析、消息分发与弹窗呈现。模块按「协议 → 连接 → 门面 → 状态/渲染」分层，
UI 库依赖被隔离在默认渲染器内，可整体替换；业务可按消息类型注入自定义处理器、
动态增删订阅主题。上层集成点只应依赖门面 `src/push/index.js` 导出的 API。

## 消息数据结构

服务端 DATA 帧 `payload` 承载的业务消息 JSON（字符串，前端需二次 `JSON.parse`）。
必填字段缺失或类型非法时该条消息被容错丢弃（告警但不阻断连接与后续消息）。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 消息唯一标识，前端据此去重 |
| type | string | 是 | 消息类型：`notify`（通知）/ `warning`（警告）/ `reminder`（提醒） |
| title | string | 否 | 标题，缺省取 i18n `push.defaultTitle` |
| content | string | 否 | 正文 |
| timestamp | number | 否 | 毫秒时间戳，缺省取前端接收时刻 |
| priority | string | 否 | 优先级：`low` / `normal` / `high` / `urgent`，非法值回退 `normal` |
| sender | string | 否 | 发送方标识 |
| url | string | 否 | 点击弹窗/消息项跳转的业务路由 |

示例数据（见 `push-message.js` 的 `MESSAGE_EXAMPLE`）：

```json
{
  "id": "M1705123456789",
  "type": "notify",
  "title": "系统通知",
  "content": "您有一条新的审批待处理，请及时处理。",
  "timestamp": 1705123456789,
  "priority": "normal",
  "sender": "system",
  "url": "/approve/123"
}
```

发送端使用 `POST /public/push/send`，`topic` 填订阅主题（支持 `*` 单级 / `#` 多级通配符）。

## 模块结构

```
src/push/
├── push-message.js    # 协议契约层：常量、默认配置 PUSH_DEFAULTS、hello/data 帧解析校验（纯函数）
├── sse-client.js      # 连接层：认证→建连→握手→订阅→心跳→退避重连；多主题与配置覆盖；单例 sseClient
├── renderer.js        # 渲染器注册表：registerRenderer/unregisterRenderer/notifyMessage（核心与 UI 解耦枢纽）
├── notify.js          # 默认渲染器：Element UI Notification 堆叠实现（唯一 element-ui 依赖点）
├── index.js           # 门面：initPush/subscribeTopics/registerMessageHandler/closePush 等统一出口
└── README.md          # 本文档
```

依赖方向（单向，无环）：

```
store/modules/push.js ──> push/index.js（门面）
push/index.js ──> sse-client.js / renderer.js / notify.js
sse-client.js ──> api/push.js（HTTP 上行）+ push-message.js（协议）
notify.js ──> element-ui + push-message.js
```

## 连接时序

```
登录成功（user/getInfo）
  └─> dispatch('push/startConnection', null, { root: true })
        └─> store action 委托门面 initPush({ token, userName, onStatus, onMessage, onRead })
              ├─> POST /private/push/auth        # 换一次性 ticket（TTL 60s）
              ├─> EventSource /private/push/sse/connect?push-ticket=xxx   # 下行长连接
              │     ├─ hello 帧 → 二次解析 payload 取 sessionId
              │     ├─ 上行 SUBSCRIBE 订阅 global.system.notify.{userName}
              │     ├─ 周期上行 PING（30s，远小于后端空闲超时）保活
              │     └─ data 帧 → 二次解析业务消息 → onMessage 流水线
              └─ 断线（error）→ 关流 → 指数退避换票重连（1s→30s + 随机抖动）
登出 / 页面卸载
  └─> dispatch('push/stopConnection', null, { root: true }) → closePush() 关闭且不再重连
```

要点：

- ticket 一次性消费，EventSource 无法自定义 Header，凭证走 query 参数；
  原生自动重连会拿已失效 ticket 反复失败，因此断线必须由 error 回调换票重连。
- 连接状态机 `CONNECTION_STATUS`：`idle / connecting / connected / reconnecting / closed`，
  经 `onStatus` 回调同步到 store，由 NavBar 状态点实时展示（i18n `push.status.*`）。

## 消息流水线与去重

`initPush` 组装的 onMessage 流水线顺序：

1. **调用方拦截（去重）**：store 的 `onMessage` 以模块级 `seenIds` Set 按消息 id 去重，
   重复消息返回 `false`，门面终止后续分发；新消息入列（上限 200 条滚动淘汰）后返回 `true`。
2. **渲染器分发**：`notifyMessage(message, { onRead })` 依次调用已注册渲染器；
   默认渲染器按优先级决定时长（urgent/high 常驻，normal 4.5s，low 3s），点击/关闭经
   `onRead(message)` 回写已读。
3. **处理器分发**：`dispatchMessage` 按 `type` 精确命中 + `'*'` 通配依次调用，
   单个处理器异常被捕获隔离。

单条消息解析/校验失败（非法 JSON、缺 id/type）在协议层即被丢弃并告警，不影响 SSE 流与后续消息。

## 公开 API（src/push/index.js）

| API | 签名 | 说明 |
|-----|------|------|
| initPush | `initPush({ token, userName, topics?, config?, onStatus?, onMessage?, onRead? })` | 建连并组装流水线；幂等 |
| subscribeTopics | `subscribeTopics(topics: string \| string[])` | 动态追加订阅；未就绪时登记，握手后统一订阅 |
| unsubscribeTopics | `unsubscribeTopics(topics: string \| string[])` | 动态退订；就绪时同步上行退订帧 |
| registerMessageHandler | `registerMessageHandler(type, handler) => 取消函数` | 按 type（或 `'*'`）注册处理器，重复注册幂等 |
| unregisterMessageHandler | `unregisterMessageHandler(type, handler)` | 注销处理器 |
| dispatchMessage | `dispatchMessage(message)` | 手动向处理器分发一条消息 |
| registerRenderer | `registerRenderer(renderer) => 取消函数` | 注册渲染器（renderer.js） |
| unregisterRenderer | `unregisterRenderer(renderer)` | 注销渲染器 |
| closePush | `closePush()` | 关闭连接并阻止重连 |
| CONNECTION_STATUS | 常量对象 | 连接状态枚举 |

## 扩展指南

### 按消息类型注入业务逻辑

```js
import { registerMessageHandler } from '@/push'

const off = registerMessageHandler('warning', message => {
  // 例如：告警消息上报埋点 / 触发业务弹窗
})
// 需要注销时：off()
```

### 动态订阅其他主题

```js
import { subscribeTopics, unsubscribeTopics } from '@/push'

subscribeTopics(['global.system.announce'])   // 连接就绪后自动生效
unsubscribeTopics(['global.system.announce'])
```

### 替换/叠加渲染器

```js
import { registerRenderer, unregisterRenderer } from '@/push'

// 叠加：注册自定义呈现（与默认弹窗并存）
registerRenderer((message, { onRead }) => { /* 自定义 UI */ })

// 替换：先注销默认 Element UI 渲染器再注册新实现（模块核心不依赖 element-ui）
```

### 配置覆盖

`connect`/`initPush` 传入 `config` 可逐项覆盖 `PUSH_DEFAULTS`：
`topicPrefix`、`heartbeatInterval`、`reconnectBaseDelay`、`reconnectMaxDelay`、`reconnectJitter`。

### 已读状态持久化（预留扩展点）

`store/modules/push.js` 的 `persistRead(messages)` 为空实现钩子，
后续接入 localStorage 或后端已读同步即可，本轮维持内存态（刷新后重置）。

## 现有集成点（勿随意改动）

| 位置 | 接线 |
|------|------|
| `store/modules/user.js` | getInfo 成功后 `dispatch('push/startConnection', null, { root: true })`；logout 中 stopConnection（同为 root 作用域） |
| `App.vue` | mounted 幂等兜底建连；`beforeunload` 关闭连接 |
| `layout/components/NavBar.vue` | 铃铛 + 连接状态点 + 未读红点，`mapGetters('push', [...])`（必须带命名空间） |
| `components/PushNotificationCenter/index.vue` | 消息中心抽屉（历史/已读未读/清空） |
| `store/index.js` | 注册 namespaced 的 push 模块 |

## 手动验证清单

1. 登录后 Network：`POST /api/private/push/auth` 200 → `GET /api/private/push/sse/connect?push-ticket=...` 处于 pending（EventStream）。
2. NavBar 状态点由「未连接」变「已连接」（i18n `push.status.*`）。
3. `POST /public/push/send`（topic 为 `global.system.notify.{用户名}` 或其通配）→ 右上角弹窗 + 铃铛红点 + 消息中心历史。
4. 重复推送同 id 消息 → 不重复弹窗、不重复入列。
5. 重启后端使 SSE 断开 → 状态变「重连中」，后端恢复后自动换票重连并重新订阅。
6. 登出 → SSE 连接关闭，不再自动重连；再次登录自动重建。
