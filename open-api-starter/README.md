# Open API Starter 模块 - API 文档

## 模块概述

Open API Starter 模块（`com.tbs:open-api-starter`）是基于 **Knife4j 4.4.0**（`knife4j-openapi3-spring-boot-starter`，内置 Springdoc OpenAPI）的 API 文档增强包，为平台提供：

- **文档元信息装配**：标题/描述/许可证/服务器列表等 OpenAPI Info 配置，启动时自动打印文档访问地址
- **Ajax 统一响应 Schema**：`@AjaxResponseType` 注解 + 双阶段 Customizer，为 `AjaxResult` 风格的接口在文档中生成准确的 `{ code, message, data }` 响应结构（解决泛型擦除导致的 data 字段类型丢失）

模块为单一 Maven 模块，仅 7 个源文件，依赖 `com.tbs:common` 与 Knife4j starter。

> 注意：模块**没有** `spring.factories` 自动配置文件，不是传统意义上的 Spring Boot Starter——必须通过 `@UseKnife4jPack` 注解启用。

## 模块结构

```
open-api-starter/
└── src/main/java/com/tbs/knife4j/
    ├── annotation/
    │   ├── UseKnife4jPack.java          # 启用注解（@Import Knife4jAutoRegister）
    │   └── AjaxResponseType.java        # Ajax 响应 data 类型标注
    └── config/
        ├── Knife4jAutoRegister.java     # 自动注册器（Feature.KNIFE4J，扫描 com.tbs.knife4j）
        ├── Knife4jConfig.java           # OpenAPI Info Bean 装配
        ├── Knife4jProperty.java         # 配置属性（tbs.knife4j）
        ├── AjaxResponseTypeOperationCustomizer.java  # 阶段一：注解收集
        └── AjaxResponseTypeOpenApiCustomizer.java    # 阶段二：Schema 替换
```

## 快速开始

### 1. 依赖引入

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>open-api-starter</artifactId>
</dependency>
```

### 2. 启用

在启动类或配置类上标注（通常由平台其他启动器间接引入）：

```java
@SpringBootApplication
@EnableCommonPack
@UseKnife4jPack   // @Import(Knife4jAutoRegister)：扫描 com.tbs.knife4j 包，注册 Feature.KNIFE4J
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 可选配置

```yaml
tbs:
  knife4j:
    enable: true                  # 是否装配 OpenAPI Info Bean，默认 true（matchIfMissing）
    show-url: true                # 启动时打印文档访问地址，默认 true
    title: 平台后端 API 文档       # 文档标题
    description: 平台后端 API 文档 # 文档描述
    license: Apache 2.0           # 许可证名称
    license-url: http://springdoc.org
    servers:                      # 服务器地址列表（OpenAPI servers）
      - http://localhost:8080
```

### 4. 访问文档

应用启动后（`show-url: true` 时控制台会直接打印地址）：

- **Knife4j 增强界面**：`http://127.0.0.1:{port}{context-path}/doc.html`
- **Swagger UI**：`http://127.0.0.1:{port}{context-path}/swagger-ui/index.html`
- **OpenAPI JSON**：`http://127.0.0.1:{port}{context-path}/v3/api-docs`

## 核心组件

### 1. Knife4jConfig - OpenAPI Info 装配

`@ConditionalOnProperty(tbs.knife4j.enable, havingValue="true", matchIfMissing=true)` 条件化创建 `OpenAPI` Bean：

- **title / description**：未配置时默认"TBS 后端服务 API 文档"；
- **license**：未配置时默认 `Apache 2.0`（`http://springdoc.org`）；
- **servers**：配置了 `tbs.knife4j.servers` 时逐项写入 OpenAPI servers；
- **show-url**：启动时打印访问地址——配置了 `servers` 时打印第一项，否则按 `server.port`（默认 8080）与 `server.servlet.context-path`（默认空）拼出 `/doc.html` 与 `/swagger-ui/index.html` 两个地址。

### 2. Knife4jProperty - 配置属性

`@ConfigurationProperties(prefix = "tbs.knife4j")`，全部属性与默认值：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enable` | boolean | `true` | 是否装配 OpenAPI Info Bean |
| `show-url` | boolean | `true` | 启动时打印文档访问地址 |
| `title` | String | `TBS 后端服务 API 文档` | 文档标题 |
| `description` | String | `TBS 后端服务 API 文档` | 文档描述 |
| `version` | String | `v1.0.0` | 文档版本（见下方实现说明） |
| `license` | String | `Apache 2.0` | 许可证名称 |
| `license-url` | String | `http://springdoc.org` | 许可证 URL（仅 license 非空时生效） |
| `external-docs-description` | String | `TBS 解决方案文档` | 外部文档描述（**当前实现未应用**） |
| `external-docs-url` | String | `https://github.com/abstergo/abstergo-solution` | 外部文档 URL（**当前实现未应用**） |
| `servers` | List\<String\> | null | 服务器地址列表 |

