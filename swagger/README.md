# Swagger API 文档模块

Swagger 模块是基于 Springfox 和 Knife4j 构建的 API 文档生成和展示框架，提供了完整的 API 文档自动化生成和美观的在线文档界面。

## 目录结构

```
com.tbs.swagger
├── config                    # 配置类
│   └── SwaggerConfig.java   # Swagger 配置
└── property                  # 配置属性
    └── SwaggerProperty.java # Swagger 属性配置
```

## 核心组件说明

### 1. Swagger 配置 (SwaggerConfig.java)

[SwaggerConfig](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/config/SwaggerConfig.java#L25-L81) 是 Swagger 的核心配置类，负责配置 API 文档的生成和展示。

主要功能包括：

- 配置 Knife4j 的静态资源处理器
- 自动显示 API 文档访问地址
- 配置 Docket 实例，用于生成 API 文档

#### 主要方法说明

1. `swaggerConfigurer()` 方法：
   配置 Knife4j 所需的静态资源映射，包括 doc.html 页面和 webjars 资源。

2. `showDocumentUrl()` 方法：
   当配置项 `tbs.framework.swagger.show-document-url` 为 true 时，在应用启动时打印 API 文档访问地址。

3. `defaultApi2()` 方法：
   创建 Docket 实例，配置 API 文档的基本信息和扫描规则。

### 2. Swagger 属性配置 (SwaggerProperty.java)

[SwaggerProperty](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/property/SwaggerProperty.java#L16-L71) 类定义了 Swagger 的各种配置属性，可以通过 application.yml 进行配置。

#### 配置属性详解

| 属性名 | 默认值 | 说明 |
|--------|--------|------|
| title | "demo" | API 文档标题 |
| description | "description" | API 文档描述 |
| version | "1.0.1" | API 版本 |
| termsOfService | "termsOfService" | 服务条款 |
| license | "license" | 许可证 |
| licenseUrl | "licenseUrl" | 许可证 URL |
| contact | "contact" | 联系人 |
| contactUrl | "contactUrl" | 联系人 URL |
| basePackage | "/" | 扫描的包路径 |
| groupName | "group" | 分组名称 |
| email | "email" | 联系邮箱 |
| pathPattern | "" | 路径匹配模式 |
| documentationType | DocumentationType.OAS_30 | 文档类型 |
| showDocumentUrl | true | 是否在日志中显示文档 URL |

## 自定义使用说明

### 1. 基础配置

在 `application.yml` 中添加以下配置来启用和自定义 Swagger：

```yaml
tbs:
  swagger:
    # API 文档标题
    title: "我的 API 文档"
    
    # API 文档描述
    description: "这是一个基于 Spring Boot 构建的 RESTful API 文档"
    
    # API 版本
    version: "1.0.0"
    
    # 服务条款 URL
    terms-of-service: "http://localhost:8080/terms"
    
    # 许可证
    license: "Apache License 2.0"
    
    # 许可证 URL
    license-url: "http://www.apache.org/licenses/LICENSE-2.0"
    
    # 联系人
    contact: "开发团队"
    
    # 联系人 URL
    contact-url: "http://localhost:8080/contact"
    
    # 联系邮箱
    email: "developer@example.com"
    
    # 扫描的包路径（重要：需要根据实际项目结构调整）
    base-package: "com.tbs.app.controller"
    
    # 分组名称
    group-name: "默认分组"
    
    # 路径匹配模式（可选）
    path-pattern: "/api/.*"
    
    # 是否在日志中显示文档 URL
    show-document-url: true
```

### 2. 控制器注解使用

在控制器类和方法上使用 Swagger 注解来丰富 API 文档信息：

```java
@RestController
@RequestMapping("/api/users")
@Api(tags = "用户管理", description = "用户相关的增删改查操作")
public class UserController {
    
    @PostMapping
    @ApiOperation(value = "创建用户", notes = "根据用户信息创建新用户")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "请求参数错误"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<User> createUser(
        @ApiParam(name = "user", value = "用户信息", required = true)
        @RequestBody User user) {
        // 实现创建用户的逻辑
        return ResponseEntity.ok(createdUser);
    }
    
    @GetMapping("/{id}")
    @ApiOperation(value = "获取用户详情", notes = "根据用户ID获取用户详细信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // 实现获取用户详情的逻辑
        return ResponseEntity.ok(user);
    }
}
```

### 3. 实体类注解使用

在实体类和字段上使用 Swagger 注解来描述数据模型：

```java
@ApiModel(description = "用户信息")
public class User {
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "用户名", example = "张三", required = true)
    private String username;
    
    @ApiModelProperty(value = "用户邮箱", example = "zhangsan@example.com")
    private String email;
    
    @ApiModelProperty(value = "创建时间", example = "2023-01-01T00:00:00.000+08:00")
    private Date createTime;
    
    // getter 和 setter 方法
}
```

### 4. 多分组配置

当需要对 API 进行分组管理时，可以通过创建多个 Docket Bean 来实现：

```java
@Bean("userApi")
public Docket userApi() {
    return new Docket(DocumentationType.OAS_30)
        .apiInfo(apiInfo("用户管理 API", "用户相关接口"))
        .groupName("用户管理")
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.tbs.app.controller.user"))
        .paths(PathSelectors.any())
        .build();
}

@Bean("orderApi")
public Docket orderApi() {
    return new Docket(DocumentationType.OAS_30)
        .apiInfo(apiInfo("订单管理 API", "订单相关接口"))
        .groupName("订单管理")
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.tbs.app.controller.order"))
        .paths(PathSelectors.any())
        .build();
}

private ApiInfo apiInfo(String title, String description) {
    return new ApiInfoBuilder()
        .title(title)
        .description(description)
        .version("1.0.0")
        .build();
}
```

### 5. 安全配置

对于需要认证的 API，可以配置安全方案：

```java
@Bean
public Docket api() {
    return new Docket(DocumentationType.OAS_30)
        .apiInfo(apiInfo)
        .securitySchemes(Arrays.asList(apiKey()))
        .securityContexts(Arrays.asList(securityContext()))
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.tbs.app.controller"))
        .paths(PathSelectors.any())
        .build();
}

private ApiKey apiKey() {
    return new ApiKey("JWT", "Authorization", "header");
}

private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .build();
}

private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
}
```

## 访问 API 文档

应用启动后，可以通过以下地址访问 API 文档：

- Knife4j 美化界面：`http://服务器地址:端口/doc.html`
- 原始 Swagger 界面：`http://服务器地址:端口/swagger-ui/index.html`

例如，如果应用运行在本地的 8080 端口，则访问地址为：
- `http://localhost:8080/doc.html`
- `http://localhost:8080/swagger-ui/index.html`

当 [showDocumentUrl](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/property/SwaggerProperty.java#L69-L69) 配置为 true 时，应用启动后会在控制台输出访问地址。

## 最佳实践

1. **包扫描配置**：
   - 确保 [basePackage](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/property/SwaggerProperty.java#L51-L51) 配置正确，指向包含控制器类的包
   - 使用精确的包路径避免扫描不必要的类

2. **文档描述**：
   - 为每个控制器添加 @Api 注解描述其功能
   - 为每个接口方法添加 @ApiOperation 注解描述其用途
   - 为复杂参数和返回值添加详细的 @ApiModelProperty 描述

3. **版本管理**：
   - 随着 API 的演进及时更新 [version](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/property/SwaggerProperty.java#L27-L27) 配置
   - 对于重大变更，考虑使用分组管理不同版本的 API

4. **安全考虑**：
   - 在生产环境中考虑禁用或限制对 API 文档的访问
   - 对于内部使用的文档，可以配置 IP 白名单或身份验证

5. **性能优化**：
   - 避免在文档中暴露过多的内部实现细节
   - 合理使用 @ApiIgnore 注解忽略不需要展示的接口

## 依赖说明

该模块依赖以下组件：

1. **springfox**：
   - springfox-swagger2：Swagger 2 规范的 Spring Boot 集成
   - springfox-swagger-ui：Swagger UI 界面

2. **knife4j**：
   - knife4j-spring-boot-starter：为 Swagger 提供更美观的文档界面

3. **项目内部依赖**：
   - common：提供通用工具类
   - web：提供 Web 相关功能

## 注意事项

1. **包扫描路径**：
   - 确保 [basePackage](file:///Users/abstergo/IdeaProjects/abstergo_solution/swagger/src/main/java/com/tbs/swagger/property/SwaggerProperty.java#L51-L51) 配置正确，否则可能无法生成 API 文档
   
2. **生产环境**：
   - 建议在生产环境中禁用 API 文档或限制访问权限，避免暴露敏感信息
   
3. **版本兼容性**：
   - 注意 Spring Boot 版本与 Springfox 版本的兼容性
   - Knife4j 版本也需要与 Springfox 版本匹配

4. **文档更新**：
   - 修改接口后需要重启应用才能看到最新的文档
   - 建议在开发过程中保持文档与代码同步