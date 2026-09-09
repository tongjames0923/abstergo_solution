# Async 模块 - 异步编程解决方案

## 模块概述

Async 模块（`com.tbs:async`）提供一套完整的进程内异步编程解决方案，包含五个子域：

| 子域 | 包 | 职责 |
|------|-----|------|
| 长期异步任务 | `com.tbs.async` | `@LongTermAsync` 注解 + AOP，方法级 fire-and-forget |
| 线程池管理 | `com.tbs.thread` | 多层级线程池统一管理、按名称/预估耗时/优先级路由任务 |
| 事件广播 | `com.tbs.broadcast` | 进程内事件发布/订阅，支持优先级队列、同步/异步分发 |
| 任务调度 | `com.tbs.schedule` | 一次性延迟任务调度（时间轮/延迟队列）、策略化执行、可插拔持久化 |
| 锁 | `com.tbs.lock` | 锁抽象 + JDK 本地锁实现（SPI 可替换为分布式锁） |

模块依赖 `com.tbs:common`（代理模式、断言、i18n 等基础能力）与 `spring-boot-starter-aop`，不依赖 Redis/MQ 等外部中间件。

## 功能特性

- **长期异步任务**：`@LongTermAsync` 标注方法即异步执行，每次调用创建独立线程，异常回调可自定义
- **多层级线程池**：主线程池 + 长任务线程池，任务按"指定名称 > 预估耗时/优先级 > 默认层"三级路由；提交时经 `ContextPropagator` 链式传播父线程上下文（如 Locale、请求上下文）
- **事件广播**：发布者 / 事件总线（优先级队列 + 信号量）/ 分发器 / 订阅者四层模型，事件状态机跟踪（8 状态），预留分布式总线/分发扩展点
- **任务调度**：`TaskScheduler` 单一门面，"一次性触发"语义；时间轮与 DelayQueue 两种时序策略可切换；执行策略三级覆盖（任务级 > 类型级 > 全局级）、并发隔离分组、生命周期监听、任务持久化 SPI
- **锁抽象**：`ILock` / `IConditionLock` / `IReadWriteLock` 接口 + `JdkLockProvider` 默认实现，`LockUtil` 提供"获取锁并执行"的函数式用法

## 依赖引入

在 Maven 项目中添加依赖（版本由父 POM `com.tbs:solution` 统一管理）：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>async</artifactId>
</dependency>
```

## 启用方式

模块无 `spring.factories` 自动配置，通过注解式启用。在启动类或配置类上标注：

```java
@SpringBootApplication
@EnableTbsAsyncPack                 // 默认同时启用事件广播(useBroadcast=true)与任务调度(useSchedule=true)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- `@EnableTbsAsyncPack(useBroadcast = false)`：关闭事件广播子域；
- `@EnableTbsAsyncPack(useSchedule = false)`：关闭任务调度子域；
- 也可单独启用调度子域：`@EnableTbsSchedule`（内部同样经自动注册器扫描 `com.tbs.schedule` 包）；
- 自动注册器默认扫描 `com.tbs.async`、`com.tbs.lock`、`com.tbs.thread` 包，并向 `FeatureManager` 注册 `Feature.ASYNC`；
- `AsyncConfig` 同时开启 Spring 的 `@EnableAsync` 与 `@EnableScheduling`；
- `ContextPropagatorAsyncConfig` 将 `@Async` 默认执行器接入上下文传播链（见"多层级线程池"一节）。

## 核心组件

### 1. 长期异步任务（@LongTermAsync）

在任意 Spring Bean 方法上标注 `@LongTermAsync`，方法调用将立即返回（返回值固定为 `null`），实际逻辑由 AOP 切面提交到独立线程执行——**不占用线程池**，适用于长耗时、低频的后台任务：

```java
@Service
public class ReportService {

    @LongTermAsync("report-generator-thread")   // 线程名；缺省时为 "类名-方法名"
    public void generateDailyReport() {
        // 长时间运行的报表生成逻辑
    }

    @LongTermAsync(value = "cleaner-thread",
        onThreadError = CustomThreadError.class) // 自定义异常处理器（默认 DefaultOnThreadError，仅记录日志）
    public void cleanExpiredData() {
        // ...
    }
}
```

