# RabbitMQ 模块 - 消息队列能力聚合

## 模块概述

RabbitMQ 模块（`com.tbs:rabbitmq`）提供基于 Spring AMQP 的消息队列能力，围绕 `mq-mod/mq-api`（MQ 契约层）实现：

- **消费者框架**：`AbstractMqConsumer` 抽象基类——自动声明资源、启动监听容器、分派结果路由（成功/重试/死信/丢弃）、手动 ACK、幂等控制、失败消息持久化重投
- **统一发送**：`IMqProducer` 契约的 RabbitMQ 落地（messageId 自动补齐），配合 mq-api 的重试调度器形成闭环
- **分布式事件广播**：`rabbitmq-broadcast` 将 async 模块的进程内事件系统桥接到 RabbitMQ，实现跨节点事件分发
- **日志采集通道**：`rabbitmq-logging` 提供 `ILogRecorder` 的 MQ 生产端实现与日志消费者基类

### 模块结构

| 子模块 | 坐标 | 职责 | 依赖 |
|--------|------|------|------|
| `rabbitmq-impl` | `com.tbs:rabbitmq-impl` | 消费者基类、资源声明、发送实现、重投实现、自动装配 | `mq-api`、`spring-boot-starter-amqp`、`async`、`common` |
| `rabbitmq-broadcast` | `com.tbs:rabbitmq-broadcast` | 基于 RabbitMQ 的分布式事件广播与接收 | `rabbitmq-impl`、`async`、`common` |
| `rabbitmq-logging` | `com.tbs:rabbitmq-logging` | 操作日志的 MQ 记录器与消费者基类 | `rabbitmq-impl`、`logging-api`、`common` |

> 契约层（`IMqProducer`/`AbstractMqProducer`、`AbstractMessageDispatcher`、`DispatchOutcome`、`IIdempotentStore`、`IFailedMessageStore`、`IRetryDelayPolicy`、`SteppedRetryDelayPolicy`、`MqHeaderConstants`、`FailedMessage` 及 `MqSupportConfig` 默认装配）位于 **mq-mod/mq-api** 模块，本模块是其在 RabbitMQ 上的厂商实现。

## 模块结构

```
rabbitmq-mod/
├── rabbitmq-impl/                    # 快速使用实现包
│   └── com.tbs.rabbitmq/
│       ├── annotation/               # @EnableRabbitMq
│       ├── config/                   # RabbitMqAutoRegister / RabbitMqSupportConfig /
│       │                             #   TbsRabbitMqConfig / RabbitMqMessageSourceProvider
│       ├── producer/                 # RabbitMqProducer（IMqProducer 实现）
│       ├── retry/                    # RabbitMqMessageRepublisher（重投递实现）
│       └── support/                  # AbstractMqConsumer / MqConsumerProperties / MqResourceDeclarer
├── rabbitmq-broadcast/               # 分布式事件广播支持包
│   └── com.tbs.broadcast.rabbitmq/
│       ├── property/                 # RabbitMqProperty（tbs.rabbitmq）
│       └── support/                  # AbstractRabbitMqSupport / SimpleRabbitMqSupport
└── rabbitmq-logging/                 # 日志支持包
    └── com.tbs.logging.rabbitmq/     # AbstractRabbitMqLogRecordWriter / RabbitMqLoggingRecorder
```

## 启用方式

模块无 `spring.factories` 自动配置，通过注解启用：

