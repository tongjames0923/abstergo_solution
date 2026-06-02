# App 模块 - 后端Web应用运行包

## 项目概述

App模块是Abstergo解决方案的后端Web应用核心运行包，基于Spring Boot框架构建，提供完整的系统管理功能，包括用户管理、角色管理、公司管理等核心业务功能。该模块集成了项目中的所有核心模块，是整个系统的运行入口。

## 技术架构

### 核心框架
- **Spring Boot 2.7.14** - 应用主框架
- **Spring Data JPA** - 数据持久化
- **MyBatis** - ORM框架
- **Dubbo 3.3.0** - RPC框架（用于微服务通信）
- **Redisson 3.52.0** - 分布式缓存管理

### 数据库支持
- **MySQL 8.4** - 主数据库
- **Redis 6.2.19** - 缓存数据库
- **RabbitMQ 3.10** - 消息队列

### 安全与认证
- **JWT Token** - 身份认证
- **RBAC** - 基于角色的访问控制

## 项目结构

```
src/main/java/com/tbs/app/
├── AppApplication.java          # 应用启动类
├── config/                      # 配置类目录
│   └── AppConfig.java          # 应用配置
├── controller/                  # 控制器层
│   ├── privates/               # 私有接口（需要认证）
│   │   ├── AdminCompanyController.java    # 公司管理接口
│   │   ├── AdminRoleController.java       # 角色管理接口
│   │   ├── AdminSystemLogController.java  # 系统日志接口
│   │   ├── AdminUserController.java       # 用户管理接口
│   │   ├── PrivateCompanyController.java  # 私有公司接口
│   │   ├── PrivateTestController.java     # 私有测试接口
│   │   ├── PrivateUserController.java     # 私有用户接口
│   │   ├── RoleController.java            # 角色接口
│   │   └── ViewController.java            # 视图控制器
│   └── publics/                 # 公共接口（无需认证）
│       ├── PublicTestController.java      # 公共测试接口
│       └── PublicUserController.java      # 公共用户接口
├── form/                        # 表单对象
│   ├── DisableTokenForm.java    # Token禁用表单
│   └── LoginForm.java           # 登录表单
├── service/                     # 服务层
│   ├── AppExecutor.java         # 应用执行器
│   ├── Test2Service.java        # 测试服务2
│   └── TestService.java         # 测试服务
└── vo/                          # 值对象
    └── UserInfoVO.java          # 用户信息视图对象
```

## 配置说明

### 主配置文件 (application.yml)
- **应用名称**: app-service
- **国际化支持**: i18n/messages
- **Dubbo配置**: Zookeeper注册中心 (127.0.0.1:2181)
- **协议端口**: 20888

### 数据库配置 (application-db.yml)
- **数据库驱动**: P6Spy (SQL监控)
- **连接池**: HikariCP
- **连接配置**: 最小5个连接，最大20个连接
- **JPA配置**: 自动DDL更新，显示SQL
- **数据库连接**: jdbc:p6spy:mysql://localhost:3306/app_system

### 缓存配置 (application-redis.yml)
- **Redis主机**: localhost:6379
- **认证密码**: 123456

### 消息队列配置 (application-mq.yml)
- **RabbitMQ**: 127.0.0.1:5672 (admin/admin123)

## 快速开始

### 环境要求
- Java 11+
- Maven 3.6+
- MySQL 8.4+
- Redis 6.0+
- Zookeeper 3.6+
- RabbitMQ 3.10+

### 启动步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd abstergo_solution_p
   ```

2. **配置数据库**
   - 创建数据库: `app_system`
   - 导入数据库初始化脚本: `etc/sql_file/app_system.sql`
   - 修改 `application-db.yml` 中的数据库连接信息

3. **启动依赖服务**
   ```bash
   # 使用Docker Compose启动所有依赖服务
   cd etc/docker
   docker-compose up -d
   
   # 或者单独启动服务
   redis-server
   zkServer.sh start
   ```

4. **编译运行**
   ```bash
   # 编译项目
   mvn clean compile
   
   # 运行应用
   mvn spring-boot:run
   ```

### 接口访问

应用启动后，可以通过以下地址访问：
- **应用主页**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health
- **API文档**: http://localhost:8080/doc.html (Knife4j)

## 核心功能

### 用户管理模块
- 用户注册、登录、注销
- 用户信息管理
- 权限分配
- Token管理

### 角色管理模块
- 角色创建、修改、删除
- 权限配置
- 角色分配

### 公司管理模块
- 公司信息管理
- 组织架构维护
- 多租户支持

### 系统日志模块
- 操作日志记录
- 系统监控
- 异常追踪

## 监控与运维

### 健康检查
应用内置Spring Boot Actuator，提供以下端点：
- `/actuator/health` - 健康状态
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

### 日志配置
- 日志级别: INFO
- 日志文件: logs/app.log
- 支持SQL语句日志输出 (P6Spy)

## 安全特性

### 认证机制
- JWT Token认证
- 支持Token刷新
- Token失效管理

### 权限控制
- 基于角色的访问控制(RBAC)
- 接口级别权限验证
- 数据权限隔离

## 部署说明

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/app-1.0-SNAPSHOT.jar
```

### Docker部署
```dockerfile
FROM openjdk:11-jre-slim
COPY target/app-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 依赖模块

App模块集成了以下核心模块：
- **common** - 公共工具和配置
- **async** - 异步执行和事件广播
- **cache** - 缓存抽象和实现
- **redis** - Redis缓存实现
- **rabbitmq** - RabbitMQ消息队列
- **sql** - SQL工具和框架
- **web** - Web基础框架
- **system** - 系统管理模块
- **tree-struct** - 树形结构处理
- **script-engine** - 脚本引擎
- **open-api-starter** - API文档生成

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
