# Logging 模块 - 日志服务

## 模块概述

Logging 模块（`com.tbs:logging`）是一个**独立的日志 Dubbo 微服务**（Java 实现），负责系统操作日志的**异步收集、批量存储与查询统计**：

- 消费 RabbitMQ 日志消息，本地缓冲后批量写入 `sys_operate_record` 表
- 以 `@DubboService` 暴露 `ISystemLogService`，提供日志分页查询、操作类型统计报表（含中位数/P90/标准差/成功率/稳定性指数）

模块包含两个子模块（父 POM：`logging-service`，描述"后端日志dubbo服务"）：

| 子模块 | 职责 |
|--------|------|
| `logging-api` | 契约层：`ILogRecorder` / `ILogExtractor` 接口、日志表单与 VO（无额外业务依赖） |
| `logging-impl` | 服务实现：Dubbo 服务提供者、DB 读写器、独立 Spring Boot 进程（boot repackage 可执行 jar） |

`logging-impl` 依赖：`logging-api`、`cache-impl`、`redis`、`shared-api`（存储实体与 Mapper）、`system-api`（`ISystemLogService` 契约）、`rabbitmq-logging`（MQ 日志投递端，传递引入 `rabbitmq-impl`/`mq-api` v2 消费链路契约）、Nacos Config、spring-dotenv。

## 全链路架构

```
[业务应用侧]（如 app 模块，AppConfig 注册 ILogRecorder Bean）
    │  RabbitMqLoggingRecorder("tbs.log.queue", "systemLog")
    │    record(form) → 组装 v2 信封经 IMqMessageProducer 发送（失败仅记日志，不阻断业务）
    │    flush()     → 发送 flushFlag=1 的空表单，通知服务端刷写缓冲
    ▼
RabbitMQ（exchange "tbs.log.queue" / routingKey "systemLog"）
    ▼
[logging 服务]（本模块，MQ v2 记录驱动消费链路）
    LoggingRecordRabbitMqContext（RabbitMqContext<LoggingRecordForm>，contextId=2）
    │  接收建档 → LoggingRecordConsumer（consumeId=1）消费
    │  → 转 SystemOperateRecordEntity（systemLogFlag=1）
    │  本地缓冲 LinkedBlockingQueue(100) → 批量 100 条 insertList
    │  批量失败降级逐条 insertSelective
    ▼
MySQL sys_operate_record 表
    ▲
    │  DbLogExtractor（QueryEngine 查询 + Redisson 缓存）
    │        │
    │  SimpleSystemLogServiceImpl（@DubboService）
    └── ISystemLogService ◀── Dubbo 消费方（前端日志查询/统计报表）
```

## 模块结构

```
logging/
├── logging-api/                      # 契约层
│   └── com.tbs.logging/
│       ├── core/                     # ILogRecorder / ILogExtractor
│       ├── form/                     # LoggingRecordForm / LoggingRecordExtractForm
│       └── vo/                       # LogRecordVO
└── logging-impl/                     # 服务实现
    └── com.tbs.logging/
        ├── LoggingServiceApplication # 启动类
        ├── config/                   # LoggingServiceConfig（v2 消费链路 Bean 集中装配）
        ├── v2/                       # LoggingRecordRabbitMqContext / LoggingRecordMessageReceiver /
        │                             #   LoggingRecordMessageProcessor / LoggingRecordConsumer
        └── impl/                     # SimpleSystemLogServiceImpl / DbLogExtractor /
                                      #   CacheNamesConstants / RemoteTableDataOperator
    └── resources/
        ├── application.yml           # 主配置（Nacos/Dubbo，server.port=8081）
        ├── application-db.yml        # 数据源（p6spy + HikariCP）
        ├── application-redis.yml     # Redis
        ├── application-tbs.yml       # tbs.* 模块配置
        └── application-mq.yml        # RabbitMQ（手动 ACK）
```

## 核心契约（logging-api）

### ILogRecorder - 日志记录器（生产端）

```java
public interface ILogRecorder {
    void record(LoggingRecordForm recordForm);  // 记录日志（异步投递 MQ）
    void flush();                               // 刷写日志（通知服务端刷缓冲）
}
```

默认实现 `RabbitMqLoggingRecorder`（位于 rabbitmq-mod 的 `rabbitmq-logging` 子模块）：构造时指定 exchange 与 routingKey，`record` 组装 v2 信封（`MqMessageOutcomeVO`：topic=交换机 / routeKey=路由键 / payload=表单）经 `IMqMessageProducer` 发布，异常仅记录日志不抛出；`flush` 发送 `flushFlag=1` 的空表单。业务应用侧的注册示例（app 模块 `AppConfig`）：

