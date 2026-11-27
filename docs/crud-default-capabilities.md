# CRUD 控制器与前端默认能力开发指南

> 适用范围：后端标准增删改查接口（`AbstractCrudController` 及其配套服务模板）与前端默认管理页面能力（`QuickUseManagementView`、`DynamicForm`、`DynamicTable` 等）。
> 读者：需要新增或改造后台管理功能的后端、前端开发者。
> 本文从**设计思路**、**用法**、**使用规范**三个层面说明“为什么这样设计、怎么正确用、哪些约定不能破”。
>
> **强制原则**：项目已提供的默认能力，**能使用就使用默认能力**，禁止重复造轮子；定制优先走配置、插槽、钩子或 `super.*` 扩展。

## 术语与阅读地图

| 术语 | 定义 | 主要源码 |
|------|------|----------|
| CRUD 控制器 | 本文对 `AbstractCrudController<T, K, Q>` 的简称；仓库中没有字面命名为 `CRUDController` 的类 | `system/system-api/src/main/java/com/tbs/app/web/AbstractCrudController.java` |
| HTTP 契约层 | 继承 CRUD 控制器的业务 Controller，负责声明路径、HTTP 方法、权限与 OpenAPI 注解 | `app/src/main/java/com/tbs/app/controller/privates/` 下的 `*Controller` |
| 服务模板 | `IServiceTemplate<T, K>` 及 `AbstractFullFeatureServiceTemplate` 系列，承载事务、审计、增删差异、数据权限 | `shared/shared-api/src/main/java/com/tbs/shared/interfaces/service/` |
| 查询条件对象（Q） | 实现 `IQueryCriteria` 的 `*Criteria`，通过注解把字段映射为 SQL 条件 | `shared/shared-pojo/src/main/java/com/tbs/shared/pojo/criteria/` |
| 响应包络 | `AjaxResult`，固定 `code` / `message` / `data` 三键，出参前由 `ResponseAjaxAspect` 摊平为 JSON | `web/src/main/java/com/tbs/web/result/` |
| 分页包络 | `PageVO<T>{total, data}`，分页参数固定 `pageNum` / `pageSize` | `common/src/main/java/com/tbs/common/data/PageVO.java` |
| 表单配置 | 描述 `DynamicForm` 字段布局的 JSON 对象，按配置键（`configName`）从 props / Vuex / 后端获取 | `app-ui-vue/src/components/DynamicForm/` |
| 配置键（configName） | 表单配置的业务标识，通常集中登记在 `app-ui-vue/src/settings.js` 的 `*FormKey` | `app-ui-vue/src/store/modules/FormConfig.js` |
| 数据获取函数（dataFetcher） | `DynamicTable` 与业务之间的异步数据适配函数，约定返回 `{ data, total }` | `app-ui-vue/src/components/DynamicTable/index.vue` |
| 查询处理器（searchHandler） | `QuickUseManagementView` 调用的业务查询函数，负责把查询/分页参数转成后端接口调用 | `app-ui-vue/src/components/QuickUseManagementView/index.vue` |
| 设备档位 | `phone` / `tablet` / `desktop`，统一由 `ResponsiveMixin` 提供，组件内不得自行判定 | `app-ui-vue/src/mixins/responsive.js` |

阅读顺序建议：先看第一部分建立模型，再按角色阅读第二部分的 2.1（后端）或 2.2（前端），接入前必须过一遍第三部分。

---

# 第一部分　设计思路

## 1.1 总体分层

这套默认能力解决的是同一个问题：**把“列表查询 + 表单增删改”从每个业务页面的重复劳动，收敛为一套可复用的契约**。后端收敛“HTTP 映射以下的通用流程”，前端收敛“表单配置 → 查询参数 → 表格数据”的页面骨架。

```
浏览器
  │  查询表单 / 新增编辑弹窗 / 表格与分页
  ▼
QuickUseManagementView ── DynamicFormWithApi ── DynamicForm ── DynamicOptions / UrlImage / customComponents
  │                                      @submit(formData)
  └── DynamicTable(dataFetcher) ── fetchData ── queryTransformer / sortTransformer / pageTransformer
                                      │
                                      ▼ searchHandler(bodyParams, requestParams)
                              src/utils/request.js
                                      │  HTTP
                                      ▼
业务 Controller（HTTP 路径 / 权限 / OpenAPI）
  │  覆写 get/save/delete/query，通常仅 super.*
  ▼
AbstractCrudController<T,K,Q>（模板方法，无 HTTP 注解）
  │  getService()
  ▼
IServiceTemplate<T,K> ← 业务 Service 接口 ← 逻辑/物理删除服务模板（事务、审计、钩子）
  │  Mapper / SQL 条件
  ▼
AjaxResult { code, message, data } → ResponseAjaxAspect → JSON
```

两端通过三条稳定约定解耦：

1. **响应约定**：所有业务响应都是 `AjaxResult`；前端 `request.js` 只取 `res.data` 作为业务载荷。
2. **分页约定**：`pageNum` / `pageSize` 走 query，复杂条件走请求体；返回 `{ data: [], total }`。
3. **字段约定**：后端 `Q` 的条件后缀（如 `xxxLike`）与前端表单字段 / 转换器保持一致，见 3.1。

## 1.2 后端 CRUD 控制器

### 1.2.1 职责边界

`AbstractCrudController` 继承 `BaseController`，只提供四个**不带任何 Spring MVC 注解**的模板方法（`AbstractCrudController.java:20-74`）：

| 模板方法 | 内部调用 | `data` 的实际类型 | 默认消息 |
|----------|----------|-------------------|----------|
| `get(K id)` | `IServiceTemplate.queryById(id)` | `T` | `common.success` |
| `save(T entity)` | `IServiceTemplate.save(entity)` | `List<T>` | `crud.save.success`，`type=SUCCESS` |
| `delete(K id)` | `IServiceTemplate.deleteById(id)` | `T` | `crud.delete.success`，`type=SUCCESS` |
| `query(Q criteria, Long pageNum, Long pageSize)` | `queryAndPage(criteria, QueryPageModel.of(...))` | `PageVO<T>` | `common.success` |

子类的职责只有三件事：声明 HTTP 契约、声明权限、注入服务实现。`getBusinessName()` 由 `BaseController` 要求，用于日志/审计标识。业务校验、事务、数据权限、增删差异全部在 Service 层；Controller 只做参数兜底、Form→Entity 组装和必要的编排。

