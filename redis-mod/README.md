# Redis 模块

Redis 模块是基于 Redisson 的高性能分布式数据解决方案，为 Abstergo Solution 提供完整的缓存、分布式锁、数据存储和消息广播功能。该模块充分利用 Redisson 的分布式特性，构建了企业级的 Redis 集成方案。

## 核心特性

### 🚀 开箱即用
- **自动配置**：基于 Spring Boot 自动配置 Redisson 客户端和连接管理
- **条件化启用**：支持通过配置灵活启用或禁用 Redis 功能
- **序列化支持**：内置多种序列化器，支持 JSON 和 JDK 序列化

### 🔒 分布式锁
- **可重入锁**：支持公平锁和非公平锁，避免死锁问题
- **条件锁**：实现完整的 Condition 机制，支持线程间协调
- **锁超时控制**：支持锁的自动释放和超时机制
- **锁状态监控**：提供锁状态查询和持有线程检测

### 💾 数据存储
- **键值存储**：完整的 CRUD 操作，支持强制写入和条件写入
- **批量操作**：支持键的批量查询、删除和模式匹配
- **数据统计**：提供存储大小、空状态和键数量统计
- **模式匹配**：支持通配符模式查找键和条目

### 📡 消息广播
- **发布订阅**：基于 Redisson Topic 的发布订阅机制
- **事件桥接**：与 Abstergo Solution 事件系统无缝集成
- **自动监听**：应用启动时自动注册消息监听器
- **分布式事件**：支持跨节点的分布式事件处理

### ⚡ 性能优化
- **连接池管理**：Redisson 内置优化的连接池机制
- **序列化效率**：支持高效的 JSON 和二进制序列化
- **批量操作**：优化批量数据操作性能
- **内存管理**：合理的缓存策略和过期机制

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加 Redis 模块依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>redis</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

在 `application.yml` 中配置 Redis 连接信息：

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 123456
    timeout: 30000

tbs:
  redis:
    enable: true  # 启用 Redis 功能
    value-serializer: org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer  # JSON 序列化器
  
  redisson:
    cache:
      cache-timeout: 3600000        # 缓存超时时间(毫秒)
      allow-null-values: false      # 是否允许空值
      transaction: false            # 是否开启 Redis 事务
      max-idle-time: 1800           # key 最大空闲时间(秒)
      max-size: 2048               # key 最大数量
      eviction-mode: LRU            # 清除模式
```

### 3. 使用数据存储

通过 <mcsymbol name="RedisDataHolder" filename="RedisDataHolder.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/holder/RedisDataHolder.java" startline="15" type="class">RedisDataHolder</mcsymbol> 进行数据存储操作：

```java
@Service
public class UserService {
    
    @Resource
    private RedisDataHolder redisDataHolder;
    
    public void saveUser(User user) {
        // 强制写入用户数据
        redisDataHolder.set("user:" + user.getId(), user, true);
        
        // 设置用户会话，如果键不存在则写入
        boolean success = redisDataHolder.set("session:" + user.getId(), 
            user.getSession(), false);
        
        log.info("用户数据保存{}", success ? "成功" : "失败");
    }
    
    public Optional<User> getUser(String userId) {
        // 获取用户数据
        return redisDataHolder.get("user:" + userId);
    }
    
    public void deleteUser(String userId) {
        // 删除用户数据
        redisDataHolder.remove("user:" + userId);
        redisDataHolder.remove("session:" + userId);
    }
    
    public List<String> findUserKeys(String pattern) {
        // 查找匹配的用户键
        return redisDataHolder.find("user:*" + pattern + "*")
            .stream()
            .collect(Collectors.toList());
    }
}
```

### 4. 使用分布式锁

通过 <mcsymbol name="RedissonLockProvider" filename="RedissonLockProvider.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/lock/RedissonLockProvider.java" startline="27" type="class">RedissonLockProvider</mcsymbol> 获取分布式锁：

```java
@Service
public class OrderService {
    
    @Resource
    private RedissonLockProvider lockProvider;
    
    public void processOrder(String orderId) {
        // 获取订单锁
        ILock lock = lockProvider.getLocker("order_lock:" + orderId);
        
        try {
            // 尝试获取锁，最多等待10秒
            if (lock.tryLock(Duration.ofSeconds(10))) {
                // 执行需要加锁的订单处理逻辑
                processOrderInternal(orderId);
                
                log.info("订单 {} 处理完成", orderId);
            } else {
                log.warn("获取订单锁失败: {}", orderId);
            }
        } finally {
            // 确保锁被正确释放
            if (lock.isHeldByCurrentThread()) {
                lock.unLock();
            }
        }
    }
    
