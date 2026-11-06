# 泛化调用架构方案（Generic API Architecture）

> 版本沿革：v1.x 把「接口 jar 在网关 classpath」当主路径并要求服务方自省 → 与功能意义冲突，作废并回退；
> v2.0.0 提出「改旧链路 + 抽公共件」，与「保持现有能力和架构」冲突，作废；
> **本文 v3.0.0 为准**：新功能独立实现，既有能力与既有架构一律不动，迁移留待后续单独立项。

---

## 一、功能定位（判据）

**只做配置，就能把一个 Dubbo 方法声明成一个 HTTP 接口。**

1. 配置人登记：目标接口全限定名、方法名、group（业务分区）、version、是否需要鉴权、走哪些预认证、**参数定义（独立子表）**；
2. 服务方只按标准 Dubbo 语义发布服务，**不依赖本项目任何自定义接口**，临时起的小模块也算数；
3. 网关 classpath **没有**目标接口 jar，也能把 HTTP 翻译成 Dubbo 泛化调用并返回 JSON；
4. 鉴权复用网关既有能力：URL 分区（`/public` 免认证、`/private` 认证 + `@preAuthUtils.accountActived`）、配置项 `need_auth`、预认证链 `pre_auth_beans`（`IApiInstancePreAuth` 的 `AccountActived`/`DeveloperUse`/`SuperAdmin`）。

硬校验标准：**任何"要 jar"或"要服务方改代码"的方案都不能进入主路径。**

---

## 二、与旧能力的关系（本轮最强约束）

| 既有资产 | 处置 |
|---|---|
| `POST /private(genric)` `/public/genric/invoke` + `HttpInvokeForm` 信封 | **原样保留**，请求体/响应体/双层成功标志一字不改 |
| `AbstractGenricApiInstance` / `DubboGenricApiInstance` / `HttpTriGenricApiInstance` | **不改**（含已知的参数名折叠、每次 new+destroy 引用、`support()` 未接线、appName 逐实例枚举）——属旧链路缺陷，迁移期一并处理 |
| `AbstractGenricApiController` 及其 Auth/NoAuth 子类 | **不改**，新链路另立控制器类；Spring 映射按「完整路径 + 方法」判唯一，新旧子路径不冲突 |
| `com.tbs.common.provider`（`IRegistryCenter`/contributor/coordinator） | **不改**（不抽 holder、不加按名寻址入口） |
| 模块依赖图（`web` 不依赖 `shared-pojo`） | **保持**，新链路在 web 侧使用中立配置模型，由 app/system 侧从实体映射 |

**已知的必然重复（有意接受）**：新链路的泛化引用缓存与 `DubboRegisterContributor` 内部那份引用缓存同构但各一份。
`ponytail:` 上限=同一接口可能被两处各建一个引用（类型化引用与 `generic=true` 引用），键与语义互不干扰；
迁移路径=抽公共 `DubboReferenceHolder`（`reference(String/Class, business, version, generic)`）并让 contributor 委托，届时一并修 §7 旧链路缺陷表。

新链路的组件全部为**新增文件**；唯一跨包引用是 web 内部：新目标类继承既有 `AbstractGenricApiInstance`（只复用字段、`equals/hashCode` 与预认证容器，为兼容 `IApiInstancePreAuth` 既有签名而**不修改 SPI**），并显式不走其 `invokeApi(...)` 编排（避开参数名折叠），见 §4.3。

---

## 三、元信息：数据库登记是唯一真源

参数元信息来源四个候选，逐个判：反射（要 jar，排除）、服务方自省（要改代码，排除）、调用方手抄（旧信封，保留但不作新链路）、**库内登记（采用）**。

代价需明确接受：**登记与真实签名会腐化**。接口改了配置没改 → provider 侧报方法未找到。缓解：① 错误信息带接口/方法/期望签名，底层原文进日志 cause；② 后续提供「对照真实签名校验配置」工具（二期）。

### 3.1 表变更

**主表 `sys_generic_api` 增列**