**关键设计取舍：基类故意不加 `@GetMapping` / `@PostMapping`。** 这样同一套模板可以服务于不同分区（`/public/**`、`/private/admin/**`）和不同权限级别，代价是 13 个业务 Controller 各自重复声明四个映射，因此必须靠规范固定写法（见 3.2）。

### 1.2.2 泛型与核心抽象

```java
public abstract class AbstractCrudController<T, K, Q extends IQueryCriteria> extends BaseController {
    public abstract IServiceTemplate<T, K> getService();
}
```

- `T`：实体/出参模型；`K`：主键类型；`Q`：查询条件，必须实现 `IQueryCriteria`。
- `IServiceTemplate<T, K>` 由 `ISaveableService`（`save` 返回 `List<E>`）、`IDeleteableService`（`deleteById` 返回 `E`）、`ISearchableService`（`queryAndPage` 返回 `PageVO<E>`）组合而成，并提供 `useDataScope()` 默认 `false`。
- 服务模板 `AbstractFullFeatureServiceTemplate` 在 `deleteById` / `deleteByIds` / `save` 上声明 `@Transactional(rollbackFor = Exception.class)`，并提供 `deletePending()`、`handleDelete()`、`insertNew()`、`updateBy()`、`compareAndUpdateValue()`、`pageVoBuildType()`、`inUseDataScope()` 等钩子；删除策略通过 `AbstractLogicDeleteFullFeatureServiceTemplate`（默认首选）与 `AbstractPhysicDeleteFullFeatureServiceTemplate` 二选一。
- Controller 层没有 `beforeSave` / `afterGet` 钩子。扩展方式是**覆写四个模板方法并调用 `super.*`**，或新增领域端点并调用 Service 方法。

### 1.2.3 响应与分页模型

`AjaxResult` 是标准包络，`ResponseAjaxAspect` 在 `beforeBodyWrite` 中把 `AjaxResult` 转成 `Map` 输出，并把异常统一转成错误响应；`@NoWrapped` 可跳过包装，`@Wrapped` 可指定 `IResultCreator`。`@AjaxResponseType` 只影响 Knife4j/OpenAPI 文档，不参与运行时包装：它用 Class 引用规避泛型擦除，把 `data` 的真实 Schema 写回 200 响应。

分页链路：`QueryPageModel.of(pageNum, pageSize)` 缺省 `1 / 10`，且要求都大于 0；`from() = (page - 1) * perPage`；`PageVO<T>` 只有 `total` 和 `data`，不回显页码与页大小。

**已知类型偏差（必须知道，不要照抄）**：现有部分 Controller 的 `@AjaxResponseType` 把 `save` 标成单实体、`delete` 标成 `void.class`，但运行时 `data` 分别是 `List<T>` 与 `T`。新增代码应按真实运行类型声明，见 3.2 与 3.5。

## 1.3 前端默认能力

### 1.3.1 组件分层

| 层 | 组件 | 职责 | 何时直接使用 |
|----|------|------|--------------|
| 页面编排层 | `QuickUseManagementView` | 组装“查询表单 + 表格 + 新增/编辑弹窗 + 导出”，提供标准 CRUD 交互骨架 | 绝大多数后台管理列表页 |
| 配置加载层 | `DynamicFormWithApi` | 按 `configName` 取表单配置，透传插槽与校验方法 | 需要单独用配置驱动表单时 |
| 渲染层 | `DynamicForm` | 按 `config.fields` 生成栅格表单；内置 5 种字段类型，其余交给 `customComponents` | 自定义页面中的查询区/编辑区 |
| 数据展示层 | `DynamicTable` | 列配置、分页、排序、选择、操作列、加载状态；手机档自动切换 `MobileCardList` | 非标准 CRUD 的列表/详情表格 |
| 字段/设备适配层 | `DynamicOptions`、`UrlImage`、`MobileCardList` | 动态选项、图片地址字段、手机卡片视图 | 一般不直接使用 |
| 配置生产层 | `FormEditor` + `views/form/FormCreator.vue` + 配置生成服务 | 从 CRUD 控制器元信息自动生成默认配置，可视化确认/微调后保存，形成“配一次、多处用”的闭环 | 需要新增/修改配置时 |

`QuickUseManagementView` 设置 `inheritAttrs: false`，把 `$attrs` 全部透传给内部 `DynamicTable`，因此 `card-fields`、`title-field`、`selection`、`responsive` 等表格属性直接写在包装器上即可。

### 1.3.2 配置驱动与数据流

表单配置的优先级（`DynamicFormWithApi.vue:149-199`）：**直接传 `config` > `loadConfigMethod(configName)` > Vuex `formConfigs/fetchFormConfig` > GET `apiUrl?key=configName`**。实际项目通常传 `configName`，由 Vuex 命中缓存或调用 `/private/view/form/{key}` 取配置。

默认配置自动生成链路：`FormCreator` 先通过 `GET /private/view/form/controllers` 列出所有 `AbstractCrudController` 子类，再按类名或类级路径调用 `GET /private/view/form/generate?controller={name}&scene={search|save|edit}`。服务端用 `ResolvableType` 解析控制器的泛型 `Q`（查询场景）或 `T`（新增/编辑场景），按字段后缀与类型推导 `input` / `select` / `date-range` / `url-image`，返回可直接装载的 `FormConfig` 草稿；前端预览确认后再写入编辑器微调、保存。生成结果是草稿，**不替代人工确认**。

查询链路：

```
DynamicForm @submit(formData)
  → QuickUseManagementView.handleSubmit：保存 searchForm、页码归 1、refresh()
  → DynamicTable.loadData：合并 { pagination, sort, filter, query }
  → dataFetcher = QuickUseManagementView.fetchData
  → queryTransformer(query + searchForm) / sortTransformer(sort) / pageTransformer(pagination)
  → searchHandler(bodyParams, requestParams)
  → request.js → POST {base}/query?pageNum=&pageSize=  → res.data
  → { data: PageVO.data, total: PageVO.total } → 表格 / 卡片
```

默认转换规则：

- 分页 → `{ pageNum, pageSize }`（query 参数）。
- 排序 → Element UI 的 `ascending` / `descending` 转成 `{ [`${prop}Order`]: 'ASC' | 'DESC' }`，并入 body。
- `rangeQueryTransform`（`utils/query-help.js`）把 `createTimeRange: [a, b]` 转成 `createTimeGreaterThan` 与 `createTimeLessThan`，正好匹配后端条件后缀约定。