    public void processOrderWithCondition(String orderId) throws Exception {
        // 获取带条件的锁
        IConditionLock conditionLock = lockProvider.getConditionLocker("order_condition:" + orderId);
        
        if (conditionLock.tryLock(Duration.ofSeconds(5))) {
            try {
                Condition condition = conditionLock.newCondition();
                
                // 等待条件满足
                condition.await(30, TimeUnit.SECONDS);
                
                // 处理订单
                processOrderInternal(orderId);
            } finally {
                conditionLock.unLock();
            }
        }
    }
}
```

### 5. 使用消息广播

继承 <mcsymbol name="AbstractRedisEventSupport" filename="AbstractRedisEventSupport.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java" startline="18" type="class">AbstractRedisEventSupport</mcsymbol> 实现消息广播：

```java
@Component
public class UserEventBroadcaster extends AbstractRedisEventSupport<UserEvent> {
    
    @Resource
    private IEventBus eventBus;
    
    @Override
    protected IEventBus getEventBus() {
        return eventBus;
    }
    
    @Override
    public String getChannel() {
        return "user.events";
    }
    
    @Override
    public void receiveEvent(UserEvent event) {
        // 处理接收到的用户事件
        log.info("接收到用户事件: {}", event);
        
        // 根据事件类型处理
        switch (event.getType()) {
            case CREATE:
                handleUserCreate(event);
                break;
            case UPDATE:
                handleUserUpdate(event);
                break;
            case DELETE:
                handleUserDelete(event);
                break;
        }
    }
    
    public void publishUserEvent(UserEvent event) {
        // 广播用户事件
        broadcastEvent(event);
        log.info("用户事件已广播: {}", event);
    }
}
```

## 核心组件详解

### BasicRedisConfig - 核心配置类

<mcsymbol name="BasicRedisConfig" filename="BasicRedisConfig.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/config/BasicRedisConfig.java" startline="23" type="class">BasicRedisConfig</mcsymbol> 是模块的核心配置类，负责创建和管理 Redisson 客户端：

**主要 Bean 定义：**
- `redissonClient()`: 创建 Redisson 客户端，支持单机模式配置
- `valueSerializer()`: 配置 Redis 值序列化器，支持 JSON 和 JDK 序列化
- **条件化配置**：基于 `tbs.redis.enable` 属性控制是否启用

### RedisProperty - 配置属性

<mcsymbol name="RedisProperty" filename="RedisProperty.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/config/property/RedisProperty.java" startline="10" type="class">RedisProperty</mcsymbol> 封装了 Redis 相关配置：

**配置属性：**
- `enable`: 是否启用 Redis 功能（默认：false）
- `ValueSerializer`: Redis 值序列化器（默认：JdkSerializationRedisSerializer）

### RedisDataHolder - 数据存储实现

<mcsymbol name="RedisDataHolder" filename="RedisDataHolder.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/holder/RedisDataHolder.java" startline="15" type="class">RedisDataHolder</mcsymbol> 提供完整的数据存储功能：

**核心功能：**
- **CRUD 操作**：支持 set、get、remove、clear 等基本操作
- **批量查询**：支持 keys、entries、values 等批量操作
- **模式匹配**：支持通配符模式查找键和条目
- **状态统计**：提供 size、isEmpty、containsKey 等状态查询

### RedissonLockProvider - 分布式锁提供者

<mcsymbol name="RedissonLockProvider" filename="RedissonLockProvider.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/lock/RedissonLockProvider.java" startline="27" type="class">RedissonLockProvider</mcsymbol> 提供高级分布式锁功能：

**锁类型支持：**
- **普通锁**：通过 `getLocker()` 获取基本分布式锁
- **条件锁**：通过 `getConditionLocker()` 获取带条件的锁
- **可重入锁**：支持同一线程多次获取锁

**条件锁特性：**
- 完整的 Condition 接口实现
- 基于 Redisson CountDownLatch 的条件等待机制
- 支持超时等待和中断处理

### AbstractRedisEventSupport - 消息广播基类

<mcsymbol name="AbstractRedisEventSupport" filename="AbstractRedisEventSupport.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java" startline="18" type="class">AbstractRedisEventSupport</mcsymbol> 提供消息广播功能：

**消息机制：**
- **自动监听**：应用启动时自动注册消息监听器
- **事件桥接**：与 Abstergo Solution 事件系统集成
- **发布订阅**：基于 Redisson Topic 的发布订阅模式
- **类型安全**：支持泛型类型的事件处理

## 高级用法

### 自定义序列化器

```java
@Configuration
public class CustomRedisConfig {
    
