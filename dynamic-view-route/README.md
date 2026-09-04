# Dynamic View Route 模块 - 前端动态路由

## 模块概述

Dynamic View Route 模块（`com.tbs:dynamic-view-route`）提供**前端动态路由**能力：将 Vue Router 风格的路由配置（路径、组件、重定向、元数据、权限角色、子路由树）存储在后端，前端启动时按分组拉取路由、按全局版本号比对增量刷新。

核心设计：

- **分组管理**：路由按分组（group）隔离，每个分组独立版本、独立缓存
- **版本控制（append-only）**：版本表为唯一事实来源，路由行挂靠版本 ID，历史不可变；一切变更（保存/回滚/清空分组）都产生新的版本记录；某分组的当前版本即该组 `MAX(id)` 的版本记录
- **全局视图版本号**：即版本表全局 `MAX(id)`，单调递增、永不回退，用于前端比对是否需要刷新路由
- **双实现可切换**：数据库实现（动态编辑/热更新/回滚）与 YAML 文件实现（静态只读），按配置切换

模块包含两个子模块，父 POM 统一依赖 `com.tbs:common`：

| 子模块 | 职责 |
|--------|------|
| `view-route-api` | 契约层：服务接口、路由模型、启用注解、属性值转换器 |
| `view-route-default-impl` | 默认实现层：数据库实现（依赖 `sql`、`shared-api`、`system-api`）、YAML 实现、自动配置、i18n 资源 |

## 模块结构

```
dynamic-view-route/
├── view-route-api/                       # 契约层（Java）
│   └── com.tbs.view/
│       ├── (root)                        # IPropertyValueTransformer
│       ├── annotation/                   # @EnableViewControl
│       ├── config/                       # ViewAutoRegister（自动注册器）
│       ├── constant/                     # ViewConstants
│       ├── impl/                         # JsonPropertyValueTransformer
│       └── route/
│           ├── api/                      # IViewRouteService
│           └── model/                    # ViewRouteModel / ViewRouteMetaModel / ViewRouteVersionModel
└── view-route-default-impl/              # 默认实现层（Java）
    └── com.tbs.view.route/
        ├── constant/                     # ViewRouteI18nConstant
        ├── config/                       # ViewRouteConfig / ViewRouteMessageSourceProvider
        │   └── property/                 # ViewRouteProperty（tbs.view.route）
        └── impl/                         # DatabaseViewRouteServiceImpl / YmlViewServiceImpl
```

## 快速开始

### 1. 启用模块

```java
@SpringBootApplication
@EnableCommonPack
@EnableViewControl      // @Import(ViewAutoRegister)：扫描 com.tbs.view 包，注册 Feature.VIEW_ROUTE
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 注入服务

`ViewRouteConfig` 在容器无 `IViewRouteService` Bean 时，按 `tbs.view.route.view-route-service-class` 配置的类经无参构造实例化（默认 `DatabaseViewRouteServiceImpl`）。业务代码直接注入接口即可：

```java
@Service
public class RouteFacadeService {

    @Resource
    private IViewRouteService viewRouteService;

    /** 前端拉取某分组的最新路由 */
    public List<ViewRouteModel> loadRoutes(String group) {
        return viewRouteService.getViewRoutesByGroup(group);
    }

    /** 前端比对版本：版本号变化才重新拉取全量路由 */
    public long currentViewVersion() {
        return viewRouteService.getViewVersion();
    }

