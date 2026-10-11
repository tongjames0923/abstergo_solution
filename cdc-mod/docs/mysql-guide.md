# MySQL CDC 部署与使用指南

> 适用模块：`cdc-mod/cdc-mysql-impl`（基于 Debezium Embedded Engine 3.0.x + MySQL binlog）
> 阅读前提：无。本文从数据库准备到业务监听一条龙覆盖，按步骤执行即可开箱即用。
> 通用契约（`CdcEvent` 结构、过滤、分发、幂等约定）见模块根 `README.md`，本文不重复。

---

## 1. 捕获原理与版本要求

连接器伪装成 MySQL 复制从库，读取 **binlog 事件流**还原行级变更。

| 要求 | 说明 |
|------|------|
| 数据库版本 | MySQL 8.x（5.7 可用，8.0+ 体验更佳） |
| binlog | `log_bin=ON` 且 `binlog_format=ROW`（STATEMENT/MIXED 无法还原行级变更） |
| 行镜像 | `binlog_row_image=FULL`（捕获完整前后镜像） |
| server_id | 非 0 且在复制拓扑内唯一（连接器的 `server-id` 同样必须全局唯一，**高危配置**） |
| GTID | 可选推荐（位点切换、主从漂移后恢复更稳） |

---

## 2. 数据库侧准备（一次性）

初始化脚本全文见 `etc/sql_file/cdc_user_init.sql` 第一节，此处为按序执行的最小步骤。

### 2.1 前置检查（逐条执行，全部满足才继续）

```sql
SHOW VARIABLES LIKE 'log_bin';               -- 必须 ON
SHOW VARIABLES LIKE 'binlog_format';         -- 必须 ROW
SHOW VARIABLES LIKE 'binlog_row_image';      -- 建议 FULL
SHOW VARIABLES LIKE 'server_id';             -- 非 0 且拓扑内唯一
SHOW VARIABLES LIKE 'binlog_expire_logs_seconds';  -- 需覆盖最长停机窗口
SHOW MASTER STATUS;                          -- 能返回 File/Position 即正常（8.4+ 用 SHOW BINARY LOG STATUS）
```

### 2.2 开启 / 修改 binlog（未满足时）

`log_bin` 无法动态开启，需写配置文件后重启：

```ini
# /etc/my.cnf（Windows 为 my.ini）
[mysqld]
server-id=1
log-bin=mysql-bin
binlog_format=ROW
binlog_row_image=FULL
binlog_row_metadata=FULL            # 8.0+ 推荐
binlog_expire_logs_seconds=604800   # 按停机窗口调整，示例 7 天
```

```sql
-- 以下参数可动态生效（免重启）
SET PERSIST binlog_format = ROW;
SET PERSIST binlog_row_image = FULL;
```

### 2.3 创建 CDC 专用账号（最小权限）

```sql
CREATE USER IF NOT EXISTS 'cdc_user'@'%' IDENTIFIED BY 'changeit';   -- 替换口令
-- 增量捕获仅此两项权限，不授业务库读写
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'cdc_user'@'%';
FLUSH PRIVILEGES;

-- 仅当 snapshot.mode=initial（全量快照）时才额外放开：
-- GRANT SELECT ON `core_app`.* TO 'cdc_user'@'%';
```

---

## 3. 应用侧部署

### 3.1 Maven 依赖（主应用 pom）

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>cdc-mysql-impl</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 3.2 启动类装配

```java
@EnableTbsCdc
@EnableCdcMysql
@SpringBootApplication(scanBasePackages = {"com.tbs.app"})
public class AppApplication { /* ... */ }
```

### 3.3 配置（`application-tbs.yml`）

```yaml
tbs:
  # 总开关默认启用：@EnableTbsCdc 注解引入即生效；如需关闭可配置 enable: false（false 时零 Bean 装配、零线程占用）
  cdc:
    # 捕获源由引入的实现子模块（cdc-mysql-impl）决定，无需声明类型
    # 事件载荷裁剪：full 前后完整行 / changed-only UPDATE 仅差异列 / key-only 仅主键
    payload-mode: changed-only
    mysql:
      hostname: ${CDC_DB_HOST:127.0.0.1}
      port: ${CDC_DB_PORT:3306}
      # CDC 专用最小权限账号，走环境变量注入，勿提交真实口令
      username: ${CDC_DB_USERNAME:cdc_user}
      password: ${CDC_DB_PASSWORD:}
      # Debezium 逻辑服务名（topic 前缀/源标识），全局唯一
      server-name: tbs_cdc
      # 【高危】连接器伪装从库的 server-id：必须全局唯一且落在 [2,4294967294]，
      # 与 MySQL 集群其他复制节点冲突会导致复制流被顶掉；启动期已断言非空与合法范围
      server-id: ${CDC_SERVER_ID:184001}
      # 捕获的数据库列表，逗号分隔（留空表示不限制）
      database-include-list: core_app
      # 【关键格式】捕获的表白名单：db.table 格式，逗号分隔（如 core_app.sys_user）；
      # 仅写表名不会命中；留空表示库内全表
      table-include-list: core_app.sys_user
      # 消费位点持久化文件：生产建议挂持久卷
      offset-storage-file: data/cdc-offsets.dat
      # 快照模式：never 仅增量；initial 全量快照+增量（需额外 SELECT 权限）
      # 首次部署推荐先 schema_only 快照结构，稳定后改回 never（见第 5 节）
      snapshot-mode: never
      # 心跳间隔（毫秒）：无变更时产生心跳事件推进位点
      heartbeat-interval-ms: 5000
    # 以下为通用段（过滤/分发/批量/错误处理），完整语义见模块 README 第 4 节
    filter:
      include-databases: []
      exclude-databases: []
      include-table-patterns: []         # 表级正则白名单（匹配纯表名或 db.table）
      exclude-table-patterns: []
      row-conditions: {}                 # 行级 SpEL，key=db.table，false 则丢弃
    dispatch:
      in-process-mode: async             # sync / async（仅进程内分发，跨服务分发业务侧自行对接 MQ）
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
public CdcChangeListener sysUserListener() {
    return new SysUserListener();
}

public class SysUserListener implements CdcChangeListener {
    @Override
    public CdcSubscription getSubscription() {
        return CdcSubscription.builder()
            // 正则，同时匹配纯表名与 db.table 两种形式
            .tablePatterns(Set.of("sys_user"))
            .changeTypes(EnumSet.of(CdcChangeType.UPDATE, CdcChangeType.DELETE))
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
    @CdcListener(tables = {"sys_user"}, types = {CdcChangeType.CREATE})
    public void onChange(CdcEvent event) { /* ... */ }
}
```

