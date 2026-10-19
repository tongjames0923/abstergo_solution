# MQ v2 记录驱动消费模型 · 设计说明

> 对应代码：`mq-mod/mq-api` 下 `com.tbs.mq.v2` 包，分支 `springboot3-mq-rework`。
> 本文按"动机 → 理念 → 分层 → 组件契约 → 运行链路 → 决策取舍 → 实现状态与待闭环"组织，
> 与当前代码实现对齐；§7 记录本轮已修复的契约层问题与仍需跨模块落地的功能项。

## 1. 设计动机

v1（Registry / Coordinator / Adapter 抽象）演进过程中暴露两个根本不满：

1. **可靠性寄生在厂商能力上**：死信、重投、重试次数（x-death）全部依赖厂商队列特性，
   换厂商等于换一套可靠性语义，且厂商侧记录不可查、不可补、不可人工介入。
2. **多消费者共享一条消息的成败语义**：一条消息多个消费者时，任一失败只能整条重投，
   已成功的消费者被迫重跑；"针对多个 consumer 分别有记录、根据记录重试"无从谈起。

**目标**：厂商通道退化为纯传输层；可靠性、重试、死信、排查全部基于自建落库记录；
每个消费者的消费进度独立可查、可单独重试。

## 2. 核心理念

- **不信任厂商，信任记录**：消息只有建档成功才允许向厂商确认投递；此后一切恢复
  （重试、回灌、人工补消费）只从记录出发，不再回到厂商队列。
- **全链路唯一依赖厂商能力的位置**：建档失败时的 nak 兜底（此时消息尚未被体系接纳，
  只有厂商能救它）。其余任何环节失败都不回退到厂商重投。
- **投递可靠性与消费可靠性分层承载**：消息主记录管"消息有没有进体系"，
  消费台账管"每个消费者有没有消费成功"。两层各司其职，互不覆盖。

## 3. 分层与泛型模型

```
IMqContext<R, T extends Serializable>
  R：厂商原始消息类型（传输层，如 rabbit Message）—— 仅留档，供排查与重放
  T：业务负载类型（消费者面对的类型）—— 由接收段 getPayload(R) 转换
```

- 传输层（R）与业务层（T）在**接收段一次性切开**，消费者只接触 T、不感知厂商类型；
  消费者按负载类型（`getMessagePayloadType()`）注册到 `IConsumerRegistry`，分发时回查路由。
- 一个上下文实例 = 一个队列 = 一个消费环境；多队列/扩容靠注册多个上下文实例，
  不做跨厂商统一抽象（厂商确认语义由各厂商子类实现 `ackMessage`/`nakMessage` 承担）。
- **上下文是装配门面**：`AbstractMqContext` 只保留"待处理队列 + 处理线程 + 生命周期"，
  把接收段下沉到 `IMqMessageReceiver`、把分发段下沉到 `IMqMessageProcessor`，
  状态推进与台账落库统一走 `IMqRecorder`，单飞保护走 `IMqMessageMonopolizer`。
- **上下文作用域贯穿记录层**：接收/记录/抢占的每个方法都以 `IMqContext` 为首参（身份见 `getContextId()`，记录器另有 `getRecorderId()`），
  使同一批协作 Bean（记录器/抢占器等）可服务多个队列并按上下文隔离归属；业务消费者是唯一不接触上下文的一端。

## 4. 包结构与组件契约

```
com.tbs.mq.v2
├── config/       （空 · 规划：厂商子类 Bean 装配 / tbs.mq.v2.enable 开关 / 自动配置）
├── constants/    （空 · 规划：主记录·台账表名、默认重试上限、consumeId 分配等常量）
├── consume/      IConsumerRegistry（按负载类型的消费者路由表）· IMqConsumer（业务消费者）
├── core/         IMqContext / AbstractMqContext            —— 门面 + 处理线程生命周期
│                 IMqMessageReceiver / AbstractMqMessageReceiver —— 接收段（组装·建档·配对·确认）
│                 IMqMessageProcessor / AbstractMqMessageProcessor —— 分发段（广播消费·构建台账）
│                 IMqRecorder                              —— 可靠性真源（主记录 + 消费台账，仅存取）
│                 IMqMessageMonopolizer                    —— 消息级单飞抢占
├── enums/        MqMessageStatusEnum（主记录状态机）· MqMessageConsumeStatusEnum（台账成败）
├── exception/    MqConsumeException（消费失败信号，携带 consumeId/businessId/retryFlag）
└── vo/           MqMessageVO<R,T>（主记录）· MqConsumeVO<T>（台账行）· MqRecordVO<T>（建档逐条结果）
```

### 4.1 双层记录模型（当前由单一 `IMqRecorder` 承载）

