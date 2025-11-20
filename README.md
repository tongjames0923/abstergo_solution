# Abstergo Solution - 企业级全栈解决方案

🌐 **基于Spring Boot + Vue.js的现代化企业级应用开发平台**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-2.6+-brightgreen.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📖 项目简介

Abstergo Solution是一个完整的企业级全栈解决方案，采用微服务架构设计，提供从后端API到前端UI的完整技术栈支持。项目基于Spring
Boot和Vue.js构建，集成了现代化的开发工具和最佳实践。

### 🎯 核心特性

- **🔧 模块化架构** - 清晰的模块划分，支持独立开发和部署
- **🛡️ 安全认证** - 完整的RBAC权限管理和JWT令牌认证
- **🚀 高性能** - 集成Redis缓存、异步处理等性能优化
- **📊 监控运维** - 完整的日志、监控和运维支持
- **🐳 容器化部署** - Docker Compose一键部署

## 🏗️ 项目架构

### 后端模块 (Spring Boot)

| 模块                   | 功能描述    | 技术栈                                       |
|----------------------|---------|-------------------------------------------|
| **app**              | 核心业务应用  | Spring Boot 2.x, Spring Data JPA, MyBatis |
| **system**           | 系统管理模块  | Spring Security, JWT, RBAC权限管理            |
| **cache**            | 缓存解决方案  | Spring Cache, Redis, TTL缓存                |
| **async**            | 异步编程框架  | Spring Async, 事件广播, 分布式锁                  |
| **web**              | Web应用框架 | Spring MVC, 统一响应, 请求追踪                    |
| **sql**              | SQL查询框架 | 注解驱动查询, 类型安全SQL构建                         |
| **tree-struct**      | 树结构管理   | 闭包表算法, 多叉树支持                              |
| **open-api-starter** | API文档   | Knife4j, OpenAPI 3.0                      |
| **redis-mod**        | Redis集成 | Redisson, 分布式锁, 缓存                        |
| **rabbitmq-mod**     | 消息队列    | Spring AMQP, 异步事件处理                       |
| **xxl-job-mod**      | 任务调度    | XXL-Job, 分布式任务调度                          |

### 前端模块 (Vue.js)

| 模块             | 功能描述   | 技术栈                                |
|----------------|--------|------------------------------------|
| **app-ui-vue** | 管理后台UI | Vue.js 2.6, Element UI, Vue Router |

### 基础设施模块

| 模块         | 功能描述  |
|------------|-------|
| **common** | 通用工具包 |
| **shared** | 共享组件库 |
| **etc**    | 部署配置  |

## 🚀 快速开始

### 环境要求

- **Java**: 11+
- **Node.js**: 14+
- **MySQL**: 5.7+
- **Redis**: 6.0+
- **Maven**: 3.6+

### 一键部署

```bash
# 启动基础设施服务
cd etc/docker
docker-compose up -d

# 初始化数据库
mysql -h localhost -P 3306 -u root -p < etc/sql_file/xxl-job/xxl_job_init.sql
```

### 后端启动

```bash
# 编译项目
mvn clean compile

# 启动核心应用
cd app
mvn spring-boot:run
```

### 前端启动

```bash
# 安装依赖
cd app-ui-vue
npm install

# 启动开发服务器
npm run serve
```

## 📋 核心功能详解

### 1. 系统管理 (system模块)

**用户认证与授权**

- 多方式认证（用户名密码、令牌刷新）
- 基于角色的权限控制（RBAC）
- JWT令牌安全管理
- 多租户数据隔离

**核心组件**

- `DefaultTokenSignCenter` - 令牌签发中心
- `DefaultUserInfoService` - 用户信息服务
- `SystemOperateInterceptor` - 操作日志拦截器

### 2. 缓存管理 (cache模块)

**高级缓存特性**

- TTL缓存策略
- 缓存监控统计
- 缓存限制管理
- 分布式缓存支持

**核心注解**

- `@Cacheable` - 缓存读取
- `@CacheEvict` - 缓存清除
- `@CachePut` - 缓存更新

### 3. 异步处理 (async模块)

**异步编程解决方案**

- 异步执行器管理
- 事件广播机制
- 分布式锁实现
- 线程池配置优化

**核心接口**

- `IAsyncExecutor` - 异步执行器
- `IEventBroadcaster` - 事件广播器
- `IDistributedLock` - 分布式锁

