# 多来源对象提供器（ObjectProvider）使用说明文档

> 适用版本：`common` 模块 `com.tbs.common.provider` 包（含 Dubbo 实例发现解析器）
> 读者：需要让「同一个接口」在本地 Spring Bean 与 Dubbo 远程实例之间按策略路由、或需要枚举并定向调用多来源实现的业务/框架开发人员
> 相关代码：门面 [IObjectProvider](../common/src/main/java/com/tbs/common/provider/IObjectProvider.java)、自动装配 [ObjectProviderConfig](../common/src/main/java/com/tbs/common/config/ObjectProviderConfig.java)、可运行用例 [ObjectProviderSmokeTest](../common/src/test/java/com/tbs/common/provider/ObjectProviderSmokeTest.java)

---

## 一、能力定位与适用场景

### 1.1 一句话能力

给定一个接口，**不关心它的实现来自 Spring 容器还是 Dubbo 注册中心**，统一完成三件事：拿到一个可用实例、枚举全部可用实现（带来源与实例标识）、把它当成普通 Bean 注入并调用。

### 1.2 三层结构与对应职责

| 层次 | 代码 | 回答的问题 |
|------|------|------------|
| 解析器链 | `IObjectResolver`（`SpringObjectResolver` / `DubboInstanceResolver`） | 实例**哪里有**：每个来源一个解析器，按 `Ordered` 排成链 |
| 统一门面 | `IObjectProvider`（`DefaultObjectProvider`） | 怎么**统一拿**：单实例、按来源、全量枚举、构建代理 |
| 路由代理 | `RoutingServiceProxy` + `RoutingServiceFactoryBean` + `@RoutingService` | 怎么**像普通 Bean 一样用**：注入即调用，运行时按策略选实现 |

### 1.3 适用与不适用

- **适用**：服务提供方「本地实现 + `@DubboService` 同时导出」的部署形态（进程内命中本地、纯消费方自动走远程）；需要按实例（`host:port`）定向调用的运维/灰度/巡检类功能；需要在两种来源间灰度切换或做兜底降级。
- **不适用**：需要非 Spring/Dubbo 的第三种来源参与**路由**（见第八节的来源扩展限制）；需要毫秒级感知 provider 上下线（实例发现是轮询语义，见第七节）；离线消息必达、事务性调用（本能力只管「拿到谁」，不管补偿）。

---

## 二、接入前置：依赖与装配

### 2.1 Maven 依赖

业务模块依赖 `common` 即可，无需新增其它依赖。Dubbo 相关能力所需的 `dubbo-spring-boot-starter` 在 `common` 中是 `<optional>`：

- 引入了 Dubbo → Dubbo 来源解析器自动装配；
- 未引入 Dubbo → `@ConditionalOnClass(ReferenceConfig.class)` 守卫失效，解析链静默退化为**仅 Spring 来源**，不报错。

### 2.2 装配生效链

`ObjectProviderConfig` 位于 `com.tbs.common.config`，随 common 包扫描注册生效。业务侧只要已经用了下列任一注解，门面与解析器就已经可用，**无需任何额外配置**：

| 启用方式 | 说明 |
|----------|------|
| `@EnableCommonPack` | 直接启用 common 包（扫描 `com.tbs.common`） |
| `@EnableWebPack` / `@EnableTbsCache` / `@EnableTbsSchedule` 等 | 内部均复用 `@EnableCommonPack`，随之生效 |

`ObjectProviderConfig` 同时 `@Import(RoutingServiceRegistrar.class)`，因此 `@RoutingService` 接口的代理注册也是开箱可用。

### 2.3 关闭开关的连锁后果（重要）

```yaml
tbs:
  common:
    object-provider:
      enable: false
```

`enable=false` 时整个 `ObjectProviderConfig` 不装配：无 `IObjectProvider`、无解析器、也不导入扫描器。
若此时业务类又显式标注了 `@EnableTbsRoutingService`，`@RoutingService` 接口仍会注册代理工厂 Bean，但其依赖的 `IObjectProvider` 缺失，**容器启动即报缺 Bean**。要关闭请直接整体关闭，不要只留 `@EnableTbsRoutingService`。

---

## 三、用法一：声明式路由代理（推荐，覆盖绝大多数场景）

