# Redis 模块 - Redisson 分布式组件集

## 模块概述

Redis 模块（`com.tbs:redis`）是基于 **Redisson**（`redisson-spring-boot-starter`）的分布式组件实现集，为平台各 SPI 提供生产级 Redis 落地：

| 能力域 | 组件 | 对接的 SPI |
|--------|------|-----------|
| 客户端装配 | `BasicRedisConfig` / `DefaultSystemCodec` | Redisson（编解码与序列化统一） |
| Spring Cache | `RedissonCacheConfig`（管理器 + L2 提供者） | cache-api：`ILevelCacheProvider` |
| TTL 装饰 | `RedissonTtlCacheDecorator` / `RedissonAdaptTtlCache` | cache-api：`ITtlCacheDecorator` |
| 键值存储 | `RedisDataHolder` | common：`IDataHolder` / `IMatchKeyFounder` |
| 分布式锁 | `RedissonLockProvider` / `RedissonLock` | lock（async 模块）：`ILockProvider` |
| 事件广播 | `AbstractRedisEventSupport` / `SimpleRedisEventSupport` | broadcast（async 模块）：`IDistributedBusSupport` 等 |
| 任务持久化 | `RedisTaskStore` | schedule（async 模块）：`TaskStore` |
| MQ 支撑 | `AbstractRedisIdempotentStore` / `RedisFaildMessageStore` | mq-api：`IIdempotentStore` / `IFailedMessageStore` |

**单 Maven 模块**（artifactId `redis`，19 个源文件），依赖 `com.tbs:common`、`com.tbs:cache-api`、`com.tbs:mq-api`、`redisson-spring-boot-starter`。

Redisson 客户端本身由 `redisson-spring-boot-starter` 基于 Spring Boot 标准 `spring.redis.*` 配置自动创建，本模块通过 `RedissonAutoConfigurationCustomizer` 定制编解码与线程参数。

## 模块结构

```
redis-mod/
└── src/main/java/
    ├── com.tbs.redis/                # 核心装配（@EnableTbsRedis 扫描范围）
    │   ├── annotation/               # @EnableTbsRedis
    │   ├── codec/                    # DefaultSystemCodec（RedisSerializer → Redisson Codec 桥接）
    │   ├── config/                   # BasicRedisConfig / RedissonCacheConfig / RedisPackAutoRegister
    │   │   └── property/             # RedisProperty(tbs.redis) / RedissonCacheProperty(tbs.redission.cache)
    │   ├── constants/                # RedisBeanNameConstants（序列化器 Bean 名）
    │   └── holder/                   # RedisDataHolder
    ├── com.tbs.cache.redis/          # 缓存侧（经 tbs.cache.* 配置或宿主扫描生效）
    ├── com.tbs.lock.redis/           # 锁侧（经 tbs.lock.lock-provider-type 配置生效）
    ├── com.tbs.broadcast.redis/      # 广播侧（需注册为 Bean）
    ├── com.tbs.schedule/             # 调度持久化（注册为 Bean 后供调度内核收集）
    └── com.tbs.mq/                   # MQ 幂等/失败消息存储（手动注册使用）
```

## 启用方式

模块无 `spring.factories`，通过注解 + 配置双重启用：

```java
@SpringBootApplication
@EnableTbsRedis   // @Import(RedisPackAutoRegister)：扫描 com.tbs.redis 包，注册 Feature.REDIS
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

```yaml
spring:
  redis:                    # Redisson 客户端连接参数（redisson-spring-boot-starter 消费）
    host: 127.0.0.1
    port: 6379
    password: 123456

tbs:
  redis:
    enable: true            # 必须为 true：控制序列化器 Bean 装配（见"使用注意事项"）
    value-serializer: org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer  # 可选：自定义序列化器实现类
