# push 推送通知模块

## 概述

`src/push` 是基于「信令 + 拉取」新链路的消息订阅与通知基础工具模块：SSE 长连接仅承载
SIGNAL 信令（某频道有新消息的轻量提示），业务消息内容由前端收到信令后统一调用
`/private/push/message/list` 拉取并规范化入列。模块按「协议 → 连接 → 门面 → 状态/渲染」分层，
UI 库依赖被隔离在默认渲染器内，可整体替换；业务可按消息类型注入自定义处理器、
动态增删订阅主题。上层集成点只应依赖门面 `src/push/index.js` 导出的 API。

消息投递范围由后端 `TopicPrefixNotifyReceiverResolver` 按 topic 前缀查询用户表解析，
前端不再调用旧全量推送接口 `/private/push/send`（已彻底移除）。

## 消息数据结构

消息不再由 SSE DATA 帧直接下发，而由拉取接口返回的 `PushNotifyMessageVO`
经 `push-message.js` 的 `normalizeFetchedMessage` 规范化：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 消息唯一标识（后端 messageId），前端据此去重 |
| type | string | 消息类型：`notify`（通知）/ `warning`（警告）/ `reminder`（提醒） |
| title | string | 标题 |
| content | string | 正文 |
| timestamp | number | 毫秒时间戳（后端 createTime，解析失败回退前端接收时刻） |
| priority | string | 优先级：`low` / `normal` / `high` / `urgent`，非法值回退 `normal` |
| sender | string | 发送方标识 |
| url | string | 点击弹窗/消息项跳转的业务路由 |
| read | boolean | 已读标记（由后端 READ 日志派生） |

## 业务 topic 契约（前后端一致）

| topic | 接收范围 | 后端解析方式 |
|-------|----------|--------------|
| `global.broadcast`（或缺省） | 全体激活用户 | 用户表全量查询（短缓存） |
| `user.name.{userName}` | 按用户名精确匹配 | 用户表精确查询 |
| `user.search.{keyword}` | 用户名前缀搜索 | 用户表 leftLike 查询 |
| `tbs.push.channel.*` | 信令保留频道 | 拒绝解析（业务不得占用） |
| 其他未知 topic | 无接收人 | 空集（仅落库审计语义） |

常量见 `push-message.js`：`TOPIC_BROADCAST` / `TOPIC_USER_NAME_PREFIX` / `TOPIC_USER_SEARCH_PREFIX`。

## 模块结构

```
src/push/
├── push-message.js    # 协议契约层：常量（含业务 topic）、PUSH_DEFAULTS、hello/signal 帧解析、拉取消息规范化（纯函数）
├── message-display.js # 展示语义层：类型归组/语义色（Apple 系统色板）、优先级徽章、相对时间、今天-昨天-更早时间分组（消息中心与通知预览卡共享，纯函数）
├── sse-client.js      # 连接层：认证→建连→握手→订阅→心跳→退避重连；多主题与配置覆盖；单例 sseClient
├── renderer.js        # 渲染器注册表：registerRenderer/unregisterRenderer/notifyMessage（核心与 UI 解耦枢纽）
├── notify.js          # 默认渲染器：Element UI Notification 堆叠实现（唯一 element-ui 依赖点）
├── index.js           # 门面：initPush/subscribeTopics/registerMessageHandler/notifyMessage/dispatchMessage 统一出口
├── admin.js           # 管理侧发布封装：表单校验/notify 接口投递编排/本地发布历史（无组件依赖，供管理页面复用）
└── README.md          # 本文档
```

依赖方向（单向，无环）：

```
store/modules/push.js ──> push/index.js（门面）+ api/push-message.js（拉取/已读/发布）
push/index.js ──> sse-client.js / renderer.js / notify.js
sse-client.js ──> api/push.js（HTTP 上行）+ push-message.js（协议）
push/admin.js ──> api/push-message.js（sendNotify）+ push-message.js（契约）
notify.js ──> element-ui + push-message.js
push/message-display.js ──> push-message.js（类型/优先级契约）
components/PushNotificationCenter/** ──> push/message-display.js + store(push) + api/push-message.js
views/push/notify-admin/components/NotifyPreviewCard.vue ──> push/message-display.js（同构语义色）
```

## 连接与消息时序

