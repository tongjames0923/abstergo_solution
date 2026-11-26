# Common 模块 - 通用工具包

## 模块概述

Common 模块（`com.tbs:common`）是 Abstergo Solution 平台的基础通用工具包（`com.tbs.common`），为上层模块提供统一的基础组件，包括：启动任务调度、配置体系、数据持有（DataHolder）、代理执行（Delegate）、错误处理、国际化（i18n）、上下文传播、功能开关注册（Feature）、UUID 生成、分页封装以及常用工具类矩阵。

该模块是纯基础包，不依赖任何业务模块，可独立引入使用。

## 功能特性

- **启动任务**：`IStartup` 接口 + `ApplicationRunner` 调度，支持 `Ordered` 排序、可选任务跳过、同步/异步执行
- **配置体系**：基于 `@ConfigurationProperties` 的属性绑定（`tbs.uuid` / `tbs.data` / `tbs.delegate` / `tbs.start-up` / `tbs.i18n`），Bean 构建支持按配置类型实例化
- **数据持有**：`IDataHolder` 键值存储抽象 + `IDataHolderManager` 多实例管理，默认内存 Map 实现
- **代理执行**：函数式代理模板（takeIn / takeOut），同步（`SimpleDelegateInvoker`）与异步（`CompletableFuture` 标记）执行、任务状态跟踪
- **错误处理**：`AssertUtil` 断言（支持 `{}` 占位符格式化与 i18n key）、错误分级（`ErrorLevelEnum`）、异常转换器（`IErrorTransformer`）、错误抛出事件（`IErrorThrowEvent`）
- **国际化**：`I18nUtil` 静态门面（优先 Spring `MessageSource`）、`I18nAutoConfiguration` 自动装配、`messages-common*.properties` 中英文资源
- **上下文传播**：`ContextPropagator` 线程池上下文传播约定，i18n Locale 随任务传播
- **功能开关**：`Feature` 预定义功能模块 + `FeatureManager` 运行时查询已启用功能
- **UUID 生成**：雪花算法（CAS 无锁、时钟回退保护）与随机数实现，可配置切换
- **分页封装**：`PageVO` 泛型分页实体 + `PageUtil` 三种构建方式（同步 / 延迟求值 / 异步并行）
- **工具类矩阵**：字符串、集合、Bean、Lambda、Spring、JSON、时间、加密等工具
- **多来源路由（provider）**：贡献者链 + 注册中心门面按坐标（business/version）在本地 Spring Bean 与 Dubbo 远端实例间寻址，字段标 `@RoutingRef` 即可注入

## 依赖引入

在 Maven 项目中添加依赖（版本由父 POM `com.tbs:solution` 统一管理）：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>common</artifactId>
</dependency>
```

基础依赖：`hutool-all`、`fastjson2`、`spring-context`、`spring-boot-autoconfigure`（随父 POM 继承 `spring-boot-starter`、`dubbo-nacos-spring-boot-starter` 等）。

## 启用方式

模块组件的启用分两个途径：

1. **`@EnableCommonPack` 注解**（推荐）：在启动类或配置类上标注，通过 `@Import(CommonPackAutoRegister.class)` 自动扫描并注册 `com.tbs.common` 包下的 Bean（配置类、工具组件等），并向 `FeatureManager` 注册 `Feature.COMMON`：

```java
@SpringBootApplication
@EnableCommonPack
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

2. **Spring Boot 自动配置**：模块自带 `META-INF/spring.factories`，仅自动注册国际化配置 `I18nAutoConfiguration`（受 `tbs.i18n.enabled` 控制，默认开启），即使不使用 `@EnableCommonPack`，i18n 能力也可用。

> 实际使用中，平台各启动器注解（如 `@EnableWebPack`、`@EnableTbsCache` 等）均依赖本模块，应用入口通常已间接启用。

## 核心组件

### 1. 启动任务（IStartup）

实现 `IStartup` 接口的 Bean 将在 Spring 容器就绪后（`ApplicationRunner` 阶段）执行 `startUp()` 方法：

```java
@Component
public class SystemInitializer implements IStartup, Ordered {

    @Override
    public void startUp() throws RuntimeException {
        // 系统初始化逻辑
    }

    @Override
    public boolean isOptional() {
        return false; // 可选任务：tbs.start-up.optional-execute=false 时跳过
    }

    @Override
    public int getOrder() {
        return 0; // 数值越小越先执行（未实现 Ordered 按 0 处理）
    }
}
```

调度行为由 `StartUpConfig` 实现：

- 按 `Ordered#getOrder()` 升序执行；
- `tbs.start-up.async=true`（默认）时在独立线程中执行全部任务，任务异常仅记录日志、不中断启动；
- `tbs.start-up.optional-execute=false` 时跳过 `isOptional()==true` 的任务；
- `tbs.start-up.enabled=false` 时整体禁用。

