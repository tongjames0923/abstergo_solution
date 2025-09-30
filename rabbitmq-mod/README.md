# RabbitMQ 模块

RabbitMQ 模块提供了基于 RabbitMQ 的消息广播和分布式事件处理功能，实现了应用间的异步通信。

## 功能特点

1. **自动创建组件**：通过Spring Bean自动创建交换机、队列和绑定关系
2. **代码创建组件**：提供通过代码动态创建RabbitMQ组件的能力
3. **高可用性**：支持队列的高可用策略和消息TTL设置
4. **灵活配置**：支持通过配置文件或代码方式配置RabbitMQ组件

## 目录结构

```
com.tbs.broadcast.rabbitmq
├── config                          # RabbitMQ 配置类
│   ├── RabbitMqConfig.java        # RabbitMQ 核心配置
│   ├── RabbitMqInitializer.java   # RabbitMQ组件初始化器
│   └── property                   # 配置属性类
│       └── RabbitMqProperty.java
└── support                         # RabbitMQ 支持类
    ├── AbstractRabbaitMqSupport.java
    └── SimpleRabbitMqSupport.java
```

## 核心组件说明

### 1. RabbitMQ 配置类 (RabbitMqConfig.java)

[RabbitMqConfig](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/config/RabbitMqConfig.java#L13-L132) 是 RabbitMQ 的核心配置类，负责创建连接工厂、RabbitTemplate、消息转换器以及定义交换机、队列和绑定关系。

#### 主要 Bean 说明

1. `connectionFactory()`: 创建 RabbitMQ 连接工厂
2. `rabbitTemplate()`: 创建用于发送消息的 RabbitTemplate
3. `amqpAdmin()`: 创建 RabbitMQ 管理器
4. `jsonMessageConverter()`: 创建 JSON 消息转换器
5. `broadcastExchange()`: 创建广播交换机
6. `broadcastQueue()`: 创建广播队列
7. `broadcastBinding()`: 绑定队列到交换机

### 2. RabbitMQ 初始化器 (RabbitMqInitializer.java)

[RabbitMqInitializer](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/config/RabbitMqInitializer.java#L15-L90) 类负责在应用程序启动时通过代码直接创建交换机、队列和绑定关系。

#### 主要功能

1. `initialize()`: 应用程序启动时初始化RabbitMQ组件
2. `createExchange()`: 动态创建交换机
3. `createQueue()`: 动态创建队列
4. `createBinding()`: 动态创建绑定关系

### 3. RabbitMQ 属性配置类 (RabbitMqProperty.java)

[RabbitMqProperty](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/config/property/RabbitMqProperty.java#L10-L35) 类定义了 RabbitMQ 的配置属性，可通过 `application.yml` 进行配置。

#### 配置属性说明

| 属性名 | 默认值 | 说明 |
|--------|--------|------|
| exchangeName | "tbs.broadcast.exchange" | 交换机名称 |
| queueName | "tbs.broadcast.queue" | 队列名称 |
| routingKey | "tbs.broadcast.routing.key" | 路由键 |
| nodeId | null | 节点 ID |
| publisherConfirm | true | 是否启用发布确认 |

### 3. RabbitMQ 支持类

#### AbstractRabbaitMqSupport.java

[AbstractRabbaitMqSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/support/AbstractRabbaitMqSupport.java#L19-L85) 是 RabbitMQ 支持的抽象基类，实现了消息监听和广播功能。

主要功能：
- 消息监听和处理
- 消息广播发送
- 手动 ACK 确认机制

#### SimpleRabbitMqSupport.java

[SimpleRabbitMqSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/support/SimpleRabbitMqSupport.java#L11-L20) 是 [AbstractRabbaitMqSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/support/AbstractRabbaitMqSupport.java#L19-L85) 的具体实现，处理 [SimpleDistributedAcceptedPublishResult](file:///Users/abstergo/IdeaProjects/abstergo_solution/broadcast/src/main/java/com/tbs/broadcast/publisher/impl/SimpleDistributedAcceptedPublishResult.java#L15-L69) 类型的消息。

## 自定义使用说明

### 1. 配置说明

在 `application.yml` 中添加以下配置：

```yaml
# RabbitMQ 连接配置
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# TBS RabbitMQ 配置
tbs:
  rabbitmq:
    # 交换机名称
    exchange-name: tbs.broadcast.exchange
    # 队列名称
    queue-name: tbs.broadcast.queue
    # 路由键
    routing-key: tbs.broadcast.routing.key
    # 节点ID
    node-id: node1
    # 是否启用发布确认
    publisher-confirm: true
```

### 2. 通过代码创建RabbitMQ组件

除了通过配置自动创建RabbitMQ组件外，还可以通过代码动态创建：

```java
@Component
public class CustomRabbitMqInitializer {
    
    @Autowired
    private AmqpAdmin amqpAdmin;
    
    public void createCustomQueue() {
        // 创建队列
        Queue queue = QueueBuilder.durable("custom.queue")
            .withArgument("x-message-ttl", 30000)
            .build();
        amqpAdmin.declareQueue(queue);
        
        // 创建交换机
        DirectExchange exchange = new DirectExchange("custom.exchange");
        amqpAdmin.declareExchange(exchange);
        
        // 创建绑定
        Binding binding = BindingBuilder.bind(queue)
            .to(exchange)
            .with("custom.routing.key");
        amqpAdmin.declareBinding(binding);
    }
}
```

### 3. 使用 RabbitMQ 广播功能

要使用 RabbitMQ 的广播功能，需要：

1. 创建自定义的 RabbitMQ 支持类：

```java
@Component
public class CustomRabbitMqSupport extends AbstractRabbaitMqSupport<YourEventType> {
    
    @Override
    public YourEventType bridge(@NotNull IPublishResult obj) {
        // 实现事件对象的桥接转换
        return (YourEventType) obj.getEvent();
    }

    @Override
    public IPublishResult unbridge(@NotNull YourEventType obj) {
        // 实现事件对象的反向桥接转换
        return new SpringManagedListenablePublishResult(obj, null, null);
    }
}
```

2. 发送消息：

```java
@Resource
private RabbitTemplate rabbitTemplate;

public void sendMessage(String message) {
    rabbitTemplate.convertAndSend("tbs.broadcast.exchange", "tbs.broadcast.routing.key", message);
}
```

### 4. 监听消息

通过继承 [AbstractRabbaitMqSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/support/AbstractRabbaitMqSupport.java#L19-L85) 类并实现相关方法来监听消息：

```java
@Component
public class MessageListener extends AbstractRabbaitMqSupport<String> {
    
    @Override
    public void receiveEvent(String message) {
        // 处理接收到的消息
        System.out.println("Received message: " + message);
    }
    
    // 实现其他抽象方法...
}
```

## 最佳实践

1. **消息确认机制**：
   - 启用手动 ACK 确认确保消息被正确处理
   - 合理处理消息处理失败的情况，避免消息丢失

2. **连接管理**：
   - 合理配置连接池参数
   - 及时关闭不再使用的连接

3. **错误处理**：
   - 实现完善的消息处理异常处理机制
   - 对于处理失败的消息，可以选择重新入队或记录日志

4. **性能优化**：
   - 合理设置预取数量以提高消费效率
   - 使用批量发送提高生产效率

## 注意事项

1. **依赖管理**：
   - 确保项目中包含了 RabbitMQ 相关依赖
   - 注意 RabbitMQ 客户端版本与服务器版本的兼容性

2. **配置正确性**：
   - 确保 RabbitMQ 服务器地址、端口、用户名和密码配置正确
   - 确保交换机、队列和路由键配置正确

3. **消息持久化**：
   - 对于重要消息，应设置持久化选项
   - 合理配置队列和交换机的持久化属性

4. **监控和日志**：
   - 实现消息发送和接收的监控
   - 记录详细日志以便问题排查