增改删链路：新增按钮仅在传入 `addFormConfigName` 时出现；弹窗用同一套 `DynamicFormWithApi` 渲染，`value` 为编辑行 `{ ...row }`；`addHandler` / `editHandler` / `deleteHandler` 由业务提供，成功或确认删除后统一 `refresh()`。消息提示由后端 `message` 与 `request.js` 统一处理，组件不重复弹成功提示。

### 1.3.3 设计取舍

| 取舍 | 收益 | 代价 / 应对 |
|------|------|-------------|
| 表单由配置生成，而非每页手写 `el-form` | 查询区/弹窗统一布局、折叠与 i18n 按钮，配置可复用 | 复杂联动靠 `customComponents`、字段槽或默认作用域槽补足 |
| 表格只认 `dataFetcher` 返回 `{ data, total }` | 与具体后端解耦，静态数据、Dubbo 泛化接口都能接 | 需写 `searchHandler` 适配层；本项目的标准适配已内置在包装器中 |
| 表格内部维护分页/排序/过滤状态，同时提供受控 props 与事件 | 简单场景零状态代码，复杂场景可外部接管 | `computedTotal = total || internalTotal`，外部传 0 会回退内部值（见 3.5） |
| 手机档切换卡片视图，列名/操作/选择复用同一份 columns | 不为移动端写第二套页面 | 使用作用域插槽时必须显式传 `hasActionsSlot`（见 3.5） |
| 配置存 Vuex + 后端 | 减少首屏请求，支持运营态改配置 | 需要保证配置键稳定、字段类型与组件支持的类型一致 |
| 全局栈适配交给 `element-ui.scss` 与 `ResponsiveMixin` | 页面不重复写断点 | 固定宽度/高度和 hover 操作仍需页面自理，遵循 `docs/guideline/05-前端开发规范.md` |

---

# 第二部分　用法

## 2.1 后端接入

### 2.1.1 接入步骤

1. 定义实体 `T`、主键类型 `K`、查询条件 `Q extends IQueryCriteria`。
2. 定义 `IXxxService extends IServiceTemplate<T, K>`；实现按删除策略继承 `AbstractLogicDeleteFullFeatureServiceTemplate`（默认）或 `AbstractPhysicDeleteFullFeatureServiceTemplate`，按需覆写服务模板钩子。
3. 定义 `*Criteria`，通常继承 `DefaultEntityCriteria`，用 `@QueryMapping` + `@QueryField(contrast = ...)` 声明条件、`@QueryOrderField` 声明排序。动态排序字段使用 `OrderedModel` + `OrderConstants.ORDERED_MODEL`，与前端默认生成的 `xxxOrder` 方向值配套（`OrderedModelDeserializer` 同时接受 `"DESC"` 字符串和 `{"ordered":"DESC"}` 对象）；固定方向才写 `ASC` / `DESC` 常量。
4. 定义 `*Controller extends AbstractCrudController<T, K, Q>`，实现 `getService()` 与 `getBusinessName()`。
5. 重声明 `get` / `save` / `delete` / `query` 四个方法，补齐路径、HTTP 方法、`@PreAuthorize`、`@Operation`、`@ApiResponse`、`@AjaxResponseType`；方法体调用 `super.*`。

### 2.1.2 最小完整示例

以下以虚构的 `widget` 业务为例，类名、路径、配置键按实际业务替换。

```java
/**
 * 组件管理控制器
 *
 * <p>标准 CRUD 由 AbstractCrudController 提供；本类只负责 HTTP 暴露与权限声明，
 * 业务校验与事务位于 IWidgetService 实现中。</p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("private/admin/widget")
@Tag(name = "组件管理接口", description = "组件登记管理相关操作接口")
public class WidgetController extends AbstractCrudController<WidgetEntity, Long, WidgetCriteria> {

    @Resource
    private IWidgetService widgetService;

    @Override
    public IServiceTemplate<WidgetEntity, Long> getService() {
        return widgetService;
    }

    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(WidgetEntity.class)
    @Operation(summary = "获取组件", description = "根据 ID 获取组件详情")
    @ApiResponse(responseCode = "200", description = "成功获取组件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "id", description = "组件ID", required = true)
                          @PathVariable Long id) {
        return super.get(id);
    }

    @Override
    @PostMapping("save")
    @AjaxResponseType(value = List.class, genericArgs = {WidgetEntity.class})
    @Operation(summary = "保存组件", description = "无 ID 新增，携带 ID 更新")
    @ApiResponse(responseCode = "200", description = "成功保存组件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult save(@Parameter(name = "entity", description = "组件信息", required = true)
                           @RequestBody WidgetEntity entity) {
        return super.save(entity);
    }

    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(WidgetEntity.class)
    @Operation(summary = "删除组件", description = "根据 ID 删除组件")
    @ApiResponse(responseCode = "200", description = "成功删除组件",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(@Parameter(name = "id", description = "组件ID", required = true)
                             @PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {WidgetEntity.class})
    @Operation(summary = "查询组件列表", description = "根据条件分页查询组件列表")
    @ApiResponse(responseCode = "200", description = "成功查询组件列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件")
                            @RequestBody(required = false) WidgetCriteria criteria,
                            @Parameter(name = "pageNum", description = "页码")
                            @RequestParam(required = false, defaultValue = "1") Long pageNum,
                            @Parameter(name = "pageSize", description = "每页大小")
                            @RequestParam(required = false, defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    @Override
    public String getBusinessName() {
        return "组件管理";
    }
}
```

服务接口与查询条件示意：

```java
public interface IWidgetService extends IServiceTemplate<WidgetEntity, Long> {
    /**
     * 提交组件并同时保存参数，由实现层保证同一事务
     *
     * @param form 组件与参数的组合表单
     */
    void submitWithParams(WidgetSaveForm form);
}
```

```java
@Data
public class WidgetCriteria extends DefaultEntityCriteria {

    /**
     * 构造方法
     */
    public WidgetCriteria() {
        super(WidgetEntity.class);
    }

    /**
     * 按组件名称模糊查询，字段名后缀与前端转换约定保持一致
     */
    @QueryMapping("name")
    @QueryField(contrast = ContrastEnum.LIKE)
    private String nameLike;

    /**
     * 按创建时间动态排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;
}
```