```java
@SpringBootApplication
@EnableRabbitMq   // @Import(RabbitMqAutoRegister)：扫描 com.tbs.rabbitmq 与 com.tbs.mq 两个包，注册 Feature.RABBITMQ
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`RabbitMqAutoRegister` 同时扫描 `com.tbs.mq`（mq-api 契约层默认装配包），一次注解即可完成"厂商实现 + 契约层默认组件"的装配。

## 核心组件

### 1. TbsRabbitMqConfig - 基础设施装配

基于 Spring Boot 标准 `spring.rabbitmq.*` 配置（`RabbitProperties`）创建基础 Bean，**全部 `@ConditionalOnMissingBean`**（容器已有对应 Bean 时不重复创建）：

| Bean | 实现与关键参数 |
|------|----------------|
| `ConnectionFactory` | `CachingConnectionFactory`：host 缺省 `localhost`、vhost 缺省 `/`；发布确认 **CORRELATED**（硬编码）；连接/关闭超时 10000ms |
| `RabbitTemplate` | 绑定消息转换器；发布确认回调中 NACK 仅记录错误日志 |
| `AmqpAdmin` | `RabbitAdmin` |
| `MessageConverter` | **`SerializerMessageConverter`（Java 序列化）**，`allowedListPatterns=["*"]` 解除 Spring AMQP 2.4+ 反序列化白名单限制 |

> 注意：默认转换器是 Java 序列化而非 JSON；消息对象需实现 `Serializable`，或自行注册 JSON 转换器覆盖（生产环境建议收紧 `allowedListPatterns`，当前为通配）。

### 2. RabbitMqProducer - 统一发送实现

`IMqProducer` 契约的 RabbitMQ 落地（经 `RabbitMqSupportConfig` 装配，`@ConditionalOnMissingBean` 可覆盖）：

- payload 为业务对象 → 经 `MessageConverter.toMessage` 转换后发送；
- payload 已是原始 `Message` → 直接发送，仅 messageId 缺失时补齐；
- messageId 统一写入消息属性（生产端由 `AbstractMqProducer` 生成 UUID，消费端据此解析幂等键）；
- routingKey 为 null 时按空串发送（适配扇出交换机）。

业务侧发送示例：

```java
@Resource
private IMqProducer mqProducer;

public void sendOrderCreated(OrderCreatedEvent event) {
    mqProducer.send("order.exchange", "order.created", event);
}
```

### 3. AbstractMqConsumer - 消费者基类（核心）

继承 mq-api 的 `AbstractMessageDispatcher<T>`，实现 `ChannelAwareMessageListener` 与 `IMqConsumerLifecycle`。子类只需实现两个方法：

```java
@Component
public class OrderCreatedConsumer extends AbstractMqConsumer<OrderCreatedEvent> {

    @Override
    protected MqConsumerProperties getProperties() {
        return MqConsumerProperties.builder("order.created.queue")
            .exchange("order.exchange")
            .routingKey("order.created")
            .concurrency("2-5")
            .prefetchCount(100)
            .retryEnabled(true)
            .maxRetryCount(3)
            .deadLetterEnabled(true)          // 自动声明 order.created.queue.dlx/.dlq
            .build();
    }

