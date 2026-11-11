# Shared 模块 - 共享组件库

## 模块概述

Shared 模块（`com.tbs:shared`）是平台的核心共享组件库（Java 21），提供系统级实体、枚举、CRUD 服务模板、审计体系、逻辑删除、校验框架等跨模块复用组件。模块本身**不含业务逻辑**，业务模块（system/logging/dic/view-route 等）的实体与 Mapper 均落地于此。

| 子模块 | 职责 | 核心依赖 |
|--------|------|----------|
| `shared-pojo` | 数据契约层：POJO 接口、实体、枚举、查询条件、序列化器、SQL 拦截器（68 个源文件） | `com.tbs:sql` |
| `shared-api` | 服务模板层：CRUD 服务模板、审计、树结构服务、校验框架、全部 Mapper（47 个源文件） | `shared-pojo`、`com.tbs:tree-api`（树服务） |

父 POM 统一引入 `spring-boot-starter-validation`（校验框架基础）。

## 模块结构

```
shared/
├── shared-pojo/
│   └── com.tbs.shared/
│       ├── annotations/        # @LogicDeleteEntity（包名为复数 annotations）
│       ├── constant/           # CommonConstant
│       ├── enums/              # 12 个业务枚举
│       ├── pojo/               # 契约接口（IBaseEntity / 审计 / 数据权限 / 部门）
│       │   ├── criteria/       # 9 个查询条件类
│       │   ├── entity/         # 系统实体（12）+ entity.version（3）+ entity.view（5）
│       │   └── model/          # ClassBaseQueryCriteria + view 视图模型
│       ├── serialize/          # OrderedModelSerializer / OrderedModelDeserializer
│       └── util/               # SqlLogicDeleteIntercepter
└── shared-api/
    └── com.tbs.shared/
        ├── annotation/         # @EnableSharedLibBean
        ├── config/             # SharedAutoRegister / SharedEntityConfig
        ├── constant/           # SharedI18nConstant
        ├── interfaces/
        │   ├── annoation/      # @UserNameValid / @PasswordValid（包名拼写如此）
        │   ├── constraint/     # 校验器契约与实现
        │   └── service/        # CRUD 服务模板体系（12 个类）
        ├── mapper/             # 19 个 Mapper + mapper.version（3）+ mapper.view（5）
        └── resources/          # messages-shared*.properties（中英文）
```

## 启用方式

```java
@SpringBootApplication
@EnableSharedLibBean   // = @EnableCommonPack + @Import(SharedAutoRegister)
public class Application { ... }
```

- `SharedAutoRegister`：扫描 `com.tbs.shared` 包，注册 `Feature.SHARED`；
- `SharedEntityConfig`：`@MapperScan("com.tbs.shared.mapper")`（含 version/view 子包）+ 注册 `messages-shared` i18n 资源提供者；实体包由各服务的 `jkit.sql.auto.packages` 配置。

> 注意：`entity.version` 包（App 版本三实体）原先**不在**默认实体扫描范围内，现已在 app 的 `jkit.sql.auto.packages` 中补齐；`shared-api` 无 `spring.factories`，必须注解启用。

## shared-pojo：契约与数据模型

### 契约接口体系

```
IIdentifiable<K>（getId/setId）──┐
IFlagable（getFlag/setFlag）────┴─▶ IBaseEntity<K>（+ Serializable）
ICreateTimeAuditable（createTime）        IUpdateTimeAuditable（updateTime）
ICreateTimeAuditable + createBy ─▶ ICommonCreateAuditable<UK>
IUpdateTimeAuditable + updateBy ─▶ ICommonUpdateAuditable<UK>
IDeleteTimeAuditable（deleteTime + deleteBy）─▶ ICommonDeleteAuditable<UK>
IBaseEntity + 创建审计 + 更新审计 ─▶ ICommonAuditableEntity<K>
ICustomDataScopeEntity（dataScopeType/dataScopeValue：数据权限范围）
IDeptBelongEntity<K>（deptId：部门归属）
```

**要点**：`IBaseEntity` 仅含 id 与 flag，**不含时间审计字段**——审计字段按需实现对应接口（如 `SystemUserEntity` 实现 `ICommonCreateAuditable` + `ICommonUpdateAuditable`）。实体间共享行为一律用接口表达，**不得引入带字段的实体基类**（历史上的 `BaseEntity` 已删除；DSL 无法引用继承字段，详见 03 分册 3.2 第 9 条）。

### @LogicDeleteEntity 与 SQL 拦截器