### 2.1.3 请求与响应

| 操作 | 请求示例 | `data` |
|------|----------|--------|
| 详情 | `GET /private/admin/widget/get/12` | `WidgetEntity` |
| 新增/更新 | `POST /private/admin/widget/save`，body 为实体，有 `id` 即更新 | `List<WidgetEntity>` |
| 删除 | `POST /private/admin/widget/delete/12` | 被删除的 `WidgetEntity` |
| 分页 | `POST /private/admin/widget/query?pageNum=1&pageSize=10`，body 为条件对象 | `{ "total": 25, "data": [...] }` |

成功响应形如：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { "total": 25, "data": [] }
}
```

### 2.1.4 常见后端场景

| 场景 | 做法 | 参考 |
|------|------|------|
| 保存前轻量校验 | 覆写 `save`，断言后调用 `super.save(entity)`；复杂规则放 Service | `EventSubscriptionController`、`ConditionRuleController` |
| 保存/删除后上报事件 | 覆写后调用 `super.*`，再用事件服务上报，不在 Controller 内写事务 | `AdminUserController` |
| 状态启停、生命周期 | 新增领域端点，Controller 只组装 Form/参数，Service 提供领域方法 | `RoleController`、`AppVersionController` |
| 聚合详情 VO | 新增 `detail` 端点，返回 `AjaxResponseType(AppVersionDetailVO.class)` | `AppVersionController` |
| 查询结果二次加工 | 直接调用 `getService().queryAndPage(...)` 后用 `PageUtil.build` 异步补充字段 | `AdminCompanyController`、`AdminDepartmentController` |
| 数据权限 | Service 覆写 `useDataScope()` 返回 `true`，实现 `inUseDataScope(context)`；Controller 不拼过滤条件 | `SimpleSystemUserServiceImpl` |

覆写示例：

```java
/**
 * 保存前补全归属信息
 *
 * @param entity 组件信息
 * @return 统一响应
 */
@Override
public AjaxResult save(WidgetEntity entity) {
    AssertUtil.isTrue(entity == null).i18n("widget.entity.empty")
        .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    return super.save(entity);
}
```

## 2.2 前端接入

### 2.2.1 标准管理页（推荐入口）

以用户管理页（`app-ui-vue/src/components/UserAdminView/index.vue`）为原型，一个标准页面由四部分组成。下面仍以 `widget` 为示意业务：

**① API 封装**（`src/api/admin_user.js`）：遵守 `request.js` 统一封装，分页走 `params`，条件走 `data`。

```javascript
import request from '@/utils/request'

/**
 * 管理员查询用户
 * @param {Object} data - 查询条件
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise} 查询结果
 */
export function adminQueryUser(data, pageNum, pageSize) {
  return request({
    url: '/private/admin/user/query',
    params: { pageNum, pageSize },
    method: 'post',
    data
  })
}
```

**② 表单配置**：优先在 `FormCreator` 点击「从 Controller 生成」，选择目标控制器与场景后得到默认配置，确认后再微调；当然也可以从零手工配置。配置键在 `settings.js` 登记，运行时由 `formConfigs` 缓存。

```javascript
// 与自动生成能力对应的 API 封装（src/api/view.js）
import { listCrudControllers, generateFormConfigFromController } from '@/api/view'

const options = await listCrudControllers()
const configResponse = await generateFormConfigFromController(options.data[0].name, 'search')
const formConfig = configResponse.data
```

生成的默认配置形如：

```javascript
{
  "title": "查询",
  "expandable": true,
  "expanded": true,
  "labelWidth": "100px",
  "gutter": 16,
  "span": 8,
  "fields": [
    { "prop": "userNameLike", "label": "用户名", "type": "input", "props": { "clearable": true } },
    {
      "prop": "flagEquals", "label": "状态", "type": "select",
      "props": { "options": [{ "label": "启用", "value": 1 }, { "label": "禁用", "value": 0 }] }
    },
    { "prop": "createTimeRange", "label": "创建时间", "type": "date-range" },
    { "prop": "companyIdEquals", "label": "公司", "type": "dynamic-options", "props": { "apiUrl": "/private/admin/company/query" } }
  ]
}
```

**③ 列配置与页面组装**：

```vue
<template>
  <div class="widget-management">
    <quick-use-management-view
      ref="managementView"
      :main-title="$t('widget.title')"
      :config-name="widgetSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      title-field="name"
      :card-fields="['name', 'flag', 'createTime']"
      :add-form-config-name="widgetAddFormKey()"
      :add-handler="saveWidget"
      :edit-form-config-name="widgetEditFormKey()"
      :edit-handler="saveWidget"
      :delete-handler="deleteWidget"
      :query-transformer="rangeQueryTransform"
    >
      <template #actions-dropdown="{ row }">
        <el-dropdown-item command="enable" @click.native="toggleStatus(row)">
          {{ $t('common.enable') }}
        </el-dropdown-item>
      </template>
    </quick-use-management-view>
  </div>
</template>

<script>
import { rangeQueryTransform } from '@/utils/query-help'
import { adminQueryWidget, saveWidget, deleteWidget, toggleWidgetStatus } from '@/api/widget'
import { widgetSearchFormKey, widgetAddFormKey, widgetEditFormKey } from '@/settings'

