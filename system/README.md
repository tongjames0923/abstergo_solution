# System 模块 - 系统管理

## 模块概述

System 模块（`com.tbs:system`）是平台的系统管理组件，提供用户/组织/角色权限、令牌与登录会话、应用版本管理、泛化 API、动态视图、功能开关、操作审计与数据权限等能力。

模块**不含安全框架实现**：认证鉴权的骨架（过滤器、令牌签发抽象、认证提供者契约）位于 `web` 模块，本模块按其 SPI 提供默认落地（`IUserInfoService`、`ITokenIssuanceCenter`、`IPasswordEncrypt`、`IUserLoginInfoService` 等实现）。

| 子模块 | artifactId | 职责 | 依赖 |
|--------|------------|------|------|
| `system-api` | `system-api` | 契约层：服务接口、VO/Form、令牌签发中心、CRUD 控制器基类（50 个源文件） | `web`、`logging-api` |
| `default-system-impl` | `default-system-impl` | 默认实现：全部服务实现、配置装配、拦截器、异常转换器（38 个源文件） | `system-api`、`dic-api`、`logging-api`、`view-route-api`、`oss-api` |

### 文件处理能力（自 app 模块迁入）

- **服务实现**：`service/impl/oss/downloadcode/OssDownloadCodeServiceImpl`（公开下载码生成/CRUD/下载解析，自定义码须为 4~64 位 Base64 字符且唯一）、`service/impl/oss/downloadcode/DbDownloadCodeStorage`（数据库存储，`tbs.oss.download-code.storage=db` 启用）、`service/oss/UploadTaskProgressManager`（上传进度内存注册表）
- **数据处理**：`data/TableDataHandlerRegistry`（本地 Spring + Dubbo 远程双模式处理器查找），HTTP 出参 VO 在 `system-api` 的 `com.tbs.app.vo`（`HandlerOptionVO`/`HandlerValidateVO`/`UploadTaskProgressVO`）
- **存储抽象与条件装配**（oss-api `com.tbs.oss.downloadcode`）：`IDownloadCodeStorage`/`OssDownloadCodeInfo`/配置键常量 `OssDownloadCodeConfigKeys`；Redis 实现位于 `redis-mod` 的 `com.tbs.redis.oss.RedisDownloadCodeStorage`（`RedisDownloadCodeStorageConfig` 装配，缺省 `storage=redis`），DB 实现位于本模块（`OssDownloadCodeStorageConfig` 装配，`storage=db`）
- **实体与 Mapper**：`shared-pojo` 的 `com.tbs.shared.pojo.entity.oss.OssDownloadCodeEntity`（flag 逻辑删除）与 `shared-api` 的 `com.tbs.shared.mapper.oss.OssDownloadCodeMapper`
- **i18n**：`oss.*`（含 `oss.download.code.*`）与 `data.handle.*` 文案迁入 `messages-system(.properties/_en_US.properties)`，常量见 `com.tbs.app.constant.FileProcessI18nConstant`；对应 HTTP 接入层（Controller）保留在 app 模块

父 POM `com.tbs:system`（`packaging=pom`）统一引入 `shared-api`、`shared-pojo`、`cache-api`。

> **注意**：父 POM 为聚合 POM，**不能作为依赖引入**（旧文档中的 `com.tbs:system` 依赖写法无效）。业务方按需引入 `system-api`（仅契约）或 `default-system-impl`（含默认实现）。

## 模块结构