### 4. 树结构管理 (tree-struct模块)

**基于闭包表算法**

- 多叉树结构支持
- 层级关系查询
- 内存与持久化存储
- 事务完整性保证

**核心实现**

- `ClosureTreeServiceImpl` - 闭包表服务
- `MultiwayTree` - 多叉树实现
- `ITreeStore` - 树存储接口

### 5. API文档 (open-api-starter模块)

**开箱即用的API文档**

- OpenAPI 3.0规范支持
- Knife4j UI界面
- 多服务器配置
- 自动配置检测

**访问地址**

- Knife4j界面: http://localhost:8080/doc.html
- Swagger界面: http://localhost:8080/swagger-ui/index.html

## 🔧 配置说明

### 应用配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app_system
    username: root
    password: your_password

  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 123456

# 系统模块配置
tbs:
  system:
    enable: true
    enable-operate-log: true

  # 缓存配置
  cache:
    enable: true
    ttl: 3600
```

### 安全配置

```yaml
# JWT令牌配置
web:
  auth:
    token:
      expire-time: 3600
      refresh-expire-time: 86400
```

## 📊 监控与运维

### 健康检查

项目提供完整的健康检查端点：

```bash
# 应用健康状态
curl http://localhost:8080/actuator/health

# 缓存状态检查
curl http://localhost:8080/actuator/caches

# 数据库连接检查
curl http://localhost:8080/actuator/database
```

### 日志管理

- **应用日志**: 基于Logback的日志配置
- **操作审计**: 自动记录系统操作行为
- **性能监控**: 集成Micrometer指标收集

### 任务调度监控

XXL-Job任务调度中心：http://localhost:9898

- 任务执行监控
- 执行器管理
- 调度日志分析

## 🎯 开发指南

### 代码规范

- 遵循Google Java代码风格
- 使用Lombok减少样板代码
- 统一异常处理机制
- 完整的单元测试覆盖

### API开发规范

```java

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息")
    public ResponseEntity<UserVO> getUser(@PathVariable Long id) {
        // 业务逻辑实现
    }
}
```

### 数据库开发

**实体类定义**
```java
@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements IBaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;

    @AutoSetDate
    private Date createTime;
}
```

**查询构建**
```java
// 使用SQL框架构建类型安全查询
UserQuery query = new UserQuery().setNameLike("John").setMinAge(18);

List<User> users = QueryUtil.getEntityQueryWrapper(User.class).where(query).fetch();
```

## 🔍 故障排查

### 常见问题

1. **应用启动失败**
   ```bash
   # 检查依赖冲突
   mvn dependency:tree
   
   # 查看启动日志
   tail -f logs/application.log
   ```

2. **数据库连接问题**
   ```bash
   # 检查数据库服务状态
   systemctl status mysql
   
   # 验证连接信息
   mysql -h localhost -P 3306 -u root -p
   ```

3. **缓存失效**
   ```bash
   # 检查Redis服务
   redis-cli -h localhost -p 6379 -a 123456 ping
   
   # 清理缓存
   redis-cli -h localhost -p 6379 -a 123456 flushall
   ```

### 性能优化

1. **数据库优化**
    - 合理使用索引
    - 避免N+1查询问题
    - 使用连接池配置

2. **缓存策略**
    - 热点数据缓存
    - 缓存穿透防护
    - 缓存雪崩预防

3. **JVM调优**
   ```bash
   # 启动参数示例
   java -Xms512m -Xmx1024m -jar app.jar
   ```

## 🤝 贡献指南

欢迎为Abstergo Solution贡献代码！请遵循以下流程：

1. **Fork项目**
2. **创建功能分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **创建Pull Request**

### 开发规范

- 提交信息使用约定式提交格式
- 新增功能需要包含单元测试
- 更新相关文档和README
- 通过代码审查

## 📄 许可证

本项目基于MIT许可证开源。详情请参阅 [LICENSE](LICENSE) 文件。

## 📞 技术支持

- **邮箱支持**: [tongjames@live.com]

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element UI](https://element.eleme.io/)
- [Redis](https://redis.io/)
- [MySQL](https://www.mysql.com/)
- [Docker](https://www.docker.com/)

---

**Abstergo Solution** - 让企业级应用开发更简单、更高效！ 🚀

