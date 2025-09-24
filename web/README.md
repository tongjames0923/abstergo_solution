# Web 模块

Web 模块是基于 Spring Boot 和 Spring Security 构建的 Web 应用框架，提供了认证授权、API 网关、统一结果返回、链路追踪等核心功能。

## 目录结构

```
com.tbs.web
├── BaseController.java         # 基础控制器
├── aop                         # AOP 切面
│   └── ApiInvokeAop.java      # API 调用切面
├── config                      # 配置类
│   ├── property               # 配置属性
│   │   ├── WebAuthProperty.java
│   │   └── WebProperty.java
│   ├── AuthConfig.java        # 认证配置
│   ├── TokenSetConfig.java    # Token 设置配置
│   └── WebConfig.java         # Web 配置
├── constants                   # 常量定义
│   └── WebBeanNameConstant.java
├── enums                       # 枚举类
│   ├── TokenCreateTypeEnum.java
│   ├── TokenSource.java
│   ├── TokenStatusEnum.java
│   └── TokenTypeEnum.java
├── exception                   # 异常类
│   ├── InvalidTokenException.java
│   ├── NoLoginException.java
│   ├── NotFoundTokenException.java
│   ├── ReLoginRequestException.java
│   └── SimpleAuthException.java
├── gate                        # 网关相关
│   ├── center                 # 网关中心
│   │   ├── impl              # 实现类
│   │   │   └── LoginGateWayCenter.java
│   │   ├── AbstractAntPathSupportGateWayCenter.java
│   │   └── IGateWayCenter.java
│   ├── intercept             # 拦截器
│   │   └── IApiInvokeInterceptor.java
│   ├── ITokenPicker.java     # Token 提取器接口
│   └── TokenHandleFilter.java # Token 处理过滤器
├── result                      # 结果处理
│   ├── annotation            # 注解
│   │   ├── NoWrapped.java
│   │   └── Wrapped.java
│   ├── aop                  # AOP 切面
│   │   └── ResponseAjaxAspect.java
│   ├── config               # 配置
│   │   ├── property        # 配置属性
│   │   │   └── ResultProperty.java
│   │   └── ResultConfig.java
│   ├── impl                 # 实现类
│   │   ├── DefaultErrorAjaxProvider.java
│   │   ├── DefaultResultCreateor.java
│   │   ├── SimpleAjaxResultBuilder.java
│   │   └── SimpleBuilderProvider.java
│   ├── AjaxResult.java      # Ajax 结果类
│   ├── IAjaxBuilderProvider.java
│   ├── IAjaxResultBuilder.java
│   ├── IOnErrorAjaxProvider.java
│   └── IResultCreator.java
├── security                  # 安全相关
│   ├── authentication       # 认证
│   │   ├── center          # 认证中心
│   │   │   ├── impl       # 实现类
│   │   │   │   ├── NoImplAuthCenter.java
│   │   │   │   └── SimpleAuthenticationCenter.java
│   │   │   ├── AbstractAuthenticationCenter.java
│   │   │   └── IAuthenticationCenter.java
│   │   └── model           # 模型
│   │       ├── DefaultTokenAuthentication.java
│   │       └── ITokenAuthentication.java
│   ├── issuance            # Token 签发
│   │   ├── center          # 签发中心
│   │   │   ├── AbstractTokenIssuanceCenter.java
│   │   │   └── ITokenIssuanceCenter.java
│   │   ├── form            # 表单
│   │   │   └── TokenRequestForm.java
│   │   └── impl            # 实现类
│   │       └── NoImplTokenIssuanceCenter.java
│   ├── token               # Token 相关
│   │   ├── model           # 模型
│   │   │   └── SimpleTokenModel.java
│   │   ├── AbstractRefreshTokenService.java
│   │   ├── DefaultTokenRemover.java
│   │   ├── IRefreshTokenService.java
│   │   ├── IRefreshTokenValidator.java
│   │   ├── ITokenModel.java
│   │   └── ITokenRemover.java
│   └── user                # 用户相关
│       ├── access          # 访问控制
│       │   ├── impl        # 实现类
│       │   │   ├── DefaultRefreshAccessTokenProvider.java
│       │   │   └── DefaultUserPasswordAccessTokenProvider.java
│       │   ├── AbstractAccessTokenProvider.java
│       │   ├── IRefreshAccessTokenProvider.java
│       │   ├── IUserAccessTokenProvider.java
│       │   └── IUsernamePasswordAccessTokenProvider.java
│       ├── form            # 表单
│       │   ├── IRefreshTokenForm.java
│       │   └── IUsernameAndPasswordForm.java
│       ├── impl            # 实现类
│       │   ├── NoEncryptPassword.java
│       │   ├── SimplePasswordEncrypt.java
│       │   └── UserLoginCountValiadtor.java
│       ├── AbstractUserLoginInfoService.java
│       ├── IPasswordEncrypt.java
│       ├── IUserInfoService.java
│       ├── IUserLoginInfoService.java
│       └── IUserValidator.java
├── trace                     # 链路追踪
│   ├── annotation           # 注解
│   │   ├── EnableTrace.java
│   │   └── Traced.java
│   ├── aop                 # AOP 切面
│   │   └── MethodTraceAop.java
│   ├── config              # 配置
│   │   ├── TraceConfig.java
│   │   └── TraceProperty.java
│   ├── core                # 核心类
│   │   ├── AbstractTraceContext.java
│   │   ├── AbstractTraceInterceptor.java
│   │   ├── IMethodInvokeTreeProvider.java
│   │   ├── ITraceIdObjectFounder.java
│   │   ├── ITraceIdObjectGenerator.java
│   │   ├── MethodTraceObject.java
│   │   └── TraceObject.java
│   ├── impl                # 实现类
│   │   ├── ImportedTraceInterceptor.java
│   │   ├── SimpleMethodInvokeTreeProvider.java
│   │   ├── SpringManagedTraceContext.java
│   │   └── UuidTraceObjectGenerator.java
│   └── tool                # 工具类
│       └── ITraceContextListenner.java
└── uitl                    # 工具类
    ├── AjaxUtil.java      # Ajax 工具类
    └── AuthUtil.java      # 认证工具类
```

