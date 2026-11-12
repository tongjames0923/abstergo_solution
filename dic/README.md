# Dic 模块 - 数据字典服务

## 模块概述

Dic 模块（`com.tbs:dic`）提供**数据字典**能力：字典（Dict）与字典项（Item）两级结构，支持字典/字典项的增删改查、启用/禁用、值比对校验，并通过 `@Cacheable` 缓存加速查询。

该模块是一个**独立 Dubbo 微服务**（区别于本仓库内的内嵌模块），以 Java 实现，包含两个子模块：

| 子模块 | 语言 | 职责 |
|--------|------|------|
| `dic-api` | Java | 契约层：实体、枚举、模型、Mapper、服务接口 `IDataDicService`（消费方仅依赖此包） |
| `dic-service` | Java | 服务实现：Dubbo 服务提供者，独立 Spring Boot 进程 |

依赖关系：

- `dic-api` → `shared-api`、`shared-pojo`、`common`、`sql`、`cache-api`、`async`
- `dic-service` → `dic-api`、`cache-impl`、`redis`（Redisson）、Nacos Config

## 模块结构

```
dic/
├── dic-api/                          # 契约层（Java）
│   └── com.tbs.dic/
│       ├── entity/                   # SystemDataDicEntity / SystemDataDicItemEntity
│       ├── enums/                    # DataDicFlagEnum / DataDicCompareEnum
│       ├── mapper/                   # DataDicMapper / DataDicItemMapper
│       ├── model/                    # DicModel / DicItemModel
│       └── service/                  # IDataDicService
└── dic-service/                      # 服务实现（Java）
    ├── src/main/java/com.tbs.dic/
    │   ├── DicServiceApplication.java  # 启动类
    │   ├── config/                   # DicMessageSourceProvider（i18n 资源注册）
    │   ├── constant/                 # DicI18nConstant（i18n key 常量）
    │   └── service/impl/             # DataDicServiceImpl（@DubboService）
    └── src/main/resources/
        ├── application.yml           # 主配置（Nacos/Dubbo）
        ├── application-db.yml        # 数据源（MySQL 驱动 + HikariCP）
        ├── application-redis.yml     # Redis
        ├── application-tbs.yml       # tbs.* 模块配置
        ├── application-mq.yml        # RabbitMQ
        ├── .env                      # 环境变量（spring-dotenv 加载）
        └── i18n/                     # messages-dic.properties（中）/ messages-dic_en_US.properties（英）
```

## 数据模型

### 数据字典表 `sys_data_dic`（SystemDataDicEntity）

| 字段 | 列 | 说明 |
|------|-----|------|
| `id` | `id` | 主键，自增 |
| `dataDicName` | `data_dic_name` | 字典名称（联合索引 `idx_data_dic_name_code`） |
| `dataDicCode` | `data_dic_code` | 字典编码（业务唯一键，联合索引同上） |
| `flag` | `flag` | 状态（索引 `idx_flag`），见 `DataDicFlagEnum` |
| `remark` | `remark` | 备注 |
| `createTime` / `updateTime` | `create_time` / `update_time` | 审计时间 |

实体标注 `@LogicDeleteEntity(filedName = "flag", activeFlagValues = {1, -1})`：启用与停用视为"存活"，删除（flag=0）后逻辑隔离。

### 字典项表 `system_data_dic_item`（SystemDataDicItemEntity）

| 字段 | 列 | 说明 |
|------|-----|------|
| `id` | `id` | 主键，自增 |
| `dicCode` | `dic_code` | 所属字典编码（索引 `idx_system_data_dic_item_dic_code`） |
| `label` | `label` | 字典项标签（同字典内唯一） |
| `value` | `value` | 字典项值 |
| `sort` | `sort` | 排序号（查询结果按其升序，null 按 0） |
| `flag` | `flag` | 状态（索引 `idx_flag`） |
| `remark` / `createTime` / `updateTime` | 同上 | — |

Mapper 均继承 `IFrameworkMapper`（sql 模块注解驱动查询框架），配合 `QueryEngine` 构建类型安全查询。

## 状态与比对枚举

### DataDicFlagEnum - 字典状态

| 枚举值 | code | 描述 |
|--------|------|------|
| `ENABLE` | 1 | 启用 |
| `DISABLE` | -1 | 停用 |
| `DELETE` | 0 | 删除（逻辑删除） |

### DataDicCompareEnum - 值比对结果

| 枚举值 | code | 描述 |
|--------|------|------|
| `EQUALS` | 1 | 相等 |
| `NOT_EQUALS` | 2 | 不相等 |
| `NO_DIC` | -1 | 字典编号不存在 |
| `NO_ITEM` | 0 | 字典项不存在 |

## 服务契约（IDataDicService）