```
登录成功（user/getInfo）
  └─> dispatch('push/startConnection', null, { root: true })
        └─> store action 委托门面 initPush({ token, userId, onStatus, onSignal })
              ├─> POST /private/push/auth        # 换一次性 ticket（TTL 60s），每次建连实时取 getToken()
              ├─> EventSource /private/push/sse/connect?push-ticket=xxx   # 下行长连接
              │     ├─ hello 帧 → 二次解析 payload 取 sessionId
              │     ├─ 上行 SUBSCRIBE 订阅 tbs.push.channel.{userId}（SIGNAL 信令频道）
              │     ├─ 周期上行 PING（30s，远小于后端空闲超时）保活；
              │     │    连续失败达 heartbeatMaxFailures（默认 3）判定会话已死 → 关流换票重连
              │     ├─ hello 握手成功 → 首次拉取（仅入列不弹窗，兜底离线期间消息）
              │     └─ signal 帧 → 防抖触发拉取（合并信令风暴）
              └─ 断线（error）→ 关流 → 指数退避换票重连（1s→30s + 随机抖动）；
                 登录态失效（authExpired）或本地无 token 时停止重连置 closed
拉取（pullLatest，store action）
  ├─> GET /private/push/message/list?limit=20&readFlag=all
  ├─ normalizeFetchedMessage 规范化 → seenIds 去重 → 入列（上限 200 条滚动淘汰）
  └─ 新增消息 → notifyMessage（渲染器弹窗，查看/关闭回传已读）+ dispatchMessage（业务处理器）
登出 / 页面卸载
  └─> dispatch('push/stopConnection', null, { root: true }) → closePush() 关闭且不再重连
```

要点：

- ticket 一次性消费，EventSource 无法自定义 Header，凭证走 query 参数；
  原生自动重连会拿已失效 ticket 反复失败，因此断线必须由 error 回调换票重连。
- 登录态令牌每次建连（含重连）都实时取 `getToken()`，不依赖 connect 时的快照，
  token 无感刷新后重连仍用新令牌；pushAuth 因登录态失效被拒（request.js 拦截器标记
  `authExpired`）或本地已无 token 时停止重连置 closed，避免过期凭证下的无效重连风暴。
- 心跳上行连续失败达 `heartbeatMaxFailures` 即判定会话已死（服务端会话可能因认证失效
  被回收但下行流仍开着），主动关流转换票重连自愈。
- 连接状态机 `CONNECTION_STATUS`：`idle / connecting / connected / reconnecting / closed`，
  经 `onStatus` 回调同步到 store，由 NavBar 状态点实时展示（i18n `push.status.*`）。
- 首次拉取（hello 兜底）仅入列不弹窗，避免登录即弹出一批历史通知；仅信令触发的增量弹窗。

## 去重与分发

1. **去重入列**：模块级 `seenIds` Set 按消息 id 幂等去重（重连/重拉不重复弹窗、不重复入列）。
2. **渲染器分发**：`notifyMessage(message, { onRead })` 依次调用已注册渲染器；
   默认渲染器按优先级决定时长（urgent/high 常驻，normal 4.5s，low 3s），点击/关闭经
   `onRead(message)` 回写已读（本地即时 + `POST /private/push/message/read` 异步回传）。
3. **处理器分发**：`dispatchMessage` 按 `type` 精确命中 + `'*'` 通配依次调用，
   单个处理器异常被捕获隔离。

## 公开 API（src/push/index.js）

| API | 签名 | 说明 |
|-----|------|------|
| initPush | `initPush({ token, userId, topics?, config?, onStatus?, onSignal? })` | 建连并订阅；信令/hello 触发 onSignal；幂等 |
| subscribeTopics | `subscribeTopics(topics: string \| string[])` | 动态追加订阅；未就绪时登记，握手后统一订阅 |
| unsubscribeTopics | `unsubscribeTopics(topics: string \| string[])` | 动态退订；就绪时同步上行退订帧 |
| registerMessageHandler | `registerMessageHandler(type, handler) => 取消函数` | 按 type（或 `'*'`）注册处理器，重复注册幂等 |
| unregisterMessageHandler | `unregisterMessageHandler(type, handler)` | 注销处理器 |
| dispatchMessage | `dispatchMessage(message)` | 向处理器分发一条消息（拉取侧调用） |
| notifyMessage | `notifyMessage(message, hooks)` | 向渲染器分发一条消息（拉取侧调用） |
| registerRenderer | `registerRenderer(renderer) => 取消函数` | 注册渲染器（renderer.js） |
| unregisterRenderer | `unregisterRenderer(renderer)` | 注销渲染器 |
| closePush | `closePush()` | 关闭连接并阻止重连 |
| CONNECTION_STATUS | 常量对象 | 连接状态枚举 |

## 消息中心 API（src/api/push-message.js）

| API | 后端接口 | 说明 |
|-----|----------|------|
| fetchMessages | `GET /private/push/message/list` | 游标分页拉取（cursor/limit/readFlag），返回 `{list, cursor, hasMore}` |
| fetchUnreadCount | `GET /private/push/message/unread-count` | 未读数（Long） |
| markRead | `POST /private/push/message/read` | 批量已读（`List<Long>` 裸数组，返回记录条数） |
| operateMessage | `POST /private/push/message/operate` | 操作回传（CLICK 等，返回写入条数） |
| sendNotify | `POST /private/push/message/notify` | 发送持久化通知（返回消息ID；接收人由后端按 topic 解析） |