### 2. 枚举基类（BaseEnum）

所有业务枚举建议实现 `BaseEnum<T>`，统一 `getCode()`（编码）与 `getValue()`（描述）访问方式：

```java
public enum UserStatus implements BaseEnum<String> {
    ACTIVE(1, "active"),
    DISABLED(0, "disabled");

    private final Integer code;
    private final String value;

    UserStatus(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getValue() { return value; }
}
```

配合 `EnumUtil` 按编码或描述值查找：

```java
UserStatus status = EnumUtil.valueOf(UserStatus.class, 1);          // 按 code
UserStatus status2 = EnumUtil.valueOf(UserStatus.class, "active");  // 按 value
boolean valid = EnumUtil.isValidCode(UserStatus.class, 1);          // 校验 code 是否合法
```

### 3. 数据持有（DataHolder）

`IDataHolder` 提供以字符串为键的通用键值存储抽象，核心方法（完整签名见源码）：

| 方法 | 说明 |
|------|------|
| `<T> boolean set(String key, T value, boolean force)` | 存储数据；`force=true` 强制覆盖，否则仅在键不存在时写入 |
| `<T> Optional<T> get(String key)` | 读取数据，返回 `Optional` |
| `<T> T computeIfAbsent(String key, SFunction<String, T> function)` | 不存在时计算并存储，返回值 |
| `void remove(String key)` / `void clear()` | 删除指定键 / 清空 |
| `boolean containsKey(String key)` / `boolean isEmpty()` / `int size()` | 状态查询 |
| `Collection<String> keys()` / `Collection<Object> values()` / `Collection<Map.Entry<String, Object>> entries()` | 视图获取 |

使用示例（注入默认实例，Bean 名为 `defaultDataHolder`）：

```java
@Service
public class UserService {

    @Resource
    private IDataHolder dataHolder;

    public void cacheUserInfo(User user) {
        dataHolder.set("user:" + user.getId(), user, true);
    }

    public Optional<User> getUserInfo(Long userId) {
        return dataHolder.get("user:" + userId);
    }

    public User getOrCreate(Long userId) {
        return dataHolder.computeIfAbsent("user:" + userId,
            key -> loadUserFromDb(userId));
    }
}
```

实现与配置：

- `MapDataHolder`：基于内存 Map 的默认实现（`tbs.data.data-holder` 可替换）；
- `SpringManagedDataHolderManager`：默认管理器（`tbs.data.data-holder-manager` 可替换），实现 `IDataHolderManager` 的注册 / 注销 / 按名称查找；
- 相关扩展接口：`IDataBridge`（数据桥接）、`ITransformer`（转换器）、`IPartitionableDataHolder`（可分片）、`IMatchKeyFounder`（键匹配）、`ValueWrapper` / `KeyValuePair`（值结构）。

### 4. 代理模式（Delegate）

代理模式提供解耦调用者与执行者的函数式执行模板：通过 `takeIn`（写操作）与 `takeOut`（读操作）登记任务，再以 `executeOnce` / `executeAll` 触发执行，并返回 `IDelegateFlag` 跟踪状态。

核心接口签名：

```java
public interface IDelegateTemplate {
    <T> IDelegateTarget<T> createDelegateTarget(T targetObject);
    <T> IDelegateInvoker<T> createDelegateInvoker(Class<? extends IDelegateInvoker> invokerType,
        IDelegateTarget<T> delegateTarget);
}

public interface IDelegateInvoker<T> {
    <P> IDelegateInvoker<T> takeIn(SBiConsumer<T, P> method, SSupplier<P> p);   // 登记写任务
    <P> IDelegateInvoker<T> takeOut(SFunction<T, P> method, SConsumer<P> consumer); // 登记读任务
    IDelegateFlag<T> executeOnce();                     // 执行一次
    Collection<IDelegateFlag<T>> executeAll();          // 执行全部
    int waittingCount();                                // 待执行任务数
}

public interface IDelegateFlag<T> {
    IDelegateTarget<T> get();               // 获取执行目标
    IDelegateFlag<T> waitUntilFinished();   // 阻塞等待完成
    Throwable getException();
    boolean isFinished();
    IDelegateFlag<T> exceptionThenThrow();  // 有异常则抛出 FormattedRuntimeException（默认实现）
}
```

推荐通过 `DelegateUtil`（Spring 组件，内部持有 `IDelegateTemplate`）使用：

```java
@Service
public class OrderService {

    @Resource
    private DelegateUtil delegateUtil;

    public PageVO<Order> fillPage() {
        PageVO<Order> vo = new PageVO<>();
        // 指定执行器类型后并行执行写任务，等待全部完成
        delegateUtil.delegateWithTarget(vo).build(SimpleDelegateInvoker.class)
            .takeIn(PageVO::setData, () -> queryOrders())
            .takeIn(PageVO::setTotal, () -> countOrders())
            .executeAll()
            .forEach(flag -> flag.waitUntilFinished().exceptionThenThrow().get());
        return vo;
    }
}
```

