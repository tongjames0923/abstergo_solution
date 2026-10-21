# 统一注册中心实现文档 —— 设计思路 · 使用 · 拓展（Registry Center Implementation Guide）

> 适用版本：`common` 模块，`com.tbs.common.registry`（通用键式注册表）+ `com.tbs.common.provider`（多来源路由域）
> 状态：**与阶段 1 ~ 3-δ 已落地实现同步**。本文是该体系的唯一现行文档，面向使用者与拓展者，
> 描述设计思路、装配、使用与拓展方式；接口级契约以各类 Javadoc 为准（含完整 `@param`/`@return` 块注释），
> 历史设计决策与接口规格的两份过程文档已废弃删除，不再生效。
> 本文不含任何业务应用试点内容。

---

## 一、定位：它解决什么问题

在「同一业务接口可能存在**本地 Spring Bean** 与**注册中心发现的 Dubbo 远端实例**（以及未来的第三方后端）」的
前提下，统一回答三个问题：

1. **有什么**：某接口在各来源、各业务分区（business）、各版本（version）下有哪些可用实现（可枚举、可观测）；
2. **用哪个**：一次调用应当路由到哪个实现（按来源偏好、坐标过滤、优先级择一）；
3. **怎么变**：远端实例扩容/下线时，上述两者如何在不重启、不手工刷新的前提下自动跟上。

历史上这三件事分散在 `IObjectProvider` 解析链（按序问解析器）与 `@RoutingService` 路由代理（手动主备降级）
两套语义里。统一注册中心把它们收敛为一个条目模型 + 一个门面：`IRegistryCenter`。

## 二、分层架构

```
com.tbs.common.registry          通用键式注册表层（无来源概念，可独立复用）
 ├─ IRegistryStore<K,V>          存储 SPI
 ├─ InMemoryRegistryStore        内存实现（读多写少）
 ├─ KeyedRegistry<K,V>           键 → 多值 门面（注册/整段替换/枚举）
 ├─ DispatchPolicy               分派语义 FIRST / ALL
 └─ IKeyMatcher<K>               键匹配策略（本期 EXACT，PATTERN/PREDICATE 预留）

com.tbs.common.provider          多来源路由域层（构建于上，接口类型 = 注册键）
 ├─ 模型：ObjectSource / SourceCoordinate / RegistryEntry / DefaultRegistryEntry
 ├─ 贡献：IRegistrationSource / ILazyEntriesContributor / ObjectResolverAsSource（旧 SPI 桥）
 ├─ 门面：IRegistryCenter / DefaultRegistryCenter
 ├─ 策略：RoutingStrategy（值对象，reorder 为排序唯一真相源）
 └─ 通道：RoutingServiceProxy / RoutingProxyFactory / RoutingServiceFactoryBean / RoutingServiceRegistrar
```

分层原则：通用层不知道「来源/坐标/路由」为何物；路由域层不重复实现「键→多值登记与替换」，
`DefaultRegistryCenter` 内部坐落 `KeyedRegistry`。两层的合并已被评估并明确拒绝（保持 `common.registry` 的
来源无关性，供 MQ topic、连接注册表等异构场景复用）。

## 三、核心模型与设计思路

### 3.1 来源是开放值对象，不是封闭枚举

`ObjectSource` 为不可变值对象：内置常量 `SPRING`（id=`spring`）、`DUBBO`（id=`dubbo`），
第三方经 `ObjectSource.of(id, description)` 造新来源，等值基于 id（**用 equals，勿用 `==`**）。
新增后端不需要改框架枚举。

### 3.2 坐标把 business/version 升为一等维度

`SourceCoordinate = {source, business, version}`：

- Dubbo 来源：`business` 取自服务注册 URL 的 **group**，group 为空时兜底 `ProviderConstant.DEFAULT_BUSINESS`（`"default"`）；`version` 取自 URL 的 version；
- Spring 来源：当前实现 business 恒为空串（`IRouteMeta` 自声明钩子已定义但解析器尚未接线，见 §七 未接线项）；
- 空串维度在过滤时语义为「不约束」，非空时要求逐字相等。

