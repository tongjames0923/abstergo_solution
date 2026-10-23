---
name: abstergo-dev-guideline
description: 在本项目中进行任何代码开发、修改或评审时遵循 DEVELOPMENT_GUIDELINE 开发指南，包括全局铁律、断言异常写法、i18n 国际化、注释规范、api/impl 模块拆分、统一响应、Vue 2 前端响应式适配（平板/手机）与前后端约定。Use when writing or modifying Java backend code, middleware modules, business modules, Vue 2 frontend code, responsive/mobile layouts, API interfaces, or reviewing code changes in this repository.
---

# Abstergo Solution 开发指南技能

本项目以 [DEVELOPMENT_GUIDELINE.md](../../../DEVELOPMENT_GUIDELINE.md) 为开发总纲。
**当 Guideline 与存量代码冲突时，以 Guideline 为准**（存量代码视为历史遗留）。

## 工作流程（每次开发任务必须遵循）

1. **先读规范**：按任务类型阅读对应分册（见下表），只需读与任务相关的分册。
2. **再读存量**：修改既有模块前，先读该模块 README 与相邻同类实现，保持风格一致。
3. **生成代码自查**：产出代码后，对照 [07-Code Review 检查清单](../../../docs/guideline/07-Code-Review检查清单.md) 逐项自检，重点是断言写法、i18n、注释格式三个高频违规点。
4. **不确定即询问**：涉及架构决策（新模块拆分方式、新中间件引入、接口契约变更）时，必须先向用户确认，不得擅自决定。
5. **不执行编译验证**：编译/构建验证（`mvn compile`、`npm run lint`/`npm run build` 等）一律由人工执行与确认，AI 不得代为运行，仅做静态自查；涉及编译结果的结论需标注"待人工验证"。

### 任务类型 → 分册映射

| 任务类型 | 必读分册 |
|----------|----------|
| 了解架构/模块地图/技术栈/命名表 | [01-项目总览](../../../docs/guideline/01-项目总览.md) |
| 中间件/工具模块开发 | [02-中间件模块开发规范](../../../docs/guideline/02-中间件模块开发规范.md) |
| 业务功能开发（实体/Mapper/Service/Controller） | [03-功能模块开发规范](../../../docs/guideline/03-功能模块开发规范.md) |
| 接口设计、错误码、权限、前后端对接 | [04-接口设计与上线规范](../../../docs/guideline/04-接口设计与上线规范.md) |
| Vue 2 前端开发（含平板/手机响应式适配） | [05-前端开发规范](../../../docs/guideline/05-前端开发规范.md)（5.8 响应式适配） |
| Jenkins/Nginx/Nacos/Docker 部署运维 | [06-部署与运维规范](../../../docs/guideline/06-部署与运维规范.md) |
| 代码评审 / 提交前自检 | [07-Code Review 检查清单](../../../docs/guideline/07-Code-Review检查清单.md) |

## 全局铁律（任何场景都必须遵守）

1. **语言策略**：后端代码统一使用 Java。项目已全面 Java 化（含 dic-service），禁止新增 Kotlin 代码与 Kotlin 依赖。
2. **注释规范**：类与函数必须使用块注释（含 `@param`/`@return`）；重要代码块加总结性块注释；**禁止行尾注释**；简单代码不加注释。
3. **断言与异常**：抛异常必须走 `AssertUtil` 链式断言：

   ```java
   AssertUtil.isTrue(条件).i18n("resource.key").params(...).errorLevel(...).throwIt()
   ```

   **禁止** `.message(...)`、禁止直接 `throw new FormattedRuntimeException(...)`。
