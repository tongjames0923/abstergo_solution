-- =============================================================================
-- CDC 前置条件检查 + 专用账号与最小权限初始化脚本（多数据库 Runbook）
-- =============================================================================
-- 覆盖：MySQL 8.x / SQL Server 2016 SP1+ / Oracle 12c+（LogMiner）/ PostgreSQL 10+
-- 注意：本文件按数据库方言分节，各节语句只能在对应数据库的客户端中执行，
--       不能把整个文件一次性丢给某一个数据库执行。
--
-- 快照模式说明：
--   * snapshot.mode=never（本模块默认）：仅增量，最小权限即可，无需业务库 SELECT。
--   * snapshot.mode=initial：启动时执行一次一致性快照，需额外授予捕获库的 SELECT，
--     各节中标注了"快照用"的权限/配置请按需放开。
--
-- 安全提示：请修改所有 IDENTIFIED BY / PASSWORD 中的默认口令，并通过环境变量
--          CDC_DB_PASSWORD 注入应用，不要把真实口令提交进代码库。
-- =============================================================================


-- #############################################################################
-- # 一、MySQL 8.x
-- #############################################################################
-- 捕获原理：作为伪装从库读取 binlog 事件流（binlog_format 必须为 ROW）。
-- ---------------------------------------------------------------------------
-- 1. 前置检查（逐条执行，全部满足才可用）
-- ---------------------------------------------------------------------------
-- 1.1 binlog 是否开启（log_bin 必须为 ON）
SHOW VARIABLES LIKE 'log_bin';

-- 1.2 binlog 格式必须为 ROW（STATEMENT/MIXED 无法还原行级变更）
SHOW VARIABLES LIKE 'binlog_format';

-- 1.3 行镜像建议为 FULL（捕获完整前后镜像）
SHOW VARIABLES LIKE 'binlog_row_image';

-- 1.4 server_id 必须非 0，且在复制拓扑内唯一
SHOW VARIABLES LIKE 'server_id';

-- 1.5 GTID（可选，推荐开启：位点切换、主从漂移后恢复更稳）
SHOW VARIABLES LIKE 'gtid_mode';
SHOW VARIABLES LIKE 'enforce_gtid_consistency';

-- 1.6 binlog 保留时长（默认 8.0 为 30 天，5.7 为永不过期；需覆盖最长停机窗口）
SHOW VARIABLES LIKE 'binlog_expire_logs_seconds';

-- 1.7 校验当前 binlog 位点（应能返回 File / Position / Binlog_Do_DB 等列）
SHOW MASTER STATUS;            -- MySQL 8.0.x
-- SHOW BINARY LOG STATUS;     -- MySQL 8.4+ 改名后的写法

-- ---------------------------------------------------------------------------
-- 2. 开启 / 修改方式
-- ---------------------------------------------------------------------------
-- 2.1 log_bin 本身无法通过 SQL 动态开启，必须写配置文件后重启：
--     Linux: /etc/my.cnf 或 /etc/mysql/my.cnf
--     Windows: 安装目录下的 my.ini
--
--     [mysqld]
--     server-id=1                        # 复制拓扑内唯一
--     log-bin=mysql-bin                  # 开启 binlog
--     binlog_format=ROW                  # 行级日志（CDC 必须）
--     binlog_row_image=FULL              # 完整前后镜像
--     binlog_row_metadata=FULL           # MySQL 8.0+，Debezium 解析列名/DDL 更稳（可选推荐）
--     gtid_mode=ON                       # 可选推荐
--     enforce_gtid_consistency=ON        # 与 gtid_mode 配套
--     binlog_expire_logs_seconds=604800  # 按停机窗口调整，示例 7 天
--
--     重启命令示例（Linux）：systemctl restart mysqld
--
-- 2.2 以下参数是动态的，可免重启立即生效（SET PERSIST 会同时持久化到 mysqld-auto.cnf）：
-- SET PERSIST binlog_format = ROW;
-- SET PERSIST binlog_row_image = FULL;
-- SET PERSIST binlog_expire_logs_seconds = 604800;

-- ---------------------------------------------------------------------------
-- 3. 创建 CDC 专用账号（口令占位符，部署时替换）
-- ---------------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'cdc_user'@'%' IDENTIFIED BY 'changeit';

-- 授予最小复制权限（增量捕获所需）：
--   * REPLICATION SLAVE  —— 允许作为复制从节点连接并读取 binlog 事件流
--   * REPLICATION CLIENT —— 允许执行 SHOW MASTER STATUS 等以获取当前 binlog 位点
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'cdc_user'@'%';

FLUSH PRIVILEGES;