### 3.3 条目（RegistryEntry）：身份、坐标、角色

一条条目 = 「某来源在某接口、某 business/version 上，以某实例标识提供了某能力」。

- **唯一身份** = `(key, coordinate, instanceKey)`，equals/hashCode 据此，供去重与整段替换的幂等判定（**不含角色**）；
- **instanceKey** 是纯物理定位：Dubbo 逐实例为 `host:port`，集群引用固定 `"cluster"`，Spring 为 beanName；
- **两种正交角色**（3-δ 引入，默认均 true 向后兼容）：
  - `enumerable()`——枚举视图：`resolve(ALL)` 只过滤 enumerable 条目（观测、广播、逐实例直连）；
  - `routable()`——路由目标：`resolve(FIRST)`/`resolveFirst` 只过滤 routable 条目（择一路由命中）；
- **懒解析**：`DefaultRegistryEntry.of(..., Supplier)` 首次 `resolve()` 时构建并缓存；`ofInstance(...)` 直接持有实例。

### 3.4 路由策略是「偏好序 + reorder」

`RoutingStrategy` 为携带 `List<ObjectSource>` 优先序的值对象，内置 `LOCAL_FIRST / REMOTE_FIRST / SPRING_ONLY / DUBBO_ONLY`
四常量（注解以字符串承载，经 `byName` 还原），第三方经 `custom(ObjectSource...)` 组合任意偏好序。

`reorder(entries)` 是排序唯一真相源：先按来源在 preference 中的位次，同来源内按 `entry.order()` 降序，
并顺带完成 accept 过滤（不在偏好序内的来源被剔除）。中心不再自建 comparator。

### 3.5 贡献模型：按需懒填充（3-γ 决策）

装配期**不预扫全容器、不提前查注册中心**：

1. 每个来源（`IRegistrationSource`）在 `contribute(center)` 时仅把自己登记为 `ILazyEntriesContributor`；
2. 中心首次读到某接口的空桶时，回调贡献者现场 `resolveAll` / `resolveRoutableTargets`，
   按身份合并两类结果并标注角色，经 `replaceEntriesBySource` 整段推送；
3. 新鲜度由贡献者自声明：静态来源（Spring）每接口贡献一次；动态来源（Dubbo）声明 `refreshOnRead`，
   读即重贡献——真实重查由解析器的快照时效节流（`dubboDiscoveryRefreshMillis`），
   与现存同源条目同集幂等时跳过替换，避免无谓换位与监听噪音。

取舍：相对「装配期全量预扫 + 事件推送」，零新增后台线程/订阅、回归面最小；将来接 Dubbo
`NotifyListener` 真事件推送只需多一个推送方，不返工。

### 3.6 Dubbo 语义保真：逐实例与集群引用并存（3-δ 决策）

直接拿逐实例直连引用做路由会在多实例时钉死单点、丢失负载均衡与 failover。解法是角色分离：

| 条目 | 身份 | 角色 | 用途 |
|------|------|------|------|
| 逐实例直连引用 | `(key, 坐标, host:port)` | 仅 enumerable | 枚举观测、精确直连某实例 |
| 坐标级集群引用 | `(key, 坐标, "cluster")` | 仅 routable | `resolveFirst` 路由命中，保留 Dubbo 原生 LB/failover |

集群引用按 `(business, version)` 坐标各建一个且缓存复用（`InterfaceProviderState`），
business 为 `default` 时不设 group（匹配无分组 provider）。两类条目身份三元组不同，故同桶并存互不顶掉。

### 3.7 三条不变量

- **I1**：调用期 `resolve` 是纯内存读（懒填充只发生在首次，或动态来源的节流刷新）；
- **I2**：动态变化只经 `replaceEntriesBySource` 整段替换进入，不散改条目；
- **I3**：路由代理是「通道」不是「实现」——中心拒绝代理条目入桶（`isRoutingProxy` 过滤），枚举结果亦不含代理，防转发递归。

## 四、装配与生命周期

### 4.1 自动装配

