# TBS Web 框架

TBS Web 是一个基于 Spring Boot 和 Spring Security 的企业级 Web 应用框架，提供完整的认证授权、统一响应格式、请求追踪和 API 拦截等核心功能。

## 功能特性

### 🔐 安全认证
- **多种认证方式支持**: 
  - **用户名密码认证**: 支持传统的用户名密码登录方式
  - **Token认证**: 支持基于Token的认证机制（不限于JWT）
  - **刷新令牌认证**: 支持使用刷新令牌重新获取访问令牌
- **多来源Token提取**: 支持从Header、Cookie、URL参数、请求体等多种方式提取Token
- **灵活认证配置**: 可根据业务需求配置认证开关、Token有效期等参数
- **认证状态管理**: 提供完整的认证状态变更通知机制

### 📊 统一响应
- **标准化响应格式**: 提供统一的 AjaxResult 响应对象
- **自动包装**: 通过 AOP 自动包装控制器返回值
- **异常处理**: 统一的异常处理和错误码定义
- **自定义注解**: 支持 `@Wrapped` 和 `@NoWrapped` 注解控制响应包装

### 🔍 请求追踪
- **方法调用追踪**: 自动记录方法调用链路
- **Trace ID 生成**: 支持 UUID 等 Trace ID 生成策略
- **调用树构建**: 提供方法调用树的构建和追踪
- **性能监控**: 集成方法执行时间监控

### 🚪 API 网关
- **路径拦截**: 基于 AntPath 的路径匹配和拦截
- **自定义拦截器**: 支持自定义 API 调用拦截器
- **网关中心**: 提供统一的网关管理和路由控制

## 快速开始

### 依赖配置

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>web</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 基础配置

#### 1. 启用认证模式

在 `application.yml` 中配置：

```yaml
tbs:
  web:
    auth:
      enable: true  # 启用认证
    cors:
      enable: true  # 启用跨域支持
    csrf:
      enable: false # 禁用 CSRF（推荐用于 API 服务）
```

#### 2. 认证配置

在 `application.yml` 中配置认证参数：

```yaml
tbs:
  web:
    auth:
      enable: true                    # 是否启用认证
      token-source: HEADER           # Token来源：HEADER/COOKIE/PARAM/BODY
      token-filed-name: Authorization # Token字段名称
      token-expire-time: 3600         # Token有效期(秒)
      refresh-token-expire-time: 168  # 刷新Token有效期(小时)
      # 可配置认证中心、Token颁发中心等实现类
```

#### 3. 创建基础控制器

```java
@RestController
public class UserController extends BaseController {
    
    @Override
    public String getBusinessName() {
        return "用户管理";
    }
    
    @GetMapping("/users")
    public AjaxResult getUsers() {
        // 获取当前用户信息
        UserDetails currentUser = getCurrentUser();
        
        // 执行业务逻辑
        List<User> users = userService.findAll();
        
        return successResult(users);
    }
}
```

## 核心组件详解

### BaseController 基础控制器

所有控制器都应继承 `BaseController`，它提供：

- **当前用户获取**: `getCurrentUser()` 方法
- **令牌模型获取**: `getCurrentTokenModel()` 方法  
- **统一响应方法**: `successResult()` 和 `errorResult()`
- **Trace 对象**: 自动注入的追踪对象

### AjaxResult 统一响应

标准化的响应格式，包含：

```java
{
    "code": 200,           // 状态码
    "message": "操作成功",  // 消息
    "data": {}             // 业务数据
}
```

**预定义状态码**:
- `200`: 成功
- `401`: 未授权
- `403`: 无权限  
- `405`: 需要重新登录
- `468`: Token 刷新失败
- `500`: 系统异常

### 认证与授权

#### 认证管理器 (IAuthManager)

提供完整的认证管理功能：

```java
// 检查用户登录状态
boolean isLoggedIn = authManager.isUserLoggedIn();

// 获取当前用户
UserDetails user = authManager.getCurrentUser();

// 获取令牌模型
ITokenModel tokenModel = authManager.getCurrentTokenModel();
```

#### 认证方式

框架支持多种认证方式：

1. **用户名密码认证** (`DefaultUserPasswordAccessTokenProvider`)
   - 支持传统的用户名密码登录
   - 可配置密码加密方式

2. **刷新令牌认证** (`DefaultRefreshAccessTokenProvider`)
   - 使用刷新令牌重新获取访问令牌
   - 支持令牌自动续期

3. **自定义认证方式**
   - 可扩展实现支持短信验证码、第三方登录等

#### 令牌处理