```

> **启用即必须 `tbs.redis.enable=true`**：`BasicRedisConfig` 中的 Customizer 以急切求值方式引用 `REDIS_VALUE_SERIALIZER` Bean，`enable=false` 时该 Bean 不存在，Redisson 初始化将抛出异常（详见"使用注意事项"）。

## 核心组件

### 1. BasicRedisConfig - 客户端与序列化装配

- **`RedissonAutoConfigurationCustomizer`** Bean：在 Redisson starter 创建客户端前定制 `Config`——
  - **Codec**：优先取容器中的 `Codec` Bean；否则用 `DefaultSystemCodec` 包装 `REDIS_VALUE_SERIALIZER` 序列化器；
  - **线程**：`threads=8`、`nettyThreads=8`（硬编码）。
- **`REDIS_VALUE_SERIALIZER`** Bean（`@ConditionalOnMissingBean(name=...)` + `@ConditionalOnProperty(tbs.redis.enable=true)`）：
  - `tbs.redis.value-serializer` 配置了实现类 → 经无参构造反射实例化；
  - 未配置 → 兜底 `GenericJackson2JsonRedisSerializer`（JSON 序列化）。

> `RedisProperty` javadoc 中"默认 JdkSerializationRedisSerializer"为过期注释，代码实际兜底为 Jackson JSON。

**DefaultSystemCodec**：将 Spring `RedisSerializer` 包装为 Redisson `BaseCodec`（编码 `serializer.serialize` → ByteBuf，解码反向）。注意：`GenericJackson2JsonRedisSerializer` 会为对象写入 `@class` 类型信息，**对 Redis key 也会带 JSON 包装**（mq 组件因此改用 StringCodec，见下文）。

### 2. 配置说明

#### tbs.redis（RedisProperty）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enable` | boolean | **false** | 序列化器 Bean 装配开关（启用模块必须为 true） |
| `value-serializer` | Class | null | 自定义 `RedisSerializer` 实现类（无参构造） |

#### tbs.redission.cache（RedissonCacheProperty）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `cache-timeout` | long | 3600 | **单位：秒**，缓存默认 TTL（配置类内部 ×1000 转毫秒） |
| `allow-null-values` | boolean | false | 是否允许缓存 null |
| `transaction` | boolean | false | 是否开启事务感知（`setTransactionAware`） |
| `max-idle-time` | long | 1800 | key 最大空闲时间（秒），0 不限制 |
| `max-size` | int | 2048 | 缓存最大条目数，0 不限制 |
| `eviction-mode` | EvictionMode | LRU | 容量超限清除模式（LRU / LFU / NONE） |

> `cache-timeout` 单位为秒（默认 3600 = 1 小时），配置类中以 `cacheTimeout*1000` 传给 Redisson `CacheConfig`。

### 3. RedissonCacheConfig - Spring Cache 管理器

- **`redisCacheManager`**（`@ConditionalOnMissingBean(RedissonSpringCacheManager.class)`）：匿名子类覆盖 `createDefaultConfig()`——以 `cacheTimeout*1000`（TTL）、`maxIdleTime*1000`、`maxSize`、`evictionMode` 构建默认 `CacheConfig`；随后 `setCodec(redissonClient 配置的 codec)`、`setAllowNullValues`、`setTransactionAware(transaction)`；
- **`redissonLevelCacheProvider`**（`@ConditionalOnProperty(tbs.cache.multi-level.enabled=true)`）：多级缓存 L2 提供者，委托管理器按 cacheName 创建/复用缓存实例。

### 4. 缓存侧三件套（com.tbs.cache.redis）

| 组件 | 行为 |
|------|------|
| `RedissonTtlCacheDecorator`（`@Component`，order=1000） | `support()`：unwrap 得到 `RedissonCache`；`decorateCache()`：包装为 `RedissonAdaptTtlCache`（默认过期时长 = `cacheTimeout` 秒） |
| `RedissonAdaptTtlCache` | `expireAll`：`nativeCache.expire(Duration)`（expireTime 为 null 用默认时长）；`expire`：原生缓存为 `RMapCache` 时 `expireEntry(key, ttl, 0)`（已过期则直接 `remove`） |
| `RedissonLevelCacheProvider`（order=1000） | `getCache(name, ttl)` **不绑定业务 TTL**——记录 warn"please use @ExpireOn"后委托管理器按全局配置创建 |

### 5. RedisDataHolder - 键值存储

实现 `IDataHolder` + `IMatchKeyFounder`（非自动注册，经 `tbs.data.data-holder` 配置指定），基于 `RBucket`：