    /** 管理端保存路由（产生新版本） */
    public ViewRouteVersionModel save(String group, List<ViewRouteModel> routes, String desc) {
        return viewRouteService.saveViewRoute(group, routes, desc);
    }
}
```

## 路由模型

### ViewRouteModel - 路由节点

| 字段 | 类型 | 说明 |
|------|------|------|
| `path` | String | 路由路径 |
| `component` | String | 组件名称/路径 |
| `redirect` | String | 重定向路径 |
| `name` | String | 路由名称 |
| `hidden` | Boolean | 是否隐藏路由（侧边栏不显示） |
| `alwaysShow` | Boolean | 是否始终显示路由 |
| `children` | Collection\<ViewRouteModel\> | 子路由（递归嵌套） |
| `meta` | ViewRouteMetaModel | 路由元数据 |
| `sortOrder` | Integer | 排序值（同级按其升序，null 按 0） |

### ViewRouteMetaModel - 路由元数据

| 字段 | 类型 | 说明 |
|------|------|------|
| `routeGroup` | String | 路由分组 |
| `title` | String | 路由标题（菜单/面包屑显示） |
| `icon` | String | 路由图标 |
| `roles` | Collection\<String\> | 权限角色编码列表 |
| `breadcrumb` | Boolean | 是否显示面包屑 |
| `activeMenu` | String | 当前激活的菜单（隐藏路由高亮用） |

### ViewRouteVersionModel - 版本记录

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 版本 ID（全局唯一、单调递增，兼作全局视图版本号） |
| `routeGroup` | String | 路由分组 |
| `versionNo` | Integer | 组内版本序号（v1、v2…，按组内计数递增） |
| `description` | String | 版本说明 |
| `createBy` | Long | 创建人 ID（取不到时为 null） |
| `createTime` | Date | 创建时间 |

## 服务契约（IViewRouteService）

| 方法 | 说明 |
|------|------|
| `List<String> getAllViewRouteGroups()` | 所有可见路由分组（读内存缓存） |
| `List<ViewRouteModel> getViewRoutesByGroup(String group)` | 指定分组当前版本的路由树（读内存缓存） |
| `ViewRouteVersionModel saveViewRoute(String group, List<ViewRouteModel>, String description)` | 保存路由：产生新版本并挂靠全部路由行（事务性） |
| `void deleteViewRoute(String group)` | 清空分组：插入一条不含路由行的空版本记录，历史保留 |
| `List<ViewRouteVersionModel> getVersionHistory(String group)` | 分组版本历史（最新在前） |
| `ViewRouteVersionModel rollbackToVersion(String group, Long versionId)` | 回滚：复制目标版本路由树（含元数据与角色）生成新版本 |
| `long getViewVersion()` | 全局视图版本号（版本表 `MAX(id)`，无记录返回 0） |

## 默认实现

### 1. 数据库实现（DatabaseViewRouteServiceImpl，默认）

数据库驱动的完整实现，实现 `IStartup`（应用启动时从数据库加载全部路由构建内存缓存）：

**存储结构**：路由配置、元数据、权限角色、版本分别落于 4 张表，由 shared 模块提供的 Mapper 访问：

- `ViewRouteConfigrationMapper` / `ViewRouteConfigrationEntity`：路由行（分组、路径、组件、名称、重定向、hidden/alwaysShow（1/0）、parentId、sortOrder、version、flag、审计字段）
- `ViewRouteMetaMapper` / `ViewRouteMetaEntity`：路由元数据（按 routeId 关联）
- `ViewRouteRoleMapper` / `ViewRouteRoleEntity`：路由-角色关联（按 routeId 关联）
- `ViewRouteVersionMapper` / `ViewRouteVersionEntity`：版本记录（提供 `findByGroupOrderByVersionNoDesc`、`countByGroup`、`findMaxId`、`findAllOrderByIdDesc` 等查询）

**版本控制语义**：

- `saveViewRoute`：校验路由列表非空（`USER_INPUT_ERROR`）→ 产生新版本（versionNo = 组内计数 + 1）→ 递归落库路由树（路由行 + 元数据 + 角色）→ 重建分组缓存（事务回滚覆盖全部写入）；
- `deleteViewRoute`：仅当分组存在且当前版本非空时，插入空版本记录（说明为"清空分组"），历史数据原样保留，分组从缓存/列表中消失；
- `rollbackToVersion`：校验目标版本存在且属于该分组（否则 `USER_INPUT_ERROR`）→ 产生新版本（说明自动填"回滚自 vN"）→ 递归复制目标版本的路由树（含元数据与角色）→ 重建分组缓存；
- `getViewVersion`：版本表 `findMaxId()`，供前端做增量比对。

**缓存与热更新**：

- 分组 → 路由树存于内存 `HashMap`（仅缓存各组当前版本）；读接口（`getAllViewRouteGroups` / `getViewRoutesByGroup`）不查库；
- 启动时两段式加载：按 ID 倒序遍历版本表取每组最新版本 ID，再按版本查路由行建树；
- 提供公开方法 `refreshCache()` 强制全量重载（多实例部署时可结合配置中心/广播触发）。

**审计**：写操作记录操作人 —— 经 `AuthUtil` + `ISystemUserService` 查询当前登录用户 ID，取不到时留空（warn 日志）不阻断业务。

### 2. YAML 实现（YmlViewServiceImpl）

轻量只读实现，实现 `IStartup`：启动时加载 classpath 下的 `view-route.yml`（文件不存在则 warn 跳过），按"顶层键 = 分组名"解析路由列表。写操作（保存/删除/回滚）抛出"功能尚未实现"异常，版本历史为空、全局版本号恒为 0。

文件格式（与解析器字段一一对应）：

```yaml
# 顶层键为分组名，值为该分组的路由数组
admin:
  - path: /system
    component: Layout
    redirect: /system/user
    name: System
    alwaysShow: true
    meta:
      title: 系统管理
      icon: setting
      roles: [admin]
      breadcrumb: true
    children:
      - path: user
        component: system/user/index
        name: User
        meta:
          title: 用户管理
          icon: user