```java
@Bean
public ILogRecorder loggerRecoreder() {
    return new RabbitMqLoggingRecorder("tbs.log.queue", "systemLog");
}
```

### ILogExtractor - 日志提取器（查询端）

```java
public interface ILogExtractor {
    List<LogRecordVO> extract(LoggingRecordExtractForm form);  // 按条件提取日志
    int count(LoggingRecordExtractForm form);                  // 统计总数
    List<String> getAllOperateTypes();                         // 所有操作类型（去重排序）
}
```

### LoggingRecordForm - 日志记录表单（生产端 → MQ）

| 字段 | 类型 | 说明 |
|------|------|------|
| `operateType` | String | 操作类型（通常为"全限定类名.方法"） |
| `operateDesc` | String | 操作描述 |
| `operateUser` / `operateIp` | String | 操作用户 / IP |
| `operateTime` / `operateFinishTime` | Date | 操作开始 / 结束时间（服务端据其计算耗时） |
| `operateResult` / `operateParam` | String | 操作结果 / 参数（JSON） |
| `errorMessage` / `errorCode` | String / Integer | 错误信息 / 错误级别码 |
| `operateUrl` / `method` | String | 请求 URL / HTTP 方法 |
| `flushFlag` | Integer | 刷新标志（1=通知服务端刷写缓冲） |
| `traceId` | String | 链路追踪 ID（表单已定义，当前写入端未持久化） |

### LoggingRecordExtractForm - 查询条件

| 字段 | 类型 | 说明 |
|------|------|------|
| `operateTypes` | List\<String\> | 操作类型集合（IN 查询） |
| `operateTimeBegin` / `operateTimeEnd` | Date | 时间范围（闭区间） |
| `pageNum` / `pageSize` | Integer | 分页参数（pageSize 缺省 15） |

### LogRecordVO - 日志记录视图（查询结果）

与表单字段基本对应，另含 `requestUrl` / `requestMethod` / `costTime`（毫秒）；`operateFinishTime` 由读取端还原（`operateTime + costTime`）。

## 存储模型（sys_operate_record）

存储实体 `SystemOperateRecordEntity` 位于 shared 模块（`shared-pojo`），Mapper 为 `SystemOperateMapper`（`shared-api`），主键经 `IdAutoProvider` 自动生成：

| 列 | 说明 | 列 | 说明 |
|----|------|----|------|
| `id` | 主键 | `operate_param_json` | 请求参数 JSON（longtext） |
| `operate_type` | 操作类型（128） | `operate_result_json` | 操作结果 JSON（longtext） |
| `operate_content` | 操作内容（512） | `cost_time` | 操作耗时（毫秒） |
| `operate_time` | 操作时间 | `operate_error` | 异常信息（longtext） |
| `operate_user_name` / `operate_ip` | 用户（64）/ IP（64） | `operate_error_level` | 异常级别码 |
| `method_type` | HTTP 方法（64） | `system_log_flag` | 系统日志标志（本服务写入恒为 1） |
| `operate_url` | 请求 URL（512） | | |

索引：`common_index(operate_content, operate_type)`、`time_idx(operate_time)`、`error_level_idx`、`user_idx(operate_user_name, operate_ip)`、`url_idx`、`method_idx`。

## Dubbo 服务（ISystemLogService）

`LoggingServiceConfig` 装配（`@DubboService`，由 Dubbo 暴露给消费方）：

```java
public interface ISystemLogService {
    List<LogOperateTypeView> getLogOperateTypeList(int lastDay);  // 近 N 天操作类型统计报表
    List<String> getOperateTypeList(String keyword);              // 操作类型列表（下拉搜索）
    PageVO<LogRecordVO> getLogRecordList(LoggingRecordExtractForm form); // 日志分页列表
}
```

实现类 `SimpleSystemLogServiceImpl`（内部委托 `ILogExtractor`）：

- **getLogOperateTypeList(lastDay)**：提取 `lastDay` 天前至今的全量日志，按操作类型聚合——次数、总/最大/最小耗时、错误次数、成功率（4 位小数 ×100）、中位数、P90（`size×0.9` 取整并防越界）、标准差、去重用户数/IP 数；最终输出**稳定性指数**（0-100）：成功率权重 40% + 响应时间稳定性 30%（变异系数 CV，越小得分越高）+ 错误分散度 20%（错误数/(用户数+IP 数)，越集中得分越低）+ 请求量 10%（≥100 次满分）；错误判定忽略"无错误信息"或 `errorCode < DATA_ACCESS_ERROR(3)`（用户输入/业务逻辑级）的记录；输出的 `operateType` 经 `suitTypeName` 截取为最后两段（`类名.方法名`）
- **getLogRecordList(form)**：`PageUtil.build(extract(form), count(form))` 组装分页（common 模块 `PageVO`）
- **getOperateTypeList(keyword)**：基于 `getAllOperateTypes()` 做 `contains` 模糊过滤；**注意**：`keyword` 为空时实际返回空列表（需传入关键词）

