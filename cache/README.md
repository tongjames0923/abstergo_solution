# Cache 模块 - 缓存解决方案

## 模块概述

Cache 模块是基于 Spring Cache 的增强缓存框架，提供 **TTL 过期控制**、**多级缓存编排**、**`@ExpireOn` 主动过期**、**缓存监听**、**操作监控** 等能力。

核心设计：

- **装饰器链**：通过 `ITtlCacheDecorator`  SPI 将任意 Spring Cache 装饰为 TTL 缓存，按 `order` 匹配最优装饰器
- **多级编排**：通过 `ILevelCacheProvider` SPI 组装 L1/L2/... 层级，读穿透、分层写、全层失效
- **TTL 策略**：`ICacheTtlPolicy` 是 TTL 的唯一事实来源，支持全局默认 + per-cacheName 覆盖
- **双场景接入**：常规单级（`DefaultTtlAdaptCacheMananger`）与多级（`MultiLevelTtlCacheMananger`）通过配置开关切换

## 模块结构

```
cache/
├── cache-api/                    # API 契约层（不含实现依赖）
│   └── com.tbs.cache/
│       ├── annotation/           # @EnableTbsCache 启用注解
│       ├── config/               # CachePackAutoRegister 自动扫描注册
│       ├── expire/               # @ExpireOn 主动过期（注解 + AOP + 过期管理）
│       ├── integration/          # 核心集成抽象（装饰器链、多级编排、TTL 适配）
│       ├── limit/                # 缓存操作限制器
│       ├── manual/               # 手动缓存处理器
│       ├── policy/               # TTL 策略（ICacheTtlPolicy + 默认实现）
│       ├── serialize/            # @CacheTransform 值序列化转换
│       ├── CacheUtils.java       # 手动缓存工具类
│       ├── ICacheListenner.java  # 缓存监听器接口
│       ├── IListenable.java      # 可监听标记接口
│       └── ITtlCacheDecorator.java  # TTL 装饰器 SPI
│
├── cache-impl/                   # 核心实现层
│   └── com.tbs.cache/
│       ├── config/               # 装配层
│       │   ├── TbsCacheConfig.java          # 主装配（开关切换单级/多级）
│       │   ├── TbsCacheProperty.java        # tbs.cache.* 属性
│       │   ├── MultiLevelCacheConfig.java   # 多级缓存装配
│       │   └── MultiLevelCacheProperty.java # tbs.cache.multi-level.* 属性
│       └── impl/
│           ├── DefaultTtlAdaptCacheMananger.java   # 常规单级管理器
│           ├── DefaultTtlCacheDecorator.java       # 默认装饰器（兜底）
│           ├── AdaptScheduleExpireTtlCache.java    # 调度式过期适配器
│           ├── MultiLevelTtlCacheMananger.java     # 多级管理器
│           ├── MultiLevelTtlCacheDecorator.java    # 多层缓存装饰器
│           ├── MultiLevelAdaptTtlCache.java        # 多层 TTL 适配
│           └── TransactionAwareCacheUnwrapperExtension.java  # 事务感知解包
│
├── cache-caffeine-impl/          # Caffeine 引擎实现
│   └── com.tbs.cache.caffeine/
│       ├── config/               # 装配层
│       │   ├── CaffeineCacheConfig.java       # 常规单级 CaffeineCacheManager
│       │   ├── CaffeineLevelCacheConfig.java  # 多级 L1 装配
│       │   └── property/
│       │       └── CaffeineLevelCacheProperty.java  # tbs.cache.level.caffeine.*
│       ├── level/
│       │   └── CaffeineLevelCacheProvider.java  # L1 层级提供者（order=0）
│       └── adapt/
│           ├── CaffeineAdaptTtlCache.java       # 调度式过期适配器
│           └── CaffeineTtlCacheDecorator.java   # TTL 装饰器（order=1000）
│
└── pom.xml                       # 父 POM（提供公共依赖）
```

