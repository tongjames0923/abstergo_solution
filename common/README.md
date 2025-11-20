# Common 通用工具包

通用工具包是项目的核心基础组件库，提供了一系列常用的工具类、配置和接口，用于简化开发流程，提高代码复用性和一致性。

## 目录结构

```
com.tbs.common
├── config                    # 配置类
│   ├── property              # 配置属性类
│   │   ├── DataHolderProperty.java
│   │   ├── DelegateProperty.java
│   │   ├── StartUpProperty.java
│   │   └── UuidProperty.java
│   ├── DataHolderConfig.java
│   ├── DelegateConfig.java
│   ├── StartUpConfig.java
│   └── UuidConfig.java
├── constant                  # 常量定义
│   └── DataHolderBeanNameConstant.java
├── context                   # 上下文接口
│   └── IContext.java
├── data                      # 数据持有者相关接口和实现
│   ├── impl                  # 实现类
│   │   ├── AbstractAntStyleMatchKeyFounder.java
│   │   ├── AbstractAutoGenDataHolderManager.java
│   │   ├── AbstractDataHolderManager.java
│   │   ├── DefaultAutoGenDataHodlerManager.java
│   │   ├── JsonTransformer.java
│   │   ├── MapDataHolder.java
│   │   └── SpringManagedDataHolderManager.java
│   ├── IDataBridge.java
│   ├── IDataHolder.java
│   ├── IDataHolderManager.java
│   ├── IFeatureWrapper.java
│   ├── IMatchKeyFounder.java
│   ├── IPartitionableDataHolder.java
│   ├── ITransformer.java
│   └── ValueWrapper.java
├── delegate                  # 代理模式相关接口和实现
│   ├── impl                  # 实现类
│   │   ├── CallerRunsDelegateTarget.java
│   │   ├── CompletableFutureDelegateFlag.java
│   │   ├── SimpleDelegateInvoker.java
│   │   └── SpringManagedDelegateTemplate.java
│   ├── provider              # 提供者接口和实现
│   │   ├── impl              # 实现类
│   │   │   ├── CompletableFutureFlagProvider.java
│   │   │   ├── SimpleCallerDelegateTargetProvider.java
│   │   │   └── SpringManagedTypeBaseInvokerProvider.java
│   │   ├── IDelegateFlagProvider.java
│   │   ├── IDelegateInvokerProvider.java
│   │   └── IDelegateTargetProvider.java
│   ├── AbstractDelegateInvoker.java
│   ├── IDelegateFlag.java
│   ├── IDelegateInvoker.java
│   ├── IDelegateTarget.java
│   └── IDelegateTemplate.java
├── error                     # 错误处理工具
│   ├── impl                  # 实现类
│   │   └── DefaultErrorTransformer.java
│   ├── AssertUtil.java
│   ├── ErrorLevelEnum.java
│   ├── ExceptionUtil.java
│   ├── IErrorThrowEvent.java
│   ├── IErrorTransformer.java
│   └── IExceptionLevel.java
├── exception                 # 自定义异常
│   └── FormattedRuntimeException.java
├── function                  # 函数式接口
│   ├── SBiConsumer.java
│   ├── SConsumer.java
│   ├── SFunction.java
│   └── SSupplier.java
├── util                      # 工具类
│   ├── base                  # 基础工具
│   │   ├── BeanUtil.java
│   │   ├── CollUtil.java
│   │   ├── ReflectUtil.java
│   │   └── StrUtil.java
│   ├── encrypt               # 加密工具
│   │   ├── AESUtil.java
│   │   ├── EncryptUitl.java
│   │   └── RSAUtil.java
│   ├── framework             # 框架工具
│   │   ├── DelegateUtil.java
│   │   ├── SpringUtil.java
│   │   └── UuidUtil.java
│   ├── function              # 函数式工具
│   │   ├── Comparators.java
│   │   ├── EnumUtil.java
│   │   └── LambdaUtil.java
│   ├── helper                # 辅助工具
│   │   ├── FeatureWrapperHelper.java
│   │   ├── IFeatureWrapperExtension.java
│   │   └── impl              # 实现类
│   │       ├── DirectCastWrapperExtension.java
│   │       └── FeatureWrapperExtension.java
│   └── time                  # 时间工具
│       └── TimeUtil.java
├── uuid                      # UUID生成器相关接口和实现
│   ├── impl                  # 实现类
│   │   ├── SimpleUUID.java
│   │   └── SnowFlakeUUID.java
│   └── IUuidGenerator.java
├── BaseEnum.java             # 枚举基类
└── IStartup.java             # 启动接口
```