```
system/
├── system-api/
│   └── com.tbs.app/
│       ├── config/               # AppSettingProperty（tbs.system）
│       ├── constant/             # SystemApiI18nConstant
│       ├── service/              # IFeatureFlagService、DefaultAuditService
│       │   ├── company/ ISystemCompanyService
│       │   ├── department/ ISystemDepartmentService / IDepartmentUserRelationService
│       │   ├── genericapi/ IGenericApiService
│       │   ├── log/ ISystemLogService + model/LogOperateTypeView
│       │   ├── role/ ISystemRoleService / ISystemRoleGroupService
│       │   ├── token/ ISystemTokenService
│       │   ├── user/ ISystemUserService（+ model/ 用户详情与权限模型、serial/ 桥接）
│       │   ├── version/ IAppVersionService / IAppPackageService / IAppVersionCompatibilityPolicyService
│       │   └── view/ IViewService
│       ├── remote/               # auth / user / company 三个远程服务契约
│       ├── vo/ form/             # 13 个 VO（远程/版本/部门/功能开关等）+ 6 个 Form
│       └── web/                  # DefaultTokenSignCenter / DefaultUserLoginInfoService /
│                                 #   AbstractCrudController / SimpleTokenModel / model（dto/bridge）
└── default-system-impl/
    └── com.tbs.app/
        ├── annotation/           # @NoLog（不记录操作日志）
        ├── config/               # DefaultAppConfig / BuisnessBeanConfig / RemoteConfig /
        │                         #   RemoteProperty（tbs.system.remote）/ SystemMessageSourceProvider /
        │                         #   OrderModelCustomJacksonConfig
        ├── constant/             # SystemI18nConstant / UserServiceFeatureFlagConstants
        ├── intercept/            # SystemOperateInterceptor / DataPermissionIntercepter
        ├── error/transformer/    # AccessDeniedTransformer / SQLErrorTransformer
        ├── service/impl/         # 用户/公司/部门/角色/令牌/版本/视图/泛化API 等实现
        ├── remote/               # auth / user / company 默认实现（@DubboService）
        └── resources/            # messages-system*.properties（中英文）
```

## 启用方式

模块**无 `spring.factories`**，靠宿主组件扫描 `com.tbs.app` 包生效（平台 `app` 模块启动类已覆盖），并通过配置开关控制 Bean 装配：

```yaml
tbs:
  system:
    enable: true              # 是否启用内置系统实现（AppSettingProperty 默认 true）
    enable-operate-log: true  # 是否启用操作日志拦截器（默认 false）
    enable-api-permission: true  # 是否启用 API 权限校验（默认 true）
```

> **两处默认值需区分**：`AppSettingProperty` 字段默认 `enable=true`、`enableOperateLog=false`；但 `DefaultAppConfig` 中各 Bean 的条件注解多为 `@ConditionalOnProperty(..., havingValue="true", matchIfMissing=false)`——即**未显式配置 `tbs.system.enable: true` 时 Bean 不会装配**（与字段默认值相反）。操作日志 Bean 例外，其 `matchIfMissing=true`。

## 核心功能

### 1. 用户与组织

| 服务 | 能力 |
|------|------|
| `ISystemUserService` | 继承 shared 的 `IServiceTemplate`（CRUD + 分页 + 逻辑删除）；额外提供 `queryByUserName` / `queryUserIdsByUserName` |
| `ISystemCompanyService` | 公司 CRUD（`SystemCompanyEntity`） |
| `ISystemDepartmentService` | 部门 CRUD + `appendChildDepartment` / `getChildrenDepartments`（树结构，闭包表） |
| `IDepartmentUserRelationService` | 用户-部门多对多：`queryUserDepartments` / `queryDepartmentUsers` / `addUserToDepartment` / `deleteUserFromDepartment` |

`SimpleSystemUserServiceImpl`（逻辑删除模板实现）关键行为：

- 继承 `AbstractLogicDeleteFullFeatureServiceTemplate`，删除标志 `SystemUserStatusEnum.DELETE`（-1）；
- **新增用户默认 `LOCKED` 状态**（需后续启用），密码经 `IPasswordEncrypt` 加密后落库；
- 用户名/密码格式校验受**功能开关**控制（见下），校验失败抛 `USER_INPUT_ERROR` 级 i18n 异常；
- 用户名重复校验、状态合法性校验（`EnumUtil.isValidCode`）；
- 密码或状态变更时调用 `AuthUtil.notifyUserAuthStatusChanged` 通知认证态刷新；
- **内置数据权限**：`useDataScope()=true`，`inUseDataScope` 置 `DataPermissionIntercepter` 开关；
- 三级缓存（`@Cacheable` + `@ExpireOn`）：`SYSTEM_USER`（5 分钟）、`USER_NAME_TO_ID` / `USER_NAME_ID_MAP`（15 分钟），写/删路径主动 evict。