说明：

- 注解属性：`value`（线程名，默认空串）、`onThreadError`（`IOnThreadError` 实现类，默认 `DefaultOnThreadError.class`）；
- 切面执行异常时回调 `IOnThreadError#onThreadError(Throwable, Thread)`。

### 2. 线程池管理（thread 包）

`IThreadPoolManager` 仅一个方法：`void submit(IThreadRequest request, IOnThreadError onThreadError)`。

任务描述 `IThreadRequest` 的默认实现 `SimpleThreadRequest` 提供 Builder 构建；提交时线程选择逻辑（`AbstractThreadPoolManager`）：

1. `expectedExecutorServiceName` 非空 → 按名称精确匹配层级；
2. 否则按 `expectedCostTime`（预估耗时，毫秒）匹配首个"最大可接受耗时"满足的层级（层级按优先级升序尝试）；
3. 均未命中 → 默认层级。

```java
@Resource
private IThreadPoolManager threadPoolManager;

public void syncOrders() {
    threadPoolManager.submit(
        SimpleThreadRequest.Builder.create("order-sync", () -> doSync())   // 名称 + Runnable（或 IThreadCall）
            .withPriority(10)                       // 可选：优先级
            .withExpectedCostTime(10_000)           // 可选：预估耗时 10s → 匹配可接受该耗时的层级
            .withExpectedExecutorServiceName("mainExecutorService") // 可选：直接指定层级
            .build(),
        new DefaultOnThreadError());                // 异常回调；为 null 且任务抛异常时直接抛出
}
```

默认装配（`AsyncConfig`，可注册自定义 `IThreadPoolManager` 覆盖）：

| 层级 | Bean | 参数 | 说明 |
|------|------|------|------|
| 主线程池 `mainExecutorService`（`@Primary`） | `ThreadPoolExecutor` | 核心 10 / 最大 100 / 空闲 60s / 队列 100（`ExecutorProperty` 可配） | 常规异步任务，拒绝策略 `CallerRunsPolicy` |
| 长任务线程池 `longTermExecutorService` | `LongTermThreadExecutor` | 每任务新建线程，容器销毁时中断全部 | 最大可接受耗时 5 分钟，优先级 100 |

上下文传播：管理器持有容器内全部 `ContextPropagator`（common 模块约定，统一经 `ContextPropagators` 工具包装），每次 `submit` 时链式包装任务，将父线程上下文快照传播到工作线程，执行后**恢复式清理**（还原工作线程执行前状态，`CallerRunsPolicy` 下任务回退到提交线程执行时不会误清提交线程自身上下文）。

**Spring `@Async` 接入**：`ContextPropagatorAsyncConfig` 实现 `AsyncConfigurer`，将 `@Async` 注解方法的默认执行器从 Spring 回退的 `SimpleAsyncTaskExecutor`（每任务一线程、无上下文传播）改为经传播链包装的主线程池 `mainExecutorService`——`@Async` 方法既复用池化线程，又能读取请求上下文/Locale。

### 3. 事件广播（broadcast 包）

四层组件模型：

```
IPublisher 发布者 ──publish──▶ IEventBus 事件总线（优先级队列+信号量）──▶ IEventDispatcher 分发器 ──▶ AbstractSubscriber 订阅者
```

#### 事件定义

`IEvent` 核心方法：`getEventId()`（实例唯一标识）、`getEventName()`（类型名称）、`getEventData()`（字节载荷）、`getEventPriority()`（默认 0，越大越优先）。默认实现 `SimpleEvent`：

```java
public class OrderPaidEvent extends SimpleEvent {

    public OrderPaidEvent(String orderId) {
        super("ORDER_PAID_" + orderId, "订单支付完成事件");  // (eventId, eventName)
        setDataForSerialization(new OrderPayload(orderId));  // 载荷经 Java 序列化存入 byte[]
    }
}

// 订阅侧还原载荷
OrderPayload payload = (OrderPayload) event.getDeserializedData();
```

#### 发布事件

`IPublisher` 方法：`publish(IEvent, HandleMode)`、`pushlishSync(IEvent)`、`pushlishAsync(IEvent)`（方法名拼写确为 `pushlish`，使用时注意）、`getPushlisherId()`。默认发布者 Bean 为 `DispatcherImportedPublisher`（绑定默认分发器）：

