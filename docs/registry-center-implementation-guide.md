# 多来源路由与注册中心实现指南（Registry Center Implementation Guide）

> 适用模块：`common` 模块 `com.tbs.common.provider`
> 状态：与当前 `feat/data-handle-rework` 实现同步。接口级契约以各类 Javadoc 为准。
> 说明：旧版本文档描述的 `IObjectProvider` / `common.registry` 键式注册表 / `@RoutingService` 接口代理体系已整体删除，本文不再保留其内容。

---

## 一、定位与边界

### 1.1 它回答什么问题

同一业务接口可能同时存在**本地 Spring Bean** 与**注册中心发现的 Dubbo 远端实例**，本体系只回答两个问题：

1. **有什么**：某接口在某来源、某业务分区（business）、某发布版本（version）下有哪些可用实例，可枚举、可观测；
2. **用哪个**：一次寻址该返回哪个实例，按来源优先级与可选的候选裁决决定。

### 1.2 它不负责什么

- **不做变更推送**：没有监听器、没有事件回调，每次读取时按需重新问贡献者；
- **不做补偿与重试**：只负责"拿到谁"，调用失败由上层处理；
- **不保证毫秒级感知**：Dubbo 来源按快照有效期刷新，最短 1 秒（空快照回退间隔）；
- **不持有库存结构**：中心只保存"排好序的贡献者列表"，实例从哪来、多久刷新由贡献者自治。

---

## 二、分层与类地图

```
调用方（业务代码 / 选择层 / 探针）
        │
        ▼
IRegistryCenter ──────────── 门面：遍历贡献者、按坐标取首命中（算法写在接口 default 方法里）
        │
        ▼
IRegisterContributor ─────── 来源 SPI：一个实现 = 一个来源，getOrder() 即来源优先级
        ├─ SpringRegisterContributor   本地容器
        └─ DubboRegisterContributor    注册中心发现的远端提供者
        │
        ▼
InstanceProxy ────────────── 统一实例载体（实例 + 坐标 + 类型名 + 来源回引）
        │
        ▼
IRouteMeta / InstanceMeta ── 实例自述的注册坐标
```

| 角色 | 类 | 职责 |
|------|----|------|
| 来源 SPI | [IRegisterContributor](../common/src/main/java/com/tbs/common/provider/IRegisterContributor.java) | 按类型与坐标枚举实例，`getContributorId()` 标识来源、`getOrder()` 决定优先级 |
| SPI 骨架 | [AbstractRegisterContributor](../common/src/main/java/com/tbs/common/provider/AbstractRegisterContributor.java) | 枚举后统一补齐类型名、读取实例自述坐标、按 business/version 过滤 |
| 门面 | [IRegistryCenter](../common/src/main/java/com/tbs/common/provider/IRegistryCenter.java) | `getAllContributors / register / unregister` + default `getInstanceProxy` |
| 门面实现 | [DefaultRegistryCenter](../common/src/main/java/com/tbs/common/provider/impl/DefaultRegistryCenter.java) | 构造期用 `ObjectProvider.orderedStream()` 收拢贡献者，运行期增删按 order 重排 |
| 实例载体 | [InstanceProxy](../common/src/main/java/com/tbs/common/provider/vo/InstanceProxy.java) | 无泛型 VO：`instance`、`business`、`version`、`interfaceFullName`、`targetTypeFullName`、`contributor`、`registryCenter` |
| 坐标模型 | [IRouteMeta](../common/src/main/java/com/tbs/common/provider/IRouteMeta.java) → [InstanceMeta](../common/src/main/java/com/tbs/common/provider/vo/InstanceMeta.java) | 实例自述 `business / version / classFullName` |
| 候选裁决 | [IRouteCoordinator](../common/src/main/java/com/tbs/common/provider/IRouteCoordinator.java) | 对中心筛出的候选做最后一次取舍；`Default` 为接口内嵌类，PackagePrefix / Composite 在 `impl/coordinator` |
| 未命中处理 | [INoProviderHandler](../common/src/main/java/com/tbs/common/provider/INoProviderHandler.java) | 寻址为空时的策略，`LoggingNoProviderHandler`（默认）/ `ErrorThrowNoProviderHandler` |

---

## 三、核心模型

### 3.1 坐标由实例自述，不由框架推导

```java
public interface IRouteMeta {

    default InstanceMeta getMeta() {
        return new InstanceMeta();
    }
}

public class InstanceMeta {

    private String business;
    private String version;
    private String classFullName;
}
```

规则：

- 字段为空串或 `null` 表示**该维度不声明**，过滤时语义为"不约束"；
- `classFullName` 只影响展示用的 `targetTypeFullName`，不参与过滤；
- 贡献者不做任何推导或兜底：**本地 Bean 没实现 `IRouteMeta` 就没有坐标**，这是与旧设计的最大差别。