```java
@LogicDeleteEntity(
    activeFlagValues = {1, 2, 0},   // 视为"存活"的 flag 值集合（默认 {1}）
    filedName = "flag",             // 标志字段名（默认 flag，空时回退反射读取字段映射列名）
    onNullIsDeleted = true          // flag 为 null 时是否视作已删除（默认 true）
)
```

`SqlLogicDeleteIntercepter`（实现 sql 模块 `SqlComposeInterceptor`，按实体类缓存条件）：为标注实体的查询自动追加 `flag IN (activeFlagValues)`（`onNullIsDeleted=false` 时追加 `OR flag IS NULL`），实现逻辑删除过滤。本模块不将其注册为 Bean，由 sql 框架配置接入。

### 状态枚举（12 个）

实现 common 的 `BaseEnum<String>`（code=数字标志，value=中文描述）。**注意两套枚举的 flag 语义不同**：

| 枚举 | 语义 |
|------|------|
| `CommonFlagEnum` | 通用：ACTIVE=1 / INACTIVE=-1 / DELETED=0 |
| `SystemUserStatusEnum` | 用户：ACTIVE=1 / LOCKED=2 / INACTIVE=0 / DELETE=-1（`SystemUserEntity` 的 `activeFlagValues={1,2,0}` 与之对应） |
| `CommonConstant` | FLAG_ON=1 / FLAG_OFF=0 |

其余：`SystemRoleTypeEnum`、`SystemDataScopeEnum`、`SystemRoleFlagEnum`、`SystemCompanyFlagEnum`、`SystemDepartmentFlagEnum`、`SystemTokenFlagEnum`（登录信息）、`DynamicFormConfigFlagEnum`、`AppVersionFlagEnum`、`AppVersionTypeEnum`、`AppVersionCompatibilityActionEnum`（版本兼容动作）。

### 实体清单（24 个）

| 分组 | 实体 |
|------|------|
| 系统管理（`entity/`） | `SystemUserEntity`（sys_user，雪花 ID、逻辑删除 activeFlagValues={1,2,0}、deptId）、`SystemRoleEntity`、`SystemRoleGroupEntity`、`SystemRoleGroupRelationEntity`、`SystemRoleGroupUserRelationEntity`、`SystemCompanyEntity`、`SystemDepartmentEntity`、`SystemUserDepartmentRelationEntity`、`SystemTokenEntity`、`SystemLoginInfoEntity`、`SystemOperateRecordEntity`（logging 模块存储表）、`SystemGenericApiEntity` |
| 前端动态路由（`entity/`） | `ViewRouteConfigrationEntity` / `ViewRouteMetaEntity` / `ViewRouteRoleEntity` / `ViewRouteVersionEntity` |
| 应用版本（`entity/version/`） | `AppVersionEntity` / `AppPackageEntity` / `AppVersionCompatibilityPolicyEntity` |
| 动态视图（`entity/view/`） | `DynamicFormConfigrationEntity` / `FormFieldConfigrationEntity` / `FormFieldRelationEntity` / `PropertyRelationEntity` / `PropertyValueEntity` |

> 用户-部门为多对多（`SystemUserDepartmentRelationEntity`），角色经"角色组"关联（`SystemRoleGroup*`）；**不存在**用户-角色直连关系表或用户-公司关系表。

### 查询条件（criteria，9 个）

- 基类 `ClassBaseQueryCriteria<T>`：按泛型实体类推导 SQL 资源路径（`sql/{EntitySimpleName}.sql`，前后缀可覆盖），设置实体类到查询模型；
- 业务条件：`CommonSystemUserQueryCriteria`、`CommonSystemRoleQueryCriteria`、`CommonSystemRoleGroupCriteria`、`CommonSystemCompanyQueryCriteria`、`CommonSystemDepartmentQueryCriteria`、`CommonTokenQueryCriteria`、`CommonGenericApiCriteria`、`AppVersionCriteria`、`AppVersionCompatibilityPolicyCriteria`、`AppPackageCriteria`。

### 序列化器（serialize）

针对 sql 模块排序模型 `OrderedModel` 的 Jackson 双向转换：序列化为小写排序名（如 `desc`）；反序列化兼容纯字符串（`"DESC"`）与对象（`{"ordered":"DESC"}` / `{"order":"DESC"}`）两种形态。`OrderedModelSerializer` 为 `@Component`，反序列化器需宿主按需注册到 ObjectMapper。

## shared-api：服务模板体系

### 服务接口层次

```
IServiceTemplate<E,K> = ISaveableService（save→List<E>）
                      + IDeleteableService（deleteById / deleteByIds / isDeleted）
                      + ISearchableService（queryAndPage / query / queryById）
                      + useDataScope()（默认 false，数据权限开关）
```

