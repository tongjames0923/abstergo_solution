# Common 通用工具包

通用工具包是项目的核心基础组件库，提供了一系列常用的工具类、配置和接口，用于简化开发流程，提高代码复用性和一致性。

## 目录结构

```
com.tbs.common
├── config                    # 配置类
│   ├── property              # 配置属性类
│   │   ├── DataHolderProperty.java
│   │   ├── DelegateProperty.java
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
│   ├── IDataHolderFeatureWrapper.java
│   ├── IDataHolderManager.java
│   ├── IFeatureWrapper.java
│   ├── IMatchKeyFounder.java
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
│   ├── AssertUtil.java
│   ├── ExceptionUtil.java
│   └── IErrorThrowEvent.java
├── exception                 # 自定义异常
│   └── FormattedRuntimeException.java
├── function                  # 函数式接口
│   ├── SBiConsumer.java
│   ├── SConsumer.java
│   ├── SFunction.java
│   └── SSupplier.java
├── tree                      # 树结构相关接口和实现
│   ├── impl                  # 实现类
│   │   ├── MultiwayTree.java
│   │   └── SimpleTreeNode.java
│   ├── AbstractTree.java
│   ├── ITree.java
│   ├── ITreeNode.java
│   └── ITreeNode.java
├── util                      # 工具类
│   ├── encrypt               # 加密工具
│   │   ├── AESUtil.java
│   │   ├── EncryptUitl.java
│   │   └── RSAUtil.java
│   ├── framework             # 框架工具
│   │   ├── DelegateUtil.java
│   │   └── UuidUtil.java
│   ├── BeanUtil.java
│   ├── CollUtil.java
│   ├── EnumUtil.java
│   ├── LambdaUtil.java
│   ├── ReflectUtil.java
│   ├── SpringUtil.java
│   ├── StrUtil.java
│   └── TimeUtil.java
├── uuid                      # UUID生成器相关接口和实现
│   ├── impl                  # 实现类
│   │   ├── SimpleUUID.java
│   │   └── SnowFlakeUUID.java
│   └── IUuidGenerator.java
├── BaseEnum.java             # 枚举基类
└── IStartup.java             # 启动接口
```