## 核心组件详细说明

### 1. 基础组件

#### BaseEnum.java

```java
public interface BaseEnum<T> {
    T getValue(); // 获取枚举值
    int getCode(); // 获取枚举编码 
}
```

- 所有枚举需实现该接口
- 提供统一的枚举值获取方法
- 示例：

```java
public enum StatusEnum implements BaseEnum<String> {
    SUCCESS(0, "success"), 
    FAIL(1, "fail");
    
    private final int code;
    private final String value;
    
    StatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }
    
    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public int getCode() {
        return code;
    }
}
```

#### IStartup.java

```java
public interface IStartup extends Ordered {
    void startUp() throws RuntimeException;
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
```

- 应用启动后执行的初始化接口
- 实现该接口的类会在Spring启动完成后自动执行`startUp()`方法
- 通过`getOrder()`方法控制执行顺序

### 2. 配置体系

#### 配置属性类

##### UuidProperty.java

```java
@ConfigurationProperties("tbs.uuid")
@Data
public class UuidProperty {
    private Class<? extends IUuidGenerator> type = SimpleUUID.class;
    private long snowFlakeWorkerId = 0;
    private long snowFlakeDataCenterId = 0;
    private int exceptLength = 32;
}
```

##### DataHolderProperty.java

```java
@ConfigurationProperties(prefix = "tbs.data")
@Data
public class DataHolderProperty {
    private Class<? extends IDataHolderManager> dataHolderManager = SpringManagedDataHolderManager.class;
    private Class<? extends IDataHolder> dataHolder = MapDataHolder.class;
}
```

##### DelegateProperty.java

```java
@ConfigurationProperties("tbs.delegate")
@Data
public class DelegateProperty {
    private Class<? extends IDelegateTargetProvider> delegateTargetProvider = SimpleCallerDelegateTargetProvider.class;
    private Class<? extends IDelegateFlagProvider> delegateFlagProvider = CompletableFutureFlagProvider.class;
}
```

##### StartUpProperty.java

```java
@ConfigurationProperties("tbs.startup")
@Data
public class StartUpProperty {
    private boolean enableAutoStartup = true;
    private int startupOrder = Ordered.LOWEST_PRECEDENCE;
}
```

#### 配置类

##### UuidConfig.java

```java
@Configuration
public class UuidConfig {
    @Resource
    private UuidProperty uuidProperty;

    @Bean
    public IUuidGenerator mainUuidGenerator() {
        if (uuidProperty.getType() == SnowFlakeUUID.class) {
            return new SnowFlakeUUID(uuidProperty.getSnowFlakeWorkerId(), uuidProperty.getSnowFlakeDataCenterId());
        } else {
            return BeanUtil.buildBeanFromProperties(() -> {
                return new SnowFlakeUUID();
            }, uuidProperty.getType(), BeanUtil::buildByEmptyArgs);
        }
    }
}
```

### 3. 数据持有者(DataHolder)

#### IDataHolder.java

数据存储接口，定义了通用的数据存储功能，提供了数据的增删改查、清空、状态检查等操作。

```java
public interface IDataHolder {
    boolean set(String key, Object value, boolean force);
    <T> Optional<T> get(String key);
    void remove(String key);
    void clear();
    boolean isEmpty();
    int size();
    boolean containsKey(String key);
    Collection<Map.Entry<String, Object>> entries();
    Collection<String> keys();
    Collection<Object> values();
    
    default <T> T computeIfAbsent(String key, SFunction<String, T> function) {
        if (containsKey(key)) {
            return (T) get(key);
        } else {
            T value = function.apply(key);
            set(key, value, true);
            return value;
        }
    }
}
```

#### 实现类

##### MapDataHolder.java

基于Map的数据存储实现类，使用LinkedHashMap作为底层存储结构。

```java
public class MapDataHolder extends AbstractAntStyleMatchKeyFounder implements IDataHolder {
    private Map<String, Object> map;
    
    public MapDataHolder(Map<String, Object> map) {
        AssertUtil.notNull(map, "map is null");
        this.map = map;
    }
    
    public MapDataHolder() {
        this.map = new LinkedHashMap<>(8);
    }
    
    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            map.put(key, value);
            return true;
        }
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }
    
    @Override
    public <T> Optional<T> get(String key) {
        if (map.containsKey(key)) {
            Object val = map.get(key);
            return Optional.ofNullable((T)val);
        }
        return Optional.empty();
    }
    
    // 其他方法实现...
}
```