消费方接入示例：

```java
@DubboReference
private ISystemLogService systemLogService;

PageVO<LogRecordVO> page = systemLogService.getLogRecordList(form);
List<LogOperateTypeView> report = systemLogService.getLogOperateTypeList(7);
```

## 消费端实现（MQ v2 链路）

存量 v1 消费者（`DbLogWriter`）已随 v2 全面替换下线，消费端由 `com.tbs.logging.v2` 四个类承担，全部由 `LoggingServiceConfig` 以 `@Bean` 集中装配（不加 `@Component`），链路见 [rabbitmq-mod README](../rabbitmq-mod/README.md)：

- `LoggingRecordRabbitMqContext`：`RabbitMqContext<LoggingRecordForm>` 子类，**直接接管原 `systemLog` 队列**（交换机 `tbs.log.queue` / 路由键 `systemLog` 均不变，投递端无需改动；无并行、无双写），`getContextId()=2`，`prefetchCount=250`，手动 ack（父类默认）、单条投递语义；
- `LoggingRecordMessageReceiver` / `LoggingRecordMessageProcessor`：接收/分发段薄壳，仅绑定 `<Message, LoggingRecordForm>` 泛型；
- `LoggingRecordConsumer`（`IMqConsumer<LoggingRecordForm>`，`consumeId=1`）：沿用 v1 高吞吐范式——本地缓冲 `LinkedBlockingQueue(100)`，满批（100 条）/ flush 消息 / 定时（5s）触发 `insertList` 批量入库，批量失败降级逐条 `insertSelective`（单条失败仅记日志，日志链路容忍丢失）；转换为实体时 `systemLogFlag=1`，耗时 = `operateFinishTime - operateTime`；消费不感知厂商通道、不做 ack/nak。

协作层：`IMqRecorder` / `IMqMessageMonopolizer` 选用 Redis 实现（`RedisMqRecorder` / `RedisMqMessageMonopolizer`，主记录 Hash `mq:recorder:0:2:main`、台账 List `...:ledger`、失败留痕 Hash `...:error`、完成归档 Hash `...:done:{yyyyMMddHH}`，单飞锁 `mq:monopoly:2:{messageId}` 经 `RedissonLockProvider` 获取），跨实例共享且重启不丢；已消费完成（PROCESSED）的记录迁入完成层随小时桶 3 天后过期，失败留痕与台账永久保留；`IConsumerRegistry` 仍为内存实现（注册的是宿主消费者对象，无法跨实例序列化），上下文无需改动。

> `flush()`（生产端发 flushFlag 消息）仍是主动触发刷盘的手段；另有 5 秒定时刷新兜底，缓冲未满且未收到 flush 消息时日志最迟经一个刷新周期落库。

## 查询端实现（DbLogExtractor）

由 `LoggingServiceConfig` 装配为 `ILogExtractor` Bean，基于 sql 模块 `QueryEngine` 构建类型安全查询：

- **extract**：条件 `operateTypes IN` + 时间闭区间，按 `operateTime` 倒序，`pageNum >= 1` 时分页（pageSize 缺省 15）；实体映射为 `LogRecordVO`（`operateFinishTime = operateTime + costTime`）
- **count**：同条件全量 `fetch()` 后取 `size()`（注意：非 COUNT SQL，数据量大时内存开销高）
- **getAllOperateTypes**：查询最近 3 个月记录，去重、排序；标注 `@Cacheable(cacheNames="logging:allOperateTypes", key="'allOperateTypes'")`（cacheNames 常量集中于 `CacheNamesConstants`）+ `@ExpireOn(expireAll=true, value=1, TimeUnit.HOURS)`（cache 模块主动过期，1 小时刷新一次）；缓存经 Redisson（`tbs.redission.cache`：900s / LRU）

## 服务端启动

### 启用组件

启动类 `LoggingServiceApplication`：

