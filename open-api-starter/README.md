# OpenAPI Starter 模块

OpenAPI Starter 模块是基于 Knife4j 的 Spring Boot Starter，为 Abstergo Solution 提供开箱即用的 API 文档生成和管理功能。该模块集成了 Springdoc OpenAPI 和 Knife4j UI，构建了企业级的 API 文档解决方案。

## 核心特性

### 🚀 开箱即用
- **自动配置**：基于 Spring Boot 自动配置 OpenAPI 和 Knife4j
- **条件化启用**：支持通过配置灵活启用或禁用 API 文档功能
- **智能检测**：自动检测服务器端口和上下文路径

### 📚 完整文档支持
- **OpenAPI 3.0**：支持最新的 OpenAPI 3.0 规范
- **Knife4j UI**：提供美观易用的 API 文档界面
- **多服务器支持**：支持配置多个服务器地址
- **许可证信息**：完整的许可证和外部文档链接

### 🔧 灵活配置
- **自定义标题**：支持自定义 API 文档标题和描述
- **版本管理**：支持 API 版本信息配置
- **服务器配置**：支持多环境服务器地址配置
- **URL 显示**：支持自动显示访问地址

### 🌐 多访问方式
- **Knife4j UI**：通过 `/doc.html` 访问增强版文档界面
- **Swagger UI**：通过 `/swagger-ui/index.html` 访问标准界面
- **OpenAPI JSON**：通过 `/v3/api-docs` 获取原始 OpenAPI 规范

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加 OpenAPI Starter 模块依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>open-api-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

在 `application.yml` 中配置 OpenAPI 相关信息：

```yaml
# OpenAPI 配置
tbs:
  knife4j:
    enable: true                    # 启用 Knife4j 功能
    show-url: true                  # 显示访问地址
    title: "我的应用 API 文档"       # 文档标题
    description: "我的应用 API 接口文档" # 文档描述
    version: "v1.0.0"              # API 版本
    license: "Apache 2.0"          # 许可证名称
    license-url: "http://springdoc.org" # 许可证 URL
    external-docs-description: "项目文档" # 外部文档描述
    external-docs-url: "https://github.com/your-project/docs" # 外部文档 URL
    servers:                        # 服务器地址列表
      - "http://localhost:8080"
      - "https://api.example.com"

# Spring Boot 基础配置
server:
  port: 8080                       # 服务器端口
  servlet:
    context-path: /api             # 上下文路径（可选）
```

### 3. 在控制器中使用 OpenAPI 注解

在 Spring Boot 控制器中使用 OpenAPI 注解来增强 API 文档：

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的 API 接口")
public class UserController {
    