## 核心组件说明

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
- 实现该接口的类会在Spring启动完成后自动执行[startUp()](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/IStartup.java#L15-L15)方法
- 通过[getOrder()](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/IStartup.java#L23-L25)方法控制执行顺序

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

### 3. 数据持有者(DataHolder)

#### IDataHolder.java

数据存储接口，定义了通用的数据存储功能，提供了数据的增删改查、清空、状态检查等操作。

主要方法：
- `boolean set(String key, Object value, boolean force)` - 存储数据
- `<T> Optional<T> get(String key)` - 获取数据
- `void remove(String key)` - 删除数据
- `void clear()` - 清空数据
- `boolean containsKey(String key)` - 检查是否包含某个key
- `Collection<Map.Entry<String, Object>> entries()` - 获取所有数据

#### 实现类

- [MapDataHolder](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/data/impl/MapDataHolder.java#L25-L89) - 基于Map的数据存储实现
- [SpringManagedDataHolderManager](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/data/impl/SpringManagedDataHolderManager.java#L15-L22) - Spring管理的数据持有者管理器

### 4. 代理模式(Delegate)

#### 核心接口

- [IDelegateTarget](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/delegate/IDelegateTarget.java#L16-L23) - 代理目标接口
- [IDelegateInvoker](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/delegate/IDelegateInvoker.java#L17-L105) - 代理执行器接口
- [IDelegateFlag](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/delegate/IDelegateFlag.java#L16-L67) - 代理执行标记接口

#### 特色实现

- [CompletableFutureDelegateFlag](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/delegate/impl/CompletableFutureDelegateFlag.java#L33-L75) - 基于CompletableFuture的异步代理执行标记
- [CallerRunsDelegateTarget](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/delegate/impl/CallerRunsDelegateTarget.java#L13-L107) - 在调用者线程中执行的代理目标

### 5. 树结构(Tree)

#### 核心接口

- [ITree](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/tree/ITree.java#L12-L94) - 树接口
- [ITreeNode](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/tree/ITreeNode.java#L9-L54) - 树节点接口

#### 实现类

- [MultiwayTree](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/tree/impl/MultiwayTree.java#L14-L56) - 多叉树实现
- [SimpleTreeNode](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/tree/impl/SimpleTreeNode.java#L11-L63) - 简单树节点实现

### 6. 工具类矩阵

#### 加密工具

| 工具类 | 功能 | 算法 |
|--------|------|------|
| [AESUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/encrypt/AESUtil.java#L22-L177) | 对称加密 | AES/CBC/PKCS5Padding |
| [RSAUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/encrypt/RSAUtil.java#L20-L196) | 非对称加密 | RSA |
| [EncryptUitl](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/encrypt/EncryptUitl.java#L15-L101) | 哈希加密 | SHA-128/256/512 |

#### 特色工具

- [AssertUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/error/AssertUtil.java#L26-L216)：增强型断言工具

```java
AssertUtil.notNull(obj, "对象不能为空"); 
AssertUtil.isTrue(condition, "条件不满足");
```

- [LambdaUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/LambdaUtil.java#L16-L155)：Lambda表达式解析

```java
Field field = LambdaUtil.getField(User::getName); // 提取字段对象
String fieldName = LambdaUtil.extractFieldName(User::getName); // 提取字段名
```

- [UuidUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/framework/UuidUtil.java#L12-L30)：统一UUID生成入口

```java
String uuid = UuidUtil.getUuidString(); // 获取配置的UUID
```

- [StrUtil](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/util/StrUtil.java#L12-L91)：字符串工具类

```java
String formatted = StrUtil.format("Hello, {}!", "World"); // 格式化字符串
String hex = StrUtil.bytesToHex(bytes); // 字节数组转十六进制
```

### 7. UUID生成体系

#### 策略对比

| 策略 | 生成方式 | 特性 | 适用场景 |
|------|----------|------|----------|
| [SimpleUUID](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/uuid/impl/SimpleUUID.java#L11-L25) | 哈希算法 | 简单快速 | 一般场景 |
| [SnowFlakeUUID](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/uuid/impl/SnowFlakeUUID.java#L13-L134) | 雪花算法 | 有序唯一 | 分布式系统 |

#### 雪花算法特性

- 时间戳位数：41位
- 机器ID位数：5位
- 数据中心位数：5位
- 序列号位数：12位

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
    type: com.tbs.common.uuid.impl.SimpleUUID
    snow-flake-worker-id: 0
    snow-flake-data-center-id: 0
    except-length: 32
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
```

### 3. 树结构使用模式

```java
// 创建多叉树
ITree<String, Object> tree = new MultiwayTree<>();

// 添加节点
ITreeNode<String, Object> root = new SimpleTreeNode<>("root", "Root Node");
tree.setRoot(root);

ITreeNode<String, Object> child = new SimpleTreeNode<>("child1", "Child Node 1");
tree.insert("root", child);

// 遍历树
tree.traverse(node -> System.out.println(node.getValue()));
```

## 注意事项

1. **线程安全**：在多线程环境中使用时，请选择合适的实现类
2. **资源释放**：某些实现类需要手动管理生命周期，请注意资源释放
3. **函数序列化**：使用函数式接口时需确保实现类可序列化
4. **异常处理**：建议实现[IErrorThrowEvent](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/error/IErrorThrowEvent.java#L8-L25)接口进行全局异常捕获
5. **UUID冲突**：分布式环境下建议使用[SnowFlakeUUID](file:///Users/abstergo/IdeaProjects/abstergo_solution/common/src/main/java/com/tbs/common/uuid/impl/SnowFlakeUUID.java#L13-L134)并合理配置节点ID