**redis-mod 中的缓存实现**（`com.tbs.cache.redis` 包）：

| 类 | 职责 |
|----|------|
| `RedissonAdaptTtlCache` | Redisson TTL 适配器（原生条目级过期） |
| `RedissonTtlCacheDecorator` | Redisson TTL 装饰器（order=1000） |
| `RedissonLevelCacheProvider` | Redisson L2 层级提供者（order=1000） |

## 架构总览

### 常规单级链路

```
应用层 @Cacheable / @CachePut
    ↓
AbstractTtlAdaptCacheMananger（SYSTEM:CACHE: 前缀管理）
    ↓ getMissingCache → createCacheOnMissCache → decorateCache
DefaultTtlAdaptCacheMananger
    ↓ getOriginal() 按类型查找预配置 Bean
CaffeineCacheManager / RedissonSpringCacheManager（业务预配置）
    ↓ 创建原始 Cache
ITtlCacheDecorator 装饰器链（按 order 匹配）
    ↓ order=1000 优先
CaffeineTtlCacheDecorator → CaffeineAdaptTtlCache（调度式过期）
RedissonTtlCacheDecorator → RedissonAdaptTtlCache（原生条目过期）
    ↓ order=LOWEST_PRECEDENCE 兜底
DefaultTtlCacheDecorator → AdaptScheduleExpireTtlCache（调度式过期）
```

### 多级缓存链路

```
应用层 @Cacheable / @CachePut
    ↓
MultiLevelTtlCacheMananger（自管创建，收集 ILevelCacheProvider）
    ↓ createCacheOnMissCache() 绑定 per-cacheName TTL
DefaultMultiLevelCache（编排层：读穿透、分层写、全层失效）
    ├── L1 CaffeineCache（order=0，进程内最快）
    └── L2 RedissonCache（order=1000，分布式共享）
    ↑ 外层装饰
MultiLevelTtlCacheDecorator（order=1000，识别 AbstractMultiLevelCache）
    → MultiLevelAdaptTtlCache（将各层分别交给对应装饰器包装）
```

### 开关切换

```yaml
tbs:
  cache:
    multi-level:
      enabled: true   # true → MultiLevelTtlCacheMananger
                      # false → DefaultTtlAdaptCacheMananger（常规单级）
```

`TbsCacheConfig.adaptCacheMananger()` 根据此开关创建唯一 `@Primary` 管理器。

## 核心概念

### 1. TTL 装饰器 SPI（ITtlCacheDecorator）

```java
public interface ITtlCacheDecorator extends Ordered {
    boolean support(Cache cache, CacheManager manager);
    AbstractTtlCache decorateCache(Cache cache, CacheManager manager);
    default int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
```

管理器在 `decorateCacheToTtl()` 中按 `order` 升序遍历装饰器链，首个 `support()` 返回 `true` 的装饰器生效。

**内置装饰器**：

| 装饰器 | order | 匹配条件 | 适配器 |
|--------|-------|----------|--------|
| `CaffeineTtlCacheDecorator` | 1000 | `instanceof CaffeineCache` | `CaffeineAdaptTtlCache` |
| `RedissonTtlCacheDecorator` | 1000 | `instanceof RedissonCache` | `RedissonAdaptTtlCache` |
| `MultiLevelTtlCacheDecorator` | 1000 | `instanceof AbstractMultiLevelCache` | `MultiLevelAdaptTtlCache` |
| `DefaultTtlCacheDecorator` | LOWEST_PRECEDENCE | 全部（兜底） | `AdaptScheduleExpireTtlCache` |

### 2. 层级缓存 SPI（ILevelCacheProvider）

```java
public interface ILevelCacheProvider {
    default int getOrder() { return 0; }
    default boolean support(String cacheName) { return true; }
    Cache getCache(String name);
    default Cache getCache(String name, Duration ttl) { return getCache(name); }
    default void configure(Map<String, Object> props) {}
}
```