| 列 | 类型 | 语义 | 空值语义 |
|----|------|------|----------|
| `business` | `varchar(100)` | Dubbo group（业务分区），与 provider 中心坐标口径同源 | 空=该维度不约束 |

**新增子表 `sys_generic_api_param`**（一行一个入参，Q2 结论）

| 列 | 类型 | 语义 |
|----|------|------|
| `id` | bigint | 主键，雪花（`@SqlAutoValue(IdAutoProvider.class, insertOnly=true)`） |
| `api_id` | varchar(100) | 关联主表业务键（不引外键，铁律 14） |
| `param_order` | int | 签名顺序，从 0 起；列名避开 `order` 关键字 |
| `param_name` | varchar(100) | 参数名，供命名式入参匹配；可空（只允许位置式调用） |
| `param_type` | varchar(300) | **擦除后的 JVM 声明类型名**，见 §3.2 |
| `required_flag` | bit(1) | 必填标记，缺省 false；列名避开 `required` 关键字在各方言下的引用差异 |
| `description` | varchar(500) | 语义说明，`/meta` 透出给前端 |
| `example` | varchar(500) | 示例值（JSON 字面量文本），供前端表单预填 |
| `flag` + 审计四列 | | 逻辑删除与创建/更新审计，沿用既有模板 |

索引（铁律 15，查询驱动）：`idx_generic_api_param_api_id (api_id)`——唯一查询路径是「按 api_id 取参数列表」；
`idx_generic_api_param_flag (flag)`——状态列必建。不建唯一索引（判重是业务规则，在 Service 断言）。

### 3.2 `param_type` 登记口径（关键）

依据 Dubbo 3.3.4 实际 API：`GenericService.$invoke(String, String[], Object[])` 的类型串要能还原成 `Class`（`ReflectUtils` 的 `name2class`/`desc2classArray` 族），且服务端用真实方法做 realize：

- **基本类型写关键字**：`long` / `int` / `boolean`，写 `java.lang.Long` 会定位不到方法；
- **对象与集合写擦除 FQCN**：`java.lang.String`、`java.util.List`、`com.x.BarDTO`；
- **泛型串不要带**：`java.util.List<com.x.BarDTO>` 无法解析成 Class → 登记 `java.util.List` 即可，
  元素还原由服务端 `PojoUtils.realize(Object[], Class[], java.lang.reflect.Type[])`（该重载已在 3.3.4 jar 中确认存在）
  按真实方法的 `getGenericParameterTypes()` 完成；POJO 参数以 Map 形态传值，服务端按声明类型 realize 成 DTO；
- **同名重载可以区分**：方法定位用「方法名 + 精确参数类型数组」，只要每个参数类型登记准确即可命中；
- 以上口径仍需 §8 的实测矩阵最终确认（我无法运行验证）。

---

## 四、新链路设计（全部新增）

### 4.1 中立配置模型（web，不依赖 shared-pojo）

```
com.tbs.web.genric.config.GenericApiConfig      apiId/interfaceName/methodName/business/version/needAuth/preAuthBeans/params
com.tbs.web.genric.config.GenericApiParamConfig order/name/type/required/description/example
```
由 app 侧控制器从 `SystemGenericApiEntity` + `SystemGenericApiParamEntity` 映射（Controller 只做组装，规则在 Service）。

### 4.2 引用治理与调用

```
com.tbs.web.genric.GenericReferenceHolder   @Component + DisposableBean
    ReferenceConfig<GenericService>：键 interfaceName#business#version
    generic=true、check=false、超时 ProviderConstant.DEFAULT_DUBBO_TIMEOUT、停机统一 destroy
com.tbs.web.genric.GenericArgumentAssembler 纯装配
    入参：GenericApiConfig + Jackson JsonNode（对象=命名式 / 数组=位置式）
    出参：String[] paramTypes + Object[] args
```
分流全交 Dubbo 原生：服务发现（Directory，接口级/应用级皆可）、实例负载均衡（LoadBalance）、集群容错（`setCluster`）、
group/version 匹配（`setGroup`/`setVersion`）、试机直连指定实例（`ReferenceConfigBase.setUrl("dubbo://h:p")`，可逗号多地址）、
标签分流（`AbstractInterfaceConfig.setTag`）。因此**不需要** contribution 枚举与 `IRouteCoordinator` 裁决。

