package com.tbs.cdc.i18n;

/**
 * CDC模块国际化资源key常量
 * <p>
 * 集中声明断言异常文案的i18n key，与{@code messages-cdc.properties}资源文件一一对应。
 * 所有面向日志与异常的文案经此引用，禁止硬编码魔法值。参考MqI18nConstant模式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class CdcI18nConstant {

    private CdcI18nConstant() {
    }

    /* ===================== 捕获源与生命周期 ===================== */

    /**
     * 未发现CDC捕获源实现：未找到CdcSource实现Bean，请确认已引入对应数据库实现子模块
     */
    public static final String SOURCE_NOT_FOUND = "cdc.source.not.found";

    /**
     * 捕获源实现唯一性：发现多个CdcSource实现Bean, 数量:{0}，有且仅允许一个
     */
    public static final String SOURCE_MULTIPLE = "cdc.source.multiple";

    /**
     * 捕获引擎启动失败：CDC捕获引擎启动失败
     */
    public static final String ENGINE_START_FAILED = "cdc.engine.start.failed";

    /**
     * 捕获引擎停止失败：CDC捕获引擎停止失败
     */
    public static final String ENGINE_STOP_FAILED = "cdc.engine.stop.failed";

    /**
     * 运行状态异常：CDC运行时启动异常
     */
    public static final String RUNTIME_START_FAILED = "cdc.runtime.start.failed";

    /* ===================== MySQL / Debezium 配置 ===================== */

    /**
     * server-id缺失：MySQL CDC配置server-id不能为空
     */
    public static final String MYSQL_SERVER_ID_REQUIRED = "cdc.mysql.server.id.required";

    /**
     * server-id非法：MySQL CDC配置server-id超出合法范围(2-4294967294), 当前:{0}
     */
    public static final String MYSQL_SERVER_ID_INVALID = "cdc.mysql.server.id.invalid";

    /**
     * 连接信息缺失：MySQL CDC配置hostname/username/password不能为空
     */
    public static final String MYSQL_CONNECT_REQUIRED = "cdc.mysql.connect.required";

    /**
     * 连接信息缺失：SQL Server CDC配置hostname/username/password不能为空
     */
    public static final String SQLSERVER_CONNECT_REQUIRED = "cdc.sqlserver.connect.required";

    /**
     * 库名列表缺失：SQL Server CDC配置database.names（对应databaseIncludeList）不能为空
     */
    public static final String SQLSERVER_DATABASE_NAMES_REQUIRED = "cdc.sqlserver.database.names.required";

    /**
     * 位点目录创建失败：CDC位点存储目录创建失败, path:{0}
     */
    public static final String OFFSET_DIR_FAILED = "cdc.offset.dir.failed";

    /**
     * 事件转换失败：CDC变更事件转换失败, 原始载荷:{0}
     */
    public static final String CONVERT_FAILED = "cdc.convert.failed";

    /* ===================== 事件与监听 ===================== */

    /**
     * 事件为空：CDC变更事件不能为空
     */
    public static final String EVENT_REQUIRED = "cdc.event.required";

    /**
     * 订阅描述为空：CDC监听器订阅描述不能为null
     */
    public static final String SUBSCRIPTION_REQUIRED = "cdc.subscription.required";

    /**
     * 监听方法签名非法：@CdcListener方法须有且仅有一个CdcEvent入参, method:{0}
     */
    public static final String LISTENER_METHOD_INVALID = "cdc.listener.method.invalid";

    /**
     * 监听器执行失败：CDC监听器执行失败, eventId:{0}, listener:{1}
     */
    public static final String LISTENER_INVOKE_FAILED = "cdc.listener.invoke.failed";

    /**
     * 行级过滤表达式执行失败：CDC行级过滤表达式执行失败, table:{0}, expression:{1}
     */
    public static final String ROW_CONDITION_FAILED = "cdc.row.condition.failed";

    /**
     * 队列满丢弃最旧事件：CDC分发队列已满, 按drop-oldest策略丢弃最旧事件, eventId:{0}
     */
    public static final String QUEUE_FULL_DROP_OLDEST = "cdc.queue.full.drop.oldest";
}