### 3.1 三步接入

**第 1 步**：在业务接口上标注 `@RoutingService`（接口必须是顶级接口或静态嵌套接口）。

```java
@RoutingService(strategy = RoutingStrategy.LOCAL_FIRST)
public interface OrderPriceService {

    /**
     * 计算订单价格
     *
     * @param orderNo 订单号
     * @return 价格，字符串形式避免精度丢失
     */
    String calcPrice(String orderNo);
}
```

**第 2 步**：本地实现按普通 Bean 注册（服务提供方进程内），远程实现按 `@DubboService` 导出。两者都不需要感知本能力。

**第 3 步**：业务侧按普通 Bean 注入并调用。

```java
@Resource
private OrderPriceService orderPriceService;

public void demo() {
    var price = orderPriceService.calcPrice("NO123");
}
```

调用时由代理每次动态选择目标：本地有实现就走本地，没有就走 Dubbo（默认 `LOCAL_FIRST`）。

### 3.2 注入规则（同接口两个 Bean 时的行为）

一个接口同时存在「本地实现 Bean」与「路由代理 Bean」，容器里该类型有两个候选，规则如下：

| 注入写法 | 命中结果 |
|----------|----------|
| `@Autowired` / `@Resource` 按类型注入 | **命中路由代理**（代理 Bean 定义已标 `primary`） |
| `@Resource(name = "orderPriceServiceImpl")` 按本地实现 Bean 名注入 | 命中本地实现，绕过路由 |
| `@Resource(name = "orderPriceServiceRoutingProxy")` | 显式命中代理 |
| `getObjects(接口)` 枚举 | **只返回真实实现，不含代理**（代理是通道不是实现） |

### 3.3 代理 Bean 的命名与冲突

| 情形 | 最终 Bean 名 |
|------|--------------|
| 未指定 `beanName` | 接口简单名首字母小写 + `RoutingProxy`，如 `OrderPriceService` → `orderPriceServiceRoutingProxy` |
| 指定 `@RoutingService(beanName = "xxx")` | 以声明为准，不附加后缀 |

后缀是刻意为之：避免代理与业务侧同名本地实现 Bean 相互顶掉。若计算出的名字已被占用，扫描器**记录 warn 并跳过该接口注册**（不覆盖别人，也不报错），日志关键字：`路由代理Bean[...]与已有BeanDefinition同名`。

### 3.4 扫描范围

| 启用形式 | 扫描基包 |
|----------|----------|
| 经 `@EnableCommonPack` 自动装配（`ObjectProviderConfig` 内 `@Import` 扫描器） | 默认 `com.tbs` 全包扫描 |
| 显式 `@EnableTbsRoutingService(basePackages = "com.tbs.biz")` | 以声明为准，覆盖默认 |

**建议业务侧显式声明 `basePackages`**：`com.tbs` 全包扫描在大型仓库下有可观测的启动开销。

注意二者同时存在时的行为：`@EnableTbsRoutingService` 与 `ObjectProviderConfig` 各自 `@Import` 同一个扫描器，Spring 会为每一次 `@Import` 分别注册一个扫描器实例并各自触发一次扫描 —— 显式声明的基包只约束它那一次，默认 `com.tbs` 那一次仍是全量扫描，重复接口因 Bean 名相同会被 warn 跳过。功能正确性不受影响，但启动开销并未真正降低。要彻底收窄，可关闭自动装配侧的导入（`tbs.common.object-provider.enable` 见 2.3，代价是整体能力关闭），或把 `@RoutingService` 接口集中放在独立基包下并确认启动日志中 `开始扫描@RoutingService标注接口, 包:` 的输出。以上结论基于对 Spring 配置类解析流程的阅读，请在接入时以实际启动日志为准复核。

### 3.5 路由策略

| 策略 | 首选来源 | 是否跨源降级 | 典型用法 |
|------|----------|--------------|----------|
| `LOCAL_FIRST`（默认） | Spring | 本地未命中 → Dubbo | 服务提供方进程；本地能跑就不走网络 |
| `REMOTE_FIRST` | Dubbo | 远程未命中 → Spring | 强制优先集群负载均衡，远程不可用时兜底本地 |
| `SPRING_ONLY` | Spring | 不降级 | 临时摘掉远程（排障/压测本地） |
| `DUBBO_ONLY` | Dubbo | 不降级 | 纯消费方，明确要求必须走远程 |