| 层 | 承载方法 | 身份键 | 状态 | 职责 |
|----|----------|--------|------|------|
| 消息主记录 | `record` / `updateFlag` / `errorFlag` / `get` | messageId | RECEIVED → PROCESSING → PROCESSED/FAILED | 投递可靠性真源：建档、状态推进、终态留痕 |
| 消费台账 | `recordConsume` | messageId + consumeId | SUCCESS / FAIL（+ 起止时间 + 异常） | 消费可靠性依据：逐消费者事后结果，重试扫描的回灌目标来源 |

> 早期设计把台账拆为独立的 `IMqConsumeRecorder`；当前实现**合并进 `IMqRecorder`**，
> 用 `beginTransaction(context)/endTransaction(context, id)` 把"状态推进 + 台账落库 + 终态留痕"纳入同一记录层事务。
> 除按全局唯一 `messageId` 回查的 `get(messageId)` 外，记录层方法均以 `context` 为首参（队列级作用域）。

语义要点：

- **主记录 PROCESSED = 分发完成，不等于全员消费成功**；单消费者失败只留 FAIL 台账，
  不中断其他消费者、不改变主记录终态。
- 台账是**事后记录**（分发完成后一次性批量落库），不是事前任务表——见 §6 取舍。
- 主记录状态机由消息级单飞（`IMqMessageMonopolizer`）保护：同一 messageId
  同一时刻仅一个处理者持有并分发；抢占失败方不得释放他人占有。

## 5. 三段运行链路

### 5.1 接收（厂商线程 → `AbstractMqContext.messageReceived` → `AbstractMqMessageReceiver.receive`）

```
逐条组装VO：genMessageId(框架发号，子类可覆盖) + resolveBusinessId(子类) + getPayload(子类) → flag=RECEIVED
  → validateMessageVo（VO/businessId/rawMessage 缺一不可，未覆盖 resolveBusinessId 即整批拒收）
  → 批量 recorder.record(context, …)（逐条结果按 messageId 配对，缺席按失败处理，宁拒勿漏）
  → 建档成功：进内存待处理队列 + onInqueueSuccess(this::ackMessage) 结清厂商投递
  → 建档失败/缺席：onInqueueFail(this::nakMessage) 拒收（唯一依赖厂商能力的位置；持续不可用即背压重投）
每条消息至多确认一次（防 delivery_unknown），批次内 null 元素忽略
```

ack/nak 以 `SConsumer<R>` 回调传入接收器，使**接收段与厂商确认语义解耦**（接收器不持有厂商能力）。
未建档的消息**绝不进入处理流程**——否则重试、留痕、恢复全部不可达。

### 5.2 处理（独立线程，`AbstractMqContext` 的 `afterPropertiesSet` 主循环 `loop()`）

```
poll(90s) → monopolizer.monopolize(context, message) → [失败即 continue，不进事务]
  recorder.beginTransaction(context)
  → recorder.updateFlag(context, messageId, PROCESSING)
  → processor.process(context, message)：按负载类型取消费者列表
      · 无消费者 → 抛可重试 MqConsumeException（AssertUtil 包装）→ 走异常路径
      · 有消费者 → 逐个 consume(payload) 各自计时、捕获异常，构建台账行（SUCCESS / FAIL+exception）
  → recorder.recordConsume(context, 台账行批量落库)
  → recorder.updateFlag(context, messageId, PROCESSED)
catch：recorder.errorFlag(context, messageId, e) → 主记录 FAILED 留痕
finally：仅持有者 monopolizer.release(context, message)；recorder.endTransaction(context, transactionId)
```

此处 `context` 即门面自身 `this`，作为记录/抢占层的队列级作用域传入。

处理段整体包裹在记录层事务内：**状态推进、台账落库、失败留痕原子提交**（提交/回滚由实现方在
`endTransaction` 内裁决）；单条消息的分发异常不中断主循环，取下一条继续。

### 5.3 重试（规划中的外部扫描组件，尚未落地）

- 输入：主记录 FAILED（分发级失败，整体重放）+ 台账 FAIL 行（消费者级失败，**只重跑该消费者**）；
- 依据：台账异常中的 `MqConsumeException.retryFlag`（未声明默认按可重试）与重试次数上限；
- 回灌前必须重新抢占消息（`monopolize`），保证与在线处理者不并发；
- 回灌载体：`IMqRecorder.get(messageId)` 回查主记录取回 payload，单消费者执行后补台账；
- 全程不碰厂商队列；死信 = 超过重试上限的终态记录，留痕不删除。

### 5.4 主记录状态机

```
RECEIVED ──(接收建档成功)──▶ 待处理队列 ──(处理线程 poll + monopolize)──▶ PROCESSING
PROCESSING ──(分发完成)──▶ PROCESSED        PROCESSING/分发异常 ──(errorFlag)──▶ FAILED
```