- **令牌提取器 (ITokenPicker)**: 从请求中提取令牌，支持 Header、Cookie、URL参数、请求体
- **令牌验证器 (IRefreshTokenValidator)**: 验证令牌有效性
- **令牌刷新服务 (IRefreshTokenService)**: 处理令牌刷新逻辑

### 请求追踪

#### 启用追踪

```java
@Configuration
@EnableTrace  // 启用方法追踪
public class TraceConfig {
}
```

#### 追踪方法

```java
@Service
public class UserService {
    
    @Traced  // 标记需要追踪的方法
    public User findById(Long id) {
        // 方法执行会被自动追踪
        return userRepository.findById(id);
    }
}
```

## 高级配置

### 自定义认证提供者

```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) {
        // 自定义认证逻辑
        return authentication;
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

### 自定义 API 拦截器

```java
@Component
public class CustomApiInterceptor implements IApiInvokeInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) {
        // 前置处理逻辑
        return true; // 返回 true 继续执行，false 中断
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // 后置处理逻辑
    }
}
```

### 安全配置自定义

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    @ConditionalOnProperty(prefix = "tbs.web.auth", name = "enable", havingValue = "true")
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .build();
    }
}
```

## 配置属性

### WebProperty 配置

```yaml
tbs:
  web:
    enable-cors: true           # 启用跨域
    enable-csrf: false         # 启用 CSRF
    enable-http-basic: false   # 启用 HTTP Basic
    enable-form-login: false   # 启用表单登录
    enable-logout: false       # 启用登出
    permit-all-paths:          # 允许匿名访问的路径
      - "/api/public/**"
    secured-paths:             # 需要认证的路径
      - "/api/secure/**"
```

### 认证属性配置

```yaml
tbs:
  web:
    auth:
      enable: true
      property:
        token-header: "Authorization"  # 令牌请求头
        token-prefix: "Bearer "        # 令牌前缀
```

## 最佳实践

### 1. 控制器设计

```java
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {
    
    @Override
    public String getBusinessName() {
        return "用户管理";
    }
    
    @GetMapping
    @Wrapped  // 明确标记需要包装响应
    public List<User> getUsers() {
        return userService.findAll();
    }
    
    @PostMapping
    public AjaxResult createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.create(request);
        return successResult(user);
    }
}
```

### 2. 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public AjaxResult handleBusinessException(BusinessException e) {
        return AjaxResult.error(e.getMessage());
    }
    
    @ExceptionHandler(NoLoginException.class)
    public AjaxResult handleNoLoginException(NoLoginException e) {
        return AjaxResult.relogin(e.getMessage());
    }
}
```

### 3. 安全最佳实践

- 使用 HTTPS 传输敏感数据
- 定期轮换 JWT 密钥
- 设置合理的令牌过期时间
- 实现令牌黑名单机制
- 记录安全审计日志

## 横向拓展性

TBS Web 框架采用高度模块化和接口化的设计，提供了强大的横向拓展能力，支持业务方根据具体需求灵活定制和扩展功能。

### 🔧 可扩展的认证体系

#### 1. 认证中心扩展
框架提供 `IAuthenticationCenter` 接口，支持自定义认证逻辑：

```java
@Component
public class CustomAuthenticationCenter implements IAuthenticationCenter {
    
    @Override
    public ITokenAuthentication createAuthenticationByTokenModel(ITokenModel model) {
        // 自定义Token验证逻辑
        if (isValidToken(model.getToken())) {
            return new DefaultTokenAuthentication(model, getUserDetails(model));
        }
        throw new InvalidTokenException("Token无效");
    }
    
    @Override
    public ITokenAuthentication authenticate(ITokenAuthentication authentication) {
        // 自定义认证逻辑
        return authentication;
    }
}
```

#### 2. Token签发中心扩展
通过 `ITokenIssuanceCenter` 接口支持自定义Token签发策略：

```java
@Component
public class CustomTokenIssuanceCenter implements ITokenIssuanceCenter {
    
    @Override
    public ITokenModel issueToken(TokenRequestForm form) {
        // 自定义Token生成逻辑
        SimpleTokenModel token = new SimpleTokenModel();
        token.setToken(generateCustomToken(form));
        token.setExpireTime(calculateExpireTime(form));
        return token;
    }
    
    @Override
    public ITokenModel getTokenByToken(String token) {
        // 自定义Token查询逻辑
        return tokenRepository.findByToken(token);
    }
}
```

#### 3. 认证提供者扩展
支持多种认证方式的扩展：

```java
// 短信验证码认证
@Component
public class SmsAccessTokenProvider extends AbstractAccessTokenProvider<ISmsCodeForm> {
    
    @Override
    protected UserDetails pullUserDetails(ISmsCodeForm form) {
        // 验证短信验证码并获取用户信息
        return userService.loadUserByPhone(form.getPhone());
    }
}

