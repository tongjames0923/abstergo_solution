# Async 异步功能模块

异步功能模块提供了完整的异步编程解决方案，包括线程管理、定时任务、分布式锁、事务管理、事件广播等核心功能。

## 目录结构

```
com.tbs.async
├── annotation                  # 注解类
│   └── LongTermAsync.java     # 长期异步执行注解
├── aop                         # AOP切面
│   └── LongTermAsyncAop.java  # 长期异步执行切面
├── config                      # 配置类
│   ├── property               # 配置属性
│   │   └── ExecutorProperty.java
│   └── AsyncConfig.java       # 异步配置
└── constant                    # 常量定义
    └── AsyncBeanNameConstant.java

com.tbs.broadcast               # 事件广播模块
├── bus                         # 事件总线
│   ├── impl                   # 实现类
│   │   └── SimpleEventBus.java
│   ├── AbstractEventBus.java
│   ├── AbstractSemaphoreEventBus.java
│   ├── IDistributedBusSupport.java
│   └── IEventBus.java
├── config                      # 配置类
│   ├── property              # 配置属性
│   │   └── BroadcastProperty.java
│   └── BroadcastConfig.java   # 广播配置
├── constant                    # 常量定义
│   └── BroadcastBeanNameConstant.java
├── dispatcher                  # 事件分发器
│   ├── impl                   # 实现类
│   │   └── SimpleEventDispatcher.java
│   ├── AbstractEventDispatcher.java
│   ├── IDistributedDispatcherSupport.java
│   └── IEventDispatcher.java
├── enums                       # 枚举类
│   ├── EventStatusEnum.java   # 事件状态枚举
│   └── HandleMode.java        # 处理模式枚举
├── event                       # 事件定义
│   ├── impl                   # 实现类
│   │   └── SimpleEvent.java
│   └── IEvent.java            # 事件接口
├── publisher                   # 事件发布者
│   ├── impl                   # 实现类
│   │   ├── DispatcherImportedPublisher.java
│   │   ├── SimpleDistributedAcceptedPublishResult.java
│   │   └── SpringManagedListenablePublishResult.java
│   ├── AbstractPublishResult.java
│   ├── AbstractPublisher.java
│   ├── IEventStatusListenner.java
│   ├── IPublishResult.java
│   └── IPublisher.java
├── subscriber                  # 事件订阅者
│   ├── impl                   # 实现类
│   │   └── SimpleUseSubscriber.java
│   ├── AbstractSubscriber.java
│   └── IReceiveHandler.java
└── support                     # 支持类
    └── AbstractDistributedSupport.java

com.tbs.lock                    # 分布式锁模块
├── config                      # 配置类
│   ├── LockConfig.java        # 锁配置
│   └── LockProperty.java      # 锁属性配置
├── impls                       # 锁实现
│   ├── providers              # 锁提供者
│   │   └── JdkLockProvider.java
│   ├── LockAdapter.java       # 锁适配器
│   └── ReadWriteLockAdapter.java
├── util                        # 工具类
│   └── LockUtil.java          # 锁工具类
├── IConditionLock.java        # 条件锁接口
├── ILock.java                 # 锁接口
├── ILockProvider.java         # 锁提供者接口
└── IReadWriteLock.java        # 读写锁接口

com.tbs.schedule                # 定时任务模块
├── config                      # 配置类
│   ├── ScheduleConfig.java    # 定时任务配置
│   └── ScheduleProperty.java  # 定时任务属性
├── impl                        # 实现类
│   ├── DelayQueueSchedule.java
│   ├── DelayedTaskWrapper.java
│   ├── SimpleLogErrorHandle.java
│   ├── SimpleScheduleTask.java
│   └── TimingWheelSchedule.java
├── AbstractAsyncBackgroundSchedule.java
├── AbstractDelayQueueAsyncBackgroundSchedule.java
├── AbstractTimingWheelAsyncBackgroundSchedule.java
├── ISchedule.java             # 定时任务接口
├── IScheduleExecuteErrorHandle.java
└── IScheduleTask.java         # 定时任务实体接口

com.tbs.thread                  # 线程管理模块
├── impl                        # 实现类
│   ├── DefaultMultiLayerThreadPoolManager.java
│   ├── DefaultOnThreadError.java
│   ├── DefaultThreadCall.java
│   ├── LongTermThreadExecutor.java
│   └── SimpleThreadRequest.java
├── AbstractThreadPoolMananger.java
├── IOnThreadError.java
├── IThreadPoolManager.java
└── IThreadRequest.java

com.tbs.transaction             # 事务管理模块
├── exception                   # 异常类
│   └── NoRollbackTransactionException.java
├── scalable                    # 可扩展事务
│   ├── conflict               # 冲突解决
│   │   └── UniqueTransactionConflictSolver.java
│   ├── impl                   # 实现类
│   │   ├── DataHolderTransactionStore.java
│   │   ├── ImportedScalableTransaction.java
│   │   └── SpringManagedScalableTransaction.java
│   ├── info                   # 事务信息
│   │   ├── impl               # 实现类
│   │   │   ├── DefaultRollbackableData.java
│   │   │   ├── DefaultRollbackableTransactionInfo.java
│   │   │   └── DefaultTransactionCreator.java
│   │   ├── AbstractRollbackableTransactionInfo.java
│   │   └── IRollbackableData.java
│   ├── AbstractScalableTransaction.java
│   ├── ITransactionConflictSolver.java
│   ├── ITransactionCreator.java
│   ├── ITransactionEdgeController.java
│   ├── ITransactionStatusChangeLisenner.java
│   └── ITransactionStore.java
├── AbstractTransaction.java
├── ITransaction.java          # 事务接口
├── ITransactionInfo.java      # 事务信息接口
└── TransactionStatus.java     # 事务状态枚举
```