也可以直接使用模板 API：

```java
IDelegateTarget<Order> target = delegateUtil.delegate(order);
IDelegateInvoker<Order> invoker =
    delegateUtil.getInstance().delegateWithTarget(order).build(SimpleDelegateInvoker.class);
invoker.takeIn(Order::setStatus, () -> OrderStatus.PAID)
       .takeOut(Order::getStatus, status -> log.info("status: {}", status))
       .executeOnce()
       .waitUntilFinished()
       .exceptionThenThrow();
```

装配与实现：

- 默认模板 `SpringManagedDelegateTemplate`（`DelegateConfig`，`@ConditionalOnMissingBean` 可覆盖）；
- 执行器默认经 `SpringManagedTypeBaseInvokerProvider` 按类型创建（`SimpleDelegateInvoker` 为同步实现，在调用者线程直接执行）；
- 目标提供者默认 `SimpleCallerDelegateTargetProvider`（`tbs.delegate.delegate-target-provider` 可替换）；
- 执行标记提供者默认 `CompletableFutureFlagProvider` / `CompletableFutureDelegateFlag`（`tbs.delegate.delegate-flag-provider` 可替换），异步执行器配合其实现非阻塞结果跟踪；
- 辅助接口：`IBackgroundJob`（后台任务）、`IDelegateTargetProvider` / `IDelegateFlagProvider` / `IDelegateInvokerProvider`。

### 5. 错误处理（error）

#### ErrorLevelEnum - 错误分级

按严重程度五级（实现 `BaseEnum<String>`）：

| 枚举值 | code | 描述 |
|--------|------|------|
| `USER_INPUT_ERROR` | 1 | 用户输入错误 |
| `BUSINESS_LOGIC_ERROR` | 2 | 业务逻辑错误 |
| `DATA_ACCESS_ERROR` | 3 | 数据访问错误 |
| `SYSTEM_ERROR` | 4 | 系统错误 |
| `CRITICAL_SYSTEM_ERROR` | 5 | 严重系统错误 |

#### FormattedRuntimeException - 格式化异常

支持 `{}` 占位符格式化与链式设置错误级别，提供按级别命名的静态工厂（直接消息与 i18n 两套）：

```java
// 直接消息
throw new FormattedRuntimeException("用户 {} 不存在", userId);
throw FormattedRuntimeException.userInputError("参数 {} 非法", param);
throw FormattedRuntimeException.businessError("库存不足");

// i18n key（自动经 I18nUtil 翻译）
throw FormattedRuntimeException.userInputErrorI18n("validation.required");

// 链式设置级别
throw new FormattedRuntimeException("异常信息").errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR);
```

#### AssertUtil - 断言工具

失败时抛出 `FormattedRuntimeException`，消息支持 `{}` 占位符参数（`isTrue` 语义为"条件为真则抛出"）：

```java
// 静态便捷方法
AssertUtil.notNull(user, "用户不存在: {}", userId);        // 对象为 null 时抛出
AssertUtil.hasText(username, "用户名不能为空");             // 字符串为空时抛出
AssertUtil.notEmpty(list, "列表不能为空");                  // 集合/数组/Map/迭代器为空时抛出
AssertUtil.isTrue(stock < 0, "库存非法: {}", stock);        // 条件为 true 时抛出

// 指定错误级别
AssertUtil.isTrueWithLevel(invalid, ErrorLevelEnum.USER_INPUT_ERROR)
           .message("参数错误").throwIt();
AssertUtil.notNullWithLevel(user, ErrorLevelEnum.BUSINESS_LOGIC_ERROR, "用户不存在");

// Builder 流式构建（支持 i18n key、异常转换、cause）
AssertUtil.isTrue(!valid)
    .i18n("validation.min.length", 8)
    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
    .throwIt();

// 自定义异常类型
AssertUtil.isTrue(dup, e -> new MyBizException(e, "重复提交"), "重复提交");
```

行为说明：

- `AssertUtil` 实现了 `IStartup`，启动时收集容器内全部 `IErrorTransformer`（按 `getOrder()` 升序）与 `IErrorThrowEvent`；
- `throwError(Supplier)`：抛出前先询问所有 `IErrorThrowEvent#onAssertUtilThrow(Throwable)`，任一返回 `true` 则吞掉异常不再抛出；
- `toFormattedRuntimeException(Throwable)`：按 `support()` 匹配转换器做**特化转换**（如 SQL/权限异常转 i18n 安全文案），未命中时原样返回、不做泛化包装。

#### IErrorTransformer - 异常转换器