用户详情模型 `DefaultSystemUserDetails` 实现 `ISystemUserDetails`（继承 Spring Security `UserDetails`）：携带部门列表、当前部门（切换时校验归属）、角色组权限集合（`DefaultGrantedAuthority`），并标注 `@CacheTransform` 以便缓存还原。

`DefaultUserInfoService` 实现 `web` 模块的 `IUserInfoService#loadUserInfoByUniqueKey`：按用户名查用户 → 按 `SystemUserStatusEnum` 映射 Spring Security 账户状态（ACTIVE=可用、LOCKED=锁定、INACTIVE=禁用）→ 装配角色组权限与部门。

### 2. 角色与权限（RBAC 三层模型）

```
用户 ──(多对多)──▶ 角色组 SystemRoleGroup ──(多对多)──▶ 角色 SystemRole
```

`ISystemRoleGroupService`：`getRoleList` / `appendRole` / `removeRole` / `applyGroup` / `removeGroup` / `getUserRoleGroupVOList` / `getUserList` / `isRoleInGroupAnyGroup` / `isUserInGroupAnyGroup`。

角色分两类（`SystemRoleTypeEnum`）：

- **API_ROLE**：接口权限，由 `PreAuthUtils.hasPermission("xxx")` 校验（配合 web 模块的 `@PreAuth` 类注解使用）；
- **DATA_ROLE**：数据权限，携带 `dataScope`（`SystemDataScopeEnum`：`ALL` / `DEPT` / `SELF`），由 `DataPermissionIntercepter` 消费。

`PreAuthUtils`（`@Component` + 静态门面）：

- `enableApiPermission=false` 或权限串为空 → 直接放行；
- 否则取当前登录用户的授权集合，遍历角色组内的 `API_ROLE` 匹配 `role.getRole()`；非 `DefaultGrantedAuthority` 类型则退化为 `authority.getAuthority()` 字符串比对。

### 3. 数据权限（DataPermissionIntercepter）

实现 sql 模块的 `SqlComposeInterceptor`，在 SQL 生成阶段改写 WHERE 条件树：

- **默认关闭**，需由服务模板的 `inUseDataScope(context)` 显式置 `DATA_PERMISSION_ENABLE_FLAG_KEY=true`（当前仅 `SimpleSystemUserServiceImpl` 开启）；
- 实体需实现 `IDeptBelongEntity`、`ICommonCreateAuditable` 或 `ICustomDataScopeEntity` 之一，否则跳过；
- 未激活账户 / 用户不存在 / 无角色组 → 追加 `1=0` 阻断查询；
- 取用户全部数据角色中**权限最大**者（`dataScope` 值最小）：
  - `ALL` → 不拦截；
  - `DEPT` → 追加 `deptId IN (用户部门ID列表)`；
  - `SELF` → 追加 `createBy = 用户ID`；
- 字段反射结果按类缓存（`DEPT_FIELD_CACHE` / `CREATE_BY_FIELD_CACHE`）。

### 4. 令牌与登录会话

| 组件 | 说明 |
|------|------|
| `DefaultTokenSignCenter` | 继承 web 模块 `AbstractTokenIssuanceCenter`，令牌落库 `SystemTokenEntity`；`createModel` 用雪花 ID 生成 token 串并记录 MAC/IP；查询限定 28 天内；`getTokenByToken` 走 `@Cacheable(TOKEN_CACHE)` + `@ExpireOn(5min)`，`setTokenStatus` 主动 evict |
| `DefaultUserLoginInfoService` | 继承 `AbstractUserLoginInfoService`：登录信息落库 `SystemLoginInfoEntity`，含会话列表查询、用户详情置换、登出清理；缓存仅存非空结果（防穿透膨胀），28 天时间窗 |
| `DefaultSystemTokenService` | 令牌 CRUD（继承 shared 模板） |
| `CookieRefreshTokenService` | 刷新令牌的 Cookie 形式实现（`IRefreshTokenService`） |

