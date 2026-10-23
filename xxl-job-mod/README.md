# xxl-job-mod 模块（artifactId：xxl-job）

基于 XXL-Job 的分布式任务调度集成模块：提供 XXL-Job 执行器的配置与装配、JSON 通用任务执行器（动态发现 `IJsonJobHandler` 处理器）、后台任务批量执行器，以及作为 **async 模块任务调度内核的外部驱动触发器**。

## 目录

- [模块定位](#模块定位)
- [依赖说明](#依赖说明)
- [快速开始](#快速开始)
- [配置参考](#配置参考)
- [JSON 任务执行器](#json-任务执行器)
- [后台任务执行器](#后台任务执行器)
- [async 任务调度外部驱动](#async-任务调度外部驱动)
- [在 XXL-Job 控制台配置任务](#在-xxl-job-控制台配置任务)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 基础层；桥接 XXL-Job 调度中心与 async 任务调度内核 |
| 核心能力 | 执行器装配、JSON 任务分发、后台任务批量执行、内核外部驱动 |
| XXL-Job | `com.xuxueli:xxl-job-core` 2.4.0 |
| 启用方式 | `@UseXxlJob` 注解（无 `spring.factories`） |

## 依赖说明

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>xxl-job</artifactId>
</dependency>
```

直接依赖：`xxl-job-core`、`common`（Feature/SpringUtil/AssertUtil/IBackgroundJob）、`async`（async 为**可选**依赖，用于 `XxlJobScheduleTrigger` 对接其调度内核）。

> `IBackgroundJob` 位于 `common`（`com.tbs.common.delegate`），非 mq-api；根 pom 中遗留的 `mq-api` 依赖已无代码引用，可随依赖清理一并移除。

> `xxl-job` 即 xxl-job-mod 的 `artifactId`，可直接作为依赖坐标。

## 快速开始

### 1. 启动类标注注解

```java
@SpringBootApplication
@UseXxlJob      // @Import(XxlAutoRegister) + @EnableCommonPack；XxlAutoRegister 扫描 com.tbs.xxl，注册 Feature.XXL_JOB
public class Application { ... }
```

`XxlAutoRegister` 继承 `AbstractAutoBeanRegister`，具备重复 `@UseXxlJob` 去重能力。

### 2. 配置执行器

`XxlJobConfig` 在满足以下条件时自动装配执行器与 JSON 执行器（非自定义 `IXXLJobsConfig` 时从 `tbs.xxl-job.config.*` 读取）：

```yaml
tbs:
  xxl-job:
    enable: true                     # 总开关（必须有，默认关闭）
    enable-json-job-executor: true   # 是否启用 JSON 任务执行器
    config:
      admin-address: http://127.0.0.1:9898/xxl-job-admin   # 管理地址（必填，缺失启动抛异常）
      app-name: your-app             # 执行器应用名（可空）
      address: ""                    # 执行器地址（可空）
      ip: ""                         # 执行器 IP（可空）
      port: 9999                     # 执行器端口（可空）
      access-token: your-token       # 调度中心访问令牌（可空）
      log-path: /data/applogs/xxl-job/jobhandler   # 执行器日志路径（可空，自动建目录）
      log-retentions-days: 30        # 日志保存天数（可空）
```

### 3. 定义 JSON 任务处理器

```java
@Component
public class SampleJobHandler implements IJsonJobHandler<SampleParams> {
    @Override public Class<SampleParams> classType() { return SampleParams.class; }
    @Override public String help() { return "示例任务，处理批量数据"; }
    @Override public String handle(SampleParams params) throws Exception {
        // 业务逻辑
        return "处理完成，条数=" + params.getData().size();
    }
}

@Data public class SampleParams {
    private String taskName;
    private List<String> data;
}
```

### 4. 自定义 `IXXLJobsConfig`

若已存在 `IXXLJobsConfig` Bean，配置类不再生成默认实现（`@ConditionalOnMissingBean`），全部从 Bean 读取：

```java
@Bean
public IXXLJobsConfig xxlJobsConfig() {
    return new IXXLJobsConfig() {
        @Override public String adminAddress() { return "http://127.0.0.1:9898/xxl-job-admin"; }
        @Override public String appName() { return "your-app"; }
        @Override public String address() { return ""; }
        @Override public String ip() { return ""; }
        @Override public Integer port() { return null; }
        @Override public String accessToken() { return ""; }
        @Override public String logPath() { return "/data/applogs/xxl-job/jobhandler"; }
        @Override public Integer logRetentionsDays() { return 30; }
    };
}
```

## 配置参考

### `tbs.xxl-job.*`（XxlJobProperty，`@Component` 自动注册）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable` | boolean | `false` | 执行器总开关；`true` 才装配 `XxlJobSpringExecutor` 与 `JsonJobExecutor` |
| `enable-json-job-executor` | boolean | `false` | JSON 任务执行器开关（还需 enable=true 生效） |
| `config.admin-address` | String | - | 调度中心地址（**必填**，缺失由 `AssertUtil.hasText` 抛异常） |
| `config.app-name` / `config.address` / `config.ip` / `config.port` | 见说明 | 空/空/空/null | 执行器网络与应用标识，非空才 set 到执行器 |
| `config.access-token` | String | - | 访问令牌，非空才 set |
| `config.log-path` | String | - | 日志路径，非空才 set，**缺失目录自动 mkdirs** |
| `config.log-retentions-days` | Integer | - | 日志保留天数，非空才 set |

装配条件小结：

| Bean | 条件 |
| --- | --- |
| `xxlJobsConfig`（默认 IXXLJobsConfig 实现） | 容器无该 Bean + `tbs.xxl-job.enable=true` |
| `xxlJobSpringExecutor` | 存在 `IXXLJobsConfig` Bean + `enable=true` |
| `runningBackGroundExecutor` | 容器无 `RunningBackGroundExecutor` Bean（无条件属性开关） |
| `jsonJobExecutor` | 存在 `IXXLJobsConfig` Bean + `enable-json-job-executor=true` |

> 注意：即使 `enable=false`，`RunningBackGroundExecutor` 仍会被装配（该 Bean 无 `enable` 属性开关）。

## JSON 任务执行器

`JsonJobExecutor` 是一个 `IStartup`（非传统 `@PostConstruct`），提供三个 `@XxlJob` 入口，统一经 XXL-Job 调用：

| Handler | 作用 |
| --- | --- |
| `jsonJobHandler` | 通用 JSON 任务入口：解析 `ExecuteInfo` → 按 `method` 查 `IJsonJobHandler` → `paramConvert` 参数 → `handle` 执行。找不到 method 调 `XxlJobHelper.handleFail`；异常 catch 后记日志 + handleFail |
| `available` | 列出已注册的全部 handler（methodName + helpText），JSON 序列化输出 |
| `help_json` | 输出示例 `ExecuteInfo`（含 method 占位 demoMethodName 与 params 演示），引导配置格式 |

`IJsonJobHandler<T>`：

```java
public interface IJsonJobHandler<T> {
    Class<? extends T> classType();                          // 参数类型，paramConvert 目标
    default String help() { return ""; }                     // 帮助文案
    default T paramConvert(Map mp) {                         // 默认：JSON 反序列化到 classType()
        return JsonUtils.parseObject(JsonUtils.toJSONString(mp), this.classType());
    }
    String handle(T params) throws Exception;                // 业务处理
}
```

`ExecuteInfo`（任务入参模型）：`method`（要调用的 handler Bean 名）+ `params`（原始 Map，由 handler 反序列化）。

**注册时机**：`JsonJobExecutor` 实现 `IStartup.startUp()`，启动时经 `SpringUtil.getBeansOfType(IJsonJobHandler.class)` 拉取容器内全部 handler，key 为 Bean 名。因此 handler 需是 Spring Bean。

## 后台任务执行器

`RunningBackGroundExecutor` 提供单个 XXL-Job 入口，用于批量驱动 common 模块的 `IBackgroundJob` Bean：

| Handler | 作用 |
| --- | --- |
| `runningBackGroundJobHandler` | 取容器内全部 `IBackgroundJob` Bean，逐个调用 `execute()`；单任务异常被捕获记日志后继续执行下一个，不影响整体 |

典型用途：把多个定时后台任务注册为 `IBackgroundJob`，在 XXL-Job 控制台仅配置一个 cron 触发 `runningBackGroundJobHandler`，统一驱动（与 logging 等定时刷盘场景配合）。

## async 任务调度外部驱动

`XxlJobScheduleTrigger`（位于 `com.tbs.schedule.xxl` 包）同时实现 common 的 `IBackgroundJob` 与 async 的 `ScheduleTrigger`，是 async 调度内核的**外部驱动触发器**：

- `name()` = `xxl-job-trigger`，`description()` = "xxl-job 外部调度驱动"；
- `start(Tickable)` 仅缓存内核视图，`stop()`/`wakeup()` 为**空实现**（外部调度模式不持有线程/信号量）；
- `execute()` 由 XXL-Job 调度中心按 cron 触发，单次调用被驱动内核的 `tick()` 执行一轮到期任务派发（区别于 async 内置的 `LocalThreadScheduleTrigger` 自启轮询循环）。

这是 async `tbs.schedule.*` 策略选 `job` 时的调度源。`IBackgroundJob` 由 common 提供，该触发器注册为 Bean 后即可被 `RunningBackGroundExecutor` 周期性驱动。

## 在 XXL-Job 控制台配置任务

1. **登录 XXL-Job 控制台**，创建执行器，应用名为配置的 `app-name`；
2. **创建任务**：
   - 自定义 JSON 任务 → 任务 Handler 填 `jsonJobHandler`，任务参数：
     ```json
     {"method": "sampleJobHandler", "params": {"taskName": "demo", "data": ["a","b"], "batchSize": 10}}
     ```
   - 后台任务批量驱动 → 任务 Handler 填 `runningBackGroundJobHandler`；
   - 查看已注册 handler → Handler 填 `available` / `help_json`。

## 注意事项

1. **`@UseXxlJob` 组合 `@EnableCommonPack`**：会一并启用 common 模块。
2. **`enable` 属性没有默认开关**：`@ConditionalOnProperty(... matchIfMissing=false)`，**必须显式 `tbs.xxl-job.enable=true`** 才装配执行器与 JSON 执行器。
3. **`admin-address` 缺失会阻断启动**：`xxlJobSpringExecutor` Bean 中 `AssertUtil.hasText(config.adminAddress(), "管理器地址不能为空")`。
4. **handler 必须注册为 Spring Bean**：`JsonJobExecutor` 在 `startUp()`（`IStartup` 时机）扫描容器，非 Bean 的 `IJsonJobHandler` 不会被发现；晚于该时机动态注册的 Bean 不会自动重扫。
5. **`RunningBackGroundExecutor` 不受 `enable` 控制**：即使 `enable=false` 也装配该 Bean（无条件属性开关）；但其 `@XxlJob` 仍需执行器就绪才被调度中心发现。
6. **`paramConvert` 依赖 Map→对象 JSON 转换**：`ExecuteInfo.params` 为原始 `Map`，默认按 `classType()` 反序列化；`JsonUtils` 来自 common。
7. **任务异常全部落入 `handleFail`**：`jsonJobHandler` 外层 catch 异常后 `handleFail`，不向上抛出。
8. **`XxlJobScheduleTrigger` 为外部驱动模式**：若同时启用 async 内置本地触发器会造成重复驱动，需按 async `tbs.schedule.embedded-trigger` 等策略正确选择单一驱动。
9. **日志路径目录自动创建**：配置了 `log-path` 且目录不存在时，装配时自动 `mkdirs`，避免 `XxlJobFileAppender` 写日志抛 IOException。

## 包结构

```
com.tbs.xxl
├── annotation/          @UseXxlJob（启用入口，@Import(XxlAutoRegister) + @EnableCommonPack）
├── api/                 IXXLJobsConfig（执行器配置契约）、IJsonJobHandler（JSON 任务处理器泛型契约）
├── config/              XxlAutoRegister（扫描 com.tbs.xxl，注册 Feature.XXL_JOB）、
│   │                    XxlJobConfig（装配执行器/JSON/后台执行器）
│   └── property/        XxlJobProperty（tbs.xxl-job）、XxlJobConfigForm（config 嵌套表单）
├── executor/            JsonJobExecutor（JSON 任务入口，IStartup）、
│   │                    RunningBackGroundExecutor（后台任务批量执行）
└── model/               ExecuteInfo（任务入参：method + params）

com.tbs.schedule.xxl
└── XxlJobScheduleTrigger    （async 调度内核的 xxl-job 外部驱动触发器）
```