-- 仅当 snapshot.mode=initial（全量快照 + 增量）时才需要放开以下权限：
-- GRANT SELECT ON `core_app`.* TO 'cdc_user'@'%';
-- GRANT RELOAD, SHOW DATABASES, PROCESS ON *.* TO 'cdc_user'@'%';  -- 无锁快照需要

-- 校验授权结果
-- SHOW GRANTS FOR 'cdc_user'@'%';


-- #############################################################################
-- # 二、SQL Server 2016 SP1 及以上
-- #############################################################################
-- 捕获原理：使用 SQL Server 原生 CDC（基于事务日志的捕获作业），不支持 Express 版。
-- ---------------------------------------------------------------------------
-- 1. 前置检查（逐条执行）
-- ---------------------------------------------------------------------------
-- 1.1 检查版本（要求 2016 SP1+，即 13.0.4001 及以上；标准版即可）
SELECT SERVERPROPERTY('ProductVersion') AS product_version;

-- 1.2 检查数据库级 CDC 是否开启（is_cdc_enabled 必须为 1）
SELECT name, is_cdc_enabled FROM sys.databases;

-- 1.3 检查 SQL Server Agent 是否运行（捕获/清理作业依赖它，status_desc 应为 Running）
SELECT servicename, status_desc
FROM sys.dm_server_services
WHERE servicename LIKE 'SQL Server Agent%';
-- 若未运行（Windows）：net start "SQL Server Agent (MSSQLSERVER)"
-- 若未运行（Linux）：sudo systemctl start mssql-server-agent

-- 1.4 检查表是否已被 CDC 跟踪（is_tracked_by_cdc = 1）
SELECT s.name AS schema_name, t.name AS table_name, t.is_tracked_by_cdc
FROM sys.tables t
JOIN sys.schemas s ON t.schema_id = s.schema_id
ORDER BY t.name;

-- 1.5 查看 CDC 捕获/清理作业配置
EXEC msdb.dbo.sp_help_cdc_jobs;

-- ---------------------------------------------------------------------------
-- 2. 开启 / 修改语句（开启库/表 CDC 需要 sysadmin 权限）
-- ---------------------------------------------------------------------------
-- 2.1 开启数据库级 CDC（要求库上无其他独占连接；开启后库内会生成 cdc 架构与作业）
USE core_app;
GO
EXEC sys.sp_cdc_enable_db;
GO

-- 2.2 开启表级 CDC（对每张需要捕获的表执行）
--     @role_name：生成的访问角色名，授权给 CDC 账号即可读取该表的变更数据
--     建议：表必须有主键（或唯一索引），否则 UPDATE/DELETE 只能按全行匹配
EXEC sys.sp_cdc_enable_table
    @source_schema     = N'dbo',
    @source_name       = N'your_table',
    @role_name         = N'cdc_admin',
    @supports_net_changes = 1;   -- 需要主键/唯一索引
GO

-- 2.3 查看已捕获的表与捕获列（校验用）
EXEC sys.sp_cdc_help_change_data_capture;

-- 2.4 回滚操作（按需）：
-- EXEC sys.sp_cdc_disable_table @source_schema = N'dbo', @source_name = N'your_table', @capture_instance = N'dbo_your_table';
-- EXEC sys.sp_cdc_disable_db;

-- ---------------------------------------------------------------------------
-- 3. 创建 CDC 专用账号（最小权限建议 db_owner 于捕获库；亦可按注释降权）
-- ---------------------------------------------------------------------------
USE master;
GO
CREATE LOGIN cdc_user WITH PASSWORD = N'changeit', CHECK_POLICY = OFF;
GO
USE core_app;
GO
CREATE USER cdc_user FOR LOGIN cdc_user;
GO
-- 方式 A（推荐，Debezium 官方要求）：捕获库 db_owner
EXEC sp_addrolemember @rolename = N'db_owner', @membername = N'cdc_user';
GO
-- 方式 B（降权方案，按需替换方式 A）：
--   EXEC sp_addrolemember @rolename = N'cdc_admin', @membername = N'cdc_user';  -- 2.2 中 @role_name 生成的角色
--   GRANT SELECT ON SCHEMA :: [cdc] TO cdc_user;                                -- 读取捕获实例表
--   GRANT EXECUTE ON SCHEMA :: [cdc] TO cdc_user;                               -- cdc.fn_cdc_get_net_changes_* 等函数
--   GRANT SELECT ON SCHEMA :: [dbo] TO cdc_user;                                -- snapshot.mode=initial 快照用
GO


-- #############################################################################
-- # 三、Oracle 12c 及以上（LogMiner 模式）
-- #############################################################################
-- 捕获原理：LogMiner 从在线/归档 redo 日志挖掘变更（Debezium oracle LogMiner 连接器）。
-- 以下 1~2 节需以 SYS（sysdba）执行；多租户（CDB）架构下请连接到具体 PDB。
-- ---------------------------------------------------------------------------
-- 1. 前置检查（逐条执行）
-- ---------------------------------------------------------------------------
-- 1.1 检查版本
SELECT banner FROM v$version;