```java
public interface IDataDicService {

    // ---- 查询 ----
    List<String> getDicCodesByCodeLike(String code);       // 按编码左模糊，仅启用态
    List<String> getDicCodesByNamesLike(String name);      // 按名称左模糊，仅启用态
    List<DicModel> getDicByCode(String code, boolean insertItem); // 按编码取字典（含按 sort 升序的项）；不存在返回空列表

    // ---- 写操作（均触发缓存失效） ----
    SystemDataDicEntity saveDataDic(SystemDataDicEntity dic);     // 新建或按编码更新名称/备注
    void saveDataDicItem(String dicCode, SystemDataDicItemEntity item); // 新建或按 id 更新项
    void enableDataDic(String dicCode);                    // 启用字典
    void disableDataDic(String dicCode);                   // 停用字典
    void enableDataDicItem(String dicCode, String label);  // 启用字典项
    void disableDataDicItem(String dicCode, String label); // 停用字典项
    void deleteDataDic(String dicCode);                    // 删除字典（逻辑删除）
    void deleteDataDicItem(String dicCode, String label);  // 删除字典项（逻辑删除）

    // ---- 校验 ----
    int valueEquals(String dicCode, String label, String value); // 比对结果见 DataDicCompareEnum
}
```

行为约定（源自 `DataDicServiceImpl`）：

- **缓存**：`getDicByCode` 标注 `@Cacheable(cacheNames = ["data_dic"], key = "#code")`；全部写操作标注 `@CacheEvict` 失效对应编码的缓存；缓存管理器经 `tbs.cache` 配置指向 Redisson（`RedissonSpringCacheManager` + `RedissonTtlCacheDecorator`），TTL 900 秒、LRU 驱逐（见配置说明）；
- **保存字典**：编码/名称非空校验（`USER_INPUT_ERROR`）；已存在时仅更新名称与备注（非空字段才覆盖）并刷新 `updateTime`；
- **保存字典项**：字典必须存在；`label` 在同字典内唯一（按 label 查重，更新时校验冲突项非自身）；带 `id` 走更新（value/remark/sort 非空才覆盖），否则新建（初始启用态）；
- **删除字典**：仅当字典下不存在启用/停用状态的项时允许删除（否则抛 `BUSINESS_LOGIC_ERROR`：`dic.has.undeleted.items`），需先删除或清空字典项；
- **值比对**：先查字典项（启用态）再查字典（启用态），任一缺失即返回对应 `NO_ITEM`/`NO_DIC` 码，均存在才比较 value；
- 所有参数校验失败抛出 `FormattedRuntimeException`（i18n key 见 `DicI18nConstant`，错误级别 `USER_INPUT_ERROR`）。

## 服务端启动

### 启用组件

启动类 `DicServiceApplication` 启用的能力（sql 与方言由 `sql-impl` + `sql-mysql-impl` 依赖自动装配，无需注解）：

```java
@SpringBootApplication(scanBasePackages = "com.tbs.dic")
@EnableTbsAsyncPack      // async 模块（长期异步任务/调度，optional-execute=false）
@EnableTbsCache          // cache 模块（Redisson 缓存管理器）
@EnableSharedLibBean     // shared 共享库 Bean
@EnableTbsRedis          // Redisson（缓存/锁/DataHolder）
@EnableDubbo             // Dubbo 服务暴露（@DubboService）
@MapperScan("com.tbs.dic.mapper")
// 实体包由 application-db.yml 的 jkit.sql.auto.packages 配置
```

### 运行方式

```bash
# 编译（根目录）
mvn clean install -pl dic/dic-api,dic/dic-service -am

# 本地运行
cd dic/dic-service
mvn spring-boot:run
```

环境变量通过 `.env` 文件（spring-dotenv 加载，位于 `dic-service/src/main/resources/.env`）或进程环境变量提供：

| 变量 | .env 默认 | yml 兜底默认 | 说明 |
|------|-----------|--------------|------|
| `NACOS_ADDRESS` | `127.0.0.1:8848` | `127.0.0.1:8848` | Nacos 注册/配置中心 |
| `DUBBO_PORT` | `20777` | `20779` | Dubbo 协议端口（.env 优先） |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1` / `3306` / `app_system` | `127.0.0.1` / `3306` / `core_app` | MySQL 连接 |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `123456` | `root` / `123456789` | 数据库凭证 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / `123456` | 同左 | Redis |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` | 同左 | RabbitMQ |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `admin` / `admin123` | 同左 | RabbitMQ 凭证 |
| `XXL_JOB_ADMIN_ADDRESS` | `http://127.0.0.1:9898/xxl-job-admin` | 同左 | 调度中心（app-name=`dataDicApplication`） |

> 注意：`.env` 中 `DB_NAME=app_system` 与 yml 兜底 `core_app` 不同，以 `.env`（或实际环境变量）为准。数据库表结构由 jkit-sql-auto（`jkit.sql.auto.mode: update`）自动建表/更新，无需手工执行 DDL。

