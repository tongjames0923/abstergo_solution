# Abstergo Solution

Abstergo Solution 是一个基于 Spring Boot 2.7.14
的企业级应用开发框架，提供了完整的后端解决方案和现代化的前端管理界面。该项目采用模块化设计，集成了常用的企业级应用功能，如认证授权、缓存、异步处理、消息队列、数据库访问、分布式服务等，旨在为企业级应用提供开箱即用的技术解决方案。

## 目录结构

```
.
├── app                    # 应用主模块，整合各功能模块并提供应用入口
├── app-ui-vue            # 基于Vue.js的前端管理界面
├── async                 # 异步处理模块，包括线程管理、定时任务、分布式锁等
├── cache                 # 缓存模块，提供带TTL功能的缓存管理
├── common                # 通用工具模块，包含基础工具类和接口定义
├── docker                # Docker配置文件
├── rabbitmq-mod          # RabbitMQ消息队列集成模块
├── redis-mod             # Redis集成模块，提供缓存、分布式锁等功能
├── shared                # 共享模块，包含接口定义和POJO实体类
├── sql                   # 数据库访问模块，基于MyBatis和Spring Data JPA
├── system                # 系统管理模块，提供系统配置和核心业务逻辑
├── web                   # Web模块，提供认证授权、API网关、统一结果返回等功能
└── pom.xml               # Maven项目配置文件
```

## 项目概述

### 设计理念

Abstergo Solution 遵循以下设计理念：

1. **模块化设计**：通过功能模块划分，实现高内聚、低耦合的架构
2. **可扩展性**：提供丰富的接口和抽象类，便于功能扩展和定制
3. **企业级特性**：集成认证授权、缓存、异步处理、消息队列等企业级应用必需功能
4. **开箱即用**：提供完整的解决方案，减少重复开发工作

### 技术架构

Abstergo Solution 采用分层架构设计，主要包括以下几个层次：

1. **表现层（Presentation Layer）**：
   - Web模块提供RESTful API接口
   - Vue.js前端界面提供用户交互

2. **业务逻辑层（Business Logic Layer）**：
   - App模块作为业务逻辑整合层
   - System模块提供系统核心业务功能

3. **数据访问层（Data Access Layer）**：
   - SQL模块提供数据库访问能力
   - Cache模块提供缓存支持

4. **集成层（Integration Layer）**：
   - Redis-mod和RabbitMQ-mod提供与外部系统的集成
   - Async模块提供异步处理能力

5. **基础层（Infrastructure Layer）**：
   - Common模块提供通用工具和基础组件
   - Shared模块提供共享接口和实体类

### 技术栈

- **后端**：
  - Spring Boot 2.7.14
  - Spring Security
  - Spring Data JPA
  - MyBatis (TK Mapper 4.2.3)
  - MySQL 8.0.32
  - Redis (Redisson 3.52.0)
  - RabbitMQ
  - Dubbo 3.2.5 + Zookeeper 3.6.3
  - Maven 3.6+
  - P6Spy 3.8.2 (SQL日志)
  - Knife4j 3.0.3 (API文档)

- **前端**：
    - Vue.js 2.x
  - Element UI
  - Axios
    - Vuex (状态管理)
    - Vue Router (路由管理)

### 核心功能

1. **认证与授权**：基于Spring Security的完整认证授权体系
2. **缓存管理**：支持本地缓存和Redis缓存，提供TTL功能
3. **异步处理**：线程池管理、定时任务、分布式锁、事务管理、事件广播等异步处理功能
4. **消息队列**：集成RabbitMQ，支持分布式事件处理
5. **数据库访问**：提供实体查询框架，简化数据库操作
6. **分布式支持**：集成Dubbo和Zookeeper，支持分布式部署

## 模块详细介绍

### 1. Common 通用工具模块

[common](common) 模块是项目的核心基础组件库，提供了一系列常用的工具类、配置和接口，用于简化开发流程，提高代码复用性和一致性。主要包含：

- **基础组件**：
  - [BaseEnum](common/src/main/java/com/tbs/common/BaseEnum.java) 枚举基类，为所有枚举提供统一接口
  - [IStartup](common/src/main/java/com/tbs/common/IStartup.java) 启动接口，用于应用启动后的初始化操作

- **配置体系**：
  - 属性配置类（UuidProperty、DataHolderProperty、DelegateProperty）
  - 配置管理类（UuidConfig、DataHolderConfig、DelegateConfig）

- **数据持有者(DataHolder)**：
  - [IDataHolder](common/src/main/java/com/tbs/common/data/IDataHolder.java) 数据存储接口
  - MapDataHolder 基于Map的数据存储实现
  - SpringManagedDataHolderManager Spring管理的数据持有者管理器

- **代理模式(Delegate)**：
  - [IDelegateInvoker](common/src/main/java/com/tbs/common/delegate/IDelegateInvoker.java) 代理执行器接口
  - CompletableFutureDelegateFlag 基于CompletableFuture的异步代理执行标记
  - CallerRunsDelegateTarget 在调用者线程中执行的代理目标

- **树结构(Tree)**：
  - [ITree](common/src/main/java/com/tbs/common/tree/ITree.java) 树接口
  - [ITreeNode](common/src/main/java/com/tbs/common/tree/ITreeNode.java) 树节点接口
  - MultiwayTree 多叉树实现