**内置实现**：

| 提供者 | order | 引擎 | 过期能力 |
|--------|-------|------|----------|
| `CaffeineLevelCacheProvider` | 0 | Caffeine（进程内） | 缓存级 `expireAfterWrite`，TTL = min(全局, 业务) |
| `RedissonLevelCacheProvider` | 1000 | Redisson RMapCache（分布式） | 条目级 `expireEntry()`，TTL = min(全局, 业务) |

### 3. TTL 策略（ICacheTtlPolicy）

```java
public interface ICacheTtlPolicy {
    Duration ttlOf(String cacheName);          // 获取 TTL（覆盖 > 默认）
    Optional<Duration> findOverride(String cacheName);  // 仅查覆盖
    Duration defaultTtl();                     // 全局默认
}
```

默认实现 `DefaultCacheTtlPolicy`：全局默认 + per-cacheName 覆盖，由 `MultiLevelCacheConfig` 在多级启用时注册。

### 4. @ExpireOn 主动过期

```java
@Retention(RUNTIME) @Target(METHOD)
public @interface ExpireOn {
    long value() default 0;              // 过期时长
    TimeUnit timeUnit() default SECONDS; // 时间单位
    String[] cacheNames() default {};    // 目标缓存名
    boolean expireAll() default false;   // 是否全部过期
}
```

需与 `@Cacheable` / `@CachePut` 组合使用。`ExpireAop` 切面在方法执行后异步提交过期任务，通过 `callToExpire` / `callToExpireAll` 写入过期请求，下次 `put` 时触发实际过期。

### 5. 缓存监听器（ICacheListenner）

```java
public interface ICacheListenner {
    boolean support(Cache cache);
    void onGet(String cacheName, Object key, Object value);
    void onPut(String cacheName, Object key, Object value);
    void onEvict(String cacheName, Object key);
    void onClear(String cacheName);
    void calledExpire(String cacheName, Object key, Date expireTime);
}
```

`AbstractTtlCache` 在 get/put/evict/clear/expire 时自动回调已注册的监听器。

### 6. 值序列化转换（@CacheTransform）

标注在实体类上，指定 `IDataBridge` 实现，`AbstractTtlCache` 在 `get` 时执行 `TRANSFORM_READ`、`put` 时执行 `TRANSFORM_WRITE`，实现缓存值的自动序列化/反序列化转换。

## 配置参考

### 常规缓存（tbs.cache）

```yaml
tbs:
  cache:
    # 常规单级 CacheManager 实现类（多级启用后此项闲置）
    cache-manager-impl: org.springframework.cache.caffeine.CaffeineCacheManager
    # 以下组件均有默认实现，通常无需覆盖
    ttl-cache-decorator: com.tbs.cache.impl.DefaultTtlCacheDecorator
    recorder: com.tbs.cache.limit.impl.SimpleCacheOperationRecorder
    manual-cache-handler: com.tbs.cache.manual.impl.MapCacheHandler
    cache-limiter: com.tbs.cache.limit.impl.SimpleCacheLimiter
```

### 多级缓存（tbs.cache.multi-level）

```yaml
tbs:
  cache:
    multi-level:
      enabled: true
      ttl:
        default-ttl: 1h           # 全局默认 TTL
        overrides:                # per-cacheName 覆盖
          userCache: 30m
          dictCache: 24h
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | false | 多级缓存总开关 |
| `ttl.default-ttl` | Duration | 1h | 未在 overrides 中声明的 cacheName 使用此值 |
| `ttl.overrides` | Map<String, Duration> | {} | 按业务 cacheName（不带前缀）覆盖 TTL |

### Caffeine 引擎（tbs.cache.level.caffeine）

```yaml
tbs:
  cache:
    level:
      caffeine:
        maximum-size: 10000       # 每实例最大条目数（LRU 淘汰）
        expire-after-write: 60s   # 缓存级原生 TTL / L1 安全网上限
        record-stats: false       # 启用统计（轻微性能开销）
        allow-null-values: false  # 是否允许缓存 null
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `maximum-size` | long | 10000 | 超出后 LRU 淘汰 |
| `expire-after-write` | Duration | 60s | 常规场景作缓存级 TTL；多级场景作 L1 安全网（实际 TTL = min(此值, 业务TTL)） |
| `record-stats` | boolean | false | 启用后 `cache.stats()` 可获取命中率 |
| `allow-null-values` | boolean | false | true 时 `put(key, null)` 缓存 null 标记 |

