---
name: oss-file-feature-migrate-to-system
overview: 将 app 模块中文件下载/文件处理相关功能（公开下载码整套、OSS 文件接口层、数据处理 handler 体系、应用包管理 Controller）迁移到 system 模块，按 system-api 契约 + default-system-impl 实现分层，HTTP 路径/表名/数据不变，i18n 中英文随迁。
todos:
  - id: closure-analysis
    content: 使用 [subagent:code-explorer] 梳理四块功能的完整文件清单与依赖闭包（Controller→Service→Mapper/Storage、Form/VO、i18n key、专属服务、pom 依赖），确认哪些服务可随迁哪些需留 app
    status: completed
  - id: system-api-contract
    content: 使用 [skill:abstergo-dev-guideline] 将契约层迁入 system-api：服务接口/实体/Mapper/Form/VO/Property/枚举/常量并入 SystemApiI18nConstant，pom 补 oss 等依赖
    status: completed
  - id: impl-migration
    content: 使用 [skill:abstergo-dev-guideline] 将实现层迁入 default-system-impl：ServiceImpl/Storage/Controller/Condition/持久化配置，保持 HTTP 路径与条件装配语义不变
    status: completed
  - id: app-cleanup
    content: 清理 app 模块：删除已迁出文件与 AppPersistenceConfig，AppI18nContant 移除迁出常量，messages-app 中英文删除迁出 key，app/pom 清理不再需要的依赖
    status: completed
  - id: i18n-system
    content: 在 default-system-impl 的 messages-system.properties 与 _en_US.properties 中按原 key 新增迁出文案（中英文同步）
    status: completed
  - id: doc-update
    content: 更新 app/README.md 与 system/README.md：新包结构、职责划分、tbs.oss.download-code.storage 配置说明
    status: completed
  - id: verify
    content: 编译验证 + 07 分册自检 + 启动冒烟（/private/oss/**、下载码 CRUD、storage=db 切换、公开下载链路、前端零改动确认）
    status: completed
---

## 需求概述
将 app 模块中与文件下载/文件处理相关的四块功能完整迁移至 system 模块，实现"业务应用层与文件处理能力"的分层解耦，迁移后功能不变、对外契约不变。

## 迁移范围（已确认全选）
1. **公开下载码整套**：downloadcode 契约包（IOssDownloadCodeService/IDownloadCodeStorage/OssDownloadCodeInfo）+ impl 包（OssDownloadCodeServiceImpl/RedisDownloadCodeStorage/DbDownloadCodeStorage）+ 两个 Controller（私有/公开）+ 实体/Mapper/枚举 + Property/StorageType/条件注解 + OssDownloadCodeForm
2. **OSS 文件接口层**：OssController（上传/下载/元数据/预签名，直接使用 com.tbs.oss.core.OssClient）+ 相关 Form/VO 及其专属服务
3. **数据处理/文件解析**：PrivateDataHandleController + app/data（TableDataHandlerRegistry、HandlerOptionVO、HandlerValidateVO）+ DATA_HANDLE 相关表单与 i18n
4. **应用包管理**：AppPackageController、AppVersionController（对应 service 已在 default-system-impl/service/impl/version）

## 兼容边界（已确认）
- HTTP 路径不变：/private/oss/**、/public/oss/download-code/download，前端 app-ui-vue 零改动
- 表名 oss_download_code 与数据不变，仅代码迁移
- oss.download.code.* 等相关文案从 messages-app 迁至 system 模块资源文件（中英文同步）

## 约束
- 迁移后 app 仅保留业务调用入口，不含文件处理实现；编译通过、无循环依赖；符合 api/impl 分层与分包命名约定；更新模块文档


## 目标分层与包映射（system 模块沿用 com.tbs.app 包名，天然被 app 组件扫描）

### system-api（契约层）
| 内容 | 目标位置 |
|------|----------|
| IOssDownloadCodeService、IDownloadCodeStorage、OssDownloadCodeInfo | com.tbs.app.service.oss.downloadcode |
| OssDownloadCodeEntity、OssDownloadCodeFlagEnum | com.tbs.app.entity、com.tbs.app.enums（或对齐 system-api 现有实体分包） |
| OssDownloadCodeMapper | com.tbs.app.mapper |
| OssDownloadCodeForm、OSS/数据处理/包管理相关 Form 与 VO | com.tbs.app.form、com.tbs.app.vo |
| OssDownloadCodeProperty、OssDownloadCodeStorageType | com.tbs.app.config.property（对齐 AppSettingProperty 位置） |
| TableDataHandlerRegistry 等被多模块注册的契约 | com.tbs.app.data（若 handler 由多模块注册则必须放 api 层） |
| AppI18nContant 中 OSS_*、OSS_DOWNLOAD_*、DATA_HANDLE_* 常量 | 并入 com.tbs.app.constant.SystemApiI18nConstant |

### default-system-impl（实现层）
| 内容 | 目标位置 |
|------|----------|
| OssDownloadCodeServiceImpl、RedisDownloadCodeStorage、DbDownloadCodeStorage | com.tbs.app.service.impl.oss.downloadcode（对齐现有 impl 分包） |
| OssDownloadCodeController、PublicOssDownloadCodeController、OssController、PrivateDataHandleController、AppPackageController、AppVersionController | com.tbs.app.controller.privates / publics（新建 controller 分包，扫描自动生效） |
| ConditionalOnDownloadCodeStorage、DownloadCodeStorageCondition | com.tbs.app.config.condition |
| 持久化配置（原 AppPersistenceConfig 职责） | default-system-impl 新配置类：@MapperScan("com.tbs.app.mapper") + @EntityScan("com.tbs.app.entity")，不重复注册 sqlLogicDeleteIntercepter（DefaultAppConfig 已有） |

## 关键技术决策
1. **依赖方向**：app 已依赖 system-api；default-system-impl 需新增对 oss 模块（OssClient）及数据处理所需模块（script-engine 等，执行时按 import 闭包确定）的 pom 依赖；严禁 system 反向依赖 app
2. **控制器注册**：system 模块类沿用 com.tbs.app 包名，被 @SpringBootApplication(scanBasePackages="com.tbs.app") 自动扫描，Controller 迁移无需额外注册
3. **依赖闭包梳理先行**：每个 Controller 迁移前必须梳理其 import 闭包（Service/Form/VO/工具类/i18n key/pom 依赖）；仅被迁移控制器使用的服务随之迁移，与 app 其他业务共享的服务留在 app 并由 system-api 契约解耦
4. **i18n 拆分**：AppI18nContant 按归属拆分常量；messages-app(.properties/_en_US.properties) 删除迁出 key，messages-system 同步新增（中英文），文案 key 本身不变
5. **兼容保证**：@RequestMapping 路径、AjaxResult 结构、表名、逻辑删除语义（@LogicDeleteEntity + SqlLogicDeleteIntercepter）、条件装配（storage=redis 缺省/db）全部不变
6. **文档**：更新 app/README.md 与 system/README.md 的模块职责与新包结构说明

```mermaid
flowchart LR
    subgraph app["app 模块（迁移后仅业务入口）"]
        A[业务 Controller 残留入口]
    end
    subgraph sapi["system-api（契约）"]
        C[服务接口/实体/Mapper/Form/VO/Property/常量]
    subgraph simp["default-system-impl（实现）"]
        S[ServiceImpl/Storage/Condition]
        K[Controller privates/publics]
    end
    A --> C
    C -.实现.- S
    S --> O[oss 模块 OssClient]
    S --> DB[(oss_download_code / Redis)]
```

## 性能与风险控制
- 纯代码迁移不改运行时行为；唯一运行时风险点是 Bean 装配（扫描包名一致、条件注解语义保持缺省 REDIS），迁移后需启动冒烟验证
- MapperScan/EntityScan 收敛到 default-system-impl 配置类，app 侧删除 AppPersistenceConfig，避免重复扫描
	

## Agent Extensions
### Skill
- **abstergo-dev-guideline**
  - Purpose: 迁移全程遵循 DEVELOPMENT_GUIDELINE（api/impl 拆分、断言/i18n/注释规范、Controller 职责、统一响应、接口分区）
  - Expected outcome: 迁移后代码符合 07 分册检查清单，i18n 中英文同步、常量归属清晰
### SubAgent
- **code-explorer**
  - Purpose: 迁移前梳理四块功能的完整依赖闭包（类清单、import 依赖、i18n key、pom 依赖、被共享的服务）
  - Expected outcome: 产出精确的迁移文件清单与 pom 变更清单，避免遗漏引用导致编译失败