认证入口由 `web` 模块提供，`DefaultAppConfig` 装配默认实现：

- `IUsernamePasswordAccessTokenProvider` → `DefaultUserPasswordAccessTokenProvider`（用户名密码登录）；
- `IRefreshAccessTokenProvider` → `DefaultRefreshAccessTokenProvider`（容器中存在 `IRefreshTokenService` 时装配）；
- `ITokenRemover` → `DefaultTokenRemover`；
- `IPasswordEncrypt` → **默认 `NoEncryptPassword`（不加密！）**，生产需配置实现（见配置说明）。

### 5. 操作日志（SystemOperateInterceptor）

实现 web 模块 `IApiInvokeInterceptor`，在控制器方法调用前后记录审计：

- `supports()`：方法或类上标注 `@NoLog` 则跳过；
- `beforeInvoke`：记录操作类型（`类名.方法名`）、IP、HTTP 方法、URI、时间、操作描述（`BaseController.getBusinessName()`）、操作用户（未登录记 `NULL USERNAME:IP`）、参数 JSON；
- `afterInvoke` / `onException`：补齐结束时间、结果 JSON（异常时记录堆栈与 `IExceptionLevel` 级别码，缺省 `SYSTEM_ERROR`），经 `ILogRecorder` 投递（默认 MQ 通道，见 logging 模块）；
- 装配条件：`@ConditionalOnProperty({"enable-operate-log","enable"}, matchIfMissing=true)`。

### 6. 应用版本管理

| 服务 | 能力 |
|------|------|
| `IAppVersionService` | 版本 CRUD + 状态流转：`DRAFT(0) → PENDING(2) → RELEASE(1) → OFFLINE(-1) → ARCHIVED(-3)`，`RELEASE` 可 `rollback` 至 `ROLLED_BACK(-2)`；`queryLatestReleased(appKey, platform)`、`listAppKeys()` |
| `IAppPackageService` | 安装包登记（`listByVersionId`），文件实体由 OSS 承担，本服务仅持存储坐标/下载地址 |
| `IAppVersionCompatibilityPolicyService` | 跨应用版本兼容策略：`enablePolicy` / `disablePolicy` / `evaluateCompatibility(appKey, platform, currentVersionCode)`（先比对最新已发布版本，再匹配以该应用为目标的启用策略，取最高强制程度作为动作：强制更新/推荐更新/仅提示） |

### 7. 泛化 API（IGenericApiService）

动态注册 API 配置（`SystemGenericApiEntity`）：`getByApiId` / `enableApi` / `disableApi` / `getAllAvaliablePreAuthList`。预认证策略（web 模块 `IApiInstancePreAuth`）内置三种：`SuperAdminPreAuth`（超管）、`AccountActivePreAuth`（账户激活）、`DeveloperPreAuth`（开发者）。

### 8. 动态视图（IViewService）

表单配置管理：`getForm` / `saveForm` / `updateForm` / `deleteForm` / `listForms(pageNum, pageSize)`，实体为 `DynamicFormConfigrationEntity`（shared-pojo `entity/view`）。

### 9. 功能开关（IFeatureFlagService）

`FeatureFlagServiceImpl` **基于 dic 数据字典服务**实现（`@DubboReference(lazy=true) IDataDicService`）：以字典编码 `SYSTEM` 的字典项为开关表（label=开关名、value=开关值、flag=启用状态）：

```java
// 判断开关值是否匹配
boolean on = featureFlagService.isEnableValue(
    UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR, "ENABLE");
// 增/改/删/查全部开关
featureFlagService.addSystemFeatureFlag(vo);
List<SystemFeatureFlagVO> all = featureFlagService.listAllSystemFeatureFlags();
```

`UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR` 控制用户服务是否启用用户名/密码格式校验（默认实现 `SimpleUserValidator`）。**字典服务不可用时所有 `isEnableValue` 返回 false**（仅 warn，不阻断）。

### 10. 远程服务（Dubbo，可选）

`RemoteConfig`（`@ConditionalOnProperty(tbs.system.remote.enable=true)`）以 `@DubboService` 暴露：

| 接口 | 能力 |
|------|------|
| `IUserRemoteService` | 按用户名/ID/昵称（模糊）/ID 批量 查询用户信息 |
| `ICompanyRemoteService` | 按 ID/名称（模糊）/税号 查询公司，及子公司列表 |
| `IAuthRemoteService` | 按 token 获取认证信息 |
| `ITreeStore` | 树结构存储（默认 `NoImplTreeStore` 空实现，可替换为持久化实现） |

### 11. 异常转换

| 转换器 | 匹配 | 结果 |
|--------|------|------|
| `AccessDeniedTransformer` | `AccessDeniedException` | `businessErrorI18n("auth.user.access.denied")` |
| `SQLErrorTransformer` | `SQLException` / `DataAccessException` | `dataAccessError(message)`（`DATA_ACCESS_ERROR` 级） |

### 12. 逻辑删除

`DefaultAppConfig` 无条件注册 `SqlLogicDeleteIntercepter`（shared-pojo），为标注 `@LogicDeleteEntity` 的实体自动追加 `flag IN (...)` 过滤。

### 13. CRUD 控制器基类（system-api）

`AbstractCrudController<T, K, Q extends IQueryCriteria>`（继承 web 模块 `BaseController`）提供 `get` / `save` / `delete` / `query(criteria, pageNum, pageSize)` 四个方法，返回统一 `AjaxResult`，消息走 i18n（`SystemApiI18nConstant.CRUD_SAVE_SUCCESS` 等）。子类实现 `getService()` 并在控制器中暴露映射即可。

## 配置说明

### tbs.system（AppSettingProperty）

```yaml
tbs:
  system:
    enable: true                  # 是否装配内置系统实现（字段默认 true，但 Bean 条件需显式 true）
    enable-operate-log: false     # 是否启用操作日志拦截器（默认 false）
    enable-api-permission: true   # 是否启用 API 权限校验（默认 true）
    # 以下均为"按类名装配"的可替换实现（无参构造）
    system-user-name-password-validator-impl: com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator
    feature-flag-service-impl: com.tbs.app.service.impl.FeatureFlagServiceImpl
    main-delegate-invoker-impl: com.tbs.common.delegate.impl.SimpleDelegateInvoker
    password-encrypt-impl: com.tbs.web.security.user.impl.NoEncryptPassword
    error-throw-event-impl: com.tbs.common.error.impl.SimpleErrorThrowEvent
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enable` | boolean | true | 系统实现总开关 |
| `enable-operate-log` | boolean | **false** | 操作日志拦截器开关 |
| `enable-api-permission` | boolean | true | API 权限校验开关（`PreAuthUtils` 消费） |
| `system-user-name-password-validator-impl` | Class | `SimpleUserValidator` | 用户名/密码格式校验器（shared 模块契约） |
| `feature-flag-service-impl` | Class | null（回退 `FeatureFlagServiceImpl`） | 功能开关服务实现 |
| `main-delegate-invoker-impl` | Class | `SimpleDelegateInvoker` | 服务模板分页构建用的代理执行器 |
| `password-encrypt-impl` | Class | **`NoEncryptPassword`** | 密码加密实现（默认不加密） |
| `error-throw-event-impl` | Class | `SimpleErrorThrowEvent` | common 模块错误抛出事件实现 |

### tbs.system.remote（RemoteProperty）