- **工具类矩阵**：
  - 加密工具类（AESUtil、RSAUtil、EncryptUitl）
  - 框架工具类（DelegateUtil、UuidUtil）
  - 常用工具类（BeanUtil、CollUtil、EnumUtil、LambdaUtil、SpringUtil、StrUtil、TimeUtil）

- **UUID生成体系**：
  - [IUuidGenerator](common/src/main/java/com/tbs/common/uuid/IUuidGenerator.java) UUID生成器接口
  - SimpleUUID 基于哈希算法的简单UUID实现
  - SnowFlakeUUID 基于雪花算法的UUID实现

### 2. Web 模块

[web](web) 模块是基于 Spring Boot 和 Spring Security 构建的 Web 应用框架，提供了认证授权、API 网关、统一结果返回、链路追踪等核心功能。

- **认证与授权**：
  - [IAuthenticationCenter](web/src/main/java/com/tbs/web/security/authentication/center/IAuthenticationCenter.java) 认证中心接口
  - SimpleAuthenticationCenter 简单认证中心实现
  - Token签发中心（ITokenIssuanceCenter）
  - 访问令牌提供者（IUserAccessTokenProvider）
  - [ITokenModel](web/src/main/java/com/tbs/web/security/token/ITokenModel.java) Token模型接口

- **API网关**：
  - [IGateWayCenter](web/src/main/java/com/tbs/web/gate/center/IGateWayCenter.java) 网关中心接口
  - LoginGateWayCenter 登录网关中心实现
  - Token处理过滤器（TokenHandleFilter）

- **统一结果返回**：
  - [@Wrapped](web/src/main/java/com/tbs/web/result/annotation/Wrapped.java) 标记需要包装返回结果的方法
  - [@NoWrapped](web/src/main/java/com/tbs/web/result/annotation/NoWrapped.java) 标记不需要包装返回结果的方法
  - [AjaxResult](web/src/main/java/com/tbs/web/result/AjaxResult.java) Ajax结果类

- **链路追踪**：
  - [@EnableTrace](web/src/main/java/com/tbs/web/trace/annotation/EnableTrace.java) 启用链路追踪
  - [@Traced](web/src/main/java/com/tbs/web/trace/annotation/Traced.java) 标记需要追踪的方法
  - TraceObject 追踪对象
  - SpringManagedTraceContext Spring管理的追踪上下文

### 3. Async 异步功能模块

[async](async) 模块提供了完整的异步编程解决方案，包括线程管理、定时任务、分布式锁、事务管理、事件广播等核心功能。

- **异步注解和AOP**：
  - [@LongTermAsync](async/src/main/java/com/tbs/async/annotation/LongTermAsync.java) 长期异步执行注解
  - LongTermAsyncAop 切面处理

- **事件广播系统**：
  - [IEvent](async/src/main/java/com/tbs/broadcast/event/IEvent.java) 事件接口
  - [IPublisher](async/src/main/java/com/tbs/broadcast/publisher/IPublisher.java) 事件发布者接口
  - [IEventDispatcher](async/src/main/java/com/tbs/broadcast/dispatcher/IEventDispatcher.java) 事件分发器接口
  - [IEventBus](async/src/main/java/com/tbs/broadcast/bus/IEventBus.java) 事件总线接口

- **分布式锁**：
  - [ILock](async/src/main/java/com/tbs/lock/ILock.java) 锁接口
  - [ILockProvider](async/src/main/java/com/tbs/lock/ILockProvider.java) 锁提供者接口
  - LockUtil 锁工具类

- **定时任务**：
  - [ISchedule](async/src/main/java/com/tbs/schedule/ISchedule.java) 定时任务接口
  - [IScheduleTask](async/src/main/java/com/tbs/schedule/IScheduleTask.java) 定时任务实体接口

- **线程管理**：
  - [IThreadPoolManager](async/src/main/java/com/tbs/thread/IThreadPoolManager.java) 线程池管理器接口
  - [IThreadRequest](async/src/main/java/com/tbs/thread/IThreadRequest.java) 线程请求接口

- **事务管理**：
  - [ITransaction](async/src/main/java/com/tbs/transaction/ITransaction.java) 事务接口
  - [ITransactionInfo](async/src/main/java/com/tbs/transaction/ITransactionInfo.java) 事务信息接口

### 4. Cache 缓存模块

[cache](cache) 模块是基于Spring Cache的扩展实现，提供了带TTL（Time To Live）功能的缓存管理器，支持缓存过期处理和监听功能。

- **配置类**：
  - [TbsCacheProperty](cache/default-impl/src/main/java/com/tbs/cache/config/TbsCacheProperty.java) 缓存属性配置
  - [TbsCacheConfig](cache/default-impl/src/main/java/com/tbs/cache/config/TbsCacheConfig.java) 缓存配置

- **核心实现**：
  - [AbstractTtlAdaptCacheMananger](cache/api/src/main/java/com/tbs/cache/integration/AbstractTtlAdaptCacheMananger.java) 抽象TTL缓存管理器
  - [DefaultTtlAdaptCacheMananger](cache/default-impl/src/main/java/com/tbs/cache/impl/DefaultTtlAdaptCacheMananger.java) 默认TTL适配缓存管理器
  - [ITtlCacheDecorator](cache/api/src/main/java/com/tbs/cache/ITtlCacheDecorator.java) TTL缓存装饰器接口
  - [DefaultTtlCacheDecorator](cache/default-impl/src/main/java/com/tbs/cache/impl/DefaultTtlCacheDecorator.java) 默认TTL缓存装饰器