```java
@Resource
private IPublisher eventPublisher;

public void onOrderPaid(Order order) {
    eventPublisher.pushlishAsync(new OrderPaidEvent(order.getId()));  // 异步发布
    // 或 eventPublisher.pushlishSync(event)  同步发布
}
```

#### 订阅事件

继承 `AbstractSubscriber` 并注册为 Bean（`IEventDispatcher#register` 自动收集）：

```java
@Component
public class OrderPaidSubscriber extends AbstractSubscriber {

    @Override
    public String getSubscriberId() {
        return "order-paid-subscriber";
    }

    @Override
    public boolean supportEvent(String eventId, String eventName) {
        return eventName.contains("订单支付");
    }

    @Override
    public boolean supportPublisher(IPublisher publisher) {
        return true;   // 接收任意发布者
    }

    @Override
    protected int onReceive(IEvent event, IPublisher publisher) {
        // 业务处理
        return RECEIVE_SUCCESS;   // RECEIVE_SUCCESS(1) / RECEIVE_FAILED(0) / RECEIVE_RETRY(-1)
    }
}
```

返回值决定事件状态：`RECEIVE_SUCCESS` → `DONE`，`RECEIVE_FAILED`（或抛异常）→ `FAILED`，`RECEIVE_RETRY` → `REQUIRE_RETRY`。

#### 分发与状态

- `HandleMode`：`SYNC`（同步）/ `ASYNC`（异步，订阅者经 `threadPoolManager` 执行）；
- `EventStatusEnum`：`IDLE` / `IN_QUEUE` / `IN_PROCESS` / `DONE` / `FAILED` / `CANCELED` / `REQUIRE_CANCEL` / `REQUIRE_RETRY`（前三终态）；
- `IEventStatusListenner`（拼写如此）：监听状态变更 `onEventStatusChange(newStatus, oldStatus, result)` 与处理异常 `onEventHandleError(Throwable, result)`；
- 默认实现：`SimpleEventBus`（`PriorityQueue` 按事件优先级排序 + 信号量阻塞唤醒）、`SimpleEventDispatcher`（遍历订阅者按模式分发，支持 `IHandleEventError` 错误处理回调）；
- 分布式扩展点：`IDistributedBusSupport` / `IDistributedDispatcherSupport`（基类 `AbstractDistributedSupport`），容器中存在实现 Bean 时总线与分发器自动启用（默认进程内，`Optional.empty()`）。

### 4. 任务调度（schedule 包）

调度框架对外唯一门面为 `TaskScheduler`，所有提交均为"**一次性触发**"语义（到点执行一次即终结，无重复定时概念）。内部架构：

```
TaskScheduler 门面
   └─▶ SchedulerCore 调度内核（tick() 单次驱动：轮询到期 → 派发）
         ├─ ScheduleStrategy 时序策略（TimingWheelStrategy 时间轮 / DelayQueueStrategy 延迟队列）
         ├─ TaskDispatcher 派发器（默认 LocalTaskDispatcher：本地线程池执行）
         │     └─ TaskExecutor 执行器（按 taskType 路由，默认 LocalRunnableTaskExecutor）
         ├─ ScheduleTrigger 触发器（默认 LocalThreadScheduleTrigger：常驻线程信号量轮询）
         ├─ TaskStore 存储 SPI（默认 NoopTaskStore 纯内存）
         └─ LifecycleListener 生命周期监听（提交/派发/运行/成功/失败/取消）
```

#### 提交任务

```java
@Resource
private TaskScheduler taskScheduler;

// 方式1：便捷提交 —— 延迟执行的本地任务（非持久化，重启丢失）
ScheduleHandle handle = taskScheduler.schedule(() -> clearCache(), Duration.ofMinutes(10));
handle.cancel();   // 软取消：到期轮询时跳过丢弃

// 方式2：指定绝对时刻（注意参数为 Date）
taskScheduler.scheduleAt(() -> sendReport(), new Date());

// 方式3：可持久化任务 —— 自定义 TaskPayload + TaskExecutor，配合 TaskStore 落盘
TaskSubmission submission = TaskSubmission.of("order-timeout", new OrderTimeoutPayload(orderId), triggerAt);
taskScheduler.submit(submission);
```

