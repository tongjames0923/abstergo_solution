# mq-mod 模块（artifactId：mq）

消息中间件能力聚合模块，提供**厂商无关的 MQ 契约层**（`mq-api`）。各具体 MQ 厂商实现模块（如 `rabbitmq-mod`）基于本模块契约落地发送/消费/重投递能力；本模块自身不依赖任何 MQ 厂商 SDK。

## 目录

- [模块定位](#模块定位)
- [子模块说明](#子模块说明)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [消息发送](#消息发送)
- [消费编排（AbstractMessageDispatcher）](#消费编排abstractmessagedispatcher)
- [消费异常分类](#消费异常分类)
- [幂等控制](#幂等控制)
- [失败重投递链路](#失败重投递链路)
- [配置参考](#配置参考)
- [扩展指南](#扩展指南)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 契约层（`mq-api`），与厂商实现模块解耦 |
| 核心能力 | 消息发送/消费契约、消费分派编排、幂等三态存储、失败消息存储与延迟重投递调度 |
| 依赖 | 仅 `common`（无任何 MQ 厂商 SDK） |
| 启用方式 | `@EnableTbsMq` 注解（无 `spring.factories`） |

## 子模块说明

`mq-mod` 为 `packaging=pom` 的聚合 POM（**不可作为依赖引入**），其下仅一个子模块：

| 子模块 | artifactId | 说明 |
| --- | --- | --- |
| mq-api | `mq-api` | MQ 抽象层：消息发送、消费、幂等、失败重试存储等全部契约接口与开箱即用的重试调度默认装配 |

依赖坐标：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>mq-api</artifactId>
</dependency>
```

## 架构设计

本模块将消息可靠性处理的公共骨架固化为契约与模板方法，让厂商实现只关心"字节的收发"，而把"幂等、重试、死信、状态记账"统一收敛到抽象层：

```
业务应用（生产）
  │  经 IMqProducer.send 发送（自动/显式 messageId 写入消息属性）
  ▼
具体 MQ 厂商（如 rabbitmq-mod 的 @EnableRabbitMq）
  │
  ▼
消费者容器（厂商消费基类，如 rabbitmq-mod 的 AbstractMqConsumer）
  │  对原始消息反序列化后调用 AbstractMessageDispatcher.execute(T)
  │
  ├─ execute 模板：幂等占位 → dispatch 业务 → 结果固化
  │    └─ dispatch 失败 → 异常分类 classify(Throwable) → DispatchOutcome
  ▼
DispatchOutcome 终态 → 消费容器据此决定：
  ├─ SUCCESS / DUPLICATE_SKIPPED     → ACK，无需额外动作
  ├─ RETRY_SCHEDULED                 → FailedMessage 快照入库（IFailedMessageStore），等待调度重投递
  ├─ BUSINESS_DISCARDED / DEAD_LETTER→ 按配置丢弃或死信留痕（DEAD_LETTER 触发 handleDeadLetter 钩子）
  ▼
IFailedMessageStore（redis-mod 的 RedisFaildMessageStore 或 db 实现）
  │
  ▼
RetryMessageDispatcher（纯分发，无调度线程）
  │  由两种互斥驱动其一周期触发：
  │    embedded（默认）→ EmbeddedRetryScheduler 内嵌守护线程
  │    job            → RetryDispatchBackgroundJob 经 IBackgroundJob 交 xxl-job 等外部平台
  ▼
IMessageRepublisher（厂商重投递器，如 rabbitmq-mod 的 RabbitMqMessageRepublisher）
     递增 retryCount + 写 x-retry-count 头 → doRepublish 还原消息重发 → 成功移除 / 失败按档位重新排期
```

## 快速开始

```java
@SpringBootApplication
@EnableTbsMq      // @Import(MqPackAutoRegister) → 扫描 com.tbs.mq，注册 Feature；重复导入按包名去重
public class Application { ... }
```

> 说明：具体厂商模块（如 `rabbitmq-impl` 的 `@EnableRabbitMq`）已包含本模块装配范围。单独使用 `@EnableTbsMq` 适用于"仅引入抽象层 + 存储实现"的场景。

引入后默认获得：`IRetryDelayPolicy`（档位式）、`IFailedMessageStore`（懒委托，真实实现缺省时 Noop 兜底）、`RetryMessageDispatcher` + `EmbeddedRetryScheduler`（内嵌调度）。厂商的 `IMqProducer`/`IMessageRepublisher` 由对应 impl 模块自行装配。

## 消息发送

```java
public interface IMqProducer {
    void send(String exchangeName, String routingKey, Object payload);              // 自动生成 messageId
    void send(String exchangeName, String routingKey, Object payload, String messageId); // 显式指定（空则自动）
}
```

`AbstractMqProducer` 模板统一处理 messageId：未提供时 `UUID`（去连字符），显式提供则原样保留；子类只需实现 `doSend(exchange, routingKey, payload, messageId)`，且必须保证 messageId 被写入消息属性（如 AMQP 的 `MessageProperties.messageId`），作为消费端幂等键的兜底来源。

## 消费编排（AbstractMessageDispatcher）

消费链路的**模板方法编排**，具体厂商的消费基类只需对接容器（ACK/原始消息字节），并按返回的 `DispatchOutcome` 决定后续动作：

```java
public abstract class AbstractMessageDispatcher<T> implements IMessageDispatcher<T> {
    public final DispatchOutcome execute(T message);   // 模板：不抛异常，全失败收敛为终态
    protected abstract void dispatch(T message);       // 业务处理（抛出的异常类型即处理策略）
}
```

`execute(T)` 三段流程：

1. **幂等占位**：`resolveIdempotentKey(message)` 解析幂等键 → `tryAcquire`；失败（重复消息）直接返回 `DUPLICATE_SKIPPED`；
2. **业务分派**：`dispatch(message)`；
3. **结果固化**：成功 → `markDone` 返回 `SUCCESS`；失败 → `release` 占位 → `safeInvokeOnFailure`（回调异常不影响结果）→ `classify(error)`。

异常分类规则（`classify`，判定顺序）：

| 优先级 | 异常类型 | 终态 |
| --- | --- | --- |
| 1 | `MqDeadLetterException` | `DEAD_LETTER` |
| 2 | `MqConsumeException` 其余子类 | `retryable() ? RETRY_SCHEDULED : BUSINESS_DISCARDED` |
| 3 | 存量 `FormattedRuntimeException` | `level.code <= BUSINESS_LOGIC_ERROR` → `BUSINESS_DISCARDED`，否则 `RETRY_SCHEDULED` |
| 4 | 其他未知异常 | `RETRY_SCHEDULED` |

子类可按需重写 `resolveIdempotentKey`（返回 null 表示该消息不参与幂等）与 `onFailure(message, error)`（默认仅记日志，可覆盖为告警/留痕）。通过链式 setter 注入依赖（`idempotentStore`/`failedMessageStore`/`retryDelayPolicy`/`idempotentTtl`），未注入的模块自动跳过对应环节。

## 消费异常分类

消费端通过**异常类型**直接判定策略，而非仅靠错误级别。异常体系：`MqConsumeException`（抽象基类，继承 `FormattedRuntimeException`）→ 三个子类：

| 异常 | 静态工厂 | 默认级别 | `retryable()` | 语义 |
| --- | --- | --- | --- | --- |
| `MqRetryableException` | `retryable(msg)` / `retryableI18n(key, params)` | `SYSTEM_ERROR` | **true** | 瞬时系统故障，入库延迟重投递 |
| `MqBusinessException` | `business(msg)` / `businessI18n(key, params)` | `BUSINESS_LOGIC_ERROR` | false | 确定性业务错误，重试无意义 |
| `MqDeadLetterException` | `deadLetter(msg)` / `deadLetterI18n(key, params)` | `CRITICAL_SYSTEM_ERROR` | false | 毒消息/格式损坏等，跳过重试直接死信 |

```java
// 业务处理中按场景抛不同异常即自动路由到对应策略
if (order.getStatus() == null) {
    throw MqBusinessException.businessI18n("order.status.required");
}
// 下游暂不可用时
throw MqRetryableException.retryable("下游服务不可用");
```

每个子类都有带 cause 的构造器与协变 `errorLevel(ErrorLevelEnum)`。

## 幂等控制

`IIdempotentStore` 采用**三态生命周期**，实现必须保证三个操作的分布式安全：

| 操作 | 签名 | 语义 |
| --- | --- | --- |
| `tryAcquire` | `boolean tryAcquire(String key, Duration ttl)` | 互斥占位（PROCESSING）；键已存在（无论占位/完成态）返回 false |
| `markDone` | `void markDone(String key, Duration ttl)` | 处理成功后固化为完成态（DONE），延续 TTL |
| `release` | `void release(String key)` | 处理失败后释放占位，允许重试再次进入；**不得误删完成态** |

接口 javadoc 明确要求：TTL 必须显著大于消息的最大重试窗口，避免"占位成功后进程崩溃"导致幂等键永久卡死；到期自动放行产生的重复投递由业务幂等兜底。

`AbstractIdempotentStore` 提供两类封装：
- **默认 TTL 重载**：`tryAcquire(key)`/`markDone(key)` 走 `defaultTtl()`，实现方只需声明默认值（建议"最大重试次数×最大单次延迟"的 3 倍以上）；
- **幂等键拼装**：`composeKey(scope, key)` 统一"作用域:业务键"格式（作用域通常传队列名），避免跨队列碰撞。

幂等键来源优先级（由消费基类组合实现）：**业务幂等键 → 消息 messageId → 消息内容摘要**。

## 失败重投递链路

### FailedMessage（消息快照）

消费失败后持久化的完整快照（`@Data`，`Serializable`），字段：`id`（建议 UUID）、`queueName`、`exchangeName`、`routingKey`、`body`（原始字节，避免二次序列化）、`contentType`、`headers`（消息头快照）、`messageId`（重投递保留以维持幂等键连续）、`retryCount`、`firstFailTime`、`errorMessage`、`nextRetryTime`。

### IFailedMessageStore（存储契约）

```java
void save(FailedMessage message);                 // 新增或按 id 更新
List<FailedMessage> fetchDue(Date now, int limit);// 取已到期（nextRetryTime <= now）
void remove(String messageId);                    // 重投递成功后移除
```

`fetchDue` 必须具备**"取出即锁定"的原子语义**（Redis ZPOPMIN / 数据库条件 UPDATE），保证多实例调度下同一条只被一个实例取走。`AbstractFailedMessageStore` 模板固化通用校验：`save` 前校验记录完整性（`id`/`nextRetryTime` 必填，i18n 消息）、`fetchDue` 对 null 兜底空列表、`remove` 对空 id 静默跳过。

### 调度驱动（RetryMessageDispatcher）

纯分发器（`FETCH_BATCH_SIZE=50`/轮），不持有调度线程，由两种**互斥**驱动其一触发：

| 驱动 | 装配条件（`tbs.mq.retry.scheduler`） | 说明 |
| --- | --- | --- |
| `EmbeddedRetryScheduler` | `embedded`（默认） | 自持单线程**守护**调度线程，`scheduleWithFixedDelay`，零配置开箱即用；间隔取 `tbs.mq.retry.interval-millis`（默认 10000ms）；`@PostConstruct` 启动 / `@PreDestroy` 停止 |
| `RetryDispatchBackgroundJob` | `job` | 注册为 `IBackgroundJob`（`name=MqRetryDispatchJob`），交 xxl-job 等外部调度平台集中触发 |

`dispatchDueSafely()` 保证单轮异常不外抛、调度线程存活；存储或重投递器缺失时静默跳过。

### 重投递记账（AbstractMessageRepublisher）

模板方法 `republish(FailedMessage)` 将记账固化：

1. **递增重试状态**：`retryCount + 1` 并同步写入消息头 `x-retry-count`；`firstFailTime` 存在时补 `x-first-fail-time` 头（`putIfAbsent`）；
2. **实际发送**：`doRepublish(failedMessage)`（子类还原原始消息并发送，保留 messageId 与消息头）；
3. **按结果记账**：成功 → `store.remove(id)`；失败 → `scheduleNextRetry` 按 `nextDelay(retryCount)` 重新计算 `nextRetryTime` 重新入库。

子类构造需注入非空 `IFailedMessageStore` 与 `IRetryDelayPolicy`（构造即 `AssertUtil` 校验，缺失抛 i18n 异常）。

### IRetryDelayPolicy / 默认档位策略

```java
public interface IRetryDelayPolicy {
    Duration nextDelay(int retryCount);  // retryCount：当前已完成的重试次数（首次失败入库时为 0）
}
```

默认实现 `SteppedRetryDelayPolicy`（`SteppedRetryDelayPolicy#nextDelay` 已实现档位取用逻辑，见下）：

```java
public class SteppedRetryDelayPolicy implements IRetryDelayPolicy {
    // 默认档位：30秒 → 2分钟 → 10分钟；第 N 次重试取第 N 档（从 0 计），超出后恒定取最后一档
}
```

注意：配置层面**尚无"最大重试次数"的强制上限**——`IRetryDelayPolicy` 与 `RetryMessageDispatcher` 均不按次数终止（resources 中存在 `mq.retry.max.count.invalid` 的 i18n 文案，但契约中未见对应消费点），超过档位数后恒定以最后档延迟，可持续重试直到人工介入或转死信。需自行在 `doRepublish` 或存储层实现次数上限。

## 配置参考

### `tbs.mq.retry.*`（MqRetryProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `scheduler` | String | `embedded` | 调度驱动二选一：`embedded`（内嵌守护线程）或 `job`（IBackgroundJob 桥接外部平台） |
| `interval-millis` | long | `10000` | 内嵌调度间隔（毫秒），仅 `scheduler=embedded` 生效，非正数按默认处理 |

`MqRetryProperty` 不注册为组件，由 `MqSupportConfig` 经 `@EnableConfigurationProperties` 显式激活，避免重复导入时重复注册。调度驱动互斥判定用 `@ConditionalOnProperty`（`matchIfMissing=true` 走 embedded）而非属性 Bean，因条件在 Bean 定义阶段评估早于属性绑定。

### 消息头约定（MqHeaderConstants）

| 常量 | 头名 | 说明 |
| --- | --- | --- |
| `RETRY_COUNT` | `x-retry-count` | 重试次数，由重投递调度器递增写入（**不依赖 Broker 的 x-death 头**） |
| `FIRST_FAIL_TIME` | `x-first-fail-time` | 首次失败毫秒时间戳，用于全链路时延追溯 |

## 扩展指南

### 实现一个 Redis 失败消息存储

```java
public class RedisFailedMessageStore extends AbstractFailedMessageStore {
    // 覆盖 doSave / doFetchDue / doRemove：ZSet 按到期时间排序，fetchDue 用 ZPOPMIN 保证原子占用
}
```

真实实现（如 `redis-mod` 的 `RedisFaildMessageStore`）注册为 `IFailedMessageStore` Bean 后即被 `DelegatingFailedMessageStore` 在运行期自动接管，无需改动抽象层代码。

### 实现厂商重投递器

```java
public class XxxMessageRepublisher extends AbstractMessageRepublisher {
    public XxxMessageRepublisher(IFailedMessageStore store, IRetryDelayPolicy policy) {
        super(store, policy);
    }
    @Override
    protected boolean doRepublish(FailedMessage failedMessage) {
        // 还原消息：new Message(failedMessage.getBody(), new MessageProperties());
        // 保留 messageId 与 headers，向 exchange/routingKey 发送
        return producer.sendRaw(...);   // 返回是否发送成功
    }
}
```

### 自定义重试延迟档位

```java
@Bean
public IRetryDelayPolicy retryDelayPolicy() {
    return new SteppedRetryDelayPolicy(List.of(
        Duration.ofSeconds(10), Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(30)));
}
```

实现 `IRetryDelayPolicy` 后经 `@ConditionalOnMissingBean` 覆盖默认。

### 外部调度平台驱动

```yaml
tbs:
  mq:
    retry:
      scheduler: job   # 停用内嵌线程，改由 xxl-job 触发 MqRetryDispatchJob
```

## 注意事项

1. **聚合模块不可依赖**：依赖请引 `com.tbs:mq-api`，而非 `mq`（`packaging=pom`）。
2. **`@EnableTbsMq` 不含消息厂商实现**：它只装配抽象层与重试存储兜底；真正的收发需要 `rabbitmq-mod` 等厂商模块提供 `IMqProducer`/`IMessageRepublisher` 实现。
3. **无真实存储时消息被丢弃**：`NoopFailedMessageStore.save` 仅记 error 日志并**丢弃消息**（`fetchDue` 恒空）。务必引入 redis-mod 等真实实现，或在首次使用重试能力前确认已装配。
4. **`fetchDue` 必须原子占用**：多实例部署若存储实现不保证"取出即锁定"，将出现重复重投递。实现 `IFailedMessageStore` 时务必遵守该契约。
5. **无强制最大重试次数**：档位策略超过档位数后恒定取最后一档，可持续重试；资源中 `mq.retry.max.count.invalid` 文案无对应消费点，上限需自行在存储/重投递器层实现。
6. **幂等键 TTL 过短风险**：必须显著大于最大重试窗口，否则处理中崩溃后键过期会重复消费（由业务幂等兜底）。
7. **幂等键建议带队列名前缀**：接口 javadoc 建议键含队列名（`composeKey` 提供"作用域:业务键"拼装），避免跨队列键碰撞。
8. **`messageId` 是幂等兜底键**：经 `AbstractMqProducer` 发送的消息会自动补齐 messageId 并写入消息属性；若厂商消费端不解析该属性，幂等将退化为仅依赖业务键。
9. **消费基类须处理 `x-death` 不可依赖**：本框架的重试计数由 `x-retry-count` 头显式维护，因为 requeue 场景不产生 Broker 的 x-death 头。
10. **死信与业务丢弃的区别**：`DEAD_LETTER` 触发死信队列留痕（可追踪），`BUSINESS_DISCARDED` 仅是逻辑丢弃（重试无意义）；两者都走 ACK，注意业务正确性。
11. **内嵌调度器是守护线程**：进程退出不阻塞 JVM 停止；调度间隔最小粒度受 `interval-millis` 控制，达不到 ms 级实时性要求。

## 包结构

```
com.tbs.mq
├── annotation/              @EnableTbsMq（启用入口，@Import(MqPackAutoRegister)）
├── config/                  MqPackAutoRegister（扫描 com.tbs.mq，注册 Feature）
│   ├── MqSupportConfig      默认组件装配（延迟策略/懒委托存储/分发器/双驱动）
│   ├── DelegatingFailedMessageStore  懒委托存储（运行期透传真实实现，缺失 Noop 兜底）
│   └── property/MqRetryProperty      配置属性 tbs.mq.retry
├── constants/               MqHeaderConstants（消息头）、MqI18nConstant（i18n key）
├── model/                   FailedMessage（失败消息快照）、DispatchOutcome（分派终态枚举）
├── api/
│   ├── producer/            IMqProducer / AbstractMqProducer（发送 + messageId 生成）
│   ├── consumer/            IMessageDispatcher / AbstractMessageDispatcher（分派编排模板）、
│   │                        IMessageRepublisher / AbstractMessageRepublisher（重投递记账）、
│   │                        IMqConsumerLifecycle / AbstractMqConsumerLifecycle（运行状态守卫）
│   ├── idempotent/          IIdempotentStore / AbstractIdempotentStore（三态幂等）
│   ├── retry/               IFailedMessageStore / AbstractFailedMessageStore、
│   │                        IRetryDelayPolicy
│   └── exception/           MqConsumeException（基类）+ MqRetryableException /
│                            MqBusinessException / MqDeadLetterException
└── impl/                    EmbeddedRetryScheduler、RetryDispatchBackgroundJob、
                             RetryMessageDispatcher、SteppedRetryDelayPolicy、NoopFailedMessageStore

resources/i18n/
└── messages-mq.properties / messages-mq_en_US.properties   （中英文 i18n 资源）
```