    @GetMapping("/{id}")
    @Operation(
        summary = "获取用户信息", 
        description = "根据用户ID获取用户详细信息"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "成功获取用户信息",
        content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = UserVO.class))
    )
    @ApiResponse(responseCode = "404", description = "用户不存在")
    public ResponseEntity<UserVO> getUser(
        @Parameter(description = "用户ID", required = true) 
        @PathVariable Long id
    ) {
        // 业务逻辑实现
        UserVO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public ResponseEntity<UserVO> createUser(
        @Parameter(description = "用户信息", required = true)
        @RequestBody @Valid CreateUserRequest request
    ) {
        // 业务逻辑实现
        UserVO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

### 4. 访问 API 文档

应用启动后，可以通过以下地址访问 API 文档：

- **Knife4j 增强界面**：http://localhost:8080/doc.html
- **Swagger 标准界面**：http://localhost:8080/swagger-ui/index.html
- **OpenAPI 规范**：http://localhost:8080/v3/api-docs

## 核心组件详解

### Knife4jConfig - 核心配置类

<mcsymbol name="Knife4jConfig" filename="Knife4jConfig.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/open-api-starter/src/main/java/com/tbs/knife4j/Knife4jConfig.java" startline="22" type="class">Knife4jConfig</mcsymbol> 是模块的核心配置类，负责创建和管理 OpenAPI 配置：

**主要功能：**
- **条件化 Bean 创建**：基于 `tbs.knife4j.enable` 属性控制是否启用
- **OpenAPI 配置**：创建 <mcsymbol name="OpenAPI" filename="Knife4jConfig.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/open-api-starter/src/main/java/com/tbs/knife4j/Knife4jConfig.java" startline="36" type="class">OpenAPI</mcsymbol> Bean，配置文档信息
- **服务器配置**：支持多服务器地址配置
- **访问地址显示**：自动检测并显示文档访问地址

**配置示例：**
```java
@Bean
@ConditionalOnProperty(name = "tbs.knife4j.enable", havingValue = "true")
public OpenAPI tbsOpenAPI() {
    OpenAPI openAPI = new OpenAPI().info(
        new Info().title(property.getTitle())
                  .description(property.getDescription())
                  .version(property.getVersion())
                  .license(new License().name(property.getLicense())
                                        .url(property.getLicenseUrl())))
        .externalDocs(new ExternalDocumentation()
            .description(property.getExternalDocsDescription())
            .url(property.getExternalDocsUrl()));
    
    // 配置服务器地址
    if (CollUtil.isNotEmpty(property.getServers())) {
        List<Server> servers = new ArrayList<>();
        for (String serverUrl : property.getServers()) {
            servers.add(new Server().url(serverUrl));
        }
        openAPI.servers(servers);
    }
    
    return openAPI;
}
```

### Knife4jProperty - 配置属性类

<mcsymbol name="Knife4jProperty" filename="Knife4jProperty.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/open-api-starter/src/main/java/com/tbs/knife4j/Knife4jProperty.java" startline="10" type="class">Knife4jProperty</mcsymbol> 封装了 Knife4j 相关配置属性：

**配置属性说明：**
- `enable`: 是否启用 Knife4j 功能（默认：true）
- `showUrl`: 是否显示访问地址（默认：true）
- `title`: 文档标题（默认："TBS 后端服务 API 文档"）
- `description`: 文档描述（默认："TBS 后端服务 API 文档"）
- `version`: API 版本（默认："v1.0.0"）
- `license`: 许可证名称（默认："Apache 2.0"）
- `licenseUrl`: 许可证 URL（默认："http://springdoc.org"）
- `externalDocsDescription`: 外部文档描述（默认："TBS 解决方案文档"）
- `externalDocsUrl`: 外部文档 URL（默认："https://github.com/abstergo/abstergo-solution"）
- `servers`: 服务器地址列表

## 高级用法

### 1. 多环境配置

根据不同环境配置不同的 API 文档设置：

```yaml
# application-dev.yml (开发环境)
tbs:
  knife4j:
    enable: true
    show-url: true
    title: "开发环境 - 我的应用 API"
    servers:
      - "http://localhost:8080"
      - "http://dev-api.example.com"

# application-prod.yml (生产环境)
tbs:
  knife4j:
    enable: false                    # 生产环境禁用文档
    show-url: false
```

### 2. 自定义 OpenAPI 配置

通过继承或自定义配置类来扩展 OpenAPI 功能：

```java
@Configuration
public class CustomOpenApiConfig {
    
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // 添加自定义安全配置
            openApi.getComponents()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
            
            // 添加全局安全要求
            openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            
            // 添加自定义扩展信息
            openApi.getInfo().addExtension("x-api-version", "1.0.0");
        };
    }
}
```

### 3. 分组 API 文档

为不同的 API 模块创建分组文档：

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/public/**")
        .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/admin/**")
        .addOpenApiCustomizer(openApi -> 
            openApi.info(new Info().title("管理接口")
                                   .description("系统管理相关接口")))
        .build();
}
```

### 4. 响应示例配置

为 API 响应配置示例数据：

```java
@Schema(description = "用户信息响应")
@Data
public class UserResponse {
    
    @Schema(description = "用户ID", example = "123")
    private Long id;
    
    @Schema(description = "用户名", example = "张三")
    private String username;
    
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
    
    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;
}
```

## 最佳实践

### 1. 文档质量
- **使用有意义的描述**：为每个 API 接口提供清晰的描述
- **提供示例数据**：为请求和响应字段提供示例值
- **标记必填字段**：明确标识必填参数
- **错误码说明**：为不同的 HTTP 状态码提供说明

### 2. 安全配置
- **生产环境禁用**：生产环境建议禁用 API 文档
- **访问控制**：为文档界面配置访问权限
- **敏感信息过滤**：避免在文档中暴露敏感信息

### 3. 版本管理
- **API 版本标识**：在文档标题中包含版本信息
- **变更记录**：记录 API 接口的变更历史
- **向后兼容**：确保 API 变更的向后兼容性

### 4. 性能优化
- **按需启用**：根据环境需要启用文档功能
- **缓存配置**：合理配置文档生成缓存
- **资源清理**：定期清理无用的文档资源

## 故障排除

### 常见问题

**1. 文档无法访问**
```
检查 tbs.knife4j.enable 配置是否为 true
确认服务器端口和上下文路径配置正确
检查防火墙和网络连接
```

**2. 注解不生效**
```
确认依赖是否正确引入
检查注解使用是否正确
查看 Spring Boot 启动日志是否有相关错误
```

**3. 界面显示异常**
```
清除浏览器缓存
检查 Knife4j 版本兼容性
查看浏览器控制台错误信息
```

### 调试技巧

1. **启用调试日志**：
```yaml
logging:
  level:
    com.tbs.knife4j: DEBUG
    org.springdoc: DEBUG
```

2. **检查依赖冲突**：
```bash
mvn dependency:tree
```

3. **验证配置生效**：
```bash
curl http://localhost:8080/v3/api-docs
```

## 版本信息

- **Spring Boot**: 2.x+
- **Springdoc OpenAPI**: 1.6.x+
- **Knife4j**: 4.4.0+
- **Java**: 11+

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进 OpenAPI Starter 模块。在提交代码前请确保：

1. 代码符合项目的编码规范
2. 添加相应的单元测试
3. 更新相关文档
4. 通过所有现有测试

## 许可证

本项目基于 Abstergo Solution 许可证。详情请参阅项目根目录的 LICENSE 文件。

## 相关链接

- [Springdoc OpenAPI 官方文档](https://springdoc.org/)
- [Knife4j 官方文档](https://doc.xiaominfo.com/)
- [OpenAPI 规范](https://swagger.io/specification/)