    @Override
    protected void handleMessage(OrderCreatedEvent message) throws Exception {
        // 业务处理；抛出异常的类型决定后续策略（见 mq-api MqConsumeException）
    }
}
```

**初始化流程**（`@PostConstruct init()`）：装配分派依赖（重试策略/失败存储/幂等存储）→ `autoDeclare=true` 时经 `MqResourceDeclarer` 声明资源 → 创建 `SimpleMessageListenerContainer` → `autoStart=true` 时启动。

**消费编排**（`onMessage`）：反序列化 → `execute(payload)`（mq-api 分派模板：幂等检查 + 异常分类）→ 按分派结果路由 → **finally 中一律 ACK**（`ackSilently`，ACK 失败仅记日志）：

| DispatchOutcome | 后续动作 |
|-----------------|----------|
| `SUCCESS` / `DUPLICATE_SKIPPED` | 直接 ACK |
| `BUSINESS_DISCARDED` | `deadLetterOnBusinessError=true`（默认）→ 转死信；否则丢弃 |
| `DEAD_LETTER` | 转死信留痕，触发 `handleDeadLetter` 钩子 |
| `RETRY_SCHEDULED` | 未超 `maxRetryCount` 且 `IFailedMessageStore` 可用 → 持久化失败快照等待重投调度；超限/存储未注入/入库失败 → 降级转死信（不静默丢失） |

**重试机制**：全程不使用 Broker 原生 requeue 与 x-death 头，重试次数由自定义消息头 `x-retry-count`（`MqHeaderConstants.RETRY_COUNT`）维护；失败快照（`FailedMessage`：body/headers/contentType/messageId/retryCount/firstFailTime/errorMessage 截断 500 字符/nextRetryTime）由 mq-api 的重投递调度器到期调用 `RabbitMqMessageRepublisher` 原样还原重发。默认延迟档位 `SteppedRetryDelayPolicy`：30 秒 → 2 分钟 → 10 分钟。

**幂等控制**（`idempotentEnabled=true` 时生效，依赖容器中 `IIdempotentStore` 实现——如 redis-mod 提供的 Redis 实现，缺失时自动降级关闭并 warn）：

- 幂等键三级兜底：`resolveBusinessIdempotentKey(message)` 业务键 → 消息 `messageId` → 消息体 MD5 摘要，统一加 `队列名:` 前缀防跨队列碰撞；
- 幂等键 TTL：未配置时取最大重试窗口（各档位延迟之和）的 3 倍。

**可选扩展点**：

| 方法 | 用途 |
|------|------|
| `handleMessageFailure(T, Throwable)` | 失败回调（默认记日志），可覆盖做告警/持久化 |
| `handleDeadLetter(Message, Throwable)` | 死信钩子（默认记日志），可覆盖做告警台账 |
| `resolveBusinessIdempotentKey(T)` | 提供业务幂等键（默认 null 走 messageId 兜底） |
| `createContainer()` | 自定义监听容器配置 |
| `deserialize(Message)` | 自定义反序列化（默认走 `MessageConverter`） |

**生命周期**：`start()` / `stop()` / `isRunning()`（`IMqConsumerLifecycle`），AtomicBoolean 守卫保证幂等；`@PreDestroy` 自动停止。

**已废弃方法**：`send(T)` / `send(String, String, T)` 已标注 `@Deprecated`——请改用统一发送出口 `IMqProducer`，以保证 messageId 自动补齐。

### 4. MqConsumerProperties - 消费者配置

Builder 模式按实例独立配置，全部属性与默认值：

| 分组 | 属性 | 默认值 | 说明 |
|------|------|--------|------|
| 队列 | `queueName` | 必填 | 队列名称 |
| | `exchangeName` / `routingKey` | null | 为 null 时不声明交换机 / 不建绑定 |
| | `exchangeType` | `DIRECT` | DIRECT / FANOUT / TOPIC / HEADERS |
| 队列属性 | `durable` / `exclusive` / `autoDelete` | true / false / false | 队列声明属性 |
| | `autoDeclare` | true | 启动时自动声明队列/交换机/绑定/死信资源 |
| | `messageTtl` / `maxLength` | 0 / 0 | 消息 TTL（毫秒）/ 队列最大长度，0 不限制 |
| 消费行为 | `acknowledgeMode` | `MANUAL` | ACK 模式 |
| | `concurrency` | `"1-1"` | 并发度，格式 `"min-max"` |
| | `prefetchCount` | 250 | 预取数量，0 不限制 |
| | `autoStart` | true | PostConstruct 自动启动 |
| 重试 | `retryEnabled` | true | 启用持久化重试（不使用 Broker requeue） |
| | `maxRetryCount` | 3 | 超限转死信 |
| | `retryDelayPolicy` | null | null 用容器默认 `SteppedRetryDelayPolicy`（30s→2min→10min） |
| 幂等 | `idempotentEnabled` | false | 启用消费幂等（需 IIdempotentStore） |
| | `idempotentTtl` | null | null 取最大重试窗口 ×3 |
| 死信 | `deadLetterEnabled` | false | 自动声明死信资源 |
| | `deadLetterOnBusinessError` | true | 业务性失败是否同样转死信（false 直接丢弃） |
| | `deadLetterQueue` / `deadLetterExchange` / `deadLetterRoutingKey` | null | 缺省命名为 `队列名.dlq` / `队列名.dlx` |

### 5. MqResourceDeclarer - 资源自动声明

启动时按配置声明队列、交换机（4 种类型）、绑定关系与死信资源：

- **死信留痕**：`deadLetterEnabled=true` 且未指定 `deadLetterExchange` 时，框架托管声明扇出型死信交换机 `队列名.dlx` 与死信队列 `队列名.dlq`（均持久化），主队列 `x-dead-letter-exchange` 指向该交换机；**死信资源先于主队列声明**，避免引用不存在的交换机；自行指定死信交换机的存量配置由使用方自行管理；
- 队列参数：`x-message-ttl`（>0 时）、`x-max-length`（>0 时）、死信参数；
- `AmqpAdmin` 未注入时跳过声明并 warn（不影响消费已存在的队列）。

### 6. RabbitMqMessageRepublisher - 失败重投递实现

`IMessageRepublisher` 契约的 RabbitMQ 落地（经 `RabbitMqSupportConfig` 装配）：将 `FailedMessage` 快照还原为原始消息（body/headers/contentType/messageId 原样恢复），经 `RabbitTemplate` 重发到原交换机与路由键；重试次数头递增与存储记账由 mq-api 基类模板统一完成。

### 7. 国际化

`RabbitMqMessageSourceProvider`（order=-500）自动注册 mq-api 模块的 `i18n/messages-mq.properties`（中）/ `messages-mq_en_US.properties`（英）资源文件，经 common 模块 MessageSource 合并机制生效。

## 分布式事件广播（rabbitmq-broadcast）

将 async 模块进程内事件系统（`IEventBus`/`IEventDispatcher`）桥接到 RabbitMQ，实现跨节点事件分发。

### RabbitMqProperty - 广播全局配置

`@ConfigurationProperties("tbs.rabbitmq")`：

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `exchange-name` | `tbs.broadcast.exchange` | 广播交换机 |
| `queue-name` | `tbs.broadcast.queue` | 广播队列 |
| `routing-key` | `tbs.broadcast.routing.key` | 路由键 |
| `node-id` | null | 节点标识（分布式环境区分节点） |
| `message-ttl` | 60000 | 广播消息 TTL 60 秒 |
| `max-retry-count` | 3 | 消费重试上限 |
| `consumer-concurrency` | `"1-1"` | 消费并发度 |
| `dead-letter-exchange` / `dead-letter-queue` | null | 为空则不启用死信队列 |

```yaml
tbs:
  rabbitmq:
    node-id: node-1
    message-ttl: 60000
    dead-letter-exchange: tbs.broadcast.dlx   # 可选：启用死信