```java
redisDataHolder.set("user:" + id, user, true);          // force=true 强制写；false 仅键不存在时写（trySet）
Optional<User> user = redisDataHolder.get("user:" + id);
redisDataHolder.remove("user:" + id);
redisDataHolder.containsKey("user:" + id);

Collection<String> matched = redisDataHolder.find("user:*");        // getKeysByPattern 模式匹配
Collection<Map.Entry<String, Object>> entries =
    redisDataHolder.findEntries("session:*");                        // 匹配键并逐个取值

int size = redisDataHolder.size();                       // RKeys.count()
```

> **危险**：`clear()` 实现为 `RKeys.flushall()`——**清空整个 Redis 实例所有数据**，绝非仅本应用前缀。接入 `tbs.data` 前务必确认无共享实例混用。

### 6. 分布式锁（com.tbs.lock.redis）

经 `tbs.lock.lock-provider-type: com.tbs.lock.redis.RedissonLockProvider` 启用：

- **`RedissonLock`**：包装 `RLock`——`tryLock(Duration)`（毫秒等待，中断恢复标志后返回 false）、`unLock()`、`isLocked()`、`isHeldByCurrentThread()`；
- **`RedissonLockProvider`**：`getLocker(Object target)` → `RedissonLock(target.toString())`；`getConditionLocker(target)` → 匿名 `IConditionLock` + 内部类 `RedissonLockCondition`；
- **`RedissonLockCondition`**：基于 `RCountDownLatch`（conditionKey = `锁键:condition`）实现 `Condition`——`await` 系列先 `trySetCount(1)` 再等待；`signal()` → `countDown()`；**`signalAll()` 与 `signal()` 等价**（Redisson CountDownLatch 计数到 0 的特性限制）。

```java
ILock lock = lockProvider.getLocker("order_lock:" + orderId);
if (lock.tryLock(Duration.ofSeconds(10))) {
    try {
        // 业务处理
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unLock();
        }
    }
}
```

### 7. Redis 事件广播（com.tbs.broadcast.redis）

将 async 模块进程内事件系统桥接到 Redis Pub/Sub（基于 `RTopic`）：

- **`AbstractRedisEventSupport<T>`**（抽象基类，需注册为 Bean）：
  - 抽象方法：`getChannel()`（频道名）、`getEventBus()`（事件总线）、bridge/unbridge（继承 `AbstractDistributedSupport` 的数据桥接约定）；
  - `@PostConstruct initMessageListenerContainer()`：`RTopic.addListener` 注册监听——收到消息后若容器内 `IEventBus` 已装配 `IDistributedBusSupport` 则走该支持分发，否则 `unbridge` 后直接 `getEventBus().inqueueEvent()` 入队；
  - `broadcastEvent(result)`：`topic.publish(result)` 发布到频道。
- **`SimpleRedisEventSupport`**（默认桥接实现）：频道固定 **`tbs.event.channel`**；`nodeId()` 返回频道名；`handleEvent` 经 `IEventDispatcher` 分发到本地订阅者；桥接 `IPublishResult` ↔ `SimpleDistributedAcceptedPublishResult`（内部 `SimpleDataBridge`：bridge 取 `event`/`publisher.getPushlisherId()`/`handleMode`，unbridge 还原为 `SpringManagedListenablePublishResult`）。

```java
@Component
public class OrderEventSupport extends SimpleRedisEventSupport {
    // 直接继承即可获得进程内事件 → Redis 频道 → 各节点事件总线的完整链路
    // 自定义频道/载荷时继承 AbstractRedisEventSupport<T> 并实现抽象方法
}
```

### 8. 调度任务持久化（RedisTaskStore）

`TaskStore` SPI 的 Redis 实现（注册为 Bean 后被 async 调度内核自动收集，实现重启恢复）：

- 存储：Redis Hash **`schedule:task`**（key/value 均为 `StringCodec` 编码，内容为 `TaskSubmissionTransformer` 序列化的 JSON 快照）；
- `save`：`isPersistable()==false` 的内存态任务跳过落盘（warn）；
- `loadUnfinished`：进程重启后逐条还原，单条损坏数据记 error（截断 128 字符摘要）并跳过，不阻断整体恢复。

### 9. MQ 幂等与失败消息存储（com.tbs.mq）

rabbitmq-mod 消费者的 Redis 落地（**需手动注册 Bean**，模块内无自动装配）：

**`AbstractRedisIdempotentStore`**（抽象类，扩展 mq-api `AbstractIdempotentStore`，子类提供 `RedissonClient`）：

