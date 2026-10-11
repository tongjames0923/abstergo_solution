package com.tbs.cdc.sqlserver.config;

import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

/**
 * Debezium配置工厂（SQL Server）
 * <p>
 * 将{@link CdcProperty.Sqlserver}段映射为Debezium嵌入式引擎的连接与SQL Server连接器配置属性，
 * 负责启动期高危项校验（连接信息完整性）与位点/结构历史文件目录的准备。
 * 产出{@link Properties}供{@code DebeziumEngine}装配。
 * </p>
 * <p>
 * 与MySQL连接器的关键差异：SQL Server无需{@code database.server.id}；连接加密由
 * {@code database.encrypt}/{@code database.trustServerCertificate}控制（新版mssql-jdbc驱动默认强制加密，
 * 本地/自签环境常需关闭）；捕获前提为目标库表已启用SQL Server CDC且SQL Server Agent运行中，
 * Agent状态探测查询默认兼容中英文服务名（中文本地化实例服务名为"SQL Server 代理 (...)"），
 * 可经{@code tbs.cdc.sqlserver.agent-status-query}覆盖。
 * 属性键按Debezium 3.0.x配置，升级大版本时需对齐键名，详见模块README。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
public class DebeziumConfigFactory {

    /**
     * SQL Server连接器实现类
     */
    private static final String CONNECTOR_CLASS = "io.debezium.connector.sqlserver.SqlServerConnector";

    /**
     * 文件位点存储实现
     */
    private static final String OFFSET_STORE_CLASS = "org.apache.kafka.connect.storage.FileOffsetBackingStore";

    /**
     * 文件结构历史存储实现
     * <p>
     * Debezium 3.x下文件型结构历史由{@code debezium-storage-file}（随连接器传递引入）的
     * {@code FileSchemaHistory}承载，属性键为{@code schema.history.internal.file.filename}。
     * </p>
     */
    private static final String SCHEMA_HISTORY_CLASS = "io.debezium.storage.file.history.FileSchemaHistory";

    /**
     * Agent状态探测查询的Debezium配置键
     * <p>
     * 连接器经SqlServerJdbcConfiguration读取JDBC配置时只保留database.*前缀子集，
     * 故实际键须带database.前缀（已实测验证：顶层键sqlserver.agent.status.query会被静默忽略）。
     * </p>
     */
    private static final String AGENT_STATUS_QUERY_KEY = "database.sqlserver.agent.status.query";

    /**
     * Agent状态探测查询的中英服务名兼容默认值
     * <p>
     * Debezium默认查询仅匹配英文服务名（LIKE N'SQL Server Agent (%'），中文本地化SQL Server实例的
     * 服务名为"SQL Server 代理 (...)"会导致探测返回0行并使连接器停止；
     * 此默认值在官方语义基础上扩展中文分支，{@code [#db]}占位符由连接器替换为当前库名。
     * </p>
     */
    private static final String DEFAULT_AGENT_STATUS_QUERY = "SELECT CASE WHEN dss.[status]=4 THEN 1 ELSE 0 END "
        + "AS isRunning FROM [#db].sys.dm_server_services dss "
        + "WHERE dss.[servicename] LIKE N'SQL Server Agent (%' "
        + "OR dss.[servicename] LIKE N'SQL Server 代理 (%'";

    /**
     * 由CDC配置构建Debezium引擎属性
     *
     * @param cdcProperty CDC配置
     * @return Debezium配置属性集
     */
    public Properties build(CdcProperty cdcProperty) {
        CdcProperty.Sqlserver sqlserver = cdcProperty.getSqlserver();
        validate(sqlserver);

        String offsetFile = sqlserver.getOffsetStorageFile();
        String historyFile = deriveHistoryFile(offsetFile);
        if (sqlserver.isResetOffset()) {
            resetOffsetFiles(offsetFile, historyFile);
        }
        prepareParentDir(offsetFile);
        prepareParentDir(historyFile);

        Properties props = new Properties();
        props.setProperty("name", sqlserver.getServerName() + "-connector");
        props.setProperty("connector.class", CONNECTOR_CLASS);
        props.setProperty("database.hostname", sqlserver.getHostname());
        props.setProperty("database.port", String.valueOf(sqlserver.getPort()));
        props.setProperty("database.user", sqlserver.getUsername());
        props.setProperty("database.password", sqlserver.getPassword());
        props.setProperty("database.encrypt", String.valueOf(sqlserver.isEncrypt()));
        props.setProperty("database.trustServerCertificate", String.valueOf(sqlserver.isTrustServerCertificate()));
        props.setProperty("topic.prefix", sqlserver.getServerName());
        // SQL Server连接器无database.include.list，以必填的database.names声明要捕获的库（逗号分隔）
        props.setProperty("database.names", sqlserver.getDatabaseIncludeList());
        // table.include.list在Debezium 3.0.8实测匹配格式为schema.table（不带数据库名前缀），仅表名不命中
        if (StrUtil.isNotBlank(sqlserver.getTableIncludeList())) {
            props.setProperty("table.include.list", sqlserver.getTableIncludeList());
        }
        props.setProperty("snapshot.mode", sqlserver.getSnapshotMode());
        props.setProperty("heartbeat.interval.ms", String.valueOf(sqlserver.getHeartbeatIntervalMs()));
        // 值编码模式：金额类可读字符串、二进制类可读十六进制，避免MQ载荷出现base64“乱码”
        props.setProperty("decimal.handling.mode", sqlserver.getDecimalHandlingMode());
        props.setProperty("binary.handling.mode", sqlserver.getBinaryHandlingMode());
        // Agent状态探测：默认中英服务名兼容，允许业务方按环境覆盖
        var agentStatusQuery = StrUtil.isBlank(sqlserver.getAgentStatusQuery())
            ? DEFAULT_AGENT_STATUS_QUERY
            : sqlserver.getAgentStatusQuery();
        props.setProperty(AGENT_STATUS_QUERY_KEY, agentStatusQuery);

        props.setProperty("offset.storage", OFFSET_STORE_CLASS);
        props.setProperty("offset.storage.file.filename", offsetFile);
        props.setProperty("offset.flush.interval.ms", "1000");

        props.setProperty("schema.history.internal", SCHEMA_HISTORY_CLASS);
        props.setProperty("schema.history.internal.file.filename", historyFile);
        props.setProperty("schema.history.internal.store.only.captured.tables.ddl", "true");

        log.info("Debezium SQL Server配置就绪, topic.prefix:{}, database.include.list:{}, offset.file:{}",
            sqlserver.getServerName(), sqlserver.getDatabaseIncludeList(), offsetFile);
        return props;
    }

    /**
     * 校验连接信息等高危配置项
     *
     * @param sqlserver SQL Server配置段
     */
    private void validate(CdcProperty.Sqlserver sqlserver) {
        AssertUtil.isTrue(StrUtil.isBlank(sqlserver.getHostname()) || StrUtil.isBlank(sqlserver.getUsername())
            || StrUtil.isBlank(sqlserver.getPassword())).i18n(CdcI18nConstant.SQLSERVER_CONNECT_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(sqlserver.getDatabaseIncludeList()))
            .i18n(CdcI18nConstant.SQLSERVER_DATABASE_NAMES_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
    }

    /**
     * 由位点文件路径派生结构历史文件路径（同目录，后缀-dbhistory.dat）
     *
     * @param offsetFile 位点文件路径
     * @return 结构历史文件路径
     */
    private String deriveHistoryFile(String offsetFile) {
        int idx = offsetFile.lastIndexOf('.');
        String base = idx > 0 ? offsetFile.substring(0, idx) : offsetFile;
        return base + "-dbhistory.dat";
    }

    /**
     * 成对重置位点与结构历史文件
     * <p>
     * 两个文件是一对状态绑定的存储：位点记录消费进度，结构历史记录对应时点的表结构，
     * 启动校验要求二者协调一致。单独删除其一会导致启动报"The db history topic is missing"，
     * 因此重置必须成对执行。重置后连接器全新启动并按snapshot.mode重新初始化。
     * </p>
     *
     * @param offsetFile 位点文件路径
     * @param historyFile 结构历史文件路径
     */
    private void resetOffsetFiles(String offsetFile, String historyFile) {
        log.warn("CDC位点重置开关已开启, 成对删除位点与结构历史文件: {}, {}", offsetFile, historyFile);
        for (String file : java.util.List.of(offsetFile, historyFile)) {
            var target = new File(file);
            if (target.exists() && !target.delete()) {
                throw AssertUtil.isTrue(true).i18n(CdcI18nConstant.OFFSET_DIR_FAILED, target.getAbsolutePath())
                    .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
            }
        }
        log.warn("位点已重置, 连接器将全新启动; 请将tbs.cdc.sqlserver.reset-offset置回false, 否则每次重启都会丢弃消费位点");
    }

    /**
     * 确保目标文件的父目录存在，创建失败抛出断言异常
     *
     * @param filePath 文件路径
     */
    private void prepareParentDir(String filePath) {
        File parent = new File(filePath).getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw AssertUtil.isTrue(true).i18n(CdcI18nConstant.OFFSET_DIR_FAILED, parent.getAbsolutePath())
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
        }
    }
}
