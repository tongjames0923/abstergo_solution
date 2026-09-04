# Cache 模块 - 缓存解决方案

## 模块概述

Cache 模块（`com.tbs:cache`）是基于 Spring Cache 的增强缓存框架，提供 **TTL 过期控制**、**多级缓存编排**、**`@ExpireOn` 主动过期**、**缓存监听**、**手动缓存与淘汰限流**、**值序列化转换** 等能力。

核心设计：

- **装饰器链**：通过 `ITtlCacheDecorator` SPI 将任意 Spring Cache 装饰为 TTL 缓存（`AbstractTtlCache` 子类），按 `order` 升序匹配首个 `support()` 通过的装饰器
- **多级编排**：通过 `ILevelCacheProvider` SPI 组装 L1/L2/... 层级（`DefaultMultiLevelCache`），读穿透静默回填、写穿透自底向上、全层失效
- **TTL 策略**：`ICacheTtlPolicy` 是多级缓存 TTL 的唯一事实来源（覆盖 > 默认）
- **双场景接入**：常规单级（`DefaultTtlAdaptCacheMananger`）与多级（`MultiLevelTtlCacheMananger`）由 `tbs.cache.multi-level.enabled` 开关切换，容器内仅保留唯一 `@Primary` 管理器

模块包含三个子模块，父 POM 统一依赖 `spring-boot-starter-cache`、`com.tbs:common`、`com.tbs:async`（过期任务经 async 调度执行）、`spring-boot-starter-aop`：

| 子模块 | 依赖 | 职责 |
|--------|------|------|
| `cache-api` | （继承父 POM） | API 契约层：SPI 接口、注解、抽象基类 |
| `cache-impl` | `cache-api` | 核心实现层：管理器、装饰器、多级适配 |
| `cache-caffeine-impl` | `cache-api`、`caffeine`、`spring-context-support` | Caffeine 引擎装配与 L1 提供 |

Redisson 引擎实现（装饰器 + L2 提供者）位于 **redis-mod** 模块的 `com.tbs.cache.redis` 包，引入 redis-mod 后自动生效。

## 启用方式

模块无 `spring.factories` 自动配置，通过注解启用：

```java
@SpringBootApplication
@EnableTbsCache   // @EnableCommonPack + @Import(CachePackAutoRegister)，扫描 com.tbs.cache 包，注册 Feature.CACHE
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`TbsCacheConfig` 同时开启 Spring 的 `@EnableCaching`。

## 模块结构

```
cache/
├── cache-api/                    # API 契约层
│   └── com.tbs.cache/
│       ├── (root)                # CacheNamesConstants / CacheUtils / ICacheListenner /
│       │                         #   IListenable / ITtlCacheDecorator
│       ├── annotation/           # @EnableTbsCache
│       ├── config/               # CachePackAutoRegister（自动扫描注册器）
│       ├── expire/               # @ExpireOn 主动过期体系
│       │   ├── annotation/       #   @ExpireOn
│       │   ├── aop/              #   ExpireAop 切面
│       │   ├── (root)            #   ICacheExpiredActuator / IExpiredActuator /
│       │   │                     #   IExpireDetailsManager / ITtlIntercepter
│       │   ├── impl/             #   CacheExpireTaskExecutor / CacheExpireTaskPayload /
│       │   │                     #   LocalMapExpireDetailsManager
│       │   └── vo/               #   ExpireDetails
│       ├── integration/          # 核心集成抽象
│       │   ├── AbstractTtlCache              # TTL 缓存装饰基类（监听/转换/过期标记）
│       │   ├── AbstractAdaptTtlCache         # 适配器基类（原始 Cache 包装）
│       │   ├── AbstractTtlAdaptCacheMananger # 单级管理器基类（SYSTEM:CACHE: 前缀）
│       │   ├── AbstractMultiLevelCache       # 多级编排基类
│       │   ├── AbstractMultiLevelTtlCacheMananger # 多级管理器基类
│       │   ├── DefaultMultiLevelCache        # 默认多级缓存
│       │   └── ILevelCacheProvider           # 层级提供者 SPI
│       ├── limit/                # 缓存限制器与操作记录（ICacheLimiter /
│       │   │                     #   ICacheOperationRecorder / ICacheOperationView）
│       │   └── impl/             # SimpleCacheLimiter / SimpleCacheOperationRecorder
│       ├── manual/               # 手动缓存（IManualCacheHandler + impl/MapCacheHandler）
│       ├── policy/               # TTL 策略（ICacheTtlPolicy + DefaultCacheTtlPolicy）
│       └── serialize/            # @CacheTransform 值转换（annotation/ + utils/ValueTransformUtils）
│
├── cache-impl/                   # 核心实现层
│   └── com.tbs.cache/
│       ├── config/               # TbsCacheConfig / TbsCacheProperty(tbs.cache) /
│       │                         #   MultiLevelCacheConfig / MultiLevelCacheProperty(tbs.cache.multi-level)
│       └── impl/                 # DefaultTtlAdaptCacheMananger / DefaultTtlCacheDecorator /
│                                 #   AdaptScheduleExpireTtlCache / MultiLevelTtlCacheMananger /
│                                 #   MultiLevelTtlCacheDecorator / MultiLevelAdaptTtlCache /
│                                 #   NoMultiLevelCacheTtlInterceptor / TransactionAwareCacheUnwrapperExtension
│
└── cache-caffeine-impl/          # Caffeine 引擎实现
    └── com.tbs.cache.caffeine/
        ├── config/               # CaffeineCacheConfig（常规 CaffeineCacheManager）/
