# 多来源路由使用指南（Provider Routing Usage Guide）

> 适用模块：`common` 模块 `com.tbs.common.provider`
> 实现原理、类地图与扩展方式见 [实现指南](registry-center-implementation-guide.md)，本文只讲怎么用。
> 旧版 `IObjectProvider` / `@RoutingService` 的用法已失效，请勿参考历史文档。

---

## 一、能力与适用场景

一句话：**同一个接口，本地 Spring Bean 与 Dubbo 远端实例都可以被找到、按坐标选中、像普通字段一样调用。**

| 场景 | 是否适合 |
|------|----------|
| 服务提供方把实现同时注册为本地 Bean 与 `@DubboService`，希望进程内优先、跨进程兜底 | 适合 |
| 同一接口有多套实现，需要按业务分区/版本切换 | 适合（把坐标写进 `IRouteMeta` 或 Dubbo group/version） |
| 缺提供者必须立刻报错，而不是静默返回空 | 适合（`noProvider = ErrorThrowNoProviderHandler.class`） |
| 需要提供者上下线毫秒级推送 | 不适合（无变更事件，远端最快 1 秒、通常 5 秒感知） |
| 事务性调用、离线必达 | 不适合（本能力只管"拿到谁"） |

---

## 二、接入前提

1. 业务模块依赖 `common`，且应用已启用 `@EnableCommonPack`（或任一内部复用它的 `@Enable*`，如 `@EnableWebPack`）；
2. provider 默认开启，无需额外注解。总开关：

```yaml
tbs:
  common:
    object-provider:
      enable: true                    # 总开关，默认 true
      dubbo-discovery-enabled: true   # 仅控制 Dubbo 来源，默认 true
```

> 关闭 `enable` 后：没有注册中心 Bean、没有字段装配器，`@RoutingRef` 字段会保持 `null` 且不报错；依赖 `IRegistryCenter` 的类会在启动时失败。要关就连同依赖方一起排除。

Dubbo 依赖在 `common` 中是 `optional`：未引入 Dubbo 时只有 Spring 来源，寻址自动降级为本地。

---

## 三、用法一：`@RoutingRef` 字段注入（推荐）

### 3.1 三步接入

```java
// 1. 字段类型即目标接口，直接标注，不要再写 @Resource / @DubboReference
@RoutingRef(noProvider = ErrorThrowNoProviderHandler.class)
private IDataDicService dataDicService;

// 2. 直接调用，与普通 Bean 无异
dataDicService.listByName("区域");
```

真实用例见 `SystemDataDicController`。注解在 Bean 初始化前生效，字段由装配器写入 JDK 动态代理，代理每次调用时按坐标向注册中心取当前实例。

### 3.2 属性

| 属性 | 默认 | 说明 |
|------|------|------|
| `business` | `""` | 业务分区坐标，空表示不约束；支持 `${...}` 占位符，经容器 `Environment` 解析 |
| `version` | `""` | 发布版本坐标，空表示不约束；支持 `${...}` 占位符 |
| `coordinator` | `IRouteCoordinator.Default.class` | 候选裁决器；优先取容器内同类型 Bean，缺失则反射无参构造 |
| `noProvider` | `LoggingNoProviderHandler.class` | 未命中处理；优先取容器内 Bean，缺失则反射无参构造 |

```java
// 按配置项定位业务分区，缺提供者直接抛 i18n 业务异常
@RoutingRef(business = "${tbs.dic.business}", version = "1.0",
    noProvider = ErrorThrowNoProviderHandler.class)
private IDataDicService dicService;
```

### 3.3 调用语义

| 情形 | 行为 |
|------|------|
| 命中 | 转发到该实例；目标抛出的异常拆掉包装后原样抛出 |
| 未命中 + 默认 `noProvider` | 打 warn 日志，返回 `null`（方法返回基本类型时返回默认值，如 `0` / `false`） |
| 未命中 + `ErrorThrowNoProviderHandler` | 抛业务异常，消息含接口、business、version |
| 调用 `toString / hashCode / equals` | 本地应答，不触发寻址 |
| 实例被替换/下线 | 下一次调用即生效（**代理不缓存实例**） |

### 3.4 使用约束

- **字段类型必须是接口**，否则启动时抛 `provider.routing.ref.target.not.interface`；
- **不能是 `final` 字段**（反射写入失败，启动抛 `provider.routing.ref.inject.failed`）；
- 不要叠加 `@Resource` / `@DubboReference`，装配器会在初始化前直接覆盖字段值；
- 静态字段也会被写入，但所有实例共享同一个代理，不建议这样用。

### 3.5 为什么字段是 `null`

| 现象 | 原因 | 处理 |
|------|------|------|
| 启动正常但字段为 `null` | 贡献者链没返回实例，默认 `noProvider` 只打 warn | 换成 `ErrorThrowNoProviderHandler` 暴露问题；用探针确认链上有没有实例 |
| 增加了 `business` 之后就查不到 | 本地实现没实现 `IRouteMeta`，business 为 `null`；或者远端 group 与本地自写的值不一致 | 见下一节"坐标从哪来" |
| 整体字段都没被装配 | `tbs.common.object-provider.enable=false`，装配器不存在 | 打开开关 |

