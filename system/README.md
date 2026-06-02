# System 模块

System模块是Abstergo解决方案的核心系统管理组件，提供完整的用户认证、权限管理、系统配置等企业级系统管理功能。该模块采用Maven多模块架构，包含system-api和default-system-impl两个子模块，为整个系统提供统一的安全认证和系统管理能力。

## 模块架构

### system-api模块
**系统管理接口定义**
- **用户管理接口**: ISystemUserService、IUserRemoteService等
- **角色权限接口**: ISystemRoleService、IUserRoleRelationService等
- **公司组织接口**: ISystemCompanyService、ICompanyUserRelationService等
- **令牌管理接口**: ISystemTokenService、IRefreshTokenService等
- **系统日志接口**: ISystemLogService、SystemOperateInterceptor等

### default-system-impl模块
**默认实现与配置**
- **用户服务实现**: DefaultUserInfoService提供用户信息加载
- **令牌签发中心**: DefaultTokenSignCenter实现JWT令牌管理
- **认证提供者**: 用户名密码认证、刷新令牌认证
- **系统配置**: DefaultAppConfig自动配置Bean
- **业务配置**: BuisnessBeanConfig业务Bean配置

## 核心功能

### 1. 用户认证与授权
- **多方式认证**: 支持用户名密码、令牌刷新等多种认证方式
- **权限管理**: 基于角色的权限控制(RBAC)
- **用户状态管理**: 支持账户锁定、禁用、过期等状态
- **公司组织**: 支持多公司用户关系管理

### 2. 令牌安全管理
- **JWT令牌**: 基于数据库的令牌签发和管理
- **令牌缓存**: 使用Redis缓存提升令牌查询性能
- **令牌状态**: 支持激活、过期、禁用等状态管理
- **会话管理**: 基于Session ID的令牌关联管理

### 3. 系统日志与审计
- **操作日志**: 自动记录系统操作行为
- **审计拦截**: SystemOperateInterceptor实现操作审计
- **日志存储**: 支持数据库和事件两种日志存储方式

### 4. 远程服务支持
- **用户查询**: 提供远程用户信息查询接口
- **服务调用**: 支持跨服务用户信息获取
- **安全传输**: 基于IRemoteRequestForm的安全请求格式

## 快速开始

### 依赖配置
在pom.xml中添加依赖：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>system</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 启用系统模块
在application.yml中启用系统模块：

```yaml
tbs:
  system:
    enable: true
    enable-operate-log: true
```

### 使用示例

#### 1. 用户认证配置
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated())
            .formLogin(withDefaults());
        return http.build();
    }
}
```

#### 2. 自定义用户服务
```java
@Service
public class CustomUserService implements ISystemUserService {
    
    @Override
    public SystemUserEntity queryById(Long id) {
        // 自定义用户查询逻辑
        return userRepository.findById(id);
    }
    
    @Override
    public List<SystemUserEntity> queryByUserName(String username) {
        // 自定义用户名查询逻辑
        return userRepository.findByUsername(username);
    }
}
```

#### 3. 令牌管理使用
```java
@RestController
public class AuthController {
    
    @Resource
    private DefaultTokenSignCenter tokenSignCenter;
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        // 用户认证
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        
        // 签发令牌
        ITokenModel token = tokenSignCenter.createToken(userDetails, TokenTypeEnum.ACCESS);
        
        return ResponseEntity.ok(new TokenResponse(token.getToken()));
    }
}
```

## 配置说明

### 系统属性配置
```yaml
tbs:
  system:
    enable: true                    # 启用系统模块
    enable-operate-log: true       # 启用操作日志
    system-user-name-password-validator-impl: # 自定义验证器
```

### 令牌配置
```yaml
web:
  auth:
    token:
      expire-time: 3600           # 令牌过期时间(秒)
      refresh-expire-time: 86400  # 刷新令牌过期时间
```

### 缓存配置
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000        # 缓存过期时间
```

## 数据模型关系

### 用户-角色-公司关系
- **用户管理**: SystemUserEntity管理用户基本信息
- **角色权限**: SystemRoleEntity定义角色权限
- **公司组织**: SystemCompanyEntity管理公司信息
- **关系映射**: 通过中间表维护多对多关系

### 令牌管理模型
- **令牌实体**: SystemTokenEntity存储令牌信息
- **令牌状态**: 支持激活、过期、禁用状态
- **令牌类型**: 区分访问令牌和刷新令牌

## 最佳实践

### 1. 安全认证设计
- 使用强密码策略和密码加密
- 实现令牌自动刷新机制
- 配置合理的令牌过期时间
- 启用操作日志审计

### 2. 权限管理策略
- 基于角色的细粒度权限控制
- 实现动态权限加载机制
- 支持多租户数据隔离
- 定期清理过期令牌

### 3. 性能优化
- 使用Redis缓存用户信息和令牌
- 实现令牌查询缓存优化
- 配置合理的连接池参数
- 启用数据库索引优化查询

## 🔄 版本信息

- **Java版本**: 11
- **Spring Boot**: 集成Spring Security
- **数据库**: 支持JPA和MyBatis
- **缓存**: 集成Redis缓存
- **构建工具**: Maven

## 📝 开发指南

### 自定义认证提供者
```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) {
        // 自定义认证逻辑
        return new UsernamePasswordAuthenticationToken(
            principal, credentials, authorities);
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
```

### 扩展用户详情
```java
public class CustomUserDetails extends DefaultSystemUserDetails {
    private String customField;
    
    // 自定义字段和方法
}
```

### 自定义操作日志
```java
@Aspect
@Component
public class CustomOperateLogAspect {
    
    @AfterReturning(pointcut = "@annotation(OperateLog)", returning = "result")
    public void logOperation(JoinPoint joinPoint, Object result) {
        // 自定义日志记录逻辑
    }
}
```

## 故障排查

### 常见问题
1. **认证失败**: 检查用户状态和密码加密配置
2. **令牌过期**: 调整令牌过期时间配置
3. **权限不足**: 检查角色权限配置
4. **缓存问题**: 清理Redis缓存数据

### 日志调试
启用DEBUG级别日志查看详细认证流程：
```yaml
logging:
  level:
    com.tbs.app: DEBUG
    org.springframework.security: DEBUG
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2026年6月
**版本**: 1.0-SNAPSHOT
