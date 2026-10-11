# SQL Server CDC 部署与使用指南

> 适用模块：`cdc-mod/cdc-sqlserver-impl`（基于 Debezium Embedded Engine 3.0.x + SQL Server 原生 CDC）
> 阅读前提：无。本文从数据库准备到业务监听一条龙覆盖，按步骤执行即可开箱即用。
> 通用契约（`CdcEvent` 结构、过滤、分发、幂等约定）见模块根 `README.md`，本文不重复。

---

## 1. 捕获原理与版本要求

连接器伪装成 SQL Server 客户端，读取 **SQL Server 原生 CDC**（基于事务日志的捕获作业）产出的变更表。

| 要求 | 说明 |
|------|------|
| 数据库版本 | **SQL Server 2016 SP1+**（13.0.4001 及以上），标准版即可；**不支持 Express 版**（无 Agent/CDC） |
| SQL Server Agent | **必须运行**（CDC 捕获/清理作业由它驱动） |
| 数据库级 CDC | 目标库 `is_cdc_enabled = 1` |
| 表级 CDC | 目标表 `is_tracked_by_cdc = 1`（建议表有主键或唯一索引） |
| 连接加密 | 新版 mssql-jdbc 默认强制加密；本地/自签环境按下方配置关闭 |

---

## 2. 数据库侧准备（一次性）

初始化脚本全文见 `etc/sql_file/cdc_user_init.sql` 第二节，此处为按序执行的最小步骤。
开启库/表 CDC 需要 **sysadmin** 权限；日常捕获使用下方专用账号。

### 2.1 前置检查（逐条执行，全部满足才继续）

```sql
-- 版本 >= 13.0.4001
SELECT SERVERPROPERTY('ProductVersion') AS product_version;

-- 目标库 is_cdc_enabled 必须为 1
SELECT name, is_cdc_enabled FROM sys.databases;

-- Agent 必须运行（中文本地化实例服务名为“SQL Server 代理 (...)”，见 2.4 说明）
SELECT servicename, status_desc FROM sys.dm_server_services;
```

### 2.2 开启数据库级与表级 CDC

```sql
USE XH_E10;   -- 替换为你的业务库
GO
EXEC sys.sp_cdc_enable_db;
GO
-- 每张需要捕获的表执行一次；表必须有主键（或唯一索引）
EXEC sys.sp_cdc_enable_table
    @source_schema        = N'dbo',
    @source_name          = N'PURCHASE_ORDER',
    @role_name            = N'cdc_admin',
    @supports_net_changes = 1;
GO
```

### 2.3 创建 CDC 专用账号

```sql
USE master;
GO
CREATE LOGIN cdc_user WITH PASSWORD = N'changeit', CHECK_POLICY = OFF;   -- 替换口令
GO
USE XH_E10;
GO
CREATE USER cdc_user FOR LOGIN cdc_user;
GO
-- 推荐：捕获库 db_owner（Debezium 官方要求）
EXEC sp_addrolemember @rolename = N'db_owner', @membername = N'cdc_user';
GO
```

降权方案（以 `cdc_admin` 角色 + cdc/dbo 架构授权替代 db_owner）见 `cdc_user_init.sql` 第二节注释。

### 2.4 SQL Server Agent 运行状态（中文本地化实例必读）

- **中文本地化实例**的 Agent 服务名为 `SQL Server 代理 (MSSQLSERVER)`（英文为 `SQL Server Agent (...)`）。
- Debezium 默认的 Agent 状态探测 SQL 只匹配英文服务名，中文实例会报
  `SQL Server Agent running status query must return exactly one value` 并停止连接器。
- **本模块已内置中英文服务名兼容的探测查询，无需任何配置**。特殊环境下可用
  `tbs.cdc.sqlserver.agent-status-query` 覆盖（查询中 `[#db]` 占位符由连接器替换为当前库名，
  查询必须恰好返回一行）。
- Agent 未运行时手动启动：Windows `net start "SQL Server Agent (MSSQLSERVER)"`；
  Linux `sudo systemctl start mssql-server-agent`。

---

## 3. 应用侧部署

### 3.1 Maven 依赖（主应用 pom）

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>cdc-sqlserver-impl</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 3.2 启动类装配

```java
@EnableTbsCdc
@EnableCdcSqlserver
@SpringBootApplication(scanBasePackages = {"com.tbs.app"})
public class AppApplication { /* ... */ }
```

