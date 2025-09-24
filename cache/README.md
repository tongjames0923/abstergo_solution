# Cache 缓存模块

缓存模块是基于Spring Cache的扩展实现，提供了带TTL（Time To Live）功能的缓存管理器，支持缓存过期处理和监听功能。

## 目录结构

```
com.tbs.cache
├── annotation              # 缓存注解
│   └── ExpireOn.java       # 缓存过期注解
├── aop                     # AOP切面处理
│   └── ExpireAop.java      # 缓存过期切面
├── config                  # 配置类
│   ├── CacheConfig.java    # 缓存配置
│   └── CacheProperty.java  # 缓存属性配置
├── constant                # 常量定义
│   └── CacheBeanNameConstant.java  # 缓存Bean名称常量
└── core                    # 核心实现
    ├── expire              # 过期处理相关
    │   ├── AbstractScheduleExpiredActuator.java      # 抽象调度过期执行器
    │   ├── AbstractSpringScheduleExpiredActuator.java # Spring调度过期执行器
    │   ├── ICacheExpiredActuator.java                # 缓存过期执行器接口
    │   └── IExpiredActuator.java                     # 通用过期执行器接口
    ├── impl                # 实现类
    │   ├── LocalExpireActuatorCache.java             # 本地过期执行器缓存
    │   └── PropertyInitTtlAwareAdaptCacheManager.java # 属性初始化TTL缓存管理器
    ├── manager             # 缓存管理器
    │   ├── AbstractTtlAwareAdaptCacheManager.java    # 抽象TTL缓存管理器
    │   ├── TtlAdaptCache.java                        # TTL适配缓存
    │   └── holder           # 持有者相关
    │       ├── DataHolderCache.java                  # 数据持有者缓存
    │       └── DataHolderCacheManager.java           # 数据持有者缓存管理器
    └── ICacheListenner.java  # 缓存监听器接口
```

## 核心组件说明

### 1. 注解和AOP

#### @ExpireOn 注解

用于标记方法的缓存过期时间，支持以下属性：

- `value()`: 过期时间值，默认为0
- `timeUnit()`: 时间单位，默认为秒
- `cacheNames()`: 缓存名称数组
- `expireAll()`: 是否使所有缓存过期，默认为false

使用示例：

```java

@Cacheable("users")
@ExpireOn(value = 30, timeUnit = TimeUnit.MINUTES)
public User getUserById(Long id) {
    // 方法实现
}
```

#### ExpireAop 切面

处理[@ExpireOn](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/annotation/ExpireOn.java#L12-L45)
注解的AOP切面，自动为缓存设置过期时间。

### 2. 配置类

#### CacheProperty 配置属性

```java

@ConfigurationProperties(prefix = "tbs.cache")
@Data
public class CacheProperty {
    private Class<? extends ICacheExpiredActuator> expiredActuator = LocalExpireActuatorCache.class;
    private Class<? extends CacheManager> cacheManagerImplementation = ConcurrentMapCacheManager.class;
    private boolean allowNullValues = false;
}
```

#### CacheConfig 配置

注册缓存相关的Bean，包括过期执行器、缓存管理器等。

### 3. 核心实现

#### TtlAdaptCache TTL适配缓存

扩展Spring的Cache接口，添加了TTL（Time To Live）功能：

- `setTTL(Object key, Date d)`: 设置缓存的过期时间
- `getExpireTime(Object key)`: 获取缓存的过期时间
- `expireAll(Date expireTime)`: 使所有缓存过期

#### AbstractTtlAwareAdaptCacheManager 抽象TTL缓存管理器

抽象类，实现了CacheManager接口，提供了TTL功能的缓存管理基础实现。

#### 过期执行器

- [ICacheExpiredActuator](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/core/expire/ICacheExpiredActuator.java#L11-L23):
  缓存过期执行器接口
- [AbstractScheduleExpiredActuator](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/core/expire/AbstractScheduleExpiredActuator.java#L14-L78):
  抽象调度过期执行器
- [LocalExpireActuatorCache](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/core/impl/LocalExpireActuatorCache.java#L12-L42):
  本地过期执行器缓存

### 4. 监听器

#### ICacheListenner 缓存监听器接口

提供缓存操作的监听功能：

- `onGet(String cacheName, Object key, Object value)`: 缓存获取事件
- `onPut(String cacheName, Object key, Object value)`: 缓存设置事件
- `onEvict(String cacheName, Object key)`: 缓存删除事件
- `onClear(String cacheName)`: 缓存清除事件

## 配置说明

### application.yml配置示例

```yaml
tbs:
  cache:
    expired-actuator: com.tbs.cache.core.impl.LocalExpireActuatorCache
    cache-manager-implementation: org.springframework.cache.concurrent.ConcurrentMapCacheManager
    allow-null-values: false
```

## 使用示例

### 1. 基本缓存使用

```java

@Service
public class UserService {

    @Cacheable("users")
    @ExpireOn(value = 30, timeUnit = TimeUnit.MINUTES)
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
        return new PropertyInitTtlAwareAdaptCacheManager();
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

## 最佳实践

1. **合理设置过期时间**：根据数据变化频率设置合适的过期时间，避免数据不一致或缓存命中率过低
2. **使用监听器监控缓存**
   ：通过实现[ICacheListenner](file:///Users/abstergo/IdeaProjects/abstergo_solution/cache/src/main/java/com/tbs/cache/core/ICacheListenner.java#L12-L54)
   接口监控缓存操作，便于问题排查和性能优化
3. **选择合适的缓存管理器**：根据业务需求选择合适的底层缓存实现，如ConcurrentMap、Redis等
4. **避免缓存雪崩**：为不同数据设置不同的过期时间，避免大量缓存同时过期

## 注意事项

1. **线程安全**：缓存实现保证了线程安全性，但在监听器实现中需要注意线程安全问题
2. **异常处理**：过期执行器中的异常不会影响主流程，但应记录日志便于排查
3. **资源释放**：在应用关闭时，确保缓存资源得到正确释放
4. **性能考虑**：监听器中的操作应尽量轻量，避免影响缓存性能
