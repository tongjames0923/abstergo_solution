# Cache 缓存解决方案

## 概述

Cache模块是一个基于Spring Cache框架的增强缓存解决方案，提供了TTL（Time To Live）缓存、缓存操作监控、缓存限制等高级功能。该模块采用模块化设计，分为API接口层和实现层，支持灵活的配置和扩展。

## 核心特性

### 1. TTL缓存支持
- 支持基于时间的缓存过期机制
- 可配置的过期时间管理
- 支持单个缓存项和批量缓存项的过期控制

### 2. 缓存操作监控
- 完整的缓存操作记录（put/get/remove）
- 操作次数统计和时间戳记录
- 异步操作记录，不影响主业务流程

### 3. 缓存限制器
- 可配置的缓存执行限制
- 防止缓存滥用和过度使用
- 与操作记录器协同工作

### 4. 手动缓存处理器
- 提供手动缓存操作接口
- 支持多种缓存存储后端（默认使用HashMap）
- 线程安全的缓存操作

### 5. 缓存监听器
- 支持缓存事件的监听
- 可扩展的监听器接口
- 支持多种缓存操作事件

## 模块结构

```
cache/
├── cache-api/          # API接口定义模块
│   ├── src/main/java/com/tbs/cache/
│   │   ├── ICacheListenner.java          # 缓存监听器接口
│   │   ├── ITtlCacheDecorator.java       # TTL缓存装饰器接口
│   │   ├── CacheUtils.java               # 缓存工具类
│   │   ├── manual/                       # 手动缓存处理
│   │   ├── limit/                        # 缓存限制相关
│   │   ├── expire/                       # 过期处理
│   │   └── integration/                  # 集成相关
│   └── pom.xml
├── cache-impl/         # 实现模块
│   ├── src/main/java/com/tbs/cache/
│   │   ├── impl/                         # 具体实现类
│   │   └── config/                       # 配置类
│   └── pom.xml
└── pom.xml             # 主pom文件
```

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>cache</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置缓存

在Spring Boot配置文件中添加缓存配置：

```yaml
tbs:
  cache:
    cache-manager-impl: org.springframework.cache.concurrent.ConcurrentMapCacheManager
    ttl-cache-decorator: com.tbs.cache.impl.DefaultTtlCacheDecorator
    recorder: com.tbs.cache.limit.impl.SimpleCacheOperationRecorder
    manual-cache-handler: com.tbs.cache.manual.impl.MapCacheHandler
    cache-limiter: com.tbs.cache.limit.impl.SimpleCacheLimiter
```

### 3. 启用缓存

在配置类上添加注解：

```java
@Configuration
@EnableCaching
public class CacheConfig {
    // 配置将自动生效
}
```

## 核心API使用

### 缓存工具类 (CacheUtils)

```java
// 获取缓存实例
CacheUtils cacheUtils = CacheUtils.getInstance();

// 放入缓存
cacheUtils.put("user:1", user);

// 获取缓存
ValueWrapper<User> userWrapper = cacheUtils.get("user:1", User.class);
if (userWrapper.isPresent()) {
    User user = userWrapper.get();
}

// 计算并缓存（如果不存在）
ValueWrapper<User> userWrapper = cacheUtils.computeIfAbsent(
    "user:1", 
    () -> userService.findById(1), 
    User.class
);

// 删除缓存
cacheUtils.remove("user:1");
```

### 手动缓存处理器 (IManualCacheHandler)

```java
@Autowired
private IManualCacheHandler manualCacheHandler;

// 基本缓存操作
manualCacheHandler.put("key", "value");
Object value = manualCacheHandler.get("key");
boolean exists = manualCacheHandler.contains("key");
manualCacheHandler.remove("key");
Collection<String> keys = manualCacheHandler.keys();
```

### TTL缓存功能

```java
// 获取TTL缓存装饰器
@Autowired
private ITtlCacheDecorator ttlCacheDecorator;

// 装饰缓存为TTL缓存
AbstractTtlCache ttlCache = ttlCacheDecorator.decorateCache(cache, cacheManager);

// 设置缓存项过期时间
ttlCache.callToExpire("key", new Date(System.currentTimeMillis() + 3600000)); // 1小时后过期

// 设置所有缓存项过期时间
ttlCache.callToExpireAll(new Date(System.currentTimeMillis() + 1800000)); // 30分钟后全部过期
```

