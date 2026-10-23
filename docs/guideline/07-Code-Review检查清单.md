# 07 - Code Review 检查清单

> 用途：提交合并请求（MR）前自检 + 评审人逐项核查。条目分三级：
> - **[必须]** 不通过即打回；
> - **[禁止]** 出现即打回；
> - **[建议]** 评审中提出，视情况采纳。
>
> AI 辅助评审时：按本清单逐项输出「通过 / 不通过 / 不适用 + 证据（文件/行号）」。

## 7.1 全局通用

| 级别 | 检查项 |
|------|--------|
| [必须] | 变更范围与需求一致，无夹带无关改动 |
| [必须] | 编译通过：后端 `mvn compile`、前端 `npm run lint` 无错误（**由人工执行与确认，AI 不代为运行**） |
| [必须] | 新增功能已自测（核心路径 + 至少一个错误分支） |
| [禁止] | 提交密钥、密码、token 等敏感信息（含配置文件、日志、注释） |
| [禁止] | 提交构建产物（target/、dist/、node_modules/）、IDE 本地文件 |
| [禁止] | 遗留调试代码：`System.out.println`、断点注释、前端 `debugger`/新增 `console.log` |
| [禁止] | AI 代为执行编译/构建验证命令（mvn compile、npm run lint/build 等，一律人工验证） |
| [建议] | 提交信息说明动机与影响面；大改动拆分多个语义独立提交 |

## 7.2 后端 - 语言、注释与包结构

| 级别 | 检查项 |
|------|--------|
| [必须] | 代码使用 Java（项目已全面 Java 化，禁止新增 Kotlin 代码与 Kotlin 依赖） |
| [必须] | 类与公共方法有块注释：功能说明 + `@param`/`@return`，含 `@author` |
| [必须] | 字段含义不直观时有块注释；枚举值/状态值注释 `@see` 对应枚举 |
| [必须] | 新增类落入职责匹配的包；新建模块按 02 分册 2.1.2 标准包结构与分包粒度分包（总纲铁律 12） |
| [禁止] | 行尾注释；为显而易见的代码写注释 |
| [禁止] | 包内类 ≥ 15 且存在多个职责聚类仍平铺不拆；新建无语义的单类包 |
| [建议] | 子包类数 ≥ 3、包层级 ≤ 3（annotation/exception 等天然单一职责小包除外）；包名全小写单词、语义化 |
| [建议] | 命名表意（Property/Config/EnableTbs/IXxxService/XxxForm/XxxVO/XxxEntity/XxxFlagEnum 符合 01 分册命名表） |

## 7.3 后端 - 断言、异常与 i18n（最高频打回点）

| 级别 | 检查项 |
|------|--------|
| [必须] | 所有业务校验使用 `AssertUtil.{断言}(条件).i18n("key").errorLevel(...).throwIt()` |
| [必须] | 新 i18n key 同时写入模块 `messages-{module}.properties` 中英文两份 |
| [必须] | 断言错误级别语义正确（USER_INPUT_ERROR / BUSINESS_LOGIC_ERROR / SYSTEM_ERROR / CRITICAL_SYSTEM_ERROR） |
| [禁止] | `.message(...)`、`.message(I18nUtil.get(key))` 组合 |
| [禁止] | 直接 `throw new FormattedRuntimeException/RuntimeException` |
| [禁止] | 代码中拼接中文/英文字面量作为用户可见消息 |
| [建议] | 带占位符的 key 使用 `.params(...)` 传参，文案占位 `{0}`/`{1}` |

## 7.4 后端 - 实体与数据访问

| 级别 | 检查项 |
|------|--------|
| [必须] | 实体实现 `IBaseEntity<Long>`，`serialVersionUID` 声明，Lombok 三注解齐全 |
| [必须] | 常用查询字段在 `@Table(indexes=...)` 声明索引；显式 `@Column` 蛇形映射 |
| [必须] | 删除语义走逻辑删除（flag + `@LogicDeleteEntity` + FlagEnum），物理删除有正当理由 |
| [必须] | `@LogicDeleteEntity` 实体未调用 `GenericMapper` 的 `select` / `selectCount` / `delete`（不注入 flag 过滤，走 QueryEngine） |
| [必须] | 调 `insertList` / `selectByIdList` / `deleteByIdList` 的写路径已加事务（分批执行非原子） |
| [必须] | 表结构变更脚本同步到 `etc/sql_file/` |
| [必须] | 唯一性/非空/默认值等约束在 Service 断言中表达，`@Table` 只声明 `@Index`，建表脚本只建普通索引（总纲铁律 14 / 03 分册 3.3.3） |
| [禁止] | 用 `uniqueConstraints`、`nullable = false`、`columnDefinition` 默认值、`@JoinColumn` 外键替代业务校验 |
| [禁止] | 建表脚本用 `UNIQUE KEY` / `NOT NULL` / `DEFAULT` / `FOREIGN KEY` / `ON DELETE CASCADE` / `CHECK` / 触发器承接业务规则（主键除外） |
| [禁止] | 字符串拼接 SQL；手写 limit 分页；绕过框架 new JdbcTemplate |
| [禁止] | 用 batch 单批方法（`insertBatch`/`selectByIdListBatch`/`deleteByIdListBatch`）直调业务，传入空集合会中断执行 |
| [建议] | 条件查询优先 QueryEngine 链式；复杂 JOIN 用 `@SelectSQL` 专用模型 |