`ObjectProviderConfig`（经 `@EnableCommonPack` 扫描注册）总开关：

| 配置键（前缀 `tbs.common.object-provider`） | 默认 | 说明 |
|--------------------------------------------|------|------|
| `enable` | true（缺失即开） | 功能总开关，关闭则门面/中心/注册器全部不装配 |
| `dubbo-discovery-enabled` | true | 关闭后仅剩 Spring 来源，路由自动退化本地直取 |
| `dubbo-timeout` | 常量缺省 | Dubbo 引用超时（集群与逐实例引用通用） |
| `dubbo-discovery-refresh-millis` | 常量缺省 | 实例快照刷新间隔；非正数=发现后不主动过期（空快照按固定回退间隔重试） |

Bean 装配顺序（同一 Config 内）：

1. `SpringObjectResolver`（order 靠前）与 `DubboInstanceResolver`（`@ConditionalOnClass(ReferenceConfig)` 守卫，Dubbo 缺失静默退化）注册为 `IObjectResolver`；
2. `DefaultObjectProvider` 收解析链 + 中心引用，作过渡门面；
3. `DefaultRegistryCenter`：存量解析器经 `ObjectResolverAsSource` 桥接为贡献来源（Dubbo 声明读即重贡献），
   业务方自定义 `IRegistrationSource` Bean 一并收集贡献；**最后** `attachRoutingProxyFactory(new RoutingProxyFactory())`
   挂接代理工厂——中心先建成再挂工厂，规避「工厂造代理需中心、中心装配需工厂」的构造期循环。

### 4.2 `@RoutingService` 接口的代理生成

```
接口标注 @RoutingService(strategy="LOCAL_FIRST", beanName="")
  → RoutingServiceRegistrar 扫描（默认基包 com.tbs，可用 @EnableTbsRoutingService(basePackages=...) 收窄，建议显式声明）
  → 注册 RoutingServiceFactoryBean（Bean 名 = 接口简单名首字母小写 + "RoutingProxy" 后缀，标 primary，OBJECT_TYPE=接口）
  → getObject() 经 IObjectProvider → 中心 getRoutingProxy，按 (接口, 策略, business, version) 缓存代理实例
```

注解的 `strategy` 是**字符串**（注解元素不可为对象类型），取值即四个内置策略名之一，未知名启动期快速失败。

### 4.3 一次路由调用的完整路径

```
业务方法调用 → RoutingServiceProxy.invoke
  → center.resolveFirst(iface, business, version, strategy)
      → 懒填充（若该键首读） → 角色过滤(routable) → business/version 过滤 → strategy.reorder → 取首个
  → entry.resolve() 得到目标实例 → Method.invoke(target, args)
```

未命中任何 routable 条目时抛业务异常（不静默返回 null）；代理工厂未挂接即取代理同样快速失败。

## 五、使用指南

### 5.1 声明一个可路由接口

```java
@RoutingService   // 默认 LOCAL_FIRST：本地 Bean 优先，远端兜底
public interface IXxxService {
    Result doSomething(Request req);
}
```

消费方按普通 Bean 注入即可，每次调用动态择一转发：

```java
@Resource
private IXxxService xxxService;   // 命中的是路由代理（primary）
```

注意：代理标 primary，**按类型注入一律命中代理**；需要绕开代理直取某本地实现时，用 `@Resource(name="具体Bean名")`
按名注入。本地实现与代理同类型共存是设计内状态（代理条目被枚举排除，不递归）。

### 5.2 程序化解析（不注入代理的场景）

```java
@Resource
private IRegistryCenter registryCenter;

// 1) 择一路由目标（返回 routable 条目；Dubbo 命中坐标级集群引用）
Optional<RegistryEntry> target =
    registryCenter.resolveFirst(IXxxService.class, "order", "1.0", RoutingStrategy.REMOTE_FIRST);
Object instance = target.map(RegistryEntry::resolve).orElse(null);

// 2) 枚举全部可用实现（返回 enumerable 条目：逐实例视图，含 host:port 与坐标）
List<RegistryEntry> all =
    registryCenter.resolve(IXxxService.class, null, null, DispatchPolicy.ALL, RoutingStrategy.LOCAL_FIRST);

// 3) 构建绑定 business/version 的路由代理（中心侧按四元组缓存）
IXxxService proxy = registryCenter.getRoutingProxy(IXxxService.class, RoutingStrategy.DUBBO_ONLY, "order", null);

// 4) 观测已登记接口
Set<Class<?>> keys = registryCenter.keys();
```