典型用法是让坐标来自业务标识而非部署维度，例如 `ITableDataHandler` 把处理器 ID 直接映射为 business：

```java
@Override
default InstanceMeta getMeta() {
    return IRouteMeta.super.getMeta().setBusiness(getId());
}
```

### 3.2 来源就是贡献者，链序就是优先级

```java
public interface IRegisterContributor extends Ordered {

    String getContributorId();

    List<InstanceProxy> getAllInstanceByTypeAndBusinessAndVersion(Class<?> type, String business, String version);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
```

内置两个来源的优先级由常量固定：

| 来源 | 常量 | 值 | 效果 |
|------|------|----|------|
| `spring` | `ProviderConstant.ORDER_SPRING_RESOLVER` | -1000 | 本地先遍历，命中即返回，不触发远程发现 |
| `dubbo` | `ProviderConstant.ORDER_DUBBO_RESOLVER` | 1000 | 远端后遍历，仅作兜底 |

`DefaultRegistryCenter` 用 `ObjectProvider<IRegisterContributor>.orderedStream()` 构造库存，因此**新增来源只要实现接口 + 登记为 Bean**，无需改动中心。

### 3.3 InstanceProxy 为什么去掉了泛型

同一个载体要同时承载本地对象与 Dubbo 代理，而 Dubbo 代理的真实类型在运行期才确定，泛型参数在跨来源时会退化成 `Object`。现在统一为裸 `InstanceProxy`，由消费方按已知接口自行 `cast`：

```java
ITableDataHandler handler = (ITableDataHandler)instanceProxy.getInstance();
```

代价是编译期类型检查前移到了调用点，收益是中心、贡献者、选择层、探针共用同一个载体。

---

## 四、一次寻址的完整路径

`IRegistryCenter#getInstanceProxy` 是接口里的 default 方法，算法只有一段：

```java
for (IRegisterContributor contributor : getAllContributors()) {
    List<InstanceProxy> proxys =
        contributor.getAllInstanceByTypeAndBusinessAndVersion(type, business, version);
    if (CollUtil.isEmpty(proxys)) {
        continue;
    }
    for (InstanceProxy proxy : proxys) {
        boolean accepted = coordinator == null
            || coordinator.route(proxy.getInstance().getClass(), type, business, version);
        if (accepted) {
            result = proxy;
            break;
        }
    }
    if (result != null) {
        break;
    }
}
if (result != null) {
    result.setRegistryCenter(this);
}
return result;
```

要点：

1. **坐标过滤在贡献者内部完成**（`AbstractRegisterContributor` 里按 instanceProxy 的 business/version 过滤），中心不重复过滤；
2. **协调器是事后裁决**：中心先拿到贡献者筛过的候选，再逐个问协调器，传 `null` 表示全部接受；
3. **跨来源首个命中即返回**，不会先收齐全量再比较，因此本地来源命中时远端贡献者根本不会被调用；
4. 命中结果会回填 `registryCenter`，便于调用方拿到来源后继续程序化操作；
5. **未命中返回 `null`**，不抛异常；抛不抛由 `INoProviderHandler` 或调用方决定。

贡献者内部的公共步骤（`AbstractRegisterContributor#getStream`）：

```
1. interfaceFullName = 查询类型全名
2. targetTypeFullName = 实例实际类名
3. 若实例实现了 IRouteMeta：
     meta.business 非空 → 覆盖 business
     meta.version  非空 → 覆盖 version
     meta.classFullName 非空 → 覆盖 targetTypeFullName
4. 按入参 business / version 过滤（空串不过滤）
```

注意第 3 步的覆盖优先级：**实例自述坐标优先于贡献者填入的坐标**。Dubbo 贡献者先按 URL 的 group/version 填坐标，若远端实现类又实现了 `IRouteMeta` 并返回非空值，就会覆盖 URL 坐标。

---

## 五、内置来源

### 5.1 Spring 贡献者

[SpringRegisterContributor](../common/src/main/java/com/tbs/common/provider/impl/SpringRegisterContributor.java)

- 枚举：`beanFactory.getBeanNamesForType(type, true, false)`，关闭 `allowEagerInit` 避免为判定类型而提前实例化 FactoryBean；
- 校验：拿到 Bean 后再做一次 `type.isInstance(bean)`，因为 FactoryBean 产出的实际类型可能与声明不符；
- 坐标：**完全不设置**，全部交给实例的 `IRouteMeta`；
- 无匹配：返回空列表并打 debug 日志，不报错。

### 5.2 Dubbo 贡献者

[DubboRegisterContributor](../common/src/main/java/com/tbs/common/provider/impl/DubboRegisterContributor.java)