- **缓存限制**：
  - [ICacheLimiter](cache/api/src/main/java/com/tbs/cache/limit/ICacheLimiter.java) 缓存限制器接口
  - [ICacheOperationRecorder](cache/api/src/main/java/com/tbs/cache/limit/ICacheOperationRecorder.java) 缓存操作记录器接口

- **手动缓存**：
  - [IManualCacheHandler](cache/api/src/main/java/com/tbs/cache/manual/IManualCacheHandler.java) 手动缓存处理器接口
  - [MapCacheHandler](cache/api/src/main/java/com/tbs/cache/manual/impl/MapCacheHandler.java) Map缓存处理器

- **监听器**：
  - [ICacheListenner](cache/api/src/main/java/com/tbs/cache/ICacheListenner.java) 缓存监听器接口

### 5. SQL 数据库访问模块

[sql](sql) 模块是一个功能强大的数据库访问框架，基于 MyBatis 和 Spring Data JPA 构建，提供了实体查询、动态 SQL 生成、自动填充、SQL 日志记录等功能。

- **实体查询框架 (Entity Framework)**：
  - [IEntityQueryWrapper](sql/src/main/java/com/tbs/sql/ef/IEntityQueryWrapper.java) 实体查询包装器
  - [IQueryQueryBuilder](sql/src/main/java/com/tbs/sql/ef/IQueryQueryBuilder.java) 查询构建器
  - [IQueryWhereProvider](sql/src/main/java/com/tbs/sql/ef/IQueryWhereProvider.java) 条件提供者
  - [IQueryOrderProvider](sql/src/main/java/com/tbs/sql/ef/IQueryOrderProvider.java) 排序提供者
  - [IQueryPageProvider](sql/src/main/java/com/tbs/sql/ef/IQueryPageProvider.java) 分页提供者

- **注解系统**：
  - [@SelectSQL](sql/src/main/java/com/tbs/sql/annotation/SelectSQL.java) 自定义SQL查询注解
  - [@QueryField](sql/src/main/java/com/tbs/sql/annotation/QueryField.java) 查询字段注解
  - [@SqlAutoValue](sql/src/main/java/com/tbs/sql/annotation/SqlAutoValue.java) 自动值填充注解

- **SQL驱动和上下文**：
  - [ISqlContext](sql/src/main/java/com/tbs/sql/framework/driver/ISqlContext.java) SQL上下文
  - [IQueryDriver](sql/src/main/java/com/tbs/sql/framework/driver/IQueryDriver.java) 查询驱动

- **映射器**：
  - [IFrameworkMapper](sql/src/main/java/com/tbs/sql/mapper/IFrameworkMapper.java) 框架映射器接口
  - [IRawSqlSelectMapper](sql/src/main/java/com/tbs/sql/mapper/IRawSqlSelectMapper.java) 原始SQL映射器接口

### 6. App 应用主模块

[app](app) 模块是整个应用程序的主入口模块，负责启动 Spring Boot 应用并整合各个功能模块。

- **启动类**：
  - [AppApplication](app/src/main/java/com/tbs/app/AppApplication.java) Spring Boot启动类

- **配置类**：
  - 应用配置类（config）

- **控制器类**：
  - 处理HTTP请求的控制器（controller）

- **业务服务类**：
  - 业务逻辑处理的服务（service）

### 7. Shared 共享模块

[shared](shared) 模块包含项目中共享的接口定义和 POJO 实体类，为其他模块提供统一的数据结构和接口规范。

- **Interfaces模块**：
  - 共享接口定义
  - 统一的API契约

- **POJO模块**：
  - 实体类（Entity）
  - 数据传输对象（DTO）
  - 视图对象（VO）

### 8. System 系统管理模块

[system](system) 模块提供系统级功能和服务，包括系统配置、核心业务逻辑和系统管理功能。

- **配置类**：
  - 系统参数配置
  - 组件初始化配置

- **表单类**：
  - 接收前端请求数据的表单

- **业务服务类**：
  - 系统核心业务逻辑实现

- **Web控制器类**：
  - 系统管理相关HTTP请求处理

### 9. Redis 集成模块

[redis-mod](redis-mod) 模块提供了基于 Redis 的缓存、分布式锁、数据存储和消息广播功能，是构建高性能分布式应用的重要组件。

- **Redis配置**：
  - [BasicRedisConfig](redis-mod/src/main/java/com/tbs/redis/config/BasicRedisConfig.java) Redis核心配置类
  - [RedisProperty](redis-mod/src/main/java/com/tbs/redis/config/property/RedisProperty.java) Redis属性配置类
  - [RedissonCacheConfig](redis-mod/src/main/java/com/tbs/redis/config/RedissonCacheConfig.java) Redisson缓存配置类
  - [RedissonCacheProperty](redis-mod/src/main/java/com/tbs/redis/config/property/RedissonCacheProperty.java) Redisson缓存属性配置类

- **数据持有者**：
  - [RedisDataHolder](redis-mod/src/main/java/com/tbs/redis/holder/RedisDataHolder.java) Redis数据持有者

- **分布式锁**：
  - [RedissonLock](redis-mod/src/main/java/com/tbs/redis/lock/RedissonLock.java) Redisson锁实现
  - [RedissonLockProvider](redis-mod/src/main/java/com/tbs/redis/lock/RedissonLockProvider.java) Redisson锁提供者

