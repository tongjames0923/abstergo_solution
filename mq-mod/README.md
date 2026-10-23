# mq-mod 模块（artifactId：mq）

消息中间件能力聚合模块，提供**厂商无关的 MQ v2 契约层**（`mq-api`）：记录驱动消费模型（Record-Driven Consumption）的全部接口、VO、状态枚举、异常信号、负载转换契约，以及开箱即用的**纯内存基本实现**。各具体 MQ 厂商实现模块（如 `rabbitmq-mod`）基于本模块契约落地发送/消费能力；本模块自身不依赖任何 MQ 厂商 SDK。

> v2 完整设计说明（动机 / 分层 / 链路 / 取舍 / 待闭环清单）见 [docs/mq-v2-design-outline.md](../docs/mq-v2-design-outline.md)，本 README 是面向使用者的契约速查与接入指南。

## 目录

- [模块定位](#模块定位)
- [核心理念](#核心理念)
- [子模块说明](#子模块说明)
- [架构设计](#架构设计)
- [契约速查](#契约速查)
- [双层记录模型](#双层记录模型)
- [消息发送（IMqMessageProducer）](#消息发送imqmessageproducer)
- [负载转换（IMqMessagePayloadConverter）](#负载转换imqmessagepayloadconverter)
- [内存基本实现（com.tbs.mq.v2.impl）](#内存基本实现comtbsmqv2impl)
- [快速开始](#快速开始)
- [消息头约定（MqHeaderConstants）](#消息头约定mqheaderconstants)
- [扩展指南](#扩展指南)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 契约层（`mq-api`），与厂商实现模块解耦 |
| 核心能力 | v2 记录驱动消费模型：收发契约、双层记录（主记录 + 消费台账）、消息级单飞、消费者路由、负载格式转换 |
| 依赖 | 仅 `common`（无任何 MQ 厂商 SDK；测试依赖 junit-jupiter） |
| 启用方式 | 契约层**无启用注解**、无 `spring.factories`；协作层 Bean 由使用方（厂商模块或业务 config）装配，见[快速开始](#快速开始) |

## 核心理念

- **不信任厂商，信任记录**：消息只有建档成功才允许向厂商确认投递；此后一切恢复（重试、回灌、人工补消费）只从记录出发，不再回到厂商队列。
- **唯一依赖厂商能力的位置**：建档失败时的 nak 兜底（消息尚未被体系接纳，只有厂商能救它）；其余环节失败一律不回退到厂商重投。
- **投递可靠性与消费可靠性分层承载**：消息主记录管"消息有没有进体系"，消费台账管"每个消费者有没有消费成功"，两层各司其职、互不覆盖。
- **厂商通道退化为纯传输层**：死信、重投、排查全部基于自建落库记录，每个消费者的消费进度独立可查、可单独重试。

## 子模块说明

`mq-mod` 为 `packaging=pom` 的聚合 POM（**不可作为依赖引入**），其下仅一个子模块：

| 子模块 | artifactId | 说明 |
| --- | --- | --- |
| mq-api | `mq-api` | v2 契约层：收发/记录/抢占/路由/转换全部接口 + 内存基本实现 |

依赖坐标：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>mq-api</artifactId>
</dependency>
```

## 架构设计

消费链路按**三段协作**拆分，`AbstractMqContext` 只是装配门面（待处理队列 + 处理线程 + 生命周期）：

```
厂商容器线程（如 RabbitMQ Listener）
  │  AbstractMqContext.messageReceived(List<R>)
  ▼
接收段 IMqMessageReceiver.receive(context, …)
  │  逐条组装 VO（genMessageId + resolveBusinessId + getPayload）→ 校验
  │  → recorder.record 批量建档（逐条按 messageId 配对，缺席按失败处理）
  │  ├─ 建档成功 → 进内存待处理队列 → onInqueueSuccess(ack) 结清厂商投递
  │  └─ 建档失败 → onInqueueFail(nak) 拒收（唯一依赖厂商能力的位置）
  ▼
处理段 AbstractMqContext 主循环（独立线程 loop）
  │  poll → monopolizer.monopolize（消息级单飞，失败即跳过）
  │  → recorder.beginTransaction → updateFlag(PROCESSING)
  │  → 分发段 IMqMessageProcessor.process：按负载类型查 IConsumerRegistry
  │      · 无消费者 → 抛可重试 MqConsumeException → 主记录 FAILED
  │      · 有消费者 → 逐个 IMqConsumer.consume 各自计时捕获，构建 SUCCESS/FAIL 台账行
  │  → recorder.recordConsume（台账批量落库）→ updateFlag(PROCESSED)
  │  catch：recorder.errorFlag → FAILED 留痕；finally：release + endTransaction
  ▼
重试段（规划中的外部扫描组件，尚未落地）
     FAILED 主记录整体重放 / FAIL 台账行按 consumeId 单消费者回灌
     全程不碰厂商队列；死信 = 超上限终态记录，留痕不删除
```

生产段与传输层对称独立：业务组装 `MqMessageOutcomeVO` 信封 → `IMqMessageProducer`（厂商实现）按 `payloadFormat` 选转换器序列化投递 → 消费端接收段按同一格式还原。

## 契约速查

| 类型 | 角色 |
|------|------|
| `IMqContext<R,T>` / `AbstractMqContext<R,T>` | 消费上下文门面（队列级环境 + 处理线程），作用域身份 `getContextId()`；厂商子类实现 `ackMessage`/`nakMessage` 确认语义 |
| `IMqMessageReceiver<R,T>` / `AbstractMqMessageReceiver` | 接收段 `receive(context,…)`：组装·校验·批量建档·按 messageId 配对·ack/nak 回调 |
| `IMqMessageProcessor<R,T>` / `AbstractMqMessageProcessor` | 分发段 `process(context,…)`：按负载类型广播消费，构建 SUCCESS/FAIL 台账行 |
| `IMqRecorder<R,T>` | 可靠性真源：主记录建档/状态推进/失败留痕/回查 + 消费台账落库 + 记录层事务，身份 `getRecorderId()`；除 `get` 外方法均以 `context` 为首参（队列级作用域） |
| `IMqMessageMonopolizer` | 消息级单飞抢占：`monopolize(context, message)` / `release(context, message)` |
| `IMqConsumer<T>` | 业务消费者：`businessId()` + `consumeId()`（台账身份键）+ `consume(T)`（**必须幂等**，失败抛 `MqConsumeException` 声明 retryFlag，禁止触碰厂商确认） |
| `IConsumerRegistry` | 按负载类型的消费者路由表：`register` / `unregister` / `getConsumers` |
| `IMqMessageProducer` | 厂商无关发送契约：`send(MqMessageOutcomeVO)` |
| `IMqMessagePayloadConverter` | 负载 ⇄ 字节双向转换契约：`payloadFormat()` + `convert(T,Class)` / `convert(byte[],Class)` |
| `MqPayloadConverterRegistry` | 按 `payloadFormat` 路由转换器（默认格式 `jdk`），收发两端共用 |
| `MqMessageVO<R,T>` / `MqMessageIncomeVO<R,T>` / `MqConsumeVO<T>` / `MqRecordVO<T>` | 主记录 / 接收段流入消息 / 台账行 / 建档逐条结果 |
| `MqMessageStatusEnum` / `MqMessageConsumeStatusEnum` | 主记录状态机（RECEIVED/PROCESSING/PROCESSED/FAILED）/ 台账成败（SUCCESS/FAIL） |
| `MqConsumeException` | 消费失败信号：携带 `consumeId`/`businessId`/`retryFlag`，提供 `retryable`/`discard`/`*I18n` 工厂 |

泛型含义：`R` = 厂商原始消息类型（仅留档，供排查与重放）；`T extends Serializable` = 业务负载类型（消费者唯一接触面）。传输层与业务层在接收段一次性切开。

## 双层记录模型

| 层 | 承载方法 | 身份键 | 状态 | 职责 |
|----|----------|--------|------|------|
| 消息主记录 | `record` / `updateFlag` / `errorFlag` / `get` | messageId | RECEIVED → PROCESSING → PROCESSED / FAILED | 投递可靠性真源：建档、状态推进、终态留痕 |
| 消费台账 | `recordConsume` | messageId + consumeId | SUCCESS / FAIL（+ 起止时间 + 异常） | 消费可靠性依据：逐消费者事后结果，重试扫描的回灌目标来源 |

- 主记录 **PROCESSED = 分发完成，不等于全员消费成功**；单消费者失败只留 FAIL 台账，不中断其他消费者、不改变主记录终态。
- 台账是**事后记录**（分发完成后一次性批量落库），不是事前任务表——因此进程分发中途崩溃时台账未写，恢复靠主记录 PROCESSING 滞留扫描 + 整体重放，**消费者必须幂等**。
- `beginTransaction(context)` / `endTransaction(context, id)` 供实现方把"状态推进 + 台账落库 + 终态留痕"纳入同一记录层事务；接口默认 no-op。

## 消息发送（IMqMessageProducer）

业务组装 `MqMessageOutcomeVO` 信封后调用 `send`：

| 信封字段 | 语义 |
|----------|------|
| `topic` / `routeKey` | 投递目标（RabbitMQ 落地为交换机 / 路由键） |
| `messageId` | 消费端建档去重的记录主键；为空由厂商发送实现发号 |
| `businessId` | 业务标识，消费端 `resolveBusinessId` 读取（缺省回落 messageId） |
| `payload` / `payloadType` / `payloadFormat` | 业务负载及其类型与序列化格式标识，消费端据此选转换器精确还原 |
| `headers` | 自定义消息头，随厂商消息属性一并写出 |

## 负载转换（IMqMessagePayloadConverter）

v2 摆脱厂商消息转换器（如 Spring AMQP `SerializerMessageConverter`），负载序列化由自建契约承担：

- 实现声明唯一 `payloadFormat()` 标识（内置默认 `JdkMqPayloadConverter` → `"jdk"`）；
- 发送端按信封 `payloadFormat` 经 `MqPayloadConverterRegistry.resolve(...)` 选定实现序列化，接收端按同一头选定实现反序列化，两端严格对称；
- 容器内全部 `IMqMessagePayloadConverter` Bean 会被注册表自动汇集，新增格式（如 JSON）只需注册新 Bean。

## 内存基本实现（com.tbs.mq.v2.impl）

协作层三个契约提供纯 JDK 并发的内存实现，**单机 / 开发 / 自检开箱即用**（进程重启即清空；集群与持久化需换 DB/Redis 实现）：

| 实现 | 契约 | 要点 |
|------|------|------|
| `InMemoryMqRecorder` | `IMqRecorder` | 主记录按 `contextId` 作用域隔离；`record` 以 `putIfAbsent` 幂等去重（重复 messageId 保留首条仍回成功，空 messageId 拒收）；`updateFlag`/`errorFlag` 推进与留痕；`recordConsume` 追加台账；事务方法沿用默认 no-op |
| `InMemoryMqMessageMonopolizer` | `IMqMessageMonopolizer` | 以 `contextId#messageId` 为占有键，并发 Set 原子 `add` 实现进程内单飞互斥 |
| `InMemoryConsumerRegistry` | `IConsumerRegistry` | 以负载 Class 为路由键，`CopyOnWriteArrayList` 承载多消费者，未注册回空集合 |

自检 `InMemoryImplTest` 覆盖建档 / 去重 / 作用域隔离 / 单飞互斥 / 路由增删。

生产级 Redis 落地已在 `redis-mod` 提供：`com.tbs.redis.mq.RedisMqRecorder`（主记录 + 台账 + 失败留痕落 Redis）与 `com.tbs.redis.mq.RedisMqMessageMonopolizer`（Redisson 带租约分布式单飞），装配后契约层与各段代码零改动（见 [redis-mod README 第 9 节](../redis-mod/README.md)）。`IConsumerRegistry` 存的是本地消费者实例，不支持也无必要 Redis 化。

## 快速开始

以下装配形态与 `app` 模块 `CdcV2ConsumerConfig`（CDC 消费链路）一致：协作者全部 `@Bean` 集中装配（实现类本身不加 `@Component`，装配选择权留给使用方；该链路记录器/抢占器已换为 Redis 实现，其余链路可按同样方式替换）。

```java
@Configuration
public class MyConsumerConfig {

    @Bean
    public IMqRecorder<Message, MyEvent> myRecorder() {
        return new InMemoryMqRecorder<>();
    }

    @Bean
    public IMqMessageMonopolizer myMonopolizer() {
        return new InMemoryMqMessageMonopolizer();
    }

    @Bean
    public IConsumerRegistry myConsumerRegistry() {
        return new InMemoryConsumerRegistry();
    }

    // 接收段/分发段薄壳：只绑定 <传输类型, 负载类型> 泛型，编排由父类承担
    // （RabbitMQ 侧为 RabbitMqMessageReceiver<T> / RabbitMqMessageProcessor<T>，见 rabbitmq-mod README）

    @Bean
    public IMqConsumer<MyEvent> myEventConsumer(IConsumerRegistry registry) {
        MyEventConsumer consumer = new MyEventConsumer();
        registry.register(consumer, MyEvent.class);   // 创建即注册，供分发段按负载类型回查
        return consumer;
    }

    @Bean
    @DependsOn("myEventConsumer")                     // 保证注册先于消费启动
    public MyRabbitMqContext myContext() {
        return new MyRabbitMqContext();               // RabbitMqContext<MyEvent> 子类，见 rabbitmq-mod
    }
}
```

消费者实现：

```java
public class MyEventConsumer implements IMqConsumer<MyEvent> {

    @Override
    public String businessId() { return "my-business"; }

    @Override
    public Integer consumeId() { return 1; }           // 同一负载类型内全局唯一

    @Override
    public void consume(MyEvent message) throws MqConsumeException {
        // 必须幂等：同一条消息可能因整体重放被多次投递
        // 失败时抛 MqConsumeException.retryable(...) / .discard(...) 声明可否重试
    }
}
```

发送侧注入 `IMqMessageProducer`（由厂商模块装配，RabbitMQ 侧 `@EnableRabbitMq` 即得）：

```java
@Resource
IMqMessageProducer mqMessageProducer;

public void publish(MyEvent event) {
    MqMessageOutcomeVO<MyEvent> envelope = MqMessageOutcomeVO.<MyEvent>builder()
        .topic("tbs.demo").routeKey("demo.event").payload(event).build();
    mqMessageProducer.send(envelope);
}
```

## 消息头约定（MqHeaderConstants）

| 常量 | 头名 | 用途 |
|------|------|------|
| `BUSINESS_ID` | `businessId` | 消费端 `resolveBusinessId` 读取的业务标识 |
| `PAYLOAD_FORMAT` | `payloadFormat` | 负载序列化格式标识（收发两端选定同一转换器） |
| `PAYLOAD_TYPE` | `payloadType` | 负载类全名，接收端还原目标类型 |
| `RETRY_COUNT` / `FIRST_FAIL_TIME` | `x-retry-count` / `x-first-fail-time` | v1 重投递遗留头，v2 链路不再写入/消费 |

## 扩展指南

1. **接入新 MQ 厂商**：继承 `AbstractMqContext<R,T>` 实现 `ackMessage`/`nakMessage`（确认语义唯一厂商相关点）与 `getMessageType`；按厂商容器提供接收入口；再落地绑定 `<R,T>` 的 `IMqMessageReceiver` / `IMqMessageProcessor` 子类与 `IMqMessageProducer` 实现。参照 `rabbitmq-mod` 的 `com.tbs.rabbitmq.v2` 包（现成范本）。
2. **生产级存储**：实现 `IMqRecorder`（DB 主记录 + 台账两张表）、`IMqMessageMonopolizer`（分布式单飞，如 Redis）、`IConsumerRegistry`，替换内存 Bean 即可，契约层代码零改动。Redis 版记录器与分布式单飞已由 `redis-mod` 提供（`com.tbs.redis.mq`），DB 版仍待落地；表结构与重试扫描/回灌组件尚未落地（见设计大纲 §7.2 待闭环清单）。
3. **新负载格式**：注册 `IMqMessagePayloadConverter` Bean（声明新 `payloadFormat()`），`MqPayloadConverterRegistry` 自动汇集接管。

## 注意事项

1. **消费者必须幂等**：台账为事后记录，分发中途崩溃恢复依赖整体重放，重复投递是设计内行为。
2. **协作层 Bean 需自行装配**：契约层无启用注解、无自动配置；内存实现适合单机/开发，生产环境应换用 `redis-mod` 的 Redis 实现（或后续 DB 实现），否则进程重启记录即丢、v2 可靠性承诺不成立。
3. **重试扫描与回灌尚未落地**：FAILED/FAIL 记录的自动重放是规划中的独立组件（不内嵌于记录层），当前版本消费失败仅留痕。
4. **消费者不得触碰厂商确认**：`consume(T)` 只收负载、不给 context，ack/nak 语义止于传输层，这是"消费环节无厂商确认"原则的结构性保证。
5. **`consumeId` 全局唯一规划**：台账以 `messageId + consumeId` 为身份键，同一负载类型内新增消费者须分配不冲突的 id。

## 包结构

```
mq-mod/
└── mq-api/                                    # v2 契约层（com.tbs.mq，零厂商依赖）
    └── src/main/java/com/tbs/mq/
        ├── constants/                 MqHeaderConstants（消息头约定）
        └── v2/
            ├── core/                  IMqContext / AbstractMqContext（门面 + 处理线程）
            │                          IMqMessageReceiver / AbstractMqMessageReceiver（接收段）
            │                          IMqMessageProcessor / AbstractMqMessageProcessor（分发段）
            │                          IMqRecorder（双层记录真源）· IMqMessageMonopolizer（单飞）
            │                          IMqMessagePayloadConverter / MqPayloadConverterRegistry（负载转换）
            ├── producer/              IMqMessageProducer（厂商无关发送契约）
            ├── consume/               IMqConsumer（业务消费者）· IConsumerRegistry（路由表）
            ├── vo/                    MqMessageVO · MqMessageIncomeVO · MqConsumeVO · MqRecordVO
            ├── enums/                 MqMessageStatusEnum（主记录状态机）· MqMessageConsumeStatusEnum（台账成败）
            ├── exception/             MqConsumeException（retryFlag 重试声明）
            └── impl/                  InMemoryMqRecorder · InMemoryMqMessageMonopolizer
                                       · InMemoryConsumerRegistry · JdkMqPayloadConverter（内存基本实现）
```
