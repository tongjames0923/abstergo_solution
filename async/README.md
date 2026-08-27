# Async 模块 - 异步功能解决方案

## 模块概述

Async模块是一个功能强大的Java异步编程解决方案，基于Spring Boot框架构建，提供了完整的异步处理、事件驱动、任务调度和分布式锁等功能。该模块旨在简化异步编程的复杂性，提高系统的并发处理能力和响应性能。

## 核心功能模块

### 1. 异步执行 (Async)

提供基于AOP的异步方法执行能力，支持长期异步任务的执行和异常处理。

**核心组件：**
- `@LongTermAsync` - 异步执行注解
- `LongTermAsyncAop` - 异步切面处理
- `AsyncDelegateInvoker` - 异步委托调用器

**使用示例：**
```java
@Service
public class UserService {
    
    @LongTermAsync("user-process-thread")
    public void processUserData(User user) {
        // 异步执行的业务逻辑
        // 该方法会在单独的线程中执行
    }
}
```

### 2. 事件广播 (Broadcast)

完整的事件驱动架构，支持事件的发布、订阅、分发和处理，提供同步和异步两种处理模式。

**核心组件：**
- `IEventBus` - 事件总线接口
- `IPublisher` - 事件发布者
- `IEventDispatcher` - 事件分发器
- `AbstractSubscriber` - 事件订阅者基类

**功能特性：**
- 支持分布式事件处理
- 事件队列管理和优先级排序
- 同步/异步事件处理模式
- 事件状态监听和回调

**使用示例：**
```java
// 发布事件
@Autowired
private IPublisher eventPublisher;

public void publishUserEvent(User user) {
    IEvent event = new SimpleEvent("user.created", user);
    // 异步发布
    IPublishResult result = eventPublisher.pushlishAsync(event);
    // 同步发布
    // IPublishResult result = eventPublisher.pushlishSync(event);
}

// 订阅事件
@Component
public class UserEventHandler extends AbstractSubscriber {
    
    @Override
    public void onEvent(IEvent event) {
        if ("user.created".equals(event.getEventType())) {
            User user = (User) event.getPayload();
            // 处理用户创建事件
        }
    }
}
```

### 3. 任务调度 (Schedule)

轻量级延迟/一次性定时触发调度框架，定位为单进程内"延迟 N 毫秒后执行一次"与"指定时刻触发一次"的轻量能力，
与 xxl-job 分布式调度形成职责互补（重分布式调度不在此重复实现）。采用"管道 + SPI"架构：

`提交 → 时序引擎(ScheduleStrategy) → 到期提升 → 派发(TaskDispatcher) → 执行(TaskExecutor + ExecutionPolicy) → 生命周期监听(LifecycleListener)`

**核心组件（api 契约层）：**
- `TaskScheduler` - 调度门面（唯一稳定入口，`submit` / `schedule` 便捷方法）
- `TaskSubmission` - 纯数据提交描述（taskId / taskType / payload / triggerAt / 策略覆盖）
- `ScheduleHandle` - 取消句柄（仅 `cancel()`）
- `ScheduleStrategy` - 时序引擎 SPI（时间轮 `TimingWheelStrategy` / 延迟队列 `DelayQueueStrategy`）
- `TaskDispatcher` - 派发 SPI（`LocalTaskDispatcher` 默认本地线程池；预留 `BroadcastTaskDispatcher`）
- `TaskExecutor` - 执行 SPI（按 taskType 匹配，`LocalRunnableTaskExecutor` 执行本地 Runnable）
- `ExecutionPolicy` - 执行策略（超时/并发隔离/失败处理/优先级，三级覆盖）
- `LifecycleListener` - 生命周期监听 SPI
- `TaskStore` - 任务持久化 SPI（默认 `NoopTaskStore` 内存）

**包结构（按职责子域分包，遵循开发规范铁律 12）：**
```
com.tbs.schedule/
├── SchedulerCore.java        # 调度内核：时序引擎驱动、到期取出与派发、优雅停机；暴露无状态 tick()
├── api/                       # 对外契约：门面、任务模型与全部 SPI 接口
│   ├── TaskScheduler / TaskSubmission / ScheduleHandle / TaskPayload
│   ├── ScheduleStrategy / TaskDispatcher / TaskExecutor
│   ├── TaskStore / LifecycleListener / TaskExecuteErrorHandle / ExecutionPolicy / TypedExecutionPolicy
│   ├── ScheduleTrigger        # 触发器 SPI（start/stop/wakeup，驱动契约）
│   └── Tickable               # 可被单次驱动的内核视图（tick）
├── dispatcher/                # 派发实现：LocalTaskDispatcher（本地线程池）
├── executor/                  # 执行实现：LocalRunnableTaskExecutor / DefaultExecutionPolicy / ExecutionPolicyResolver
├── impl/                      # 时序策略与兜底实现：TimingWheelStrategy / DelayQueueStrategy / SimpleLogErrorHandle / NoopTaskStore / LocalRunnablePayload
│   ├── LocalThreadScheduleTrigger  # 本地线程常驻轮询触发器（实现 IStartup，@LongTermAsync 自启循环）
│   └── NoopScheduleTrigger        # 空触发器（外驱场景便利类，按需引用，不进入默认装配）
├── config/                    # 自动装配：EnableTbsSchedule / ScheduleAutoRegister / ScheduleConfig / ScheduleProperty
└── i18n/                      # 模块化国际化：ScheduleMessageSourceProvider
```