`TaskSubmission` 构建入口：

- `local(Runnable, Duration)` / `localAt(Runnable, Date)`：内存态本地任务（`isPersistable()==false`，接入持久化存储时内核跳过落盘）；
- `of(String taskType, TaskPayload payload, Date triggerAt[, ExecutionPolicy policyOverride])`：可序列化载荷的持久化入口。

#### 时序策略

- `TimingWheelStrategy(tickDuration, wheelSize, maxTicksPerPoll)`：环形数组 + round 计数的时间轮（默认）；
- `DelayQueueStrategy`：基于 JDK `DelayQueue`（毫秒精度，`drainTo` 批量取尽到期任务）。

#### 执行策略（ExecutionPolicy）

策略内容：`getTimeout()`（执行超时）、`getIsolationGroup()`（并发隔离分组，同组共享执行线程池）、`getPriority()`（同批到期任务执行先后，不影响触发精度）、`getErrorHandle()`（失败处理器）。解析遵循**三级覆盖**：任务级 `policyOverride` > 类型级（实现 `TypedExecutionPolicy` 并声明 `getTaskType()`）> 全局默认。失败处理默认 `SimpleLogErrorHandle`（仅记录日志，一次性语义下不自动重试）。

#### 派发与执行

- `LocalTaskDispatcher`：内部 `cachedThreadPool`（`schedule-dispatcher-N` 守护线程）；按 taskType 从 Spring 容器匹配 `TaskExecutor`（`supports(String)`），并编排 `LifecycleListener` 状态回调；
- 默认执行器 `LocalRunnableTaskExecutor` 处理 `local` 类型任务（直接运行 `LocalRunnablePayload` 承载的 Runnable）。

#### 触发器与生命周期

- 默认触发器 `LocalThreadScheduleTrigger(pollTime, pollTimeUnit)`：实现 `IStartup`，应用启动后以 `@LongTermAsync("本地调度触发器")` 起常驻线程，信号量惰性唤醒（新任务提交立即 `tick()`，否则等待轮询周期）；`tbs.schedule.embedded-trigger=true`（默认）时装配；
- 内核 `SchedulerCore` 提供 `destroy()` 优雅停机：停止触发循环，`executeAllOnDestroy=true` 时将全部未完成任务提前派发（默认 `false`，直接丢弃）。

#### 持久化（可选）

- `TaskStore` SPI：`save` / `find(taskId)` / `remove(taskId)` / `loadUnfinished()`，默认 `NoopTaskStore` 纯内存；注册 DB/Redis 实现即可获得重启恢复能力（内核启动时自动加载未终结任务重新调度）；
- `TaskSubmissionTransformer`：实现 common 模块 `ITransformer`，提供 `TaskSubmission` 与 JSON 快照互转（触发时刻以 epoch 毫秒存储、载荷/策略以"实现类全限定名 + JSON"成对存储且类型限定为 `TaskPayload`/`ExecutionPolicy` 实现；`policyOverride` 还原失败降级为空，由三级策略解析兜底），供 `TaskStore` 实现落盘（如 redis-mod 的 `RedisTaskStore`）。

### 5. 锁（lock 包）

锁接口体系（JDK 本地锁默认实现，无 Redis 依赖）：

| 接口 | 方法 | 说明 |
|------|------|------|
| `ILock` | `tryLock(Duration)`、`unLock()`、`isLocked()`、`isHeldByCurrentThread()` | 锁基础契约 |
| `IConditionLock extends ILock` | `newCondition()` | 条件变量支持 |
| `IReadWriteLock` | `readLock()`、`writeLock()` | 读写锁 |
| `ILockProvider` | `getLocker(Object target)`、`getConditionLocker(Object target)` | 锁提供者 SPI |

默认实现：`JdkLockProvider`（按目标对象缓存 `LockAdapter`，内部 `ReentrantLock`）；`tbs.lock.lock-provider-type` 可替换为分布式锁实现（如 redis-mod 提供的 Redisson 实现）。

`LockUtil` 函数式用法（静态单例，内部持有 `ILockProvider`）：