策略在**每次方法调用时**决策，不是启动时固化：provider 后上线、本地实现后注册、临时关闭发现等场景，下一次调用即生效，无需重启。

---

## 四、用法二：编程式门面

需要枚举多实现、定向到某个实例、或运行时决定策略时才用门面；能用注解注入的不要用门面。

```java
@Resource
private IObjectProvider objectProvider;
```

### 4.1 API 一览

| 方法 | 语义 | 未命中 |
|------|------|--------|
| `<T> T getObject(Class<T> type)` | 解析链首个命中来源的最优实例（Spring 优先；Dubbo 侧返回负载均衡整体引用） | `null` |
| `<T> T getObject(Class<T> type, String beanName)` | 精确实例标识：Spring 侧为 Bean 名，Dubbo 侧为 `host:port` | `null` |
| `<T> Optional<T> getObjectOptional(Class<T> type[, String beanName])` | 同上，Optional 语义 | `Optional.empty()` |
| `<T> Collection<ResolvedObject<T>> getObjects(Class<T> type)` | 枚举全部来源的全部实现，顺序为 Spring 在前、Dubbo 在后 | 空集合 |
| `<T> Optional<ResolvedObject<T>> getResolvedObject(Class<T> type, ObjectSource source)` | 只访问指定来源的解析器，不枚举其余来源 | `Optional.empty()` |
| `<T> T getRoutingProxy(Class<T> interfaceType, RoutingStrategy strategy)` | 手动构建路由代理（同接口同策略复用同一实例） | — |

`ResolvedObject<T>` 携带三样信息：`getInstance()` 实例本体、`getSource()` 来源、`getInstanceKey()` 实例标识。这个载体的完整使用边界见 4.5。

### 4.2 枚举全部实现并逐个调用

```java
public void demoEnumerate() {
    Collection<ResolvedObject<OrderPriceService>> objects = objectProvider.getObjects(OrderPriceService.class);
    objects.forEach(resolved -> {
        log.info("来源: {}, 实例: {}", resolved.getSource(), resolved.getInstanceKey());
        var price = resolved.getInstance().calcPrice("NO123");
    });
}
```

定向只取某一来源的实现：

```java
objectProvider.getObjects(OrderPriceService.class).stream()
    .filter(resolved -> resolved.isSource(ObjectSource.DUBBO))
    .forEach(resolved -> log.info("远程实例: {}", resolved.getInstanceKey()));
```

### 4.3 精确指定实例

```java
// Spring 来源：按 Bean 名
var local = objectProvider.getObjectOptional(OrderPriceService.class, "orderPriceServiceImpl");
// Dubbo 来源：按 host:port（实例标识在两个来源间统一语义，同一入口即可）
var node = objectProvider.getObjectOptional(OrderPriceService.class, "192.168.80.12:20880");
```

指定实例标识时解析器**只会返回该实例**，不会降级到同来源的其它实现，也不会降级到整体引用；两个来源都无该标识时返回空。

### 4.4 按来源定向解析

```java
objectProvider.getResolvedObject(OrderPriceService.class, ObjectSource.DUBBO)
    .ifPresent(r -> r.getInstance().calcPrice("NO123"));
```

Dubbo 来源返回的是**负载均衡后的整体引用**（由 Dubbo 在集群内选节点），不是某个固定节点。同接口多 Bean 时若需要确定性地挑某一个，请用 4.3 的实例标识精确解析，不要依赖按类型解析的返回顺序。

### 4.5 ResolvedObject：这个载体的使用边界

[ResolvedObject](../common/src/main/java/com/tbs/common/provider/ResolvedObject.java) 是「实例 + 元数据」的不可变三元组，本身没有任何行为，只提供区分与定位能力：

| 访问器 | 语义 | 典型用途 |
|--------|------|----------|
| `getInstance()` | 实例本体，可直接当接口调用 | 定向调用某个具体实现（不走路由） |
| `getSource()` / `isSource(ObjectSource)` | 来源判别 | 分流：本地实现做 A 逻辑、远程实例做 B 逻辑；日志与指标打来源标签 |
| `getInstanceKey()` | 来源内的实例唯一标识 | 区分同接口的多个实现；作为下发任务的节点参数 |
| `isClusterReference()` | 是否为 Dubbo 整体负载均衡引用 | 区分「集群语义」与「定点语义」，无需比对标识字符串 |