- **消息广播**：
  - [AbstractRedisEventSupport](redis-mod/src/main/java/com/tbs/redis/boradcast/AbstractRedisEventSupport.java) Redis消息广播抽象基类
  - SimpleRedisEventSupport 具体实现

- **缓存装饰器**：
  - [RedissonAdaptTtlCache](redis-mod/src/main/java/com/tbs/redis/cache/RedissonAdaptTtlCache.java) Redisson适配TTL缓存
  - [RedissonTtlCacheDecorator](redis-mod/src/main/java/com/tbs/redis/cache/RedissonTtlCacheDecorator.java) Redisson TTL缓存装饰器

### 10. RabbitMQ 集成模块

[rabbitmq-mod](rabbitmq-mod) 模块提供了基于 RabbitMQ 的消息广播和分布式事件处理功能，实现了应用间的异步通信。

- **RabbitMQ配置**：
  - [RabbitMqConfig](rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/config/RabbitMqConfig.java) RabbitMQ核心配置类
  - [RabbitMqProperty](rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/config/property/RabbitMqProperty.java) RabbitMQ属性配置类

- **支持类**：
  - [AbstractRabbaitMqSupport](rabbitmq-mod/src/main/java/com/tbs/broadcast/rabbitmq/support/AbstractRabbaitMqSupport.java) RabbitMQ支持抽象基类
  - SimpleRabbitMqSupport 具体实现

### 11. 前端管理界面

[app-ui-vue](app-ui-vue) 模块是基于 Vue.js 和 Element UI 构建的现代化前端管理界面，提供了用户友好的操作界面。

- **技术栈**：
  - Vue.js 2.x
  - Element UI 组件库
  - Axios HTTP客户端

- **功能特性**：
  - 响应式布局
  - 权限控制
  - 动态路由
  - 国际化支持

- **核心组件**：
  - **DynamicForm 动态表单组件**：
    - 支持通过配置自动生成表单
    - 提供多种字段类型（输入框、选择器、日期范围等）
    - 支持自定义组件扩展
    - 内置表单验证和重置功能
  
  - **FormEditor 表单编辑器组件**：
    - 可视化表单配置编辑器
    - 支持字段的增删改和排序
    - 提供丰富的字段类型配置选项
    - 支持JSON格式配置导入导出
  
  - **DynamicTable 动态表格组件**：
    - 支持通过配置自动生成表格
    - 内置分页、排序、筛选功能
    - 支持自定义列渲染
    - 提供操作列和选择列功能
  
  - **QuickUseManagementView 管理视图组件**：
    - 集成DynamicForm和DynamicTable的一站式管理界面
    - 自动处理增删改查操作
    - 支持自定义操作按钮和下拉菜单
    - 提供统一的UI风格和交互体验
  
  - **其他功能组件**：
    - DynamicOptions 动态选项组件
    - UrlImage 图片URL组件
    - CardList 卡片列表组件
    - UserAvatar 用户头像组件

- **快速搭建管理界面**：
  
  项目通过组合使用DynamicForm、FormEditor、DynamicTable和QuickUseManagementView等核心组件，可以快速搭建功能完整的管理界面。以用户管理模块为例：
  
  1. **配置驱动开发**：
     - 使用FormEditor可视化配置搜索表单和新增/编辑表单
     - 通过配置定义表格列和操作按钮
     - 所有UI元素均通过JSON配置驱动，无需编写重复代码
  
  2. **组件复用**：
     - QuickUseManagementView封装了常见的管理界面模式
     - 通过props传入配置名称和处理器函数即可工作
     - 支持自定义操作按钮和下拉菜单
  
  3. **用户管理界面搭建过程**：
     UserAdminView组件的构建过程如下：
     
     - **基础结构**：使用QuickUseManagementView作为主容器，它内部整合了DynamicForm（用于搜索）和DynamicTable（用于数据展示）
     
     - **搜索功能**：通过config-name属性指定搜索表单配置，searchHandler处理搜索请求
     
     - **数据展示**：通过columns属性定义表格列，actions属性定义操作按钮
     
     - **新增功能**：通过add-form-config-name指定新增表单配置，add-handler处理新增请求
     
     - **编辑功能**：通过edit-form-config-name指定编辑表单配置，edit-handler处理编辑请求
     
     - **自定义操作**：通过actions-dropdown插槽添加更多操作按钮，如"禁用/启用"、"登录信息"、"重置密码"、"权限管理"等
     
     - **弹窗组件**：针对特殊操作（如重置密码、查看登录信息、权限管理）使用el-dialog创建独立的弹窗界面
     
     - **权限管理**：通过UserRoleAdminView组件实现，内部使用CardList组件以卡片形式展示角色信息，支持搜索和分页
     
     - **登录信息查看**：使用DynamicTable组件展示会话和Token信息，支持两层数据展示（会话列表和Token详情）
     
     整个UserAdminView通过组合使用多个核心组件，实现了完整的用户管理功能，包括CRUD操作和扩展功能。
  
  4. **统一UI风格**：
     - 所有组件遵循一致的设计语言
     - 支持响应式布局适配不同屏幕尺寸
     - 提供统一的操作反馈和错误处理机制

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+ (或 5.7+)
- Redis 3.0+ (建议 5.0+)
- RabbitMQ 3.7+ (建议 3.8+)
- Zookeeper 3.6+ (如需使用 Dubbo)
- Node.js 14+ 和 npm 6+ (仅前端需要)

