package com.tbs.cdc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CDC模块配置属性
 * <p>
 * 统一收拢{@code tbs.cdc.*}全部配置，禁止散落@Value注入。由{@code CdcConfig}经
 * {@code @EnableConfigurationProperties}激活。{@code enable}为模块总开关，
 * 关闭时运行期不装配任何组件、不启动任何线程。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.cdc")
public class CdcProperty {

    /**
     * 模块总开关，false时零Bean装配、零线程占用
     */
    private boolean enable = false;

    /**
     * 捕获源类型标识：mysql/postgresql/mongodb
     */
    private String sourceType = "mysql";

    /**
     * 事件载荷裁剪模式：full/changed-only/key-only
     */
    private String payloadMode = PayloadMode.CHANGED_ONLY.value;

    /**
     * MySQL捕获源连接与位点配置
     */
    private Mysql mysql = new Mysql();

    /**
     * SQL Server捕获源连接与位点配置
     */
    private Sqlserver sqlserver = new Sqlserver();

    /**
     * 过滤控制配置
     */
    private Filter filter = new Filter();

    /**
     * 消息分发配置
     */
    private Dispatch dispatch = new Dispatch();

    /**
     * 批量与频率控制配置
     */
    private Batch batch = new Batch();

    /**
     * 错误处理配置
     */
    private Error error = new Error();

    /**
     * MySQL捕获源配置
     * <p>
     * 透传给Debezium MySQL连接器：连接信息、复制标识、库表白名单与位点存储。
     * </p>
     */
    @Data
    public static class Mysql {

        /**
         * MySQL主机地址
         */
        private String hostname = "127.0.0.1";

        /**
         * MySQL端口
         */
        private int port = 3306;

        /**
         * CDC专用账号（仅需REPLICATION SLAVE/CLIENT权限），走环境变量注入
         */
        private String username;

        /**
         * CDC专用账号密码，走环境变量注入
         */
        private String password;

        /**
         * Debezium逻辑服务名，用于topic前缀与源标识，须全局唯一
         */
        private String serverName = "cdc_mysql";

        /**
         * MySQL复制节点server-id，须唯一且不与集群其他复制节点冲突（高危配置项）
         */
        private Integer serverId;

        /**
         * 捕获的数据库列表，逗号分隔（透传database.include.list）
         */
        private String databaseIncludeList;

        /**
         * 捕获的表列表，逗号分隔，格式db.table（透传table.include.list）
         */
        private String tableIncludeList;

        /**
         * 消费位点持久化文件路径，删除该文件将触发全量快照重新消费
         */
        private String offsetStorageFile = "data/cdc-offsets.dat";

        /**
         * 快照模式：initial/never/schema_only等，默认never仅增量
         */
        private String snapshotMode = "never";

        /**
         * 心跳间隔（毫秒），无变更事务时产生心跳事件以推进位点
         */
        private long heartbeatIntervalMs = 5000L;
    }

    /**
     * SQL Server捕获源配置
     * <p>
     * 透传给Debezium SQL Server连接器：连接信息、库表白名单与位点/结构历史存储。
     * 前提：目标库表已启用SQL Server CDC（{@code sp_cdc_enable_db}/{@code sp_cdc_enable_table}）
     * 且SQL Server Agent运行中，连接器据此读取变更表（change tables）。
     * </p>
     */
    @Data
    public static class Sqlserver {

        /**
         * SQL Server主机地址
         */
        private String hostname = "127.0.0.1";

        /**
         * SQL Server端口
         */
        private int port = 1433;

        /**
         * CDC专用账号（需对目标库的读取与CDC元数据访问权限），走环境变量注入
         */
        private String username;

        /**
         * CDC专用账号密码，走环境变量注入
         */
        private String password;

        /**
         * Debezium逻辑服务名，用于topic前缀与源标识，须全局唯一
         */
        private String serverName = "cdc_sqlserver";

        /**
         * 是否加密连接（透传database.encrypt），新版mssql-jdbc驱动默认true，本地自签环境常置false
         */
        private boolean encrypt = false;

        /**
         * 是否信任服务端证书（透传database.trustServerCertificate），encrypt为false时通常置true
         */
        private boolean trustServerCertificate = true;

        /**
         * 捕获的数据库列表，逗号分隔（透传database.names，SQL Server必填，连接器无database.include.list概念）
         */
        private String databaseIncludeList;

        /**
         * 捕获的表列表，逗号分隔（透传table.include.list）；
         * Debezium 3.0.8实测匹配格式为{@code schema.table}（如dbo.PURCHASE_ORDER），不带数据库名前缀，仅表名不命中
         */
        private String tableIncludeList;

        /**
         * 消费位点持久化文件路径，删除该文件可能触发快照重新消费
         */
        private String offsetStorageFile = "data/cdc-offsets-sqlserver.dat";

        /**
         * 快照模式：initial/no_data/schema_only/recovered等，SQL Server不支持never，默认no_data仅结构不快照数据
         */
        private String snapshotMode = "no_data";