`instanceKey` 的取值随来源而变，这是全局统一语义（`ObjectResolveContext.matchesInstanceKey` 也按它匹配）：

| 来源 | instanceKey 取值 | 示例 | 判定方法 |
|------|------------------|------|----------|
| `SPRING` | Bean 名称 | `orderPriceServiceImpl` | `isSource(ObjectSource.SPRING)` |
| `DUBBO`（`getObjects` 逐实例） | 提供者地址 `host:port` | `192.168.80.12:20880` | `isSource(DUBBO) && !isClusterReference()` |
| `DUBBO`（`getResolvedObject` / `getObject` 整体引用） | 固定标识 `cluster` | `cluster` | `isClusterReference()` |

使用时注意三条边界：

1. **不要长期持有 `getInstance()`**。Dubbo 来源的实例是直连引用，实例从快照消失后该引用会被立即销毁（见第七节），缓存下来的 `instance` 可能已不可用。每次需要时重新向门面获取，要记住的是 `instanceKey` 而不是实例本体。
2. **不要拿载体本身做相等比较或放进 Set 去重**。该类只有 `@Getter`/`@ToString`，未实现 `equals`/`hashCode`，同来源同实例的两次解析结果是不同对象。去重请按 `source` + `instanceKey` 二元组。
3. **区分「整体引用」与「具体实例」请用 `isClusterReference()`，不要硬编码 `cluster`**。该标识值已收拢为 `ProviderConstant.DUBBO_CLUSTER_INSTANCE_KEY`，但调用方不需要也不应该去比对字符串：

```java
objectProvider.getResolvedObject(OrderPriceService.class, ObjectSource.DUBBO)
    .ifPresent(resolved -> {
        if (resolved.isClusterReference()) {
            log.info("拿到集群整体引用, 具体节点由 Dubbo 负载均衡选定");
        } else {
            log.info("拿到定点实例: {}", resolved.getInstanceKey());
        }
    });
```

一句话选择：只要「能跑通就行」→ 用第三章的注入代理；要「指定某个实现」或「逐个实现都跑一遍」→ 用 `getObjects` + `ResolvedObject`。

---

## 五、失败语义与返回值设计约束（务必阅读）

统一原则：**「找不到」不是异常，是空值**，由调用方决定如何处理。全部来源解析器的内部失败一律降级为空集合并记 warn 日志，不向调用方抛出。

| 场景 | 行为 |
|------|------|
| 门面单实例获取未命中 | 返回 `null` / `Optional.empty()`，不抛异常 |
| 门面枚举未命中 | 返回空集合，不为 `null` |
| 路由代理两来源均未命中 | 记 warn 日志，方法返回类型为引用类型时返回 `null` |
| 路由代理未命中且方法返回**基本类型** | 抛 `FormattedRuntimeException`（`SYSTEM_ERROR` 级别），文案 key `provider.routing.target.not.found` |
| Dubbo 引用构建失败 | 记 warn，跳过该实例/降级为直连首个实例 |
| 目标实现自身抛异常 | 原样透出（代理解包 `InvocationTargetException`，抛的是 cause） |

**接口方法返回值请优先用包装类型**（`Integer`/`Boolean`/`Long`）。基本类型在无实现时返回 `null` 会在调用方拆箱处炸出一个与业务无关的 NPE，难以定位——因此框架选择显式抛错，而不是让你线上猜。

排查用的日志关键字：

| 关键字 | 含义 |
|--------|------|
| `路由代理未命中任何来源实例` | 代理拿不到实现，看接口/策略/方法名定位配置问题 |
| `未发现Dubbo注册中心配置(RegistryConfig)` | 远程来源整体不可用（未配注册中心） |
| `Dubbo接口[...]实例发现失败` | 注册中心可达性/权限问题 |
| `构建Dubbo实例[...]直连引用失败` | 该节点已下线或协议/分组不匹配 |
| `开始扫描@RoutingService标注接口` / `共注册 N 个路由代理Bean` | 确认接口是否被扫到 |

