# RabbitMQ 模块 - MQ v2 厂商传输层实现

## 模块概述

RabbitMQ 模块（`com.tbs:rabbitmq`）是 **mq-mod/mq-api v2 契约层**（记录驱动消费模型）在 RabbitMQ 上的厂商实现，基于 Spring AMQP：

- **v2 消费链路**：`com.tbs.rabbitmq.v2` 提供"厂商无关公共基类 + 两种接入基类"——`AbstractRabbitMqContext`（确认语义）+ `RabbitMqContext`（自建容器）/ `RabbitMqListenerContext`（`@RabbitListener` 注解），业务子类绑定负载类型声明为 Bean 即用
- **v2 统一发送**：`RabbitMqMessageProducer` 落地 `IMqMessageProducer` 契约（信封 → 厂商消息：messageId/payloadType/payloadFormat/businessId 落消息头，payload 按格式序列化）
- **分布式事件广播**：`rabbitmq-broadcast` 将 async 模块的进程内事件系统桥接到 RabbitMQ（消费侧继承 v2 `RabbitMqContext`，发送侧走 `IMqMessageProducer`）
- **日志采集通道**：`rabbitmq-logging` 提供 `ILogRecorder` 的 v2 生产端实现（消费端在 logging-impl 的 `com.tbs.logging.v2`）

### 模块结构

| 子模块 | 坐标 | 职责 | 依赖 |
|--------|------|------|------|
| `rabbitmq-impl` | `com.tbs:rabbitmq-impl` | v2 传输层适配器、v2 发送实现、基础设施装配、资源声明器 | `mq-api`、`spring-boot-starter-amqp`、`common` |
| `rabbitmq-broadcast` | `com.tbs:rabbitmq-broadcast` | 基于 RabbitMQ 的分布式事件广播与接收 | `rabbitmq-impl`、`async`、`common` |
| `rabbitmq-logging` | `com.tbs:rabbitmq-logging` | 操作日志的 v2 MQ 记录器 | `rabbitmq-impl`、`logging-api`、`common` |

> v2 契约（`IMqContext`/`IMqMessageReceiver`/`IMqMessageProcessor`/`IMqRecorder`/`IMqMessageMonopolizer`/`IMqConsumer`/`IConsumerRegistry`/`IMqMessageProducer`/`IMqMessagePayloadConverter` 及各 VO/枚举/异常）与内存基本实现位于 **mq-mod/mq-api**，详见 [mq-mod README](../mq-mod/README.md)；本模块只承担"字节收发 + 厂商确认"的传输层。设计动机与整体模型见 [docs/mq-v2-design-outline.md](../docs/mq-v2-design-outline.md)。

## 启用方式

模块无 `spring.factories` 自动配置，通过注解启用：

```java
@SpringBootApplication
@EnableRabbitMq   // @Import(RabbitMqAutoRegister)：扫描 com.tbs.rabbitmq 与 com.tbs.mq 两个包，注册 Feature.RABBITMQ
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application, args);
    }
}
```

`@EnableRabbitMq` 引入的默认装配（全部 `@ConditionalOnMissingBean`，可被使用方覆盖）：

| 装配类 | 提供内容 |
|--------|----------|
| `TbsRabbitMqConfig` | 基础设施四件套：`ConnectionFactory` / `RabbitTemplate` / `AmqpAdmin` / `MessageConverter`（厂商原生 `SerializerMessageConverter`，仅作 v1 旧链路兜底，v2 负载序列化不经过它） |
| `RabbitMqV2SupportConfig` | v2 生产段：`JdkMqPayloadConverter`（默认格式 `jdk`）、`MqPayloadConverterRegistry`、`RabbitMqMessageProducer`（`IMqMessageProducer` 实现） |
| `RabbitMqMessageSourceProvider` | 模块 i18n 资源（`mq` 相关文案 key）注册 |