│                                  #   CaffeineLevelCacheConfig（L1 装配）/
│                                  #   property/CaffeineLevelCacheProperty(tbs.cache.level.caffeine)
        └── level/                # CaffeineLevelCacheProvider（L1 层级提供者，order=0）

redis-mod: com.tbs.cache.redis    # RedissonTtlCacheDecorator(order=1000) /
                                  #   RedissonAdaptTtlCache（原生条目过期）/
                                  #   RedissonLevelCacheProvider（L2，order=1000）
```

## 架构总览

### 常规单级链路

```
应用层 @Cacheable / @CachePut
    ↓
AbstractTtlAdaptCacheMananger（getCache 自动追加 SYSTEM:CACHE: 前缀）
    ↓ getMissingCache → createCacheOnMissCache → decorateCache
DefaultTtlAdaptCacheMananger
    ↓ getOriginal() 按配置类型（tbs.cache.cache-manager-impl）懒加载原始管理器
ConcurrentMapCacheManager（默认）/ CaffeineCacheManager / RedissonSpringCacheManager
    ↓ 创建原始 Cache 后进入装饰器链
ITtlCacheDecorator 链（SpringUtil.getBeansOfType 收集，按 order 升序，首个 support() 生效）
    ↓ order=1000：RedissonTtlCacheDecorator → RedissonAdaptTtlCache（原生条目级过期）
    ↓ order=LOWEST_PRECEDENCE 兜底：DefaultTtlCacheDecorator → AdaptScheduleExpireTtlCache（调度式过期）
```

> 注意：模块内**没有** Caffeine 专用装饰器。`CaffeineCache` 由兜底的 `DefaultTtlCacheDecorator` 装饰（调度式过期）。Caffeine 引擎的原生 TTL 通过 `CaffeineCacheManager` 构建参数（`tbs.cache.level.caffeine.expire-after-write`）生效。

### 多级缓存链路

```
应用层 @Cacheable / @CachePut
    ↓
MultiLevelTtlCacheMananger（自管创建，收集容器内全部 ILevelCacheProvider，按 order 升序）
    ↓ createCacheOnMissCache()：剥离 SYSTEM:CACHE: 前缀 → resolveBusinessTtl(name)
    ↓                          → 逐层 provider.support(name) ? getCache(name, businessTtl)
DefaultMultiLevelCache（编排层：读穿透、分层写、全层失效）
    ├── L1 CaffeineCache（CaffeineLevelCacheProvider，order=0，创建时绑定业务 TTL）
    └── L2 RedissonCache（RedissonLevelCacheProvider，order=1000，不绑定业务 TTL，见下）
    ↑ 外层装饰（多级启用时由 MultiLevelCacheConfig 注册）
MultiLevelTtlCacheDecorator（order=1000，识别 AbstractMultiLevelCache）
    → MultiLevelAdaptTtlCache（将各层分别交给装饰器链包装，过期时逐层下发）