### 4.3 目标与编排

`com.tbs.web.genric.ConfiguredGenricApiInstance extends AbstractGenricApiInstance<Serializable>`

- 复用父类的 `apiId/apiVersion/needAuth/preAuths` 与 `equals/hashCode`，从而**不改 `IApiInstancePreAuth` 签名**即可跑既有预认证链；
- 新增自有入口 `invokeByConfig(JsonNode body, UserDetails u)`：登录断言 → 预认证链 → 装配 → `$invoke` → 耗时/异常写 `ApiInvokeResultVo`；
- **显式不调用父类 `invokeApi(...)`**：其「按参数名折成 Map」的实现会丢重名/null 名参数（旧链路缺陷 P2），新链路以有序装配替代；
- 父类抽象方法 `invoke(Map, vo, Optional)` 实现为断言不支持（`GENERIC.API.UNSUPPORTED_ENTRY`），避免被误用为入口。

响应结构沿用 `ApiInvokeResultVo`（D3：双层成功标志本轮不动）。

### 4.4 HTTP 契约

| 方法 | 路径 | 入参 | 出参 |
|------|------|------|------|
| POST | `/private/genric/call/{apiId}`、`/public/genric/call/{apiId}` | `version`（query，可空）；body 命名式 `{"barCode":"A001"}` 或位置式 `["A001",3]`；无参方法可省略 body | `AjaxResult.data` = `ApiInvokeResultVo` |
| GET | `/private/genric/meta/{apiId}` | `version`（query，可空） | `AjaxResult.data` = `List<GenericApiParamConfig>`（前端据此渲染表单） |

装配规则：

1. 命名式：按登记顺序逐个取名取值；`required=true` 缺省 → 报错；body 出现登记外的键 → 报错，不静默忽略；
2. 位置式：数组长度必须等于登记条数，否则报参数个数不匹配；
3. 未登记参数且 body 非空 → 报错「先登记参数定义」，绝不猜类型；
4. `paramTypes[i]` 取登记 `param_type`；`args[i]` 原样传 JSON 值（POJO 以 Map/List 传，服务端 realize）。

控制器：`AbstractGenricInvokeController`（web，新增）+ app 侧两个具体类（`/private/genric` 认证版、`/public/genric` 免认证版，
沿用 `AuthUtil` 取当前用户）；`@RequestMapping` 前缀与旧控制器相同但子路径不冲突，旧 `/invoke` 不受影响。

---

## 五、Service 层新增（system-api / default-system-impl）

- `IGenericApiService` 新增（旧方法一字不动，铁律 11）：
  `SystemGenericApiEntity getInvokableApi(String apiId)`：存在 + 启用断言；
  `SystemGenericApiEntity getPublicInvokableApi(String apiId)`：上一条 + 禁 `need_auth=true`；
- 新增 `ISystemGenericApiParamService` + 实现：按 `apiId` 取参数定义（`@Cacheable`）、保存（校验后落库）、按 `apiId` 清除；
- 参数校验全部断言化（`order` 从 0 连续不重复、`param_type` 非空、非空 `param_name` 不重复、`api_id` 存在）；
- 缓存名进既有 `CacheNamesConstants`（铁律 13），失效只随参数写路径自身（主表变更不影响参数内容，无跨维派生失效）。

---

## 六、演示与试机（临时小模块即通）

演示服务全部落在 `logging-impl`（**不进 api 模块，网关 classpath 看不到接口类**），以此实测「零 jar」：

- `com.tbs.logging.demo.IStandaloneDemoService` + `StandaloneDemoVo` + `StandaloneDemoServiceImpl`，经 `LoggingServiceConfig` 以 `@DubboService(group = "demo")` 发布；
- 方法覆盖：无参、`String`、`long`（基本类型）、POJO 往返、`List<Vo>`+`int`、枚举、`BigDecimal`、同名不同参重载；
- SQL：`etc/sql_file/demo_generic_api.sql` 给主表行（含 `business='demo'` 与留空两种坐标）+ 子表参数行；
- 试机三组：命名式 body、位置式 body、旧信封 `/invoke`（验证新旧并存）。