### 后端部署

1. 克隆项目代码：
   ```bash
   git clone <repository-url>
   cd abstergo_solution
   ```

2. 初始化数据库：
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE your_database DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   # 执行 SQL 脚本 (位于 app/src/main/resources/sql/ 目录)
   mysql -u root -p your_database < app/src/main/resources/sql/init.sql
   ```

3. 配置应用：
   ```bash
   # 修改 app/src/main/resources/application.yml 以及相关配置文件
   # application-db.yml - 数据库配置
   # application-redis.yml - Redis配置
   # application-mq.yml - RabbitMQ配置
   # application-tbs.yml - 业务配置
   ```

4. 编译和打包：
   ```bash
   # 清理并编译整个项目
   mvn clean install
   
   # 或者只编译不执行测试
   mvn clean install -DskipTests
   ```

5. 启动应用：
   ```bash
   # 方式1: 使用 Maven 插件运行
   cd app
   mvn spring-boot:run
   
   # 方式2: 运行 JAR 包
   java -jar app/target/app-1.0-SNAPSHOT.jar
   
   # 方式3: 指定配置文件
   java -jar app/target/app-1.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```

6. 验证部署：
   ```bash
   # 访问健康检查接口
   curl http://localhost:8080/actuator/health
   
   # 访问 API 文档 (Knife4j)
   浏览器打开: http://localhost:8080/doc.html
   ```

### 前端部署

1. 安装依赖：
   ```bash
   cd app-ui-vue
   # 使用 npm 安装
   npm install
   
   # 或使用国内镜像加速
   npm install --registry=https://registry.npmmirror.com
   ```

2. 配置后端地址：
   ```bash
   # 修改 .env.development 文件（开发环境）
   VUE_APP_BASE_API = 'http://localhost:8080'
   
   # 修改 .env.production 文件（生产环境）
   VUE_APP_BASE_API = 'https://your-production-api.com'
   ```

3. 开发环境运行：
   ```bash
   npm run dev
   # 默认访问地址: http://localhost:9528
   ```

4. 生产环境构建：
   ```bash
   # 构建生产环境
   npm run build:prod
   
   # 构建测试环境
   npm run build:stage
   
   # 构建后的文件在 dist/ 目录，部署到 Nginx 或其他 Web 服务器即可
   ```

5. Nginx 配置示例：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           root /path/to/dist;
           index index.html;
           try_files $uri $uri/ /index.html;
       }
       
       # API 代理
       location /api/ {
           proxy_pass http://localhost:8080/api/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

## 项目配置详解

Abstergo Solution 采用 Spring Boot 的配置管理方式，通过 application.yml
文件进行配置。项目支持多种配置方式，包括环境变量、命令行参数等，优先级从高到低为：命令行参数 > 环境变量 >
application-{profile}.yml > application.yml。

配置文件位于 `app/src/main/resources/` 目录，包括：

- `application.yml` - 主配置文件
- `application-db.yml` - 数据库配置
- `application-redis.yml` - Redis 配置
- `application-mq.yml` - RabbitMQ 配置
- `application-tbs.yml` - TBS 框架自定义配置
- `spy.properties` - P6Spy SQL 日志配置

### 核心配置项

#### 1. 服务器配置

```yaml
server:
  port: 8080  # 应用监听端口
```

#### 2. 数据库配置

```yaml
# application-db.yml
spring:
  datasource:
    # 数据源基本配置
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
    driver-class-name: com.p6spy.engine.spy.P6SpyDriver  # 使用 P6Spy 进行 SQL 日志记录
    # driver-class-name: com.mysql.cj.jdbc.Driver  # 不使用 P6Spy 时的配置

    # 连接池配置 (HikariCP)
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: HikariCP-Pool

  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: none  # 生产环境建议使用 none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

# MyBatis 配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.tbs.shared.pojo.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
```

#### 3. Redis配置

```yaml
# application-redis.yml
spring:
  redis:
    # 单机模式配置
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 30000ms

    # 连接池配置 (Lettuce)
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

tbs:
  redis:
    enable: true  # 是否启用 Redis 功能
    value-serializer: org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer

  redission:
    cache:
      cache-timeout: 3600000  # 缓存超时时间(毫秒)，1小时
      allow-null-values: false  # 是否允许空值
      transaction: false  # 是否开启redis事务
      max-idle-time: 1800  # redis缓存的key最大空闲时间，单位秒
      max-size: 2048  # redis缓存的key最大数量
      eviction-mode: LRU  # 缓存的key的清除模式 (LRU/LFU/SOFT/WEAK/NONE)
```

#### 4. RabbitMQ配置

```yaml
# application-mq.yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

    # 连接配置
    connection-timeout: 15000

    # 生产者配置
    publisher-confirm-type: correlated  # 开启发布确认
    publisher-returns: true  # 开启发布返回

    # 消费者配置
    listener:
      simple:
        acknowledge-mode: manual  # 手动确认模式
        prefetch: 1  # 预取数量
        retry:
          enabled: true
          max-attempts: 3

tbs:
  rabbitmq:
    exchange-name: tbs.broadcast.exchange
    queue-name: tbs.broadcast.queue
    routing-key: tbs.broadcast.routing.key
    node-id: node1  # 节点ID，用于分布式环境
    publisher-confirm: true  # 是否启用发布确认