```

多级语义（`AbstractMultiLevelCache`）：

- **读取**：按 L1 → L2 顺序穿透，命中后**静默回填**上层（不触发监听器）；`get(key, valueLoader)` 仅委托最底层加载后回填上层
- **写入**：自底向上（L2 → L1），远层失败**快速失败**（抛出异常）
- **失效**：`evict`/`clear` 全层尽力而为（单层失败仅告警）
- **per-cacheName TTL**：L1 创建时绑定业务 TTL（业务 TTL 为 null 时取 Caffeine 全局 `expire-after-write`）；L2 提供者**不绑定**业务 TTL（记录 warn 日志并提示使用 `@ExpireOn`），引擎 TTL 取决于 `RedissonSpringCacheManager` 配置

### 开关切换

```yaml
tbs:
  cache:
    multi-level:
      enabled: true   # true → MultiLevelTtlCacheMananger
                      # false（默认）→ DefaultTtlAdaptCacheMananger（常规单级）
```

`TbsCacheConfig.adaptCacheMananger()`（`@Primary`）根据此开关创建管理器；多级启用时同时注册 `DefaultCacheTtlPolicy` 与 `MultiLevelTtlCacheDecorator`（均 `@ConditionalOnProperty`）。

## 核心概念

### 1. TTL 装饰器 SPI（ITtlCacheDecorator）

```java
public interface ITtlCacheDecorator extends Ordered {
    default boolean support(Cache cache, CacheManager manager) { return false; }
    AbstractTtlCache decorateCache(Cache cache, CacheManager manager);
    default int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
```

**内置装饰器**（全部核对自源码）：

| 装饰器 | order | 注册条件 | 匹配条件 | 适配器 |
|--------|-------|----------|----------|--------|
| `RedissonTtlCacheDecorator` | 1000 | redis-mod（`@Component`） | unwrap 得到 `RedissonCache` | `RedissonAdaptTtlCache`（原生条目级过期） |
| `MultiLevelTtlCacheDecorator` | 1000 | 多级启用时（`MultiLevelCacheConfig`） | `instanceof AbstractMultiLevelCache` | `MultiLevelAdaptTtlCache` |
| `DefaultTtlCacheDecorator` | LOWEST_PRECEDENCE | 默认（`@Component`） | 全部（兜底） | `AdaptScheduleExpireTtlCache`（async 调度式过期） |

无装饰器匹配时，管理器抛出 `CRITICAL_SYSTEM_ERROR` 级异常（`No TTL cache decorator found for cache: {}`）。

### 2. 层级缓存 SPI（ILevelCacheProvider）

```java
public interface ILevelCacheProvider {
    default int getOrder() { return 0; }              // 越小越靠近应用层
    default boolean support(String cacheName) { return true; }
    Cache getCache(String name);
    default Cache getCache(String name, Duration ttl) { return getCache(name); }
    default void configure(Map<String, Object> props) {}
}
```

**内置实现**：

| 提供者 | order | 引擎 | TTL 行为（源码核实） |
|--------|-------|------|----------------------|
| `CaffeineLevelCacheProvider` | 0 | Caffeine（每 cacheName 独立实例） | 有效 TTL = 业务 TTL，为 null 时取全局 `expire-after-write` |
| `RedissonLevelCacheProvider` | 1000 | Redisson `RedissonSpringCacheManager`（L2） | 不绑定业务 TTL（warn 日志），建议用 `@ExpireOn` 过期 |

`RedissonLevelCacheProvider` 由 redis-mod 的 `RedissonCacheConfig` 在多级启用时注册（`@ConditionalOnProperty(tbs.cache.multi-level.enabled=true)`）。

### 3. TTL 策略（ICacheTtlPolicy）

```java
public interface ICacheTtlPolicy {
    Duration ttlOf(String cacheName);                    // 覆盖 > 默认
    Optional<Duration> findOverride(String cacheName);   // 仅查覆盖
    Duration defaultTtl();                               // 全局默认
}
```

默认实现 `DefaultCacheTtlPolicy(defaultTtl, overrides)`，由 `MultiLevelCacheConfig` 在多级启用时注册：默认 TTL 取 `tbs.cache.multi-level.default-ttl`，覆盖表取 `tbs.cache.ttl.overrides`。业务可注册自定义 `ICacheTtlPolicy` Bean（`@ConditionalOnMissingBean` 让位）。

### 4. @ExpireOn 主动过期

```java
@Retention(RUNTIME) @Target(METHOD)
public @interface ExpireOn {
    long value() default 0;              // 过期时长，<=0 时切面直接跳过
    TimeUnit timeUnit() default SECONDS;
    String[] cacheNames() default {};    // 显式声明：与注解操作的 cacheNames 取交集
    boolean expireAll() default false;   // true → 全缓存过期（key=null）
}
```

**必须与 `@Cacheable` 或 `@CachePut` 组合使用**（切点表达式同时匹配两个注解）。执行流程（`ExpireAop`）：

1. 方法执行后，从 `CacheAspectSupport` 获取该方法的缓存操作与生成的 key；
2. 经 `IThreadPoolManager` 异步提交过期任务（线程名 `expire-task`）；
3. 从管理器获取缓存，unwrap 出 `AbstractTtlCache`，调用 `callToExpire(key, expireTime)` / `callToExpireAll(expireTime)` 登记过期请求；
4. 该缓存的**下一次 `put`** 时检查过期标记并触发实际过期（`AbstractTtlCache.put` 内置检查）。

实际过期动作因适配器而异：

- `AdaptScheduleExpireTtlCache`（默认路径）：向 async 调度模块提交 `cache-expire` 类型任务（`CacheExpireTaskExecutor`），到点后经 `CacheManager.evict(key)` / `clear()` 删除；
- `RedissonAdaptTtlCache`：直接操作 Redisson 原生 TTL——单条目 `RMapCache.expireEntry(key, ttl, 0)`（已过期则 `remove`），全缓存 `expire(Duration)`；
- `MultiLevelAdaptTtlCache`：逐层下发过期，各层过期时刻可经 `IMultiLevelCacheTtlInterceptor` 调整（默认实现 `NoMultiLevelCacheTtlInterceptor` 原样放行）。

过期时间在登记时可经 `ITtlIntercepter` Bean 链调整（`onTtlExpired` 返回新时刻）。过期详情由 `IExpireDetailsManager` 管理，默认 `LocalMapExpireDetailsManager`（ConcurrentHashMap 内存实现，key=null 表示全缓存过期）。

```java
@Cacheable(cacheNames = "orderCache", key = "#orderId")
@ExpireOn(value = 30, timeUnit = TimeUnit.MINUTES, cacheNames = "orderCache")
public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId).orElse(null);
}