## 七、旧链路缺陷登记（本轮不修，迁移期处理）

| 编号 | 缺陷 | 位置 |
|------|------|------|
| P1 | 类型契约泄露给调用方（缺 `exceptdClassName` 时按值运行时类推断，复杂类型必失败） | `DubboGenricApiInstance` |
| P2 | 参数名折叠丢参（重名/null 名被 `HashMap` 静默覆盖） | `AbstractGenricApiInstance#invokeApi` |
| P3 | 按 `appName` 逐实例 new 出 N 个完全相同实例，只执行第一个 | `GenricController`/`PublicGenricController` |
| P4 | 版本匹配依赖候选遍历顺序，与注释声明的优先级不符 | `AbstractGenricApiController` |
| P5 | `support()` 抽象钩子从不调用 | 控制器三件套 |
| P6 | 每次调用 new + destroy `ReferenceConfig` | `DubboGenricApiInstance` |
| P7 | 一个 `/invoke` 吃掉全部 API，无自描述与文档 | `AbstractGenricApiController` |

新链路不复用这些路径，因此不受上述缺陷影响；旧调用方继续按旧行为工作。

---

## 八、必须由实测钉死的项（AI 不代为验证）

| 待测 | 判据 | 若不成立的处理 |
|---|---|---|
| 擦除类型串能否命中方法 | `$invoke` 传 `java.util.List` 调 `List<Vo>` 方法是否成功 | 改为登记精确 desc，装配时折算 |
| 基本类型是否必须关键字 | 目标 `long` 参数分别登记 `long` 与 `java.lang.Long`，各调一次 | 在 Service 校验里强制关键字写法 |
| `List<Vo>` 元素还原 | 传 `[{"name":"a"}]`，服务端是否拿到 DTO | 装配时给元素补 `class` 键 |
| group 坐标 | `business='demo'` 命中；填错坐标时报错清晰 | 调整错误文案与 `check=false` 组合 |
| 重载定位 | 同名不同参两个方法分别调用 | 强制禁止同名重载（Service 校验加严） |

## 九、实施顺序

1. shared-pojo：`SystemGenericApiParamEntity` + 主表 `business` 列；`etc/sql_file` 建表/增列脚本；
2. system：`ISystemGenericApiParamService`/实现 + `IGenericApiService` 两个新方法 + 校验 + 缓存常量 + i18n 中英；
3. web：`config` 两模型、`GenericReferenceHolder`、`GenericArgumentAssembler`、`ConfiguredGenricApiInstance`、`AbstractGenricInvokeController` + i18n 中英；
4. app：`GenricInvokeController`（private）与 `PublicGenricInvokeController`（public），旧控制器不动；
5. logging-impl：演示服务 + `LoggingServiceConfig` 增导出 + `demo_generic_api.sql`；
6. 静态自检（07 清单）+ `mvn -o compile`；运行期实机与 §8 矩阵回填由开发者完成。
7. 管理端 UI（子表参数编辑、`business` 录入）本轮不做——一期用 SQL 登记即可打通试机。

## 十、本轮已落地清单

旧链路（`AbstractGenricApiInstance`、`AbstractGenricApiController`、`DubboGenricApiInstance`、`HttpTriGenricApiInstance`、
`GenricController`、`PublicGenricController`、`common/provider` 全部类）**未改一行**，P1–P7 依旧只在旧链路存在。