## 配置说明

### 主配置（application.yml）

```yaml
spring:
  application:
    name: data-dic-service            # 服务名
  profiles:
    include: [redis, db, tbs, mq]     # 四个本地 profile
  config:
    import: optional:nacos:data-dic-service.yml?group=DEFAULT_GROUP&refreshEnabled=true  # Nacos 远程配置（optional）
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDRESS:127.0.0.1:8848}
  lifecycle:
    timeout-per-shutdown-phase: 30s   # 优雅停机
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

dubbo:
  application:
    name: dubbo-data-dic-service
    logger: slf4j
    check-serializable: false
    serialize-check-status: DISABLE
  registry:
    address: nacos://${NACOS_ADDRESS:127.0.0.1:8848}   # Nacos 注册中心
  protocol:
    port: ${DUBBO_PORT:20779}         # tri（Triple）协议
    name: tri
  shutdown:
    await-duration: 10000
  consumer:
    filter: -ContextHolderAuthenticationPrepareFilter   # 排除认证上下文准备过滤器
```

### tbs 模块配置（application-tbs.yml）

```yaml
tbs:
  i18n:
    enabled: true
    encoding: UTF-8
    default-locale: zh_CN
    cache-seconds: 3600
    use-code-as-default-message: true
  cache:
    cache-manager-impl: org.redisson.spring.cache.RedissonSpringCacheManager
    ttl-cache-decorator: com.tbs.cache.redis.RedissonTtlCacheDecorator
  broadcast:
    event-bus-type: com.tbs.broadcast.bus.impl.SimpleEventBus
  lock:
    lock-provider-type: com.tbs.lock.redis.RedissonLockProvider     # Redisson 分布式锁
  data:
    data-holder-manager: com.tbs.common.data.impl.DefaultAutoGenDataHolderManager
    data-holder: com.tbs.redis.holder.RedisDataHolder               # Redis 数据持有者
  redis:
    enable: true
  redission:
    cache:
      cache-timeout: 900          # 字典缓存 TTL 900 秒
      eviction-mode: lru
  start-up:
    optional-execute: false       # 启动任务失败不跳过
  xxl-job:
    enable: true
    enable-json-job-executor: true
    config:
      admin-address: ${XXL_JOB_ADMIN_ADDRESS:http://127.0.0.1:9898/xxl-job-admin}
      app-name: dataDicApplication
      access-token: ${XXL_JOB_ACCESS_TOKEN:123456789}
      log-retentions-days: 3
  knife4j:
    enable: true
    show-url: true
```

### 数据访问（application-db.yml）

- 驱动为 `com.mysql.cj.jdbc.Driver`，URL 形如 `jdbc:mysql://...`；SQL 观测由 `sql` 模块的 JDBC 装饰层承担（`application-tbs.yml` 的 `tbs.sql` 配置）；
- 连接池 HikariCP：最小空闲 5 / 最大 20 / 连接超时 30s / 空闲超时 600s / 最大存活 1800s；
- `jkit.sql.auto.mode: update`（自动建表，可用 `jkit.sql.auto.enabled: false` 关闭），MyBatis 开启驼峰映射。

### 消息队列（application-mq.yml）

RabbitMQ 连接配置（host/port/凭证），监听器 `acknowledge-mode: manual` 手动确认。

## 国际化

- 资源文件：`i18n/messages-dic.properties`（中文）、`i18n/messages-dic_en_US.properties`（英文）；
- 经 `DicMessageSourceProvider`（继承 common 模块 `AbstractMessageSourceProvider`，order=-300）自动合并到全局 MessageSource，无需在 `tbs.i18n.basenames` 中显式声明；
- key 常量集中于 `DicI18nConstant`，占位符风格 `{0}`，包括：`dic.code.empty`、`dic.name.empty`、`dic.not.exists`、`dic.has.undeleted.items`、`dic.item.label.empty`、`dic.item.value.param.empty`、`dic.item.label.exists`、`dic.item.id.not.exists`、`dic.item.label.not.exists`；新增 key 须中英文两份同步维护。

## 消费方接入

其他服务通过 Dubbo 引用字典服务（仅依赖 `dic-api`）：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>dic-api</artifactId>
</dependency>
```

```java
@Service
public class EnumHelperService {

    @DubboReference
    private IDataDicService dataDicService;

    public List<DicModel> loadGenderDic() {
        return dataDicService.getDicByCode("GENDER", true);
    }

    /** 校验用户输入的性别值是否为合法字典值 */
    public boolean isLegalGender(String inputValue) {
        int result = dataDicService.valueEquals("GENDER", "性别", inputValue);
        return result == DataDicCompareEnum.EQUALS.getCode();
    }
}
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。