##### SpringManagedDataHolderManager.java

Spring管理的数据持有者管理器，支持自动装配和生命周期管理。

### 4. 代理模式(Delegate)

#### 核心接口

##### IDelegateInvoker.java

代理执行器接口，定义了代理模式中执行器的功能规范。

```java
public interface IDelegateInvoker<T> {
    void setTarget(IDelegateTarget<T> delegateTarget);
    IDelegateTarget<T> getTarget();
    
    <P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p);
    <P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer);
    
    IDelegateFlag<T> executeOnce();
    Collection<IDelegateFlag<T>> executeAll();
    int waittingCount();
}
```

##### IDelegateTarget.java

代理目标接口，定义了代理操作的目标对象。

##### IDelegateFlag.java

代理执行标记接口，用于跟踪和管理代理任务的执行状态和结果。

#### 实现类

##### SimpleDelegateInvoker.java

简单代理执行器实现类，直接在调用者线程中同步执行代理任务。

```java
public class SimpleDelegateInvoker<T> extends AbstractDelegateInvoker<T> {
    @Resource
    private IDelegateFlagProvider delegateFlagProvider;
    
    @Override
    protected IDelegateFlagProvider getDelegateFlagProvider() {
        return delegateFlagProvider;
    }
    
    @Override
    protected <P> void executeTakeIn(IDelegateFlag<T> flag, SBiConsumer<T, P> method, SSupplier<P> p) {
        getTarget().write(method, p);
        flag.setFinished(this);
    }
    
    @Override
    protected <P> void executeTakeOut(IDelegateFlag<T> flag, SFunction<T, P> method, SConsumer<P> consumer) {
        getTarget().read(method, consumer);
        flag.setFinished(this);
    }
}
```

##### CallerRunsDelegateTarget.java

在调用者线程中执行的代理目标实现。

##### CompletableFutureDelegateFlag.java

基于CompletableFuture的异步代理执行标记实现。

### 5. 错误处理体系

#### AssertUtil.java

增强型断言工具类，提供丰富的断言方法和错误级别支持。

```java
@Component
public class AssertUtil implements IStartup {
    private static Collection<IErrorTransformer> errorTransformers;
    private static List<IErrorThrowEvent> errorThrowEvents;
    
    @Override
    public void startUp() throws RuntimeException {
        errorTransformers = SpringUtil.getBeansByTypeSafe(IErrorTransformer.class).stream()
            .sorted(Comparators::asc)
            .collect(Collectors.toList());
        errorThrowEvents = SpringUtil.getBeansByTypeSafe(IErrorThrowEvent.class).stream()
            .collect(Collectors.toList());
    }
    
    public static void notNull(Object object, String msg, Object... params) {
        notNull(object, FormattedRuntimeException::new, msg, params);
    }
    
    public static void notNull(Object object, Function<String, ? extends RuntimeException> exceptionSupplier, 
                              String message, Object... params) {
        isTrue(object == null, exceptionSupplier, message, params);
    }
    
    public static void isTrue(boolean expression, String message, Object... params) {
        isTrue(expression, FormattedRuntimeException::new, message, params);
    }
    
    public static void isTrue(boolean expression, Function<String, ? extends RuntimeException> exceptionSupplier, 
                             String message, Object... params) {
        if (expression) {
            throwError(() -> exceptionSupplier.apply(StrUtil.format(message, params)));
        }
    }
    
    public static <T extends RuntimeException> void throwError(Supplier<T> exceptionSupplier) {
        T e = exceptionSupplier.get();
        if (errorThrowEvents.stream().anyMatch(event -> event.onAssertUtilThrow(e))) {
            return;
        }
        throw e;
    }
    
    // 更多断言方法...
}
```

#### ErrorLevelEnum.java

错误级别枚举，定义了不同级别的错误类型。

```java
public enum ErrorLevelEnum implements BaseEnum<String> {
    USER_INPUT_ERROR("user_input_error", "用户输入错误"),
    BUSINESS_LOGIC_ERROR("business_logic_error", "业务逻辑错误"),
    SYSTEM_ERROR("system_error", "系统错误"),
    CRITICAL_SYSTEM_ERROR("critical_system_error", "严重系统错误");
    
    private final String value;
    private final String description;
    
    ErrorLevelEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getCode() {
        return ordinal();
    }
}
```