## 7.5 后端 - Service 层

| 级别 | 检查项 |
|------|--------|
| [必须] | 标准 CRUD 继承 shared-api 服务模板，覆写 `getMapper()`/`newEntity()` |
| [必须] | 业务校验全部在 Service（Controller 无校验逻辑） |
| [必须] | 多步写操作加 `@Transactional(rollbackFor = Exception.class)` |
| [必须] | 缓存读写一致：`@Cacheable` 有对应 `@CacheEvict`，cacheNames 蛇形命名 |
| [必须] | 缓存注解 `cacheNames`/`value` 引用模块内 `CacheNamesConstants`（或 `CacheKeyConstants`）常量，禁止硬编码魔法值字符串（总纲铁律 13） |
| [必须] | 公共契约方法（api 接口/Dubbo 接口/对外 Service 接口）改造走「同接口新增方法 + 旧方法 `@Deprecated`」渐进迁移（总纲铁律 11 / 03 分册 3.9） |
| [必须] | 弃用方法同时具备 `@Deprecated` 注解 + 块注释 `@deprecated`，且注明替代方法与计划移除版本 |
| [禁止] | Service 感知 HTTP（request/response/AjaxResult） |
| [禁止] | 事务方法内调用 RPC/MQ 发送等不可回滚操作 |
| [禁止] | 直接修改公共契约方法签名/语义，或直接删除仍有调用方的公共方法（安全/严重缺陷、全量切换例外需在 MR 说明理由） |
| [建议] | 跨服务能力复用既有接口（IUserRemoteService 等），不重复造轮子 |

## 7.6 后端 - Controller 与接口

| 级别 | 检查项 |
|------|--------|
| [必须] | 路径与包位置一致：`/private/**`→`controller/privates`，`/public/**`→`controller/publics` |
| [必须] | 注解组合完整：`@Tag`、`@Operation`、`@ApiResponse`、`@AjaxResponseType`、`@Parameter` |
| [必须] | 私有接口至少 `@PreAuthorize("@preAuthUtils.accountActived")`，管理接口按需加权限 |
| [必须] | 返回统一 `successResult()/errorResult()`；继承 `BaseController` 并覆写 `getBusinessName()` |
| [必须] | Form/VO 字段 `@Schema` 描述齐全，必填标 REQUIRED |
| [必须] | 新增菜单/API 权限点已登记（type=1/2/3），前端 component 路径与 views 目录一致 |
| [禁止] | Controller 内写业务校验、直接访问 Mapper |
| [禁止] | 敏感字段（密码/token）出现在出参 VO/Form |
| [禁止] | 破坏性接口变更（改字段名/语义）未做兼容处理 |
| [建议] | 本地打开 Knife4j 验证文档渲染；接口契约同步给前端 |

## 7.7 后端 - 中间件/框架模块

| 级别 | 检查项 |
|------|--------|
| [必须] | api/impl 拆分正确：契约（接口/Property/模型）在 api，实现在 impl；api 不依赖中间件客户端 |
| [必须] | 模块内包结构符合 02 分册 2.1.2/2.1.2.1 分包粒度细则：按子域归包、无平铺大包、无过度拆分（总纲铁律 12） |
| [必须] | 新增配置绑定 `@ConfigurationProperties` Property 类（不加 `@Component`，Config 中 `@EnableConfigurationProperties` 激活），字段有默认值与块注释 |
| [必须] | 自动配置带 `@ConditionalOnProperty(prefix="tbs.{module}", name="enable")` 开关；同职责实现互斥装配 |
| [必须] | 模块文案走模块化 i18n（资源文件 + `AbstractMessageSourceProvider` 注册，order 错开） |
| [必须] | 存储类组件纯职责（doSave/doFetchDue/doRemove），调度与分发分离 |
| [禁止] | 零散 `@Value("${...}")` 注入；api 反向依赖 impl；双调度/双装配 |
| [禁止] | 业务代码重复 declare 交换机/队列；自建 RedisTemplate/锁用法绕过框架抽象 |
| [建议] | 提供 Noop/内存兜底实现保证未配置中间件可启动；扩展点面向接口 + 配置驱动替换 |
| [建议] | 模块 README 同步更新（概述/快速开始/配置说明/核心组件） |

## 7.8 后端 - Dubbo

