# push-mod 模块（artifactId：push）

双向长连接推送能力聚合模块，提供**低依赖的协议规范层**（`push-api`，定义通信协议、消息帧与消息源/分发/投递记录契约，不依赖任何传输 SDK）与**多传输适配层**（`websocket-impl`、`sse-impl`，基于协议契约落地 WebSocket / SSE 组合通道）。协议层默认不集成安全验证，只预留判定口（连接拦截 `IConnectionInterceptor`、订阅鉴权 `ISubscriptionAuthorizer`、上行守卫 `IUploadGuard`、授权集 `IPushTopicGranter`）且缺省全部放行，鉴权决策留给业务接入方。

## 目录

- [模块定位](#模块定位)
- [子模块说明](#子模块说明)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [协议要素](#协议要素)
- [消息发布与投递](#消息发布与投递)
- [SSE 组合通道](#sse-组合通道)
- [配置参考](#配置参考)
- [扩展指南](#扩展指南)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 协议规范层（`push-api`）+ 传输适配层（`websocket-impl` / `sse-impl`），参照 mq-mod 的 api/impl 分层 |
| 核心能力 | 帧结构/编解码/心跳/错误码/版本协商、订阅模型、消息源存储、投递协调器、分发编排、完整投递记录、内存兜底 |
| 依赖 | `push-api` 仅依赖 `common`（fastjson2 经 common 传递，零新增依赖）；impl 模块各依赖 push-api + 对应传输组件 |
| 启用方式 | `@EnableTbsPush`（抽象层）+ `@EnableTbsPushWebSocket` / `@EnableTbsPushSse`（含抽象层装配范围） |
| 连接建立 | 统一流程：HTTP POST 认证签发 sessionId+一次性 ticket → 携 `push-ticket` 建连 → 原子消费验证 → 统一 ACK；两传输完全一致 |
| 安全边界 | 协议层不内置具体鉴权，只留四个缺省放行的判定口：认证决策 `IPushAuthenticator`、建连拦截 `IConnectionInterceptor`、授权集计算 `IPushTopicGranter`、订阅鉴权 `ISubscriptionAuthorizer`（另有上行守卫 `IUploadGuard`） |

## 子模块说明

`push-mod` 为 `packaging=pom` 的聚合 POM（**不可作为依赖引入**），其下三个子模块：

| 子模块 | artifactId | 说明 |
| --- | --- | --- |
| push-api | `push-api` | 协议规范层：帧结构、编解码 SPI、心跳、错误码、版本协商、订阅/会话契约、消息源/投递记录契约、协调器 SPI、分发编排模板与全部默认装配 |
| websocket-impl | `push-websocket-impl` | WebSocket 传输适配：TextWebSocketHandler 端点、统一会话包装、条件装配 |
| sse-impl | `push-sse-impl` | SSE 组合通道适配：SseEmitter 下行 + HTTP POST 上行组合为统一双向会话 |

依赖坐标（业务模块按需引其一）：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>push-websocket-impl</artifactId>
</dependency>
```

依赖方向（02 分册铁律）：`业务模块 ──► push-websocket-impl / push-sse-impl ──► push-api ──► common`；两个 impl 模块之间零依赖，可独立引入。

## 架构设计

```
业务代码 IPushEndpoint.publish(topic, payload)
  │  消息先入消息源（削峰 + 多实例安全）
  ▼
IPushMessageStore 消息源（内存兜底 / Redis 等持久化实现经懒委托自动接管）
  │  fetchPending 取出即占用（原子语义，同一条消息只被一个实例取走）
  ▼
AbstractPushDispatcher 分发编排（模板：目标解析 → 逐会话投递 → 结果留痕 → 标记完成）
  │
  ├─ IRouteCoordinator 投递协调器（"投递给谁"的唯一决策点）
  │    ├─ TopicRouteCoordinator（默认）：topic 匹配 ITopicRegistry 订阅会话
  │    └─ 自定义协调器：按消息元数据 + 会话属性定向路由
  ▼
ISessionRegistry 会话注册表（全部在线 IConnectionSession 单点登记）
  │
  ├─ websocket-impl：TextWebSocketHandler → WebSocketTransportAdapter（establish/acceptUpstreamText/closeSession）→ WebSocketSessionWrapper
  └─ sse-impl：SseEmitter 下行 + /private/push/sse/upload HTTP POST 上行 → SseTransportAdapter → SseSessionWrapper
  │    （两适配器共用协议层 PushConnectionOrchestrator：建立编排/上行分发/清理链幂等门禁）
  │
  ▼
IDeliveryRecordStore 投递记录（每次投递动作完整留痕，投递一次即可、失败不重试）
```

调度驱动二选一互斥（`tbs.push.dispatch.scheduler`）：`embedded`（默认，内嵌守护线程）或 `job`（IBackgroundJob 桥接 xxl-job 等外部平台）。

## 统一连接建立流程

WebSocket 与 SSE 共用同一套"认证 → 建连 → ACK"流程，仅建连载体不同：

```
1. 认证  POST /private/push/auth
   请求体: {"token":"...","userId":"...","attributes":{...}}
   经 IPushAuthenticator SPI 校验（默认放行）→ 服务端预分配 sessionId
   并签发一次性 ticket（默认 60s 有效），同时由链式 IPushTopicGranter 计算订阅授权集
   → 响应 data:
   {"sessionId":"...","ticket":"...","expireAt":"...","grantedTopics":[...],"version":1}

2. 建连  携 push-ticket（query param，两传输携带方式一致）
   WebSocket: ws://host:8080/push/ws?push-ticket={ticket}
   SSE:       GET /private/push/sse/connect?push-ticket={ticket}

3. 验证  适配器原子消费凭证（取出即失效，防重放）
   失败: WS 关闭(1008) / SSE 返回 AjaxResult 错误
   成功: 绑定预分配 sessionId + 认证属性与授权集（凭证派生为会话属性）
          → 注册会话 → 下发统一 ACK 帧 → 可用

4. 通信  订阅/心跳/数据语义两传输一致；断线重连需重新 POST 认证换取新 ticket
```

认证端点为薄壳控制器：逻辑全部在协议层 `PushCredentialService`，两个 impl 各持一个互斥的 MVC 壳，端点路径取自协议层常量 `PushFrameConstants.AUTH_PATH`。

### 传输适配器全链路契约（ITransportAdapter）

`ITransportAdapter` 定位是覆盖「建立连接 → 上下行传输 → 主动/被动关闭」的全链路能力门面，每种传输一个实现；流程编排全部收拢在协议层 `PushConnectionOrchestrator`（跨传输行为唯一实现），适配器只做物理通道构造与传输语境的快速失败断言：

| 契约方法 | 语义 |
| --- | --- |
| `establish(ticket, physicalChannel)` | 消费凭证并按统一编排建立会话（注册 → 拦截器链 → 置可用 → ACK） |
| `acceptUpstream` / `acceptUpstreamText` | 上行契约唯一受理口；WebSocket 容器文本事件也默认转接本口，跨传输上行语义一致 |
| `closeSession(sessionId, reason)` | 服务端主动关闭（token 过期、心跳超时、管理踢线等），物理拆除与清理链一次调用闭环，不依赖事件回传 |
| `onChannelClosed(sessionId, reason)` | 物理通道关闭的事件回传入口，与主动关闭共享幂等门禁 |
| `stop()` | 优雅关停，按注册表全扫描逐个主动关闭 |

清理链（注销 → 订阅清理 → 断开拦截器）的幂等门禁是注册表注销的原子唯一性（`ISessionRegistry.unregister` 首次移除成功方）——主动关闭与通道事件回传双入口竞态时仅一方执行清理，杜绝订阅泄漏。

## 订阅授权集

把"能订什么"从逐条规则去堵收敛为**签发时显式授权、订阅时校验授权**，三个时点各管一段：

| 时点 | 动作 | 载体 |
| --- | --- | --- |
| 签票 | 链式 `IPushTopicGranter` 合并去重出可订阅主题模式列表 | `PushCredential.grantedTopics`（唯一真源，随 auth 响应下发供客户端按需订阅） |
| 建连 | 凭证把授权集派生为会话属性（`PushSessionAttrConstants.GRANTED_TOPICS`）连同认证属性注入统一会话 | `IConnectionSession` 属性 |
| 订阅 | `ISubscriptionAuthorizer` 按会话属性判定；不通过回 ERROR 帧（code 1008）**仅拒该次订阅不断流** | 协议层 `PushFrameProcessor` 校验点 |

语义约定：

- **签票时点快照**：长连接期间授权变更不生效，降权不追溯、升权需重新签票换票重连；
- **向前兼容**：未注册授权器（授权集为空）或未注册校验器时会话全放行，与无鉴权部署的历史行为一致；
- **列表为唯一真源**：会话属性字符串只是跨层载体，由 `PushCredential.getSessionAttributes()` 建连时派生（不参与 JSON 序列化，凭证响应只暴露 `grantedTopics` 列表），不存在双写；
- 授权集支持订阅侧通配模式（如 `global.biz.order.*`），匹配复用 `TopicMatcher`。

## 快速开始

### WebSocket 传输

```java
@SpringBootApplication
@EnableTbsPushWebSocket   // 已包含 @EnableTbsPush 装配范围
public class Application { ... }
```

```yaml
tbs:
  push:
    enable: true            # 总开关，默认 false
    transport: websocket    # 传输选择，未配置时默认 websocket
```

客户端（任意 WebSocket 客户端）：

1. 调用 `POST /private/push/auth` 提交业务凭证，取回 `sessionId` 与 `ticket`；
2. 连接 `ws://host:8080/push/ws?push-ticket={ticket}`，收到 HELLO 确认帧即连接可用；
3. 订阅主题：`{"version":1,"type":"SUBSCRIBE","topic":"order.notice",...}`，收到同名确认帧生效；
4. 心跳：周期发送 `{"type":"PING",...}`，收 `PONG` 应答；服务端侧超时未活动将关闭连接（`tbs.push.heartbeat-timeout-millis`）；
5. 接收业务数据：`{"type":"DATA","topic":"order.notice","payload":"{...}",...}`；
6. HELLO 帧为可选版本确认（重复发送无害）。

### SSE 组合通道

```java
@SpringBootApplication
@EnableTbsPushSse   // 已包含 @EnableTbsPush 装配范围
public class Application { ... }
```

```yaml
tbs:
  push:
    enable: true
    transport: sse
```

1. 调用 `POST /private/push/auth` 提交业务凭证，取回 `sessionId` 与 `ticket`；
2. `GET /private/push/sse/connect?push-ticket={ticket}`（EventSource）建立下行流，首个 `hello` 事件确认连接可用；
3. 上行统一 `POST /private/push/sse/upload`，请求体 `{"sessionId":"...","frame":"{协议帧JSON}"}`，返回 `AjaxResult`；
4. 订阅/心跳/数据语义与 WebSocket 完全一致（同一套协议帧）。

### 业务发布消息

```java
@Resource
private IPushEndpoint pushEndpoint;

public void notifyOrder(String orderNo) {
    // 消息入源 → 协调器按 topic 解析订阅会话 → 投递 → 投递记录留痕
    pushEndpoint.publish("order.notice", Map.of("orderNo", orderNo));
}
```

## 协议要素

### 帧结构（PushFrame）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| version | int | 协议版本（当前 1），握手阶段协商 |
| type | FrameTypeEnum | HELLO / PING / PONG / DATA / SUBSCRIBE / UNSUBSCRIBE / ERROR |
| messageId | String | 帧唯一标识，DATA 帧与投递记录的关联键 |
| topic | String | 业务主题，DATA/SUBSCRIBE/UNSUBSCRIBE 必填（非空且 ≤128 字符） |
| seq | Long | 发送方序列号，可选，用于乱序检测 |
| timestamp | long | 帧产生时间戳 |
| payload | String | 载荷文本（DATA 为业务数据 JSON；HELLO 确认为 sessionId+version；ERROR 为错误描述） |

### 错误码（PushErrorCodeEnum，ERROR 帧 + 全局关闭判定）

| 错误码 | 枚举 | 语义 | 是否关闭连接 |
| --- | --- | --- | --- |
| 1001 | PROTOCOL_VERSION_MISMATCH | 版本协商失败 | 是 |
| 1002 | FRAME_MALFORMED | 帧解析失败/必填字段缺失 | 是 |
| 1003 | TOPIC_INVALID | 主题不合法 | 否 |
| 1004 | FRAME_TYPE_UNSUPPORTED | 不支持的帧类型 | 否 |
| 1005 | SESSION_NOT_FOUND | 会话不存在（SSE 上行失效） | HTTP 错误响应 |
| 1006 | NOT_CONNECTED | 未完成握手即发 DATA | 否 |
| 1007 | INTERNAL_ERROR | 服务端内部错误 | 否 |
| 1008 | SUBSCRIBE_DENIED | 订阅主题不在签票授权集内（越权订阅） | 否 |
| 1009 | UPLOAD_DENIED | 上行帧未通过协议层守卫链（IUploadGuard，如会话归属校验） | 否 |

### 协议层职责边界

**属于协议层**：帧结构、序列化格式（JSON 默认 + `IPushCodec` SPI）、心跳（PING/PONG 契约 + 服务端空闲超时统一判定）、错误码、版本协商、订阅/退订语义、会话状态机、消息源/协调器/投递记录契约、分发编排、内存兜底。

**不属于协议层**：传输建立细节（WS 握手/SSE EventSource）、安全验证的具体规则（协议层只留判定口与缺省放行，身份/角色/权限组语义全部在业务实现）、持久化实现、业务消息体结构、重试/死信/断线补发（投递一次即可，失败如实留痕）、前端客户端重连。

## 消息发布与投递

- **发布即入源**：`publish` 先将消息写入消息源，再触发一轮即时分发；未及时取出的消息由调度驱动兜底补发一轮；
- **投递一次即可**：`fetchPending` 取出即占用（原子），同一条消息不会被重复分发；发送失败仅记 `FAILED` 投递记录，不做重试；
- **完整留痕**：每次投递动作（含无目标场景 `NO_SUBSCRIBER`）均写一条 `DeliveryRecord`（messageId/topic/sessionId/transport/result/deliverTime/errorMessage）；
- **死连接防护**：会话关闭时由注册表注销并清理全部 topic 订阅；心跳空闲超时（`HeartbeatMonitor`）统一判定，经 `ITransportAdapter.closeSession` 走完整主动关闭链（物理拆除 + 注销 + 订阅清理 + 断开拦截器，不依赖半开连接不可靠的事件回传）。

## SSE 组合通道

| 端点 | 方法 | 说明 |
| --- | --- | --- |
| `/private/push/sse/connect` | GET | 建立 SSE 下行流（`@NoWrapped` 直接返回 SseEmitter），首个 `hello` 事件下发 sessionId |
| `/private/push/sse/upload` | POST | 上行协议帧，请求体 `{"sessionId","frame"}`，返回 `AjaxResult`；会话失效快速失败 |

- 端点位于 `/private/**`，由项目认证体系统一保护，控制器自身不集成鉴权；
- SSE 客户端需周期 POST `{"type":"PING"}` 帧保活，否则被心跳监控按空闲超时关闭；
- 客户端断线重连视为全新会话（新 sessionId、重新握手订阅）。

## 配置参考

### `tbs.push.*`（PushProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable` | boolean | `false` | 总开关，push 相关装配全部生效的前提 |
| `transport` | String | `websocket` | 传输选择：`websocket` / `sse`，互斥装配 |
| `heartbeat-timeout-millis` | long | `90000` | 会话空闲超时（毫秒），非正数关闭心跳监控 |
| `grant-public-topics` | List | `[global.broadcast]` | 授权集的配置层来源：内置授权器无条件授予全部认证会话的公开主题模式（支持订阅侧通配） |
| `memory-message-capacity` | int | `10000` | 内存消息源容量上限，满时淘汰最早消息 |
| `memory-record-capacity` | int | `10000` | 内存投递记录容量上限，满时淘汰最早记录 |

### `tbs.push.dispatch.*`（PushDispatchProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `scheduler` | String | `embedded` | 调度驱动二选一：`embedded`（内嵌守护线程）或 `job`（IBackgroundJob 桥接外部平台） |
| `interval-millis` | long | `5000` | 内嵌调度间隔（毫秒），仅 `scheduler=embedded` 生效 |
| `batch-size` | int | `50` | 分发器单轮取出条数 |

### `tbs.push.websocket.*`（WebSocketPushProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `path` | String | `/push/ws` | WebSocket 端点路径 |
| `allowed-origins` | String | `*` | 允许的跨域来源，逗号分隔 |

### `tbs.push.auth.*`（PushAuthProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `ticket-ttl-millis` | long | `60000` | 一次性连接凭证有效期（毫秒），过期消费失败需重新认证 |
| `credential-capacity` | int | `10000` | 内存凭证存储容量上限，满时先清理过期凭证，仍满则拒绝签发 |

## 扩展指南

### 自定义投递协调器（定向投递）

```java
@Component
public class UserRouteCoordinator implements IRouteCoordinator {
    @Resource
    private ISessionRegistry sessionRegistry;

    @Override
    public List<IConnectionSession> resolveTargets(PushMessage message) {
        // 按 x-target-attribute 元数据匹配会话属性，实现"按用户ID定向投递"
        String userId = message.getMetadata().get(PushFrameConstants.METADATA_KEY_TARGET_ATTRIBUTE);
        return sessionRegistry.all().stream()
            .filter(s -> userId.equals(s.getAttribute("userId")))
            .collect(Collectors.toList());
    }
}
```

注册即覆盖默认 `TopicRouteCoordinator`（`@ConditionalOnMissingBean`）。会话属性在连接拦截器或上行处理时写入。

### 接入真实鉴权（连接认证器）

认证端点 `POST /private/push/auth` 的校验决策点为 `IPushAuthenticator` SPI，默认放行；
业务方注册实现 Bean 即接入真实鉴权（校验登录令牌、注入用户属性供协调器路由）：

```java
@Component
public class TokenPushAuthenticator implements IPushAuthenticator {
    @Override
    public PushAuthResult authenticate(PushAuthForm form) {
        if (!tokenService.verify(form.getToken().getValue())) {
            return PushAuthResult.failure("token.invalid");
        }
        Map<String, String> attributes = Map.of(
            NoopAuthenticator.ATTR_USER_ID, form.getUserId());
        return PushAuthResult.success(attributes);
    }
}
```

认证失败返回统一错误响应（i18n key `push.auth.failed`），凭证签发被拒绝。
连接建立后的会话期拦截（如限流、来源审计）仍可用 `IConnectionInterceptor` 扩展点。

### 扩展授权来源（主题授权器）与订阅校验

授权集的三个来源分层：硬编码层（本人专属频道，服务端推导）+ 配置层（`tbs.push.grant-public-topics`）由业务内置实现，
其余来源（角色/权限组映射、查库、外部授权系统）注册额外 `IPushTopicGranter` Bean 即可链式叠加，协议层去重合并：

```java
@Component
public class RoleTopicGranter implements IPushTopicGranter {
    @Override
    public List<String> grantTopics(PushAuthResult authResult) {
        // 从认证结果取服务端绑定身份，按角色/权限组映射出主题模式
        return permissionService.resolveTopicPatterns(authResult.getAttributes().get("userId"));
    }
}
```

订阅时的判定规则由 `ISubscriptionAuthorizer` SPI 承担（单一实现，`@ConditionalOnMissingBean` 可覆盖）：
入参为 `session + topic`，返回 `false` 即回发 ERROR 帧（code 1008）拒该次订阅且不断流；
未注册实现时全放行。注意授权语义属于业务层：协议层只定义“可否订阅”的判定口，不感知用户与角色。

### 持久化消息源 / 投递记录（如 Redis）

继承 `AbstractPushMessageStore` / `AbstractDeliveryRecordStore` 实现 do 钩子并注册为 Bean，
即被 `DelegatingPushMessageStore` / `DelegatingDeliveryRecordStore` 运行期自动接管，
无需改动抽象层代码；`fetchPending` 必须保证"取出即占用"的原子语义。

### 自定义编解码格式

实现 `IPushCodec`（帧↔文本、载荷序列化）并注册为 Bean，覆盖默认 `JsonPushCodec`。

## 注意事项

1. **聚合模块不可依赖**：依赖请引 `push-api` / `push-websocket-impl` / `push-sse-impl`，而非 `push`（packaging=pom）。
2. **`transport` 互斥**：同一时刻仅一个传输适配器与对应认证薄壳生效；两个 impl 模块可同时引入依赖，但需经 `tbs.push.transport` 显式二选一。
3. **未开总开关装配不生效**：`tbs.push.enable=false`（默认）时全部 push 装配跳过。
4. **先认证后建连**：未携有效 `push-ticket` 建连将被拒绝（WS 关闭 1008 / SSE 错误响应）；ticket 一次性消费，重连必须重新 POST 认证。
5. **`fetchPending` 必须原子占用**：自定义持久化消息源不保证"取出即锁定"将导致多实例重复投递；凭证存储 `consume` 同理需"取出即失效"防重放。
6. **内存兜底不跨重启**：进程重启后内存消息源、投递记录与未消费凭证清空，需持久化请注册覆盖实现。
7. **心跳双端约定**：WebSocket 客户端发 PING、SSE 客户端 POST PING 帧；服务端仅做空闲超时判定。
8. **SSE 会话标识必须回传**：上行端点按 sessionId 定位会话，客户端需妥善保存认证响应与 ACK 帧下发的 sessionId。
9. **上行 DATA 需要监听器**：未注册 `IUpstreamListener` 时客户端上行的 DATA 帧仅记 debug 日志后忽略。
10. **投递失败不重试**：发送失败仅记 `FAILED` 记录，业务需要可靠送达时应在业务侧基于投递记录自行补偿。
11. **授权集是签票快照**：长连接期间的提权/降权不生效（升权需重新签票换票重连）；客户端侧 `grantedTopics` 仅供展示可订清单，真正的拦截在服务端订阅校验点，不可据其信任客户端。

## 包结构

```
com.tbs.push                                  （push-api：协议规范层）
├── annotation/              @EnableTbsPush（启用入口，@Import(PushPackAutoRegister)）
├── api/
│   ├── auth/                IPushAuthenticator / IPushTopicGranter（授权集SPI）/ IPushCredentialStore / AbstractPushCredentialStore
│   ├── frame/               IPushCodec（编解码 SPI）
│   ├── session/             IConnectionSession / ISessionRegistry / ITopicRegistry / IUploadGuard（上行守卫SPI）/ ISubscriptionAuthorizer（订阅鉴权SPI）
│   ├── transport/           ITransportAdapter（全链路能力契约）/ IPushEndpoint / IUpstreamListener
│   ├── dispatch/            IPushDispatcher / AbstractPushDispatcher（分发编排模板）
│   ├── store/               IPushMessageStore / AbstractPushMessageStore（消息源契约）
│   ├── coordinator/         IRouteCoordinator（投递协调器 SPI）
│   ├── record/              IDeliveryRecordStore / AbstractDeliveryRecordStore（投递记录契约）
│   ├── intercept/           IConnectionInterceptor（连接拦截扩展点，默认无校验）
│   └── exception/           PushException + PushProtocolException / PushTransportException
├── config/                  PushPackAutoRegister / PushSupportConfig / 懒委托存储 /
│   └── property/            PushProperty（tbs.push）、PushDispatchProperty（tbs.push.dispatch）
├── constants/               PushFrameConstants（版本/传输/关闭原因）、PushSessionAttrConstants（会话属性键）、PushI18nConstant（i18n key）
├── model/                   PushFrame / PushMessage / DeliveryRecord / PushAuthForm / PushAuthResult /
│                            PushCredential / FrameTypeEnum / ConnectionStateEnum / PushErrorCodeEnum /
│                            DeliveryResultEnum / TransportCapability
└── impl/                    JsonPushCodec、InMemoryPushMessageStore、InMemoryDeliveryRecordStore、
                             InMemoryCredentialStore、NoopAuthenticator、PushCredentialService、
                             DefaultSessionRegistry、DefaultTopicRegistry、TopicRouteCoordinator、
                             PushFrameProcessor、PushConnectionOrchestrator（连接生命周期编排）、
                             DefaultPushDispatcher、DefaultPushEndpoint、
                             EmbeddedPushScheduler、PushDispatchBackgroundJob、HeartbeatMonitor、
                             ConnectionInterceptorChain、NoopConnectionInterceptor

com.tbs.push.websocket        （websocket-impl：WebSocket 传输适配）
├── annotation/              @EnableTbsPushWebSocket（含抽象层装配范围）
├── adapter/                 WebSocketTransportAdapter（全链路契约实现：establish/acceptUpstream/closeSession）
├── controller/              WsPushAuthController（认证薄壳，transport=websocket 互斥激活）
├── handler/                 PushWebSocketHandler（容器事件薄壳，物理动作转接适配器契约口，协议逻辑全部委托协议层）
├── session/                 WebSocketSessionWrapper（统一会话包装，预分配 sessionId）
└── config/                  WebSocketPushAutoRegister / WebSocketPushConfig / property/WebSocketPushProperty

com.tbs.push.sse              （sse-impl：SSE 组合通道适配）
├── annotation/              @EnableTbsPushSse（含抽象层装配范围）
├── adapter/                 SseTransportAdapter（全链路契约实现：connect便捷入口/establish/acceptUpstream/emitter回调转接onChannelClosed）
├── session/                 SseSessionWrapper（统一会话包装，预分配 sessionId）
├── controller/              SsePushAuthController（认证薄壳）与 SsePushController（connect/upload 端点）、SseUploadForm
└── config/                  SsePushAutoRegister / SsePushConfig

resources/i18n/
└── messages-push.properties / messages-push_en_US.properties   （中英文 i18n 资源）
```
