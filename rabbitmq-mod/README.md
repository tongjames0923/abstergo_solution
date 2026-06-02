# RabbitMQ 模块 - 分布式消息广播解决方案

## 模块概述
- **自动配置**：基于 Spring Boot 自动配置 RabbitMQ 连接工厂、模板和管理器
- **组件自动声明**：应用启动时自动创建交换机、队列和绑定关系
- **配置驱动**：支持通过 YAML 配置文件灵活调整 RabbitMQ 参数

### 可靠性保障
- **消息确认机制**：支持发布者确认和手动消费者确认
- **消息持久化**：队列和交换机默认启用持久化，防止消息丢失
- **重试机制**：内置连接重试和消息处理失败重试机制
- **高可用性**：支持队列镜像和高可用策略

### 分布式支持
- **节点标识**：支持多节点环境下的消息路由和事件分发
- **事件桥接**：提供事件对象的序列化和反序列化桥接机制
- **统一接口**：与 Abstergo Solution 的事件分发系统无缝集成

### 性能优化
- **连接池管理**：优化的连接工厂配置，支持连接复用
- **消息转换器**：高效的 JSON 序列化消息转换器
- **预取控制**：支持消费者预取数量配置，平衡吞吐量和公平性

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加 RabbitMQ 模块依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>rabbitmq</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

在 `application.yml` 中配置 RabbitMQ 连接信息：

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: admin
    password: admin123
    virtual-host: /
    
    # 连接超时配置
    connection-timeout: 15000
    
    # 生产者配置
    publisher-confirm-type: correlated
    publisher-returns: true
    
    # 消费者配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        retry:
          enabled: true
          max-attempts: 3

tbs:
  rabbitmq:
    exchange-name: tbs.broadcast.exchange    # 交换机名称
    queue-name: tbs.broadcast.queue         # 队列名称
    routing-key: tbs.broadcast.routing.key  # 路由键
    node-id: node1                          # 节点标识
    publisher-confirm: true                  # 启用发布确认
```

### 3. 创建消息监听器

继承 `AbstractRabbaitMqSupport` 类创建自定义消息处理器：

```java
@Component
public class UserEventHandler extends AbstractRabbaitMqSupport<UserEvent> {
    
    @Override
    public void receiveEvent(UserEvent event) {
        // 处理接收到的用户事件
        log.info("接收到用户事件: {}", event);
        
        // 业务逻辑处理
        userService.processUserEvent(event);
    }
    
    @Override
    public UserEvent bridge(@NotNull IPublishResult obj) {
        // 将发布结果转换为具体的事件对象
        return (UserEvent) obj.getEvent();
    }
    
    @Override
    public IPublishResult unbridge(@NotNull UserEvent event) {
        // 将事件对象转换为发布结果
        return new SpringManagedListenablePublishResult(
            event, 
            event.getPublisher(), 
            event.getHandleMode()
        );
    }
}
```

### 4. 发送消息

使用注入的 `RabbitTemplate` 发送消息：

```java
@Service
public class MessageService {
    
    @Resource
    private RabbitTemplate rabbitTemplate;
    
    @Resource
    private RabbitMqProperty rabbitMqProperty;
    
    public void sendUserEvent(UserEvent event) {
        // 发送消息到指定交换机和路由键
        rabbitTemplate.convertAndSend(
            rabbitMqProperty.getExchangeName(),
            rabbitMqProperty.getRoutingKey(),
            event
        );
        
        log.info("用户事件已发送: {}", event);
    }
}
```

## 核心组件详解

### RabbitMqConfig - 核心配置类

RabbitMqConfig是模块的核心配置类，负责创建和管理RabbitMQ相关组件：

**主要Bean定义：**
- `connectionFactory()`: 创建带连接池的 RabbitMQ 连接工厂
- `rabbitTemplate()`: 配置消息发送模板，支持发布确认
- `amqpAdmin()`: 提供 RabbitMQ 管理功能
- `jsonMessageConverter()`: JSON 序列化消息转换器
- `broadcastExchange()`: 创建持久化直连交换机
- `broadcastQueue()`: 创建带 TTL 和高可用策略的队列
- `broadcastBinding()`: 绑定队列到交换机

### RabbitMqInitializer - 组件初始化器

RabbitMqInitializer在应用启动时主动创建RabbitMQ组件：

**初始化流程：**
1. 创建持久化直连交换机
2. 创建带消息 TTL（60秒）和高可用策略的队列
3. 建立队列到交换机的绑定关系
4. 记录初始化日志

### AbstractRabbaitMqSupport - 消息支持基类

AbstractRabbaitMqSupport是消息处理的核心抽象类：

**核心功能：**
- **消息监听**：自动创建消息监听容器，监听指定队列
- **手动 ACK**：实现手动消息确认机制，确保消息可靠处理
- **异常处理**：消息处理失败时自动拒绝并重新入队
- **事件广播**：提供统一的事件广播接口
- **队列管理**：自动确保队列存在，避免声明冲突

### RabbitMqProperty - 配置属性

RabbitMqProperty封装了RabbitMQ相关配置：

**配置属性：**
- `exchangeName`: 交换机名称（默认：tbs.broadcast.exchange）
- `queueName`: 队列名称（默认：tbs.broadcast.queue）
- `routingKey`: 路由键（默认：tbs.broadcast.routing.key）
- `nodeId`: 节点标识，用于分布式环境
- `publisherConfirm`: 是否启用发布确认（默认：true）

## 高级用法

### 自定义队列配置

```java
@Component
public class CustomQueueConfig {
    
