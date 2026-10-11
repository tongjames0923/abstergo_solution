package com.tbs.cdc.mysql.config;

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
 * Debezium配置工厂
 * <p>
 * 将{@link CdcProperty}的MySQL段映射为Debezium嵌入式引擎的连接与连接器配置属性，
 * 负责启动期高危项校验（server-id非空与合法范围、连接信息完整性）与位点/结构历史
 * 文件目录的准备。产出{@link Properties}供{@code DebeziumEngine}装配。
 * </p>
 * <p>
 * 注意：Debezium 2.x/3.x对部分连接器属性做了重命名（如{@code topic.prefix}、
 * {@code schema.history.internal}），本工厂按3.0.x键名配置，升级Debezium大版本时需对齐键名，
 * 详见模块README。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true")
public class DebeziumConfigFactory {

    /**
     * MySQL连接器实现类
     */
    private static final String CONNECTOR_CLASS = "io.debezium.connector.mysql.MySqlConnector";

    /**
     * 文件位点存储实现
     */
    private static final String OFFSET_STORE_CLASS = "org.apache.kafka.connect.storage.FileOffsetBackingStore";

    /**
     * 文件结构历史存储实现
     * <p>
     * Debezium 3.x 已删除2.x的{@code io.debezium.relational.history.FileDatabaseHistory}，
     * 文件型结构历史改由{@code debezium-storage-file}（随{@code debezium-connector-mysql}传递引入）的
     * {@code FileSchemaHistory} 承载，属性键仍为{@code schema.history.internal.file.filename}。
     * 升级Debezium大版本时需重新校对实现类与属性键。
     * </p>
     */
    private static final String SCHEMA_HISTORY_CLASS = "io.debezium.storage.file.history.FileSchemaHistory";

    /**
     * server-id合法下界
     */
    private static final int SERVER_ID_MIN = 2;

    /**
     * server-id合法上界
     */
    private static final long SERVER_ID_MAX = 4294967294L;

    /**
     * 由CDC配置构建Debezium引擎属性
     *
     * @param cdcProperty CDC配置
     * @return Debezium配置属性集
     */
    public Properties build(CdcProperty cdcProperty) {
        CdcProperty.Mysql mysql = cdcProperty.getMysql();
        validate(mysql);

        String offsetFile = mysql.getOffsetStorageFile();
        String historyFile = deriveHistoryFile(offsetFile);
        prepareParentDir(offsetFile);
        prepareParentDir(historyFile);

        Properties props = new Properties();
        props.setProperty("name", mysql.getServerName() + "-connector");
        props.setProperty("connector.class", CONNECTOR_CLASS);
        props.setProperty("database.hostname", mysql.getHostname());
        props.setProperty("database.port", String.valueOf(mysql.getPort()));
        props.setProperty("database.user", mysql.getUsername());
        props.setProperty("database.password", mysql.getPassword());
        props.setProperty("topic.prefix", mysql.getServerName());
        props.setProperty("database.server.id", String.valueOf(mysql.getServerId()));
        if (StrUtil.isNotBlank(mysql.getDatabaseIncludeList())) {
            props.setProperty("database.include.list", mysql.getDatabaseIncludeList());
        }
        if (StrUtil.isNotBlank(mysql.getTableIncludeList())) {
            props.setProperty("table.include.list", mysql.getTableIncludeList());
        }
        props.setProperty("snapshot.mode", mysql.getSnapshotMode());
        props.setProperty("heartbeat.interval.ms", String.valueOf(mysql.getHeartbeatIntervalMs()));

        props.setProperty("offset.storage", OFFSET_STORE_CLASS);
        props.setProperty("offset.storage.file.filename", offsetFile);
        props.setProperty("offset.flush.interval.ms", "1000");

        props.setProperty("schema.history.internal", SCHEMA_HISTORY_CLASS);
        props.setProperty("schema.history.internal.file.filename", historyFile);
        props.setProperty("schema.history.internal.store.only.captured.tables.ddl", "true");

        // 位点文件删除且快照模式为initial时才会触发全量重放，never仅增量
        log.info("Debezium MySQL配置就绪, topic.prefix:{}, database.include.list:{}, offset.file:{}",
            mysql.getServerName(), mysql.getDatabaseIncludeList(), offsetFile);
        return props;
    }

    /**
     * 校验连接信息与server-id等高危配置项
     *
     * @param mysql MySQL配置段
     */
    private void validate(CdcProperty.Mysql mysql) {
        AssertUtil.isTrue(StrUtil.isBlank(mysql.getHostname()) || StrUtil.isBlank(mysql.getUsername())
            || StrUtil.isBlank(mysql.getPassword())).i18n(CdcI18nConstant.MYSQL_CONNECT_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();

        Integer serverId = mysql.getServerId();
        AssertUtil.isTrue(serverId == null).i18n(CdcI18nConstant.MYSQL_SERVER_ID_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(serverId < SERVER_ID_MIN || serverId > SERVER_ID_MAX)
            .i18n(CdcI18nConstant.MYSQL_SERVER_ID_INVALID, serverId)
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