```java
@SpringBootApplication(scanBasePackages = {"com.tbs.logging"})
@EnableTbsSql            // sql 模块（注解驱动查询框架）
@EnableTbsAsyncPack      // async 模块
@EnableTbsCache          // cache 模块（Redisson 缓存管理器）
@EnableSharedLibBean     // shared 共享库 Bean（含 SystemOperateMapper）
@UseMysql                // MySQL 方言
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis          // Redisson
@EnableDubbo             // Dubbo 服务暴露
@EnableRabbitMq          // RabbitMQ 消费者
@MapperScan("com.tbs.logging.mapper")
```

### 运行方式

```bash
# 编译（根目录）
mvn clean install -pl logging/logging-api,logging/logging-impl -am

# 本地运行
cd logging/logging-impl
mvn spring-boot:run
```

表结构经 JPA `ddl-auto: update` 自动建表/更新，无需手工 DDL。模块未内置 `.env` 文件（依赖 spring-dotenv，可自行在运行目录提供），环境变量需经进程环境注入：

| 变量 | 兜底默认 | 说明 |
|------|----------|------|
| `NACOS_ADDRESS` | `127.0.0.1:8848` | Nacos 注册/配置中心 |
| `DUBBO_PORT` | `20780` | Dubbo tri 协议端口 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1` / `3306` / `core_app` | MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `123456789` | 数据库凭证 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / `123456` | Redis |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` | RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `admin` / `admin123` | RabbitMQ 凭证 |

## 配置说明

### 主配置（application.yml）

```yaml
spring:
  application:
    name: logging-service
  profiles:
    include: [redis, db, tbs, mq]
  config:
    import:
      - optional:nacos:logging-service.yml?group=DEFAULT_GROUP&refreshEnabled=true  # Nacos 远程配置（optional）
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDRESS:127.0.0.1:8848}
  lifecycle:
    timeout-per-shutdown-phase: 30s

dubbo:
  application:
    name: dubbo-logging-view-service
    logger: slf4j
    check-serializable: false
    serialize-check-status: DISABLE
  registry:
    address: nacos://${NACOS_ADDRESS:127.0.0.1:8848}
  protocol:
    port: ${DUBBO_PORT:20780}
    name: tri
  shutdown:
    await-duration: 10000
  consumer:
    filter: -ContextHolderAuthenticationPrepareFilter

server:
  port: 8081
```

### tbs 模块配置（application-tbs.yml）

```yaml
tbs:
  cache:
    cache-manager-impl: org.redisson.spring.cache.RedissonSpringCacheManager
    ttl-cache-decorator: com.tbs.cache.redis.RedissonTtlCacheDecorator
  lock:
    lock-provider-type: com.tbs.lock.redis.RedissonLockProvider
  data:
    data-holder-manager: com.tbs.common.data.impl.DefaultAutoGenDataHolderManager
    data-holder: com.tbs.redis.holder.RedisDataHolder
  redis:
    enable: true
  redission:
    cache:
      cache-timeout: 900        # 操作类型缓存 TTL 900 秒
      eviction-mode: lru
  start-up:
    optional-execute: false     # 启动任务失败不跳过
```

### 数据访问（application-db.yml）

- 驱动 `com.p6spy.engine.spy.P6SpyDriver`（SQL 打印），URL `jdbc:p6spy:mysql://...`；
- HikariCP：最小空闲 5 / 最大 20 / 连接超时 30s / 空闲超时 600s / 最大存活 1800s / 测试查询 `SELECT 1`；
- JPA `ddl-auto: update`，MyBatis 驼峰映射。

### 消息队列（application-mq.yml）

RabbitMQ 连接配置；v2 消费上下文的确认模式由 `MqConsumerProperties` 默认 `AcknowledgeMode.MANUAL` 承担（建档成功才 ack，失败恢复走记录层，不依赖厂商重投/死信）。

## 使用注意事项

1. **刷写时机**：批量入库由满批（100 条）/ flush 消息 / 5 秒定时刷新三路触发；服务异常重启会丢失缓冲中未落库的日志，生产端仍可在合适时机（如请求结束/定时任务）调用 `ILogRecorder.flush()` 主动刷盘
2. **count 为内存统计**：`LoggingRecordExtractForm` 无时间范围时 `count` 会全表加载，前端查询务必携带时间范围
3. **统计报表为内存聚合**：`getLogOperateTypeList` 会拉取 `lastDay` 全量日志做内存聚合，`lastDay` 过大时开销显著
4. **操作类型搜索需传关键词**：`getOperateTypeList` 的 `keyword` 为空返回空列表
5. **traceId 未持久化**：`LoggingRecordForm.traceId` 当前在写入链路中被丢弃，链路追踪需另行方案
6. **systemLogFlag=1**：本服务写入的记录均带系统日志标志，可与业务应用直接写入的记录区分

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