```

### 模块详细配置

#### 1. Common模块配置

Common模块包含基础工具类和通用配置，是其他模块的基础。

```yaml
# application-tbs.yml
tbs:
  # UUID配置
  uuid:
    type: com.tbs.common.uuid.impl.SimpleUUID  # UUID生成策略 (SimpleUUID 或 SnowFlakeUUID)
    snow-flake-worker-id: 0  # 雪花算法工作节点ID (0-31)
    snow-flake-data-center-id: 0  # 雪花算法数据中心ID (0-31)
    except-length: 32  # 期望的UUID长度
  
  # 数据持有者配置
  data:
    data-holder-manager: com.tbs.common.data.impl.SpringManagedDataHolderManager  # 数据持有者管理器
    data-holder: com.tbs.common.data.impl.MapDataHolder  # 默认数据持有者实现
  
  # 代理配置
  delegate:
    delegate-target-provider: com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider  # 代理目标提供者
    delegate-flag-provider: com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider  # 代理标记提供者
```

#### 2. Web模块配置

Web模块提供认证授权、API网关、统一结果返回等功能。

```yaml
# application-tbs.yml
tbs:
  # Web基础配置
  web:
    enable-cors: true  # 是否启用跨域
    enable-csrf: false  # 是否启用CSRF保护
    enable-anonymous: true  # 是否启用匿名访问
    enable-form-login: false  # 是否启用表单登录
    enable-http-basic: false  # 是否启用HTTP Basic认证
    enable-logout: false  # 是否启用注销功能
    secured-paths:  # 需要认证才能访问的路径
      - "/api/**"
    permit-all-paths:  # 允许所有用户访问的路径
      - "/"
      - "/login"
      - "/public/**"
      - "/doc.html"  # API文档
      - "/swagger-resources/**"
      - "/v2/api-docs"
  
  # 认证配置
  auth:
    enable: true  # 是否启用认证
    token-source: HEADER  # Token来源(HEADER, PARAMETER, COOKIE)
    token-filed-name: "Authorization"  # Token字段名
    token-expire-time: 60  # Token过期时间(分钟)
    refresh-token-expire-time: 168  # 刷新Token过期时间(小时)
    token-picker-impl: com.tbs.web.security.impl.DefaultLoginTokenPicker  # Token提取器实现类
    authentication-center-impl: com.tbs.web.security.authentication.center.impl.SimpleAuthenticationCenter  # 认证中心实现类
  
  # 链路追踪配置
  trace:
    trace-id-object-generator: com.tbs.web.trace.impl.UuidTraceObjectGenerator  # Trace ID生成器
    trace-context: com.tbs.web.trace.impl.SpringManagedTraceContext  # 链路追踪上下文
    method-invoke-tree-provider: com.tbs.web.trace.impl.SimpleMethodInvokeTreeProvider  # 方法调用树提供者
    exclude-paths: # 排除追踪的路径
      - "/actuator/**"
      - "/static/**"

  # 结果返回配置
  result:
    result-builder-provier: com.tbs.web.result.impl.SimpleBuilderProvider  # 结果构建器提供者
```

#### 3. Cache模块配置

Cache模块提供带TTL功能的缓存管理。

```yaml
# application-tbs.yml
tbs:
  cache:
    cache-manager-impl: org.springframework.cache.concurrent.ConcurrentMapCacheManager  # 缓存管理器实现
    ttl-cache-decorator: com.tbs.cache.impl.DefaultTtlCacheDecorator  # TTL缓存装饰器实现
    recorder: com.tbs.cache.limit.impl.SimpleCacheOperationRecorder  # 缓存操作记录器实现
    manual-cache-handler: com.tbs.cache.manual.impl.MapCacheHandler  # 手动缓存实现
    cache-limiter: com.tbs.cache.limit.impl.SimpleCacheLimiter  # 缓存限制器实现

# 使用 Redisson 作为缓存实现时，需要在启动类上添加 @UseRedissonCacheMananger 注解
```

#### 4. Async模块配置

Async模块提供异步处理能力，包括线程池、定时任务等。

```yaml
# application-tbs.yml
tbs:
  async:
    executor:
      # 主执行器配置
      core-pool-size: 10  # 核心线程数
      max-pool-size: 20  # 最大线程数
      queue-capacity: 100  # 任务队列容量
      keep-alive-seconds: 60  # 线程空闲时间(秒)
      thread-name-prefix: "async-exec-"  # 线程名称前缀

  broadcast:
    event-dispatcher: com.tbs.broadcast.dispatcher.impl.SimpleEventDispatcher  # 事件分发器
    event-bus: com.tbs.broadcast.bus.impl.SimpleEventBus  # 事件总线

  lock:
    lock-provider-type: com.tbs.lock.impls.providers.JdkLockProvider  # 锁提供者类型

  schedule:
    schedule-execute-error-handle: com.tbs.schedule.impl.SimpleLogErrorHandle  # 定时任务错误处理
```

### 配置优先级和环境管理

项目支持多种环境配置：

1. **开发环境**：application-dev.yml
2. **测试环境**：application-test.yml
3. **生产环境**：application-prod.yml

通过 `spring.profiles.active` 参数激活对应环境配置：

```yaml
spring:
  profiles:
    active: dev  # 激活开发环境
```

命令行指定环境：

```bash
# 开发环境
java -jar app.jar --spring.profiles.active=dev

