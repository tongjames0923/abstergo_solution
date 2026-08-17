---
name: doc-generator
description: 根据项目代码自动生成中文技术文档，包括 Java 后端 API/接口文档、Vue 2 前端组件文档、模块/架构文档。Use when the user asks to generate documentation, write API docs, document a module, create component docs, or when a new class/module/component is created and documentation is needed. Also auto-suggests documentation when detecting newly created Java controllers, services, Vue components, or Maven modules.
---

# 文档生成技能

根据项目代码自动生成结构化中文技术文档，覆盖三种文档类型。所有文档使用**中文**编写。

## 工作流程

### 1. 判断文档类型

| 目标代码 | 文档类型 | 触发场景 |
|----------|----------|----------|
| Controller / Service / Form / VO / Entity / Mapper | API 接口文档 | 用户要求为接口生成文档，或新建了 Controller/Service |
| `.vue` 组件文件 | 前端组件文档 | 用户要求为组件生成文档，或新建了 views/components 下的组件 |
| Maven 模块目录（含 pom.xml） | 模块/架构文档 | 用户要求为模块生成文档，或新建了模块 |

### 2. 读取代码并提取信息

按文档类型阅读对应规则（见下方），提取关键信息后生成文档。

### 3. 生成文档

按对应模板输出，详细模板参见 [templates.md](templates.md)。

### 4. 自检

- [ ] 文档语言为中文
- [ ] 所有代码引用与源文件一致
- [ ] 方法签名、参数类型、返回值准确无误
- [ ] Props/Events/Slots 与组件代码完全匹配
- [ ] 模块依赖关系与实际 pom.xml 一致

## API 接口文档生成规则

**目标**：从一个或多个 Controller 类提取完整的接口文档。

**提取步骤**：

1. 读取 Controller 类：提取 `@Tag`（接口分组名）、`@RequestMapping`（路径前缀）、`getBusinessName()`
2. 逐个方法提取：`@Operation`（summary/description）、HTTP 方法 + 路径、`@Parameter`（入参）、`@ApiResponse`（响应）、`@AjaxResponseType`（data 实际类型）
3. 读取关联 Form/VO：提取 `@Schema(description)` 和 `requiredMode`，列出字段清单
4. 读取 Service 接口：提取业务方法签名与 Javadoc 摘要

**输出结构**：

```
# {业务名称}接口文档

## 概述
- 控制器：{Controller 类名}
- 路径前缀：{RequestMapping}
- 认证要求：{private/public}

## 接口列表
### {接口名称}
- **URL**：`{METHOD} {完整路径}`
- **描述**：{description}
- **认证**：{权限要求}
- **请求参数**：（表格：字段名 | 类型 | 必填 | 说明）
- **响应结构**：（AjaxResult + data 实际类型说明）
- **错误码**：（相关 i18n key 及语义）

## 数据模型
### {Form/VO 名称}
（表格：字段名 | 类型 | 必填 | 说明）
```

**注意**：
- 响应结构一律为 `AjaxResult`（code/message/data），标注 `@AjaxResponseType` 中 data 的实际泛型类型
- 分页接口注明 `pageNum`/`pageSize` 为 query 参数
- 错误码从 Service 层的 `AssertUtil.i18n(key)` 提取，关联 `messages-{module}.properties` 中的中英文文案

## 前端组件文档生成规则

**目标**：从一个 Vue 2 单文件组件提取使用说明文档。

**提取步骤**：

1. 读取 `<template>`：提取组件结构、Element UI 组件使用、插槽位置
2. 读取 `<script>`：
   - `name`：组件名
   - `components`：子组件依赖
   - `props`：名称、类型、默认值、validator
   - `data()`：响应式数据
   - `computed`：计算属性
   - `methods`：方法签名与功能
   - `$emit`：事件名称与载荷
   - `watch`：侦听器
   - 生命周期钩子中的关键逻辑
3. 读取 `<style>`：scoped/全局、关键样式变量

**输出结构**：

```
# {组件名称}

## 概述
{组件用途简述}

## 使用方式
{基本引入与使用示例}

## Props
| 属性名 | 类型 | 默认值 | 必填 | 说明 |
|--------|------|--------|------|------|

## Events
| 事件名 | 参数 | 触发时机 |
|--------|------|----------|

## Slots
| 插槽名 | 作用域参数 | 说明 |
|--------|------------|------|

## 方法
| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|

## 依赖
- 子组件：{列表}
- API 调用：{列表}
- Vuex 模块：{列表}
```

**注意**：
- 如果组件使用了 `$t()` 国际化，列出涉及的 i18n key
- 如果组件使用了 `request` 调用 API，标注对应的 `src/api/{biz}.js` 函数

## 模块/架构文档生成规则

**目标**：从一个 Maven 模块（或聚合模块的子模块）提取架构说明文档。

**提取步骤**：

1. 读取 `pom.xml`：模块名、parent、依赖关系、子模块列表
2. 读取 `README.md`（如存在）：提取现有描述，在此基础上补充
3. 扫描包结构：`com.tbs.{module}/` 下的分包（annotation/api/config/model/impl 等）
4. 读取核心类：
   - `@Configuration` 类：Bean 定义、条件装配注解
   - `@ConfigurationProperties` 类：配置项清单
   - 核心接口/抽象类：契约说明
   - `@EnableTbs{Feature}` 注解：启用方式
   - `AbstractMessageSourceProvider`：i18n 配置
5. 扫描 `resources/`：配置文件、i18n 资源

**输出结构**：

```
# {模块名称}

## 概述
{模块定位与核心能力，2-3 句话}

## 模块结构
{目录树，标注各子模块/包的职责}

## 依赖关系
| 依赖模块 | 类型 | 用途 |
|----------|------|------|

## 配置项
| 配置键 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|

## 核心组件
### {接口/类名}
- **职责**：{一句话说明}
- **关键方法**：{方法签名列表}
- **使用方式**：{代码片段}

## 启用方式
{如何引入该模块：Maven 依赖 + 注解}

## i18n 资源
{模块国际化 key 清单，如有}
```

**注意**：
- 依赖方向必须标注（api → 被谁依赖，impl → 依赖哪些 api）
- 条件装配的开关属性（`tbs.{module}.enable`）必须列出
- 如果模块有 `@ConditionalOnMissingBean` 的兜底实现，需说明覆盖方式

## 自动建议触发条件

当检测到以下场景时，主动建议用户生成文档：

1. **新建 Java 类**：包含 `@RestController`、`@Service`、`@Entity` 注解 → 建议生成 API 文档
2. **新建 Vue 文件**：位于 `views/` 或 `components/` 目录 → 建议生成组件文档
3. **新建 Maven 模块**：检测到新 `pom.xml` 含 `<modules>` 或 `com.tbs.*` 包 → 建议生成模块文档
4. **批量新建**：完成一个完整功能（Controller + Service + Form + Vue 页面）后 → 建议生成完整 API 文档

建议话术示例：「检测到新建了 `{类名}`，是否需要为其生成{文档类型}？」

## 补充说明

- 生成的文档默认输出到对话中，用户可要求写入文件
- 如写入文件：API 文档放模块 `docs/` 目录或代码同级；组件文档放组件目录；模块文档放模块根目录 `README.md`
- 遵循项目注释规范：块注释格式，禁止行尾注释（参见 [01-项目总览](../../../docs/guideline/01-项目总览.md) 1.7.1）
- 详细输出模板与示例参见 [templates.md](templates.md)