#### FormattedRuntimeException.java

格式化运行时异常，支持参数化错误消息和错误级别。

### 6. 工具类矩阵

#### 加密工具

| 工具类 | 功能 | 算法 | 主要方法 |
|--------|------|------|----------|
| AESUtil | 对称加密 | AES/CBC/PKCS5Padding | `encrypt(String data, String key)`, `decrypt(String encryptedData, String key)` |
| RSAUtil | 非对称加密 | RSA | `encrypt(String data, String publicKey)`, `decrypt(String encryptedData, String privateKey)` |
| EncryptUitl | 哈希加密 | SHA-128/256/512 | `sha256(String data)`, `sha512(String data)` |

#### 基础工具

##### StrUtil.java

字符串工具类，提供字符串操作相关功能。

```java
public class StrUtil {
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    public static String format(String template, Object... params) {
        if (params == null || params.length == 0) {
            return template;
        }
        return String.format(template.replace("{}", "%s"), params);
    }
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    // 更多字符串方法...
}
```

##### CollUtil.java

集合工具类，提供集合操作相关功能。

```java
public class CollUtil {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }
    
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }
    
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
    
    // 更多集合方法...
}
```

##### BeanUtil.java

Bean工具类，提供Bean操作相关功能。

```java
public class BeanUtil {
    public static <T> T buildBeanFromProperties(Supplier<T> defaultSupplier, 
                                               Class<? extends T> targetClass, 
                                               Function<Class<? extends T>, T> builder) {
        if (targetClass == null) {
            return defaultSupplier.get();
        }
        return builder.apply(targetClass);
    }
    
    public static <T> T buildByEmptyArgs(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }
    
    // 更多Bean方法...
}
```

#### 函数式工具

##### LambdaUtil.java

Lambda表达式解析工具，用于从Lambda表达式中提取对应的Field、Method或字段名。

```java
public class LambdaUtil {
    private static final ConcurrentMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    
    public static <T, R> Field getField(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String key = buildCacheKey(lambda);
            
            return FIELD_CACHE.computeIfAbsent(key, k -> {
                try {
                    String methodName = lambda.getImplMethodName();
                    String fieldName;
                    if (methodName.startsWith("get") || methodName.startsWith("set")) {
                        fieldName = Introspector.decapitalize(methodName.substring(3));
                    } else if (methodName.startsWith("is")) {
                        fieldName = Introspector.decapitalize(methodName.substring(2));
                    } else {
                        fieldName = Introspector.decapitalize(methodName);
                    }
                    
                    Class<?> targetClass = Class.forName(lambda.getImplClass().replace('/', '.'));
                    return targetClass.getDeclaredField(fieldName);
                } catch (Exception e) {
                    AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
                }
                return null;
            });
        } catch (Exception e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
        }
        return null;
    }
    
    public static <T, R> String extractFieldName(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String key = buildCacheKey(lambda);
            
            return FIELD_NAME_CACHE.computeIfAbsent(key, k -> {
                String methodName = lambda.getImplMethodName();
                if (methodName.startsWith("get") || methodName.startsWith("set")) {
                    return Introspector.decapitalize(methodName.substring(3));
                } else if (methodName.startsWith("is")) {
                    return Introspector.decapitalize(methodName.substring(2));
                }
                return methodName;
            });
        } catch (Exception e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
        }
        return null;
    }
    
    // 更多Lambda方法...
}
```

##### EnumUtil.java

枚举工具类，提供枚举操作相关功能。

#### 框架工具

##### UuidUtil.java

统一UUID生成入口工具类。

```java
@Component
public class UuidUtil {
    @Getter
    private static UuidUtil instance;
    @Resource
    private IUuidGenerator uuidGenerator;
    @Resource
    private UuidProperty uuidProperty;
    
    public UuidUtil() {
        instance = this;
    }
    
    public String getUuidString() {
        return uuidGenerator.getUuidString(uuidProperty.getExceptLength());
    }
}
```

##### SpringUtil.java

Spring工具类，提供Spring上下文操作相关功能。

##### DelegateUtil.java

代理工具类，提供代理模式相关工具方法。

### 7. UUID生成体系