发现流程：

```
容器内第一个 RegistryConfig → 补默认协议 dubbo://
  → ApplicationModel 取 RegistryFactory 自适应扩展
  → registry.lookup(interface + category=providers + check=false)
  → 过滤 EMPTY_PROTOCOL 占位 URL
  → 每个 URL 折算坐标 (group → business, version → version)
  → 按坐标归并（同一坐标的多个提供者共享一个引用，但当前会在枚举结果里重复出现，见第九节）
```

引用构建：

- 每个坐标一个 `ReferenceConfig` 集群引用，缓存键为 `接口全名#business#version`，同一坐标复用同一引用；
- 不指定直连地址，同坐标的多个提供者由 Dubbo 自身负载均衡；
- `check=false`：提供者暂缺不阻塞启动，窗口期内调用失败由上层处理；
- `timeout` 取 `ProviderConstant.DEFAULT_DUBBO_TIMEOUT`（10000ms），当前**不读配置项**。

快照刷新：

| 快照状态 | 有效期 | 说明 |
|----------|--------|------|
| 非空 | `DEFAULT_DISCOVERY_REFRESH_MILLIS` = 5000ms | 提供者上下线最迟 5 秒后可见 |
| 空 | `EMPTY_DISCOVERY_RETRY_MILLIS` = 1000ms | 提供者尚未上线时不会长期沿用空结果 |

失败降级：注册中心地址缺失、不可达、接口查不到实例、单个坐标引用构建失败，均只记录日志并跳过，**不影响其他来源和其他坐标**。

释放：坐标下线的引用保留到停机，由 `DisposableBean#destroy()` 统一销毁（`ponytail:` 注释已标注该已知上限）。

### 5.3 两个来源的坐标差异（重要）

| 维度 | Spring 来源 | Dubbo 来源 |
|------|-------------|------------|
| business 写在哪 | 实现类的 `IRouteMeta.getMeta()` | 提供者 URL 的 `group` |
| version 写在哪 | 同上 | 提供者 URL 的 `version` |
| 未声明时 | `null` | 空串 |
| 谁负责对齐 | 实现方 | 提供方（`@DubboService(group=..., version=...)`） |

同一个接口的本地实现与远端实现，如果想在同一个坐标上互相顶替，**两边的 business/version 必须写成同一个值**；否则它们只是两个不同坐标下的实例，互相兜不了底。

---

## 六、策略扩展点

### 6.1 IRouteCoordinator：候选裁决

```java
boolean route(Class<?> targetClass, Class<?> interfaceClass, String business, String version);
```

内置实现：

| 类 | 语义 | 装配方式 |
|----|------|----------|
| `IRouteCoordinator.Default` | 全部接受，与传 `null` 等价 | 注解 `coordinator()` 的缺省值，反射无参构造 |
| `AcceptAllRouteCoordinator` | 同 `Default`，登记为 Bean | `@Component` + 开关 |
| `PackagePrefixRouteCoordinator` | 只接受实现类全名命中包前缀白名单的候选 | 程序化 `new` 后传给中心 |
| `CompositeRouteCoordinator` | `all(...)` / `any(...)` 组合多个协调器 | 程序化构造 |

分工要清楚：**来源优先级由链序承担，协调器只叠加额外策略**（限定包、灰度名单、按实现在哪个模块等）。不要在协调器里重新实现"本地优先"。

### 6.2 INoProviderHandler：未命中处理

| 实现 | 行为 | 适用 |
|------|------|------|
| `LoggingNoProviderHandler` | warn 日志 + 返回 `null`（基本类型返回默认值） | 缺提供者可容忍的调用点 |
| `ErrorThrowNoProviderHandler` | 抛业务异常，消息走 i18n（接口 + business + version） | 缺提供者必须立刻暴露 |

---

## 七、装配与开关

总开关：`tbs.common.object-provider.enable`（默认 `true`）。
Dubbo 来源另有 `tbs.common.object-provider.dubbo-discovery-enabled`（默认 `true`，仅对 Dubbo 贡献者生效）。

带条件的类：

| 类 | 条件 |
|----|------|
| `DefaultRegistryCenter` | `enable` |
| `SpringRegisterContributor` | `enable` |
| `DubboRegisterContributor` | `enable` **且** `dubbo-discovery-enabled` |
| `AcceptAllRouteCoordinator` | `enable` |
| `RoutingRefBeanPostProcessor` | `enable` |
| `LoggingNoProviderHandler` | 无条件（未命中兜底不依赖开关） |

关闭后的连锁后果（务必知道）：