4. **国际化（i18n）**：所有面向用户的文案（错误提示、业务名称、界面文案）禁止硬编码魔法值。后端走 i18n 资源 key，前端走 `$t()`；资源文件按 `messages-{module}.properties` / `messages-{module}_en_US.properties` 命名，中英文两份同步维护。
5. **配置管理**：配置项统一收拢到 `@ConfigurationProperties` 的 Property 类，**禁止**散落的 `@Value` 注入；模块必须提供 `tbs.{module}.enable` 总开关。
6. **职责分离**：Controller 只做 HTTP 接入、参数组装、调用服务层；**所有业务校验与规则判断在 Service 层**；Service 不感知 HTTP。
7. **模块拆分**：技术型模块必须拆分 `api`（契约，不含实现依赖）与 `impl`（实现）两个 Maven 子模块。
8. **统一响应**：所有 HTTP 接口返回 `AjaxResult`（code/message/data），错误码语义全局统一（200/401/403/405/468/500 等），禁止自定义结构。
9. **接口分区**：`/public/**` 免认证，`/private/**` 必须认证；私有管理接口统一放在 `controller/privates` 包下。
10. **评审前置**：任何 MR/提交在合并前必须通过 07 分册检查清单。
11. **方法改造兼容**：改造或重构公共契约方法（api 模块接口、Dubbo 接口、对外 Service 接口）时，禁止直接修改原方法签名或语义，必须在同一接口下新增方法承载新逻辑，并对原方法标注 `@Deprecated`（注释注明替代方法与计划移除版本），促进缓慢迁移、保持向前兼容；仅安全/严重缺陷或调用方已全量切换可例外（见 03 分册 3.9）。
12. **规范分包**：类按单一职责归入语义化命名的包；禁止平铺大包，也禁止无语义的单类包与无意义深层嵌套（见 02 分册 2.1.2）。
13. **缓存名称常量**：`@Cacheable`/`@CachePut`/`@CacheEvict` 的 `cacheNames`/`value` 必须引用模块内 `CacheNamesConstants` 常量，禁止硬编码字符串。
14. **存储层不承担业务规则**：唯一性、非空、默认值、外键与级联、状态合法性一律在业务代码中用断言校验，禁止依赖数据库的 `UNIQUE`/`NOT NULL`/`DEFAULT`/`FOREIGN KEY`/`ON DELETE CASCADE`/`CHECK`/触发器兜底；`@Table` 只声明 `@Index`；并发由业务层加锁或幂等键保证，主键生成策略除外（见 03 分册 3.3.3）。

## 后端高频要点速查

- 实体实现 `IBaseEntity<Long>`，含 `serialVersionUID` 与 Lombok 注解；删除语义走逻辑删除（flag + `@LogicDeleteEntity` + FlagEnum）。
- 实体只声明 `@Index`，不声明 `uniqueConstraints`/`nullable = false`/默认值/外键；唯一性与必填校验用 `AssertUtil` 在 Service 层断言。
- 标准 CRUD 继承 shared-api 服务模板；多步写操作加 `@Transactional(rollbackFor = Exception.class)`。
- Controller 注解组合完整：`@Tag`、`@Operation`、`@ApiResponse`、`@AjaxResponseType`、`@Parameter`；继承 `BaseController` 并覆写 `getBusinessName()`。
- 中间件模块自动配置带 `@ConditionalOnProperty(prefix="tbs.{module}", name="enable")` 开关；Property 类不加 `@Component`，由 Config 中 `@EnableConfigurationProperties` 激活。
- Dubbo：接口定义在 api 模块；消费端 `@DubboReference(lazy = true)`。

## 前端高频要点速查（Vue 2）

- HTTP 请求一律经 `@/utils/request`，禁止自建 axios 实例。
- 用户可见文案全部 `$t()`，中英文资源同步。
- 代码风格：无分号、单引号、2 空格缩进、prefer-const；`npm run lint` 必须通过。
- 新页面三要素：views 目录组件存在 + 后端菜单权限（type=1）登记 + component 路径正确。
- 响应式适配：档位只认 `utils/device.js` / `ResponsiveMixin`（isPhone/isTablet/isTouch），断点只认 `$bp-phone`/`$bp-tablet`；禁止自写 768/992 或监听 resize。
- 布局适配优先用 `@include respond-to()` / `responsive-container`；固定宽度加 `max-width: 100%`，多列网格与双栏在平板及以下降列/堆叠。
- 触摸设备没有 hover：行内操作按钮的 `opacity: 0` + `:hover` 必须配 `@media (hover: none)` 常显，否则功能不可达。
- 禁止 Vue 3 语法混入、禁止第二套 UI 框架、禁止新增 `console.log`/`debugger`。

## 评审输出格式

执行代码评审时，按 07 分册 7.11 的格式逐项输出「通过 / 不通过 / 不适用 + 证据（文件:行号）」：

```
## Review 结论：通过 / 需修改 / 打回

### [必须]/[禁止] 未通过项
1. 文件:行号 — 问题描述 — 依据条款（如 03 分册 3.4.2）

### [建议] 项
1. ...

### 风险提示
（兼容性、性能、数据安全等）
```