export default {
  name: 'WidgetManagementView',
  data() {
    return {
      columns: [
        { prop: 'name', label: this.$t('widget.name'), minWidth: 160 },
        { prop: 'flag', label: this.$t('widget.status'), slot: 'flag' },
        { prop: 'createTime', label: this.$t('common.createTime'), sortable: 'custom', minWidth: 180 }
      ]
    }
  },
  methods: {
    widgetSearchFormKey,
    widgetAddFormKey,
    widgetEditFormKey,
    rangeQueryTransform,
    saveWidget,
    deleteWidget,
    /**
     * 查询处理器：body 为查询/排序条件，params 为分页参数
     * @param {Object} bodyParams - 查询与排序条件
     * @param {Object} requestParams - 分页参数
     * @returns {Promise} request.js 响应，data 为 PageVO
     */
    async searchHandler(bodyParams, requestParams) {
      return adminQueryWidget(bodyParams, requestParams.pageNum, requestParams.pageSize)
    },
    /**
     * 状态切换：业务端点调用成功后刷新表格
     * @param {Object} row - 当前行数据
     * @returns {Promise} 请求结果
     */
    async toggleStatus(row) {
      await toggleWidgetStatus(row.id, row.flag === 1 ? 0 : 1)
      return this.$refs.managementView.refresh()
    }
  }
}
</script>
```

**④ 交互闭环**：

- 查询：`DynamicForm @submit` → 保存 `searchForm` → 页码归 1 → 表格刷新。
- 重置：清空 `searchForm` → 表格刷新 → 冒泡 `reset`。
- 新增/编辑：弹窗表单回填行数据 → `addHandler` / `editHandler` → 成功后刷新。
- 删除：`$confirm` 二次确认 → `deleteHandler` → 成功后刷新。
- 导出：包装器内置导出按钮，导出 `originalData`（当前页原始数据）。

### 2.2.2 核心组件参考

#### DynamicForm

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | String | `''` | 标题；优先级高于 `config.title` |
| `config` | Object | `{}` | 表单配置，结构见下表 |
| `value` | Object | `{}` | 表单数据，深度监听外部更新 |
| `formProps` | Object | `{}` | `v-bind` 到 `el-form`，可传 `rules`、`label-position` 等 |
| `customComponents` | Object | `{}` | 字段类型 → 组件映射，扩展自定义字段 |

| Events | 参数 | 触发时机 |
|--------|------|----------|
| `input` | `formData` | 任一字段值变化 |
| `reset` | — | 点击重置 |
| `submit` | `formData` | 点击提交（提交前请先调用 `validate()`） |
| `expand-change` | `boolean` | 折叠状态切换 |

| 插槽 | 作用域参数 | 说明 |
|------|------------|------|
| `header` | — | 自定义头部 |
| `before-form` / `after-form` | — | 表单首尾内容 |
| `default` | `{ form, formData, set-value }` | **存在时关闭自动布局**，完全自定义布局 |
| `actions` | `{ form, formData, reset, submit }` | 自定义按钮区 |
| `form-item-${prop}` | `{ field, name, set-value, value }` | 按字段替换渲染 |

| 方法 | 说明 |
|------|------|
| `validate(callback)` / `validateField(props, callback)` / `clearValidate(props)` | 代理 `el-form` 校验能力 |
| `resetForm()` | 重置为初始化值并清校验 |
| `submitForm()` | 触发 `submit` 事件 |
| `getFormData()` / `setFieldValue(prop, value)` | 读取/写入表单数据 |

表单配置结构：

| 字段 | 类型 | 说明 |
|------|------|------|
| `title` / `expandable` / `expanded` | String / Boolean | 标题、是否可折叠、初始是否展开；`expandable === false` 关闭折叠 |
| `labelWidth` / `gutter` / `span` | String / Number | 标签宽度、栅格间距、默认列宽 |
| `responsive` | Object | 全局响应式跨度，键为 `xs/sm/md/lg/xl` |
| `fields[]` | Array | 字段列表，`prop` 必须唯一，同时用于数据键和字段槽名 |
| `fields[].type` | String | `input`、`select`、`date-range`、`dynamic-options`、`url-image` 或 `customComponents` 的键 |
| `fields[].props` | Object | `v-bind` 到具体控件；`select` 的选项写在 `props.options` |
| `fields[].itemProps` | Object | `v-bind` 到 `el-form-item` |
| `fields[].span` / `fields[].responsive` | Number / Object | 字段级栅格覆盖 |
| `fields[].onChange` | Function | **仅 `date-range` 变更时触发**，签名 `(val, formData)` |

内置类型的约束：`date-range` 默认值为 `[]`，其他字段默认 `undefined`；`url-image` 强制占满 24 栅格；`dynamic-options` 的数据源优先级为 `options` > `dictKey` > `apiUrl`。

#### DynamicFormWithApi

Props 在 `DynamicForm` 基础上增加：`configName`（String）、`config`（Object，最高优先级）、`loadConfigMethod`（Function）、`apiUrl`（默认 `/private/view/getSearchForm`）、`useStore`（默认 `true`）。事件只透传 `submit` / `reset`，其余作用域插槽透传给 `DynamicForm`，校验方法同样可透传调用。**实际项目应始终传 `configName` 或 `config`**，不要依赖默认 `apiUrl`（见 3.5）。

#### DynamicTable

| 分类 | Prop（默认值） |
|------|----------------|
| 数据 | `dataFetcher(null)`、`data([])`、`columns([])` |
| 外观 | `autoLoad(true)`、`loadingText('')`、`border(true)`、`stripe(false)`、`size('medium')`、`height/maxHeight`、`fit(true)`、`highlightCurrentRow(false)` |
| 选择/序号 | `selection(false)`、`selectionWidth(55)`、`reserveSelection(false)`、`selectable`、`index(false)`、`indexLabel('#')`、`indexWidth(55)` |
| 分页 | `pagination(true)`、`paginationLayout('total, sizes, prev, pager, next, jumper')`、`pageSizes([10,20,50,100])`、`pagerCount(7)`、`paginationBackground(true)`、`total(0)`、`currentPage(1)`、`pageSize(10)` |
| 操作列 | `actions(null)`、`actionsLabel('')`、`actionsWidth(150)`、`actionsFixed('right')`、`actionsAlign('center')` |
| 响应式 | `responsive(true)`、`cardFields(null)`、`titleField('')` |

`dataFetcher(params)` 契约：

```javascript
/**
 * 表格数据获取函数
 * @param {Object} params - { pagination: { page, size }, sort, filter, query }
 * @returns {Promise<{data: Array, total: Number}>} 当前页数据与总条数
 */