```yaml
tbs:
  system:
    remote:
      enable: false               # 是否以 Dubbo 暴露远程服务（默认 false）
      tree-store-impl: com.tbs.struct.tree.api.persistence.impl.NoImplTreeStore
      auth-remote-service-impl: com.tbs.app.remote.auth.impl.DefaultAuthRemoteServiceImpl
      user-remote-service-impl: com.tbs.app.remote.user.impl.DefaultUserRemoteServiceImpl
      company-remote-service-impl: com.tbs.app.remote.company.impl.DefaultCompanyRemoteServiceImpl
```

## 国际化

- `SystemMessageSourceProvider`（order=-500）注册 `messages-system.properties`（中）/ `messages-system_en_US.properties`（英）；
- key 常量：`SystemI18nConstant`（用户/角色/部门/公司/令牌/版本等全量校验与业务消息，约 13KB）、`SystemApiI18nConstant`（CRUD 成功消息）；
- 新增 key 须中英文两份同步维护，消息经 `I18nUtil` 读取，占位符风格 `{0}`。

## 扩展开发

### 替换密码加密实现

```java
@Component
public class BCryptPasswordEncrypt implements IPasswordEncrypt {
    @Override
    public String encrypt(String raw) {
        return new BCryptPasswordEncoder().encode(raw);
    }
    // matches 等方法按契约实现
}
```

```yaml
tbs:
  system:
    password-encrypt-impl: com.example.BCryptPasswordEncrypt
```

### 自定义用户服务

```java
@Service
public class CustomUserService extends SimpleSystemUserServiceImpl {
    @Override
    protected SystemUserEntity insertNew(SystemUserEntity entity) {
        // 复用父类校验与加密，追加自定义逻辑
        return super.insertNew(entity);
    }
}
```

注册为 `ISystemUserService` 类型的 Bean 即可让位 `BuisnessBeanConfig` 的默认装配（`@ConditionalOnMissingBean`）。

### 自定义数据权限粒度

覆写服务模板的 `useDataScope()` 返回 `true` 并在 `inUseDataScope(context)` 中调用 `DataPermissionIntercepter.setEnableFlag(context, true)`；如需新的权限维度，实现 `ICustomDataScopeEntity` 并自行扩展拦截器。

### 自定义操作日志

实现 web 模块 `IApiInvokeInterceptor` 并注册为 Bean（与 `SystemOperateInterceptor` 并存），或用 `@NoLog` 标注无需记录的方法/类。

## 使用注意事项

1. **依赖坐标不可写父 POM**：`com.tbs:system` 为 `packaging=pom` 聚合模块，请依赖 `system-api` 或 `default-system-impl`
2. **`tbs.system.enable` 必须显式置 true**：字段默认 true，但 Bean 条件注解 `matchIfMissing=false`，未配置时不装配（操作日志 Bean 例外）
3. **默认密码不加密**：`passwordEncryptImpl` 默认 `NoEncryptPassword`，生产环境务必替换为 BCrypt 等实现（默认实现仅便于开发调试）
4. **新增用户默认为 LOCKED**：需管理员显式启用后才能登录
5. **功能开关依赖 dic 服务**：`FeatureFlagServiceImpl` 经 Dubbo 调用数据字典服务（编码 `SYSTEM`），服务不可用时所有开关返回 false——依赖开关的校验（如密码格式）会静默放行
6. **数据权限默认关闭**：仅 `SimpleSystemUserServiceImpl` 开启；自定义服务需自行覆写 `useDataScope()` 与 `inUseDataScope`
7. **依赖宿主组件扫描**：无 `spring.factories`，宿主扫描路径须覆盖 `com.tbs.app`
8. **缓存依赖 cache 模块**：用户/令牌/登录信息的 `@Cacheable` + `@ExpireOn` 需要 cache 模块启用且缓存管理器可用（平台默认 Redisson）
9. **用户状态枚举非通用枚举**：`SystemUserStatusEnum`（DELETE=-1）与通用 `CommonFlagEnum`（DELETED=0）语义相反，跨模块比较时以实体注解为准
10. **远程服务默认关闭**：`tbs.system.remote.enable=false`，需 Dubbo 环境且显式开启

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年9月
**版本**: 1.0-SNAPSHOT