---

## 四、坐标从哪来（最容易踩的坑）

中心按 `business` / `version` 过滤实例，两个来源的坐标写法不同：

| 来源 | business 来源 | version 来源 |
|------|---------------|--------------|
| 本地 Spring Bean | 实现类的 `IRouteMeta.getMeta()` | 同上 |
| Dubbo 远端 | 提供者 URL 的 `group`（`@DubboService(group = ...)`） | 提供者 URL 的 `version` |

本地实现示例：

```java
public class RegionalPriceService implements IPriceService, IRouteMeta {

    @Override
    public InstanceMeta getMeta() {
        return new InstanceMeta().setBusiness("north").setVersion("1.0");
    }
}
```

远端提供方示例：

```java
@DubboService(group = "north", version = "1.0")
public class RegionalPriceServiceImpl implements IPriceService {
}
```

三个必须记住的规则：

1. **两边的值要完全一致**，否则它们只是两个不同坐标的实例，互相兜不了底；
2. 本地不实现 `IRouteMeta` → 坐标为 `null`，只要 `@RoutingRef` 里写了 `business` 就永远不命中；
3. 远端实现类**不要再实现 `IRouteMeta`**：枚举时装配器会调用 `getMeta()`，这会让每次枚举对每个远程实例多一次 RPC；远端坐标交给 URL 即可。

---

## 五、用法二：编程式经注册中心

需要枚举全部实例、按来源观察、或做运行时判断时，直接注入 `IRegistryCenter`：

```java
@Resource
private IRegistryCenter registryCenter;

// 1. 取首命中（与 @RoutingRef 同一算法）
InstanceProxy proxy = registryCenter.getInstanceProxy(IPriceService.class, "north", "1.0", null);
IPriceService service = proxy == null ? null : (IPriceService)proxy.getInstance();

// 2. 枚举各来源（绕过协调器，逐个看）
for (IRegisterContributor contributor : registryCenter.getAllContributors()) {
    List<InstanceProxy> instances =
        contributor.getAllInstanceByTypeAndBusinessAndVersion(IPriceService.class, null, null);
    // contributor.getContributorId() 是 "spring" / "dubbo"
}
```

`InstanceProxy` 上可直接读到：`getInstance()`、`getBusiness()`、`getVersion()`、`getInterfaceFullName()`、`getTargetTypeFullName()`、`getContributor()`、`getRegistryCenter()`。

注意：`getInstance()` 返回 `Object`，需要按已知接口自行 `cast`。

---

## 六、用法三：按业务 ID 选用实现（真实样例）

数据处理链路是"处理器 ID 即 business 坐标"的完整示例，可作为接入模板。

`ITableDataHandler` 把 ID 映射为坐标：

```java
public interface ITableDataHandler extends IRouteMeta {

    String getId();

    @Override
    default InstanceMeta getMeta() {
        return IRouteMeta.super.getMeta().setBusiness(getId());
    }
}
```

选择层 `TableDataHandlerRegistry` 只做三件事：

| 方法 | 行为 |
|------|------|
| `getAllHandlers()` | 逐贡献者枚举 `ITableDataHandler`，本地在前、远端在后 |
| `getHandler(id)` | 先按 `business = id` 坐标寻址，未命中再逐来源按 `getId()` 兜底匹配 |
| `getHandlerSource(id)` | 返回命中来源标识 `spring` / `dubbo`，未命中返回 `null` |

对外接口（`PrivateDataHandleController`）：

- `GET /private/.../handler/list`：下拉选项；
- `GET /private/.../handler/validate/{handlerId}`：校验自定义 ID，返回 `available` + `source`；
- `POST /private/.../handle/csv/{handlerId}`：上传 CSV 并交给命中处理器。

为什么需要兜底：远端实现 `RemoteTableDataOperator` 当前把坐标写成 `business = "remote"`、`version = "1.0"`，而不是处理器 ID，纯坐标寻址找不到它。若把远端坐标改成 `getId()` 对应的值，`resolve` 里的兜底可以删除。

---

## 七、配置项

| 配置 | 默认 | 当前状态 |
|------|------|----------|
| `tbs.common.object-provider.enable` | `true` | 生效，控制中心、贡献者、装配器、默认协调器 |
| `tbs.common.object-provider.dubbo-discovery-enabled` | `true` | 生效，仅控制 Dubbo 贡献者；`false` 时只剩本地来源 |
| `tbs.common.object-provider.dubbo-timeout` | `10000` | **暂未接线**：Dubbo 贡献者直接使用常量 `ProviderConstant.DEFAULT_DUBBO_TIMEOUT` |
| `tbs.common.object-provider.dubbo-discovery-refresh-millis` | `5000` | **暂未接线**：快照有效期取常量（非空 5000ms、空 1000ms） |

属性类 `ObjectProviderProperty` 已定义但当前没有消费方，改超时/刷新间隔需要改常量或补接线。

---

## 八、可观测性：贡献者探针