function dataFetcher(params) {
  return Promise.resolve({ data: [], total: 0 })
}
```

| Events | 说明 |
|--------|------|
| `update:data` / `load-success(result)` / `load-error(error)` | 数据加载结果 |
| `update:currentPage` / `current-page-change`、`update:pageSize` / `page-size-change` | 分页变化 |
| `sort-change` / `filter-change` | 排序、过滤变化（组件会自动重载） |
| `current-change` / `row-click` / `row-dblclick` / `cell-click` / `cell-dblclick` / `selection-change` | 表格交互转发 |

| 插槽 | 作用域参数 | 说明 |
|------|------------|------|
| `toolbar` | — | 表格上方工具栏 |
| 动态列槽（名同 `column.slot`） | `{ row, $index, column }` | 自定义列渲染 |
| `actions` | `{ row, $index }` | 操作列内容 |

常用方法：`refresh(params)`、`loadData(params)`、`updateColumns(columns)`、`updatePagination({ page, size })`、`handleSortChange`、`handleFilterChange`、`handleAction(action, row, index)`、`getSelection()` / `clearSelection()` / `setSelection(rows)`、`formatValue(row, column)`。

列配置是 Element UI `el-table-column` 属性的子集：`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`className`、`labelClassName`、`sortable`、`sortMethod`、`sortBy`、`sortOrders`、`resizable`、`formatter`、`showOverflowTooltip`、`filterMethod`、`filters`、`filterPlacement`、`filterMultiple`、`columnKey`、`renderHeader`，另有 `render(h, scope)` 与 `slot` 两种自定义渲染（**优先于 `formatter`**）。默认 `showOverflowTooltip: true`、`resizable: true`、`filterMultiple: true`。

操作项配置：`{ label, type, size, icon, disabled, loading, handler(row, index) }`，其中 `disabled` / `loading` 支持布尔值或 `(row, index) => boolean`。

#### QuickUseManagementView

必填 props：`configName`（String）、`searchHandler`（Function）。常用可选 props：`mainTitle`、`columns`、`autoLoad`、`addFormConfigName`、`addHandler`、`editFormConfigName`、`editHandler`、`deleteHandler`、`editable`、`deleteable`、`deleteConfirmText`、`queryTransformer`、`sortTransformer`、`pageTransformer`、`actions`、`addButtonText` / `editActionButtonText` / `deleteActionButtonText`。

插槽：`actions`（旧版兼容）、`actions-dropdown`（更多操作下拉）。事件：`add-success` / `add-error` / `edit-success` / `edit-error` / `delete-success` / `delete-cancel` / `action-command` / `load-error` / `reset` / `update:columns` / `add-cancel` / `edit-cancel`。方法：`refresh()`、`getSelection()`、`clearSelection()`、`updateColumns()`。

`searchHandler` 的返回值必须保留 `request.js` 的完整响应对象（包装器内部取 `result.data.data` 与 `result.data.total`），不能直接返回 `PageVO`。

### 2.2.3 其他前端场景

**单独使用 DynamicForm（非列表页）**：

```vue
<dynamic-form ref="form" :config="config" :form-props="{ rules }" @submit="handleSubmit" />
```

提交前校验：

```javascript
this.$refs.form.validate(valid => {
  if (valid) {
    this.handleSubmit(this.$refs.form.getFormData())
  }
})
```

**单独使用 DynamicTable（静态数据 / 受控分页）**：不传 `dataFetcher` 时组件只使用 `data`，不会自动分页请求；需要外部分页时传 `:data / :total / :current-page / :page-size`，监听 `@current-page-change` / `@page-size-change` 自行请求，参考 `UserAdminView/UserLoginInfoView.vue`。

**扩展字段类型**：传 `customComponents: { 'my-type': MyComponent }`，自定义组件需接收 `value` 并在值变化时触发 `input` 或 `change`；字段配置写 `{ prop: 'x', type: 'my-type' }`。

**生成与修改配置**：`FormCreator.vue` 点击「从 Controller 生成」→ 选择控制器/场景 → 生成配置草稿 → 预览 JSON → `$confirm` 确认应用到编辑器 → 在 `FormConfigEditor` 中微调 → `getData()` 输出配置 → `saveFormConfig` / `updateFormConfig` 落库；`FormManager.vue` 可预览。新增字段类型时要同时改生成器的类型推导规则、`FormEditor/index.vue` 的类型分支和对应 `ConfigComponents/*Config.vue`。

**纯原生表格**：`views/admin/version/VersionList.vue` 展示了直接用 `el-table` 的场景；新列表页优先 `DynamicTable`，以便自动获得手机卡片视图。

## 2.3 端到端调用流程

1. 后端登记 Controller 与四个端点，前端在 `src/api/{biz}.js` 封装 `query` / `save` / `delete`。
2. 用 `FormCreator` 的「从 Controller 生成」得到查询/新增/编辑配置草稿，确认微调后保存；在 `settings.js` 登记配置键，条件名与 `*Criteria` 字段保持一致。
3. 页面用 `QuickUseManagementView` 组装；首次挂载触发表格 `loadData`，默认 `page=1, size=10`。
4. 用户提交查询表单 → `searchForm` 更新 → 页码归 1 → `searchHandler(body, { pageNum, pageSize })` → 后端 `query`。
5. 后端 `super.query` → Service `queryAndPage` → `PageVO` → `AjaxResult` → `request.js` → 表格显示。
6. 新增/编辑/删除 → 对应 Handler → `super.save/delete` → 成功消息由后端返回 → 包装器刷新表格。
7. 手机档下表格自动切为 `MobileCardList`，列与操作复用同一配置，无需额外页面。

---

# 第三部分　使用规范

## 3.1 命名约定

### 后端

| 对象 | 约定 | 示例 |
|------|------|------|
| Controller | `{Biz}Controller`；管理端 `Admin{Biz}Controller`，放 `controller/privates` 或 `controller/publics` | `ThirdPartyController`、`AdminUserController` |
| CRUD 方法名与路径 | 固定 `get` / `save` / `delete` / `query`，路径 `get/{id}`、`save`、`delete/{id}`、`query` | 见 2.1.2 |
| Service | 接口 `I{Biz}Service extends IServiceTemplate<Entity, K>`；实现按删除策略继承服务模板 | `IThirdPartyAppService` |
| 查询条件 | `*Criteria` / `Common*QueryCriteria`，实现 `IQueryCriteria`，一般继承 `DefaultEntityCriteria(Entity.class)` | `CommonThirdPartyAppCriteria` |
| 条件字段后缀 | `xxxEquals`、`xxxLike`、`xxxIn`、`xxxGreaterThan`、`xxxLessThan`、`xxxStart`、`xxxEnd`、`xxxOrder` | `userNameLike`、`createTimeRange` |
| 分页参数 | 固定 `pageNum` / `pageSize`，query 传参 | — |
| Form / VO | 普通 `Serializable` DTO，`@Data` + `@Schema(description)`，必填标 `requiredMode = REQUIRED` | `GenericApiSaveForm` |
| i18n Key | 常量集中在 `*I18nConstant`，中英文资源成对维护 | `crud.save.success` / `crud.delete.success` |