// 第三方登录认证
@Component
public class OAuthAccessTokenProvider extends AbstractAccessTokenProvider<IOAuthForm> {
    
    @Override
    protected UserDetails pullUserDetails(IOAuthForm form) {
        // 第三方认证逻辑
        return oauthService.authenticate(form.getProvider(), form.getCode());
    }
}
```

### 🔌 可插拔的组件架构

#### 1. Token提取器扩展
支持从不同来源提取Token：

```java
@Component
public class CustomTokenPicker implements ITokenPicker {
    
    @Override
    public String pick(HttpServletRequest request) {
        // 自定义Token提取逻辑
        return extractTokenFromCustomHeader(request);
    }
}
```

#### 2. 用户信息服务扩展
支持自定义用户信息加载逻辑：

```java
@Component
public class CustomUserInfoService implements IUserInfoService {
    
    @Override
    public UserDetails loadUserInfoByUniqueKey(String uniqueKey) {
        // 自定义用户信息加载逻辑
        return userRepository.findByUsernameOrEmailOrPhone(uniqueKey);
    }
}
```

#### 3. 密码加密扩展
支持自定义密码加密算法：

```java
@Component
public class CustomPasswordEncrypt implements IPasswordEncrypt {
    
    @Override
    public String encrypt(String rawPassword) {
        // 自定义密码加密算法
        return customEncryptAlgorithm.encrypt(rawPassword);
    }
}
```

### 🎯 配置驱动的扩展

#### 1. 动态配置切换
通过配置文件灵活切换实现类：

```yaml
tbs:
  web:
    auth:
      # 认证中心实现类
      authentication-center-impl: com.example.CustomAuthenticationCenter
      # Token签发中心实现类
      token-issuance-center-impl: com.example.CustomTokenIssuanceCenter
      # Token提取器实现类
      token-picker-impl: com.example.CustomTokenPicker
```

#### 2. 条件化配置
支持基于条件的组件启用：

```java
@Configuration
@ConditionalOnProperty(name = "tbs.web.auth.enable", havingValue = "true")
public class CustomAuthConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public IAuthenticationCenter authenticationCenter() {
        return new CustomAuthenticationCenter();
    }
}
```

### 🚀 扩展场景示例

#### 1. 多租户认证扩展
```java
@Component
public class MultiTenantAuthenticationCenter extends AbstractAuthenticationCenter {
    
    @Override
    protected ITokenAuthentication createAuth(ITokenModel model) {
        // 根据租户ID选择不同的认证策略
        String tenantId = extractTenantId(model);
        TenantAuthStrategy strategy = tenantService.getAuthStrategy(tenantId);
        return strategy.authenticate(model);
    }
}
```

#### 2. 分布式Token管理
```java
@Component
public class DistributedTokenIssuanceCenter implements ITokenIssuanceCenter {
    
    @Override
    public ITokenModel issueToken(TokenRequestForm form) {
        // 使用Redis集群存储Token
        String token = redisTemplate.opsForValue()
            .setIfAbsent(generateTokenKey(), form, Duration.ofHours(1));
        return createTokenModel(token, form);
    }
}
```

#### 3. 生物特征认证
```java
@Component
public class BiometricAccessTokenProvider extends AbstractAccessTokenProvider<IBiometricForm> {
    
    @Override
    protected UserDetails pullUserDetails(IBiometricForm form) {
        // 生物特征验证逻辑
        return biometricService.verifyAndGetUser(form.getBiometricData());
    }
}
```

### 📋 扩展最佳实践

1. **遵循接口契约**：自定义实现必须严格遵循接口定义的方法契约
2. **异常处理**：合理处理各种异常情况，提供清晰的错误信息
3. **性能考虑**：在扩展时考虑性能影响，合理使用缓存和异步处理
4. **安全审计**：记录关键操作日志，便于安全审计和问题排查
5. **向后兼容**：确保扩展功能与现有系统的兼容性

通过这种高度可扩展的设计，TBS Web框架能够适应各种复杂的业务场景，为企业的数字化转型提供强有力的技术支撑。

## 故障排除

### 常见问题

1. **认证不生效**
   - 检查 `tbs.web.auth.enable` 配置是否为 true
   - 确认认证提供者已正确配置

2. **跨域问题**
   - 检查 `tbs.web.cors.enable` 配置
   - 确认 CORS 配置源是否正确注册

3. **响应未包装**
   - 检查控制器是否继承 BaseController
   - 确认方法是否被 ResponseAjaxAspect 拦截

## 版本信息

- **Spring Boot**: 2.x+
- **Java**: 11+
- **Spring Security**: 5.x+

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个框架。

## 许可证

本项目基于 TBS 内部许可证。