```java
public interface IErrorTransformer extends Ordered {
    boolean support(Throwable throwable);
    FormattedRuntimeException toFormattedRuntimeException(Throwable throwable);
    // getOrder() 默认 Ordered.LOWEST_PRECEDENCE
}
```

模块不再提供全量兜底的默认转换器：转换链只处理有特化语义的异常，其余异常类型原样传递，
由响应层的 `resolveErrorMessage` 分层兜底（自建异常原文回传、框架异常回退通用提示），
避免包装丢失异常类型语义（如重登类异常被降级为通用 500）。

#### ExceptionUtil - 异常工具

```java
ExceptionUtil.newException("消息 {}", param);                    // 仅消息
ExceptionUtil.newException(cause, "消息 {}", param);             // 带原因
ExceptionUtil.newException(cause);                               // 仅原因
ExceptionUtil.newException(getClass(), cause, "消息 {}", param); // 带异常源类名
```

### 6. 国际化（i18n）

`I18nUtil` 为静态门面，解析顺序：自定义 `IMessageSource`（`setCustomMessageSource` 设置，非 Spring 环境使用）→ Spring `MessageSource` → 返回 code 本身：

```java
String msg = I18nUtil.get("user.not.found");                    // 简单消息
String msg2 = I18nUtil.get("validation.min.length", 8);         // 带参数
String msg3 = I18nUtil.get("user.not.found", Locale.ENGLISH);   // 指定语言
boolean exist = I18nUtil.has("user.not.found");                 // 判断 key 是否存在
Locale locale = I18nUtil.getCurrentLocale();                    // 当前 Locale（I18nContextHolder 托管）
```

`I18nAutoConfiguration`（经 `spring.factories` 自动注册，`tbs.i18n.enabled` 默认开启）装配内容：

- `MessageSource`（`ResourceBundleMessageSource`）：合并两类 basename —— 容器内 `IMessageSourceProvider` Bean（按 `getOrder()` 排序）与 `tbs.i18n.basenames` 配置，去重后统一装载；未配置任何 basename 时回退默认 `messages`；
- `ContextPropagator`（`I18nContextPropagator`）：线程池任务传播 Locale；
- 启动时按 `tbs.i18n.default-locale` 初始化 `I18nContextHolder` 默认语言。

模块自带资源文件：`messages-common.properties`（中文）、`messages-common_en_US.properties`（英文），key 集中维护在 `CommonI18nConstant`（如 `common.error`、`bean.build.failed`、`delegate.error`、`snowflake.clock.rollback`、`validation.required` 等），新增 key 须中英文两份同步维护。

业务模块可通过继承 `AbstractMessageSourceProvider` 注册自己的资源文件：

```java
@Component
@Order(100)
public class SystemModuleMessageSourceProvider extends AbstractMessageSourceProvider {
    @Override
    public String getBasename() {
        return "messages-system";
    }
}
```

### 7. 上下文传播（context）

- `IContext`：环境生命周期接口（`destroy()` / `rebuild()` / `isValid()` / `checkValid()`）；
- `ContextPropagator`：线程池上下文传播约定 —— `wrap(Runnable)` 在提交线程捕获上下文快照，返回的 `Runnable` 在工作线程恢复并在 `finally` 中**恢复式清理**（还原工作线程执行前状态，任务回退到提交线程同步执行时不会误清其上下文）；
- `ContextPropagators`：传播链统一包装工具——`wrap`（链式包装任务）与 `wrapExecutor`（包装整个 `Executor`），供托管线程池、Spring `@Async`、Spring MVC 异步等各执行路径复用。

`I18nContextPropagator`（i18n 自动装配提供）是该约定的内置实现，将父线程 Locale 传播至异步任务线程。

### 8. 功能开关（Feature / FeatureManager）

`Feature` 标识系统中可支持的功能模块，每个功能对应一个自动注册器（`AbstractAutoBeanRegister` 子类，如 `CommonPackAutoRegister`），注册器生效即代表功能启用：

```java
// 预定义功能
Feature.COMMON / REDIS / RABBITMQ / CACHE / ASYNC / SCRIPT / VIEW_ROUTE /
Feature.WEB / SHARED / CLOSURE_TABLE / KNIFE4J / XXL_JOB / LOGGING / TRACE / OSS

// 自定义功能
Feature myFeature = Feature.create("my-module", "我的模块");
Feature predefined = Feature.of("redis"); // 大小写不敏感，未找到返回 null
```

`FeatureManager` 查询运行时已注册功能：

```java
@Autowired
private FeatureManager featureManager;

featureManager.isSupported(Feature.REDIS);                  // 单个检查
featureManager.isAllSupported(Feature.REDIS, Feature.CACHE); // 全部支持
featureManager.isAnySupported(Feature.REDIS, Feature.CACHE); // 任一支持
featureManager.getSupportedFeatures();                       // 已注册功能集合
```