---

## 六、配置项

前缀统一为 `tbs.common.object-provider`，全部有默认值，通常**不需要配置**。

| 配置键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enable` | boolean | `true` | 总开关，关闭后整个能力不装配（后果见 2.3） |
| `dubbo-discovery-enabled` | boolean | `true` | Dubbo 来源开关。关闭后解析链只剩 Spring，`REMOTE_FIRST`/`DUBBO_ONLY` 恒未命中 |
| `dubbo-timeout` | int | `10000` | Dubbo 引用超时（毫秒），整体引用与实例直连引用都生效 |
| `dubbo-discovery-refresh-millis` | long | `5000` | 已发现到实例时的快照刷新间隔（毫秒）；非正数表示不再主动过期 |

```yaml
tbs:
  common:
    object-provider:
      enable: true
      dubbo-discovery-enabled: true
      dubbo-timeout: 10000
      dubbo-discovery-refresh-millis: 5000
```

`dubbo-discovery-refresh-millis` 调小 → 更快发现扩容/重启，代价是更频繁 lookup；纯消费方稳定集群可放宽到 30000。

---

## 七、Dubbo 来源的行为细节

| 方面 | 行为 |
|------|------|
| 发现方式 | 首次解析时向注册中心 `lookup` 该接口 providers 目录，**懒加载**，不提前订阅 |
| 快照时效 | 空快照按固定 1 秒回退间隔重查（不受刷新间隔配置影响），非空快照按 `dubbo-discovery-refresh-millis` 过期 |
| 为什么这样设计 | 发现早于服务上线时若不重查，该来源会被永久锁死；若无间隔则无 provider 环境下每次解析都打注册中心 |
| 感知延迟 | 轮询语义：provider 上下线**最迟一个间隔后**被感知，非实时订阅 |
| 引用管理 | 整体引用与逐实例直连引用均缓存复用（避免重复建连与订阅）；实例从快照消失后其直连引用随即销毁；进程关闭时统一释放（`DisposableBean`） |
| 单实例语义 | `resolveOne` 返回不带地址的整体引用，由 Dubbo 做集群负载均衡与 failover，不会钉死发现列表第一个节点 |
| 分组/版本 | 逐实例直连引用会从 provider URL 上读取 `group`/`version` 并带入引用构建，多分组服务下枚举与定向调用无需额外配置；整体负载均衡引用不指定分组，若服务只以特定 group 导出，请用 `getObjects` 按实例调用（该行为待真实 Dubbo 环境联调验证） |
| `@DubboReference` 的 Bean | 已注册进容器的引用 Bean 属于 **Spring 来源**，走本地 Bean 路径，与 Dubbo 发现来源是两条独立通道，会同时出现在 `getObjects` 结果中 |

---

## 八、扩展新的对象来源（SPI）

新增来源只需实现 `IObjectResolver` 并注册为 Spring Bean，门面对 `List<IObjectResolver>` 的收集会自动纳入。

```java
public class XxxInstanceResolver implements IObjectResolver {

    /**
     * 声明本解析器管辖的来源
     *
     * @return 对象来源
     */
    @Override
    public ObjectSource getSource() {
        return ObjectSource.XXX;
    }

    /**
     * 枚举本来源下目标类型的全部可用实例，内部失败降级为空集合，禁止抛异常
     *
     * @param context 解析上下文
     * @param <T>     目标类型
     * @return 带来源元数据的实例集合
     */
    @Override
    public <T> Collection<ResolvedObject<T>> resolveAll(ObjectResolveContext<T> context) {
        List<ResolvedObject<T>> result = new ArrayList<>();
        for (var candidate : discoverCandidates(context.getTargetType())) {
            var instanceKey = keyOf(candidate);
            if (!context.matchesInstanceKey(instanceKey)) {
                continue;
            }
            result.add(ResolvedObject.of(buildInstance(candidate), ObjectSource.XXX, instanceKey));
        }
        return result;
    }