> 消费段协作者（`IMqRecorder` / `IMqMessageMonopolizer` / `IConsumerRegistry`）与上下文 Bean **不在本模块自动装配**：存储选择（内存 / DB / Redis）是使用方决策，按 mq-mod README 快速开始的形式以 `@Bean` 集中装配（现成参照：`app` 模块 `CdcV2ConsumerConfig`、logging-impl `LoggingServiceConfig`）。

## 核心组件（com.tbs.rabbitmq.v2）

### 1. AbstractRabbitMqContext&lt;T&gt; - 公共基类（确认语义）

`extends AbstractMqContext<Message, T>`，承载易错且唯一的厂商确认逻辑：

- `R` 固定为 Spring AMQP `Message`；投递通道以 `ThreadLocal` 承载——ack/nak 只在投递线程内被同步调用（接收段内联回调），处理线程不触碰厂商确认，无需跨线程映射；
- `protected final ingest(List<Message>, Channel)`：统一接收入口（写入 currentChannel → `messageReceived` → finally 清理），两种接入方式共用；
- `ackMessage` = `basicAck(tag, false)`；`nakMessage` = `basicNack(tag, false, false)`——**不 requeue**：建档失败恢复只走记录层，不依赖厂商重投风暴。

### 2. RabbitMqContext&lt;T&gt; - 接入方式①：自建监听容器

子类只需实现 `getMessagePayloadType()`（负载类型，注册路由键）、`getContextId()`（队列级作用域身份，全局唯一）、`getMqConsumerProperties()`（传输配置）：

- `afterPropertiesSet`：先启父类处理线程，再按配置自动声明资源（`MqResourceDeclarer`，`autoDeclare=true` 时）并拉起 `SimpleMessageListenerContainer`；
- `destroy`：先停容器再停处理线程，避免停机丢消息；
- **批量消费**：实现 Spring AMQP 原生 `ChannelAwareBatchMessageListener`，开启 `consumerBatchEnabled` 后容器自动识别，同线程收集至多 `batchSize` 条回调 `onMessageBatch` **批量建档、仍逐条 ack**（`onMessage` 单条亦委托批量入口）。

### 3. RabbitMqListenerContext&lt;T&gt; - 接入方式②：@RabbitListener 注解

不自建容器，依托 Spring AMQP 注解接入。子类声明 `@RabbitListener` 方法并在方法体内灌入：

```java
@RabbitListener(queues = "demo.queue")
public void listen(Message message, Channel channel) {
    handleMessage(message, channel);        // 单条 → ingest
}

@RabbitListener(queues = "demo.queue")
public void listenBatch(List<Message> messages, Channel channel) {
    handleMessageBatch(messages, channel);  // 批量 → ingest
}
```

> 所用 `RabbitListenerContainerFactory` 必须 `AcknowledgeMode.MANUAL`（批量需 `setBatchListener(true)`），否则框架自动确认将架空 v2"建档成功才 ack"的铁律。

### 4. RabbitMqMessageReceiver&lt;T&gt; / RabbitMqMessageProcessor&lt;T&gt; - 接收/分发段薄壳

- 接收器：`getPayload` 经容器 `MessageConverter` 反序列化负载、`genMessageId` 优先复用厂商 messageId、`resolveBusinessId` 读消息头 `businessId` 回落 messageId；构造注入 `IMqRecorder<Message,T>`；
- 分发器：仅绑定 `<Message,T>` 使泛型可注入，广播逻辑厂商无关（父类 `AbstractMqMessageProcessor` 承担）；
- 业务侧通常无需覆写，各建一个泛型固定子类（或匿名内部类）即可（见上文装配示例）。

### 5. RabbitMqMessageProducer - v2 统一发送