### 9. UUID 生成器

接口 `IUuidGenerator`：`byte[] getUuid(int exceptLength)` / `String getUuidString(int exceptLength)` / `Long getUuidLong()`。

- **SnowFlakeUUID**（默认，`tbs.uuid.type`）：CAS 无锁雪花算法。workerId 与 dataCenterId 范围 `[0,31]`，越界、时钟回退、时间戳溢出均抛出 i18n 化异常；无参构造自动计算（workerId 取本机 IP 末段对 32 取模，dataCenterId 取 `server.port` 对 32 取模）；`getUuidString` 以 16 进制拼接至期望长度。
- **SimpleUUID**：基于随机数（`Random.nextLong()`）的 16 进制实现。
- **PooledUuidGenerator**：通用池化包装器，可包装任意 `IUuidGenerator`（雪花、随机数等）以降低每次取 ID 的开销；双缓冲 + 异步预填充，缓冲耗尽时降级直调底层生成器。旧名 `PooledSnowFlakeUUID` 已废弃。

装配（`UuidConfig`）：`tbs.uuid.type` 为 `SnowFlakeUUID` 时按配置传入 workerId / dataCenterId，否则按类型经无参构造实例化（缺省回退 `SnowFlakeUUID()`）。

手动包装任意生成器（不改变 `tbs.uuid.type` 的默认装配）：

```java
IUuidGenerator pooled = new PooledUuidGenerator(new SnowFlakeUUID(0, 0), 4096);
Long id = pooled.getUuidLong();
```

包装器预生成 ID，若底层算法把时间戳编入 ID，取用时刻会略晚于生成时刻；强依赖时间戳精确性时请减小缓冲容量。包装器实现 `AutoCloseable`，需在应用关闭时调用 `close()` 停止填充线程。

池化适合“底层生成昂贵、消费突发”的场景；持续满速消费时单填充线程可能跟不上，降级计数会上升。对 `SimpleUUID` 这类本身极廉价的生成器，池化的原子与锁开销可能超过收益，建议按实测结果选用。

使用方式（`UuidUtil` 为 Spring 组件，内部持有生成器与 `UuidProperty`）：

```java
@Resource
private UuidUtil uuidUtil;

String id = uuidUtil.getUuidString(); // 长度取 tbs.uuid.except-length（默认 32）
Long num = uuidUtil.getUuidLong();
```

### 10. 分页（PageVO / PageUtil）

`PageVO<T>`：泛型分页实体，字段 `total`（总数）与 `data`（当前页数据），支持链式 setter，实现 `Serializable`。

`PageUtil` 三种构建方式：

```java
// 方式1：直接构建 —— 数据与总数已知
PageVO<User> page = PageUtil.build(userList, totalCount);

// 方式2：Lambda 延迟求值 —— 按需获取
PageVO<User> page2 = PageUtil.build(() -> queryList(), () -> countAll());

// 方式3：异步并行构建 —— 数据查询与计数并行执行（基于代理模式）
PageVO<User> page3 = PageUtil.build(SimpleDelegateInvoker.class,
    () -> queryList(), () -> countAll());
```

### 11. 工具类矩阵

> `StrUtil` / `CollUtil` / `BeanUtil` / `SpringUtil` 均继承自 Hutool 对应类，除下列扩展方法外可用全部 Hutool 静态方法。

#### 字符串 - StrUtil

```java
StrUtil.format("用户 {} 的年龄是 {}", name, age); // {} 占位符（slf4j MessageFormatter）
StrUtil.isEmpty(text);                             // 继承自 Hutool
StrUtil.readStreamAsString(inputStream);           // 输入流转字符串（可指定 Charset）
StrUtil.toString(obj);                             // 对象转字符串（null 返回空串）
StrUtil.bytesToHex(bytes);                         // 字节数组转十六进制
```

#### 集合 - CollUtil 与 CollWrapper

```java
CollUtil.isEmpty(list);                        // 继承自 Hutool
CollUtil.newArrayList("a", "b");               // 继承自 Hutool
CollUtil.isIn(val, arr);                       // 值是否在数组/集合中
CollWrapper<User> wrapper = CollUtil.of(users); // 集合/流/数组/可迭代对象包装为流式操作器
```

#### Bean - BeanUtil

```java
BeanUtil.buildBeanFromProperties(() -> defaultBean, property.getType(), BeanUtil::buildByEmptyArgs);
BeanUtil.buildByEmptyArgs(SomeClass.class); // 按无参构造反射实例化，失败抛 FormattedRuntimeException
BeanUtil.getAs(value);                      // 强制泛型转换
BeanUtil.getIfBase(value, IBase.class);     // 类型匹配则返回转换结果，否则 null
BeanUtil.isBaseFrom(target, base);          // 判断子类/实现关系
```