@CachePut(cacheNames = "reportCache", key = "#type")
@ExpireOn(value = 1, timeUnit = TimeUnit.HOURS, expireAll = true)   // 不显式声明 cacheNames 时取注解操作的缓存名
public Report generateReport(String type) {
    // ...
}
```

### 5. 缓存监听器（ICacheListenner / IListenable）

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

`AbstractTtlCache` 在 get/put/evict/clear/expire 时回调容器内全部 `ICacheListenner` Bean（按 `support()` 过滤，监听器异常被捕获记录、不影响缓存操作）：

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

### 6. 值序列化转换（@CacheTransform）

标注在**实体类**（`@Target(TYPE)`）上，指定 `IDataBridge` 实现：

```java
@CacheTransform(bridgeType = UserBridge.class)   // bridgeName 缺省时按类型获取 Bean
public class User { ... }
```

`AbstractTtlCache` 在 `get(Object key)` 时执行 `bridge.unbridge`（TRANSFORM_READ）、`put` 时执行 `bridge.bridge`（TRANSFORM_WRITE，null 跳过）。桥接器经 Spring 容器获取，解析结果缓存在 `CacheUtils` 手动缓存中（key 为 `CACHE_TRANSFORM_INFO` + 类名）。

### 7. CacheUtils 手动缓存

`@Component` + `IStartup`（启动时初始化静态单例），底层走 `IManualCacheHandler`（默认 `MapCacheHandler`，内存 HashMap），每次操作以 `LockUtil.lockThen(this)` 串行化，并联动操作记录器与淘汰限流器：

```java
// 注入或静态获取均可
@Resource
private CacheUtils cacheUtils;

// 基本操作
cacheUtils.put("key", value);                        // null 值直接忽略
ValueWrapper<User> wrapper = cacheUtils.get("key", User.class);
cacheUtils.remove("key");

// 不存在则计算（原子）
ValueWrapper<User> wrapper2 = cacheUtils.computeIfAbsent("key",
    () -> userService.findById(1), User.class);