### 4.3 `CdcEvent` 关键字段

| 字段 | 说明 |
|------|------|
| `eventId` | 事件唯一标识，消费端幂等去重依据 |
| `changeType` | `CREATE` / `UPDATE` / `DELETE`（`READ` 仅枚举预留，日志流机制捕获不到） |
| `database` / `table` | 库名（`core_app`）/ 纯表名（`sys_user`） |
| `primaryKey` | 主键列值 |
| `beforeData` / `afterData` | 变更前/后行数据（受 `payload-mode` 裁剪） |
| `sourceTimestamp` / `captureTimestamp` | 源库变更时间 / 捕获时间 |
| `transaction` / `position` | 事务信息 / binlog 位点 |

---

## 5. 首次启动流程（重要）

binlog 事件本身不含完整表结构，**首次部署推荐按此顺序**：

1. `snapshot-mode: schema_only` 启动（仅快照表结构，不重放存量数据）；
2. 对捕获表做一次 UPDATE，确认 `CDC EVENT` 日志输出，全链路打通；
3. 改回 `snapshot-mode: never`（纯增量）并重启——**前提是保留位点与结构历史文件**；
4. 若需要存量数据一次性入仓/初始化，用 `snapshot-mode: initial`（需给 CDC 账号补 SELECT 权限），完成后改回 `never`。

---

## 6. 故障排查

| 现象 | 根因 | 处理 |
|------|------|------|
| 连接器报 binlog 权限/连接失败 | 账号缺 `REPLICATION SLAVE/CLIENT` | 按 2.3 授权 |
| 启动断言 server-id 相关错误 | `server-id` 未配置 / 越界 / 与其他复制节点冲突 | 唯一化处理（高危项） |
| 启动报表结构缺失、无法反序列化行事件 | `never` 模式 + 位点/结构历史为空 | 先用 `schema_only` 快照结构（见第 5 节） |
| `The db history topic is missing` | 位点文件与结构历史文件状态不同步（只删了其中一个） | **成对删除**两个文件后重启：`cdc-offsets.dat` 与 `cdc-offsets-dbhistory.dat`；正常重启永远不需要删 |
| 连接器正常启动但零事件 | binlog 未开 ROW / include list 不命中 / 环境变量覆盖 yml | 按 `binlog_format` → include 格式（db.table）→ 环境变量顺序排查 |
| 事件只有主键没有行数据 | `payload-mode: key-only` | 按需改 `full` / `changed-only` |

---

## 7. 位点与结构历史文件（运维要点）

- **成对文件**：`{offset-storage-file}` 与同目录派生的 `{去后缀}-dbhistory.dat` 是状态绑定的一对——
  位点记录 binlog 消费进度，结构历史记录对应时点的表结构，启动校验要求二者一致。
- **规则一句话：要么都别动，要么一起删。** 正常重启（两文件完整）不需要任何手动操作。
- 删除位点文件且 `snapshot.mode=initial` 会触发全量重新消费；`never` 模式下删除位点将从当前 binlog 起点增量。
- 生产环境两个文件都挂持久卷。

---

## 8. 与 SQL Server 模块的差异速查

| 差异点 | MySQL | SQL Server |
|--------|-------|------------|
| 捕获机制 | binlog（伪装从库） | 原生 CDC + Agent 作业 |
| 实现子模块 | `cdc-mysql-impl` | `cdc-sqlserver-impl` |
| 装配注解 | `@EnableCdcMysql` | `@EnableCdcSqlserver` |
| include list 格式 | `db.table`（如 `core_app.sys_user`） | `schema.table`（如 `dbo.PURCHASE_ORDER`，**不带库名**） |
| 必填高危项 | `server-id` 全局唯一 | Agent 必须运行；中文实例 Agent 名已内置兼容 |
| 首次部署快照 | `schema_only` → 改回 `never` | `schema_only`（SQL Server 不支持 never） |
| 位点重置开关 | 无（成对手动删除两个文件） | `tbs.cdc.sqlserver.reset-offset` |