#### Lambda - LambdaUtil（结果带缓存）

```java
String field = LambdaUtil.extractFieldName(User::getName); // 从方法引用提取字段名
Field f = LambdaUtil.getField(User::getName);              // 提取 Field 对象
Method m = LambdaUtil.getMethod(User::getName);            // 提取 Method 对象
String name = LambdaUtil.extractMethodName(User::getName); // 提取方法名（去除 get/set/is 前缀）
```

#### Spring - SpringUtil

```java
UserService svc = SpringUtil.getBean(UserService.class);           // 继承自 Hutool
Optional<UserService> opt = SpringUtil.getBeanOptional(UserService.class); // 失败返回 Optional.empty()
Collection<IErrorTransformer> ts = SpringUtil.getBeansByTypeSafe(IErrorTransformer.class); // 失败返回空集合
UserService s2 = SpringUtil.getBeanSafe(UserService.class, e -> log.warn("缺失", e));       // 失败回调
```

#### JSON - JsonUtils（Jackson 封装，优先复用 Spring 容器 ObjectMapper）

```java
String json = JsonUtils.toJSONString(user);
String pretty = JsonUtils.toPrettyJSONString(user);
byte[] bytes = JsonUtils.toJSONBytes(user);
User user = JsonUtils.parseObject(json, User.class);
List<User> users = JsonUtils.parseArray(json, User.class);
Map<String, Object> map = JsonUtils.parseMap(json);
List<User> list2 = JsonUtils.parseObject(json, new TypeReference<List<User>>() {});
boolean ok = JsonUtils.isJson(json);
```

#### 时间 - TimeUtil

```java
long millis = TimeUtil.getUtcTime();                    // UTC 毫秒时间戳
long seconds = TimeUtil.getUtcTime(TimeUnit.SECONDS);   // 指定单位
// 常量：DATE_FORMAT_yyyyMMddHHmmss_STR、DATE_FORMAT_yyyyMMddHHmm_STR 及对应 Formatter

// 基于 Hutool DateUtil 封装：出入参均为 JDK 类型，返回的 Date 不会是 Hutool DateTime
Date now = TimeUtil.now();
Date weekAgo = TimeUtil.offsetDay(now, -7);             // 无需再手动 toJdkDate()
Date monthStart = TimeUtil.beginOfMonth(now);
String text = TimeUtil.format(now, "yyyyMMdd");
Date parsed = TimeUtil.parse("2026-11-26", "yyyy-MM-dd");
LocalDateTime dateTime = TimeUtil.parseLocalDateTime("2026-11-26 10:00:00", TimeUtil.DATE_FORMAT_yyyyMMddHHmmss_STR);
long days = TimeUtil.between(weekAgo, now, ChronoUnit.DAYS);
```

另有 `offsetSecond/Minute/Hour/Day/Week/Month/Year`、`begin|endOfDay`、`begin|endOfMonth`、
`toLocalDateTime`/`toLocalDate`/`toDate`、`formatDateTime`、`isSameDay`、`isIn` 等方法，覆盖 Hutool 常用日期操作且对外不泄漏 Hutool 类型。

#### 加密 - EncryptUitl / AESUtil / RSAUtil

```java
// EncryptUitl：不可逆哈希（SHA-1 / SHA-256 / SHA-512），类名确为 Uitl
String hash = EncryptUitl.hash256Encrypt(data, salt);
String hash1 = EncryptUitl.hash128Encrypt(data, salt);
String hash512 = EncryptUitl.hash512Encrypt(data, salt);

// AESUtil / RSAUtil：对称 / 非对称加解密工具
```

#### 函数式接口（function 包）

`SSupplier` / `SFunction` / `SConsumer` / `SBiConsumer`：对应 `java.util.function` 接口的 `Serializable` 扩展，供代理模式等需要序列化 Lambda 的场景使用。

### 12. Nacos 配置支持

`NacosConfig` 提供兜底 `ConfigService` Bean（`@ConditionalOnMissingBean`），服务地址读取 `spring.cloud.nacos.config.server-addr`（默认 `127.0.0.1:8848`）。

### 13. 多来源路由（provider）

把「实例从哪里来」抽象掉：同一接口的实现可以来自 Spring 容器或 Dubbo 注册中心，业务侧用注解或注册中心 API 取用。

| 角色 | 实现 | 职责 |
|------|------|------|
| 来源 SPI | `IRegisterContributor`（`AbstractRegisterContributor`） | 一个实现 = 一个来源；`getOrder()` 决定来源优先级（`spring` -1000 先于 `dubbo` 1000） |
| 注册中心门面 | `IRegistryCenter` / `DefaultRegistryCenter` | 收拢贡献者、按坐标取首命中（`getInstanceProxy` 为接口 default 算法） |
| 坐标模型 | `IRouteMeta` → `InstanceMeta` | 实例自述 business / version / classFullName；本地 Bean 不实现则无坐标 |
| 字段装配 | `@RoutingRef` + `RoutingRefBeanPostProcessor` | 初始化前为字段写入按坐标寻址的 JDK 代理，每次调用实时解析 |
| 候选裁决 / 未命中 | `IRouteCoordinator` / `INoProviderHandler` | 事后取舍（Default / PackagePrefix / Composite）、未命中策略（日志 / 抛错） |