-- 1.2 检查归档模式（log_mode 必须为 ARCHIVELOG；NOARCHIVELOG 不可用）
--     cdb = YES 表示多租户架构，连接串需指向具体 PDB
SELECT log_mode, cdb FROM v$database;

-- 1.3 检查数据库级最小补充日志（必须为 YES，否则 redo 中没有足够的前镜像信息）
SELECT supplemental_log_data_min FROM v$database;

-- 1.4 检查表级补充日志（LogMiner 需要 ALL 列级补充日志才能可靠识别 UPDATE/DELETE）
SELECT owner, log_group_name, table_name, always, derived_from
FROM dba_log_groups
WHERE owner = 'CORE_APP';

-- 1.5 检查回收站参数（旧版本 LogMiner 与闪回查询建议关闭）
SHOW PARAMETER recyclebin;

-- ---------------------------------------------------------------------------
-- 2. 开启 / 修改语句
-- ---------------------------------------------------------------------------
-- 2.1 开启归档模式（需 sysdba + 重启数据库到 MOUNT 状态，请安排停机窗口）：
-- SHUTDOWN IMMEDIATE;
-- STARTUP MOUNT;
-- ALTER DATABASE ARCHIVELOG;
-- ALTER DATABASE OPEN;

-- 2.2 开启数据库级最小补充日志（免重启，立即生效）
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;

-- 2.3 对捕获表开启全列补充日志（每张捕获表执行一次）
ALTER TABLE core_app.your_table ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;

-- 2.4 关闭回收站（写入 SPFILE 后需重启生效；仅旧版本/闪回查询冲突时需要）
-- ALTER SYSTEM SET recyclebin = OFF SCOPE = SPFILE;

-- ---------------------------------------------------------------------------
-- 3. 创建 CDC 专用账号并授权（以 SYS 执行）
-- ---------------------------------------------------------------------------
CREATE USER cdc_user IDENTIFIED BY "changeit";

-- 基础登录与 LogMiner 挖掘权限（增量必需）
GRANT CREATE SESSION         TO cdc_user;
GRANT LOGMINING              TO cdc_user;   -- LogMiner 相关包权限
GRANT SELECT ANY TRANSACTION TO cdc_user;   -- 读取事务/undo 信息
GRANT SELECT ANY DICTIONARY  TO cdc_user;   -- 读取 v$database / dba_* 等数据字典
GRANT EXECUTE_CATALOG_ROLE   TO cdc_user;   -- 执行 DBMS_LOGMNR 等 SYS 所属包

-- 仅当 snapshot.mode=initial（快照）时需要：
-- GRANT SELECT_CATALOG_ROLE TO cdc_user;            -- 快照元数据
-- GRANT FLASHBACK ANY TABLE TO cdc_user;            -- 一致性读快照（闪回查询）

-- 读取捕获表数据（快照必需；增量模式下按需收敛到具体表）
-- GRANT SELECT ANY TABLE TO cdc_user;               -- 粗粒度
GRANT SELECT ON core_app.your_table TO cdc_user;      -- 细粒度（推荐）


-- #############################################################################
-- # 四、PostgreSQL 10+（附：pgoutput 逻辑复制）
-- #############################################################################
-- 捕获原理：逻辑复制（logical decoding，pgoutput 插件）+ 复制槽。
-- ---------------------------------------------------------------------------
-- 1. 前置检查
-- ---------------------------------------------------------------------------
-- 1.1 wal_level 必须为 logical（默认为 replica，不满足）
SHOW wal_level;

-- 1.2 复制槽与发送进程上限（需大于连接器数量）
SHOW max_replication_slots;
SHOW max_wal_senders;

-- ---------------------------------------------------------------------------
-- 2. 开启 / 修改方式（写入 postgresql.conf 后重启；RDS 用参数组 rds.logical_replication=1）
-- ---------------------------------------------------------------------------
-- wal_level = logical
-- max_replication_slots = 10
-- max_wal_senders = 10
-- # 快照一致性要求：old_snapshot_threshold = 0（禁用旧快照上限）
-- 重启命令示例：sudo systemctl restart postgresql

-- ---------------------------------------------------------------------------
-- 3. 创建 CDC 专用账号（以超级用户执行）
-- ---------------------------------------------------------------------------
CREATE ROLE cdc_user WITH LOGIN PASSWORD 'changeit';
ALTER ROLE cdc_user WITH REPLICATION;                 -- 建立复制槽需要
GRANT SELECT ON ALL TABLES IN SCHEMA public TO cdc_user;   -- 快照/初始位面用
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cdc_user;