- 幂等键两态生命周期（RBucket value 即状态）：`PROCESSING`（占位）/ `DONE`（完成）；
- `tryAcquire`：`setIfAbsent(PROCESSING, ttl)` 单命令原子占位（防"崩溃后键无 TTL 永久残留"）；
- `markDone`：覆盖为 `DONE` 并延续 TTL；
- `release`：Lua 脚本**仅当值仍为 PROCESSING 时删除**——失败释放不会误删完成态记录。

**`RedisFaildMessageStore`**（类名拼写如此，扩展 mq-api `AbstractFailedMessageStore`，仅存储职责、无调度逻辑）：

- 数据结构：`failed:message:index`（ZSet，score = nextRetryTime 毫秒）+ `failed:message:status`（Hash，PROCESSING 标记）+ `failed:message:{id}`（Hash，FailedMessage JSON）；
- 全部 Redis 操作经 **Lua 脚本 + `StringCodec`** 原子执行——规避 `DefaultSystemCodec`（Jackson）为 key 附加类型前缀导致的 key 不一致问题；`FailedMessage` 的序列化在 Java 侧由 `ObjectMapper` 完成，Lua 只传递原始字符串；
- `doFetchDue`：原子地"取到期 → 标记 PROCESSING → 移出索引"；单条反序列化失败跳过，内容缺失时清理状态标记。

```java
@Configuration
public class MqRedisStoreConfig {

    @Bean
    public IFailedMessageStore failedMessageStore(RedissonClient client) {
        return new RedisFaildMessageStore(client);   // rabbitmq 消费者重试持久化依赖此 Bean
    }

    @Bean
    public IIdempotentStore idempotentStore(RedissonClient client) {
        // AbstractRedisIdempotentStore 为抽象类，需按需提供轻量子类实现 getRedissonClient()
        return new SimpleRedisIdempotentStore(client);
    }
}
```

## 装配途径说明

模块组件的生效途径分三类，接入前请确认对应途径已就位：

| 途径 | 组件 | 说明 |
|------|------|------|
| `@EnableTbsRedis` 自动扫描 | `BasicRedisConfig`、`RedissonCacheConfig`、codec、常量 | 扫描范围仅 `com.tbs.redis` 包，注册 `Feature.REDIS` |
| 宿主组件扫描（`@ComponentScan` 覆盖 `com.tbs`） | `RedissonTtlCacheDecorator`（`@Component`） | 宿主仅扫描自身包时不会生效 |
| 配置指定类名 / 手动注册 Bean | `RedisDataHolder`（`tbs.data.data-holder`）、`RedissonLockProvider`（`tbs.lock.lock-provider-type`）、`RedisTaskStore`、mq 存储、广播支持 | 按需接入 |

## 使用注意事项

1. **`clear()` = `flushall()`**：`RedisDataHolder.clear()` 清空整个 Redis 实例，共享实例环境严禁调用
2. **必须 `tbs.redis.enable=true`**：Customizer 中 `orElse(new DefaultSystemCodec(SpringUtil.getBean(REDIS_VALUE_SERIALIZER, ...)))` 为急切求值——`enable=false` 时 Bean 不存在，`getBean` 抛异常导致 Redisson 初始化失败（当前实现行为，启用模块即意味着置 true）
3. **Jackson key 包装**：`GenericJackson2JsonRedisSerializer` 序列化的 key 带 `@class` 类型信息，与本模块 mq 组件（StringCodec 纯文本 key）共存时注意键空间隔离；mq 组件的 StringCodec 方案即为此设计
4. **序列化器跨服务一致**：读写两端（多个微服务）应配置相同 `value-serializer`，否则反序列化失败
5. **广播无确认/TTL**：Redis Pub/Sub 为即发即失，节点离线期间的广播事件不会补投；可靠事件请使用 rabbitmq-mod 的 MQ 广播
6. **条件锁 signalAll 语义弱化**：基于 CountDownLatch，`signalAll()` 等价 `signal()`，仅唤醒一次等待；多等待者场景需自行循环 signal
7. **锁键即 `target.toString()`**：`getLocker(Object)` 直接调用 toString 作为 Redis 锁键，自定义对象请实现有区分度的 toString（或直接传 String）

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