> 主记录仅保留 `RECEIVED(0)`/`PROCESSING(1)`/`PROCESSED(2)`/`FAILED(3)` 四个有效状态（未使用的中间态已删除）。

## 6. 关键设计决策与取舍

1. **台账式（事后记录）vs 任务式（事前落行）**：选择台账式。
   任务式每条消息 × 每个消费者事前插行，写入量大、链路重；台账式一次分发只写一批结果行，
   主循环保持消息级简单结构。代价：进程在分发中途崩溃时台账未写，恢复靠主记录
   PROCESSING 滞留扫描 + 整体重放 → **消费者必须幂等**（已在契约注释中强制）。
2. **失败语义不互相覆盖**：投递层归主记录、消费层归台账，单消费者失败不 errorFlag 主记录。
3. **上下文作用域分层**：接收/记录/抢占层显式以 `context` 为首参做队列级隔离；而 `IMqConsumer.consume(T)` 只收负载、不给 context，
   杜绝消费者触碰 ack/nak 破坏"消费环节无厂商确认"原则；上下文注入止于传输/记录层，不下探到业务消费者。
   重试语义通过抛 `MqConsumeException(retryFlag)` 声明，由台账承载。
4. **无消费者拒收 / FAILED**：没有订阅者的消息不视为成功。分发时发现无注册消费者抛可重试
   异常留 FAILED 记录，消费者注册后由回灌补救，不依赖厂商重投风暴等待。
5. **门面 + 三段协作**：接收/分发/记录/抢占各自独立契约，上下文只做编排与线程看护，
   便于按厂商、按存储分别扩展实现，也便于横向扩容（见 §7 并行化）。
6. **ack/nak 回调注入**：接收段通过 `SConsumer` 拿到确认动作，本身不依赖厂商类型，职责更纯。
7. **存储与调度职责隔离**：`IMqRecorder` 只做存取，重试扫描、回灌调度是独立组件
   （对齐既有重投递架构决策），杜绝记录层内嵌定时逻辑。

## 7. 实现状态与待闭环清单

### 7.1 本轮已修复（契约层代码质量与一致性，均在 `com.tbs.mq.v2` 包内）

| 原问题 | 处置 |
|--------|------|
| `AbstractMqContext` 的 `recorder`/`processor` 未注入（必 NPE） | 补 `@Resource`，与 `receiver`/`mqMessageMonopolizer` 统一装配 |
| `getQueueSize()` 抽象但队列私有、子类无法实现 | 基类直接实现为 `messageQueue.size()` |
| `IMqRecorder`/`AbstractMqContext` 残留 `IMqConsumeRecorder`/`handleMessage`/`MqMessageProcessor` 过时引用 | javadoc 全量重写，改为 `recordConsume` / `IMqMessageProcessor#process` |
| `consumeId` 在异常为 `String`、台账为 `Integer` | `MqConsumeException.consumeId` 统一为 `Integer`，并修正类注释中的错误用法示例 |
| `MqMessageStatusEnum` 的 `IN_MEMORY`/`PERSISTED` 死状态 | 删除并重编号 `0~3` |
| `validateMessageVo`、处理器无消费者提示硬编码文案（铁律 3） | 改为 `AssertUtil...i18n(...)` / `I18nUtil.get(...)`，新增 `mq.v2.*` 资源 key（中英） |
| `MqMessageVO` 缺 `serialVersionUID`、多个接口 javadoc 偏薄 | 补齐 `serialVersionUID` 与全量类/方法注释 |
| 记录/接收/抢占层改为上下文作用域（`context` 首参）并新增 `getContextId()`/`getRecorderId()` | javadoc 全量对齐：修 `IMqRecorder` 事务方法的过时 `@link` 签名、为各方法补 `@param context`、补身份 getter 与作用域语义
| RabbitMQ 厂商传输层落地 | 新增 `com.tbs.rabbitmq.v2`，按“公共基类 + 两种接入基类”拆分。`AbstractRabbitMqContext`（厂商无关公共基类，R 固定为 Spring AMQP `Message`）：`ThreadLocal` 承载通道、`ingest(List,Channel)` 接收入口（接收与确认同线程）、`ackMessage`/`nakMessage` 走 `basicAck(tag,false)`/`basicNack(tag,false,false)`（不 requeue，失败恢复只走记录层）。业务二选一接入：① `RabbitMqContext`（自建 `SimpleMessageListenerContainer`）支持 Spring AMQP 原生批量——实现 `ChannelAwareBatchMessageListener`，`MqConsumerProperties` 加 `consumerBatchEnabled`/`batchSize`（默认关闭），开启后容器同线程收集至多 N 条回调 `onMessageBatch` 批量建档、仍逐条确认；② `RabbitMqListenerContext`（`@RabbitListener` 注解接入，不自建容器）——子类声明注解方法并在方法体调用 `handleMessage`/`handleMessageBatch` 灌入 receive，所用容器工厂须 `AcknowledgeMode.MANUAL`。两者均复用 `RabbitMqMessageReceiver`（`getPayload` 反序列化、`genMessageId` 复用厂商 messageId、`resolveBusinessId` 读头）与 `RabbitMqMessageProcessor`（绑定 `<Message,T>` 注入 `IConsumerRegistry`），业务叶子子类绑定负载类型并声明为 Bean 即用 |
| 内存基本实现（协作层） | 新增 `com.tbs.mq.v2.impl`：`InMemoryMqRecorder`（主记录按 `contextId` 作用域隔离、`record` 以 `putIfAbsent` 幂等去重、`updateFlag`/`errorFlag` 推进与留痕、`recordConsume` 追加台账，事务方法沿用默认 no-op）、`InMemoryMqMessageMonopolizer`（以 `contextId#messageId` 为占有键、并发 Set 原子 `add` 实现单飞互斥、`release` 交还）、`InMemoryConsumerRegistry`（以负载 Class 为路由键，`CopyOnWriteArrayList` 承载多消费者，未注册回空集合）。三者均为纯 JDK 并发、无厂商/存储依赖的开箱即用实现（单机/开发/自检），进程重启即清空；集群与持久化场景需替换为 DB/Redis 实现（见 §7.2）。自检 `InMemoryImplTest` 覆盖建档/去重/作用域隔离/单飞互斥/路由增删 |