business/version 过滤语义：**null 或空串 = 该维度不约束**；非空 = 逐字相等命中。多 business 并存且不约束时，
`resolveFirst` 命中 reorder 后第一个坐标的目标条目。

### 5.3 策略选择速查

| 策略 | 效果 | 典型场景 |
|------|------|----------|
| `LOCAL_FIRST`（默认） | Spring 优先，Dubbo 兜底 | 单体/微服务混合部署自适应 |
| `REMOTE_FIRST` | Dubbo 优先，Spring 兜底 | 算力下沉、本地仅做容灾 |
| `SPRING_ONLY` / `DUBBO_ONLY` | 锁定单一来源 | 明确部署形态、隔离验证 |
| `RoutingStrategy.custom(A, B, ...)` | 任意来源偏好序（含第三方） | 第三方后端接入，不改中心 |

### 5.4 过渡门面 `IObjectProvider`

`getObject/getObjectOptional/getObjects/getResolvedObject(type, source)` 仍走**旧解析链**（过渡期保留的对照路径，
方向上随 `IObjectResolver` 一并 `@Deprecated`）；唯 `getRoutingProxy(type, strategy)` 已委派中心。
新代码直接使用 `IRegistryCenter`，不建议新增对旧链方法的调用。

## 六、拓展指南

### 6.1 新增一种来源（推荐路径：`IRegistrationSource`）

实现 `IRegistrationSource` 并注册为 Spring Bean，`ObjectProviderConfig` 收集后自动贡献：

```java
public class NacosRegistrationSource implements IRegistrationSource {

    @Override
    public ObjectSource source() {
        return ObjectSource.of("nacos", "Nacos 服务实例");
    }

    @Override
    public void contribute(IRegistryCenter center) {
        // 静态来源：resolve 出条目后 replaceEntriesBySource(source(), key, entries)；
        // 动态来源：注册事件回调，变化时整段替换；亦可参照 ILazyEntriesContributor 走懒填充
    }

    @Override
    public int getOrder() {
        return 500;   // 与既有解析器排序协同：Spring=-1000，Dubbo=1000
    }
}
```

要点：贡献失败**记录日志并贡献空集**，不得抛出打断装配；条目按角色标注（见 §6.3）；
同来源同键的刷新必须走 `replaceEntriesBySource` 整段替换（不变量 I2）。

### 6.2 兼容旧 SPI：`IObjectResolver`（存量路径，已 @Deprecated）

容器中只有 `IObjectResolver` Bean 而某来源尚无显式 `IRegistrationSource` 时，Config 自动经
`ObjectResolverAsSource` 桥接入中心（Dubbo 来源桥为读即重贡献，其余为静态贡献）。
存量解析器需正确实现 `resolveRoutableTargets`——默认实现等于 `resolveAll`（本地实例天然双角色）；
只有「路由目标 ≠ 逐实例集合」的来源（如集群引用型 RPC）才需要覆写。

### 6.3 条目角色怎么标

| 你的条目是… | enumerable | routable |
|-------------|-----------|----------|
| 本地对象、单实例 | true | true |
| 远端逐实例直连引用（供观测/精确调用） | true | false |
| 远端集群引用（承载 LB/failover，供路由命中） | false | true |
| 纯内部占位（既不应被枚举也不应被路由） | false | false |

用 `DefaultRegistryEntry.ofInstance(key, coordinate, instanceKey, order, instance, enumerable, routable)`
或 7 参 `of(..., Supplier, enumerable, routable)` 构造；5 参工厂默认双角色。

### 6.4 感知条目变化