#### IUuidGenerator.java

UUID生成器接口，定义了UUID生成的标准方法。

```java
public interface IUuidGenerator {
    byte[] getUuid(int exceptLength);
    String getUuidString(int exceptLength);
}
```

#### 实现类

##### SimpleUUID.java

简单UUID生成器，基于哈希算法生成UUID。

```java
public class SimpleUUID implements IUuidGenerator {
    @Override
    public byte[] getUuid(int exceptLength) {
        return getUuidString(exceptLength).getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public String getUuidString(int exceptLength) {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString().replace("-", "");
        if (uuidStr.length() > exceptLength) {
            return uuidStr.substring(0, exceptLength);
        }
        return uuidStr;
    }
}
```

##### SnowFlakeUUID.java

雪花算法UUID生成器，支持分布式环境下的有序唯一ID生成。

```java
public class SnowFlakeUUID implements IUuidGenerator {
    private final long twepoch = 1598598185157L; // 开始时间截 (2020-08-28)
    private final long workerIdBits = 5L;        // 机器id所占的位数
    private final long datacenterIdBits = 5L;    // 数据标识id所占的位数
    private final long sequenceBits = 12L;       // 序列在id中占的位数
    
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);        // 支持的最大机器id
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 支持的最大数据标识id
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);       // 生成序列的掩码
    
    private final long workerIdShift = sequenceBits;                     // 机器ID向左移12位
    private final long datacenterIdShift = sequenceBits + workerIdBits;  // 数据标识id向左移17位
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits; // 时间截向左移22位
    
    private long workerId;      // 工作机器ID(0~31)
    private long datacenterId;  // 数据中心ID(0~31)
    private long sequence = 0L; // 毫秒内序列(0~4095)
    private long lastTimestamp = -1L; // 上次生成ID的时间截
    
    public SnowFlakeUUID(long workerId, long datacenterId) {
        AssertUtil.isTrue(!(workerId <= maxWorkerId && workerId >= 0),
            "worker Id can t be greater than %d or less than 0", maxWorkerId);
        AssertUtil.isTrue(!(datacenterId <= maxDatacenterId && datacenterId >= 0),
            "datacenter Id can t be greater than %d or less than 0", maxDatacenterId);
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        AssertUtil.isTrue(timestamp < lastTimestamp,
            "Clock moved backwards.  Refusing to generate id for {} milliseconds", timestamp - lastTimestamp);
        
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        long id = ((timestamp - twepoch) << timestampLeftShift)
            | (datacenterId << datacenterIdShift)
            | (workerId << workerIdShift)
            | sequence;
        return id;
    }
    
    @Override
    public String getUuidString(int exceptLength) {
        Long uuid = 0L;
        StringBuilder uuidBuilder = new StringBuilder();
        while (uuidBuilder.length() < exceptLength) {
            uuid = nextId();
            uuidBuilder.append(Long.toHexString(uuid));
        }
        return uuidBuilder.substring(0, exceptLength);
    }
    
    // 其他方法...
}
```

#### 雪花算法特性

- **时间戳位数**：41位（69年时间范围）
- **机器ID位数**：5位（最多32台机器）
- **数据中心位数**：5位（最多32个数据中心）
- **序列号位数**：12位（每毫秒4096个序列号）
- **总位数**：64位（长整型）

#### 策略对比

| 策略 | 生成方式 | 特性 | 适用场景 |
|------|----------|------|----------|
| SimpleUUID | 哈希算法 | 简单快速，无序 | 一般场景，单机应用 |
| SnowFlakeUUID | 雪花算法 | 有序唯一，分布式 | 分布式系统，高并发场景 |

## 配置说明

### application.yml配置示例

```yaml
tbs:
  data:
    data-holder-manager: com.tbs.common.data.impl.SpringManagedDataHolderManager
    data-holder: com.tbs.common.data.impl.MapDataHolder
  delegate:
    delegate-target-provider: com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider
    delegate-flag-provider: com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider
  uuid:
    type: com.tbs.common.uuid.impl.SnowFlakeUUID
    snow-flake-worker-id: 0
    snow-flake-data-center-id: 0
    except-length: 32
  startup:
    enable-auto-startup: true
    startup-order: -2147483648
```

### Maven依赖配置

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 最佳实践

### 1. 数据持有者使用模式