**当前实现的行为细节**（以 `Knife4jConfig` 源码为准）：

- `version` 配置实际不生效：Info 构建末尾存在无条件覆盖 `info.version("v1.0.0")`，任何自定义版本都会被改写为 `v1.0.0`；
- `external-docs-description` / `external-docs-url` 属性已定义但装配逻辑未消费（不会写入 OpenAPI externalDocs）；
- `license-url` 仅在 `license` 非空（走自定义分支）时生效，默认分支固定使用 `http://springdoc.org`。

### 3. @AjaxResponseType - Ajax 统一响应 Schema

平台接口统一返回 `AjaxResult{ code, message, data }` 包装结构，`data` 为 `Object` 泛型时 Springdoc 无法推导实际类型。本注解标注在**控制器方法**上，通过 Class 引用规避泛型擦除：

```java
// 简单对象：data 为 DicModel
@AjaxResponseType(DicModel.class)
@PostMapping("/query")
public AjaxResult query(@RequestBody DicQuery query) { ... }

// 集合类型：data 为 DicModel 数组
@AjaxResponseType(value = List.class, genericArgs = {DicModel.class})
@GetMapping("/listAll")
public AjaxResult listAll() { ... }

// Map 类型：data 为 key=String、value=DicModel 的对象
@AjaxResponseType(value = Map.class, genericArgs = {String.class, DicModel.class})
@GetMapping("/mapAll")
public AjaxResult mapAll() { ... }

// 无 data 返回（纯操作类接口）：响应仅含 code 与 message
@AjaxResponseType(void.class)
@PostMapping("/enable/{id}")
public AjaxResult enable(@PathVariable Long id) { ... }
```

**双阶段定制机制**（解决 `OperationCustomizer` 的修改被 Springdoc 后续处理覆盖的问题）：

1. **收集阶段**（`AjaxResponseTypeOperationCustomizer`，实现 `OperationCustomizer`）：Springdoc 构建每个 Operation 时读取方法上的 `@AjaxResponseType`，以 `类全限定名.方法名` 为键缓存注解，并向 Operation 写入 `x-ajax-response-type-method` 扩展标记；
2. **应用阶段**（`AjaxResponseTypeOpenApiCustomizer`，实现 `OpenApiCustomiser`）：全部 Operation 构建完成后遍历 Spec 的 GET/POST/PUT/DELETE/PATCH 操作，按扩展标记取回注解，构建 `{ code(integer, 响应状态码, 示例 200), message(string, 响应消息), data?(响应数据) }` 包装 Schema，替换/创建该操作 `200` 响应的 `application/json` Schema（保留原 description）。

data Schema 构建规则：

| 注解 value | 生成的 data Schema |
|------------|---------------------|
| `void.class` / `Void.class` | 无 data 字段 |
| `List`/`Set`/`Collection` 及其子类 | `array`，items 为 `genericArgs[0]` 的 `$ref`（未指定时为 object） |
| `Map` 及其子类 | `map`，additionalProperties 为 `genericArgs[1]` 的 `$ref`（未指定时为 object） |
| 其他类型 | `#/components/schemas/{SimpleName}` 引用 |

> `$ref` 指向 components/schemas 中 Springdoc 已注册的定义，因此被引用的 VO 类需在至少一个接口的签名中出现（或被 `@Schema` 扫描），否则文档中无对应定义。

## 使用注意事项

1. **必须注解启用**：无 spring.factories，未标注 `@UseKnife4jPack` 时仅 `Knife4jProperty`（`@Component`）会被组件扫描注册，`OpenAPI` Bean 与 Ajax Schema 定制均不生效（除非扫描路径恰好覆盖 `com.tbs.knife4j`）
2. **version / externalDocs 配置暂不生效**：见上文"当前实现的行为细节"，如需自定义版本需修改 `Knife4jConfig`
3. **`@AjaxResponseType` 作用于 200 响应**：仅替换 `200` 响应的 `application/json` Schema，错误响应等其他状态码不受影响
4. **方法重载需谨慎**：注解缓存键为 `类名.方法名`（不含参数类型），同名重载方法上的注解会相互覆盖
5. **tbs.knife4j.enable=false 的效果**：仅关闭 `OpenAPI` Info Bean 与地址打印，Springdoc/Knife4j 的 UI 端点与 `@AjaxResponseType` 定制器（`@Component`）仍会生效；生产环境如需彻底关闭文档，应通过 Springdoc 的 `springdoc.api-docs.enabled=false` 与 knife4j 的生产屏蔽配置实现

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

## 相关链接

- [Knife4j 官方文档](https://doc.xiaominfo.com/)
- [Springdoc OpenAPI 官方文档](https://springdoc.org/)
- [OpenAPI 规范](https://swagger.io/specification/)