`center.addListener(IRegistryChangeListener)` 在 `replaceEntriesBySource` 实际发生增删后按变化坐标回调，
用于自建缓存失效、健康联动、观测面板等。监听器需容忍高频小批量回调并自行去抖。

### 6.5 替换路由代理实现

`IRoutingProxyFactory` 是代理构建的插槽：默认 `RoutingProxyFactory` 产 JDK 动态代理
（`RoutingServiceProxy` 处理器）。需要拦截语义增强（重试、熔断、调用审计）时实现该接口并在装配后
`attachRoutingProxyFactory` 替换，中心侧的四元组缓存与 `resolveFirst` 契约不变。

### 6.6 键匹配拓展位

通用层 `IKeyMatcher` 本期只接 EXACT；出现通配键/谓词键需求（如通配 topic 匹配）时在通用层实现第 2 个
matcher 即可，路由域无需改动。

## 七、当前边界与未接线项

| 项 | 现状 | 计划位 |
|----|------|--------|
| Spring 来源 business 派生 | `SpringObjectResolver` 未读 `IRouteMeta` 自声明，Spring 条目 business 恒空串，多本地实现暂不能按 business 区分 | 阶段 3-ε/4 接线坐标推导 |
| 字段级业务绑定 `@RoutingRef` | 未落地，business 定向现阶段经 `center.getRoutingProxy(..., business, version)` 程序化获取 | 阶段 4 |
| 广播 invoke-all 语义 | `DispatchPolicy.ALL` 仅枚举条目集，中心不代做「逐个调用」；广播执行由使用方遍历条目自行分派 | 按需评估 |
| Dubbo 真事件推送 | 动态新鲜度走「读即重贡献 + 快照时效节流」，未接 `NotifyListener` | 可平滑追加，不返工 |
| 多 business 不约束路由 | general 路由（business=null）命中 reorder 后第一坐标，无跨 business 加权 | 设计内行为 |

## 八、关键类速查

| 类 | 包 | 一句话职责 |
|----|----|-----------|
| `IRegistryCenter` / `DefaultRegistryCenter` | provider / provider.impl | 注册+解析+分派门面；角色过滤 + reorder + 懒填充 |
| `RegistryEntry` / `DefaultRegistryEntry` | provider / provider.impl | 条目模型与双角色工厂 |
| `SourceCoordinate` / `ObjectSource` | provider | 来源坐标三元组 / 开放来源值对象 |
| `RoutingStrategy` | provider | 来源偏好值对象；`reorder` 排序唯一真相源 |
| `DispatchPolicy` | registry | FIRST/ALL 分派语义（复用为角色选择器） |
| `IRegistrationSource` / `ILazyEntriesContributor` | provider | 来源贡献 SPI / 懒填充契约 |
| `ObjectResolverAsSource` | provider.impl | 旧 `IObjectResolver` → 新来源桥 |
| `SpringObjectResolver` / `DubboInstanceResolver` | provider.impl | 两内置来源存量解析器（@Deprecated 保留） |
| `RoutingServiceProxy` / `RoutingProxyFactory` / `RoutingServiceFactoryBean` | provider.impl(.support) | 路由代理处理器 / 工厂插槽 / Bean 工厂 |
| `RoutingServiceRegistrar` | provider.impl | `@RoutingService` 扫描与 primary 代理注册 |
| `ObjectProviderConfig` / `ObjectProviderProperty` | config(.property) | 自动装配与配置收拢 |

## 九、自检用例索引（`common/src/test`）

| 用例 | 覆盖面 |
|------|--------|
| `RegistryCenterContractTest` | 坐标归一/必填断言、条目身份等值、懒解析只建一次 |
| `DefaultRegistryCenterTest` | 来源偏好排序、business/version 过滤、FIRST/ALL、整段替换+监听、代理工厂缺失快速失败 |
| `RegistrySourceContributionTest` | 桥接贡献、静态/动态新鲜度、**路由走集群条目而枚举保留逐实例**（角色分离回归防线） |

编译与测试执行按项目约定由人工 `mvn -pl common test` 验证。