## 消息中心 UI（Liquid Glass 玻璃拟态）

消息中心抽屉按「编排层 + 五个职责单一子组件 + 共享样式 token」拆分，视觉与全站
macOS 浅色布局同色系（系统蓝 #0a84ff / 绿 #34c759 / 橙 #ff9f0a / 红 #ff3b30），
数据流保持单一来源：

```
components/PushNotificationCenter/
├── index.vue            # 编排层：抽屉外壳、筛选状态（activeType/unreadOnly，视图态不进 Vuex）、三态派生、matchMedia 三档视口
├── components/
│   ├── CenterHeader.vue # 头部两行：信标+标题+未读+常驻关闭按钮 / 全部已读·清空操作组（手机端操作组转为抽屉底部毛玻璃浮层）
│   ├── FilterBar.vue    # 筛选栏：全部/只看未读分段切换 + 类型 Tab 胶囊（未知类型动态归入"其他"，各 Tab 未读角标）
│   ├── MessageList.vue  # 滚动容器：时间分组吸顶标题、滚动近底部节流加载更多（与底部按钮双通道）
│   ├── MessageCard.vue  # 单条消息卡：玻璃卡 + 语义色导轨 + 优先级徽章 + 相对时间
│   └── ListState.vue    # 三态复用：首屏骨架 / 空态（无消息 vs 筛选无结果）/ 错误态 + 重试
└── styles/
    └── _message-card.scss  # 卡片视觉 token partial，供 NotifyPreviewCard(center 模式) @import 同构复用
```

数据流与关键决策：

- **单一数据源**：消息列表、未读数、分页游标、loading/loadError 全部收敛在 `store/modules/push`；
  筛选为组件 computed 客户端派生（`filteredMessages` → `groupByDay`），不与服务端
  `readFlag=unread` 参数混用，避免游标/去重双数据源错乱（该 API 参数保留供未来服务端过滤演进）。
- **类型归组**：后端 msgType 字典扩展出的未知类型经 `resolveTypeKey` 归入 `other`，
  "其他" Tab 仅在实际存在时动态出现，未知类型绝不直出 i18n key。
- **时间分组**：`groupByDay` 基于消息 `timestamp` 按今天/昨天/更早 O(n) 分桶，分组标题吸顶。
- **三态**：首屏骨架（loading 且无数据）、错误重试（loadError 且无数据，store `retry` 动作）、
  空态区分"无消息"与"筛选无结果"（后者提供一键清除筛选）。
- **响应式三档**：手机（≤768px）100% 全屏 + 底部固定操作栏 + 安全区留白、平板（769-992px）480px、
  桌面（>992px）400px；触摸目标 ≥ 44px；移动端关闭入场错峰/呼吸/shimmer 动画降 GPU 负载。
- **同构预览**：`views/push/notify-admin/components/NotifyPreviewCard.vue` center 模式
  @import 同一 token partial 并复用 `message-display.js` 语义色，与 MessageCard 视觉严格一致。
- **清空仅本地**：后端无删除接口，清空只清内存（刷新后重新拉取），维持既有语义。

## 管理侧发布封装（admin.js）

面向管理界面的通知发布能力，全部为纯函数 + localStorage + api 层调用，不依赖任何组件：

| API | 签名 | 说明 |
|-----|------|------|
| validateNotifyForm | `validateNotifyForm(form) => { valid, errors: [{field, code}] }` | 结构化校验；code: required/tooLong/invalid/empty |
| publishNotify | `async publishNotify(form) => { ok, summary, results, record }` | 全体走 `global.broadcast` 单发；指定用户逐人 `user.name.{username}`（单条失败不中断）；完成后写入本地历史 |
| getPublishHistory / addHistoryRecord / removeHistoryRecord / clearHistory | — | 本地发布历史（key `PUSH_NOTIFY_HISTORY`，上限 50 条滚动淘汰，读写均 try-catch 容错） |
| NOTIFY_FORM_LIMITS | 常量 | 标题 50 / 正文 500 长度上限 |

接入示例（任意管理模块）：

```js
import { publishNotify, validateNotifyForm } from '@/push/admin'

const check = validateNotifyForm(form)
if (!check.valid) {
  // 按 check.errors[{field, code}] 映射提示文案
  return
}
const result = await publishNotify(form)
// result.summary: { total, delivered, failed }
// result.results: [{ target, topic, ok, messageId?, error? }]
// result.record: 已写入本地历史的记录
```

