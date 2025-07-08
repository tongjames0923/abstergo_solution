# Lock 模块文档

## 目录结构

```
com.tbs.lock 
├── config # 配置相关类 
├── impls # 核心实现类 │ 
├── providers # 锁提供者实现 
├── util # 工具类 
├── ILock.java # 锁基础接口 
├── ILockProvider.java # 锁提供者接口 
├── IReadWriteLock.java # 读写锁接口 
└── LockProperty.java # 配置属性类
```

## 模块说明

### 1. 核心接口

#### [ILock](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\ILock.java#L10-L46)

定义基础锁操作：

```java 
public interface ILock {
    boolean tryLock(Duration timeOut); // 尝试获取锁 

    boolean isHeldByCurrentThread(); // 检查当前线程是否持有锁 

    boolean isLocked(); // 检查锁是否被占用 

    void unLock(); // 释放锁

    Condition newCondition(); // 创建条件变量
}
```

#### `ILockProvider`

锁实例工厂接口：

```java
public interface ILockProvider {
    ILock getLocker(Object target); // 根据目标对象获取锁
}
```

#### `IReadWriteLock`

读写锁接口：

```java
 public interface IReadWriteLock {
    ILock readLock(); // 获取读锁

    ILock writeLock(); // 获取写锁 
}
```

### 2. 核心实现

#### [LockAdapter](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\impls\LockAdapter.java#L12-L60)

JDK锁适配器：

- 继承[ILock](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\ILock.java#L10-L46)
  接口
- 包装`java.util.concurrent.locks.Lock`
- 线程安全实现：使用`ThreadLocal`跟踪锁状态

#### [ReadWriteLockAdapter](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\impls\ReadWriteLockAdapter.java#L10-L30)

读写锁适配器：

- 组合[ILock](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\ILock.java#L10-L46)
  实现读写分离
- 基于JDK的`ReadWriteLock`

#### [JdkLockProvider](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\impls\providers\JdkLockProvider.java#L12-L19)

默认锁提供者：

- 使用ConcurrentHashMap缓存锁实例
- 基于`ReentrantLock`实现
- 线程安全的单例模式

### 3. 配置组件

#### [LockProperty](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\LockProperty.java#L11-L20)

配置属性类：

```java 

@Component
@ConfigurationProperties(prefix = "tbs.lock")
public class LockProperty {
    private Class<? extends ILockProvider> lockProviderType; // 锁提供者类型 
}
```

#### [LockConfig](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\config\LockConfig.java#L14-L28)

配置类：

- 通过Spring依赖注入初始化
- 使用BeanUtil创建锁提供者实例

### 4. 工具类

#### [LockUtil](file://C:\Users\tongj\IdeaProjects\abstergo_solution\async\src\main\java\com\tbs\lock\util\LockUtil.java#L14-L108)

提供静态方法调用：

```java
 // 获取锁的三种方式 
getLockerByStr(String lockName);// 通过字符串标识 

getLockerByObj(Object target); // 通过对象标识

getLockerByClass(Class<?> clazz); // 通过类信息

// 加锁执行方法 
lockThen(String lockName, Runnable runnable); // 带锁执行
```

## 使用示例

```java
 // 1. 注入工具类
@Autowired
private LockUtil lockUtil;
// 2. 使用锁执行 
lockUtil.

lockThen("myLock",() ->{
    // 执行临界区代码 
    System.out.println("正在执行受保护代码"); });
```

## 配置说明

在application.yml中配置：

```yaml
 tbs:
   lock:
     lock-provider-type: com.tbs.lock.impls.providers.JdkLockProvider
```
## 注意事项
1. 锁对象生命周期由JdkLockProvider自动管理
2. 使用lockThen方法时需处理可能抛出的FormattedRuntimeException
3. 读写锁适用于读多写少的场景
4. 锁名称建议使用业务相关标识符保证可读性