    @Override
    public int getOrder() {
        return 2000;
    }
}
```

上面只列契约要点：`discoverCandidates` 与 `buildInstance` 是来源自己的获取逻辑，`instanceKey` 在本来源内必须唯一且稳定（参照 Spring 用 Bean 名、Dubbo 用 `host:port`）。

约束与限制，接手扩展前先确认：

1. `ObjectSource` 是枚举，新增来源需要先扩展枚举值（编译期即波及全部实现）。
2. `resolveAll` **不得抛异常**，来源不可用时返回空集合；`resolveOne` 只在自身语义有更优选择时才覆写（如 Dubbo 的负载均衡整体引用）。
3. 返回的实例必须通过 `context.matchesInstanceKey(...)` 校验，指定实例标识的请求不允许降级返回其它实例。
4. **路由代理目前只在 Spring 与 Dubbo 两个来源间做主备决策**（`RoutingStrategy.primarySource()/backupSource()` 返回二者之一）。新来源不会参与代理路由，只能通过 `getObjects` / `getResolvedObject` 消费。要让新来源参与路由，需要扩展 `RoutingStrategy` 的来源语义——这属于契约变更，请先与模块负责人确认。
5. 若新来源的产物可能是 JDK 代理，参照 `RoutingServiceProxy.isRoutingProxy(...)` 在自身 `resolveAll` 中排除路由代理，否则会出现「代理转发给自己」的无限递归。

---

## 九、常见问题（FAQ）

| 现象 | 原因与处理 |
|------|------------|
| 按类型注入报候选不唯一 | 目标接口未标注 `@RoutingService`（代理未注册），或业务侧自定义了与代理同名的 Bean 导致代理被 warn 跳过；搜启动日志确认注册数量 |
| 调用返回 `null` | 未命中任何来源，查 `路由代理未命中任何来源实例` 日志中的接口与策略；远程来源不可见时先确认注册中心配置与 `dubbo-discovery-enabled` |
| 明明有本地实现却打到远程 | 策略写成了 `REMOTE_FIRST`/`DUBBO_ONLY`；或本地实现 Bean 名与代理重名冲突被跳过 |
| 本地实现被路由代理「吃掉」不好排查 | 显式按本地 Bean 名注入（`@Resource(name = "...")`）临时旁路代理验证 |
| provider 上线很久仍调不通 | 首次发现早于服务上线，最长等一个刷新/回退间隔；确认注册中心配置存在（`未发现Dubbo注册中心配置` 日志） |
| `getObjects` 数量比预期多 | 检查是否同时存在 `@DubboReference` 引用 Bean（Spring 来源）与发现实例（Dubbo 来源），二者是两条通道 |
| 需要临时全部走本地 | 直接 `tbs.common.object-provider.dubbo-discovery-enabled: false`，无需改代码 |
| 拿到的 Dubbo 实例不确定打到哪台 | 整体引用（`isClusterReference()` 为 true）本来就是集群负载均衡语义，要定点请用 `getObjects` 枚举后按 `host:port` 选实例 |

---

## 十、参考与自检

- 可运行用例（**改本模块代码后必须保持通过**）：[ObjectProviderSmokeTest](../common/src/test/java/com/tbs/common/provider/ObjectProviderSmokeTest.java)，覆盖 Spring 来源解析、多来源枚举、`LOCAL_FIRST` 本地直取、注解扫描注册的代理按名/按类型注入、未命中返回 null、代理不参与枚举、实例标识匹配、整体引用判定、快照新鲜度判定。
- 契约入口：[IObjectProvider](../common/src/main/java/com/tbs/common/provider/IObjectProvider.java)、[IObjectResolver](../common/src/main/java/com/tbs/common/provider/IObjectResolver.java)、[RoutingStrategy](../common/src/main/java/com/tbs/common/provider/RoutingStrategy.java)、[ResolvedObject](../common/src/main/java/com/tbs/common/provider/ResolvedObject.java)。
- 配置常量：[ObjectProviderProperty](../common/src/main/java/com/tbs/common/config/property/ObjectProviderProperty.java)、[ProviderConstant](../common/src/main/java/com/tbs/common/constant/ProviderConstant.java)。
- 说明：本文档描述的是当前代码实际行为。若另有设计理念类文档提到「门面维护 `selfProxies` 集合排除代理」，该描述已过期——排除逻辑已下沉到各来源解析器，按调用处理器类型判定（`RoutingServiceProxy.isRoutingProxy`）。