```java
@RoutingRef(noProvider = ErrorThrowNoProviderHandler.class)
private IDataDicService dataDicService;
```

行为要点：

- 不要求接口预先登记，坐标来自实例自述（`IRouteMeta`）或 Dubbo URL 的 `group` / `version`；
- 代理不缓存实例，来源上下线在下一次调用生效；本地来源命中即返回，不触发远程发现；
- 未实现 `IRouteMeta` 的本地 Bean 坐标为空，按 business 寻址不会命中；
- Dubbo 依赖在 common 中为 optional，未引入时只剩 Spring 来源；
- 未命中默认返回 `null`（基本类型返回默认值）并打 warn；`ErrorThrowNoProviderHandler` 改为抛 i18n 业务异常。

完整用法、坐标对齐规则、配置与排查看 [使用指南](../docs/object-provider-usage-guide.md)；实现原理与扩展方式见 [实现指南](../docs/registry-center-implementation-guide.md)。

## 配置说明

完整配置示例（所有项均有默认值，未配置即可使用）：

```yaml
tbs:
  # UUID 生成器
  uuid:
    type: com.tbs.common.uuid.impl.SnowFlakeUUID  # 生成器类型，默认 SnowFlakeUUID
    snow-flake-worker-id: 0                       # 雪花 workerId [0,31]，默认 0
    snow-flake-data-center-id: 0                  # 雪花 dataCenterId [0,31]，默认 0
    except-length: 32                             # UUID 字符串期望长度，默认 32

  # 数据持有者
  data:
    data-holder-manager: com.tbs.common.data.impl.SpringManagedDataHolderManager
    data-holder: com.tbs.common.data.impl.MapDataHolder

  # 代理模式
  delegate:
    delegate-target-provider: com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider
    delegate-flag-provider: com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider

  # 启动任务（注意前缀为 start-up）
  start-up:
    enabled: true            # 是否启用 IStartup 调度
    async: true              # 是否异步执行启动任务
    optional-execute: true   # 是否执行 isOptional()==true 的任务

  # 国际化（spring.factories 自动配置，enabled 默认 true）
  i18n:
    enabled: true
    basenames:               # 资源文件 basename 列表（可与 IMessageSourceProvider Bean 合并）
      - messages-common
    encoding: UTF-8
    default-locale: zh_CN    # 格式 language_COUNTRY
    cache-seconds: 3600      # 0 不缓存，-1 永久缓存
    use-code-as-default-message: true

  # 多来源路由（provider，随 EnableCommonPack 生效）
  common:
    object-provider:
      enable: true                        # 总开关，默认 true
      dubbo-discovery-enabled: true       # Dubbo 来源开关，默认 true
      # dubbo-timeout / dubbo-discovery-refresh-millis 属性类已定义，当前未接线（贡献者取常量）
```

配置项与属性类对照：

| 前缀 | 属性类 | 属性（默认值） |
|------|--------|----------------|
| `tbs.uuid` | `UuidProperty` | `type`（SnowFlakeUUID）、`snowFlakeWorkerId`（0）、`snowFlakeDataCenterId`（0）、`exceptLength`（32） |
| `tbs.data` | `DataHolderProperty` | `dataHolderManager`（SpringManagedDataHolderManager）、`dataHolder`（MapDataHolder） |
| `tbs.delegate` | `DelegateProperty` | `delegateTargetProvider`（SimpleCallerDelegateTargetProvider）、`delegateFlagProvider`（CompletableFutureFlagProvider） |
| `tbs.start-up` | `StartUpProperty` | `enabled`（true）、`async`（true）、`optionalExecute`（true） |
| `tbs.i18n` | `I18nProperty` | `enabled`（true）、`basenames`（空）、`encoding`（UTF-8）、`defaultLocale`（zh_CN）、`cacheSeconds`（3600）、`useCodeAsDefaultMessage`（true） |
| `tbs.common.object-provider` | `ObjectProviderProperty` | `enable`（true）、`dubboDiscoveryEnabled`（true）生效；`dubboTimeout`（10000）、`dubboDiscoveryRefreshMillis`（5000）已定义但暂未接线 |

## 扩展开发

### 自定义数据持有者

实现 `IDataHolder` 并通过 `tbs.data.data-holder` 指定类型（需提供无参构造）：