`IMqMessageProducer` 的 RabbitMQ 落地：信封 `topic`/`routeKey` → 交换机/路由键；`messageId` 空则框架发号并写入厂商消息属性；`payload` 按 `payloadFormat` 经 `MqPayloadConverterRegistry` 选定转换器序列化为字节；`payloadType`/`payloadFormat`/`businessId` 与自定义 headers 落消息头——与消费端反序列化路径严格对称。

### 6. MqConsumerProperties - 消费传输配置（Builder）

`RabbitMqContext` 子类经 `getMqConsumerProperties()` 按实例独立提供（`MqConsumerProperties.builder(queueName)…build()`），v2 生效项：

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `queueName` | （必填） | 队列名 |
| `exchangeName` / `routingKey` | null | 自动声明时使用的交换机与绑定；null 则只挂队列（交换机/绑定由生产端声明，如 cdc-service） |
| `exchangeType` | `DIRECT` | direct / topic / fanout / headers |
| `durable` / `exclusive` / `autoDelete` | `true` / `false` / `false` | 队列与交换机声明属性 |
| `autoDeclare` | `true` | 启动时经 `MqResourceDeclarer` 幂等声明资源 |
| `acknowledgeMode` | `MANUAL` | v2 必须手动确认（建档成功才 ack） |
| `concurrency` | `"1-1"` | 容器消费者并发 |
| `prefetchCount` | `250` | 预取（≤0 不设置） |
| `consumerBatchEnabled` / `batchSize` / `batchReceiveTimeout` | `false` / `1` / `1500`ms | 原生批量建档开关与参数 |
| `autoStart` | `true` | 容器创建后是否立即启动 |
| `deadLetterEnabled` / `deadLetterExchange` / `deadLetterQueue` / `deadLetterRoutingKey` | `false` / null | 队列声明参数（`x-dead-letter-*`，仅厂商侧兜底声明，v2 恢复不依赖之） |
| `messageTtl` / `maxLength` | `0` | 队列声明参数（`x-message-ttl` / `x-max-length`，0 不设置） |

> `retryEnabled` / `maxRetryCount` / `idempotentEnabled` / `idempotentTtl` 为 v1 遗留字段，v2 链路不消费（重试与幂等语义已上移至记录层与消费者契约）。

## 快速开始（完整消费链路示例）