```java
// 按字符串/对象/类名获取锁
LockUtil.getInstance().getLockerByStr("order-stock-" + skuId);

// 获取锁并执行：tryLock 失败（默认 1 秒，tbs.lock.accquire-timeout 可配）抛出格式化异常
LockUtil.getInstance().lockThen("order-stock-" + skuId, () -> {
    deductStock(skuId);
});

LockUtil.getInstance().lockThen(OrderService.class, () -> { /* ... */ });
```

## 配置说明

完整配置示例（所有项均有默认值）：

```yaml
tbs:
  async:
    executor:                       # 线程池配置（ExecutorProperty）
      main-executor-service-core-pool-size: 10        # 主线程池核心线程数，默认 10
      main-executor-service-max-pool-size: 100        # 最大线程数，默认 100
      main-executor-service-queue-capacity: 100       # 任务队列容量，默认 100
      main-executor-service-keep-alive-seconds: 60    # 线程空闲超时（秒），默认 60
      main-executor-service-thread-name-prefix: mainExecutorService-   # 线程名前缀
      main-executor-service-thread-factory: org.springframework.scheduling.concurrent.CustomizableThreadFactory
      main-executor-service-uncaught-exception-handler: java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy

  broadcast:                      # 事件广播（BroadcastProperty）
    event-bus-type: com.tbs.broadcast.bus.impl.SimpleEventBus              # 事件总线实现
    event-dispatcher-type: com.tbs.broadcast.dispatcher.impl.SimpleEventDispatcher   # 分发器实现

  lock:                           # 锁（LockProperty）
    lock-provider-type: com.tbs.lock.impls.providers.JdkLockProvider        # 锁提供者，默认 JDK 本地锁
    accquire-timeout: 1000                                                  # lockThen 获取锁超时（毫秒），默认 1000

  schedule:                       # 任务调度（ScheduleProperty）
    enable: true                    # 是否装配调度内核，默认 true
    strategy: com.tbs.schedule.impl.TimingWheelStrategy      # 时序策略，默认时间轮
    poll-time: 5                    # 触发器轮询间隔，默认 5
    poll-time-unit: SECONDS         # 轮询间隔单位，默认秒
    tick-duration: 1000             # 时间轮槽位间隔（毫秒），默认 1000
    wheel-size: 300                 # 时间轮槽数量，默认 300
    max-ticks-per-poll: 1000        # 单次轮询最大推进槽数，默认 1000
    embedded-trigger: true          # 是否装配内置本地线程触发器，默认 true
    schedule-execute-error-handle: com.tbs.schedule.impl.SimpleLogErrorHandle   # 执行失败处理器
```

配置项与属性类对照：

| 前缀 | 属性类 | 关键属性（默认值） |
|------|--------|---------------------|
| `tbs.async.executor` | `ExecutorProperty` | 见上表，另含 `mainExecutorServiceName`（mainExecutorService） |
| `tbs.broadcast` | `BroadcastProperty` | `eventBusType`（SimpleEventBus）、`eventDispatcherType`（SimpleEventDispatcher） |
| `tbs.lock` | `LockProperty` | `lockProviderType`（JdkLockProvider）、`accquireTimeout`（1000） |
| `tbs.schedule` | `ScheduleProperty` | 见上表 |

模块自带国际化资源 `messages-schedule.properties`（中文）/ `messages-schedule_en_US.properties`（英文），key 集中维护于 `ScheduleI18nConstant`（提交校验、执行器类型、持久化转换失败等提示）。

## 扩展开发

### 自定义任务类型（调度）

新增一种可持久化任务只需两步，无需改动内核：

```java
// 1. 定义可序列化载荷
public class OrderTimeoutPayload implements TaskPayload {
    private String orderId;      // 须可 JSON 序列化
    // getter/setter ...
}

// 2. 注册对应执行器 Bean
@Component
public class OrderTimeoutTaskExecutor implements TaskExecutor {
    @Override
    public boolean supports(String taskType) {
        return "order-timeout".equals(taskType);
    }

    @Override
    public void execute(TaskSubmission submission) {
        OrderTimeoutPayload payload = (OrderTimeoutPayload) submission.getPayload();
        // 业务处理
    }
}
```