### Redisson 引擎（tbs.redission.cache）

```yaml
tbs:
  redission:
    cache:
      cache-timeout: 3600     # 全局缓存超时（秒），也作 L2 安全网上限
      max-idle-time: 1800     # 最大空闲时间（秒）
      max-size: 2048          # 最大条目数
      eviction-mode: LRU      # 驱逐模式
      allow-null-values: false
      transaction: false
```

## 快速开始

### 1. 启用缓存

```java
@Configuration
@EnableTbsCache   // 导入 CachePackAutoRegister，自动扫描 com.tbs.cache 包
public class AppConfig {
}
```

### 2. 常规单级使用（Caffeine）

```yaml
tbs:
  cache:
    cache-manager-impl: org.springframework.cache.caffeine.CaffeineCacheManager
    level:
      caffeine:
        maximum-size: 10000
        expire-after-write: 60s
```

```java
@Service
public class UserService {

    @Cacheable(cacheNames = "userCache", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @CachePut(cacheNames = "userCache", key = "#user.id")
    public User update(User user) {
        userRepository.save(user);
        return user;
    }

    @CacheEvict(cacheNames = "userCache", key = "#id")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 3. @ExpireOn 主动过期

```java
@Cacheable(cacheNames = "orderCache", key = "#orderId")
@ExpireOn(value = 30, timeUnit = TimeUnit.MINUTES, cacheNames = "orderCache")
public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId).orElse(null);
}

@CachePut(cacheNames = "reportCache", key = "#type")
@ExpireOn(value = 1, timeUnit = TimeUnit.HOURS, cacheNames = "reportCache", expireAll = true)
public Report generateReport(String type) {
    // ...
}
```

### 4. 多级缓存

```yaml
tbs:
  cache:
    multi-level:
      enabled: true
      ttl:
        default-ttl: 1h
        overrides:
          userCache: 30m
    level:
      caffeine:
        maximum-size: 10000
        expire-after-write: 60s
  redission:
    cache:
      cache-timeout: 3600
```

启用后自动组装：`L1 Caffeine（order=0）` → `L2 Redisson（order=1000）`。

读取穿透：L1 miss → L2 命中 → 静默回填 L1。
写入：自底向上（L2 → L1），远层失败快速失败。
失效：全层尽力而为。

### 5. 缓存监听器

```java
@Component
public class MetricsCacheListener implements ICacheListenner {

    @Override
    public boolean support(Cache cache) {
        return true; // 监听所有缓存
    }

    @Override
    public void onGet(String cacheName, Object key, Object value) {
        // 统计命中率
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) { }

    @Override
    public void onEvict(String cacheName, Object key) { }

    @Override
    public void onClear(String cacheName) { }

    @Override
    public void calledExpire(String cacheName, Object key, Date expireTime) { }
}
```

### 6. CacheUtils 手动缓存

```java
@Resource
private CacheUtils cacheUtils;

// 基本操作
cacheUtils.put("key", value);
ValueWrapper<User> wrapper = cacheUtils.get("key", User.class);
cacheUtils.remove("key");

// 不存在则计算
ValueWrapper<User> wrapper = cacheUtils.computeIfAbsent("key",
    () -> userService.findById(1), User.class);