与 `app` 模块 `CdcV2ConsumerConfig` 同构，协作者（记录器/抢占器/注册表）装配参照 [mq-mod README 快速开始](../mq-mod/README.md#快速开始)：

```java
// 接收/分发段薄壳：固定 <Message, MyEvent> 泛型
static class MyMessageReceiver extends RabbitMqMessageReceiver<MyEvent> {
    MyMessageReceiver(IMqRecorder<Message, MyEvent> recorder) { super(recorder); }
}

static class MyMessageProcessor extends RabbitMqMessageProcessor<MyEvent> {
}

// 消费上下文：自建容器接入
static class MyRabbitMqContext extends RabbitMqContext<MyEvent> {
    @Override
    public Class<MyEvent> getMessagePayloadType() { return MyEvent.class; }

    @Override
    public Long getContextId() { return 9L; }   // 队列级作用域身份，全局唯一规划

    @Override
    protected MqConsumerProperties getMqConsumerProperties() {
        return MqConsumerProperties.builder("demo.queue")
            .exchangeName("tbs.demo").routingKey("demo.event")
            .prefetchCount(250)
            .build();
    }
}
```

发送侧注入 `IMqMessageProducer`（`@EnableRabbitMq` 即得默认装配）：

```java
@Resource
IMqMessageProducer mqMessageProducer;

MqMessageOutcomeVO<MyEvent> envelope = MqMessageOutcomeVO.<MyEvent>builder()
    .topic("tbs.demo").routeKey("demo.event").payload(event).build();
mqMessageProducer.send(envelope);
```

自检：`RabbitMqV2AdapterTest`（JDK 动态代理伪造 Channel，断言两种接入的单条/批量 ack/nak）、`RabbitMqV2ProducerRoundTripTest`（发送→接收信封往返对称性）。

## rabbitmq-broadcast - 分布式事件广播

将 async 模块的进程内事件系统桥接到 RabbitMQ 实现跨节点分发，**已整体运行在 v2 链路**：

- `AbstractRabbitMqSupport<T>`：`extends RabbitMqContext<T>`（复用 v2 自装配容器与接收→建档→分发→确认全链路），同时实现 async 的 `IDistributedBusSupport<T>`、`IDistributedDispatcherSupport` 与 common 的 `IDataBridge<T, IPublishResult>`——消费动作即把负载交回分布式事件系统；
- **发送经 v2 统一契约 `IMqMessageProducer`**：以信封（topic=交换机 / routeKey=路由键）投递，与消费端 payloadType/payloadFormat 头严格对称；
- `SimpleRabbitMqSupport`：内置 `SimpleDistributedAcceptedPublishResult` 负载的开箱实现；
- `RabbitMqProperty`（前缀 `tbs.rabbitmq`）：`exchangeName`（默认 `tbs.broadcast.exchange`）、`queueName`（默认 `tbs.broadcast.queue`）、`routingKey`、`nodeId`、`contextId`（默认 1，广播链路的记录层作用域身份）、`messageTtl`（默认 60000）、`consumerConcurrency`、`deadLetterExchange/Queue`。

## rabbitmq-logging - 日志采集通道

- `RabbitMqLoggingRecorder`（`ILogRecorder` 实现）：**投递走 v2 统一发送契约 `IMqMessageProducer`**，信封以 `topic`=交换机、`routeKey`=路由键描述目标，负载 `LoggingRecordForm` 由 v2 负载转换器序列化并携带 payloadType/payloadFormat/contentType 头；与接收端对称：v2 链路按 payloadType 头精确反序列化，v1 旧链路（厂商 `SerializerMessageConverter`）凭 contentType 兜底兼容，两代消费者均可读。
- 消费端不在本子模块：logging-impl 的 `com.tbs.logging.v2`（`LoggingRecordRabbitMqContext` 等）以 v2 记录驱动模型消费日志队列，装配见其 `LoggingServiceConfig`。

## 扩展指南

1. **新增一条 v2 消费链路**：四个 Bean——记录器/抢占器/注册表（或复用既有）、接收/分发薄壳各一、`RabbitMqContext<T>` 子类（或 `@RabbitListener` 接入）、`IMqConsumer<T>` 注册进注册表；`@DependsOn` 保证消费者注册先于上下文启动。
2. **换负载序列化格式**：注册自定义 `IMqMessagePayloadConverter` Bean（新 `payloadFormat()` 标识），收发两端自动接管；注意与仍在运行的 v1 旧消费者之间的 contentType 兜底兼容性。
3. **生产级记录存储**：以 DB/Redis 实现的 `IMqRecorder`/`IMqMessageMonopolizer` Bean 覆盖内存实现即可（本模块无内置存储实现）。

## 注意事项

1. **确认模式只能 MANUAL**：v2 的可靠性基石是"建档成功才 ack"；任何自动确认（容器 AUTO、`@RabbitListener` 默认工厂）都会架空该语义。
2. **nak 不 requeue**：建档失败即拒收，依赖厂商背压重投队列恢复；持续建档失败表现为消费侧背压，而非消息丢失到死信。
3. **上下文记录层不落库即无重试**：当前版本重试扫描/回灌组件尚未落地（mq-mod README 注意事项 3），`IMqRecorder` 内存实现重启即清空——生产部署必须配持久化记录实现。
4. **`getContextId()` 全局唯一规划**：记录/台账按上下文作用域隔离，多条链路撞 id 会互相污染记录归属（现占用：广播链路 1，日志链路 2，CDC 链路 3）。
5. **批量建档仍逐条 ack**：`consumerBatchEnabled` 只合并建档写入，确认粒度不变。