### 自定义生命周期监听（调度）

实现 `LifecycleListener` 并注册为 Bean（所有方法默认空实现，按需覆写），用于埋点、审计与告警；监听器异常被内核隔离。

### 自定义订阅者错误处理（广播）

为 `SimpleEventDispatcher` 设置 `IHandleEventError`，或在分发器实现类中自定义 `handleError` 行为。

### 自定义锁实现

实现 `ILockProvider` 并指定 `tbs.lock.lock-provider-type`，或在容器中注册同类型 Bean 覆盖 `LockConfig` 默认装配；建议基于 `LockAdapter` 适配底层锁对象。

### 替换线程池管理器

注册自定义 `IThreadPoolManager` Bean 即可覆盖 `@ConditionalOnMissingBean` 的默认 `DefaultMultiLayerThreadPoolManager`；如需传播自定义上下文，提供 `ContextPropagator` 实现即可被自动收集。

## 包结构一览

```
com.tbs
├── async                 # 长期异步任务
│   ├── annotation        # @EnableTbsAsyncPack / @LongTermAsync
│   ├── aop               # LongTermAsyncAop 切面
│   ├── config            # AsyncConfig / ContextPropagatorAsyncConfig(@Async 执行器接入传播链) /
│   │                     #   ExecutorProperty(tbs.async.executor) / AsyncAutoRegister
│   ├── constant          # AsyncBeanNameConstant
│   └── delegate          # AsyncDelegateInvoker（桥接 common 代理模式与线程池）
├── thread                # 线程池管理
│   ├── (root)            # IThreadPoolManager / IThreadRequest / IOnThreadError / AbstractThreadPoolManager
│   └── impl              # DefaultMultiLayerThreadPoolManager / SimpleThreadRequest /
│                         #   LongTermThreadExecutor / DefaultOnThreadError / DefaultThreadCall
├── broadcast             # 事件广播
│   ├── bus               # IEventBus / AbstractEventBus / AbstractSemaphoreEventBus / SimpleEventBus
│   ├── config            # BroadcastConfig / BroadcastProperty(tbs.broadcast)
│   ├── constant          # 广播常量
│   ├── dispatcher        # IEventDispatcher / AbstractEventDispatcher / SimpleEventDispatcher
│   ├── enums             # HandleMode / EventStatusEnum
│   ├── event             # IEvent / SimpleEvent
│   ├── publisher         # IPublisher / AbstractPublisher / IPublishResult / IEventStatusListenner 等
│   ├── subscriber        # AbstractSubscriber
│   └── support           # IDistributedBusSupport / IDistributedDispatcherSupport / AbstractDistributedSupport
├── schedule              # 任务调度
│   ├── (root)            # SchedulerCore（实现 TaskScheduler + Tickable）
│   ├── api               # 对外 SPI：TaskScheduler / TaskSubmission / ScheduleHandle / TaskPayload /
│   │                     #   ScheduleStrategy / TaskDispatcher / TaskExecutor / TaskStore /
│   │                     #   ScheduleTrigger / Tickable / ExecutionPolicy / TypedExecutionPolicy /
│   │                     #   LifecycleListener / TaskExecuteErrorHandle
│   ├── config            # @EnableTbsSchedule / ScheduleAutoRegister / ScheduleConfig / ScheduleProperty(tbs.schedule)
│   ├── constant          # ScheduleI18nConstant
│   ├── dispatcher        # LocalTaskDispatcher
│   ├── executor          # LocalRunnableTaskExecutor / ExecutionPolicyResolver
│   ├── i18n              # 调度国际化支持
│   └── impl              # TimingWheelStrategy / DelayQueueStrategy / LocalThreadScheduleTrigger /
│                         #   LocalRunnablePayload / TaskSubmissionTransformer / SimpleLogErrorHandle / NoopTaskStore
└── lock                  # 锁
    ├── (root)            # ILock / IConditionLock / IReadWriteLock / ILockProvider
    ├── config            # LockConfig / LockProperty(tbs.lock)
    ├── impls             # LockAdapter / ReadWriteLockAdapter
    ├── impls.providers   # JdkLockProvider
    └── util              # LockUtil
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