guest:
  - path: /home
    component: home/index
    name: Home
    hidden: false
    meta:
      title: 首页
```

> 注意：YAML 解析不读取 `sortOrder`（排序值为 null，同级按 0 处理），需要精确排序请使用数据库实现。

## 配置说明

```yaml
tbs:
  view:
    route:
      # IViewRouteService 实现类（无参构造），二选一：
      #   com.tbs.view.route.impl.DatabaseViewRouteServiceImpl  （默认：数据库实现）
      #   com.tbs.view.route.impl.YmlViewServiceImpl            （YAML 文件实现）
      view-route-service-class: com.tbs.view.route.impl.DatabaseViewRouteServiceImpl
```

| 前缀 | 属性类 | 属性（默认值） |
|------|--------|----------------|
| `tbs.view.route` | `ViewRouteProperty` | `viewRouteServiceClass`（`DatabaseViewRouteServiceImpl.class`） |

> 属性类 javadoc 中"默认为 YmlViewServiceImpl"为过期注释，代码实际默认值为数据库实现。

## 国际化

- 资源文件：`i18n/messages-view-route.properties`（中文）/ `messages-view-route_en_US.properties`（英文）；
- 经 `ViewRouteMessageSourceProvider`（继承 common 模块 `AbstractMessageSourceProvider`，order=-600）自动合并到全局 MessageSource；
- key 常量集中于 `ViewRouteI18nConstant`，占位符风格 `{0}`，共 10 个：配置校验与操作失败（`view.route.config.cannot.be.empty`、`view.route.save.failed`、`view.route.delete.failed`、`view.route.load.failed`、`view.route.load.yaml.failed`、`view.route.function.not.implemented`）、版本控制（`view.route.clear.group`、`view.route.version.not.found.or.not.in.group`、`view.route.rollback.from`、`view.route.rollback.failed`）；新增 key 须中英文两份同步维护。

## 扩展开发

### 自定义路由服务

实现 `IViewRouteService` 并注册为 Bean（`@ConditionalOnMissingBean` 让位），即可接管路由读取与版本管理（如接入配置中心、多级缓存等）：

```java
@Service
public class RemoteViewRouteServiceImpl implements IViewRouteService, IStartup {
    // 实现全部 7 个方法；建议实现 IStartup 在启动时预加载
}
```

也可不注册 Bean，改用配置指定实现类（经无参构造实例化）：

```yaml
tbs:
  view:
    route:
      view-route-service-class: com.example.RemoteViewRouteServiceImpl
```

### 属性值转换器（api 层附属能力）

`IPropertyValueTransformer`（继承 common 的 `ITransformer<Object>`）提供对象与 UTF-8 字符串的互转默认方法，默认实现 `JsonPropertyValueTransformer`（JSON 序列化，Bean 名 `view_transformer_json`），供视图属性（表单/表单域关联，常量见 `ViewConstants`）等场景按名获取使用。

## 使用注意事项

1. **读接口不查库**：数据库实现的读接口走内存缓存，多实例部署时各实例独立缓存，跨实例变更需自行触发 `refreshCache()`（或依赖前端以 `getViewVersion()` 比对后重新拉取）
2. **版本不可变**：任何变更都产生新版本记录，数据量随变更次数线性增长，请合理规划版本清理策略
3. **YAML 实现只读**：需要管理端动态编辑路由时必须使用默认的数据库实现
4. **权限角色仅为数据**：`meta.roles` 只负责随路由下发，鉴权由前端路由守卫/后端接口各自实现

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
