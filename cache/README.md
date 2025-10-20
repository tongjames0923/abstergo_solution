# Cache 缓存模块

缓存模块是基于Spring Cache的扩展实现，提供了带TTL（Time To Live）功能的缓存管理器，支持缓存过期处理和监听功能。

## 目录结构

```
com.tbs.cache
├── config                  # 配置类
│   └── TbsCacheProperty.java # 缓存属性配置
├── impl                    # 实现类
│   ├── AdaptScheduleExpireTtlCache.java          # 适配调度过期TTL缓存
│   ├── DefaultTtlAdaptCacheMananger.java         # 默认TTL适配缓存管理器
│   └── DefaultTtlCacheDecorator.java             # 默认TTL缓存装饰器
├── integration             # 集成类
│   ├── AbstractAdaptTtlCache.java                # 抽象适配TTL缓存
│   ├── AbstractTtlAdaptCacheMananger.java        # 抽象TTL适配缓存管理器
│   └── AbstractTtlCache.java                     # 抽象TTL缓存
├── expire                  # 过期处理相关
│   ├── ICacheExpiredActuator.java                # 缓存过期执行器接口
│   └── IExpiredActuator.java                     # 通用过期执行器接口
├── limit                   # 缓存限制相关
│   ├── ICacheLimiter.java                        # 缓存限制器接口
│   ├── ICacheOperationRecorder.java              # 缓存操作记录器接口
│   ├── impl                                      # 限制器实现
│   │   ├── SimpleCacheLimiter.java               # 简单缓存限制器
│   │   └── SimpleCacheOperationRecorder.java     # 简单缓存操作记录器
├── manual                  # 手动缓存相关
│   ├── IManualCacheHandler.java                  # 手动缓存处理器接口
│   └── impl                                      # 手动缓存实现
│       └── MapCacheHandler.java                  # Map缓存处理器
├── serialize               # 序列化相关
│   └── annotation                                # 序列化注解
│       └── CacheTransform.java                   # 缓存转换注解
├── CacheUtils.java         # 缓存工具类
├── ICacheListenner.java    # 缓存监听器接口
├── IListenable.java        # 可监听接口
└── ITtlCacheDecorator.java # TTL缓存装饰器接口
```

## 核心组件说明

### 1. 配置类

#### TbsCacheProperty 配置属性

```java
@ConfigurationProperties(prefix = "tbs.cache")
@Component
@Data
public class TbsCacheProperty {
    private Class<? extends CacheManager> cacheManagerImpl = ConcurrentMapCacheManager.class;
    private Class<? extends ITtlCacheDecorator> ttlCacheDecorator = DefaultTtlCacheDecorator.class;
    private Class<? extends ICacheOperationRecorder> recorder = SimpleCacheOperationRecorder.class;
    private Class<? extends IManualCacheHandler> manualCacheHandler = MapCacheHandler.class;
    private Class<? extends ICacheLimiter> cacheLimiter = SimpleCacheLimiter.class;
}
```

#### TbsCacheConfig 配置

注册缓存相关的Bean，包括缓存管理器、TTL缓存装饰器等。

### 2. 核心实现

#### AbstractTtlAdaptCacheMananger 抽象TTL缓存管理器

抽象类，实现了CacheManager接口，提供了TTL功能的缓存管理基础实现。

#### DefaultTtlAdaptCacheMananger 默认TTL适配缓存管理器

默认的TTL缓存管理器实现，基于Spring CacheManager实现。

#### ITtlCacheDecorator TTL缓存装饰器接口

TTL缓存装饰器接口，用于装饰原始缓存以添加TTL功能。

#### DefaultTtlCacheDecorator 默认TTL缓存装饰器

默认的TTL缓存装饰器实现。

### 3. 缓存限制

#### ICacheLimiter 缓存限制器接口

缓存限制器接口，用于限制缓存的使用。

#### ICacheOperationRecorder 缓存操作记录器接口

缓存操作记录器接口，用于记录缓存操作。

### 4. 手动缓存

#### IManualCacheHandler 手动缓存处理器接口

手动缓存处理器接口，用于手动操作缓存。

#### MapCacheHandler Map缓存处理器

基于Map的手动缓存处理器实现。

### 5. 监听器

#### ICacheListenner 缓存监听器接口

提供缓存操作的监听功能：

- `onGet(String cacheName, Object key, Object value)`: 缓存获取事件
- `onPut(String cacheName, Object key, Object value)`: 缓存设置事件
- `onEvict(String cacheName, Object key)`: 缓存删除事件
- `onClear(String cacheName)`: 缓存清除事件

### 6. 工具类

#### CacheUtils 缓存工具类

