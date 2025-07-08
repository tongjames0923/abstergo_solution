# Common 模块开发文档

## 一、目录结构

```
com.tbs.common 
├── config # 配置类 
│ ├── property # 配置属性类 
│ └── HolderConfig.java # 上下文配置 
│ └── UuidConfig.java # UUID配置 
├── constant # 常量定义 
│ └── HolderBeanNameConstant.java # Bean名称常量 
├── exception # 异常体系 
│ ├── FormattedRuntimeException.java # 格式化运行时异常 
│ └── TimeoutException.java # 超时异常 
├── function # 函数式接口 
│ ├── SBiConsumer.java # 可序列化的BiConsumer 
│ ├── SConsumer.java # 可序列化的Consumer 
│ ├── SFunction.java # 可序列化的Function 
│ └── SSupplier.java # 可序列化的Supplier 
├── holder # 上下文管理 
│ ├── context # 上下文接口与实现 
│ │ ├── IContext.java # 上下文接口 
│ │ ├── AbstractNamedContext.java # 抽象命名上下文 
│ │ ├── LocalStoredContext.java # 本地存储上下文 
│ │ └── ThreadLocalStoredContext.java # 线程存储上下文 
│ ├── impl # 上下文持有者实现 
│ │ ├── ImportedContextHolder.java # 导入上下文持有者 
│ │ └── SpringManagedContextHolder.java # Spring管理的上下文持有者 
│ └── IContextHolder.java # 上下文持有者接口 
├── util # 工具类 
│ ├── AESUtil.java # AES加密解密 
│ ├── AssertUtil.java # 断言工具 
│ ├── BeanUtil.java # Bean操作工具 
│ ├── CollUtil.java # 集合工具 
│ ├── EncryptUitl.java # 加密工具 
│ ├── EnumUtil.java # 枚举工具 
│ ├── ExceptionUtil.java # 异常工具 
│ ├── HolderUtil.java # 上下文工具 
│ ├── LambdaUtil.java # Lambda表达式工具 
│ ├── RSAUtil.java # RSA加密解密 
│ ├── SpringUtil.java # Spring工具 
│ ├── StrUtil.java # 字符串工具 
│ └── UuidUtil.java # UUID工具 
├── uuid # UUID生成 
│ ├── impl # UUID实现 
│ │ ├── SimpleUUID.java # 简单UUID生成器 
│ │ └── SnowFlakeUUID.java # 雪花算法UUID 
│ └── IUuidGenerator.java # UUID生成接口 
├── BaseEnum.java # 枚举基类 
└── IErrorThrowEvent.java # 异常抛出事件接口
```

## 二、核心组件说明

### 1. 基础组件

#### [BaseEnum.java](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\BaseEnum.java)

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
public enum StatusEnum implements BaseEnum<String> {SUCCESS(0, "success"), FAIL(1, "fail");}
```

#### [IErrorThrowEvent.java](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\IErrorThrowEvent.java)

```java
public interface IErrorThrowEvent {
    boolean onAssertUtilThrow(Throwable throwable);
}
``` 

- 异常抛出事件监听接口
- 支持异常拦截处理
- 可通过Spring注入实现自定义异常处理逻辑

### 2. 配置体系

#### [HolderProperty.java](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\config\property\HolderProperty.java)

```java

@ConfigurationProperties(prefix = "tbs.holder")
public class HolderProperty {
    private Class<? extends IContextHolder> mainContextHolderType;
    private boolean useLocalStoredContext; // 是否启用本地存储 
    private boolean useThreadLocalStoredContext; // 是否启用线程存储 
}
```

#### `UuidProperty.java`

```java

@ConfigurationProperties("tbs.uuid")
@Data
public class UuidProperty {
    private Class<? extends IUuidGenerator> type = SimpleUUID.class;
    private long snowFlakeWorkerId; // 雪花算法工作节点ID 
    private long snowFlakeDataCenterId; // 雪花算法数据中心ID 
    private int exceptLength = 32; // UUID期望长度 
}
  ```

### 3. 上下文管理

#### 上下文实现类

| 类名                       | 特性                 | 适用场景  |
|--------------------------|--------------------|-------|
| LocalStoredContext       | 基于HashMap的本地存储     | 单线程环境 |
| ThreadLocalStoredContext | 基于ThreadLocal的线程存储 | 多线程环境 |

#### 上下文持有者

- [ImportedContextHolder](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\holder\impl\ImportedContextHolder.java#L15-L71)
  ：手动管理的上下文容器
- [SpringManagedContextHolder](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\holder\impl\SpringManagedContextHolder.java#L17-L52)
  ：Spring托管的上下文容器

### 4. 工具类矩阵

#### 加密工具

| 工具类         | 功能    | 算法                   |
|-------------|-------|----------------------|
| AESUtil     | 对称加密  | AES/CBC/PKCS5Padding |
| RSAUtil     | 非对称加密 | RSA                  |
| EncryptUitl | 哈希加密  | SHA-128/256/512      |

#### 特色工具

- [AssertUtil](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\util\AssertUtil.java#L26-L216)
  ：增强型断言工具

 ```java
AssertUtil.notNull(obj, "对象不能为空"); 
AssertUtil.isTrue(condition, "条件不满足");
```

- [LambdaUtil](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\util\LambdaUtil.java#L16-L155)
  ：Lambda表达式解析

```java
Field field = LambdaUtil.getField(User::getName); // 提取字段对象
String fieldName = LambdaUtil.extractFieldName(User::getName); // 提取字段名
```

- [UuidUtil](file://C:\Users\tongj\IdeaProjects\abstergo_solution\common\src\main\java\com\tbs\common\util\UuidUtil.java#L12-L30)
  ：统一UUID生成入口
  ```java
   String uuid = UuidUtil.getUuidString(); // 获取配置的UUID
  ```

### 5. UUID生成体系

#### 策略对比

| 策略            | 生成方式 | 特性   | 适用场景  |
|---------------|------|------|-------|
| SimpleUUID    | 哈希算法 | 简单快速 | 一般场景  |
| SnowFlakeUUID | 雪花算法 | 有序唯一 | 分布式系统 |

#### 雪花算法特性

- 时间戳位数：41位
- 机器ID位数：5位
- 数据中心位数：5位
- 序列号位数：12位

### 6. 异常体系

#### 异常工具链

```mermaid graph TD 
A[AssertUtil] --> B[FormattedRuntimeException]
C[ExceptionUtil] --> B
D[IErrorThrowEvent] --> B
```

## 三、配置说明

### application.yml配置示例

```yaml
 tbs:
   holder:
     use-thread-local-stored-context: true # 启用线程上下文 use-local-stored-context: true # 启用本地上下文
 main-context-holder-type: com.tbs.common.holder.impl.ImportedContextHolder
 uuid:
   type:
     com.tbs.common.uuid.impl.SnowFlakeUUID snow-flake-worker-id: 1 
     snow-flake-data-center-id: 1 
     except-length: 32
```
## 四、最佳实践
### 1. 上下文使用模式

```java
 // 获取上下文 
 IContext context = HolderUtil.getDefaultLocalStoredContext();
// 存储数据 
context.set("user", user);
// 获取数据 
User user = (User) context.get("user");
```
## 五、注意事项
1. **线程安全**：ThreadLocalStoredContext适用于多线程环境
2. **异常处理**：建议实现IErrorThrowEvent接口进行全局异常捕获
3. **UUID冲突**：分布式环境下建议使用SnowFlakeUUID并合理配置节点ID
4. **资源释放**：ImportedContextHolder需要手动管理上下文生命周期
5. **函数序列化**：使用函数式接口时需确保实现类可序列化