### 7.2 剩余 · 功能落地（跨模块，需表结构与产品决策，非本包可独立修复）

| # | 类型 | 事项 |
|---|------|------|
| 1 | 落地 | 存储/协作实现与装配：`IMqRecorder`（DB 主记录+台账）、`IMqMessageMonopolizer`（分布式单飞，如 Redis）、`IConsumerRegistry`（消费者路由表）及 `tbs.mq.v2.enable` 开关与自动配置（填 `config/`）。内存基本实现已在 `com.tbs.mq.v2.impl` 落地（可作单机/开发默认 Bean），生产需另提供 DB/Redis 实现并在 config 装配；RabbitMQ 传输层适配器已在 `com.tbs.rabbitmq.v2` 落地 |
| 2 | 落地 | 主记录/台账两张表的实体与建表（表名、索引、`consumeId` 分配可入 `constants/`） |
| 3 | 落地 | 重试扫描 + 回灌入口：独立组件消费台账 FAIL 行与主记录 FAILED，`IMqRecorder.get` 回查后按 messageId(+consumeId) 重放；重试上限与死信终态在此层裁决 |
| 4 | 正确性 | `retryFlag` 落地：处理器把异常存入台账 `exception`，回灌需从异常读取 `retryFlag`/重试次数，与 #5 落库序列化一并定 |
| 5 | 正确性 | 台账/建档 `exception` 字段为 `Exception` 对象，DB 落库需转字符串（message + stack 摘要）或契约层改为 `String` |
| 6 | 扩容 | 无界队列 + 单处理线程：改有界队列需配套回灌保证（队满丢弃依赖 #3 扫描回灌，否则已 ack 消息会滞留），建议与 #3 一并设计

## 8. 契约速查

| 类型 | 角色 |
|------|------|
| `IMqContext<R,T>` / `AbstractMqContext<R,T>` | 消费上下文门面（队列级环境 + 处理线程）+ 作用域身份 `getContextId()`，厂商子类实现确认语义 |
| `IMqMessageReceiver<R,T>` / `Abstract...` | 接收段 `receive(context,…)`：组装·校验·批量建档·按 messageId 配对·ack/nak 回调 |
| `IMqMessageProcessor<R,T>` / `Abstract...` | 分发段 `process(context,…)`：按负载类型广播消费，构建 SUCCESS/FAIL 台账行 |
| `IMqRecorder<R,T>` | 可靠性真源（方法以 `context` 为首参，`get` 除外）：主记录建档/状态推进/失败留痕/回查 + 消费台账落库 + 记录层事务 + `getRecorderId()` |
| `IMqMessageMonopolizer` | 消息级单飞抢占（`monopolize` / `release`，均带 `context` 作用域） |
| `IMqConsumer<T>` | 业务消费者（面向负载，必须幂等，`consumeId`/`businessId` 为台账身份键） |
| `IConsumerRegistry` | 按负载类型的消费者路由表（运行期可注册/注销） |
| `MqMessageVO<R,T>` / `MqConsumeVO<T>` / `MqRecordVO<T>` | 主记录 / 台账行 / 建档逐条结果 |
| `MqMessageStatusEnum` / `MqMessageConsumeStatusEnum` | 主记录状态机 / 台账成败状态 |
| `MqConsumeException` | 消费失败信号，携带 `consumeId`/`businessId`/`retryFlag`，提供 `retryable`/`discard`/`*I18n` 工厂 |