### 前端

| 对象 | 约定 | 示例 |
|------|------|------|
| 组件目录 | `components/{Name}/index.vue`，组件 `name` 与目录一致，PascalCase | `DynamicForm/index.vue` |
| 包装组件后缀 | 远程加载配置用 `WithApi`；设备适配用 `Mobile*` | `DynamicFormWithApi`、`MobileCardList` |
| API 文件 | `src/api/{biz}.js`，一业务域一文件，函数名与后端操作对应 | `admin_user.js` 的 `adminQueryUser` / `saveUser` |
| 配置键 | 统一在 `settings.js` 定义 `*FormKey`，业务代码引用函数/常量，不写魔法字符串 | `userSearchFormKey` |
| 字段类型 | 小写连字符：`input`、`select`、`date-range`、`dynamic-options`、`url-image` | — |
| 列配置 | camelCase，与 `el-table-column` 属性同口径 | `minWidth`、`showOverflowTooltip` |
| 作用域槽 | 列槽名等于 `column.slot`；字段槽名为 `form-item-${prop}` | — |
| 设备判断 | 只用 `ResponsiveMixin` 的 `isPhone` / `isTablet` / `isDesktop` / `isTouch` | — |
| 文案 | 用户可见文案一律 `$t('key')`，中英文资源同步 | `common.query` / `common.reset` |

## 3.2 配置约束

### 后端（强制）

1. 基类方法不带 HTTP 注解；子类必须逐个重声明四个映射，且保留方法名与参数顺序，否则前端默认路径无法复用。
2. 私有接口至少声明 `@PreAuthorize("@preAuthUtils.accountActived")`；权限点按需要登记 type=3 API 权限，数据权限只用 Service 钩子。
3. 注解组合不可缺：`@Operation`（summary + description）、`@ApiResponse`、`@AjaxResponseType`、`@Parameter`。
4. `@AjaxResponseType` 必须声明**运行时真实 data 类型**：`get → Entity.class`，`save → List.class + genericArgs = {Entity.class}`，`delete → Entity.class`（若覆写为无数据成功响应，则改为 `void.class`），`query → PageVO.class + genericArgs = {Entity.class}`。
5. 统一返回 `AjaxResult` / `successResult(...)`，禁止手工 `new AjaxResult` 或返回裸对象；业务校验用 `AssertUtil`，事务放 Service。
6. `pageNum` / `pageSize` 缺省由 `QueryPageModel.of` 兜底为 `1 / 10`，但显式传 0 或负数会触发用户输错异常；前端不要传 0。
7. 查询条件注解必须与数据库列一致：`@QueryMapping("列名")` + `@QueryField(contrast = ...)`，空值默认被忽略；逻辑删除用 `@LogicDeleteEntity`。
8. i18n Key 必须中英文两份都有；新增错误码同步 `AjaxResult`、前端 `settings.js` 与 `docs/guideline/04-接口设计与上线规范.md`。

### 前端（强制 / 推荐）

1. `config.fields[].prop` 必须唯一，且必须与后端 `*Criteria` 字段名 / 实体字段名对齐；字段名后缀转换只能由Transformer处理。
2. `configName` 必须能解析到合法配置；直接传 `config` 时无需 `configName`。表单配置中不要出现组件不支持的 `type`。
3. `DynamicForm` 的 `default` 作用域槽与自动布局互斥；用了默认槽就要自己写 `el-form-item` 并调用 `set-value` 回填数据。
4. `select` 的选项放 `field.props.options`；`dynamic-options` 的 `options` / `dictKey` / `apiUrl` 三选一，避免同时配置造成歧义。
5. `DynamicTable` 的 `dataFetcher` 必须是返回 Promise 的函数，结果形如 `{ data: Array, total: Number }`；`request.js` 的响应要先取 `res.data` 再交给表格。
6. `QuickUseManagementView` 的 `searchHandler` 返回完整响应对象（`res`），内部按 `res.data.data` / `res.data.total` 读取。
7. 排序默认转成 `xxxOrder`（ASC/DESC），分页默认转成 `pageNum/pageSize`；后端字段不符合时通过 `sortTransformer` / `pageTransformer` 适配，不要改包装器源码。
8. 表单提交前调用 `validate()`；成功/失败文案依赖后端 `message`，页面不重复弹提示。
9. 所有请求经 `src/utils/request.js`，禁止业务代码自建 axios 实例。
10. 列表页优先用 `DynamicTable`（`responsive` 默认开启）；新增页面必须按 `docs/guideline/05-前端开发规范.md` 完成 375 / 768 / 820 / 992 / 1440 五档自检。

## 3.3 扩展方式

### 后端扩展点

| 层级 | 扩展方式 | 边界 |
|------|----------|------|
| Controller | 覆写四个模板方法并调用 `super.*`，或新增业务端点 | 只做参数兜底、Form 组装、事件触发；不放业务规则和事务 |
| Service 接口 | 新增领域方法（如 `saveWithParams`），由实现层保证事务 | 多步写必须同事务，用 `AssertUtil` 做校验 |
| 服务模板 | 覆写 `insertNew`、`handleDelete`、`deletePending`、`updateBy`、`compareAndUpdateValue`、`pageVoBuildType` | 通过 `getInstance()` 调用钩子以保持代理生效 |
| 数据权限 | `useDataScope()` + `inUseDataScope(context)` | 不手写租户/公司过滤 SQL |
| 查询 | `*Criteria` 注解、`AbstractResourceCriteria` 资源 SQL、`NacosConfigQueryCriteria` 热加载 | 新代码统一字段后缀，历史命名不扩散 |
| 响应 | `@AjaxResponseType`、`@Wrapped` / `@NoWrapped`、`IResultCreator` | 文档类型必须与实际数据一致 |

### 前端扩展点