### AbstractFullFeatureServiceTemplate - 全功能 CRUD 模板

继承 `AbstractService`（注入 sql 模块 `IEntityQueryWrapper`，`@PostConstruct` 用 `newEntity()` 初始化查询模板），实现 `IServiceTemplate`。**必须实现的抽象方法**：

| 方法 | 职责 |
|------|------|
| `getMapper()` / `newEntity()` | Mapper 与实体模板 |
| `pageVoBuildType()` | 分页构建用的代理执行器类型（common 代理模式，`PageUtil.build` 异步并行分页） |
| `insertNew(T)` | 新增落地 |
| `updateBy(T)` | 更新落地（返回更新结果列表） |
| `handleDelete(K)` | 删除落地（逻辑删除/物理删除二选一继承） |
| `compareAndUpdateValue(newOne, oldOne)` | 合并新旧值（典型：新值非空字段覆盖旧值） |

**模板行为**：

- `deleteById`：空 ID 校验（i18n）→ 查实体 → `isDeleted` 已删返回 null → `deletePending` 钩子 → 审计 `onDelete` → `handleDelete`；全程 `@Transactional`；
- `save`：id 为空走新增（审计 `onCreate` → `insertNew`），否则查旧值 → 不存在抛 i18n 异常 → `compareAndUpdateValue` → 审计 `onUpdate` → `updateBy`；
- `queryAndPage`：`ConditionBuilder.buildSqlContext(qo)` 构建条件 → `useDataScope()` 时回调 `inUseDataScope(context)` 注入数据权限 → `PageUtil.build(pageVoBuildType(), 查询, 计数)` 异步并行分页；
- **代理自调用**：模板内部经 `AopContext.currentProxy()`（`getInstance()`）调用自身以让事务生效——**宿主必须开启 `@EnableAspectJAutoProxy(exposeProxy = true)`**，否则运行时抛异常。

### 删除策略二选一

| 模板 | handleDelete | isDeleted |
|------|--------------|-----------|
| `AbstractLogicDeleteFullFeatureServiceTemplate` | 置 `getFlagDeleted()` 标志后更新（`BUSINESS_LOGIC_ERROR` 级"实体不存在"校验） | `flag == getFlagDeleted()` |
| `AbstractPhysicDeleteFullFeatureServiceTemplate` | `deleteByPrimaryKey` 物理删除 | `entity == null` |

### 审计体系

`AbstractAuditService` 按 `instanceof` 自动填充：创建时 `createTime` + `createBy`；更新时 `updateTime` + `updateBy`；删除时 `deleteTime` + `deleteBy`。`IAuditService.getCurrentUserId()` 由业务实现（system 模块提供基于登录态的实现）。服务模板经 `getAuditServiceSupport()`（默认 `Optional.empty()`）接入。

### 树结构服务（依赖 tree-api）

`ITreeStructService`（getChildren / getNode / deleteLeaf / isLeaf / isRoot / setToTree）+ `AbstractTreeStructService`：经 `ITreeStore<Long>`（tree-struct 模块闭包表）按分组管理实体树；`setToTree` 自动补建缺失的父节点；`rebuildTree()` 重建结构。

### Mapper 清单（27 个）

均继承 `IFrameworkMapper<E, K>`（sql 模块：`GenericMapper` 通用 CRUD + `SmartQueryMapper` 条件查询），`@Repository` 标注：

- **系统（15）**：SystemUser / SystemRole / SystemRoleGroup / SystemRoleGroupRelation / SystemRoleGroupUserRelation / SystemCompany / SystemDepartment / SystemUserDepartmentRelation（含 `queryDepartmentUsers` / `queryUserDepartmentRelation` 自定义 SQL）/ SystemToken / SystemOperate / SystemGenericApi / LoginInfo
- **动态路由（4）**：ViewRouteConfigration / ViewRouteMeta / ViewRouteRole / ViewRouteVersion
- **应用版本（3）**：AppVersion / AppVersionCompt / AppVersionPackage
- **动态视图（5）**：FormConfig / FormFieldRelation / PropertyRelation / PropertyValue / RoleGroupUserRelation

## 校验框架（shared-api）

