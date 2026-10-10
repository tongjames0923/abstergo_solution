-- =============================================================================
-- CDC 专用账号与最小权限初始化脚本（MySQL 8.x）
-- =============================================================================
-- 用途：为数据库变更数据捕获（CDC）模块创建仅用于读取 binlog 日志流的最小权限账号。
-- 前提：MySQL 已开启 binlog（log_bin=ON、binlog_format=ROW、binlog_row_image=FULL）。
--
-- 权限模型：
--   * REPLICATION SLAVE  —— 允许作为复制从节点连接并读取 binlog 事件流
--   * REPLICATION CLIENT —— 允许执行 SHOW MASTER STATUS 等以获取当前 binlog 位点
--   本账号不授予任何业务库读写权限，仅覆盖增量捕获所需。
--
-- 快照模式说明：
--   * snapshot.mode=never（本模块默认）：仅增量，下方两条权限即可，无需 SELECT。
--   * snapshot.mode=initial：启动时执行一次一致性快照，需额外授予捕获库的 SELECT，
--     并可能需要 RELOAD / SHOW DATABASES / LOCK TABLES（取决于是否启用无锁快照），
--     请按需放开文末注释行，生产环境建议优先使用 never + 外部初始化基线数据。
--
-- 安全提示：请修改 IDENTIFIED BY 中的默认口令，并通过环境变量 CDC_DB_PASSWORD 注入应用，
--          不要把真实口令提交进代码库。
-- =============================================================================

-- 创建 CDC 专用账号（口令占位符，部署时替换）
CREATE USER IF NOT EXISTS 'cdc_user'@'%' IDENTIFIED BY 'changeit';

-- 授予最小复制权限（增量捕获所需）
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'cdc_user'@'%';

FLUSH PRIVILEGES;

-- -----------------------------------------------------------------------------
-- 仅当 snapshot.mode=initial（全量快照 + 增量）时才需要放开以下权限：
-- -----------------------------------------------------------------------------
-- GRANT SELECT ON `core_app`.* TO 'cdc_user'@'%';
-- GRANT RELOAD, SHOW DATABASES, PROCESS ON *.* TO 'cdc_user'@'%';

-- 校验授权结果
-- SHOW GRANTS FOR 'cdc_user'@'%';