| 层级 | 扩展方式 | 边界 |
|------|----------|------|
| 表单字段 | `customComponents[type]`，组件支持 `value` + `input/change` | 新类型同时补 `FormEditor` 配置项，保证可视化编辑可用 |
| 表单布局 | `form-item-${prop}` 字段槽或 `default` 作用域槽 | 用默认槽时自行负责数据回填 |
| 表格单元格 | 列 `render(h, scope)` 或 `column.slot` + 同名槽 | 两者同时存在时 `render` 优先；手机端卡片视图会转发列槽 |
| 操作区 | `actions[]` 的 `handler`，或 `#actions` / `#actions-dropdown` 插槽 | 复杂操作用下拉，删除必须二次确认 |
| 参数转换 | `queryTransformer` / `sortTransformer` / `pageTransformer` | 优先配置转换器，不改公共组件 |
| 配置来源 | `config`、`loadConfigMethod`、Vuex、`apiUrl` | 业务侧显式指定来源，避免隐式回退 |
| 页面骨架 | 特殊页面直接用 `DynamicForm` + `DynamicTable` 组合 | 不要再复制一份 CRUD 骨架 |

## 3.4 最佳实践

1. **先定契约，再写页面**：后端四个端点的路径、条件字段名、分页参数确定后，前端一次性完成 API 封装、配置键登记和页面组装。
2. **薄 Controller，厚 Service**：Controller 只承担 HTTP 契约与响应包装；事务、校验、数据权限、审计一律在 Service 模板或其钩子中。
3. **配置即代码**：表单配置纳入版本管理（或由 `FormCreator` 统一管理），改配置要像改代码一样评审；配置键稳定不变。
4. **展示与数据分离**：`render` / `slot` 只负责展示，不写业务请求；业务请求放 `searchHandler`、`addHandler` 等处理器。
5. **刷新统一走组件方法**：新增/编辑/删除成功后调用 `this.$refs.managementView.refresh()`，不要手动改表格数据。
6. **响应式交给基础设施**：列表用 `DynamicTable`，弹窗用包装器自带的手机全屏逻辑；不要在各页面重复写断点判断。
7. **错误分支联调**：至少覆盖 200 / 403 / 405 / 468 / 500，确认 `request.js` 的刷新、重登、通知行为正常；运行期验证由开发者本人完成，不以静态检查代替。
8. **交付前自检**：后端 Knife4j 检查 `@AjaxResponseType` 渲染；前端 `npm run lint` 与五档宽度实测。

## 3.5 已知偏差与注意事项

以下均为当前代码库中的真实状态，接入时按“处理方式”执行，不要在业务代码里复制旧写法。

| 现象 | 影响 | 处理方式 |
|------|------|----------|
| 部分 Controller 的 `save` 标成单实体、`delete` 标成 `void.class` | OpenAPI 文档与运行时数据不一致 | 新代码按 3.2 第 4 条声明；改造旧接口时同步修正 |
| `DynamicFormWithApi.apiUrl` 默认 `/private/view/getSearchForm`，`api/view.js` 中无对应实现 | 不传 `configName` 时配置加载失败 | 始终传 `configName` 或 `config` |
| `DynamicFormWithApi` 的加载中/失败/空配置文案为硬编码中文 | 与小语种/i18n 规范冲突 | 业务侧不要依赖这些文案；如需多语言，改用 `loadConfigMethod` 自行处理错误 |
| `DynamicOptions` 的 `dictKey` 依赖 Vuex `dictionaries` 模块，当前 store 中未注册 | 使用字典模式时选项为空 | 优先 `options` 或 `apiUrl`，或先注册该模块 |
| `FormEditor/ConfigComponents/DynamicOptionsConfig.vue` 暴露的 `method` / `autoLoad` / `headers` 等字段 | 编辑器展示但 `DynamicOptions` 未消费 | 视为预留能力，不要按已生效配置设计业务 |
| `store/modules/FormConfig.js` 内置示例使用 `type: 'urlImage'` / `'custom'` | 与 `DynamicForm` 识别的 `'url-image'` / `customComponents` 不一致 | 新配置统一用连字符类型；旧示例仅作参考 |
| `settings.js` 中 `roleGroupAddFormKey` 与 `roleGroupEditFormKey` 同为 `'15'` | 新增/编辑复用同一表单配置 | 属业务有意复用；其他业务不要照抄，差异大时分开配置 |
| `DynamicTable.computedTotal = total || internalTotal` | 外部传 `total=0` 会回退到内部值 | 受控分页时确保首次即传正确总数，或只用内部 `dataFetcher` 模式 |
| 手机卡片视图通过 `hasActionsSlot` 判断是否存在 `#actions` | 只写 `v-slot` 但不传 prop 时手机端操作区为空 | 使用 `#actions` / `#actions-dropdown` 时同步传 `:has-actions-slot="true"` |
| `styles/dynamic-form.scss` 的 `--dynamic-form-*` 变量 | 未被入口样式引入，覆盖不生效 | 不要承诺可通过这些变量换肤；用组件 scoped 样式或全局 `element-ui.scss` |
| `request.js` 返回完整响应，业务数据取 `res.data` | 直接把 `res` 交给表格会导致 `data/total` 为空 | 按 2.2.2 契约取 `res.data` |

## 3.6 交付自检清单

后端：

- [ ] `getService()` 返回的服务接口继承 `IServiceTemplate<T, K>`，业务校验与事务在 Service。
- [ ] 四个模板方法均重声明了 HTTP 映射、权限、`@Operation`、`@ApiResponse`、`@AjaxResponseType`、`@Parameter`。
- [ ] `@AjaxResponseType` 与运行时 data 类型一致；分页参数只接受正数。
- [ ] 查询条件字段名与前端配置/转换器一致；i18n 中英文成对。

前端：

- [ ] 配置键在 `settings.js` 登记，`config.fields[].prop` 唯一且与后端字段一致。
- [ ] API 封装经 `request.js`，分页走 `params`，条件走 `data`，JSDoc 完整。
- [ ] `searchHandler` 返回完整响应；`dataFetcher` 契约满足 `{ data, total }`。
- [ ] 用户可见文案使用 `$t()`；提交前校验，成功后统一刷新。
- [ ] 使用 `#actions` 时处理 `hasActionsSlot`；响应式五档无溢出、操作可点。
- [ ] `npm run lint` 与 Knife4j 文档由开发者完成验证并确认。

---

## 变更记录

| 版本 | 时间 | 说明 |
|------|------|------|
| 1.1.0 | 2026-11 | 新增从 Controller 自动生成默认表单配置：`GET /private/view/form/controllers` + `GET /private/view/form/generate`，`FormCreator` 支持生成预览、确认应用与手动微调 |
| 1.0.0 | 2026-11 | 初版：覆盖 CRUDController 与前端默认能力的设计思路、用法、使用规范 |