## 核心组件说明

### 1. 异步注解和AOP

#### @LongTermAsync 注解

用于标记需要长期异步执行的方法，支持以下属性：

- `value()`: 线程名称，默认为空
- `onThreadError()`: 线程错误处理类，默认为`IOnThreadError`的实现

#### LongTermAsyncAop 切面

处理[@LongTermAsync](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/async/annotation/LongTermAsync.java#L11-L33)
注解的AOP切面，自动将标记的方法放入独立线程中执行。

### 2. 事件广播系统

#### 核心接口

- [IEvent](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/broadcast/event/IEvent.java#L11-L50):
  事件接口
- [IPublisher](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/broadcast/publisher/IPublisher.java#L15-L58):
  事件发布者接口
- [IEventDispatcher](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/broadcast/dispatcher/IEventDispatcher.java#L15-L51):
  事件分发器接口
- [IEventBus](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/broadcast/bus/IEventBus.java#L14-L58):
  事件总线接口

#### 使用示例

```java
// 发布事件
@Autowired
private IPublisher publisher;

public void publishEvent() {
    SimpleEvent event = new SimpleEvent();
    event.setEventName("user.login");
    event.setData("user data");
    publisher.publish(event);
}

// 订阅事件
@Component
public class UserLoginSubscriber extends SimpleUseSubscriber {

    public UserLoginSubscriber() {
        super((event, publisher) -> {
                // 处理事件
                System.out.println("处理用户登录事件: " + event.getData());
                return RECEIVE_SUCCESS;
            }, Collections.singleton("user.login"),  // 支持的事件名称
            null,  // 支持的事件ID
            false, // 是否支持任意事件名称
            false, // 是否支持任意事件ID
            "userLoginSubscriber" // 订阅者ID
        );
    }
}
```

### 3. 分布式锁

#### 核心接口

- [ILock](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/lock/ILock.java#L10-L48):
  锁接口
- [ILockProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/lock/ILockProvider.java#L8-L23):
  锁提供者接口
- [IReadWriteLock](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/lock/IReadWriteLock.java#L7-L21):
  读写锁接口

#### 使用示例

```java

@Autowired
private LockUtil lockUtil;

public void doSomething() {
    lockUtil.lockThen("myLock", () -> {
        // 执行需要加锁的操作
        System.out.println("执行需要同步的操作");
    });
}
```

### 4. 定时任务

#### 核心接口

- [ISchedule](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/schedule/ISchedule.java#L11-L34):
  定时任务接口
- [IScheduleTask](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/schedule/IScheduleTask.java#L9-L45):
  定时任务实体接口

#### 使用示例

```java

@Autowired
private ISchedule schedule;

public void scheduleTask() {
    // 延迟5秒执行任务
    schedule.pin(() -> {
        System.out.println("定时任务执行");
    }, Duration.ofSeconds(5));
}
```

### 5. 线程管理

#### 核心接口

- [IThreadPoolManager](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/thread/IThreadPoolManager.java#L8-L17):
  线程池管理器接口
- [IThreadRequest](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/thread/IThreadRequest.java#L9-L30):
  线程请求接口

#### 使用示例

```java

@Autowired
private IThreadPoolManager threadPoolManager;

public void submitTask() {
    IThreadRequest request = new SimpleThreadRequest(() -> {
        System.out.println("在线程池中执行任务");
    });

    threadPoolManager.submit(request, (thread, throwable) -> {
        System.err.println("线程执行出错: " + throwable.getMessage());
    });
}
```

### 6. 事务管理

#### 核心接口

- [ITransaction](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/transaction/ITransaction.java#L17-L87):
  事务接口
- [ITransactionInfo](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/transaction/ITransactionInfo.java#L12-L69):
  事务信息接口

#### 使用示例

```java

@Autowired
private ITransaction transaction;

public void doTransaction() {
    String transactionId = "txn-" + System.currentTimeMillis();

    // 开始事务
    transaction.begin(transactionId);

    try {
        // 执行业务操作
        doBusinessOperation();

        // 提交事务
        transaction.commit(transactionId);
    } catch (Exception e) {
        // 回滚事务
        transaction.rollback(transactionId);
        throw e;
    }
}
```

## 配置说明

### application.yml配置示例

```yaml
tbs:
  async:
    executor:
      core-pool-size: 10
      max-pool-size: 20
      queue-capacity: 100
  broadcast:
    event-dispatcher: com.tbs.broadcast.dispatcher.impl.SimpleEventDispatcher
    event-bus: com.tbs.broadcast.bus.impl.SimpleEventBus
  lock:
    lock-provider-type: com.tbs.lock.impls.providers.JdkLockProvider
  schedule:
    schedule-execute-error-handle: com.tbs.schedule.impl.SimpleLogErrorHandle
```

## 最佳实践

1. **合理使用异步注解**
   ：对于耗时较长的操作，使用[@LongTermAsync](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/async/annotation/LongTermAsync.java#L11-L33)
   注解避免阻塞主线程
2. **正确管理锁资源**
   ：使用[LockUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/lock/util/LockUtil.java#L15-L108)
   工具类确保锁的正确获取和释放
3. **事件驱动架构**：使用事件广播系统解耦组件间的依赖关系
4. **定时任务管理**
   ：使用[ISchedule](file:///Users/abstergo/IdeaProjects/abstergo_solution/async/src/main/java/com/tbs/schedule/ISchedule.java#L11-L34)
   接口统一管理定时任务
5. **事务边界控制**：合理划分事务边界，避免长时间持有锁

## 注意事项

1. **异常处理**：异步操作中的异常需要特别处理，避免丢失
2. **资源释放**：注意线程、锁等资源的正确释放
3. **线程安全**：在多线程环境中确保共享资源的线程安全
4. **性能考虑**：合理配置线程池参数，避免资源浪费
5. **事务一致性**：分布式环境下需要考虑事务的一致性问题