## 核心组件说明

### 1. 认证与授权

Web 模块基于 Spring Security 提供了完整的认证与授权机制。

#### 认证中心 (Authentication Center)

认证中心负责验证 Token 并创建认证对象：

- [IAuthenticationCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/IAuthenticationCenter.java#L14-L35):
  认证中心接口
- [AbstractAuthenticationCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/AbstractAuthenticationCenter.java#L17-L47):
  抽象认证中心
- [SimpleAuthenticationCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/impl/SimpleAuthenticationCenter.java#L17-L49):
  简单认证中心实现
- [NoImplAuthCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/impl/NoImplAuthCenter.java#L14-L31):
  无实现认证中心

#### Token 签发中心 (Token Issuance Center)

Token 签发中心负责创建和管理 Token：

- [ITokenIssuanceCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/issuance/center/ITokenIssuanceCenter.java#L25-L74):
  Token 签发中心接口
- [AbstractTokenIssuanceCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/issuance/center/AbstractTokenIssuanceCenter.java#L36-L212):
  抽象 Token 签发中心
- [NoImplTokenIssuanceCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/issuance/impl/NoImplTokenIssuanceCenter.java#L14-L47):
  无实现 Token 签发中心

#### 访问令牌提供者 (Access Token Provider)

访问令牌提供者负责根据不同的认证方式生成访问令牌：

- [IUserAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/IUserAccessTokenProvider.java#L12-L22):
  用户访问令牌提供者接口
- [IUsernamePasswordAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/IUsernamePasswordAccessTokenProvider.java#L10-L22):
  用户名密码访问令牌提供者接口
- [IRefreshAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/IRefreshAccessTokenProvider.java#L10-L22):
  刷新访问令牌提供者接口
- [AbstractAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/AbstractAccessTokenProvider.java#L18-L100):
  抽象访问令牌提供者
- [DefaultUserPasswordAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/impl/DefaultUserPasswordAccessTokenProvider.java#L19-L56):
  默认用户名密码访问令牌提供者

#### Token 模型

Token 模型定义了 Token 的结构和属性：

- [ITokenModel](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/token/ITokenModel.java#L15-L78):
  Token 模型接口
- [SimpleTokenModel](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/token/model/SimpleTokenModel.java#L14-L110):
  简单 Token 模型实现

### 2. API 网关

API 网关负责处理请求的认证和权限验证。

#### 网关中心 (Gateway Center)

网关中心定义了处理请求的接口：

- [IGateWayCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/center/IGateWayCenter.java#L14-L52):
  网关中心接口
- [AbstractAntPathSupportGateWayCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/center/AbstractAntPathSupportGateWayCenter.java#L11-L29):
  基于路径匹配的抽象网关中心
- [LoginGateWayCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/center/impl/LoginGateWayCenter.java#L25-L76):
  登录网关中心实现

#### Token 提取器 (Token Picker)

Token 提取器负责从请求中提取 Token：

- [ITokenPicker](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/ITokenPicker.java#L14-L25):
  Token 提取器接口

#### Token 处理过滤器

- [TokenHandleFilter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/TokenHandleFilter.java#L23-L80):
  Token 处理过滤器

### 3. 统一结果返回

统一结果返回机制确保 API 响应格式的一致性。

#### 注解

- [@Wrapped](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/annotation/Wrapped.java#L13-L24):
  标记需要包装返回结果的方法
- [@NoWrapped](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/annotation/NoWrapped.java#L11-L18):
  标记不需要包装返回结果的方法

#### 核心接口

- [IAjaxResultBuilder](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IAjaxResultBuilder.java#L11-L47):
  Ajax 结果构建器接口
- [IAjaxBuilderProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IAjaxBuilderProvider.java#L10-L20):
  Ajax 构建器提供者接口
- [IResultCreator](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IResultCreator.java#L9-L26):
  结果创建者接口
- [IOnErrorAjaxProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IOnErrorAjaxProvider.java#L12-L24):
  错误 Ajax 提供者接口

#### 核心类

- [AjaxResult](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/AjaxResult.java#L13-L85):
  Ajax 结果类
- [ResponseAjaxAspect](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/aop/ResponseAjaxAspect.java#L23-L113):
  响应 Ajax 切面

### 4. 链路追踪

链路追踪功能帮助开发者监控和分析请求在系统中的执行路径。

#### 核心组件

- [AbstractTraceContext](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/AbstractTraceContext.java#L22-L227):
  抽象链路追踪上下文
- [AbstractTraceInterceptor](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/AbstractTraceInterceptor.java#L14-L68):
  抽象链路追踪拦截器
- [ITraceIdObjectGenerator](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/ITraceIdObjectGenerator.java#L9-L19):
  Trace ID 对象生成器接口
- [IMethodInvokeTreeProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/IMethodInvokeTreeProvider.java#L11-L23):
  方法调用树提供者接口
- [TraceObject](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/TraceObject.java#L11-L100):
  追踪对象

#### 实现类

- [SpringManagedTraceContext](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/impl/SpringManagedTraceContext.java#L15-L75):
  Spring 管理的追踪上下文
- [UuidTraceObjectGenerator](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/impl/UuidTraceObjectGenerator.java#L9-L15):
  UUID 追踪对象生成器

#### 注解

- [@EnableTrace](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/annotation/EnableTrace.java#L8-L12):
  启用链路追踪
- [@Traced](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/annotation/Traced.java#L11-L18):
  标记需要追踪的方法

## 自定义使用说明

### 1. 认证与授权自定义

#### 实现自定义认证中心

要实现自定义认证中心，需要实现 [IAuthenticationCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/IAuthenticationCenter.java#L14-L35)
接口或继承 [AbstractAuthenticationCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/authentication/center/AbstractAuthenticationCenter.java#L17-L47)
类：

```java

@Component
public class CustomAuthenticationCenter extends AbstractAuthenticationCenter {

    @Override
    protected ITokenAuthentication createAuth(@NotNull ITokenModel model) {
        // 创建认证对象
        DefaultTokenAuthentication tokenAuthentication = new DefaultTokenAuthentication(model, null);
        return tokenAuthentication;
    }

    @Override
    public ITokenAuthentication authenticate(ITokenAuthentication authentication) throws AuthenticationException {
        // 执行认证逻辑
        AssertUtil.isTrue(authentication == null, () -> new SimpleAuthException("认证信息为空"));
        AssertUtil.isTrue(authentication.getTokenModel() == null, () -> new SimpleAuthException("令牌信息为空"));
        AssertUtil.isTrue(authentication.getUserDetails() == null, () -> new SimpleAuthException("用户信息为空"));
        try {
            AuthUtil.checkUserDetails(authentication.getUserDetails());
            authentication.setAuthenticated(true);
        } catch (Exception e) {
            authentication.setAuthenticated(false);
            AssertUtil.throwError(
                () -> new SimpleAuthException(StrUtil.format("用户信息验证失败。详情:{}", e.getLocalizedMessage())));
        }
        return authentication;
    }
}
```

#### 实现自定义 Token 签发中心

要实现自定义 Token
签发中心，需要实现 [ITokenIssuanceCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/issuance/center/ITokenIssuanceCenter.java#L25-L74)
接口：

```java

@Component
public class CustomTokenIssuanceCenter implements ITokenIssuanceCenter {

    @Override
    public ITokenModel issueToken(@NotNull TokenRequestForm form) {
        // 签发令牌逻辑
        SimpleTokenModel tokenModel = new SimpleTokenModel();
        tokenModel.setToken(UUID.randomUUID().toString());
        tokenModel.setUserName(form.getUserDetails().getUsername());
        tokenModel.setCreateTime(new Date());
        tokenModel.setExpireTime(new Date(System.currentTimeMillis() + 3600000)); // 1小时后过期
        tokenModel.setTokenType(form.getTokenType());
        tokenModel.setCreateType(form.getTokenCreateType());
        tokenModel.setStatus(TokenStatusEnum.ACTIVE);
        tokenModel.setSessionId(form.getSessionId());
        // 保存令牌
        tokenRepository.save(tokenModel);
        return tokenModel;
    }

    @Override
    public ITokenModel getTokenByToken(@NotNull String token) {
        // 根据令牌字符串获取令牌模型
        return tokenRepository.findByToken(token);
    }

    @Override
    public void setTokenStatus(@NotNull String token, @NotNull TokenStatusEnum status) {
        // 设置令牌状态
        ITokenModel model = getTokenByToken(token);
        if (model != null) {
            model.setStatus(status);
            tokenRepository.save(model);
        }
    }

    @Override
    public Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType) {
        // 根据用户名和令牌类型获取用户令牌集合
        return tokenRepository.findByUserNameAndTokenType(userName, tokenType);
    }

    @Override
    public Collection<ITokenModel> getTokensBySessionId(String sessionId) {
        // 根据SessionId获取用户令牌集合
        return tokenRepository.findBySessionId(sessionId);
    }
}
```

#### 实现自定义访问令牌提供者

要实现自定义访问令牌提供者，需要继承 [AbstractAccessTokenProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/security/user/access/AbstractAccessTokenProvider.java#L18-L100)
类：

```java

@Component
public class CustomUsernamePasswordAccessTokenProvider extends AbstractAccessTokenProvider<IUsernameAndPasswordForm>
    implements IUsernamePasswordAccessTokenProvider {

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    IPasswordEncrypt encrypt;

    @Override
    protected void validateForm(@NotNull IUsernameAndPasswordForm form) {
        // 验证表单
        AssertUtil.hasText(form.getUsername(), "用户名不能为空");
        AssertUtil.hasText(form.getPassword(), "密码不能为空");
    }

    @Override
    protected UserDetails pullUserDetails(@NotNull IUsernameAndPasswordForm form) {
        // 根据用户名和密码获取用户详情
        UserDetails details = userInfoService.loadUserInfoByUniqueKey(form.getUsername());
        AssertUtil.notNull(details, "用户 {} 不存在", form.getUsername());
        AssertUtil.isTrue(!Objects.equals(encrypt.encrypt(form.getPassword()), details.getPassword()),
            "用户 {} 密码错误", form.getUsername());
        return details;
    }

    @Override
    protected TokenRequestForm createTokenRequestForm(@NotNull IUsernameAndPasswordForm form, UserDetails details) {
        // 创建 token 请求表单
        return new TokenRequestForm(UuidUtil.getInstance().getUuidString(), details,
            TokenTypeEnum.ACCESS_TOKEN.getCode(), TokenCreateTypeEnum.LOGIN.getCode());
    }
}
```

### 2. API 网关自定义

#### 实现自定义网关中心

要实现自定义网关中心，可以实现 [IGateWayCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/center/IGateWayCenter.java#L14-L52)
接口或继承 [AbstractAntPathSupportGateWayCenter](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/center/AbstractAntPathSupportGateWayCenter.java#L11-L29)
类：

```java

@Component
public class CustomGateWayCenter extends AbstractAntPathSupportGateWayCenter {

    private ITokenPicker customTokenPicker;

    @Resource
    IAuthenticationCenter authenticationCenter;

    @Resource
    private AuthenticationManager manager;

    public CustomGateWayCenter(ITokenPicker customTokenPicker) {
        super(new AntPathRequestMatcher("/api/custom/**"));
        this.customTokenPicker = customTokenPicker;
    }

    @Override
    public ITokenPicker getTokenPicker() {
        // 返回自定义的 token 提取器
        return customTokenPicker;
    }

    @Override
    public void onNoneToken() {
        // 处理无 token 的情况
        throw new NoLoginException("需要登录才能访问");
    }

    @Override
    public void handleToken(ITokenModel tokenModel) {
        // 处理 token
        ITokenAuthentication tokenAuthentication = authenticationCenter.createAuthenticationByTokenModel(tokenModel);
        AuthUtil.getInstance().setAuthentication((ITokenAuthentication)manager.authenticate(tokenAuthentication));
    }

    @Override
    public boolean onErrorWillBroken(Throwable e) {
        // 决定是否中断请求处理
        return true;
    }
}
```

#### 实现自定义 Token 提取器

要实现自定义 Token
提取器，需要实现 [ITokenPicker](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/gate/ITokenPicker.java#L14-L25)
接口：

```java

@Component
public class CustomTokenPicker implements ITokenPicker {

    @Override
    public String pick(HttpServletRequest request) throws NotFoundTokenException {
        // 从请求头中提取 token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            throw new NotFoundTokenException("未找到认证信息");
        }
        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
```

### 3. 统一结果返回自定义

#### 实现自定义结果构建器

要实现自定义结果构建器，需要实现 [IAjaxResultBuilder](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IAjaxResultBuilder.java#L11-L47)
接口：

```java

@Component
public class CustomAjaxResultBuilder implements IAjaxResultBuilder {

    @Override
    public IAjaxResult1Step create() {
        return new CustomAjaxResult1Step();
    }

    public class CustomAjaxResult1Step implements IAjaxResult1Step {
        private Map<String, Object> map = new HashMap<>(4);

        @Override
        public <T> IAjaxResult1Step appendField(String field, T value) {
            map.putIfAbsent(field, value);
            return this;
        }

        @Override
        public IAjaxResult2Step next() {
            return new CustomAjaxResult2Step(map);
        }
    }

    public class CustomAjaxResult2Step implements IAjaxResult2Step {
        private AjaxResult ajaxResult = new AjaxResult();

        public CustomAjaxResult2Step(Map<String, Object> datas) {
            if (CollUtil.isEmpty(datas)) {
                return;
            }
            if (ajaxResult.getMap() == null) {
                ajaxResult.setMap(new LinkedHashMap<>(datas.size()));
            }
            ajaxResult.getMap().putAll(datas);
        }

        @Override
        public IAjaxResult2Step preHandle(SConsumer<Map<String, Object>> consumer) {
            if (consumer != null) {
                consumer.accept(ajaxResult.getMap());
            }
            return this;
        }

        @Override
        public AjaxResult build() {
            return ajaxResult;
        }
    }
}
```

#### 实现自定义结果创建者

要实现自定义结果创建者，需要实现 [IResultCreator](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/IResultCreator.java#L9-L26)
接口：

```java

@Component
public class CustomResultCreator implements IResultCreator {

    @Override
    public AjaxResult create(IAjaxResultBuilder builder, Object data) {
        // 自定义结果创建逻辑
        if (data instanceof AjaxResult) {
            return (AjaxResult)data;
        }
        // 包装普通数据为 AjaxResult
        return builder.create().appendField(AjaxResult.CODE_KEY, AjaxResult.DEFAULT_SUCCESS_CODE)
            .appendField(AjaxResult.MESSAGE_KEY, AjaxResult.DEFAULT_SUCCESS_MESSAGE)
            .appendField(AjaxResult.DATA_KEY, data).next().build();
    }
}
```

### 4. 链路追踪自定义

#### 实现自定义 Trace ID 生成器

要实现自定义 Trace ID
生成器，需要实现 [ITraceIdObjectGenerator](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/ITraceIdObjectGenerator.java#L9-L19)
接口：

```java

@Component
public class CustomTraceIdGenerator implements ITraceIdObjectGenerator {

    @Override
    public TraceObject generateTraceObject() {
        TraceObject traceObject = new TraceObject();
        traceObject.setTraceId(UUID.randomUUID().toString());
        traceObject.setContextId(Thread.currentThread().getName());
        traceObject.setAvaliableTime(new Date());
        return traceObject;
    }
}
```

#### 实现自定义方法调用树提供者

要实现自定义方法调用树提供者，需要实现 [IMethodInvokeTreeProvider](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/core/IMethodInvokeTreeProvider.java#L11-L23)
接口：

```java

@Component
public class CustomMethodInvokeTreeProvider implements IMethodInvokeTreeProvider {

    private TraceObject rootTraceObject;

    @Override
    public void setMethodRoot(TraceObject root) {
        // 设置方法调用树的根节点
        this.rootTraceObject = root;
    }

    @Override
    public TraceObject getTree() {
        // 获取方法调用树的根节点
        return rootTraceObject;
    }

    @Override
    public TraceObject getCurrentTraceObject() {
        // 获取当前方法调用的追踪对象
        return rootTraceObject;
    }

    @Override
    public void beginMethodFrom(String traceId, MethodTraceObject traceObject) {
        // 方法调用开始
        // 可以在这里记录方法调用的开始时间等信息
    }

    @Override
    public TraceObject endMethod(String traceId) {
        // 方法调用结束
        // 可以在这里记录方法调用的结束时间等信息
        return rootTraceObject;
    }
}
```

## 配置说明

### 1. Web 配置

在 `application.yml` 中配置 Web 相关参数：

```yaml
tbs:
  web:
    # 是否启用跨域
    enable-cors: true

    # 是否启用CSRF
    enable-csrf: false

    # 是否启用匿名访问
    enable-anonymous: true

    # 是否启用表单登录
    enable-form-login: false

    # 是否启用HTTP Basic认证
    enable-http-basic: false

    # 是否启用注销功能
    enable-logout: false

    # 需要认证才能访问的路径
    secured-paths:
      - "/api/**"

    # 允许所有用户访问的路径
    permit-all-paths:
      - "/"
      - "/login"
      - "/public/**"
```

### 2. 认证配置

在 `application.yml` 中配置认证相关参数：

```yaml
tbs:
  web:
    auth:
      # 是否启用认证
      enable: true

      # Token 字段名
      token-filed-name: "access_token"

      # Token 来源 (HEADER, PARAMETER, COOKIE)
      token-source: HEADER

      # Token 过期时间 (分钟)
      token-expire-time: 30

      # Token 提取器实现类
      token-picker-impl: com.tbs.web.security.impl.DefaultLoginTokenPicker

      # Token 签发中心实现类
      token-issuance-center-impl: com.tbs.web.security.issuance.impl.NoImplTokenIssuanceCenter
```

### 3. 链路追踪配置

在 `application.yml` 中配置链路追踪相关参数：

```yaml
tbs:
  trace:
    # Trace ID 对象生成器
    trace-id-object-generator: com.tbs.web.trace.impl.UuidTraceObjectGenerator

    # 链路追踪上下文
    trace-context: com.tbs.web.trace.impl.SpringManagedTraceContext

    # 链路追踪排除路径
    exclude-paths: [ ]

    # 方法调用树提供者
    method-invoke-tree-provider: com.tbs.web.trace.impl.SimpleMethodInvokeTreeProvider
```

### 4. 结果返回配置

在 `application.yml` 中配置结果返回相关参数：

```yaml
tbs:
  result:
    # 结果构建器提供者
    result-builder-provier: com.tbs.web.result.impl.SimpleBuilderProvider
```

## 使用示例

### 1. 启用 Web 功能

在 Spring Boot 应用的配置类上添加相关注解：

```java

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 启用认证功能

在配置类上添加认证相关注解：

```java

@Configuration
public class SecurityConfig {

    @Bean
    public IAuthenticationCenter authenticationCenter() {
        return new SimpleAuthenticationCenter();
    }

    @Bean
    public ITokenIssuanceCenter tokenIssuanceCenter() {
        return new CustomTokenIssuanceCenter();
    }
}
```

### 3. 启用链路追踪功能

在配置类上添加链路追踪相关注解：

```java

@SpringBootApplication
@EnableTrace
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. 使用统一结果返回

在 Controller
方法上使用 [@Wrapped](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/annotation/Wrapped.java#L13-L24)
注解：

```java
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    @GetMapping("/{id}")
    @Wrapped
    public User getUser(@PathVariable Long id) {
        // 返回用户信息，会自动包装为 AjaxResult
        return userService.findById(id);
    }

    @PostMapping
    @Wrapped
    public User createUser(@RequestBody User user) {
        // 创建用户并返回，会自动包装为 AjaxResult
        return userService.create(user);
    }

    @GetMapping("/raw")
    @NoWrapped
    public User getRawUser(@PathVariable Long id) {
        // 返回原始用户信息，不进行包装
        return userService.findById(id);
    }
}
```

### 5. 使用链路追踪

在需要追踪的方法上使用 [@Traced](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/annotation/Traced.java#L11-L18)
注解：

```java

@Service
public class UserService {

    @Traced
    public User findById(Long id) {
        // 这个方法的执行会被追踪
        return userRepository.findById(id);
    }

    @Traced
    public User create(User user) {
        // 这个方法的执行会被追踪
        return userRepository.save(user);
    }
}
```

## 最佳实践

1. **认证与授权**：
    - 实现自定义的认证中心和 Token 签发中心以满足业务需求
    - 使用 HTTPS 保护 Token 传输安全
    - 定期刷新 Token 以提高安全性

2. **API 网关**：
    - 根据业务需求实现多个网关中心处理不同路径的请求
    - 合理设计 Token 提取器以支持多种 Token 传递方式

3. **统一结果返回**：
    - 统一 API 响应格式，便于前端处理
    -
   合理使用 [@Wrapped](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/annotation/Wrapped.java#L13-L24)
   和 [@NoWrapped](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/result/annotation/NoWrapped.java#L11-L18)
   注解控制返回格式

4. **链路追踪**：
   -
   在关键业务方法上使用 [@Traced](file:///Users/abstergo/IdeaProjects/abstergo_solution/web/src/main/java/com/tbs/web/trace/annotation/Traced.java#L11-L18)
   注解进行追踪
    - 合理配置排除路径，避免追踪不必要的请求

## 注意事项

1. **安全性**：
    - 确保 Token 的安全存储和传输
    - 实现合理的 Token 过期和刷新机制
    - 防止 Token 泄露和重放攻击

2. **性能**：
    - 避免在链路追踪中记录过多不必要的信息
    - 合理使用缓存提高认证和授权效率

3. **异常处理**：
    - 正确处理各种认证和授权异常
    - 提供清晰的错误信息帮助调试

4. **扩展性**：
    - 通过接口和抽象类设计，便于扩展和定制功能
    - 遵循开闭原则，对扩展开放，对修改关闭