- 容器内**没有 `IRegistryCenter` Bean**，任何 `@Resource IRegistryCenter` 的类启动即失败（例如 `TableDataHandlerRegistry`）；
- **没有 `RoutingRefBeanPostProcessor`**，`@RoutingRef` 字段不会被装配，运行时保持 `null`，且不会报错；
- 贡献者与协调器均不装配，`register/unregister` 也不可用。

因此关闭开关时必须同时排除依赖方，不能只关 provider。

---

## 八、扩展指南

### 8.1 新增一种来源

```java
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX, name = ProviderConstant.ENABLE_PROPERTY_NAME,
    matchIfMissing = true)
public class ZookeeperRegisterContributor extends AbstractRegisterContributor {

    @Override
    public String getContributorId() {
        return "zookeeper";
    }

    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    protected <T> List<InstanceProxy> getInstanceByType(Class<T> type) {
        // 1. 按 type 发现实例
        // 2. 为每个实例建 InstanceProxy：setInstance / setContributor
        // 3. 坐标可先填，实例若实现 IRouteMeta 会以后者为准
    }
}
```

要求：

- `getOrder()` 决定它在链中的位置，不实现时默认 `LOWEST_PRECEDENCE`（排在最后）；
- 枚举失败必须降级为空列表，不能抛出打断整条链；
- **不要在贡献者构造期反向注入 `IRegistryCenter`**：中心构造时会立即实例化全部贡献者，构成循环依赖。

运行期增删：`registryCenter.register(contributor)` / `unregister(contributor)`，`register` 后按 order 重排，运行期新增的来源不会排到链尾而丢失优先级。

### 8.2 新增一个协调器

实现 `IRouteCoordinator` 即可。若要作为 `@RoutingRef(coordinator = X.class)` 使用，需满足：**登记为容器 Bean，或具备无参构造**（装配器优先取容器 Bean，缺失则反射构造）。

### 8.3 新增一种未命中处理

实现 `INoProviderHandler`，同样满足"Bean 或无参构造"，然后在字段上指定 `noProvider = X.class`。

---

## 九、已知边界与设计取舍

| 项 | 现状 | 影响 / 规避 |
|----|------|-------------|
| 枚举期远程调用 | `getStream` 对每个实例调用 `getMeta()`；Dubbo 代理若实现 `IRouteMeta`，每次枚举每个远程实例一次 RPC | 远端坐标应交给 URL（group/version），远程实现类不要再实现 `IRouteMeta`；后续可把 `getMeta()` 限定为仅本地实例 |
| 本地坐标无兜底 | 未实现 `IRouteMeta` 的本地 Bean，business/version 为 `null` | 按 business 寻址时查不到它；要么实现 `IRouteMeta`，要么寻址时不传 business |
| 无变更事件 | 无监听器，调用期重新问贡献者 | Dubbo 侧有 5s/1s 快照；Spring 侧每次扫容器，开销低但非零 |
| 去泛型 | `InstanceProxy#getInstance()` 返回 `Object` | 消费方自行 cast，类型错误在运行期暴露 |
| 远程引用驻留 | 坐标下线后引用保留至停机 | 已在 `destroy()` 统一释放，代码里以 `ponytail:` 标注升级路径 |
| 同坐标多实例重复候选 | `DubboCoordinate` 未重写 `equals`，`distinct()` 对同坐标的多个提供者不生效 | 引用仍复用同一个 `ReferenceConfig`，首命中结果不受影响；若消费方要按实例计数需自行去重 |
| 配置未接线 | `ObjectProviderProperty` 的 `dubboTimeout` / `dubboDiscoveryRefreshMillis` 当前无消费方，实际取常量 | 改超时/刷新间隔现在只能改常量；需要时可把属性类接入贡献者 |

---

## 十、关键类速查

| 想知道 | 看哪个类 |
|--------|----------|
| 寻址算法 | `IRegistryCenter#getInstanceProxy` |
| 贡献者排序与库存 | `impl/DefaultRegistryCenter` |
| 坐标补齐与过滤 | `AbstractRegisterContributor#getStream` |
| 本地实例怎么枚举 | `impl/SpringRegisterContributor` |
| 远端坐标怎么来、引用怎么建 | `impl/DubboRegisterContributor` |
| 字段怎么被装配 | `impl/RoutingRefBeanPostProcessor` |
| 候选怎么被裁决 | `IRouteCoordinator` + `impl/coordinator/*` |
| 未命中怎么办 | `INoProviderHandler` + `impl/*NoProviderHandler` |
| 真实业务用法 | `ITableDataHandler`、`TableDataHandlerRegistry`、`PrivateDataHandleController` |

运行期自检：`GET /public/provider/contributors?type=&business=&version=`，返回各来源的实例盘点与中心首命中详情，判读方式见 [使用指南](object-provider-usage-guide.md) 的"可观测性"一节。