| 模块 | 文件 | 动作 |
|------|------|------|
| shared-pojo | `SystemGenericApiEntity` | 新增 `business` 列（不建索引） |
| shared-pojo | `SystemGenericApiParamEntity` | 新增入参定义实体（`idx_..._api_id` + `idx_..._flag`） |
| shared-api | `SystemGenericApiParamMapper` | 新增（`@MapperScan("com.tbs.shared.mapper")` 自动接管） |
| system-api | `IGenericApiService` | 新增 `getInvokableApi`/`getPublicInvokableApi`（旧 `getByApiId` 不动） |
| system-api | `ISystemGenericApiParamService` | 新增：服务模板 + `listByApiId` |
| default-system-impl | `GenericApiServiceImpl` | 新增两个可调用配置方法（规则断言下沉） |
| default-system-impl | `GenericApiParamServiceImpl` | 新增：apiId 归属/顺序必填与不重复/类型必填均用断言，`@Cacheable(GENERIC_API_PARAM)` 与写路径成对失效 |
| default-system-impl | `CacheNamesConstants`/`SystemI18nConstant`/`BuisnessBeanConfig` | 新增缓存名常量、5 个校验文案、服务 Bean 装配 |
| web | `genric.config.GenericApiConfig`、`GenericApiParamConfig` | 新增中立模型（web 不依赖实体包） |
| web | `genric.GenericReferenceHolder` | 新增：`interfaceName#business#version` → `generic=true` 引用，停机统一释放 |
| web | `genric.GenericArgumentAssembler` | 新增：命名式/位置式 body → `paramTypes/args`，类型只取登记值 |
| web | `genric.ConfiguredGenricApiInstance` | 新增：自有编排（避开旧 `invokeApi` 的参数名折叠） |
| web | `genric.GenericApiTargetFactory` | 新增：预认证索引一次性构建，目标不做缓存 |
| web | `genric.AbstractGenricInvokeController` | 新增：`POST /call/{apiId}`、`GET /meta/{apiId}` |
| app | `GenericApiConfigLoader`、`GenricInvokeController`、`PublicGenricInvokeController` | 新增：实体→中立模型映射 + 两个具体入口（private/public） |
| logging-impl | `demo.IStandaloneDemoService`/`StandaloneDemoVo`/`StandaloneDemoStatus`/`StandaloneDemoServiceImpl` + `LoggingServiceConfig` | 新增演示服务（`group=demo`）；接口与实现均不进 api 模块，**有意偏离 07 清单 7.8**，理由写在类注释：验收标准就是网关看不到接口类 |
| i18n | `messages-web*`、`messages-system*` | 新增 10 + 5 个文案，中英同步 |
| etc/sql_file | `app_system_struct.sql`、`upgrade_sys_generic_api.sql`、`demo_generic_api.sql` | 全量脚本同步；增量 ALTER+建表；演示登记行与 10 组 curl 样例 |

未做（一期有意略过）：子表的管理端 UI 与 REST 接口（登记走 SQL 即可打通试机）、新接口的 Swagger 注解
（`web` 不依赖 `open-api-starter`，自描述由 `GET /meta/{apiId}` 承担）、新路径的菜单/API 权限点登记（需你在权限管理里录入 type=3）。

## 变更记录

| 版本 | 时间 | 说明 |
|------|------|------|
| 3.1.0 | 2026-11 | 本轮实施完成：新增落地清单；`required` 列定名为 `required_flag`；演示服务接口不进 api 模块的偏离理由入档；旧链路保持不动 |
| 3.0.0 | 2026-11 | **重写**：确立「既有能力与架构一律不动，新功能只新增文件」；参数元信息改独立子表 `sys_generic_api_param`（Q2）；`/call/{apiId}` 支持命名式 + 位置式双 body（Q3）；分流明确交 Dubbo 原生（发现/负载均衡/集群/group-version/`setUrl` 直连/`setTag`），不改 provider 中心，引用缓存重复一份并标注迁移路径（Q1 修正）；补 `param_type` 擦除登记口径与 Dubbo 3.3.4 API 依据；登记旧链路 P1–P7 供迁移期处理 |
| 2.0.0 | 2026-11 | （作废）改旧链路 + 抽公共 holder 的方案，与「保持现有」冲突 |
| 1.1.0 | 2026-11 | （作废）「Provider 即泛化接口」实施一轮，已回退 |
| 1.0.0 | 2026-11 | （作废）类型化/泛化双链设计，依赖接口 jar 与自省契约 |