```

### AbstractRabbitMqSupport - 广播支持基类

`AbstractMqConsumer<T>` 子类，同时实现 async 事件系统的 `IDistributedBusSupport<T>`、`IDistributedDispatcherSupport` 与 common 的 `IDataBridge<T, IPublishResult>`：

- **消费侧**：`getProperties()` 由 `RabbitMqProperty` 适配生成（手动 ACK、TTL、死信交换机透传）；`handleMessage` → `receiveEvent(data)` → `handleEvent` → **先 `unbridge` 再交 `IEventDispatcher` 分发**到本地订阅者；
- **发布侧**：`broadcastEvent(result)` 经统一出口 `IMqProducer` 发送到 `tbs.broadcast.exchange`/routingKey（messageId 自动补齐）；
- `nodeId()` 返回 `RabbitMqProperty.getNodeId()`。

### SimpleRabbitMqSupport - 默认桥接实现

事件对象桥接（进程内 `IPublishResult` ↔ 跨节点传输对象 `SimpleDistributedAcceptedPublishResult`，传输内容为 event / publisherId / handleMode）：

- `bridge(IPublishResult)` → `new SimpleDistributedAcceptedPublishResult(event, publisher.getPushlisherId(), handleMode)`
- `unbridge(SimpleDistributedAcceptedPublishResult)` → `new SpringManagedListenablePublishResult(event, publisher, handleMode)`

自定义事件类型时，继承 `AbstractRabbitMqSupport<T>` 并实现自己的 bridge/unbridge 即可。

## 日志采集通道（rabbitmq-logging）

为 logging 模块提供 MQ 传输支撑（全链路见 logging 模块 README）：

- **`RabbitMqLoggingRecorder`**：`ILogRecorder` 生产端实现——构造时指定 exchange/routingKey（app 模块注册为 `new RabbitMqLoggingRecorder("tbs.log.queue", "systemLog")`）；`record(form)` 经 `RabbitTemplate.convertAndSend` 发送（失败仅记日志不抛出）；`flush()` 发送 `flushFlag=1` 空表单通知服务端刷写缓冲；
- **`AbstractRabbitMqLogRecordWriter`**：`AbstractMqConsumer<LoggingRecordForm>` 空壳基类，日志服务（logging-impl 的 `DbLogWriter`）继承它并实现 `getProperties()`/`handleMessage()`，自动获得本模块全部消费基础设施（手动 ACK、重试、死信、幂等）。

## 与 Spring Boot 自动配置的关系

`TbsRabbitMqConfig` 读取 Spring Boot 标准 `spring.rabbitmq.*` 配置（`RabbitProperties`），且全部 Bean 带 `@ConditionalOnMissingBean`：

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: admin
    password: admin123
    virtual-host: /
```