```java
// 获取数据持有者
IDataHolder dataHolder = new MapDataHolder();

// 存储数据
dataHolder.set("user", user, true);

// 获取数据
Optional<User> user = dataHolder.get("user");

// 使用computeIfAbsent避免重复计算
User cachedUser = dataHolder.computeIfAbsent("user", key -> loadUserFromDB());

// 批量操作
Collection<Map.Entry<String, Object>> allData = dataHolder.entries();
Collection<String> allKeys = dataHolder.keys();
Collection<Object> allValues = dataHolder.values();
```

### 2. 代理模式使用模式

```java
// 创建代理目标
IDelegateTarget<User> target = new CallerRunsDelegateTarget<>(user);

// 创建代理执行器
IDelegateInvoker<User> invoker = new SimpleDelegateInvoker<>();
invoker.setTarget(target);

// 执行代理操作
IDelegateFlag<User> flag = invoker
    .takeIn(User::setName, () -> "newName")
    .takeOut(User::getName, name -> System.out.println("Name: " + name))
    .executeOnce();

// 等待执行完成
IDelegateTarget<User> result = flag.get();

// 批量执行
Collection<IDelegateFlag<User>> flags = invoker.executeAll();
```

### 3. 断言工具使用模式

```java
// 基础断言
AssertUtil.notNull(user, "用户对象不能为空");
AssertUtil.isTrue(user.getAge() > 0, "用户年龄必须大于0");
AssertUtil.hasText(user.getName(), "用户名不能为空");
AssertUtil.notEmpty(userList, "用户列表不能为空");

// 带错误级别的断言
AssertUtil.notNullWithLevel(user, ErrorLevelEnum.BUSINESS_LOGIC_ERROR, "用户对象不能为空");
AssertUtil.isTrueSystemError(condition, "系统条件不满足");

// 使用断言构建器
AssertUtil.AssertBuilder builder = AssertUtil.isTrue(condition)
    .message("条件不满足")
    .params("param1", "param2")
    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR);
if (builder.isBuild()) {
    builder.throwIt();
}
```

### 4. UUID生成使用模式

```java
// 获取UUID
String uuid = UuidUtil.getUuidString();

// 直接使用生成器
IUuidGenerator generator = new SnowFlakeUUID(1, 1);
String snowflakeUuid = generator.getUuidString(32);

// 简单UUID生成
IUuidGenerator simpleGenerator = new SimpleUUID();
String simpleUuid = simpleGenerator.getUuidString(36);
```

### 5. Lambda工具使用模式

```java
// 提取字段对象
Field field = LambdaUtil.getField(User::getName);

// 提取字段名
String fieldName = LambdaUtil.extractFieldName(User::getName);

// 提取方法对象
Method method = LambdaUtil.getMethod(User::getName);

// 提取方法名
String methodName = LambdaUtil.extractMethodName(User::getName);
```

## 注意事项

### 1. 线程安全

- **MapDataHolder**：非线程安全，多线程环境下需要使用同步机制
- **SnowFlakeUUID**：线程安全，使用`synchronized`保证原子性
- **AssertUtil**：线程安全，静态方法支持并发调用

### 2. 资源管理

- 数据持有者需要手动管理生命周期
- 代理执行器会自动管理执行标记
- UUID生成器无需特殊资源管理

### 3. 异常处理

- 建议实现`IErrorThrowEvent`接口进行全局异常捕获
- 使用`FormattedRuntimeException`提供结构化的错误信息
- 通过`ErrorLevelEnum`区分不同级别的错误

### 4. 性能考虑

- **SnowFlakeUUID**：高性能，适合高并发场景
- **LambdaUtil**：使用缓存提高性能
- **AssertUtil**：轻量级，对性能影响小

### 5. 配置建议

- 分布式环境下合理配置SnowFlakeUUID的节点ID
- 根据业务需求选择合适的DataHolder实现
- 调整代理模式的执行策略以适应不同场景

## 版本历史

- **v1.0.0**：初始版本，包含基础工具类和框架组件
- 支持数据持有者模式
- 支持代理模式
- 提供丰富的工具类矩阵
- 实现雪花算法UUID生成器

## 贡献指南

欢迎提交Issue和Pull Request来改进这个通用工具包。在提交代码时，请确保：

1. 遵循现有的代码风格
2. 添加适当的单元测试
3. 更新相关文档
4. 通过所有现有的测试

## 许可证

本项目采用MIT许可证，详情请参阅LICENSE文件。