### 3.3 配置（`application-tbs.yml`）

```yaml
tbs:
  # 总开关默认启用：@EnableTbsCdc 注解引入即生效；如需关闭可配置 enable: false（false 时零 Bean 装配、零线程占用）
  cdc:
    # 捕获源由引入的实现子模块（cdc-sqlserver-impl）决定，无需声明类型
    # 事件载荷裁剪：full 前后完整行 / changed-only UPDATE 仅差异列 / key-only 仅主键
    payload-mode: changed-only
    sqlserver:
      hostname: ${CDC_DB_HOST:192.168.80.12}
      port: ${CDC_DB_PORT:1433}
      # CDC 专用账号，走环境变量注入，勿提交真实口令
      username: ${CDC_DB_USERNAME:cdc_user}
      password: ${CDC_DB_PASSWORD:}
      # Debezium 逻辑服务名（topic 前缀/源标识），全局唯一
      server-name: tbs_cdc
      # 捕获的数据库列表，逗号分隔（必填）
      database-include-list: ${CDC_DB_INCLUDE:XH_E10}
      # 【关键格式】捕获的表白名单：schema.table 格式，逗号分隔；
      # Debezium 3.0.8 实测匹配 schema.table，不带数据库名前缀（写 XH_E10.dbo.PURCHASE_ORDER 不会命中），
      # 仅写表名 PURCHASE_ORDER 同样不命中
      table-include-list: ${CDC_TABLE_INCLUDE:dbo.PURCHASE_ORDER}
      # 消费位点持久化文件：生产建议挂持久卷
      offset-storage-file: ${CDC_OFFSET_FILE:data/cdc-offsets-sqlserver.dat}
      # 位点重置维护开关：true 时启动前【成对】删除位点+结构历史文件（全新启动）。
      # 仅用于位点与结构历史不一致的故障恢复；用完必须置回 false，否则每次重启都丢弃消费位点
      reset-offset: false
      # 快照模式：首次部署用 schema_only（仅快照表结构后进入增量）；SQL Server 不支持 never
      snapshot-mode: ${CDC_SNAPSHOT_MODE:schema_only}
      encrypt: false                     # 本地/自签环境
      trust-server-certificate: true
      # 心跳间隔（毫秒）：无变更时产生心跳事件推进位点，防止位点文件长期不刷新
      heartbeat-interval-ms: 5000
      # Agent 状态探测查询：留空使用内置中英文服务名兼容默认值，一般无需配置
      # agent-status-query: ""
    # 以下为通用段（过滤/分发/批量/错误处理），完整语义见模块 README 第 4 节
    filter:
      include-databases: [ "XH_E10" ]
      exclude-databases: []
      include-table-patterns: []         # 表级正则白名单（匹配纯表名或 db.table）
      exclude-table-patterns: []
      row-conditions: {}                 # 行级 SpEL，key=db.table，false 则丢弃
    dispatch:
      mode: in-process                   # in-process / mq / both
      in-process-mode: async             # sync / async
      mq:
        exchange: tbs.cdc
        routing-pattern: "cdc.{database}.{table}.{changeType}"
    batch:
      queue-capacity: 10000
      batch-size: 200
      flush-interval-ms: 500
      on-full: block                     # block 不丢 / drop-oldest 不阻塞可能丢
    error:
      retry-times: 3
      retry-delay-ms: 1000
      on-fail: skip                      # skip / dead-letter
```

---

## 4. 编写监听器

### 4.1 编程式（实现 `CdcChangeListener`）

```java
@Bean
public CdcChangeListener purchaseOrderListener() {
    return new PurchaseOrderListener();
}

public class PurchaseOrderListener implements CdcChangeListener {
    @Override
    public CdcSubscription getSubscription() {
        return CdcSubscription.builder()
            // 正则，同时匹配纯表名与 db.table 两种形式
            .tablePatterns(Set.of("PURCHASE_ORDER"))
            .changeTypes(EnumSet.of(CdcChangeType.UPDATE, CdcChangeType.CREATE))
            .async(true)
            .build();
    }

    @Override
    public void onCdcEvent(CdcEvent event) {
        // 至少一次投递，必须按 event.getEventId() 幂等
        log.info("CDC EVENT {}", JsonUtils.toJSONString(event));
    }
}
```