容器中已存在 `ConnectionFactory`/`RabbitTemplate`/`AmqpAdmin`/`MessageConverter` 时，模块配置让位——可直接复用 Spring Boot AMQP 自动配置或自定义 Bean。

## 扩展开发

### 自定义消费者

见上文 `AbstractMqConsumer` 示例；按需覆盖扩展点（失败回调/死信钩子/业务幂等键/容器定制）。

### 替换消息转换器为 JSON

```java
@Configuration
public class JsonMessageConverterConfig {

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // 默认转换器与消费端反序列化需一致
        return converter;
    }
}
```

> 注意：替换后所有生产/消费端必须使用同一转换器，且历史 Java 序列化消息将无法消费。

### 自定义重试延迟策略

实现 `IRetryDelayPolicy`（mq-api）并注册为 Bean（`defaultRetryDelayPolicy`），或在消费者 `MqConsumerProperties.builder().retryDelayPolicy(...)` 按实例指定。

### 自定义失败消息存储 / 幂等存储

实现 `IFailedMessageStore` / `IIdempotentStore`（mq-api）并注册为 Bean；redis-mod 已提供 Redis 实现，注册后消费者的重试持久化与幂等控制自动启用。

### 覆盖发送/重投实现

注册自定义 `IMqProducer` / `IMessageRepublisher` Bean 即可让位 `RabbitMqSupportConfig` 的默认装配（如迁移到其他 MQ 厂商时仅替换实现层）。

## 使用注意事项

1. **默认转换器为 Java 序列化**：消息对象需实现 `Serializable`；跨语言/跨版本场景建议替换为 JSON 并保持两端一致
2. **反序列化白名单为通配**：`SerializerMessageConverter` 的 `allowedListPatterns=["*"]` 便于开发，生产环境建议收紧为可信包前缀
3. **重试不走 Broker requeue**：失败消息持久化到 `IFailedMessageStore` 由调度器重投；未注册 `IFailedMessageStore` 时 `RETRY_SCHEDULED` 消息会丢弃（warn 日志），生产环境务必提供（redis-mod 实现或自建 DB 实现）
4. **死信依赖声明顺序**：框架自动声明的死信资源先于主队列创建；修改队列参数（TTL/maxLength/死信指向）后需删除旧队列重新声明（AMQP 特性）
5. **发送务必用 IMqProducer**：`AbstractMqConsumer.send()` 已废弃，直接用 `RabbitTemplate` 发送会丢失 messageId，影响消费端幂等
6. **广播消息 TTL 默认 60 秒**：离线节点的广播队列积压消息过期后转入死信/丢弃，分布式事件不保证补投

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