// 注解方式（走 Spring Cache 链路）
cacheUtils.cacheableSet("key", value);
Object val = cacheUtils.cacheableGet("key");
cacheUtils.cacheableEvict("key");
cacheUtils.cacheableEvictAll();
```

## 扩展开发

### 自定义层级提供者

```java
public class HazelcastLevelCacheProvider implements ILevelCacheProvider {

    @Override
    public int getOrder() { return 500; } // L1.5，位于 Caffeine(0) 和 Redisson(1000) 之间

    @Override
    public Cache getCache(String name, Duration ttl) {
        // 创建 Hazelcast 缓存实例
    }
}
```

在配置类中注册为 Bean 即可被 `MultiLevelTtlCacheMananger` 自动收集。

### 自定义 TTL 装饰器

```java
@Component
public class CustomTtlCacheDecorator implements ITtlCacheDecorator {

    @Override
    public boolean support(Cache cache, CacheManager manager) {
        return cache instanceof CustomCache;
    }

    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        return new CustomAdaptTtlCache((CustomCache) cache);
    }

    @Override
    public int getOrder() { return 500; } // 先于默认兜底，后于 1000
}
```

### 自定义 TTL 策略

```java
@Bean
@Primary
ICacheTtlPolicy customTtlPolicy() {
    return new ICacheTtlPolicy() {
        @Override
        public Duration ttlOf(String cacheName) {
            // 从数据库/配置中心动态获取
        }
        @Override
        public Optional<Duration> findOverride(String cacheName) {
            return Optional.ofNullable(ttlOf(cacheName));
        }
        @Override
        public Duration defaultTtl() { return Duration.ofHours(1); }
    };
}
```

## 依赖关系

```
cache-api ──────── common, async（ISchedule）, spring-boot-starter-cache, spring-boot-starter-aop
cache-impl ─────── cache-api
cache-caffeine-impl ── cache-api, caffeine, spring-context-support
redis-mod（缓存部分）── cache-api, redisson
```

## 最佳实践

1. **TTL 安全网**：各层引擎的全局 TTL 配置作为安全网上限，`effectiveTtl = min(全局, 业务TTL)`，确保缓存不会无限存活
2. **L1 < L2 存活**：L1 的 `expireAfterWrite` 应小于 L2 的 `cacheTimeout`，避免读到过期数据
3. **per-cacheName TTL**：通过 `tbs.cache.multi-level.ttl.overrides` 为高频变更的缓存设置较短 TTL
4. **监控命中率**：启用 `record-stats: true` 配合 `ICacheListenner` 监控缓存健康度
5. **空值防护**：`allow-null-values: false` 时注意 `@Cacheable` 返回 null 的异常处理
6. **事务安全**：`TransactionAwareCacheUnwrapperExtension` 自动解包事务感知缓存，业务代码无需手动处理

## 故障排除

| 问题 | 排查方向 |
|------|----------|
| 缓存不生效 | 检查 `@EnableTbsCache` 是否标注；检查 `tbs.cache.cache-manager-impl` 配置 |
| TTL 未生效 | 确认对应引擎的装饰器 order < `DefaultTtlCacheDecorator`（LOWEST_PRECEDENCE） |
| 多级未命中 | 检查 `tbs.cache.multi-level.enabled=true`；确认 `ILevelCacheProvider` Bean 已注册 |
| @ExpireOn 不触发 | 必须与 `@Cacheable`/`@CachePut` 组合；检查 `value > 0` |
| 装饰器未匹配 | 日志搜索 `No TTL cache decorator found`，检查引擎依赖是否引入 |

启用调试日志：

```yaml
logging:
  level:
    com.tbs.cache: DEBUG
    com.tbs.cache.caffeine: DEBUG
    com.tbs.cache.redis: DEBUG
```

---

**最后更新**: 2026年8月
**版本**: 1.0-SNAPSHOT