```java
public class RedisDataHolder implements IDataHolder {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> boolean set(String key, T value, boolean force) {
        if (force) {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) redisTemplate.opsForValue().get(key));
    }

    @Override
    public void remove(String key) { redisTemplate.delete(key); }

    @Override
    public void clear() { /* 按需实现 */ }

    @Override
    public boolean isEmpty() { return size() == 0; }

    @Override
    public int size() { /* 按需实现 */ return 0; }

    @Override
    public boolean containsKey(String key) { return Boolean.TRUE.equals(redisTemplate.hasKey(key)); }

    @Override
    public Collection<Map.Entry<String, Object>> entries() { /* 按需实现 */ return Collections.emptyList(); }

    @Override
    public Collection<String> keys() { /* 按需实现 */ return Collections.emptyList(); }

    @Override
    public Collection<Object> values() { /* 按需实现 */ return Collections.emptyList(); }
}
```

### 自定义异常转换器

实现 `IErrorTransformer` 并注册为 Bean，按 `getOrder()` 参与匹配（数值越小越优先）：

```java
@Component
public class BizErrorTransformer implements IErrorTransformer {

    @Override
    public boolean support(Throwable throwable) {
        return throwable instanceof BizException;
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        return FormattedRuntimeException.businessError(throwable.getMessage());
    }
}
```

### 自定义 i18n 资源提供者

实现 `IMessageSourceProvider`（或继承 `AbstractMessageSourceProvider`），返回模块资源文件 basename，即可与 `tbs.i18n.basenames` 合并装载。

### 自定义上下文传播器

实现 `ContextPropagator.wrap(Runnable)`：在提交线程捕获快照、工作线程恢复并在 `finally` 中清理，参考 `I18nContextPropagator` 实现。

## 包结构一览

```
com.tbs.common
├── annotation        # @EnableCommonPack
├── config            # 自动配置：DataHolderConfig / DelegateConfig / StartUpConfig /
│   │                 #   UuidConfig / I18nAutoConfiguration / NacosConfig / CommonPackAutoRegister
│   └── property      # UuidProperty / DataHolderProperty / DelegateProperty /
│                     #   StartUpProperty / I18nProperty / ObjectProviderProperty
├── constant          # CommonI18nConstant / DataHolderBeanNameConstant / ProviderConstant
├── context           # IContext / ContextPropagator / ContextPropagators
├── data              # IDataHolder / IDataHolderManager / PageVO 等
│   ├── impl          # MapDataHolder / SpringManagedDataHolderManager / JsonTransformer 等
│   ├── serializer    # ToStringOrNumberDeserializer
│   └── structs       # KeyValuePair
├── delegate          # IDelegateTemplate / IDelegateInvoker / IDelegateFlag 等
│   ├── impl          # SimpleDelegateInvoker / SpringManagedDelegateTemplate /
│   │                 #   CallerRunsDelegateTarget / CompletableFutureDelegateFlag
│   ├── provider      # 目标/标记/执行器提供者接口
│   └── provider.impl # SimpleCallerDelegateTargetProvider / CompletableFutureFlagProvider 等
├── error             # AssertUtil / ErrorLevelEnum / IErrorTransformer / IErrorThrowEvent 等
│   └── impl          # SimpleErrorThrowEvent
├── exception         # FormattedRuntimeException
├── function          # SSupplier / SFunction / SConsumer / SBiConsumer
├── i18n              # I18nUtil / IMessageSource / AbstractMessageSourceProvider /
│                     #   I18nContextHolder / I18nContextPropagator 等
├── provider          # IRegistryCenter / IRegisterContributor / AbstractRegisterContributor /
│   │                 #   IRouteMeta / IRouteCoordinator / INoProviderHandler
│   ├── annotation    # @RoutingRef
│   ├── impl          # DefaultRegistryCenter / SpringRegisterContributor /
│   │                 #   DubboRegisterContributor / RoutingRefBeanPostProcessor /
│   │                 #   LoggingNoProviderHandler / ErrorThrowNoProviderHandler
│   ├── impl.coordinator # AcceptAll / PackagePrefix / Composite
│   └── vo            # InstanceProxy / InstanceMeta
├── util              # PageUtil
│   ├── base          # StrUtil / CollUtil / CollWrapper / BeanUtil / JsonUtils / ReflectUtil / StructUtils
│   ├── encrypt       # AESUtil / RSAUtil / EncryptUitl
│   ├── function      # EnumUtil / LambdaUtil / Comparators
│   ├── framework     # SpringUtil / DelegateUtil / UuidUtil
│   ├── helper        # FeatureWrapperHelper / IFeatureWrapperExtension
│   └── time          # TimeUtil
└── uuid              # IUuidGenerator
    └── impl          # SnowFlakeUUID / SimpleUUID / PooledUuidGenerator / PooledSnowFlakeUUID(废弃兼容)
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
