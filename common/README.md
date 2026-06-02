# Common 模块 - 通用工具包

## 模块概述

Common模块是一个企业级Java通用工具包，为Abstergo解决方案平台提供核心基础组件支持。该包封装了项目中常用的工具类、配置管理、数据持有、代理模式、错误处理等通用功能，旨在提高代码复用性、降低开发复杂度、统一技术规范。

## 特性

- **模块化设计**：清晰的模块划分，便于按需使用
- **Spring Boot 集成**：无缝集成 Spring Boot 框架，支持自动配置
- **企业级质量**：完善的错误处理、类型安全、性能优化
- **可扩展架构**：基于接口设计，支持自定义实现
- **文档完善**：详细的代码注释和使用示例

## 快速开始

### 依赖引入

在 Maven 项目中添加依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 基础配置

在 `application.yml` 中配置：

```yaml
tbs:
  uuid:
    type: com.tbs.common.uuid.impl.SnowFlakeUUID
    snow-flake-worker-id: 1
    snow-flake-data-center-id: 1
    except-length: 32
  
  data:
    data-holder-manager: com.tbs.common.data.impl.SpringManagedDataHolderManager
    data-holder: com.tbs.common.data.impl.MapDataHolder
  
  delegate:
    delegate-target-provider: com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider
    delegate-flag-provider: com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider
  
  startup:
    enable-auto-startup: true
    startup-order: 0
```

## 核心模块详解

### 1. 基础接口

#### BaseEnum - 枚举基类

提供统一的枚举接口规范，所有业务枚举都应实现此接口：

```java
public enum UserStatus implements BaseEnum<String> {
    ACTIVE(1, "active"),
    INACTIVE(0, "inactive"),
    DELETED(-1, "deleted");
    
    private final Integer code;
    private final String value;
    
    UserStatus(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
    
    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public Integer getCode() {
        return code;
    }
}
```

#### IStartup - 启动接口

Spring 应用启动后执行的初始化任务接口：

```java
@Component
public class SystemInitializer implements IStartup {
    
    @Override
    public void startUp() throws RuntimeException {
        // 系统初始化逻辑
        initCache();
        loadConfig();
    }
    
    @Override
    public int getOrder() {
        return 0; // 执行顺序，数值越小优先级越高
    }
}
```

### 2. 配置体系

#### 配置属性类

- **UuidProperty**: UUID 生成器配置
- **DataHolderProperty**: 数据持有者配置  
- **DelegateProperty**: 代理模式配置
- **StartUpProperty**: 启动配置

#### 自动配置类

Spring Boot 自动配置，无需手动创建 Bean：

- **UuidConfig**: UUID 生成器自动配置
- **DataHolderConfig**: 数据持有者自动配置
- **DelegateConfig**: 代理模式自动配置
- **StartUpConfig**: 启动组件自动配置

### 3. 数据持有者 (DataHolder)

#### 核心接口

- **IDataHolder**: 数据存储接口，提供键值对存储功能
- **IDataHolderManager**: 数据持有者管理器接口
- **IDataBridge**: 数据桥接接口，支持数据转换
- **IFeatureWrapper**: 功能包装器接口

#### 使用示例

```java
@Service
public class UserService {
    
    @Resource
    private IDataHolder dataHolder;
    
    public void cacheUserInfo(User user) {
        // 存储用户信息
        dataHolder.set("user:" + user.getId(), user, true);
    }
    
    public Optional<User> getUserInfo(String userId) {
        // 获取用户信息
        return dataHolder.get("user:" + userId);
    }
}
```

#### 实现类

- **MapDataHolder**: 基于 Map 的内存数据存储
- **SpringManagedDataHolderManager**: Spring 托管的数持者管理器
- **JsonTransformer**: JSON 数据转换器

### 4. 代理模式 (Delegate)

#### 核心概念

代理模式提供了一种解耦调用者和执行者的机制，支持同步/异步执行、结果回调等功能。

#### 核心接口

- **IDelegateInvoker**: 代理执行器
- **IDelegateTarget**: 代理目标
- **IDelegateFlag**: 代理执行标记
- **IDelegateTemplate**: 代理模板

#### 使用示例

