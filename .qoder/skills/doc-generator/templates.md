# 文档生成详细模板

本文件包含三种文档类型的完整输出模板，供生成文档时参照。

## 一、API 接口文档模板

```markdown
# {业务名称}接口文档

## 概述

| 项目 | 说明 |
|------|------|
| 控制器 | `{ControllerClassName}` |
| 路径前缀 | `{RequestMapping路径}` |
| 认证要求 | 私有接口（需认证） / 公共接口（免认证） |
| 业务描述 | {一句话业务说明} |

## 接口清单

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|----------|------|------|------|
| 1 | {方法名} | POST/GET | {完整路径} | {summary} |

---

### 1. {接口名称}

- **URL**：`POST {完整路径}`
- **描述**：{description}
- **权限**：`@PreAuthorize("{表达式}")`
- **请求参数**（Request Body）：

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| {fieldName} | {Java类型} | 是/否 | {description} | {example} |

- **响应结构**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { @AjaxResponseType 声明的实际类型 }
}
```

- **data 字段说明**（当 data 为对象/列表时）：

| 字段名 | 类型 | 说明 |
|--------|------|------|

- **业务错误**：

| i18n Key | 触发条件 | 中文文案 | 英文文案 |
|----------|----------|----------|----------|
| {key} | {条件} | {中文} | {English} |

---

## 数据模型

### {FormName}

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|

### {VOName}

| 字段名 | 类型 | 说明 |
|--------|------|------|

### {EntityName}

| 字段名 | 类型 | 数据库列 | 说明 |
|--------|------|----------|------|

## 关联资源

- 服务接口：`I{Name}Service`（{api 模块路径}）
- 国际化资源：`messages-{module}.properties`
- 前端 API 封装：`src/api/{biz}.js`
```

### API 文档示例（数据字典）

```markdown
# 数据字典管理接口文档

## 概述

| 项目 | 说明 |
|------|------|
| 控制器 | `SystemDataDicController` |
| 路径前缀 | `private/admin/dic` |
| 认证要求 | 私有接口（需认证 + 账户激活） |
| 业务描述 | 管理系统数据字典及字典项的增删改查与状态切换 |

## 接口清单

| 序号 | 接口名称 | 方法 | 路径 | 说明 |
|------|----------|------|------|------|
| 1 | enableDic | POST | /private/admin/dic/enableDic | 启用数据字典 |
| 2 | disableDic | POST | /private/admin/dic/disableDic | 禁用数据字典 |

---

### 1. 启用数据字典

- **URL**：`POST /private/admin/dic/enableDic`
- **描述**：根据字典编码启用对应的数据字典
- **权限**：`@PreAuthorize("@preAuthUtils.accountActived")`
- **请求参数**（Request Body）：

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| dicCode | String | 是 | 字典编码 | "SYS_STATUS" |

- **响应结构**：

```json
{ "code": 200, "message": "操作成功", "data": null }
```

- **业务错误**：

| i18n Key | 触发条件 | 中文文案 | 英文文案 |
|----------|----------|----------|----------|
| dic.code.empty | 字典编码为空 | 字典编码不能为空 | Dic code cannot be empty |
| dic.not.exists | 字典不存在 | 字典 {0} 不存在 | Dictionary {0} does not exist |
```

---

## 二、前端组件文档模板

```markdown
# {组件名称}

## 概述

{组件用途，1-3 句话}

## 使用方式

```vue
<!-- 基本用法 -->
<{component-name}
  :prop-a="value"
  @event-b="handler"
/>
```

## Props

| 属性名 | 类型 | 默认值 | 必填 | 说明 |
|--------|------|--------|------|------|
| {propName} | {type} | {default} | 是/否 | {description} |

## Events

| 事件名 | 参数 | 触发时机 |
|--------|------|----------|
| {eventName} | {payload描述} | {何时触发} |

## Slots

| 插槽名 | 作用域参数 | 说明 |
|--------|------------|------|
| {slotName} | {scope描述} | {用途} |
| default | — | {默认插槽说明} |

## 方法（供父组件调用）

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|

## 内部状态

| 数据名 | 类型 | 初始值 | 说明 |
|--------|------|--------|------|

## 依赖

- **子组件**：{列表}
- **API 调用**：`src/api/{biz}.js` 中的 {函数名}
- **Vuex 模块**：{模块名}（如使用）
- **i18n Keys**：`{key1}`, `{key2}`, ...

## 样式说明

- scoped：是/否
- 关键 CSS 变量/类名：{列表}
```

---

## 三、模块/架构文档模板

```markdown
# {模块名称}

## 概述

{模块定位，核心能力，2-3 句话}

## 模块结构

```
{module-name}/
├── pom.xml                    聚合 POM
├── {module}-api/              契约层
│   └── src/main/java/com/tbs/{module}/
│       ├── annotation/        启用注解
│       ├── api/               核心接口与抽象类
│       ├── model/             数据模型
│       └── config/property/   配置属性类
└── {module}-impl/             实现层
    └── src/main/java/com/tbs/{module}/
        ├── config/            自动配置类
        └── impl/              默认/兜底实现
```

## 依赖关系

| 模块 | 方向 | 说明 |
|------|------|------|
| common | api → common | 通用工具依赖 |
| shared-pojo | api → shared | 基础实体/枚举 |
| {具体中间件} | impl → {中间件} | 实现层依赖 |

```
依赖方向图：
业务模块 ──► impl ──► api ──► common/shared
```

## 配置项

| 配置键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| tbs.{module}.enable | Boolean | false | 模块总开关 |
| tbs.{module}.{param} | {Type} | {default} | {说明} |

## 核心组件

### {接口名}（`I{Name}`）

- **职责**：{一句话}
- **所在包**：`com.tbs.{module}.api`
- **关键方法**：

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|

### {实现类名}

- **职责**：{一句话}
- **条件装配**：`@ConditionalOnProperty(prefix = "tbs.{module}", name = "enable")`
- **覆盖方式**：`@ConditionalOnMissingBean`，业务方可自定义实现替代

### {启用注解}

```java
@EnableTbs{Feature}  // 在启动类或配置类上添加
```

## 启用方式

### Maven 依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>{module}-impl</artifactId>
</dependency>
```

### 代码启用

```java
@EnableTbs{Feature}
@SpringBootApplication
public class Application { ... }
```

### Nacos 配置

```yaml
tbs:
  {module}:
    enable: true
    {param}: {value}
```

## i18n 资源

| Key | 中文 | 英文 |
|-----|------|------|
| {module}.{key} | {中文} | {English} |

资源文件位置：`{module}-impl/src/main/resources/i18n/messages-{module}.properties`

## 故障排除

| 现象 | 可能原因 | 解决方案 |
|------|----------|----------|
| {问题} | {原因} | {方案} |
```

---

## 四、文档输出位置建议

| 文档类型 | 建议路径 | 说明 |
|----------|----------|------|
| API 接口文档 | 模块 `docs/` 或 Controller 同级 | 按业务域命名，如 `数据字典接口文档.md` |
| 前端组件文档 | 组件目录同级 | 如 `src/views/dic/README.md` |
| 模块/架构文档 | 模块根目录 `README.md` | 覆盖或补充现有 README |