**功能特性：**
- **一次性触发语义**：任务到点执行一次即终结，框架无重复定时概念
- 任务模型与生命周期状态分离（`TaskSubmission` 纯数据，状态收口内核 `ScheduledEntry`）
- 执行外移：内核只做"按时派发"，执行落线程池，避免慢任务拖累整批
- 任务类型多样：新增 Payload + `TaskExecutor` 即扩展，无需改内核
- 触发方式可扩展：手动/事件/外部 API 均通过 `submit` 入口提交（客户端，不进内核）
- 多种时序策略可插拔
- **触发机制解耦**：内核仅暴露无状态 `tick()`，由 `ScheduleTrigger` SPI 决定驱动方式；
  `tbs.schedule.embedded-trigger=true`（默认）由内置本地线程常驻轮询（信号量惰性唤醒）驱动；
  设为 `false` 时不装配内置触发器，由使用方注册自定义 `ScheduleTrigger` Bean 驱动内核 `tick()`（例如接入任意外部调度器时，在该调度器模块中提供调用 `SchedulerCore.tick()` 的触发器实现），async 模块对具体调度器零假设

**使用示例：**
```java
@Autowired
private TaskScheduler scheduler;

public void scheduleTask() {
    // 延迟 5 秒执行本地任务
    ScheduleHandle handle = scheduler.schedule(() -> {
        // 需要延迟执行的任务逻辑
        log.info("任务执行了");
    }, Duration.ofSeconds(5));

    // 指定时刻触发
    // scheduler.scheduleAt(() -> {...}, Instant.now().plusMillis(1000));

    // 取消任务
    // handle.cancel();
}
```

**启用方式：** 启动类标注 `@EnableTbsSchedule`，并在 `application.yml` 设置 `tbs.schedule.enable=true`。

### 4. 分布式锁 (Lock)

提供分布式环境下的锁机制，支持读写锁和条件锁。

**核心组件：**
- `ILock` - 锁接口
- `IReadWriteLock` - 读写锁接口
- `IConditionLock` - 条件锁接口
- `ILockProvider` - 锁提供者

**功能特性：**
- 支持超时获取锁
- 读写锁分离
- 条件等待机制
- 可扩展的锁提供者

**使用示例：**
```java
@Autowired
private ILockProvider lockProvider;

public void processWithLock() {
    ILock lock = lockProvider.getLock("resource-lock");
    
    try {
        if (lock.tryLock(Duration.ofSeconds(5))) {
            // 获取锁成功，执行业务逻辑
            // ...
        }
    } finally {
        lock.unLock();
    }
}
```

### 5. 线程池管理 (Thread)

统一的线程池管理框架，支持多层线程池和线程错误处理。

**核心组件：**
- `IThreadPoolManager` - 线程池管理器
- `IThreadRequest` - 线程请求
- `IOnThreadError` - 线程错误处理
- `DefaultMultiLayerThreadPoolManager` - 默认多层线程池管理器

**功能特性：**
- 多层线程池管理
- 线程异常处理机制
- 线程请求封装
- 线程池资源管理

**使用示例：**
```java
@Autowired
private IThreadPoolManager threadPoolManager;

public void submitTask() {
    IThreadRequest request = new SimpleThreadRequest(() -> {
        // 需要在线程池中执行的任务
        System.out.println("线程任务执行");
    });
    
    threadPoolManager.submit(request, new DefaultOnThreadError());
}
```

## 配置说明

### Maven依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>async</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Spring Boot配置

模块提供了自动配置功能，只需在配置文件中添加相关配置即可：

```yaml
# 异步配置
async:
  executor:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 100

# 广播配置
broadcast:
  enabled: true
  mode: async

# 调度配置
tbs:
  schedule:
    enable: true                 # 总开关，默认 false
    strategy: com.tbs.schedule.impl.TimingWheelStrategy   # 时序策略，默认时间轮
    poll-time: 5
    poll-time-unit: SECONDS
    tick-duration: 1000          # 时间轮槽间隔（毫秒）
    wheel-size: 300
    max-ticks-per-poll: 1000

# 锁配置
lock:
  provider: jdk
  timeout: 30s
```

## 架构设计

### 模块分层

```
async/
├── async/          # 异步执行模块
├── broadcast/      # 事件广播模块
├── schedule/       # 任务调度模块
├── lock/           # 分布式锁模块
└── thread/         # 线程池管理模块
```

### 设计原则

1. **解耦性** - 各模块功能独立，可单独使用
2. **扩展性** - 接口设计支持多种实现
3. **易用性** - 提供注解和配置化使用方式
4. **可靠性** - 完善的异常处理和资源管理

## 使用场景

### 适合场景

- 需要异步处理的耗时操作
- 事件驱动的微服务架构
- 分布式环境下的资源竞争控制
- 定时任务和延迟任务管理
- 高并发场景下的线程资源管理

### 最佳实践

1. **异步处理** - 对于耗时操作，使用`@LongTermAsync`注解
2. **事件驱动** - 系统解耦时使用事件广播机制
3. **资源保护** - 并发访问共享资源时使用分布式锁
4. **任务调度** - 延迟任务使用调度器管理
5. **线程管理** - 统一使用线程池管理器提交任务

## 性能考虑

- 事件总线使用队列机制，避免阻塞
- 调度器支持多种实现，可根据场景选择
- 锁机制支持超时，避免死锁
- 线程池支持配置调优

## 扩展开发

模块提供了丰富的接口和抽象类，支持自定义扩展：

- 实现`IEventBus`接口创建自定义事件总线
- 继承`AbstractSubscriber`创建事件处理器
- 实现`TaskExecutor`接口 + 扩展`TaskPayload`支持自定义任务类型
- 实现`TaskDispatcher`接口（如 `BroadcastTaskDispatcher`）支持广播派发
- 实现`ExecutionPolicy`/`TypedExecutionPolicy`覆盖执行策略；实现`LifecycleListener`做观测
- 注册`TaskStore`实现（DB/Redis）获得任务可靠性；扩展`ILockProvider`支持更多锁实现

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