```java
@Service
public class OrderService {
    
    @Resource
    private IDelegateTemplate delegateTemplate;
    
    public void processOrderAsync(Order order) {
        // 异步处理订单
        IDelegateInvoker<OrderProcessor> invoker = delegateTemplate.createInvoker(OrderProcessor.class);
        
        invoker.takeIn(OrderProcessor::process, () -> order)
               .executeOnce();
    }
    
    public CompletableFuture<OrderResult> processOrderWithResult(Order order) {
        // 异步处理并获取结果
        IDelegateInvoker<OrderProcessor> invoker = delegateTemplate.createInvoker(OrderProcessor.class);
        
        CompletableFuture<OrderResult> future = new CompletableFuture<>();
        
        invoker.takeOut(OrderProcessor::processWithResult, future::complete)
               .executeOnce();
        
        return future;
    }
}
```

#### 实现类

- **SimpleDelegateInvoker**: 简单代理执行器（同步）
- **CompletableFutureDelegateFlag**: 基于 CompletableFuture 的异步标记
- **CallerRunsDelegateTarget**: 调用者线程执行目标

### 5. 错误处理体系

#### AssertUtil - 增强断言工具

提供丰富的断言方法和错误级别支持：

```java
@Service
public class UserService {
    
    public User getUser(String userId) {
        // 参数校验
        AssertUtil.notNull(userId, "用户ID不能为空");
        AssertUtil.isTrue(userId.length() > 0, "用户ID格式错误");
        
        User user = userRepository.findById(userId);
        
        // 业务校验
        AssertUtil.notNull(user, "用户不存在: {}", userId);
        AssertUtil.isTrue(user.isActive(), "用户已被禁用");
        
        return user;
    }
}
```

#### ErrorLevelEnum - 错误级别

定义不同级别的错误类型：

- **USER_INPUT_ERROR**: 用户输入错误
- **BUSINESS_LOGIC_ERROR**: 业务逻辑错误  
- **SYSTEM_ERROR**: 系统错误
- **CRITICAL_SYSTEM_ERROR**: 严重系统错误

#### FormattedRuntimeException - 格式化异常

支持参数化错误消息：

```java
throw new FormattedRuntimeException("用户 {} 不存在", userId);
```

### 6. 工具类矩阵

#### 加密工具

| 工具类 | 功能 | 主要方法 |
|--------|------|----------|
| **AESUtil** | AES 对称加密 | `encrypt(data, key)`, `decrypt(encryptedData, key)` |
| **RSAUtil** | RSA 非对称加密 | `encrypt(data, publicKey)`, `decrypt(encryptedData, privateKey)` |
| **EncryptUitl** | 哈希加密 | `sha256(data)`, `sha512(data)` |

#### 基础工具

**StrUtil - 字符串工具**

```java
// 字符串判空
if (StrUtil.isEmpty(text)) {
    throw new IllegalArgumentException("文本不能为空");
}

// 格式化字符串
String message = StrUtil.format("用户 {} 的年龄是 {}", userName, age);

// 字节数组转十六进制
String hex = StrUtil.bytesToHex(bytes);
```

**CollUtil - 集合工具**

```java
// 集合判空
if (CollUtil.isEmpty(users)) {
    return Collections.emptyList();
}

// 创建集合
List<String> names = CollUtil.newArrayList("Alice", "Bob", "Charlie");
Map<String, User> userMap = CollUtil.newHashMap();
```

**BeanUtil - Bean 工具**

```java
// 根据配置创建 Bean
IUuidGenerator generator = BeanUtil.buildBeanFromProperties(
    () -> new SimpleUUID(), 
    uuidProperty.getType(), 
    BeanUtil::buildByEmptyArgs
);
```

#### 函数式工具

**LambdaUtil - Lambda 表达式工具**

```java
// 从 Lambda 表达式获取字段名
String fieldName = LambdaUtil.extractFieldName(User::getName);

// 获取字段对象
Field nameField = LambdaUtil.getField(User::getName);
```

**EnumUtil - 枚举工具**

```java
// 根据编码获取枚举
UserStatus status = EnumUtil.getByCode(UserStatus.class, 1);

// 根据值获取枚举  
UserStatus status = EnumUtil.getByValue(UserStatus.class, "active");
```

#### 框架工具

**SpringUtil - Spring 上下文工具**

```java
// 获取 Spring Bean
UserService userService = SpringUtil.getBean(UserService.class);

// 获取所有指定类型的 Bean
Collection<IErrorTransformer> transformers = SpringUtil.getBeansByTypeSafe(IErrorTransformer.class);
```

