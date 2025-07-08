# 事件广播系统 (Event Broadcast System)

## 项目概述

基于发布-订阅模式构建的企业级事件驱动架构解决方案，提供以下核心特性：

- 支持优先级的事件队列处理（优先级数值越小优先级越高）
- 同步/异步双模式事件处理
- 完整的事件状态生命周期管理（7种状态+4种终态）
- 可扩展的拦截器机制
- 线程安全的事件分发机制
- 支持Spring自动装配集成

## 架构设计

### 模块划分

com.tbs.boardcast
├── bus # 事件队列核心实现
├── dispatcher # 事件分发机制
├── event # 事件模型定义
├── publisher # 事件发布机制
├── subscriber # 事件订阅处理
└── config # Spring 集成配置

### 核心组件关系

[Event Publisher] → [Event Bus] → [Event Queue] → [Event Dispatcher] → [Subscriber]

### 模块说明

#### 1. 事件总线模块 (bus)

- **IEventBus**: 事件队列核心接口
    - [inqueueEvent()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\bus\IEventBus.java#L18-L18):
      事件入队
    - [nextUnhandledEvent()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\bus\IEventBus.java#L26-L26):
      获取待处理事件
    - [getEventQueueSize()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\bus\IEventBus.java#L33-L33):
      获取队列大小
- **SimpleEventBus**: 默认实现类
    - 使用PriorityQueue实现优先级队列
    - 基于LockUtil实现线程安全
    - 支持事件超时等待机制

#### 2. 事件分发模块 (dispatcher)

- **IEventDispatcher**: 分发核心接口
    - `register/unregister`: 订阅者管理
    - `handleEvent/stopHandle`: 事件处理控制
- **AbstractEventDispatcher**: 抽象实现
    - 支持同步/异步两种处理模式
    - 维护订阅者注册表
    - 提供异常处理回调接口

#### 3. 事件模型模块 (event)

- **IEvent**: 事件基础接口
    - 定义事件ID、名称、数据等基础属性
    - 支持优先级设置
- **EventStatusEnum**: 状态枚举

```
    IDLE(0) → IN_QUEUE(1) → IN_PROCESS(2)
                    ↘ FAILED(4) ↗
                        ↘ DONE(3) ↗
                            ↘ CANCELED(5) ↗
                                ↘ REQUIRE_RETRY(7)
```

- **HandleMode**: 处理模式
    - SYNC: 同步处理
    - ASYNC: 异步处理

#### 4. 发布模块 (publisher)

- **IPublisher**: 发布接口
    - [publish()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\publisher\IPublisher.java#L25-L25):
      事件发布方法
    - 提供同步/异步快捷方法
- **AbstractPublisher**: 抽象实现
    - 管理发布者ID
    - 绑定事件总线
- **IPublishResult**: 发布结果跟踪
    - 支持状态监听
    - 提供CompletableFuture式阻塞等待

#### 5. 订阅模块 (subscriber)

- **AbstractSubscriber**: 抽象订阅者
    - 提供同步/异步接收接口
    - 需要实现以下抽象方法：
        - [getSubscriberId()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\subscriber\AbstractSubscriber.java#L27-L27):
          订阅者唯一标识
        - [supportEvent()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\subscriber\AbstractSubscriber.java#L35-L35):
          事件匹配判断
        - [supportPublisher()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\subscriber\AbstractSubscriber.java#L43-L43):
          发布者匹配判断
        - [onReceive()](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\boardcast\subscriber\AbstractSubscriber.java#L51-L51):
          事件处理逻辑

#### 6. 配置模块 (config)

- **BoardcastConfig**: Spring配置类
    - 自动装配事件总线和分发器
    - 提供默认线程池实现
- **BoardcastProperty**: 配置属性类
    - 支持自定义事件总线类型
    - 支持自定义分发器类型

## 核心流程说明

1. **事件发布阶段**
    - 创建事件对象（SimpleEventImpl）
    - 通过Publisher发布事件
    - 事件进入优先级队列

2. **事件处理阶段**
    - 分发器轮询获取事件
    - 根据事件类型和发布者匹配订阅者
    - 调用订阅者的接收方法
    - 更新事件状态

3. **状态生命周期管理**
    - 初始状态：IDLE
    - 入队后：IN_QUEUE
    - 开始处理：IN_PROCESS
    - 处理完成：DONE/FAILED/CANCELED/REQUIRE_RETRY

## 关键实现细节

1. **线程安全机制**
    - 使用LockUtil实现队列操作的原子性
    - Condition实现事件等待/通知机制
    - 线程池隔离不同订阅者的执行上下文

2. **状态机管理**
    - 所有状态转换通过setEventStatus()统一处理
    - 终态检测防止非法状态转换
    - CompletableFuture支持同步等待

3. **扩展性设计**
    - 发布者/订阅者可扩展
    - 支持自定义线程池
    - 通过Spring Bean实现监听器扩展

## 使用示例

```java
// 创建事件发布者
IPublisher publisher = new DispatcherImportedPublisher("myPublisher", eventDispatcher);

// 创建事件
IEvent<String> event = new SimpleEventImpl<>("event1", "testEvent");
event.

setEventData("Hello World");

// 发布事件（同步模式）
IPublishResult<?> result = publisher.publishSync(event);

// 订阅者实现示例
public class MySubscriber extends AbstractSubscriber {
    public MySubscriber(ExecutorService executor) {
        super(executor);
    }

    @Override
    public String getSubscriberId() {
        return "mySubscriber";
    }

    @Override
    public boolean supportEvent(String eventName) {
        return "testEvent".equals(eventName);
    }

    @Override
    public boolean supportPublisher(IPublisher publisher) {
        return true;
    }

    @Override
    protected void onReceive(IEvent<?> event, IPublisher publisher) {
        System.out.println("Received event: " + event.getEventData());
    }
}

```