// 注解方式（走 Spring Cache 链路，cacheNames 固定为 CACHE_UTILS_SET）
cacheUtils.cacheableSet("key", value);
Object val = cacheUtils.cacheableGet("key");
cacheUtils.cacheableEvict("key");
cacheUtils.cacheableEvictAll();
```

**淘汰限流**（`SimpleCacheLimiter`）：每 128 次手动缓存操作检查一次，当条目数超过 2048 时，按 `lastGetTime` 越早越先淘汰（从未被 get 的条目最后淘汰，同序按 putCount 少者先），一次多淘汰 `maxSize/8`（256）个，防止手动缓存无限增长。

## 配置参考

### 常规缓存（tbs.cache，TbsCacheProperty）

```yaml
tbs:
  cache:
    # 常规单级 CacheManager 实现类；默认 ConcurrentMapCacheManager，
    # 多级启用后此项闲置。Caffeine 场景示例：
    cache-manager-impl: org.springframework.cache.caffeine.CaffeineCacheManager
    # 以下组件均有默认实现，通常无需覆盖
    ttl-cache-decorator: com.tbs.cache.impl.DefaultTtlCacheDecorator
    recorder: com.tbs.cache.limit.impl.SimpleCacheOperationRecorder
    manual-cache-handler: com.tbs.cache.manual.impl.MapCacheHandler
    cache-limiter: com.tbs.cache.limit.impl.SimpleCacheLimiter
    ttl:
      overrides:                # per-cacheName TTL 覆盖（DefaultCacheTtlPolicy 使用）
        userCache: 30m
        dictCache: 24h
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `cache-manager-impl` | Class | `ConcurrentMapCacheManager` | 常规单级原始管理器类型（懒加载自 Spring 容器） |
| `ttl-cache-decorator` | Class | `DefaultTtlCacheDecorator` | 装饰器类型属性（装饰器链实际按容器 Bean 收集） |
| `recorder` | Class | `SimpleCacheOperationRecorder` | 手动缓存操作记录器 |
| `manual-cache-handler` | Class | `MapCacheHandler` | 手动缓存处理器 |
| `cache-limiter` | Class | `SimpleCacheLimiter` | 手动缓存淘汰限流器 |
| `ttl.overrides` | Map<String, Duration> | {} | 按**业务原始 cacheName**（不带 SYSTEM:CACHE: 前缀）覆盖 TTL |

### 多级缓存（tbs.cache.multi-level，MultiLevelCacheProperty）

```yaml
tbs:
  cache:
    multi-level:
      enabled: true      # 总开关，默认 false
      default-ttl: 1h    # 全局默认 TTL，未命中 overrides 时使用
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | false | 多级缓存总开关 |
| `default-ttl` | Duration | 1h | 默认 TTL；覆盖项配置在 `tbs.cache.ttl.overrides` |

### Caffeine 引擎（tbs.cache.level.caffeine，CaffeineLevelCacheProperty）

```yaml
tbs:
  cache:
    level:
      caffeine:
        maximum-size: 10000       # 每实例最大条目数（LRU 淘汰）
        expire-after-write: 60s   # 常规场景缓存级 TTL / 多级场景 L1 兜底 TTL
        record-stats: false       # 启用统计（轻微性能开销）
        allow-null-values: true   # 是否允许缓存 null
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `maximum-size` | long | 10000 | 超出后 LRU 淘汰 |
| `expire-after-write` | Duration | 60s | 常规场景作 `CaffeineCacheManager` 缓存级 TTL；多级场景作 L1 兜底（业务 TTL 为 null 时生效） |
| `record-stats` | boolean | false | 启用后 `cache.stats()` 可获取命中率 |
| `allow-null-values` | boolean | **true** | 允许缓存 null 标记 |

### Redisson 引擎（tbs.redission.cache，RedissonCacheProperty，redis-mod）

```yaml
tbs:
  redission:
    cache:
      cache-timeout: 3600     # 全局缓存超时（秒），RedissonTtlCacheDecorator 默认过期时长
      allow-null-values: false
      transaction: false
      max-idle-time: 1800     # 最大空闲时间（秒）
      max-size: 2048          # 最大条目数
      eviction-mode: LRU      # 驱逐模式
```

## 快速开始

