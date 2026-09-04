# web 模块

Web 层基础功能解决方案：基于 Spring Security 提供令牌认证、路径级网关、统一响应包装、请求上下文（含 Dubbo 跨服务传播）、国际化与泛化 API 调用能力。本模块**只定义契约与默认装配，不含具体业务实现**——令牌签发中心、用户信息服务等关键 SPI 需由宿主应用落地（`default-system-impl` 即为官方实现参考）。

## 目录

- [模块定位](#模块定位)
- [依赖说明](#依赖说明)
- [快速开始](#快速开始)
- [架构总览](#架构总览)
- [认证体系](#认证体系)
- [用户与登录 SPI](#用户与登录-spi)
- [统一响应体系](#统一响应体系)
- [请求上下文与跨服务传播](#请求上下文与跨服务传播)
- [API 调用拦截器](#api-调用拦截器)
- [国际化](#国际化)
- [泛化 API 调用](#泛化-api-调用)
- [其他组件](#其他组件)
- [配置参考](#配置参考)
- [扩展指南](#扩展指南)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 基础层（与 common 同级，被 system/dic/logging 等模块依赖） |
| 核心能力 | 认证（token）、授权网关、统一响应、请求上下文、i18n、泛化 API |
| 实现方式 | Spring Security（`WebSecurityConfigurerAdapter`）+ Spring AOP + Dubbo Filter |
| Web 容器 | Undertow（已排除 Tomcat） |
| 启用方式 | `@EnableWebPack` 注解（无 `spring.factories`） |

## 依赖说明

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>web</artifactId>
</dependency>
```

直接依赖：`common`、`tree-api`、`spring-boot-starter-web`（排除 Tomcat，替换为 **Undertow**）、`spring-boot-starter-security`、`spring-boot-starter-aop`、`dubbo-spring-boot-starter`。

## 快速开始

### 1. 启动类标注注解

```java
@SpringBootApplication
@EnableWebPack          // 导入 WebAutoRegister，扫描注册 com.tbs.web 包，注册 Feature.WEB
public class Application { ... }
```

> `@EnableWebPack` 同时组合了 `@EnableCommonPack`，会一并启用 common 模块（i18n、UUID、数据持有等）。

### 2. 必须落地的 SPI

仅引入本模块**无法完成登录闭环**，至少需要实现：

| SPI | 职责 | 默认实现（不可用于生产） |
| --- | --- | --- |
| `ITokenIssuanceCenter` | 令牌签发/查询/状态管理 | `NoImplTokenIssuanceCenter`（调用即抛"请实现一个Token签发中心"） |
| `IUserInfoService` | 按唯一标识加载用户 | 无 |
| `IUserLoginInfoService` | 令牌 ↔ 用户详情映射 | 无 |
| `IAuthenticationCenter` | 令牌模型 → 认证对象 + 校验 | `SimpleAuthenticationCenter`（按容器内已有组件的简单串联） |

推荐直接引入 `default-system-impl`（system 模块），其提供了基于缓存 + 数据库的完整实现（`DefaultTokenSignCenter` 等）。

### 3. 最小可用配置

```yaml
tbs:
  web:
    secured-paths:        # 需要登录才能访问的路径（Ant 风格）
      - /api/**
    permit-all-paths:     # 匿名放行路径
      - /
      - /login
      - /public/**
  web.auth:
    token-source: HEADER            # 令牌来源：HEADER / COOKIE / PARAM / BODY
    token-filed-name: Authorization # 令牌字段名（属性名源码拼写即为 FiledName）
    token-expire-time: 3600         # 访问令牌有效期（秒）
```

## 架构总览

一次携带令牌的 HTTP 请求的处理链路：

```
HTTP 请求
  │
  ├─ I18nFilter（@Order(HIGHEST_PRECEDENCE+10)，先于 Spring Security）
  │    解析语言（?lang= → Accept-Language → X-Lang/X-Locale）写入 I18nContextHolder
  │    finally 清理 Locale
  │
  ├─ Spring Security 过滤链
  │    └─ TokenHandleFilter（插在 UsernamePasswordAuthenticationFilter 之前）
  │         遍历所有 IGateWayCenter（按 Ordered 排序）：
  │           LoginGateWayCenter（匹配 secured-paths）
  │             ├─ ITokenPicker.pick(request)          提取令牌串
  │             ├─ ITokenIssuanceCenter.getTokenByToken 令牌模型（含惰性过期修正）
  │             ├─ 无令牌 → onNoneToken() 抛 NotFoundTokenException
  │             └─ 有令牌 → IAuthenticationCenter.createAuthenticationByTokenModel
  │                          → AuthenticationManager.authenticate（TokenAuthenticationProvider
  │                             委托 IAuthenticationCenter.authenticate）
  │                          → AuthUtil.setAuthentication 写入 SecurityContext
  │
  ├─ ApiInvokeAop（拦截 BaseController 子类的 @*Mapping 方法）
  │    依次执行 IApiInvokeInterceptor：
  │      ├─ RequestContextInterceptor（创建/填充 RequestDataContext）
  │      └─ I18nInterceptor（备份语言解析）
  │
  ├─ Controller 业务方法
  │
  └─ ResponseAjaxAspect（@RestControllerAdvice）
       ├─ 异常：@ExceptionHandler(Throwable) → 分级日志 → IOnErrorAjaxProvider 链 → AjaxResult
       └─ 响应：ResponseBodyAdvice → 按 @NoWrapped / @Wrapped / BaseController 规则包装为统一结构
```

## 认证体系

### 令牌模型（ITokenModel）

```java
public interface ITokenModel extends Serializable {
    String getToken();                                    // 令牌串
    Date getExpireTime();                                // 过期时间（null=永久）
    Date getCreateTime();
    String getUserName();                                // 归属用户
    TokenCreateTypeEnum getCreateType();                 // 创建方式
    TokenTypeEnum getTokenType();                        // 令牌类型
    TokenStatusEnum getStatus();
    void setStatus(TokenStatusEnum status);
    String getSessionId();                               // 会话标识
}
```

配套枚举：

| 枚举 | 值 |
| --- | --- |
| `TokenTypeEnum` | `WEB_ACCESS_TOKEN(1)`、`REFRESH_TOKEN(2)`、`APPLICATION_ACCESS_TOKEN(3)` |
| `TokenStatusEnum` | `ACTIVE(1)`、`DELETED(0)`、`REVOKE(-1)`、`EXPIRED(-2)`、`INACTIVE(-3)` |
| `TokenCreateTypeEnum` | `LOGIN(1)`、`REFRESH(2)` |
| `TokenSource` | `COOKIE(1)`、`HEADER(2)`、`PARAM(3)`、`BODY(4)` |

### 令牌签发中心（ITokenIssuanceCenter / AbstractTokenIssuanceCenter）

对外契约：

```java
public interface ITokenIssuanceCenter {
    ITokenModel issueToken(TokenRequestForm form);                    // 签发
    ITokenModel getTokenByToken(String token);                         // 查询（含惰性过期修正）
    void setTokenStatus(String token, TokenStatusEnum status);         // 状态管理
    Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType);
    Collection<ITokenModel> getTokensBySessionId(String sessionId);
    Collection<ITokenModel> listSessions(String userName, Date beg, Date end);
}
```

继承 `AbstractTokenIssuanceCenter` 实现时只需落地 6 个抽象方法（`getModel`/`saveToken`/`saveManageInfo`/`getUserNameAndTokenType`/`getTokensWithSessionId`/`listSessionsByUserName`/`createModel`），模板层提供：

- **参数校验**：`issueToken` 对表单逐项断言（表单/用户信息/用户名/令牌类型/创建方式/会话 ID 均不能为空），错误信息为 i18n 消息；
- **惰性过期**：`getTokenByToken` 查询时若已过期且状态为 `ACTIVE`，置 `EXPIRED` 并回写存储，不依赖定时任务；
- **写顺序保证**：`saveModel` 先写「用户名-类型-令牌」索引（`saveManageInfo`），再写令牌本体（`saveToken`）；
- **有效期推荐**：`recommendExpireTime` 按 `WebAuthProperty` 计算——刷新令牌/应用令牌按小时、访问令牌按秒；
- **会话兜底**：`listSessions` 时间范围为空时默认取最近 168 天；
- **去重**：按用户+类型查询的结果按令牌串去重。

`TokenRequestForm` 字段：`sessionId`、`userDetails`、`tokenType`（Integer，取 `TokenTypeEnum#getCode()`）、`tokenCreateType`。

### 网关中心（IGateWayCenter）

请求入口的认证处理单元，`TokenHandleFilter` 启动时收集容器内全部实现并按 `Ordered` 排序：

```java
public interface IGateWayCenter extends Ordered {
    ITokenPicker getTokenPicker();                    // 该中心的令牌拾取器
    void onNoneToken();                              // 无令牌时的处理
    boolean support(HttpServletRequest request);      // 是否处理该请求
    void handleToken(ITokenModel tokenModel);        // 有令牌时的处理
    boolean onErrorWillBroken(Throwable e);           // 异常是否中断请求
}
```

内置实现：

| 实现 | 行为 |
| --- | --- |
| `LoginGateWayCenter` | 匹配 `secured-paths`（基于 `AntPathRequestMatcher` 的 `OrRequestMatcher`）；无令牌抛 `NotFoundTokenException`；有令牌则构造认证对象 → `AuthenticationManager` 校验 → 写入上下文；**所有异常一律中断请求**（`onErrorWillBroken` 恒 true） |
| `AbstractAntPathSupportGateWayCenter` | 路径匹配基类，`setMatcher(paths)` 编译 Ant 风格路径集合 |

异常中断时由 `TokenHandleFilter` 经 `IOnErrorAjaxProvider` 链转换为 `AjaxResult` 并写入响应；非中断异常仅 warn 放行。

### 令牌拾取器（ITokenPicker / SimpleTokenPicker）

```java
public interface ITokenPicker {
    String pick(HttpServletRequest request) throws NotFoundTokenException;
}
```

`SimpleTokenPicker` 按 `tokenSource` 四种来源提取令牌：`HEADER`（请求头）、`COOKIE`、`PARAM`（URL 参数）、`BODY`（读取请求体解析 JSON 字段）。

`DefaultLoginTokenPicker` 在 `@PostConstruct` 时从 `WebAuthProperty` 读取字段名、来源与有效期，是 `tbs.web.auth.token-picker-impl` 的默认值。BODY 来源会消费请求体流，与后续 `@RequestBody` 解析可能冲突，建议使用 HEADER。

### 认证中心与认证管理

- `IAuthenticationCenter`：`createAuthenticationByTokenModel(model)`（令牌模型 → `ITokenAuthentication`，非法时抛 `InvalidTokenException`）+ `authenticate(authentication)`（校验签名/过期/状态）。默认实现 `SimpleAuthenticationCenter`。
- `IAuthManager`：认证信息的统一读写门面（`isUserLoggedIn`/`getCurrentUser`/`getCurrentTokenModel`/`setAuthentication`/`clearAuthentication(token)`/`getAuthentication(token)`/`notifyUserAuthStatusChanged`）。默认实现 `SpringSecurityContextHolderAuthManager`（基于 SecurityContextHolder）。
- `ITokenAuthentication extends Authentication`：在 Spring Security 认证对象上扩展 `getTokenModel`/`setTokenModel`/`hasToken`/`getUserDetails`/`getAuthException`/`setAuthException`——认证链路各环节通过它共享令牌与失败原因。
- `TokenAuthenticationProvider`：接入 Spring Security 认证链，仅支持 `DefaultTokenAuthentication` 类型；若认证对象已携带预处理异常则直接抛出，避免重复校验。

### 安全配置（WebConfig）

`WebConfig` 继承 `WebSecurityConfigurerAdapter`（Spring Security 5.7 起已废弃，2.7 下仍可用）并标注 `@EnableMethodSecurity(prePostEnabled = true)`（支持 `@PreAuthorize`）。行为由 `WebProperty` 驱动：

- `TokenHandleFilter` 注册于 `UsernamePasswordAuthenticationFilter` 之前；
- 会话策略固定 `STATELESS`；
- `permit-all-paths` 全放行，`secured-paths` 需认证；
- 异常处理挂接 `DefaultAuthEntryPoint`（认证入口）与 `DefaultAuthAccessDeniedHandler`（拒绝访问）；
- CORS 开启时配置为**放行全部来源/头/方法**（演示默认，生产需收紧）。

## 用户与登录 SPI

宿主应用（或 `default-system-impl`）需实现的核心契约：

| SPI | 方法 | 说明 |
| --- | --- | --- |
| `IUserInfoService` | `loadUserInfoByUniqueKey(String)` | 按用户名等标识加载 `UserDetails`，不依赖登录态 |
| `IUserLoginInfoService` | `insertUserLoginInfo` / `removeUserLoginInfo(token)` / `replaceUserDetails` / `getUserDetails(token)` / `listUserSessions(username)` / `getUserAvaliableSessionId`（已废弃） | 维护令牌 ↔ 用户详情映射；`listUserSessions` 返回 `LoginSessionVO`（sessionId、accessTokens、online、tokenCount、activeRefreshTokenCount、lastActiveTime） |
| `IPasswordEncrypt` | `encrypt(String)` | 密码加密。内置 `NoEncryptPassword`（明文）与 `SimplePasswordEncrypt` |
| `IUserValidator` | `validate(UserDetails)`（`Ordered` 链） | 附加校验（账号状态等），失败抛 `FormattedRuntimeException` |
| `IRefreshTokenService` / `IRefreshTokenValidator` / `ITokenRemover` | 刷新令牌签发（`AbstractRefreshTokenService` 模板）/ 校验 / 移除 | 刷新令牌体系 |
| `IUsernamePasswordAccessTokenProvider` / `IRefreshAccessTokenProvider`（继承 `IUserAccessTokenProvider`，推荐基于 `AbstractAccessTokenProvider`） | `IUsernameAndPasswordForm` / `IRefreshTokenForm` → 令牌 | 登录/刷新的入参编排与签发入口 |

内置 `UserLoginCountValiadtor`（类名拼写如此）：登录次数校验器。

`AuthUtil`（单例，`AuthUtil.getInstance()`）是业务代码获取登录态的统一入口：

```java
AuthUtil.getInstance().getCurrentUser();          // UserDetails（未登录抛 NoLoginException）
AuthUtil.getInstance().getCurrentTokenModel();    // ITokenModel
AuthUtil.getInstance().isUserLoggedIn();
AuthUtil.getInstance().getAuthentication();       // Optional<ITokenAuthentication>
AuthUtil.checkUserDetails(details);               // 静态校验：存在性/用户名/密码/启用/过期/凭证过期
```

## 统一响应体系

### AjaxResult

`AjaxResult` 内部是 `Map<String, Object>`（`getMap()`），约定三个键：`code`、`message`、`data`。默认码值常量：

| 常量 | 值 | 语义 |
| --- | --- | --- |
| `DEFAULT_SUCCESS_CODE` | 200 | 成功 |
| `DEFAULT_ERROR_CODE` | 500 | 错误 |
| `DEFAULT_UNAUTHORIZED_CODE` | 401 | 未授权 |
| `DEFAULT_FORBIDDEN_CODE` | 403 | 无权限 |
| `RELOGIN_CODE` | 405 | 需重新登录 |
| `REFRESH_FAIL_CODE` | 468 | 刷新令牌失败 |
| `DEBUG_ERROR_CODE` | 601 | 调试错误 |
| `NOTICE_CODE` | 701 | 通知 |

默认消息经 i18n 解析（`common.success`/`common.fail`/`common.error`/`common.relogin` 等 key，其中 success/error/unauthorized/forbidden 定义在 common 模块）。

### 注解驱动包装

```java
@GetMapping("/user")
@Wrapped                                    // 默认 DefaultResultCreateor：{code:200, message:操作成功, data:返回值}
public UserVO getUser() { ... }

@GetMapping("/raw")
@NoWrapped                                   // 原样返回，不做统一包装（文件下载等场景）
public byte[] download() { ... }
```

`ResponseAjaxAspect`（`@RestControllerAdvice`）兼具两类职责：

1. **全局异常处理**（`@ExceptionHandler(Throwable.class)`）：先经 `AssertUtil.toFormattedRuntimeException` 归一化，按 `ErrorLevelEnum` 分级记日志（`USER_INPUT_ERROR`/`BUSINESS_LOGIC_ERROR` 记 warn，其余记 error），再经 `IOnErrorAjaxProvider` 链转换为 `AjaxResult`；响应已提交时返回 null。
2. **响应体包装**（`ResponseBodyAdvice`）：判定顺序——返回值已是 `AjaxResult` 则直接展开 Map；`@NoWrapped` 放行；方法在 `BaseController` 子类中则用 `successResult(body)` 包装；`@Wrapped` 则按注解指定的 `IResultCreator` 构建。方法级判定结果以「类全名.方法名」为键缓存在两个 `ConcurrentHashMap` 中。

### 结果构建 SPI

```java
public interface IResultCreator {
    IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder, Object val);
}
```

`IAjaxResultBuilder` 为两步式构建：`create().appendField(key, value)...next().preHandle(map -> ...).build()`。

`AjaxUtil`（单例）提供快捷构建与错误转换：

```java
AjaxUtil.getInstance().success(data);                          // 成功
AjaxUtil.getInstance().success(msg, data);                     // 成功（自定义消息）
AjaxUtil.getInstance().error(msg, data);                      // 错误
AjaxUtil.getInstance().debugError(msg);                        // 调试错误（601）
AjaxUtil.getInstance().custom(code, msg, data);                // 自定义码
AjaxUtil.getInstance().successWithRedirect(data, "/index");   // 成功 + 重定向
AjaxUtil.getInstance().noticeError(title, msg, WebMessageTypeEnum.WARNING, 5000); // 通知
AjaxUtil.getInstance().createAjaxResultOnError(e);            // 异常 → AjaxResult（按 provider 链匹配）
AjaxUtil.writeInfoResp(response, ajaxResult);                 // 写入 HttpServletResponse（JSON）
AjaxUtil.builder().code(200).message("ok").data(vo).build();  // AjaxResultFluentBuilder 流式构建
```

错误转换链：启动时（`IStartup`）收集容器内全部 `IOnErrorAjaxProvider<T extends Throwable>`（`support(clazz)` + `getOrder()` 排序）：

| 内置 Provider | 行为 |
| --- | --- |
| `ReLoginErrorAjaxProvider` | 专处理 `ReLoginRequestException`，返回 405 提示重新登录（无条件注册） |
| `DefaultErrorAjaxProvider` | 兜底（`LOWEST_PRECEDENCE`），异常本地化消息为空时回退通用错误消息 |

另有 `MethodArgumentNotValidExceptionTransformer`（实现 common 的 `IErrorTransformer`）：把 `@Valid` 校验失败转为 `USER_INPUT_ERROR` 级别的 `FormattedRuntimeException`，字段级消息优先。

### BaseController

```java
public abstract class BaseController {
    public abstract String getBusinessName();           // 业务名称（日志/审计标识）
    public AjaxResult successResult(Object val);         // 成功响应（i18n 成功消息）
    public AjaxResult successResult();
    public AjaxResult errorResult(Throwable e);          // 通用错误消息 + 异常消息
}
```

> 注意：获取当前用户请使用 `AuthUtil.getInstance().getCurrentUser()`，`BaseController` 本身不提供该方法。

## 请求上下文与跨服务传播

### RequestDataContext / RequestContextHolder

`RequestDataContext`（可序列化）携带：`traceId`（格式 `timestamp-16位随机串`）、`startTime`、`clientIp`、`requestMethod`、`requestUri`、`controllerName`、`methodName`、`serviceName`，以及业务数据 Map（`put`/`get`/`get(key, type)`/`getOrDefault`/`remove`/`containsKey`/`elapsed()`）。

`RequestContextHolder` 基于 `InheritableThreadLocal`（子线程可继承），提供 `current()`/`get()`/`context()`（Optional）/`getOrCreate()`/`traceId()`/`put()`/`get()`/`clear()`。

### Dubbo 跨服务传播

`DubboRequestContextFilter` 是 Dubbo Filter（`@Activate(group = {CONSUMER, PROVIDER}, order = -10000)`）：

- **Consumer 端**：调用前将上下文 JSON 序列化 + Base64 编码放入 RpcContext attachments（键 `tbs.request.context`），同时附轻量键 `tbs.request.traceId`；调用后 finally 清理 attachments 防污染；
- **Provider 端**：提取并反序列化恢复到当前线程；仅有 traceId 时按 traceId 新建上下文；调用后 finally 还原之前的上下文。

配合 `WebConfig` 注册的两个 `ContextPropagator`（common 模块异步线程池传播 SPI）——`RequestContextPropagator` 与 `SecurityContextPropagator`（认证信息传播）。

## API 调用拦截器

`ApiInvokeAop`（`@Aspect`）拦截 `BaseController` 子类中带 `@RequestMapping/@GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping` 的方法，在启动时（`IStartup`）收集容器内全部 `IApiInvokeInterceptor`：

```java
public interface IApiInvokeInterceptor {
    default boolean supports(ApiInvokeContext context) { return true; }
    ApiInvokeContext beforeInvoke(ApiInvokeContext context);          // 前置（可修改上下文）
    void afterInvoke(ApiInvokeContext context, ValueWrapper<Object> result); // 后置
    void onException(ApiInvokeContext context, Throwable e);           // 异常（异常仍会继续抛出）
    // ApiInvokeContext { BaseController target; Method method; Object[] args; boolean supported; }
}
```

内置实现：

- `RequestContextInterceptor`：前置创建/恢复上下文并填充 IP、URI、方法名等；后置记录耗时日志；异常时把异常类名/消息写入上下文（`_exception`/`_errorMessage`）；
- `I18nInterceptor`：备份语言解析（主路径为 `I18nFilter`）。

拦截器自身抛出的异常会被 AOP 捕获并记 error，不影响业务调用。

## 国际化

- `I18nFilter`（`@Order(HIGHEST_PRECEDENCE + 10)`，先于 Spring Security 执行）：解析语言写入 `I18nContextHolder`，请求结束 finally 清理。解析优先级：`?lang=` / `?locale=` 参数 → `Accept-Language` 头 → `X-Lang` / `X-Locale` 头。
- `WebMessageSourceProvider`：注册 `messages-web` 资源（order=-200），中英文两份（`messages-web.properties` / `messages-web_en_US.properties`）。
- `WebI18nConstant`：集中定义全部 key（通用/认证/令牌/用户/泛化 API/枚举文案/日志分组）。注意 `common.success`、`common.error`、`common.unauthorized`、`common.forbidden` 定义于 common 模块，web 跨模块引用。

## 泛化 API 调用

包名 `com.tbs.web.genric`（拼写如此）。提供统一的"按 id/version 动态调用远程接口"能力：

- `AbstractGenricApiController`：暴露 `POST /invoke`，入参 `HttpInvokeForm`（`id`/`version`/`parameters`）。实例匹配优先级：**无版本实例 > 未指定版本的请求 > 版本精确一致**，命中即执行；无匹配返回 i18n 错误。
- `AbstractAuthGenricApiController`：仅承接 `needAuth=true` 的实例，当前用户取自 `AuthUtil`（配套还有匿名版本控制器抽象）。
- `AbstractGenricApiInstance<R extends Serializable>`：以 `apiId + apiVersion` 标识（equals/hashCode 亦基于此）的远程调用单元。`invokeApi` 统一编排：登录校验（needAuth 且无用户时抛 i18n 异常）→ 预认证链（`IApiInstancePreAuth` 列表）→ 参数按名索引并编号 → `invoke`（子类实现协议）→ **所有异常捕获后写入结果 VO，绝不向上抛**；同时记录起止时间与成功标志。
- 协议实现：`DubboGenricApiInstance`（Dubbo 泛化调用）、`HttpTriGenricApiInstance`（Triple HTTP）。
- 结果载体 `ApiInvokeResultVo`：`successFlag`/`exceptionClassName`/`exceptionMessage`/`exceptionObject`/`beginTime`/`endTime`。

```java
// 子类化示例（认证版）
@RestController
@RequestMapping("/open-api")
public class MyGenricController extends AbstractAuthGenricApiController {
    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) { ... }
}
```

## 其他组件

- **JacksonCustomConfig**：实现 `Jackson2ObjectMapperBuilderCustomizer`，`Long`/`long` 序列化为字符串（解决雪花 ID 前端精度丢失），反序列化兼容字符串与数字两种形式（`ToStringOrNumberDeserializer`）。
- **DubboConfig**：注册 `ServiceDiscovery`（`NacosServiceDiscovery`），地址取 `${dubbo.registry.address}`。
- **异常类**（`com.tbs.web.exception`）：`NoLoginException`（未登录）、`NotFoundTokenException`（令牌缺失）、`InvalidTokenException`（令牌非法）、`ReLoginRequestException`（需重新登录，转 405）、`SimpleAuthException`（通用认证异常）。

## 配置参考

### `tbs.web.*`（WebProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable-cors` | boolean | `true` | 启用 CORS（全放行） |
| `enable-csrf` | boolean | `false` | 启用 CSRF |
| `enable-anonymous` | boolean | `true` | **当前版本无消费点（未生效）** |
| `enable-form-login` | boolean | `false` | 启用表单登录（`/login` 页面） |
| `enable-http-basic` | boolean | `false` | 启用 HTTP Basic |
| `enable-logout` | boolean | `false` | 启用注销端点 |
| `secured-paths` | List&lt;String&gt; | `[/api/**]` | 需认证路径（Ant 风格） |
| `permit-all-paths` | List&lt;String&gt; | `[/, /login, /public/**]` | 放行路径 |

### `tbs.web.auth.*`（WebAuthProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable` | boolean | `true` | 关闭后不装配令牌拾取器/签发中心/处理过滤器 |
| `token-source` | TokenSource | `HEADER` | 令牌来源（HEADER/COOKIE/PARAM/BODY） |
| `token-filed-name` | String | `Authorization` | 令牌字段名（属性拼写源码如此） |
| `token-expire-time` | int | `3600` | 访问令牌有效期（秒） |
| `refresh-token-expire-time` | int | `168` | 刷新令牌有效期（小时，7 天） |
| `application-token-expire-time` | int | `168` | 应用令牌有效期（小时） |
| `token-picker-impl` | Class | `DefaultLoginTokenPicker` | 令牌拾取器实现类 |
| `authentication-center-impl` | Class | `SimpleAuthenticationCenter` | 认证中心实现类 |
| `token-issuance-center-impl` | Class | `NoImplTokenIssuanceCenter` | 签发中心实现类（**必须替换**） |
| `token-data-source-name` | String | `TOKEN` | 令牌数据源名称（供实现层使用） |
| `auth-manager-impl` | Class | `SpringSecurityContextHolderAuthManager` | 认证管理器实现类 |

### `tbs.result.*`（ResultProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable` | boolean | `true` | 是否装配统一响应组件 |
| `result-creator` | Class | `DefaultResultCreateor` | 结果创建者实现类 |
| `result-builder-provier` | Class | `SimpleBuilderProvider` | 构建器提供者实现类（属性拼写源码如此） |
| `error-result-builder-provier` | Class | `DefaultErrorAjaxProvider` | 错误结果提供者实现类 |

## 扩展指南

### 自定义网关中心

```java
public class ApiTokenGateWayCenter extends AbstractAntPathSupportGateWayCenter implements IStartup {
    private final ITokenPicker picker;
    public ApiTokenGateWayCenter(ITokenPicker picker) { this.picker = picker; }

    @Override public ITokenPicker getTokenPicker() { return picker; }
    @Override public void onNoneToken() { /* 应用令牌缺失时允许匿名 */ }
    @Override public void handleToken(ITokenModel model) { /* 写入应用级上下文 */ }
    @Override public boolean onErrorWillBroken(Throwable e) { return false; }
    @Override public void startUp() { setMatcher(List.of("/open/**")); }
}
```

注册为 Spring Bean 即会被 `TokenHandleFilter` 自动发现（按 `Ordered` 排序执行）。

### 自定义错误响应

```java
@Component
public class BizErrorProvider implements IOnErrorAjaxProvider<MyBizException> {
    @Override public AjaxResult onError(MyBizException e) {
        return AjaxUtil.getInstance().custom(5001, e.getMessage(), null);
    }
    @Override public boolean support(Class<? extends Throwable> clazz) {
        return MyBizException.class.isAssignableFrom(clazz);
    }
    @Override public int getOrder() { return 10; } // 先于兜底 DefaultErrorAjaxProvider
}
```

### 自定义结果包装策略

```java
public class PageResultCreator implements IResultCreator {
    @Override
    public IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder, Object val) {
        return builder.create()
            .appendField(AjaxResult.CODE_KEY, AjaxResult.DEFAULT_SUCCESS_CODE)
            .appendField(AjaxResult.MESSAGE_KEY, AjaxResult.getDefaultSuccessMessage())
            .appendField("page", val).next();
    }
}

// 使用：@Wrapped(PageResultCreator.class)
// 或配置切换全局默认：tbs.result.result-creator=com.xxx.PageResultCreator
```

### 自定义令牌拾取器

实现 `ITokenPicker` 后通过 `tbs.web.auth.token-picker-impl` 指定，或继承 `SimpleTokenPicker` 后在子类注入提取规则。

## 注意事项

1. **必须实现令牌签发中心**：默认 `NoImplTokenIssuanceCenter` 调用即抛异常（"请实现一个Token签发中心"）。登录闭环需 `ITokenIssuanceCenter` + `IUserInfoService` + `IUserLoginInfoService` 全套落地（或直接引入 `default-system-impl`）。
2. **`@EnableWebPack` 的 javadoc 与实际行为不符**：javadoc 称会开启 `@EnableWebSecurity` 与 `@EnableMethodSecurity`，但注解上并未标注（相关 import 未使用）。实际方法级安全由 `WebConfig` 上的 `@EnableMethodSecurity(prePostEnabled = true)` 开启；Web 安全配置在 Spring Boot 环境下由 Boot 自动配置兜底生效。
3. **`WebSecurityConfigurerAdapter` 已废弃**（Spring Security 5.7+），升级 Spring Boot 3 时需重写为 `SecurityFilterChain` Bean 风格。
4. **CORS 默认全放行**（`allowedOrigin(" * ")` 等），生产环境务必收紧。
5. **`tbs.web.enable-anonymous` 配置项无消费点**，配置后不产生任何效果。
6. **BODY 令牌来源有冲突风险**：`SimpleTokenPicker` 的 BODY 分支会读取并消费请求体流，与 `@RequestBody` 参数解析冲突；推荐 HEADER。
7. **响应包装缓存键不含参数签名**：`ResponseAjaxAspect` 以"类全名.方法名"缓存 `@Wrapped` 判定结果，同名重载方法共享缓存条目。
8. **拦截器异常隔离**：`IApiInvokeInterceptor` 抛出的异常会被 `ApiInvokeAop` 捕获记日志，不会阻断业务调用；但 `onException` 回调中抛出的异常同样被吞。
9. **泛化 API 实例异常不上抛**：`AbstractGenricApiInstance.invokeApi` 将全部异常写入 `ApiInvokeResultVo`（普通异常的 `exceptionObject` 为堆栈字符串），调用方需检查 `successFlag`。
10. **`DubboConfig` 混用两代 Dubbo 包**：同时引用 `com.alibaba.dubbo.common.URL` 与 `org.apache.dubbo.*`，依赖严格版本对齐，升级 Dubbo 时需注意。
11. **`TokenHandleFilter` 中各网关中心顺序**：多个中心按 `Ordered.getOrder()` 升序执行；`LoginGateWayCenter` 未覆写 getOrder（默认 `LOWEST_PRECEDENCE`），需要先行处理的中心应返回更小值。
12. **工具包目录名为 `uitl`**（拼写如此）：`AuthUtil`/`AjaxUtil`/`NetUtil` 均位于 `com.tbs.web.uitl`；`constant`（`WebI18nConstant`）与 `constants`（`WebBeanNameConstant`）是两个不同目录。
13. **`AbstractAntPathSupportGateWayCenter` 未设置 matcher 时 NPE**：`support()` 直接调用 `matcher.matches(request)`，子类务必在 `startUp()` 中完成 `setMatcher`，且路径列表不能为空集合。

## 包结构

```
com.tbs.web
├── annotation/            @EnableWebPack（启用入口）
├── aop/                   ApiInvokeAop（API 调用拦截切面）
├── config/                WebConfig（Security）、AuthConfig、TokenSetConfig、
│                          JacksonCustomConfig、DubboConfig、WebAutoRegister
│   └── property/          WebProperty / WebAuthProperty
├── constant/              WebI18nConstant（i18n key 常量）
├── constants/             WebBeanNameConstant（Bean 名常量）
├── context/               RequestDataContext / RequestContextHolder
│   └── propagator/        DubboRequestContextFilter（跨服务传播）
├── enums/                 TokenTypeEnum / TokenStatusEnum / TokenCreateTypeEnum /
│                          TokenSource / WebMessageTypeEnum
├── exception/             NoLogin / NotFoundToken / InvalidToken / ReLoginRequest / SimpleAuth
├── gate/                  TokenHandleFilter、ITokenPicker、I18nFilter
│   ├── center/            IGateWayCenter、AbstractAntPathSupportGateWayCenter
│   │   └── impl/          LoginGateWayCenter
│   └── intercept/         IApiInvokeInterceptor、RequestContextInterceptor、I18nInterceptor
├── genric/                泛化 API：控制器/实例/预认证/表单/VO
├── result/                AjaxResult、AjaxResultFluentBuilder、IAjaxResultBuilder、
│                          IResultCreator、IOnErrorAjaxProvider、IAjaxBuilderProvider
│   ├── annotation/        @Wrapped / @NoWrapped
│   ├── aop/               ResponseAjaxAspect（异常处理 + 响应包装）
│   ├── config/            ResultConfig（+ property/ResultProperty）
│   └── impl/              DefaultResultCreateor、SimpleBuilderProvider、
│                          DefaultErrorAjaxProvider、ReLoginErrorAjaxProvider
├── security/
│   ├── authentication/    IAuthManager、AbstractAuthManager、SecurityContextPropagator
│   │   ├── center/        IAuthenticationCenter、AbstractAuthenticationCenter（impl/ 简单与占位实现）
│   │   ├── impl/          SpringSecurityContextHolderAuthManager
│   │   └── model/         ITokenAuthentication、DefaultTokenAuthentication
│   ├── issuance/          ITokenIssuanceCenter、AbstractTokenIssuanceCenter（form/TokenRequestForm）
│   │   └── impl/          NoImplTokenIssuanceCenter
│   ├── impl/              TokenAuthenticationProvider、SimpleTokenPicker、DefaultLoginTokenPicker、
│   │                      DefaultAuthEntryPoint、DefaultAuthAccessDeniedHandler
│   ├── token/             ITokenModel、IRefreshTokenService、IRefreshTokenValidator、
│   │                      ITokenRemover（AbstractRefreshTokenService、DefaultTokenRemover）
│   └── user/              IUserInfoService、IUserLoginInfoService（AbstractUserLoginInfoService）、
│                          IPasswordEncrypt、IUserValidator
│       ├── access/        IUserAccessTokenProvider 体系（AbstractAccessTokenProvider + 两个默认实现）
│       ├── form/          IUsernameAndPasswordForm、IRefreshTokenForm
│       ├── impl/          NoEncryptPassword、SimplePasswordEncrypt、UserLoginCountValiadtor
│       └── vo/            LoginSessionVO
├── uitl/                   AuthUtil、AjaxUtil、NetUtil（目录名拼写如此）
└── BaseController          控制器基类（getBusinessName / successResult / errorResult）

resources/
└── messages-web.properties / messages-web_en_US.properties   （中英文 i18n 资源）
```