## 高级功能

### 缓存操作记录

缓存操作记录器会记录每个缓存键的操作统计信息：

```java
@Autowired
private ICacheOperationRecorder recorder;

// 获取缓存操作视图
ICacheOperationView operationView = recorder.getCacheOperationView(handler, "key");

// 查看操作统计
int putCount = operationView.getPutCount();      // 存入次数
int getCount = operationView.getGetCount();      // 获取次数
int removeCount = operationView.getRemoveCount(); // 删除次数
Date lastPutTime = operationView.getLastPutTime(); // 最后存入时间
```

### 缓存监听器

实现自定义缓存监听器来监听缓存事件：

```java
@Component
public class CustomCacheListener implements ICacheListenner {
    
    @Override
    public boolean support(Cache cache) {
        return cache.getName().startsWith("user");
    }
    
    @Override
    public void onGet(String cacheName, Object key, Object value) {
        log.info("缓存获取: {} - {}", cacheName, key);
    }
    
    @Override
    public void onPut(String cacheName, Object key, Object value) {
        log.info("缓存设置: {} - {}", cacheName, key);
    }
    
    @Override
    public void onEvict(String cacheName, Object key) {
        log.info("缓存删除: {} - {}", cacheName, key);
    }
    
    @Override
    public void onClear(String cacheName) {
        log.info("缓存清空: {}", cacheName);
    }
}
```

### 缓存限制器

缓存限制器可以防止缓存滥用：

```java
@Autowired
private ICacheLimiter cacheLimiter;

// 限制器会在缓存操作时自动执行限制逻辑
// 可自定义实现复杂的限制策略
```

## 配置选项

### TbsCacheProperty 配置属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| cacheManagerImpl | Class | ConcurrentMapCacheManager | 缓存管理器实现类 |
| ttlCacheDecorator | Class | DefaultTtlCacheDecorator | TTL缓存装饰器实现类 |
| recorder | Class | SimpleCacheOperationRecorder | 缓存操作记录器实现类 |
| manualCacheHandler | Class | MapCacheHandler | 手动缓存处理器实现类 |
| cacheLimiter | Class | SimpleCacheLimiter | 缓存限制器实现类 |

## 扩展开发

### 自定义缓存处理器

```java
@Component
public class CustomCacheHandler implements IManualCacheHandler {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    @Override
    public Object get(String key) {
        return cache.get(key);
    }
    
    // 实现其他接口方法...
}
```

### 自定义TTL缓存装饰器

```java
@Component
public class CustomTtlCacheDecorator implements ITtlCacheDecorator {
    
    @Override
    public AbstractTtlCache decorateCache(Cache cache, CacheManager manager) {
        return new CustomTtlCache(cache);
    }
}
```

## 依赖关系

- **Spring Boot Starter Cache**: 提供基础的缓存功能
- **Common模块**: 提供通用工具和基础类
- **Async模块**: 提供异步执行支持
- **Spring Boot Starter AOP**: 提供AOP支持

## 最佳实践

1. **合理设置TTL**: 根据业务需求设置合适的缓存过期时间
2. **监控缓存命中率**: 使用操作记录器监控缓存使用情况
3. **避免缓存穿透**: 使用computeIfAbsent方法防止缓存穿透
4. **合理使用缓存监听器**: 用于业务监控和日志记录
5. **配置缓存限制**: 防止缓存被过度使用

## 故障排除

### 常见问题

1. **缓存不生效**: 检查是否启用了@EnableCaching注解
2. **TTL功能无效**: 确认TTL缓存装饰器配置正确
3. **操作记录为空**: 检查异步配置和线程池设置

### 日志调试

启用调试日志来查看缓存操作详情：

```yaml
logging:
  level:
    com.tbs.cache: DEBUG
```

## 版本信息

- **当前版本**: 1.0-SNAPSHOT
- **Java版本**: 11+
- **Spring Boot**: 兼容版本

## 贡献指南

欢迎提交Issue和Pull Request来改进这个缓存解决方案。