### 1. 常规单级使用（Caffeine）

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

### 2. 多级缓存（Caffeine L1 + Redisson L2）

```yaml
tbs:
  cache:
    multi-level:
      enabled: true
      default-ttl: 1h
    ttl:
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

启用后自动组装 `L1 Caffeine（order=0）` → `L2 Redisson（order=1000）`。

## 扩展开发

### 自定义层级提供者

```java
@Component
public class HazelcastLevelCacheProvider implements ILevelCacheProvider {

    @Override
    public int getOrder() { return 500; } // L1.5，位于 Caffeine(0) 和 Redisson(1000) 之间

    @Override
    public Cache getCache(String name, Duration ttl) {
        // 创建 Hazelcast 缓存实例（可按 ttl 绑定引擎原生过期）
    }
}
```

注册为 Bean 即可被 `MultiLevelTtlCacheMananger` 自动收集。

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
        return new CustomAdaptTtlCache(cache);   // 继承 AbstractAdaptTtlCache 实现 expire/expireAll
    }

    @Override
    public int getOrder() { return 500; } // 先于兜底，后于 1000
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

### 自定义多级过期拦截器

实现 `MultiLevelAdaptTtlCache.IMultiLevelCacheTtlInterceptor` 并注册为 Bean（覆盖默认 `NoMultiLevelCacheTtlInterceptor`），可按层级（`level`）与 key 调整各层的实际过期时刻。

### 自定义过期详情管理器

实现 `IExpireDetailsManager` 并注册为 Bean（覆盖默认 `LocalMapExpireDetailsManager`），可将过期标记外置（如 Redis），实现集群一致的 `@ExpireOn` 过期。

## 最佳实践

1. **TTL 安全网**：各层引擎的全局 TTL 配置（Caffeine `expire-after-write`、Redisson `cache-timeout`）作为兜底，业务 TTL 缺失时兜底生效
2. **L1 < L2 存活**：L1 的 `expire-after-write` 应小于 L2 的 `cache-timeout`，避免读到过期数据
3. **per-cacheName TTL**：通过 `tbs.cache.ttl.overrides` 为高频变更的缓存设置较短 TTL（注意该配置在多级启用时才由 DefaultCacheTtlPolicy 消费）
4. **L2 业务 TTL**：`RedissonLevelCacheProvider` 不绑定业务 TTL，精细过期请用 `@ExpireOn`
5. **监控命中率**：启用 `record-stats: true` 配合 `ICacheListenner` 监控缓存健康度
6. **空值防护**：`allow-null-values: false`（Redisson 默认）时注意 `@Cacheable` 返回 null 的异常处理；Caffeine 默认允许 null
7. **事务安全**：`TransactionAwareCacheUnwrapperExtension` 自动解包 `TransactionAwareCacheDecorator`，装饰器匹配与特性获取无需业务处理
8. **手动缓存容量**：`CacheUtils` 手动缓存有 2048 条的淘汰上限与 128 次一检的限流节奏，超大容量场景请实现自定义 `IManualCacheHandler`

## 故障排除

| 问题 | 排查方向 |
|------|----------|
| 缓存不生效 | 检查 `@EnableTbsCache` 是否标注；检查 `tbs.cache.cache-manager-impl` 配置 |
| TTL 未生效（单级） | 确认对应引擎的装饰器 order 低于兜底装饰器；确认原始管理器类型正确 |
| 多级未生效 | 检查 `tbs.cache.multi-level.enabled=true`；确认 `ILevelCacheProvider` Bean 已注册 |
| 装饰器未匹配 | 管理器抛出 `No TTL cache decorator found`（CRITICAL 级），检查引擎依赖是否引入 |
| @ExpireOn 不触发 | 必须与 `@Cacheable`/`@CachePut` 组合；`value > 0`；过期在下一次 `put` 时触发（调度式路径还有调度延迟） |
| L1/L2 数据不一致 | 确认 L1 `expire-after-write` < L2 TTL；写路径远层失败会快速失败，检查 Redis 连接 |

启用调试日志：

```yaml
logging:
  level:
    com.tbs.cache: DEBUG
    com.tbs.cache.caffeine: DEBUG
    com.tbs.cache.redis: DEBUG
```

---

**最后更新**: 2026年9月
**版本**: 1.0-SNAPSHOT