### 4.2 注解式（`@CdcListener`）

```java
@Component
public class MyListener {
    @CdcListener(tables = {"PURCHASE_ORDER"}, types = {CdcChangeType.UPDATE})
    public void onChange(CdcEvent event) { /* ... */ }
}
```

### 4.3 `CdcEvent` 关键字段

| 字段 | 说明 |
|------|------|
| `eventId` | 事件唯一标识，消费端幂等去重依据 |
| `changeType` | `CREATE` / `UPDATE` / `DELETE`（`READ` 仅枚举预留，日志流机制捕获不到） |
| `database` / `table` | 库名（`XH_E10`）/ 纯表名（`PURCHASE_ORDER`） |
| `primaryKey` | 主键列值 |
| `beforeData` / `afterData` | 变更前/后行数据（受 `payload-mode` 裁剪） |
| `sourceTimestamp` / `captureTimestamp` | 源库变更时间 / 捕获时间 |
| `transaction` / `position` | 事务信息 / 日志位点 |

---

## 5. 首次启动验证清单

1. 启动应用，日志应依次出现：
   `Debezium SQL Server配置就绪` → 无 Agent 探测报错 → 无 include list 告警。
2. 出现 `CDC is enabled for table ... not on connector's table include list` → 见第 6 节 T2。
3. 对捕获表执行一条 `UPDATE`，日志出现 `CDC EVENT {...}` 即全链路打通。
4. 重启应用，确认位点续传正常（无 db history 报错）。

---

## 6. 故障排查（本模块实战踩坑实录）

| 现象 | 根因 | 处理 |
|------|------|------|
| `SQL Server Agent running status query must return exactly one value` | Debezium 默认探测只匹配英文服务名；中文本地化实例为 `SQL Server 代理 (...)` → 0 行 | 模块已内置兼容查询；如自定义 `agent-status-query`，注意键由工厂自动加 `database.` 前缀 |
| `CDC is enabled for table ... but the table is not on connector's table include list` | `table.include.list` 在 3.0.8 实测匹配 **schema.table**，写三段全限定名或仅表名都不命中 | 配置改为 `dbo.表名`（见 3.3）；另检查运行环境是否导出旧值 `CDC_TABLE_INCLUDE` 覆盖 yml 默认 |
| `The db history topic is missing` | 位点文件与结构历史文件状态不同步（只删了其中一个，或某次启动中途失败） | 置 `reset-offset: true` 重启一次完成成对重置，**再置回 false**；日常永远不要单独删除其中一个文件 |
| 连接器正常启动但零事件 | 表未开 CDC / include list 不命中 / 环境变量覆盖了 yml | 按 `sys.tables.is_tracked_by_cdc` → include 格式 → 环境变量顺序排查 |

---

## 7. 位点与结构历史文件（运维要点）

- **成对文件**：`{offset-storage-file}` 与同目录派生的 `{去后缀}-dbhistory.dat` 是状态绑定的一对——
  位点记录消费进度，结构历史记录对应时点的表结构，启动校验要求二者一致。
- **规则一句话：要么都别动，要么一起删。** 正常重启（两文件完整）不需要任何手动操作。
- 故障恢复用 `reset-offset: true`（成对重置，全新启动），**用完必须置回 `false`**。
- `schema_only` 首次启动后如需改 `no_data` 增量模式，前提是**保留**这两个文件。
- 生产环境两个文件都挂持久卷；丢失位点文件会按 `snapshot.mode` 重新初始化。

---

## 8. 环境变量速查

| 变量 | 默认 | 说明 |
|------|------|------|
| `CDC_DB_HOST` / `CDC_DB_PORT` | 192.168.80.12 / 1433 | SQL Server 地址 |
| `CDC_DB_USERNAME` / `CDC_DB_PASSWORD` | fr / - | CDC 账号 |
| `CDC_DB_INCLUDE` | XH_E10 | 捕获库列表 |
| `CDC_TABLE_INCLUDE` | dbo.PURCHASE_ORDER | 捕获表列表（schema.table） |
| `CDC_OFFSET_FILE` | data/cdc-offsets-sqlserver.dat | 位点文件 |
| `CDC_SNAPSHOT_MODE` | schema_only | 快照模式 |

> **注意**：环境变量已导出时会覆盖 yml 默认值。改了 yml 默认值不生效时优先检查运行环境残留变量。