        /**
         * 心跳间隔（毫秒），无变更事务时产生心跳事件以推进位点
         */
        private long heartbeatIntervalMs = 5000L;

        /**
         * SQL Server Agent状态探测查询，留空使用模块内置中英服务名兼容默认值；
         * 透传时由工厂自动添加database.前缀（连接器JDBC配置仅保留database.*子集）；
         * {@code [#db]}占位符由连接器替换为当前库名，查询必须恰好返回一行，否则连接器停止；
         * Debezium默认查询仅匹配英文服务名，中文本地化实例（服务名"SQL Server 代理 (...)"）会探测失败
         */
        private String agentStatusQuery = "";
    }

    /**
     * 过滤控制配置
     */
    @Data
    public static class Filter {

        /**
         * 库白名单，为空表示不限制；命中include后再看exclude
         */
        private List<String> includeDatabases = new ArrayList<>();

        /**
         * 库排除列表
         */
        private List<String> excludeDatabases = new ArrayList<>();

        /**
         * 表级正则白名单，为空表示不限制
         */
        private List<String> includeTablePatterns = new ArrayList<>();

        /**
         * 表级正则排除列表
         */
        private List<String> excludeTablePatterns = new ArrayList<>();

        /**
         * 行级过滤条件，key为{@code db.table}，value为SpEL表达式；
         * 表达式以{@code #beforeData}/{@code #afterData}/{@code #changeType}为变量，返回false则丢弃事件
         */
        private Map<String, String> rowConditions = new LinkedHashMap<>();
    }

    /**
     * 消息分发配置
     */
    @Data
    public static class Dispatch {

        /**
         * 分发模式：in-process/mq/both
         */
        private String mode = DispatchMode.IN_PROCESS.value;

        /**
         * 进程内分发线程模式：sync/async
         */
        private String inProcessMode = InProcessMode.ASYNC.value;

        /**
         * MQ桥接配置，mode为mq/both时生效
         */
        private Mq mq = new Mq();
    }

    /**
     * MQ桥接配置
     */
    @Data
    public static class Mq {

        /**
         * 目标交换机名称
         */
        private String exchange = "tbs.cdc";

        /**
         * 路由键模板，支持占位符{database}/{table}/{changeType}
         */
        private String routingPattern = "cdc.{database}.{table}.{changeType}";
    }

    /**
     * 批量与频率控制配置
     */
    @Data
    public static class Batch {

        /**
         * 异步分发有界队列容量
         */
        private int queueCapacity = 10000;

        /**
         * 单批刷新最大事件数
         */
        private int batchSize = 200;

        /**
         * 队列刷新最大间隔（毫秒）
         */
        private long flushIntervalMs = 500L;

        /**
         * 队列满背压策略：block/drop-oldest
         */
        private String onFull = OnFullPolicy.BLOCK.value;
    }

    /**
     * 错误处理配置
     */
    @Data
    public static class Error {

        /**
         * 监听器执行失败重试次数
         */
        private int retryTimes = 3;

        /**
         * 重试间隔（毫秒）
         */
        private long retryDelayMs = 1000L;

        /**
         * 最终失败策略：skip/dead-letter
         */
        private String onFail = OnFailPolicy.SKIP.value;
    }

    /**
     * 事件载荷裁剪模式
     */
    public enum PayloadMode {

        /**
         * 携带变更前后完整行数据
         */
        FULL("full"),

        /**
         * UPDATE仅携带差异列，降低内存与序列化开销
         */
        CHANGED_ONLY("changed-only"),

        /**
         * 仅携带主键，数据最精简
         */
        KEY_ONLY("key-only");

        /**
         * 配置取值
         */
        private final String value;

        PayloadMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 分发模式
     */
    public enum DispatchMode {

        /**
         * 仅进程内分发
         */
        IN_PROCESS("in-process"),

        /**
         * 仅桥接至MQ
         */
        MQ("mq"),

        /**
         * 进程内与MQ同时分发
         */
        BOTH("both");

        /**
         * 配置取值
         */
        private final String value;

        DispatchMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 进程内分发线程模式
     */
    public enum InProcessMode {

        /**
         * 同步直发，在捕获线程内执行监听
         */
        SYNC("sync"),

        /**
         * 异步批量，经有界队列由独立线程池刷新分发
         */
        ASYNC("async");

        /**
         * 配置取值
         */
        private final String value;

        InProcessMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 队列满背压策略
     */
    public enum OnFullPolicy {

        /**
         * 阻塞捕获线程直至有空位（保证不丢）
         */
        BLOCK("block"),

        /**
         * 丢弃最旧事件并告警（保证不阻塞，可能丢）
         */
        DROP_OLDEST("drop-oldest");

        /**
         * 配置取值
         */
        private final String value;

        OnFullPolicy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 监听最终失败策略
     */
    public enum OnFailPolicy {

        /**
         * 跳过并记录日志
         */
        SKIP("skip"),

        /**
         * 记录死信快照（含事件完整上下文），供后续人工或MQ失败存储补偿
         */
        DEAD_LETTER("dead-letter");

        /**
         * 配置取值
         */
        private final String value;

        OnFailPolicy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