配套管理页面：`src/views/push/notify-admin/index.vue`（发布表单 + 实时预览 + 本地历史），
经动态路由接入（菜单 component 填 `push/notify-admin/index`，在系统「路由管理」界面录入）。
使用该页面搜索指定用户需具备后端 `API.QUERY.USER` 权限。

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

后端已启用**订阅鉴权**：可订阅主题以 auth 签票时下发的授权集为准——服务端返回的
`grantedTopics` 是 `PushCredential` 的独立字段（字符串数组，见 `@/api/push` 的 `pushAuth`），
需要渲染"可订清单"时直接读它，不必再从 `attributes` 里拆字符串；
本人信令频道 `tbs.push.channel.{userId}` 硬编码授权且建连时服务端已自动订阅（前端自报
订阅与之幂等并存，不必移除）；公共主题受 `tbs.push.grant-public-topics` 白名单控制，
业务主题需后端 `IPushTopicGranter` 扩展授权。**越权订阅会被服务端拒绝**（下行 error
帧，code 1008，仅拒该次订阅不断流），前端无需改码，但新主题接入前应先与后端确认
授权覆盖；授权变更需重新换票重连才生效。

### 替换/叠加渲染器

```js
import { registerRenderer, unregisterRenderer } from '@/push'

// 叠加：注册自定义呈现（与默认弹窗并存）
registerRenderer((message, { onRead }) => { /* 自定义 UI */ })

// 替换：先注销默认 Element UI 渲染器再注册新实现（模块核心不依赖 element-ui）
```

### 配置覆盖

`connect`/`initPush` 传入 `config` 可逐项覆盖 `PUSH_DEFAULTS`：
`topicPrefix`、`heartbeatInterval`、`heartbeatMaxFailures`、`reconnectBaseDelay`、`reconnectMaxDelay`、`reconnectJitter`、`sseHeartbeatTimeout`。

`sseHeartbeatTimeout` 是 `EventSourcePolyfill` 的「下行流无数据」看门狗：polyfill 默认 45 秒
无字节就主动断流重连，而心跳上行由客户端定时器驱动，后台标签页被节流或服务端推荐周期被
拉长时，两次 PONG 间隔很容易超过 45 秒。默认值放宽到 600000ms（与服务端会话回收点及
hello 下发的 `heartbeatDeadline` 对齐），真正的半开连接由服务端心跳监控、前端 deadline 与
上行失败守卫兜底；TCP 层断链不经过该看门狗，会立即触发连接级 error。

### 已读状态持久化（预留扩展点）

`store/modules/push.js` 的 `persistRead(messages)` 为空实现钩子，
后续接入 localStorage 或后端已读同步即可，本轮维持内存态（刷新后重置）。

## 现有集成点（勿随意改动）

| 位置 | 接线 |
|------|------|
| `store/modules/user.js` | getInfo 成功后 `dispatch('push/startConnection', null, { root: true })`；logout 中 stopConnection（同为 root 作用域） |
| `App.vue` | mounted 幂等兜底建连；`beforeunload` 关闭连接 |
| `layout/components/NavBar.vue` | 铃铛 + 连接状态点 + 未读红点，`mapGetters('push', [...])`（必须带命名空间） |
| `components/PushNotificationCenter/index.vue` | 消息中心抽屉编排层（筛选/时间分组/三态/响应式，子组件见 `components/PushNotificationCenter/components/`） |
| `views/push/notify-admin/components/NotifyPreviewCard.vue` | 管理侧实时预览卡（center 模式复用消息中心共享 token 保持同构） |
| `store/index.js` | 注册 namespaced 的 push 模块 |
| `views/push/notify-admin/index.vue` | 管理侧发布页面（走 admin.js → notify 接口） |

## 手动验证清单

1. 登录后 Network：`POST /api/private/push/auth` 200 → `GET /api/private/push/sse/connect?push-ticket=...` 处于 pending（EventStream）。
2. NavBar 状态点由「未连接」变「已连接」（i18n `push.status.*`）。
3. 管理页发布全体通知（`global.broadcast`）→ 在线接收人右上角弹窗 + 铃铛红点 + 消息中心历史。
4. 发布指定用户通知（`user.name.{username}`）→ 仅该用户在线会话收到信令并拉取展示。
5. 重复触发信令/拉取同 id 消息 → 不重复弹窗、不重复入列。
6. 重启后端使 SSE 断开 → 状态变「重连中」，后端恢复后自动换票重连并重新订阅。
7. 登出 → SSE 连接关闭，不再自动重连；再次登录自动重建并首拉兜底。
8. 刷新页面 → 旧连接随 beforeunload 关闭且服务端回收会话；新页面重走 getInfo→startConnection
   全链路（令牌取刷新后的最新值），首拉仅入列不弹窗，空窗期消息不丢（20 条内，更早靠分页补齐）。