| 组件 | 说明 |
|------|------|
| `@UserNameValid` / `@PasswordValid` | JSR-303 `@Constraint` 注解（**包名拼写为 `interfaces.annoation`**），标注于字段/参数，默认消息"用户名格式错误"/"密码格式错误" |
| `UserNameValidator` / `PasswordValidator` | `ConstraintValidator` 实现，`@Resource` 注入 `IUserNamePasswordValidator` Bean 委托校验 |
| `IUserNamePasswordValidator` | 校验策略接口（validateUserName / validatePassword），注册不同实现即可全局切换规则 |
| `SimpleUserValidator` | 默认严格规则：用户名 `^[a-zA-Z0-9_]{6,16}$`；密码强正则（含数字/小写/大写/特殊字符，8-20 位） |
| `NoUserNameAndPasswordValidator` | 放行规则：非空即通过 |

```java
// 切换为宽松校验：注册放行实现即可
@Bean
public IUserNamePasswordValidator userValidator() {
    return new NoUserNameAndPasswordValidator();
}
```

## 国际化

`SharedI18nConstant` 收拢 6 个 CRUD 校验 key（`{0}` 占位符），中英文资源随 `SharedEntityConfig` 自动注册：

| key | 说明 |
|-----|------|
| `crud.delete.id.empty` | 空 Id 无法被删除 |
| `crud.save.entity.empty` | 空对象无法被保存 |
| `crud.save.entity.not.exists` | 实体数据不存在，无法保存 |
| `crud.update.entity.not.exists` | 实体数据不存在，无法更新 |
| `crud.delete.entity.not.exists` | 需要删除的实体Id{0} 不存在 |
| `crud.audit.entity.not.exists` | 实体数据不存在，无法处理审计 |

## 扩展开发

### 基于模板构建业务服务（以逻辑删除为例）

```java
@Service
public class DemoService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<DemoEntity, Long, DemoMapper> {

    @Resource
    private DemoMapper demoMapper;

    @Override
    protected DemoMapper getMapper() { return demoMapper; }

    @Override
    protected DemoEntity newEntity() { return new DemoEntity(); }

    @Override
    protected Integer getFlagDeleted() { return -1; }   // 逻辑删除标志

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return SimpleDelegateInvoker.class;             // 分页异步并行构建
    }

    @Override
    protected DemoEntity insertNew(DemoEntity entity) {
        getMapper().insertSelective(entity);
        return entity;
    }

    @Override
    protected List<DemoEntity> updateBy(DemoEntity entity) {
        getMapper().updateByPrimaryKeySelective(entity);
        return List.of(entity);
    }

    @Override
    protected DemoEntity compareAndUpdateValue(DemoEntity newOne, DemoEntity oldOne) {
        // 合并策略：新值非空字段覆盖旧值
        return oldOne;
    }
}
```

> **注意**：本模块不提供 Controller 模板（无 `AbstractCrudController`），Web 层由 web 模块与应用自行实现，注入 `IServiceTemplate` 即可。

### 自定义审计

```java
@Service
public class DemoAuditService extends AbstractAuditService<DemoEntity, Long, Long> {
    @Override
    public Long getCurrentUserId() {
        return CurrentUserHolder.getUserId();   // 接入登录态
    }
}
// DemoService 覆写 getAuditServiceSupport() 返回 Optional.of(auditService)
```

## 使用注意事项

1. **宿主必须开启 AOP 代理暴露**：服务模板经 `AopContext.currentProxy()` 自调用以保证事务生效，启动类需 `@EnableAspectJAutoProxy(exposeProxy = true)`（平台各服务已默认开启）
2. **`IBaseEntity` 不含审计字段**：需要审计的实体请实现 `ICommonAuditableEntity` / `ICommonDeleteAuditable` 等；共享行为用接口，不抽带字段的实体基类
3. **两套 flag 语义不一致**：通用 `CommonFlagEnum`（DELETED=0）与用户状态 `SystemUserStatusEnum`（DELETE=-1）相反，跨实体比较 flag 时以实体 `@LogicDeleteEntity` 注解为准
4. **`entity.version` 原先未被默认实体扫描覆盖**：现已在 app 的 `jkit.sql.auto.packages` 中补齐；其它宿主服务如需管理 App 版本三实体，同样把该包加入自己的 `jkit.sql.auto.packages`
5. **包名拼写**：`shared-pojo` 的注解包为 `annotations`（复数），`shared-api` 校验注解包为 `interfaces.annoation`（拼写如此），import 时注意
6. **分页依赖代理模式**：`pageVoBuildType()` 需要容器内存在对应 `IDelegateInvoker` 提供者（`@EnableCommonPack` 已默认装配 `SimpleDelegateInvoker`）
7. **`OrderedModelDeserializer` 不自动注册**：需要反序列化排序字段时，自行注册到 ObjectMapper（或 `@JsonDeserialize` 标注字段）

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年9月
**版本**: 1.0-SNAPSHOT
