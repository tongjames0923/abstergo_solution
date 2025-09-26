# App 模块

App 模块是整个应用程序的主入口模块，负责启动 Spring Boot 应用并整合各个功能模块。

## 目录结构

```
com.tbs.app
├── AppApplication.java     # Spring Boot 启动类
├── config                  # 应用配置类
├── controller              # 控制器类
├── form                    # 表单类
├── service                 # 业务服务类
└── vo                      # 视图对象类
```

## 核心组件说明

### 1. 启动类 (AppApplication.java)

[AppApplication](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L30)
是整个应用程序的启动类，包含 main 方法用于启动 Spring Boot 应用。

#### 主要注解说明

- [@SpringBootApplication](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  标识这是一个 Spring Boot 应用
- [@EnableTbsSqlUtils](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  启用 TBS SQL 工具
- [@MYSQL](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  指定使用 MySQL 数据库方言
- [@MapperScan](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  扫描 MyBatis Mapper 接口
- [@EntityScan](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  扫描 JPA 实体类
- [@EnableAspectJAutoProxy](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L17):
  启用 AspectJ 自动代理

### 2. 配置类 (config)

包含应用程序的各种配置类。

### 3. 控制器类 (controller)

包含处理 HTTP 请求的控制器类。

### 4. 表单类 (form)

包含用于接收前端请求数据的表单类。

### 5. 业务服务类 (service)

包含业务逻辑处理的服务类。

### 6. 视图对象类 (vo)

包含用于向前端返回数据的视图对象类。

## 自定义使用说明

### 1. 启动应用

要启动应用，可以运行 [AppApplication](file:///Users/abstergo/IdeaProjects/abstergo_solution/app/src/main/java/com/tbs/app/AppApplication.java#L17-L30)
类的 main 方法：

```bash
mvn spring-boot:run
```

或者打包后运行：

```bash
mvn clean package
java -jar target/app-1.0-SNAPSHOT.jar
```

### 2. 配置说明

在 `application.yml` 中配置相关参数：

```yaml
# 服务器配置
server:
  port: 8080

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis 配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.tbs.shared.pojo.entity

# 日志配置
logging:
  level:
    com.tbs: DEBUG
```

## 最佳实践

1. **模块化设计**：
    - 将不同功能的代码放在对应的包中
    - 保持 controller 层的简洁，将业务逻辑放在 service 层

2. **配置管理**：
    - 使用 application.yml 进行配置管理
    - 敏感信息如密码应使用环境变量或配置中心管理

3. **启动优化**：
    - 合理使用组件扫描范围，避免不必要的类加载
    - 根据实际需要启用相关功能模块

## 注意事项

1. **依赖管理**：
    - 确保 pom.xml 中包含了所有必要的依赖
    - 定期更新依赖版本以修复安全漏洞

2. **数据库连接**：
    - 确保数据库连接配置正确
    - 合理配置连接池参数以优化性能

3. **日志配置**：
    - 合理配置日志级别，避免生产环境输出过多调试信息
    - 定期清理日志文件，避免磁盘空间不足