    @Resource
    private AmqpAdmin amqpAdmin;
    
    @PostConstruct
    public void createCustomQueues() {
        // 创建带特殊属性的队列
        Queue customQueue = QueueBuilder.durable("custom.queue")
            .withArgument("x-message-ttl", 30000)           // 消息TTL 30秒
            .withArgument("x-max-length", 1000)             // 最大消息数量
            .withArgument("x-dead-letter-exchange", "dlx") // 死信交换机
            .withArgument("x-ha-policy", "all")            // 高可用策略
            .build();
        
        amqpAdmin.declareQueue(customQueue);
    }
}
```

### 批量消息处理

```java
@Component
public class BatchMessageProcessor extends AbstractRabbaitMqSupport<List<BatchEvent>> {
    
    @Override
    public void receiveEvent(List<BatchEvent> events) {
        // 批量处理消息
        events.forEach(event -> {
            try {
                processSingleEvent(event);
            } catch (Exception e) {
                log.error("处理事件失败: {}", event, e);
            }
        });
    }
    
    // 实现其他抽象方法...
}
```

### 消息优先级处理

```java
@Component
public class PriorityMessageHandler extends AbstractRabbaitMqSupport<PriorityEvent> {
    
    @Override
    public void receiveEvent(PriorityEvent event) {
        // 根据优先级处理消息
        switch (event.getPriority()) {
            case HIGH:
                processHighPriority(event);
                break;
            case NORMAL:
                processNormalPriority(event);
                break;
            case LOW:
                processLowPriority(event);
                break;
        }
    }
}
```

## 监控和调试

### 启用详细日志

在 `application.yml` 中启用 RabbitMQ 相关日志：

```yaml
logging:
  level:
    com.tbs.broadcast.rabbitmq: DEBUG
    org.springframework.amqp: INFO
    com.rabbitmq.client: WARN
```

### 健康检查

模块自动集成 Spring Boot Actuator 的健康检查：

```bash
# 检查 RabbitMQ 连接状态
curl http://localhost:8080/actuator/health
```

### 管理界面

访问 RabbitMQ 管理界面查看队列状态：
- **地址**: http://localhost:15672
- **用户名**: admin
- **密码**: admin123

## 最佳实践

### 1. 连接管理
- 合理配置连接超时和心跳间隔
- 使用连接池避免频繁创建连接
- 监控连接状态，及时处理连接异常

### 2. 消息设计
- 消息体尽量小，避免大消息影响性能
- 使用合理的消息 TTL，避免消息积压
- 为重要消息设置持久化属性

### 3. 错误处理
- 实现完善的异常处理机制
- 对于处理失败的消息，记录详细日志
- 考虑实现死信队列处理无法处理的消息

### 4. 性能优化
- 根据业务需求调整预取数量
- 使用批量发送提高吞吐量
- 监控队列深度，及时扩容消费者

## 故障排除

### 常见问题

**1. 连接失败**
```
检查 RabbitMQ 服务是否启动
确认连接参数（主机、端口、用户名、密码）是否正确
检查网络连接和防火墙设置
```

**2. 消息丢失**
```
确认启用了发布确认机制
检查消息持久化设置
验证消费者 ACK 机制是否正确实现
```

**3. 性能问题**
```
调整连接池大小和预取数量
检查消息体大小和序列化效率
监控系统资源使用情况
```

### 调试技巧

1. **启用调试日志**：查看详细的 RabbitMQ 操作日志
2. **使用管理界面**：通过 Web 界面监控队列状态
3. **消息追踪**：实现消息 ID 追踪，便于问题定位

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