| 级别 | 检查项 |
|------|--------|
| [必须] | 服务接口定义在 api 模块，参数/返回值可序列化 |
| [必须] | 消费端 `@DubboReference(lazy = true)`；提供方 `@DubboService` |
| [禁止] | 自建 HTTP 调用替代 Dubbo；接口传输不可序列化对象 |

## 7.9 前端

| 级别 | 检查项 |
|------|--------|
| [必须] | `npm run lint` 通过（无分号/单引号/2 空格/prefer-const） |
| [必须] | HTTP 请求一律经 `@/utils/request`，函数有完整 JSDoc |
| [必须] | 用户可见文案全部 `$t()`，中英文资源同步 |
| [必须] | 新页面：views 目录组件存在 + 后端菜单权限（type=1）登记 + component 路径正确 |
| [必须] | URL/参数结构与后端契约一致（分页 pageNum/pageSize 走 query） |
| [必须] | 错误分支处理验证（成功提示、失败展示 res.message、401/403 行为） |
| [必须] | 新增/修改页面在 375 / 768 / 820 / 992 / 1440 五档无横向溢出、无内容被裁切（05 分册 5.8） |
| [必须] | 固定宽度容器有 `max-width: 100%`；多列网格与左右双栏在平板及以下降列或堆叠 |
| [必须] | 依赖 `:hover` 显示的操作入口，在 `@media (hover: none)` 下常显（否则触摸设备不可达） |
| [禁止] | 组件内写死 768/992 断点或自行监听 resize 判断设备（应走 `utils/device.js` / `ResponsiveMixin`） |
| [禁止] | 重复实现 `element-ui.scss` 已覆盖的全局兜底（对话框/抽屉/表单标签堆叠/表格横滚/固定列隐藏等） |
| [禁止] | 绕过 request.js 自建 axios；硬编码后端地址；硬编码业务菜单路由 |
| [禁止] | Vue 3 语法混入；引入第二套 UI 框架；提交新增 console.log/debugger |
| [禁止] | 页面内自行实现登录态/权限判断（应走 permission.js 与 utils/permission） |
| [建议] | 删除/危险操作有二次确认；列表页遵循 查询表单+表格+分页 标准结构 |
| [建议] | 列表优先 `DynamicTable`（手机自动切卡片）；`el-descriptions` 手机档降为 1 列 |
| [建议] | 样式使用 styles 变量；组件声明 PascalCase name；scoped 样式 |

## 7.10 配置与发布

| 级别 | 检查项 |
|------|--------|
| [必须] | 新配置项：Property 类字段 → Nacos 录入 → 文档（01/06 分册）一致 |
| [必须] | 错误码变更三处同步：后端 AjaxResult、前端 settings.js、04 分册码表 |
| [必须] | 生产 Knife4j 关闭、debug 级错误码不暴露内部信息 |
| [禁止] | 生产沿用 compose 默认密码；配置只改一端（前后端/Nacos 不一致） |
| [建议] | 按 06 分册发布检查单执行发布与回滚预案 |

## 7.11 评审输出格式（供人工与 AI 统一）

```
## Review 结论：通过 / 需修改 / 打回

### [必须]/[禁止] 未通过项
1. 文件:行号 — 问题描述 — 依据条款（如 03 分册 3.4.2）

### [建议] 项
1. ...

### 风险提示
（兼容性、性能、数据安全等）
```

## 变更记录

| 版本 | 时间 | 说明 |
|------|------|------|
| 1.5.0 | 2026-10 | 7.9 新增响应式检查项 6 条：五档宽度无溢出、固定宽度/多列/双栏适配、hover-only 操作常显、禁止自建断点与 resize 判定、禁止重复实现全局兜底（配合 05 分册 5.8） |
| 1.4.0 | 2026-10 | 7.4 新增「不依赖数据库能力」三条检查项：实体不声明约束、建表脚本不用 UNIQUE/NOT NULL/DEFAULT/外键/级联/CHECK 承接业务规则（配合总纲铁律 14 / 03 分册 3.3.3） |
| 1.3.1 | 2026-10 | 7.2 语言检查项更新：项目已全面 Java 化，禁止新增 Kotlin 代码与 Kotlin 依赖 |
| 1.3.0 | 2026-08 | 7.5 新增缓存名称常量检查项（配合总纲铁律 13） |
| 1.2.0 | 2026-08 | 7.2 扩展为「语言、注释与包结构」并新增分包检查项；7.7 新增模块包结构粒度检查（配合总纲铁律 12 / 02 分册 2.1.2.1） |
| 1.1.1 | 2026-08 | 7.1 明确编译验证由人工执行与确认，新增禁止 AI 代为运行编译/构建命令 |
| 1.1.0 | 2026-08 | 7.5 新增公共契约方法改造兼容性检查项（配合总纲铁律 11 / 03 分册 3.9） |
| 1.0.0 | 2026-08 | 初版 |