**UuidUtil - UUID 工具**

```java
// 生成 UUID
String uuid = UuidUtil.generate();

// 生成指定长度的 UUID
String shortUuid = UuidUtil.generate(16);
```

### 7. UUID 生成器

#### 核心接口

**IUuidGenerator**: UUID 生成器接口

#### 实现类

- **SimpleUUID**: 基于 UUID.randomUUID() 的简单实现
- **SnowFlakeUUID**: 雪花算法实现，支持分布式环境

#### 使用示例

```java
@Service
public class OrderService {
    
    @Resource
    private IUuidGenerator uuidGenerator;
    
    public String createOrder(OrderRequest request) {
        String orderId = uuidGenerator.generate();
        // 创建订单逻辑
        return orderId;
    }
}
```

## 最佳实践

### 1. 错误处理规范

```java
@Service
public class UserService {
    
    public User updateUser(User user) {
        try {
            // 参数校验
            AssertUtil.notNull(user, "用户信息不能为空");
            AssertUtil.notNull(user.getId(), "用户ID不能为空");
            
            // 业务逻辑
            User existingUser = userRepository.findById(user.getId());
            AssertUtil.notNull(existingUser, "用户不存在: {}", user.getId());
            
            return userRepository.save(user);
            
        } catch (FormattedRuntimeException e) {
            // 业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 系统异常，包装后抛出
            throw ExceptionUtil.newException(e, "更新用户失败");
        }
    }
}
```

### 2. 数据持有者使用

```java
@Service
public class CacheService {
    
    @Resource
    private IDataHolder dataHolder;
    
    public <T> T getOrCreate(String key, Supplier<T> supplier) {
        return dataHolder.computeIfAbsent(key, k -> supplier.get());
    }
    
    public void clearUserCache(String userId) {
        // 清除用户相关缓存
        dataHolder.remove("user:" + userId);
        dataHolder.remove("user_permissions:" + userId);
        dataHolder.remove("user_profile:" + userId);
    }
}
```

### 3. 代理模式应用

```java
@Service  
public class NotificationService {
    
    @Resource
    private IDelegateTemplate delegateTemplate;
    
    public void sendNotifications(List<Notification> notifications) {
        IDelegateInvoker<NotificationSender> invoker = 
            delegateTemplate.createInvoker(NotificationSender.class);
        
        // 批量发送通知
        for (Notification notification : notifications) {
            invoker.takeIn(NotificationSender::send, () -> notification);
        }
        
        // 异步执行
        Collection<IDelegateFlag<NotificationSender>> flags = invoker.executeAll();
        
        // 等待所有任务完成（可选）
        flags.forEach(flag -> {
            try {
                flag.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

## 配置说明

### 完整配置示例

```yaml
tbs:
  # UUID 配置
  uuid:
    type: com.tbs.common.uuid.impl.SnowFlakeUUID  # UUID 生成器类型
    snow-flake-worker-id: 1                       # 雪花算法工作节点ID
    snow-flake-data-center-id: 1                  # 雪花算法数据中心ID
    except-length: 32                             # 期望的UUID长度
  
  # 数据持有者配置
  data:
    data-holder-manager: com.tbs.common.data.impl.SpringManagedDataHolderManager  # 数据持有者管理器
    data-holder: com.tbs.common.data.impl.MapDataHolder                           # 数据持有者实现
  
  # 代理模式配置
  delegate:
    delegate-target-provider: com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider  # 代理目标提供者
    delegate-flag-provider: com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider        # 代理标记提供者
  
  # 启动配置
  startup:
    enable-auto-startup: true    # 是否启用自动启动
    startup-order: 0             # 启动顺序（数值越小优先级越高）
```

## 扩展开发

### 自定义数据持有者

```java
@Component
public class RedisDataHolder implements IDataHolder {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }
    
    @Override
    public <T> Optional<T> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((T) value);
    }
    
    // 其他方法实现...
}
```

### 自定义错误转换器

```java
@Component
public class CustomErrorTransformer implements IErrorTransformer {
    
    @Override
    public RuntimeException transform(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            return (BusinessException) throwable;
        }
        
        // 自定义错误转换逻辑
        return new FormattedRuntimeException("系统异常: {}", throwable.getMessage());
    }
    
    @Override
    public int getOrder() {
        return 0; // 转换器执行顺序
    }
}
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