[CacheUtils](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/api/src/main/java/com/tbs/cache/CacheUtils.java#L16-L110) 提供了静态方法来操作缓存，包括获取、设置、删除缓存等操作。它实现了[IStartup](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/IStartup.java#L11-L18)接口，在应用启动时初始化。

主要方法：
- `get(String key, Class<T> clazz)`: 获取缓存值
- `put(String key, T value)`: 设置缓存值
- `computeIfAbsent(String key, SSupplier<T> sSupplier, Class<T> clazz)`: 如果缓存中不存在则计算并放入缓存
- `remove(String key)`: 删除缓存

使用示例：
```java
// 获取缓存
ValueWrapper<User> userWrapper = CacheUtils.getInstance().get("user_123", User.class);
User user = userWrapper.orElse(null);

// 设置缓存
CacheUtils.getInstance().put("user_123", user);

// 计算并缓存（如果不存在）
ValueWrapper<User> userWrapper = CacheUtils.getInstance().computeIfAbsent("user_123", () -> {
    return userService.getUserById(123);
}, User.class);

// 删除缓存
CacheUtils.getInstance().remove("user_123");
```

### 7. 序列化

#### CacheTransform 注解

[CacheTransform](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/api/src/main/java/com/tbs/cache/serialize/annotation/CacheTransform.java#L12-L16) 注解用于指定缓存数据的转换方式，通过实现[IDataBridge](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/data/IDataBridge.java#L10-L20)接口来定义数据的序列化和反序列化方式。

## 配置说明

### application.yml配置示例

```yaml
tbs:
  cache:
    cache-manager-impl: org.springframework.cache.concurrent.ConcurrentMapCacheManager
    ttl-cache-decorator: com.tbs.cache.impl.DefaultTtlCacheDecorator
    recorder: com.tbs.cache.limit.impl.SimpleCacheOperationRecorder
    manual-cache-handler: com.tbs.cache.manual.impl.MapCacheHandler
    cache-limiter: com.tbs.cache.limit.impl.SimpleCacheLimiter
```

## 使用示例

### 1. 基本缓存使用

```java
@Service
public class UserService {

    @Cacheable("users")
    public User getUserById(Long id) {
        // 从数据库获取用户
        return userRepository.findById(id);
    }

    @CacheEvict("users")
    public void updateUser(User user) {
        // 更新用户信息
        userRepository.save(user);
    }
}
```

### 2. 自定义缓存管理器

```java
@Configuration
public class CustomCacheConfig {

    @Bean
    public CacheManager customCacheManager() {
        return new DefaultTtlAdaptCacheMananger(ConcurrentMapCacheManager.class);
    }
}
```

### 3. 实现缓存监听器

```java
@Component
public class CustomCacheListener implements ICacheListenner {

    @Override
    public boolean support(Cache cache) {
        return "users".equals(cache.getName());
    }

    @Override
    public void onGet(String cacheName, Object key, Object value) {
        log.info("Cache get: name={}, key={}, value={}", cacheName, key, value);
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) {
        log.info("Cache put: name={}, key={}, value={}", cacheName, key, value);
    }

    @Override
    public void onEvict(String cacheName, Object key) {
        log.info("Cache evict: name={}, key={}", cacheName, key);
    }

    @Override
    public void onClear(String cacheName) {
        log.info("Cache clear: name={}", cacheName);
    }
}
```

### 4. 使用CacheUtils工具类

```java
@Component
public class BusinessService {
    
    public User getUserWithCache(Long userId) {
        // 使用CacheUtils获取缓存
        ValueWrapper<User> userWrapper = CacheUtils.getInstance().get("user_" + userId, User.class);
        if (!userWrapper.isEmpty()) {
            return userWrapper.get();
        }
        
        // 缓存未命中，从数据库获取
        User user = userRepository.findById(userId);
        if (user != null) {
            // 将结果放入缓存
            CacheUtils.getInstance().put("user_" + userId, user);
        }
        return user;
    }
    
    public void updateUser(User user) {
        // 更新数据库
        userRepository.save(user);
        // 删除缓存
        CacheUtils.getInstance().remove("user_" + user.getId());
    }
}
```

## 最佳实践

1. **合理设置缓存策略**：根据业务需求选择合适的缓存管理器实现
2. **使用监听器监控缓存**：通过实现[ICacheListenner](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/api/src/main/java/com/tbs/cache/ICacheListenner.java#L12-L54)接口监控缓存操作，便于问题排查和性能优化
3. **选择合适的缓存管理器**：根据业务需求选择合适的底层缓存实现，如ConcurrentMap、Redis等
4. **避免缓存雪崩**：为不同数据设置不同的过期时间，避免大量缓存同时过期

## 注意事项

1. **线程安全**：缓存实现保证了线程安全性，但在监听器实现中需要注意线程安全问题
2. **异常处理**：缓存操作中的异常不会影响主流程，但应记录日志便于排查
3. **资源释放**：在应用关闭时，确保缓存资源得到正确释放
4. **性能考虑**：监听器中的操作应尽量轻量，避免影响缓存性能