    @Bean
    public RedisSerializer<Object> customSerializer() {
        // 创建自定义序列化器
        return new RedisSerializer<Object>() {
            @Override
            public byte[] serialize(Object obj) throws SerializationException {
                // 自定义序列化逻辑
                return CustomSerializationUtils.serialize(obj);
            }
            
            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                // 自定义反序列化逻辑
                return CustomSerializationUtils.deserialize(bytes);
            }
        };
    }
}
```

### 批量数据操作

```java
@Service
public class BatchDataService {
    
    @Resource
    private RedisDataHolder redisDataHolder;
    
    public void batchUpdateUsers(List<User> users) {
        // 批量更新用户数据
        users.forEach(user -> {
            redisDataHolder.set("user:" + user.getId(), user, true);
        });
        
        log.info("批量更新 {} 个用户数据", users.size());
    }
    
    public Map<String, User> batchGetUsers(List<String> userIds) {
        // 批量获取用户数据
        return userIds.stream()
            .map(userId -> {
                Optional<User> user = redisDataHolder.get("user:" + userId);
                return new AbstractMap.SimpleEntry<>(userId, user.orElse(null));
            })
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
```

### 分布式锁的高级用法

```java
@Service
public class AdvancedLockService {
    
    @Resource
    private RedissonLockProvider lockProvider;
    
    public void distributedTransaction(String resourceId) {
        ILock lock = lockProvider.getLocker("resource:" + resourceId);
        
        try {
            // 尝试获取锁，带重试机制
            boolean acquired = false;
            for (int i = 0; i < 3; i++) {
                if (lock.tryLock(Duration.ofSeconds(5))) {
                    acquired = true;
                    break;
                }
                Thread.sleep(1000); // 等待后重试
            }
            
            if (acquired) {
                // 执行分布式事务
                performDistributedTransaction(resourceId);
            } else {
                throw new RuntimeException("获取分布式锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("锁获取被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unLock();
            }
        }
    }
}
```

## 监控和调试

### 启用详细日志

在 `application.yml` 中启用 Redis 相关日志：

```yaml
logging:
  level:
    com.tbs.redis: DEBUG
    org.redisson: INFO
    org.springframework.data.redis: WARN
```

### 健康检查

模块自动集成 Spring Boot Actuator 的健康检查：

```bash
# 检查 Redis 连接状态
curl http://localhost:8080/actuator/health
```

### Redis 监控工具

使用 Redis 命令行工具监控数据：

```bash
# 连接 Redis
redis-cli -h 127.0.0.1 -p 6379 -a 123456

# 查看所有键
keys *

# 查看内存使用情况
info memory

# 查看客户端连接
info clients
```

## 最佳实践

### 1. 键命名规范
- 使用冒号分隔的层次结构：`domain:type:id`
- 避免过长的键名，提高存储效率
- 为不同业务域使用不同的键前缀

### 2. 序列化策略
- 生产环境推荐使用 JSON 序列化，便于调试
- 性能敏感场景可使用 JDK 序列化
- 自定义序列化器时注意版本兼容性

### 3. 锁使用规范
- 控制锁的粒度，避免锁范围过大
- 设置合理的锁超时时间，防止死锁
- 确保锁的正确释放，使用 try-finally 块

### 4. 内存管理
- 合理设置数据过期时间
- 监控 Redis 内存使用情况
- 定期清理过期和无用数据

## 故障排除

### 常见问题

**1. 连接失败**
```
检查 Redis 服务是否启动
确认连接参数（主机、端口、密码）是否正确
检查网络连接和防火墙设置
```

**2. 序列化错误**
```
确认序列化器配置正确
检查序列化对象的版本兼容性
避免序列化敏感信息
```

**3. 锁竞争问题**
```
调整锁的超时时间和重试策略
优化锁的粒度，减少竞争
监控锁的使用情况，及时发现瓶颈
```

### 调试技巧

1. **启用调试日志**：查看详细的 Redis 操作日志
2. **使用监控工具**：通过 Redis 命令行工具监控数据状态
3. **性能分析**：监控 Redis 内存使用和响应时间

## 版本信息

- **Spring Boot**: 2.x+
- **Redisson**: 3.x+
- **Redis**: 5.0+
- **Java**: 11+

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进 Redis 模块。在提交代码前请确保：

1. 代码符合项目的编码规范
2. 添加相应的单元测试
3. 更新相关文档
4. 通过所有现有测试

## 许可证

本项目基于 Abstergo Solution 许可证。详情请参阅项目根目录的 LICENSE 文件。