```http
GET /public/provider/contributors?type=com.tbs.common.data.ITableDataHandler&business=&version=
```

- `type` 留空默认取 `ITableDataHandler`；
- `business` / `version` 留空表示该维度不约束。

响应 `data` 结构：

```json
{
  "type": "com.tbs.common.data.ITableDataHandler",
  "business": null,
  "version": null,
  "contributorIds": ["spring", "dubbo"],
  "contributors": [
    {
      "contributorId": "spring",
      "order": -1000,
      "count": 1,
      "instances": [
        {"targetTypeFullName": "com.tbs.app.service.SimpleJsonLogDataHandler", "business": "json", "version": null}
      ]
    },
    {
      "contributorId": "dubbo",
      "order": 1000,
      "count": 1,
      "instances": [
        {"targetTypeFullName": "com.tbs.app.service.logging.RemoteTableDataOperator", "business": "remote", "version": "1.0"}
      ]
    }
  ],
  "centerHit": {
    "hit": true,
    "contributorId": "spring",
    "interfaceFullName": "com.tbs.common.data.ITableDataHandler",
    "targetTypeFullName": "com.tbs.app.service.SimpleJsonLogDataHandler",
    "business": "json",
    "version": null
  }
}
```

判读顺序：

1. `contributorIds` 是否包含预期的来源（缺 `dubbo` 说明 Dubbo 贡献者没装配或开关关闭）；
2. 各来源 `count` 是否为 0（为 0 时看 `error` 字段，单来源失败不会打断探针）；
3. `instances` 里的 `business/version` 是否与你的注解一致——不一致就是坐标对不上；
4. `centerHit.hit` 是否命中，命中的 `contributorId` 是否符合优先级预期。

> 探针会真实枚举，因此会触发 Dubbo 引用构建与远端 `getMeta()` 调用；高频调用没有意义。

---

## 九、Dubbo 来源行为细节

- 坐标：URL 的 `group → business`、`version → version`；都未声明时为空串；
- 引用：每个坐标一个集群引用，缓存到停机；同坐标多提供者由 Dubbo 负载均衡；
- 启动：`check=false`，提供者未上线不影响启动；
- 超时：10000ms（常量，当前不读配置）；
- 发现：`category=providers` 的 lookup，结果按坐标去重；
- 刷新：非空快照 5 秒、空快照 1 秒重新发现；
- 降级：注册中心不可达、查不到实例、单坐标引用构建失败，都只记录日志并跳过；
- 释放：坐标下线的引用保留到停机，由 `destroy()` 统一销毁（已知上限）。

---

## 十、从旧设计迁移自查

| 旧用法 | 新做法 |
|--------|--------|
| `@RoutingService(strategy = ...)` 标在接口上 | 删除；改为在**字段**上标 `@RoutingRef` |
| `@EnableTbsRoutingService` | 删除，provider 随 `@EnableCommonPack` 生效 |
| `IObjectProvider` / `ResolvedObject` / `ObjectSource` | 删除；改用 `IRegistryCenter` + `InstanceProxy` |
| 接口上的 `business/version` 声明 | 移到实现类的 `IRouteMeta.getMeta()`，或远端 `@DubboService(group/version)` |
| `@RoutingRef` + `@Resource` 并存 | 只保留 `@RoutingRef` |
| `List<ResolvedObject> getObjects(...)` | `contributor.getAllInstanceByTypeAndBusinessAndVersion(...)` 或 `registryCenter.getInstanceProxy(...)` |
| 依赖 `RoutingStrategy` 配来源偏好 | 依赖贡献者 `getOrder()`；额外规则写 `IRouteCoordinator` |

迁移后必查两项：

1. 本地实现是否实现 `IRouteMeta`（否则按 business 寻址不命中）；
2. 远端 `@DubboService` 的 `group/version` 是否与本地坐标一致。

---

## 十一、FAQ

**Q：`@RoutingRef` 和 `@Resource` 能一起用吗？**
不能。装配器在初始化前写入代理，`@Resource` 之后注入会覆盖它或反之，行为不可预期。

**Q：能不能直接注入某个本地实现，绕过路由？**
可以，普通 `@Resource` 按 Bean 名注入即可；`@RoutingRef` 是"按坐标选择"的入口，不是替代品。

**Q：每次调用都会查一遍注册中心吗？**
是。代理不缓存实例，这样才能让上下线立即生效；Dubbo 侧有快照缓存，Spring 侧是一次容器类型枚举。

**Q：同接口两个本地 Bean，`@RoutingRef` 取哪个？**
取贡献者链顺序下第一个通过坐标过滤与协调器的实例；Spring 内部顺序不保证，需要明确选择时请用 `business/version` 区分。

**Q：远端实例调用失败会怎样？**
本能力不做重试与降级，异常原样抛给调用方；需要降级请自行 catch 或换 `noProvider` 策略。

**Q：为什么探针里 `targetTypeFullName` 是远端的实现类名？**
坐标来自注册 URL，实现类名来自实例自述的 `classFullName` 或代理实际类型，两者都可用于核对"命中的到底是谁"。
