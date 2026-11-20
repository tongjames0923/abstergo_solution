# app 模块 - 后端 Web 运行包

Abstergo Solution 的**主应用入口**（pom 描述：后端 web 运行包）：将平台各能力模块装配为可运行的单体 Web 服务，提供用户/角色/公司/部门等系统管理 REST API、登录认证、动态视图路由、数据字典、对象存储、缓存管理、应用版本管理、泛化 API 调用等完整后端能力。经 Dubbo（Nacos 注册中心）对外提供 RPC 服务，并作为 `app-ui-vue` 前端的默认后端。

## 目录

- [模块定位](#模块定位)
- [集成的能力模块](#集成的能力模块)
- [快速开始](#快速开始)
- [启动类与注解装配](#启动类与注解装配)
- [配置体系](#配置体系)
- [REST API 清单](#rest-api-清单)
- [核心配置类与服务](#核心配置类与服务)
- [定时任务](#定时任务)
- [特殊机制](#特殊机制)
- [事件订阅与动作编排链路](#事件订阅与动作编排链路)
- [构建与部署](#构建与部署)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色 | 全平台的组装层与运行入口（唯一可执行模块之一，另见 dic/logging 独立服务） |
| 打包 | `spring-boot-maven-plugin` repackage 可执行 jar + `build-info`（供版本接口读取构建信息） |
| Web 容器 | Undertow（经 web 模块排除 Tomcat） |
| 服务名 | `app-service`（Dubbo 应用名 `dubbo-app-system-service`） |
| 注册/配置中心 | Nacos（`NACOS_ADDRESS` 环境变量，默认 `127.0.0.1:8848`） |
| 前端配套 | `app-ui-vue`（开发代理默认指向本服务 8080 端口） |

## 集成的能力模块

本模块 pom 直接引入（即运行时启用）的平台能力：

| 依赖 | 启用的能力 |
| --- | --- |
| `common` / `async` | 基础工具、代理模式、事件广播、任务调度、线程池、本地锁 |
| `default-system-impl` | 系统管理全套实现：用户/角色/角色组/公司/部门/版本/泛化 API/功能开关服务、`DefaultTokenSignCenter` 令牌签发、认证 SPI 落地 |
| `cache-impl` + `cache-caffeine-impl` | TTL 缓存 + 多级缓存（Caffeine L1） |
| `view-route-default-impl` | 动态视图路由（数据库实现） |
| `redis` | Redisson：分布式锁、缓存 L2、DataHolder、任务存储（MQ v1 幂等/失败存储已随 v2 下线） |
| `rabbitmq-broadcast` + `rabbitmq-logging` | RabbitMQ 事件广播、日志通道 |
| `closure-list-impl` | 闭包表树结构（公司/部门树） |
| `open-api-starter` | Knife4j OpenAPI 文档（doc.html） |
| `oss-minio-impl` | MinIO 对象存储 |
| `script-engine-api` | SpEL 脚本引擎 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 配置中心导入 |

另有 `spring-boot-configuration-processor`（编译期生成 `app.gateway.*` 配置元数据，IDE 自动补全）。

## 快速开始

### 1. 启动依赖服务

```bash
cd etc/docker
docker compose up -d
```

启动 7 个依赖服务：MySQL 8.4（3306，root/123456）、Nacos v2.5.3（8848，standalone）、Redis 6.2（6379，密码 123456）、RabbitMQ 4.3（5672/15672，admin/admin123）、MinIO（9000/9001，minioadmin）、xxl-job-admin 2.5.0（9898，token 123456789）、Dubbo Admin 0.6.0（9600）。

> xxl-job 需先执行 `etc/sql_file/xxl-job/xxl_job_init.sql` 初始化 `xxl_job` 库；Nacos 需初始化 `nacos_config` 库。

### 2. 初始化数据库

应用库默认 `core_app`（jkit-sql-auto `mode: update` 启动时自动建表）；业务数据表结构由 shared 模块实体承载。

### 3. 启动应用

```bash
mvn spring-boot:run -pl app
# 或打包后运行
mvn clean package -pl app -am
java -jar app/target/app-1.0-SNAPSHOT.jar
```

默认端口 `server.port=8080`。启动后：

- Swagger 文档：`http://127.0.0.1:8080/doc.html`（Knife4j，`tbs.knife4j.enable=true`）
- 版本探针：`curl http://127.0.0.1:8080/public/gateway/version`
- Nacos 不可用时应用仍可启动（配置导入带 `optional:` 前缀，回退本地 yml）

### 4. 前端联调

`app-ui-vue` 开发服务器（9528）默认把 `/api` 代理到 `http://127.0.0.1:8080`，无需额外配置。

## 启动类与注解装配

`AppApplication` 共叠加 **14 个平台注解**（模块能力的启用开关；sql 已改为自动装配，不再占用注解）：

```java
@SpringBootApplication(scanBasePackages = {"com.tbs.app"})
@EnableWebPack                // web：Spring Security + 令牌认证 + 统一响应（含 @EnableCommonPack）
@EnableTbsAsyncPack           // async：线程池/事件广播/调度
@EnableTbsCache               // cache：TTL/多级缓存
@EnableRabbitMq               // rabbitmq：MQ 收发
@UseXxlJob                    // xxl-job：执行器（含 @EnableCommonPack 重复导入去重）
@UseKnife4jPack               // open-api-starter：Knife4j 文档
@EnableViewControl            // dynamic-view-route：动态视图路由
@EnableSharedLibBean          // shared：服务模板 + Mapper 扫描
@UseClosureTableTree          // tree-struct：闭包表树
@EnableAspectJAutoProxy(exposeProxy = true)   // shared 服务模板自调用需要
@EnableTbsRedis               // redis：Redisson 装配
@EnableTbsOss                 // oss：对象存储门面
@EnableDubbo                  // Dubbo RPC（应用名 dubbo-app-system-service）
@EnableScriptEngine           // script-engine：SpEL 引擎
```

> **sql 无启用注解**：依赖 `sql-impl` + `sql-mysql-impl`（方言）到位后，由 Spring Boot `AutoConfiguration.imports` 自动装配 QueryEngine/SqlDialect/事务/Mapper 扫描，无需 `@EnableTbsSql`/`@UseMysql`。

> 注意 `@SpringBootApplication(scanBasePackages = {"com.tbs.app"})`：组件扫描仅限本包，但各平台注解经 AutoRegister 机制注册各自包的 Bean（`com.tbs.web`、`com.tbs.cache` 等），因此无需扩大扫描范围。

## 配置体系

### Profile 组合（application.yml）

`spring.profiles.include: tbs, db, redis, mq` 四个本地 profile + Nacos 远程配置导入：

```yaml
spring:
  application:
    name: app-service
  config:
    import:   # optional 前缀：Nacos 不可用时仍可本地启动
      - optional:nacos:RABBITMQ.yml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:CoreAppDb.yml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:REDIS.yml?group=DEFAULT_GROUP&refreshEnabled=true
      - optional:nacos:${spring.application.name}.yml?group=DEFAULT_GROUP&refreshEnabled=true
```

Nacos 中的同名配置优先于本地 profile（远程覆盖本地）。

### 环境变量总表

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `NACOS_ADDRESS` | `127.0.0.1:8848` | Nacos 注册与配置中心地址 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1` / `3306` / `core_app` | 数据库 |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `123456789` | 数据库凭证 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / `123456` | Redis |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` | RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `admin` / `admin123` | RabbitMQ 凭证 |
| `XXL_JOB_ADMIN_ADDRESS` | `http://127.0.0.1:9898/xxl-job-admin` | 调度中心地址 |
| `XXL_JOB_ACCESS_TOKEN` | `123456789` | 调度中心令牌 |
| `OSS_ENABLE` / `OSS_TYPE` | `false` / `minio` | 对象存储开关与实现 |
| `OSS_ENDPOINT` / `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | `http://127.0.0.1:9000` / `minioadmin` / `minioadmin` | MinIO 连接 |
| `OSS_DEFAULT_BUCKET` | `app-files` | 默认桶 |
| `APP_GATEWAY_VERSION` | pom 版本号 | 版本标识覆盖（蓝绿部署验证） |

### `tbs.*` 平台配置（application-tbs.yml）

本模块的核心平台配置聚合文件（节选关键项，完整语义见各模块 README）：

```yaml
tbs:
  web:
    permit-all-paths: [/public/**]        # 匿名放行
    secured-paths: [/private/**]           # 需登录
    auth:
      enable: true
      token-expire-time: 1800              # 访问令牌 30 分钟
      token-issuance-center-impl: com.tbs.app.web.DefaultTokenSignCenter
      refresh-token-expire-time: 168       # 刷新令牌 7 天（小时）
      application-token-expire-time: 168
    enable-cors: true
  system:
    enable: true
    enable-operate-log: true               # 操作日志（写 MQ）
    enable-api-permission: true            # API 权限校验
    main-delegate-invoker-impl: com.tbs.async.delegate.AsyncDelegateInvoker
    remote:
      enable: true
      tree-store-impl: com.tbs.struct.tree.closure.impl.service.ClosureTreeServiceImpl
  cache:
    cache-manager-impl: org.redisson.spring.cache.RedissonSpringCacheManager
    ttl-cache-decorator: com.tbs.cache.redis.RedissonTtlCacheDecorator
    multi-level:
      enabled: true                        # Caffeine L1 + Redis L2
    ttl:
      overrides:
        "[app:user_name_id_map]": 2h       # 按缓存名单独覆盖 TTL（方括号记法）
  broadcast:
    event-bus-type: com.tbs.broadcast.bus.impl.SimpleEventBus
  lock:
    lock-provider-type: com.tbs.lock.redis.RedissonLockProvider
  data:
    data-holder-manager: com.tbs.common.data.impl.DefaultAutoGenDataHolderManager
    data-holder: com.tbs.redis.holder.RedisDataHolder
  redis:
    enable: true
  redission:
    cache:
      allow-null-values: true
      max-idle-time: 7200
      cache-timeout: 14400
  start-up:
    optional-execute: false
  xxl-job:
    enable: true
    enable-json-job-executor: true
    config:
      admin-address: ${XXL_JOB_ADMIN_ADDRESS:http://127.0.0.1:9898/xxl-job-admin}
      app-name: mainSystemApplication
      access-token: ${XXL_JOB_ACCESS_TOKEN:123456789}
      log-retentions-days: 3
      log-path: "logs/xxl-job/jobhandler"
  knife4j:
    enable: true
    show-url: true
  mq:
    retry:
      scheduler: job        # v1 遗留配置：MQ v2 重构后无代码消费此属性（失败恢复基于自建记录层），可随配置清理移除
  oss:
    enable: ${OSS_ENABLE:false}
    type: ${OSS_TYPE:minio}
    # ... 连接项见环境变量表
```

### 应用自有配置（app.*）

```yaml
app:
  gateway:
    version: ${APP_GATEWAY_VERSION:@project.version@}   # Maven 资源过滤注入
    route-groups: [default]                               # 视图路由分组
    enable-test-controller: pri                            # 测试控制器：none/pub/pri/all
```

### 其他本地配置

| 文件 | 内容 |
| --- | --- |
| `application-db.yml` | 数据源：MySQL 驱动、HikariCP（max 20）、`jkit.sql.auto`（自动建表）、MyBatis 驼峰映射、`tbs.sql` SQL 观测配置 |
| `application-redis.yml` | Redis 连接 |
| `application-mq.yml` | RabbitMQ + 手动 ACK |
| `view-route.yml` | **视图路由 YML 兜底配置**（common/admin/develop 三组菜单，含角色 `MENU.ADMIN`/`MENU.DEV`）；数据库实现启用时此文件不生效 |
| `i18n/messages-app*.properties` | 前端菜单文案（`view.menu.*` key）与取值器文案（`value.getter.*` key），中英文 |

## REST API 清单

路径前缀约定：`/public/**` 免登录放行，`/private/**` 需认证。统一响应结构 `{code, message, data}`（web 模块 `AjaxResult`）。

> **实现位置说明**：本模块的 Controller 仅承担 HTTP 接入；对象存储/公开下载码/数据处理/应用包管理的服务实现位于 `default-system-impl`（`com.tbs.app.service.impl.oss.downloadcode`、`com.tbs.app.data` 等），契约位于 `system-api` 与 `oss-api`（`com.tbs.oss.downloadcode`：存储抽象/条件装配），Redis 存储实现位于 `redis-mod`（`com.tbs.redis.oss.RedisDownloadCodeStorage`），下载码实体与 Mapper 位于 `shared-pojo`/`shared-api` 的 `oss` 子包。

### 公开接口（/public）

| 控制器 | 路径 | 接口 |
| --- | --- | --- |
| `PublicUserController` | `/public/user` | `POST /login`（账密登录→`ITokenModel`）、`GET /refresh`（无感刷新令牌，无 `IRefreshTokenService` Bean 时返回 468） |
| `PublicGatewayVersionController` | `/public/gateway` | `GET /version`（版本探针，见[特殊机制](#特殊机制)） |
| `PublicAppVersionUpdateController` | `/public/app-update` | `POST /check`（客户端更新检查） |
| `PublicGenricController` | `/public/genric` | 泛化 API 调用入口（免认证版）：按数据库配置 `SystemGenericApiEntity` 动态生成 `DubboGenricApiInstance`，经 Dubbo 服务发现调用远端 |
| `PublicTestController` | `/public` | 测试接口（条件装配）：树结构 `getChildren`/`getParentNodes`/`deleteNode`、SpEL `scriptTest` |

### 私有接口（/private）

| 控制器 | 路径 | 接口 |
| --- | --- | --- |
| `PrivateUserController` | `/private/user` | `GET /logout`、`GET /info`（用户信息+角色+部门）、`GET /getRefreshToken`、`POST /setCurrentDepartment/{id}` |
| `AdminUserController` | `/private/admin/user` | 用户 CRUD（继承 `AbstractCrudController`）+ `GET sessions/{id}`（登录会话）、`GET tokens/{sessionId}`、`POST token/disable`（令牌禁用） |
| `RoleController` | `/private/admin/role` | 角色 CRUD + `GET enable/{id}` / `disable/{id}` |
| `AdminRoleController` | `/private/admin/roles` | 用户角色维度：`GET /{userId}`（用户角色）、`POST /query`、`GET account/activite/{userId}`、`GET account/disable/{userId}` |
| `AdminRoleGroupController` | `/private/role/group` | 角色组 CRUD + 组内角色增删（`POST/DELETE roles/{groupId}/{roleId}`）+ 组内用户增删 + `GET detail/{groupId}`、`GET user/{userId}` |
| `AdminCompanyController` | `/private/admin/company` | 公司 CRUD + `POST moveCompanyTo`（树移动）+ `GET listChildren/{parent}` |
| `AdminDepartmentController` | `/private/admin/department` | 部门 CRUD + 移动 + 子部门 |
| `PrivateDepartmentController` | `/private/department` | 部门成员：`GET avaliable/{userId}`、`POST addEmp/{depId}/{userId}`、`POST delEmp/{depId}/{userId}`、`GET employee/{depId}` |
| `SystemDataDicController` | `/private/admin/dic` | 数据字典管理：`POST query`、启停字典/字典项、删除、保存（`DicSaveForm`/`DicItemSaveForm`） |
| `ViewController` | `/private/view` | **动态表单 + 动态路由管理**：表单 CRUD（`GET form/{id}`、`GET listForms`、`POST saveForm`、`DELETE/PUT form`）+ 路由管理（`GET /routes`、`POST /routes/{group}`、`DELETE /routes/{group}`、`GET route-groups`、`POST /routes/refresh`、`GET /routes/{group}/versions`、`POST /routes/{group}/rollback/{versionId}`） |
| `CacheController` | `/private/cache` | 缓存管理：`GET names`、`POST keys`、`POST get`、`POST evict` |
| `OssController` | `/private/oss` | 对象存储：`POST upload`（multipart，最大 1024MB）、`GET upload/progress`（轮询进度）、`GET download`、`GET metadata`、`GET exist`、`GET list`、`GET presigned-url`、`DELETE batch` |
| `OssDownloadCodeController` | `/private/oss/download-code` | 公开下载码管理（遵循接口设计规范）：`save`（POST，生成，可自定义4~64位Base64码）、`get/{code}`（GET）、`update`（POST，码由请求体承载）、`delete/{code}`（POST）、`query`（POST 分页，`pageNum`/`pageSize` 走 query）、`statistics`（GET）；`PublicOssDownloadCodeController` 提供 `/public/oss/download-code/download?code=` 免认证下载 |
| `PrivateDataHandleController` | `/private/data` | 数据处理：`GET handler/list`、`GET handler/validate/{handlerId}`、`POST handle/csv/{handlerId}`（CSV 解析→ITableDataHandler） |
| `AppVersionController` | `/private/admin/app-version` | 应用版本状态机：CRUD + `POST submit/release/offline/rollback/archive/{id}` + `GET latest` + `GET appKeys` |
| `AppVersionCompatibilityPolicyController` | `/private/admin/app-version-policy` | 版本兼容策略：CRUD + `POST enable/disable/{id}` + `POST check` |
| `AppPackageController` | `/private/admin/app-package` | 安装包管理：CRUD + `GET listByVersion/{versionId}` |
| `GenericApiController` | `/private/admin/generic-api` | 泛化 API 配置管理：CRUD + `POST enable/disable/{apiId}` + `GET allPreAuth` |
| `GenricController` | `/private/genric` | 泛化 API 调用入口（认证版，当前用户注入） |
| `AdminSystemLogController` | `/private/log` | 操作日志报表：`GET listWithOpType`、`GET operateTypes`、`POST listRecord`、`POST forceFlush` |
| `PrivateTestController` | `/private` | 测试接口（条件装配）：`GET /test`、`GET /testAuth` |
| `EventSubscriptionController` | `/private/event-subscription` | 订阅编排：`GET reporters` / `events?reporterId=` / `triggers` / `by-event`、`POST query` / `save` / `save-event` / `report`、`POST delete/{id}` / `enable/{id}` / `disable/{id}`；权限 `API.DEV.EVENT.SUBSCRIPTION.*` |
| `EventActionController` | `/private/event-action` | 动作编排：`GET actions` / `by-event`、`POST query` / `save` / `save-event` / `test`、`POST delete/{id}` / `enable/{id}` / `disable/{id}`；权限 `API.DEV.EVENT.ACTION.*` |

> 具体参数与响应结构见 `doc.html`（Swagger 注解齐全，含 `@AjaxResponseType` 包装响应 Schema）。

## 核心配置类与服务

### AppConfig（装配中枢）

| Bean | 说明 |
| --- | --- |
| `appMessageSourceProvider` | 注册 `i18n/messages-app`（前端菜单文案） |
| `rabbitMqSupport` | `SimpleRabbitMqSupport`：RabbitMQ 事件广播支持（频道 `tbs.event.channel`） |
| `loggerRecoreder` | `RabbitMqLoggingRecorder("tbs.log.queue", "systemLog")`：操作日志生产端（组装 v2 信封经 `IMqMessageProducer` 发送 → logging 服务消费落库） |
| `userLoginInfoService` | `DefaultUserLoginInfoService`（system 模块）：令牌↔用户详情映射 |
| `refreshTokenService` | `CookieRefreshTokenService`（system 模块）：刷新令牌服务 |
| `logCacheListenner` | 全量缓存操作日志监听（debug 观测用） |
| `xxlJobTrigger` | `XxlJobScheduleTrigger`：async 调度内核的外部驱动 |
| `redisTaskStore` | `RedisTaskStore`：async 任务持久化存储 |
| `redissonCodec` | `Kryo5Codec`：Redisson 序列化编解码器 |

### DubboGenericCallConfig

注册 Dubbo `ApplicationConfig`/`RegistryConfig` Bean，供 `PublicGenricController` 构造 `DubboGenricApiInstance` 泛化调用使用。

### 条件装配（测试控制器开关）

`@ConditionalOnTestControllerMode` + `TestControllerModeCondition`：按 `app.gateway.enable-test-controller` 的值（`none`/`pub`/`pri`/`all`，默认 `none`，本模块 yml 配置为 `pri`）决定 `PublicTestController`（匹配 `ALL`/`PUB`）与 `PrivateTestController`（匹配 `ALL`/`PRI`）是否注册为 Bean。支持注解多值"或"语义。

### service 层

| 类 | 职责 |
| --- | --- |
| `AppExecutor` | XXL-Job 任务执行器（见[定时任务](#定时任务)） |
| `CacheListenner` | 缓存键收集器：`onPut`/`onEvict` 等事件把缓存键登记进 `CACHE_INFO_HOLDER` DataHolder（供 `CacheController` 的 keys 查询；经 Redis 持有，多实例共享） |
| `getter/` | 值获取器集合（见[值获取器](#值获取器动态事件条件来源)）：系统信息/当前用户/日期时间/配置属性/数据持有者变量/请求上下文，供脚本与未来动态事件触发系统作为条件来源 |
| `event/` | 事件上报与动作执行链路（见[事件订阅与动作编排链路](#事件订阅与动作编排链路)）：`EventReportSupport`、`DbEventSubscriptionRegistry`、`ActionParamInterpolator`、`ActionConfigValidator`，以及 `reporter/` / `action/` / `condition/` 子包实现 |
| `oss/UploadTaskProgressManager` | 内存上传进度注册表：`register/updateTransferred/markSuccess/markFailed/get`，条目 10 分钟过期自动清理，供前端轮询 `GET /private/oss/upload/progress` |

> 业务服务实现（用户/角色/字典/版本/树结构等）位于 `default-system-impl` 模块（同包名 `com.tbs.app.service`），本模块仅做控制器层装配。

### form / vo

- form：`LoginForm`（登录）、`CacheForm`、`DicSaveForm`/`DicItemSaveForm`/`DicSearchForm`/`DicItemSearchForm`（字典）、`DisableTokenForm`（令牌禁用）、`EventSubscriptionSaveForm`/`EventActionSaveForm`（按事件整体替换绑定）、`EventActionTestForm`（单动作测试）
- vo：`UserInfoVO`（用户信息聚合）、`RoleVO`、`UploadTaskProgressVO`（上传进度：taskId/status/totalBytes/transferredBytes/percentage）、`EventActionTestResultVO`（动作测试回执：actionId/success/elapsedMs/errorMsg）

## 定时任务（AppExecutor）

| `@XxlJob` Handler | 行为 |
| --- | --- |
| `company_tree_rebuild` | 公司树重建（当前为空实现） |
| `department_tree_rebuild` | 部门树重建（`DepartmentTreeStructService.rebuildTree()`，闭包表关系重建） |
| `flushLogData` | 操作日志刷盘：`ILogRecorder.flush()`（将 `tbs.log.queue` 缓冲日志推入 MQ） |

另有 xxl-job-mod 提供的三个通用 Handler：`jsonJobHandler`（JSON 任务）、`runningBackGroundJobHandler`（批量 `IBackgroundJob` 驱动）、`available`/`help_json`（查询已注册 Handler）。

## 特殊机制

### 网关版本探针（蓝绿部署验证）

`GET /public/gateway/version`（免认证）返回：

| 字段 | 说明 |
| --- | --- |
| `version` | 版本号，解析优先级：`APP_GATEWAY_VERSION` 启动参数 → `build-info.properties`（Maven 打包）→ `@project.version@` 资源过滤注入 → `unknown` |
| `artifact` / `buildTime` | 构建信息（仅 Maven 打包后存在，IDE 运行为 unknown） |
| `instancePort` | 当前实例端口（蓝绿切换时区分流量落点） |
| `viewVersion` | 视图路由配置版本号（版本表 `MAX(id)`，YML 实现时为 0）——**前端菜单缓存比对依据**（app-ui-vue 拉取本地缓存的版本号与之比较，不一致则重新拉取动态路由） |

### 值获取器（动态事件条件来源）

`com.tbs.app.service.getter` 下的 `IValueGetter` 实现由 `IValueGetterService` 统一注册与调用，并经 `ValueGetterController`（`private/value-getter/list|detail|invoke`）暴露给前端调试。取值失败一律返回空字符串，不中断脚本执行。

| Getter ID | 用途 |
| --- | --- |
| `SystemInfoGetter` | 主机/系统信息：主机名称、IP、MAC、端口、OS、CPU、内存 |
| `CurrentUserGetter` | 当前登录会话：登录状态、用户名、会话 ID、令牌过期时间/类型/状态（不返回令牌原文） |
| `DateTimeGetter` | 当前时间：时间戳、日期时间、日期、时间、年/月/日/星期/小时，可选 Java ZoneId 时区 |
| `ConfigPropertyGetter` | 配置属性：仅允许 `tbs.`/`app.`/`server.` 前缀及 `spring.application.name`，含敏感词的 key 一律拒绝 |
| `DataHolderGetter` | 运行时变量：按 holder 名与 key 只读访问本地/Redis DataHolder，默认 `defaultDataHolder` |
| `RequestContextGetter` | 请求上下文：traceId 或自定义上下文变量 |
| `ConditionGetter` | 布尔条件：对左右操作数比较输出 true/false，两个操作数都可以是另一次 getter 调用，可嵌套为复合条件 |

`ConditionGetter` 的操作数中，普通字符串按字面量处理；以 `{` 开头的字符串按 JSON 解析为 getter 调用：

```json
{"getterId":"DateTimeGetter","paramValues":[{"value":"dayOfWeek"}]}
```

例如左操作数填上述 JSON、运算符选 `ge`、右操作数填 `5`，即「星期 >= 5 则返回 true」。两个操作数都可填 getter 调用以比较两个 getter 的返回值，也可嵌套 `ConditionGetter` 自身构建复合条件（内置循环引用检测，最大嵌套深度 8 层）。大小比较在两边均为数值时按数值比较，否则按字典序；任何取值失败均返回 false。

`AbstractStringValueGetter` 收敛字符串结果构造、参数按下标读取与 i18n 文案解析。文案位于 `i18n/messages-app*.properties` 的 `value.getter.*` 命名空间。`@Component` 实现 `IValueGetter` 即被 `IRegistryCenter` 自动发现，新增 getter 无需改动注册代码。

### 事件订阅与动作编排链路

本模块是事件链路的**接入层**：脚本模块（`script-api`）提供事件/触发器/动作的契约与编排内核，`system` 模块提供服务与存储实现，本模块负责埋点、持久化覆盖、动作实现与 HTTP 管理接口。

```
业务代码埋点（AdminUserController / PrivateUserController）
  → EventReportSupport.reportQuietly(reporterId, eventCode, params)
  → IEventOrchestrator.report(envelope)
      → BusinessEventReporter.report（编排层已完成校验，上报器记录审计日志；返回汇总结果由编排层组装）
      → DbEventSubscriptionRegistry.find（sys_event_subscription，@Primary 覆盖内存实现）
      → ConditionRuleTrigger → ConditionRuleConditionEvaluator → ConditionRuleEvaluator
          → IValueGetterService.invokeValueGetter（sys_condition_rule 中 getter 编排）
      → ConditionActionTriggerCallback
          → sys_event_action（顺序读取启用绑定）
          → ActionParamInterpolator（${paramName} 替换为事件参数）
          → ITriggerAction.execute（PushNotifyAction / LogEventAction）
```

| 类 | 职责 |
| --- | --- |
| `service/event/EventReportSupport` | 统一上报门面：把 `Map`/`Collection` 参数转成 `ParamValue` 并构造 `EventEnvelope`；`reportQuietly` 捕获全部异常只记 warn，保证事件链路故障不影响主业务 |
| `service/event/reporter/BusinessEventReporter` | 声明用户事件 `user.created` / `user.updated` / `user.deleted` / `user.loggedOut`，参数 `userId` / `userName` 均带 `ParamType` 元数据 |
| `service/event/DbEventSubscriptionRegistry` | `@Primary` 覆盖脚本模块 `InMemoryEventSubscriptionRegistry`；每次读取实时查库，写操作走 `IEventSubscriptionService.replaceByEvent` 增量合并 |
| `service/event/condition/ConditionActionTriggerCallback` | 继承 `AbstractActionConditionTriggerCallback`，一次查询解析全部动作绑定；参数 JSON 非法或动作未注册时跳过该动作并 warn |
| `service/event/ActionParamInterpolator` | 用正则解析 `${paramName}` 占位符，仅替换本次事件参数中存在的同名参数 |
| `service/event/ActionConfigValidator` | 保存前校验动作已注册、`paramJson` 为合法 `List<ParamValue>` 数组 |
| `service/event/action/PushNotifyAction` | 复用 `IPushNotifyService.send`，参数含 `msgType` / `title` / `content` / `priority` / `topic` / `url`，`source` 固定为 `event` |
| `service/event/action/LogEventAction` | 最小示例动作：分级日志输出事件编码与参数 |

埋点位置：`AdminUserController.save`（按 id 是否为空区分 created/updated）、`AdminUserController.delete`、`PrivateUserController.logout`（在令牌失效前读取用户名）；均只在主流程成功后调用 `reportQuietly`。

前端对接接口：`EventSubscriptionController` 提供上报器/事件/触发器元数据、订阅 CRUD 与 `POST report` 模拟上报（返回 `EventReportResult` 及触发器明细）；`EventActionController` 提供动作元数据、动作绑定 CRUD 与 `POST test` 单动作测试。

持久化与初始化：两张表 `sys_event_subscription`、`sys_event_action` 由 `shared-pojo` 实体定义（索引声明在 `@Table`），DDL、API 权限（`API.DEV.EVENT.SUBSCRIPTION.*` / `API.DEV.EVENT.ACTION.*`）与开发者角色组绑定分别见 `etc/sql_file/sys_event_subscription.sql`、`etc/sql_file/sys_event_action.sql`，全量结构同步在 `etc/sql_file/app_system_struct.sql`。未执行权限脚本时管理接口会返回 403，前端下拉框将为空。

i18n：校验与业务文案分别在 `i18n/messages-app*.properties`（`event.action.*` / `event.subscription.*`）与 `system` 模块的 `messages-system*.properties`（服务层校验）中维护，中英文同步。

### 登录与令牌链路

```
POST /public/user/login (LoginForm)
  → IUsernamePasswordAccessTokenProvider.requestToken
      → IUserInfoService.loadUserInfoByUniqueKey（default-system-impl）
      → IUserValidator 链（账号状态校验）
      → ITokenIssuanceCenter.issueToken（DefaultTokenSignCenter：雪花 ID + 28 天窗口 + 缓存）
  → ITokenModel（token/expireTime/sessionId/tokenType...）

GET /public/user/refresh（携带旧访问令牌）
  → CookieRefreshTokenService.useRefreshToken
  → 新 ITokenModel（AjaxResult 包装）
```

### 表结构自动维护

jkit-sql-auto（`jkit.sql.auto.mode: update`）：按 `jkit.sql.auto.packages` 扫描 shared/tree-struct/dic 等模块的实体表（`sys_user`、`tree_node` 等）并在启动时自动创建/更新，无需手工 DDL。

### SQL 观测

SQL 观测由 `sql` 模块的 JDBC 装饰层统一承担（`tbs.sql.enable-datasource-intercept: true`）：全量 SQL 经 `datasource.sql.execution` 日志器输出（含耗时），执行耗时达到 `tbs.sql.slow-sql-threshold-millis`（默认 2000ms）以 WARN 输出慢 SQL 告警，`tbs.sql.exclude-sql` 排除健康检查语句。

## 构建与部署

```bash
# 完整构建（repackage 可执行 jar + build-info）
mvn clean package -pl app -am

# 运行
java -jar app/target/app-1.0-SNAPSHOT.jar

# 覆盖版本标识（蓝绿部署验证）
java -DAPP_GATEWAY_VERSION=1.2.3-hotfix -jar app/target/app-1.0-SNAPSHOT.jar

# 指定 Nacos 与数据库（覆盖本地 yml 默认值）
DB_HOST=mysql.internal NACOS_ADDRESS=nacos.internal:8848 java -jar app-*.jar
```

生产建议：将 `RABBITMQ.yml`/`CoreAppDb.yml`/`REDIS.yml`/`app-service.yml` 配置迁移到 Nacos 配置中心（远程优先于本地 profile，支持 `refreshEnabled` 动态刷新）；优雅停机超时已设 30s（`spring.lifecycle.timeout-per-shutdown-phase`），配合 Dubbo `shutdown.await-duration: 10000`。

## 注意事项

1. **MyBatis 读写 + jkit 建表**：jkit-sql-auto 只负责启动期 DDL（`jkit.sql.auto.mode: update`），业务 CRUD 走自研 sql 框架/MyBatis 的 `GenericMapper`；生产环境建议把 `jkit.sql.auto.enabled` 置 `false`（或 `mode: none`），用迁移脚本管理表结构。
2. **`enable-test-controller` 默认 none，本模块配置为 `pri`**：`PrivateTestController`（`/private/test`、`/private/testAuth`）默认注册；生产部署应改回 `none` 关闭全部测试控制器。
3. **`dubbo.consumer.filter: -ContextHolderAuthenticationPrepareFilter`**：显式移除 web 模块的 Dubbo 消费端认证上下文过滤器，本应用作为消费端调用时不注入用户身份头。
4. **上传大小限制 1024MB/2048MB**：远大于 Spring Boot 默认（1MB/10MB），网关/反向代理层需同步放开限制，且 Undertow 缓冲区会占用内存。
5. **`view-route.yml` 仅在 YML 路由实现下生效**：当前 `tbs.view.route.view-route-service-class` 默认数据库实现，菜单实际从 `view_route` 等表加载；该文件只是兜底参考。
6. **Nacos optional 语义**：配置中心不可用不阻断启动，但 MQ/DB 等远程覆盖配置失效后回退本地默认值，多环境部署时注意本地默认值与远程配置的一致性。
7. **`oss.enable` 默认 false**：未显式开启（`OSS_ENABLE=true` 或 tbs.oss.enable=true）时 `OssController` 的对象存储调用会因无 `OssClient` Bean 而失败。
8. **登录依赖 system 模块实现**：`DefaultTokenSignCenter` 等来自 `default-system-impl`，直接移除该依赖会导致登录不可用（web 模块默认 `NoImplTokenIssuanceCenter` 调用即抛异常）。
9. **上传进度仅内存态**：`UploadTaskProgressManager` 为单实例内存注册表，多实例负载均衡下进度查询可能落到其他实例而查不到（返回 null）；条目 10 分钟过期。
10. **日志链路依赖 RabbitMQ 与 logging 服务**：操作日志经 `tbs.log.queue` 异步落库，logging-service 未启动时日志仅滞留 MQ；`flushLogData` 定时任务负责兜底刷盘。

## 包结构

```
com.tbs.app
├── AppApplication              启动类（16 个平台注解）
├── config/
│   ├── AppConfig               装配中枢（10 个 Bean：日志通道/进度管理/调度驱动/编解码等）
│   ├── DubboGenericCallConfig  Dubbo 泛化调用配置
│   ├── condition/              ConditionalOnTestControllerMode + TestControllerModeCondition
│   │   （TestControllerMode：NONE/PUB/PRI/ALL）
│   └── property/              GatewayProperty（app.gateway：version/routeGroups/enableTestController）
├── controller/
│   ├── publics/               公开接口（免登录）：登录/刷新/版本探针/更新检查/泛化调用/测试
│   └── privates/              私有接口（需登录）：用户/角色/角色组/公司/部门/字典/视图路由/
│                              缓存/OSS/版本管理/泛化API配置/操作日志/测试
├── enums/                      （空目录）
├── form/                       LoginForm/CacheForm/Dic*Form/DisableTokenForm
├── service/
│   ├── AppExecutor             XXL-Job 任务执行器（树重建/日志刷盘）
│   ├── CacheListenner          缓存键收集（DataHolder 登记）
│   ├── getter/                 IValueGetter 实现：SystemInfo/CurrentUser/DateTime/
│   │                           ConfigProperty/DataHolder/RequestContext/Condition
│   │                           （+抽象基类）
│   └── oss/UploadTaskProgressManager   上传进度注册表
└── vo/                         UserInfoVO/RoleVO/UploadTaskProgressVO

resources/
├── application.yml             主配置（profiles/Nacos导入/Dubbo/app.gateway）
├── application-tbs.yml          平台 tbs.* 配置聚合
├── application-db.yml           数据源（MySQL 驱动/HikariCP/jkit 建表配置 + tbs.sql 观测）
├── application-redis.yml        Redis
├── application-mq.yml           RabbitMQ
├── view-route.yml               视图路由 YML 兜底配置
└── i18n/messages-app*.properties  前端菜单文案（中英文）
```