# 生产环境
java -jar app.jar --spring.profiles.active=prod
```

### 额外配置说明

#### Dubbo 配置

```yaml
# application.yml
dubbo:
  application:
    name: abstergo-solution
    version: 1.0.0

  registry:
    address: zookeeper://localhost:2181  # Zookeeper注册中心地址
    timeout: 5000

  protocol:
    name: dubbo
    port: 20880
    threads: 200

  consumer:
    timeout: 3000
    retries: 0

  provider:
    timeout: 3000
    retries: 0
```

#### Knife4j API文档配置

```yaml
knife4j:
  enable: true  # 启用Knife4j
  production: false  # 生产环境建议设为true，禁用API文档
  basic:
    enable: false  # 是否启用认证
    username: admin
    password: 123456
```

#### Actuator 监控配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env  # 暴露的监控端点
  endpoint:
    health:
      show-details: always  # 显示详细健康信息
```

#### 日志配置

```yaml
logging:
  level:
    root: INFO
    com.tbs: DEBUG  # TBS框架日志级别
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG  # SQL语句日志
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
```

## 扩展开发指南

### 自定义模块开发

Abstergo Solution 采用API与实现分离的设计理念，类似于system模块的结构，支持自定义模块开发。可以按照以下步骤进行：

#### 1. 创建模块结构

以创建一个名为 `custom-module` 的模块为例：

```
custom-module/
├── custom-module-api/          # API模块（定义接口）
│   ├── src/main/java/
│   │   └── com/tbs/custom/
│   │       ├── service/           # 服务接口
│   │       │   └── ICustomService.java
│   │       ├── dto/               # 数据传输对象
│   │       │   └── CustomDTO.java
│   │       └── entity/            # 实体类
│   │           └── CustomEntity.java
│   └── pom.xml
├── custom-module-impl/         # 实现模块（具体实现）
│   ├── src/main/java/
│   │   └── com/tbs/custom/
│   │       ├── config/            #自动配置
│   │       │   └── CustomModuleAutoConfiguration.java
│   │       ├── service/impl/      # 服务实现
│   │       │   └── CustomServiceImpl.java
│   │       └── mapper/            # MyBatis Mapper
│   │           └── CustomMapper.java
│   └── pom.xml
└── pom.xml                     # 父模块POM
```

#### 2. API模块设计

**服务接口定义** (ICustomService.java):

```java
package com.tbs.custom.service;

import com.tbs.custom.dto.CustomDTO;
import com.tbs.custom.entity.CustomEntity;

import java.util.List;

/**
 * 自定义服务接口
 */
public interface ICustomService {

    /**
     * 创建自定义实体
     */
    CustomEntity create(CustomDTO dto);

    /**
     * 更新自定义实体
     */
    CustomEntity update(Long id, CustomDTO dto);

    /**
     * 删除自定义实体
     */
    void delete(Long id);

    /**
     * 根据ID查询
     */
    CustomEntity findById(Long id);

    /**
     * 查询列表
     */
    List<CustomEntity> findAll();
}
```

**实体类定义** (CustomEntity.java):

```java
package com.tbs.custom.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "custom_entity")
public class CustomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
```

#### 3. 实现模块设计

**服务实现** (CustomServiceImpl.java):

```java
package com.tbs.custom.service.impl;

import com.tbs.custom.dto.CustomDTO;
import com.tbs.custom.entity.CustomEntity;
import com.tbs.custom.mapper.CustomMapper;
import com.tbs.custom.service.ICustomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomServiceImpl implements ICustomService {

    private final CustomMapper customMapper;

    @Override
    @Transactional
    public CustomEntity create(CustomDTO dto) {
        CustomEntity entity = new CustomEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        customMapper.insertSelective(entity);
        return entity;
    }

    @Override
    @Transactional
    public CustomEntity update(Long id, CustomDTO dto) {
        CustomEntity entity = customMapper.selectByPrimaryKey(id);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setUpdateTime(new Date());

        customMapper.updateByPrimaryKeySelective(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customMapper.deleteByPrimaryKey(id);
    }

    @Override
    public CustomEntity findById(Long id) {
        return customMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<CustomEntity> findAll() {
        return customMapper.selectAll();
    }
}
```

**自动配置类** (CustomModuleAutoConfiguration.java):

```java
package com.tbs.custom.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

@Configuration
@ComponentScan("com.tbs.custom")
@MapperScan("com.tbs.custom.mapper")
public class CustomModuleAutoConfiguration {
    // 自动配置类，启用组件扫描和Mapper扫描
}
```

创建 `src/main/resources/META-INF/spring.factories` 文件：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.tbs.custom.config.CustomModuleAutoConfiguration
```

#### 4. 模块集成

在app模块的 `pom.xml` 中添加对新模块的依赖：

```xml

<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>custom-module-impl</artifactId>
    <version>${project.version}</version>
</dependency>
```

在需要使用的地方注入相应的服务接口：

```java

@RestController
@RequestMapping("/api/custom")
public class CustomController {

    @Resource
    private ICustomService customService;

    @PostMapping
    public CustomEntity create(@RequestBody CustomDTO dto) {
        return customService.create(dto);
    }

