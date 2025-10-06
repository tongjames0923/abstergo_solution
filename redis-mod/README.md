# Redis 模块

Redis 模块提供了基于 Redisson 的缓存、分布式锁、数据存储和消息广播功能，是构建高性能分布式应用的重要组件。与传统的Redis客户端不同，Redisson提供了更多分布式特性。

## 目录结构

```
com.tbs.redis
├── boradcast              # Redis 消息广播实现
│   ├── AbstractRedisEventSupport.java
│   └── SimpleRedisEventSupport.java
├── cache                  # Redis 缓存相关
│   ├── RedisCacheExpireActuator.java
│   └── RedisDataHolderExpireActuator.java
├── config                 # Redis 配置类
│   ├── BasicRedisConfig.java
│   └── property
│       └── RedisProperty.java
├── constants              # 常量定义
│   └── RedisBeanNameConstants.java
├── holder                 # Redis 数据持有者
│   └── RedisDataHolder.java
└── lock                   # Redis 分布式锁
    ├── RedissonLock.java
    └── RedissonLockProvider.java
```

## 核心组件说明

### 1. Redis 配置类 (BasicRedisConfig.java)

[BasicRedisConfig](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/config/BasicRedisConfig.java#L23-L152) 是 Redis 的核心配置类，负责创建 Redisson 客户端、序列化器以及缓存管理器等。

#### 主要 Bean 说明

1. `redissonClient()`: 创建 Redisson 客户端，用于分布式锁、缓存等功能
2. `keySerializer()`: 创建 Redis 键的序列化器
3. `valueSerializer()`: 创建 Redis 值的序列化器
4. `cacheManager()`: Redisson 缓存管理器，替代了传统的 RedisCacheManager
5. `redisMessageListenerContainer()`: Redis 消息监听容器（已移除，Redisson使用自己的消息机制）

### 2. Redis 属性配置类 (RedisProperty.java)

[RedisProperty](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/config/property/RedisProperty.java#L14-L47) 类定义了 Redis 的配置属性，可通过 `application.yml` 进行配置。

#### 配置属性说明

| 属性名 | 默认值 | 说明 |
|--------|--------|------|
| enable | false | 是否启用 Redis |
| KeySerializer | StringRedisSerializer.class | Redis 键的序列化器 |
| ValueSerializer | JdkSerializationRedisSerializer.class | Redis 值的序列化器 |
| HashKeySerializer | StringRedisSerializer.class | Redis 哈希键的序列化器 |
| HashValueSerializer | JdkSerializationRedisSerializer.class | Redis 哈希值的序列化器 |
| cacheTimeout | -1L | 缓存超时时间 |
| allowNullValues | false | 是否允许空值 |

### 3. Redis 常量类 (RedisBeanNameConstants.java)

[RedisBeanNameConstants](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/constants/RedisBeanNameConstants.java#L11-L42) 定义了 Redis 相关的 Bean 名称常量，确保 Bean 名称的一致性。

### 4. Redis 数据持有者 (RedisDataHolder.java)

[RedisDataHolder](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/holder/RedisDataHolder.java#L15-L130) 实现了 [IDataHolder](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/data/IDataHolder.java#L18-L161) 接口，提供了基于 Redisson 的数据存储功能。

主要功能：
- 数据的增删改查操作
- 数据过期时间设置
- 键匹配查找功能

### 5. Redis 分布式锁 (RedissonLock.java & RedissonLockProvider.java)

[RedissonLock](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/lock/RedissonLock.java#L14-L51) 和 [RedissonLockProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/lock/RedissonLockProvider.java#L23-L145) 提供了基于 Redisson 的分布式锁实现。

主要功能：
- 获取和释放分布式锁
- 条件锁支持
- 锁超时控制

### 6. Redis 消息广播

#### AbstractRedisEventSupport.java

[AbstractRedisEventSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java#L18-L76) 是 Redis 消息广播的抽象基类，实现了消息监听和广播功能，基于Redisson的消息机制。

#### SimpleRedisEventSupport.java

[SimpleRedisEventSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/SimpleRedisEventSupport.java#L16-L73) 是 [AbstractRedisEventSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java#L18-L76) 的具体实现，处理分布式事件。

### 7. Redis 缓存过期处理器

#### RedisCacheExpireActuator.java

[RedisCacheExpireActuator](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/cache/RedisCacheExpireActuator.java#L15-L32) 实现了缓存过期处理功能，基于Redisson实现。

#### RedisDataHolderExpireActuator.java

[RedisDataHolderExpireActuator](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/cache/RedisDataHolderExpireActuator.java#L15-L33) 实现了 Redis 数据持有者的过期处理功能，基于Redisson实现。

## 自定义使用说明

### 1. 配置说明

在 `application.yml` 中添加以下配置：

```yaml
# Redis 连接配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    timeout: 30000

# TBS Redis 配置
tbs:
  redis:
    # 是否启用 Redis
    enable: true
    # Redis 键的序列化器
    key-serializer: org.springframework.data.redis.serializer.StringRedisSerializer
    # Redis 值的序列化器
    value-serializer: org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
    # Redis 哈希键的序列化器
    hash-key-serializer: org.springframework.data.redis.serializer.StringRedisSerializer
    # Redis 哈希值的序列化器
    hash-value-serializer: org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
    # 缓存超时时间(毫秒)
    cache-timeout: 3600000
    # 是否允许空值
    allow-null-values: false
```

### 2. 使用 Redis 缓存

使用 [@Cacheable](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/core/impl/SpringManagedCacheManager.java#L111-L111) 注解实现方法缓存：

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        // 从数据库获取用户信息
        return userRepository.findById(id);
    }
}
```

### 3. 使用 Redis 数据存储

通过 [RedisDataHolder](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/holder/RedisDataHolder.java#L15-L130) 进行数据存储：

```java
@Component
public class DataService {
    
    @Resource
    private RedisDataHolder redisDataHolder;
    
    public void saveData(String key, Object value) {
        redisDataHolder.set(key, value, true);
    }
    
    public <T> Optional<T> getData(String key) {
        return redisDataHolder.get(key);
    }
}
```

### 4. 使用 Redis 分布式锁

通过 [RedissonLockProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/lock/RedissonLockProvider.java#L23-L145) 获取分布式锁：

```java
@Component
public class DistributedService {
    
    @Resource
    private RedissonLockProvider lockProvider;
    
    public void doSomething() {
        ILock lock = lockProvider.getLocker("lockKey");
        try {
            if (lock.tryLock(Duration.ofSeconds(10))) {
                // 执行需要加锁的操作
                performAction();
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unLock();
            }
        }
    }
}
```

### 5. 使用 Redis 消息广播

通过继承 [AbstractRedisEventSupport](file:///Users/abstergo/IdeaProjects/abstergo_solution/redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java#L18-L76) 实现消息广播功能：

```java
@Component
public class CustomRedisEventSupport extends AbstractRedisEventSupport<YourEventType> {
    
    @Override
    public String getChannel() {
        return "your.event.channel";
    }
    
    @Override
    public void receiveEvent(YourEventType event) {
        // 处理接收到的事件
        handleEvent(event);
    }
}
```

## 最佳实践

1. **序列化选择**：
   - 根据实际需求选择合适的序列化器
   - JSON 序列化器便于调试和跨语言使用
   - JDK 序列化器性能较好但不易读

2. **连接池配置**：
   - Redisson内部已经优化了连接管理，一般无需额外配置

3. **缓存策略**：
   - 合理设置缓存过期时间
   - 对热点数据进行缓存
   - 注意缓存穿透、缓存雪崩和缓存击穿问题

4. **分布式锁使用**：
   - 控制锁的粒度，避免锁范围过大
   - 设置合理的锁超时时间
   - 确保锁的正确释放

5. **监控和运维**：
   - 监控 Redis 内存使用情况
   - 定期清理过期数据
   - 做好 Redis 集群的高可用配置

## 注意事项

1. **依赖管理**：
   - 确保项目中包含了 Redisson 相关依赖
   - 注意版本兼容性问题

2. **配置正确性**：
   - 确保 Redis 服务器地址、端口和密码配置正确
   - 启用 Redis 功能需要将 `tbs.redis.enable` 设置为 true

3. **序列化安全**：
   - 使用 JSON 序列化器时注意类型安全
   - 避免序列化敏感信息

4. **性能考虑**：
   - 避免在 Redis 中存储过大的对象
   - 合理使用 Pipeline 提高批量操作性能

5. **异常处理**：
   - 做好 Redis 连接异常的处理
   - 实现降级策略，当 Redis 不可用时能正常提供服务