    @GetMapping("/{id}")
    public CustomEntity findById(@PathVariable Long id) {
        return customService.findById(id);
    }
}
```

### 自定义组件集成

1. **自定义缓存实现**：
   - 实现ITtlCacheDecorator接口创建自定义缓存装饰器
   - 继承AbstractTtlAdaptCacheMananger创建自定义缓存管理器

2. **自定义异步任务**：
   - 实现IThreadPoolManager接口创建自定义线程池管理器
   - 使用@LongTermAsync注解标记需要异步执行的方法

3. **自定义认证逻辑**：
   - 实现IAuthenticationCenter接口创建自定义认证中心
   - 实现ITokenIssuanceCenter接口创建自定义Token签发中心

4. **自定义数据源**：
   - 实现IDataHolder接口创建自定义数据持有者
   - 实现IFrameworkMapper接口创建自定义数据访问接口

## 常见问题解答 (FAQ)

### 1. 如何解决数据库连接失败？

**问题**：启动应用时报错“Communications link failure”

**解决方案**：

- 检查MySQL服务是否启动
- 确认application-db.yml中的数据库地址、端口、用户名和密码正确
- 检查防火墙设置，确保3306端口可访问
- 如果使用Docker，确保容器网络配置正确

### 2. 如何启用/禁用某个模块？

**解决方案**：
在pom.xml中注释掉不需要的模块依赖，或者在配置文件中设置 `enable: false`。

例如禁用Redis：

```yaml
tbs:
  redis:
    enable: false
```

### 3. 如何修改默认端口？

**解决方案**：
在application.yml中修改：

```yaml
server:
  port: 8081  # 修改为想要的端口号
```

### 4. P6Spy如何配置？

**解决方案**：
修改 `app/src/main/resources/spy.properties` 文件：

```properties
# 使用Slf4j记录SQL
appender=com.p6spy.engine.spy.appender.Slf4JLogger
# 自定义SQL日志格式
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(currentTime) | SQL耗时: %(executionTime) ms | 连接信息: %(category)-%(connectionId) | 执行SQL: %(sql)
# 过滤日志
excludecategories=info,debug,result,resultset,batch
```

### 5. 如何集成Swagger/Knife4j？

**解决方案**：
项目已经集成Knife4j，直接访问 `http://localhost:8080/doc.html` 即可查看API文档。

如需添加认证，在application.yml中配置：

```yaml
knife4j:
  basic:
    enable: true
    username: admin
    password: 123456
```

### 6. 如何进行分布式部署？

**解决方案**：

1. 部署Zookeeper集群
2. 配置Dubbo注册中心地址
3. 为每个节点配置不同的node-id
4. 部署多个应用实例

```yaml
dubbo:
  registry:
    address: zookeeper://zk1:2181,zk2:2181,zk3:2181

tbs:
  rabbitmq:
    node-id: node1  # 每个节点使用不同ID
```

### 7. 如何优化启动速度？

**解决方案**：

1. 缩小组件扫描范围：

```java
@SpringBootApplication(scanBasePackages = "com.tbs.app")  // 只扫描需要的包
```

2. 关闭不需要的自动配置：

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class  // 示例：排除数据源自动配置
})
```

3. 使用懒加载：

```java

@Bean
@Lazy
public MyService myService() {
    return new MyServiceImpl();
}
```

### 8. 如何调试SQL语句？

**解决方案**：

1. 启用P6Spy（项目默认已启用）
2. 设置日志级别：

```yaml
logging:
  level:
    com.p6spy: DEBUG
    org.hibernate.SQL: DEBUG
```

3. 查看控制台输出的SQL语句

## 性能优化建议

### 1. 数据库优化

- 为常用查询字段添加索引
- 使用连接池，合理配置连接池参数
- 对于大表考虑分表分库
- 使用预编译语句防止SQL注入并提升性能

### 2. 缓存优化

- 合理设置缓存过期时间
- 使用多级缓存策略（本地缓存 + Redis）
- 对热点数据进行缓存预热
- 防止缓存雪崩、穿透和击穿

### 3. 并发优化

- 合理配置线程池参数
- 使用异步处理耗时操作
- 使用消息队列实现异步解耦
- 使用分布式锁控制并发访问

### 4. JVM调优

```bash
java -jar app.jar \
  -Xms2g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof
```

### 5. 网络优化

- 启用GZIP压缩
- 使用CDN加速静态资源
- 使用HTTP/2
- 合理配置连接超时

## 安全最佳实践

### 1. 身份认证

- 使用强密码策略
- 启用Token过期机制
- 实现刷新Token机制
- 防止暴力破解（限制登录尝试次数）

### 2. 数据安全

- 敏感数据加密存储
- 使用HTTPS传输数据
- 实现数据脱敏机制
- 定期备份数据

### 3. 接口安全

- 防止SQL注入
- 防止XSS攻击
- 防止CSRF攻击
- 实现请求频率限制

### 4. 依赖安全

- 定期更新依赖包
- 扫描并修复安全漏洞
- 使用依赖管理工具（如OWASP Dependency Check）

## 贡献指南

欢迎对Abstergo Solution项目进行贡献：

1. Fork项目到自己的仓库
2. 创建功能分支进行开发
3. 编写相关测试用例
4. 提交Pull Request

### 代码规范

1. 遵循项目现有的代码风格
2. 添加必要的注释说明
3. 编写单元测试确保代码质量
4. 遵循Git提交规范

## 社区和支持

- 提交Issue报告问题或建议新功能
- 查看Wiki获取更多文档和教程
- 参与讨论提供反馈

## 许可证

[MIT](LICENSE)
