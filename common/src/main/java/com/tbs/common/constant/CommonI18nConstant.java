package com.tbs.common.constant;

/**
 * 通用模块国际化常量
 * <p>
 * 集中收拢 common 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code common/src/main/resources/messages-common.properties}（中文）
 * 与 {@code messages-common_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class CommonI18nConstant {

    private CommonI18nConstant() {
    }

    /* ===================== 集合工具 ===================== */

    /**
     * 集合分片大小非正数校验提示：分割大小必须为正数
     */
    public static final String COLL_SPLIT_SIZE_POSITIVE = "common.coll.split.size.positive";

    /* ===================== Bean 构建相关 ===================== */

    /**
     * Bean 构建失败提示：构建bean失败
     */
    public static final String BEAN_BUILD_FAILED = "bean.build.failed";

    /**
     * 缺少无参构造函数提示：没有无参构造函数
     */
    public static final String BEAN_NO_DEFAULT_CONSTRUCTOR = "bean.no.default.constructor";

    /* ===================== 代理执行相关 ===================== */

    /**
     * 代理执行异常提示：代理执行错误
     */
    public static final String DELEGATE_ERROR = "delegate.error";

    /**
     * 代理目标对象为空校验提示：delegateTarget不能为null
     */
    public static final String DELEGATE_TARGET_REQUIRED = "delegate.target.required";

    /**
     * 代理方法为空校验提示：method不能为null
     */
    public static final String DELEGATE_METHOD_REQUIRED = "delegate.method.required";

    /**
     * 代理提供者为空校验提示：provider不能为null
     */
    public static final String DELEGATE_PROVIDER_REQUIRED = "delegate.provider.required";

    /**
     * 代理消费者为空校验提示：consumer不能为null
     */
    public static final String DELEGATE_CONSUMER_REQUIRED = "delegate.consumer.required";

    /* ===================== SnowFlake 相关 ===================== */

    /**
     * SnowFlake 机器ID非法提示：SnowFlake 机器ID非法，范围0~{0}，当前值：{1}
     */
    public static final String SNOWFLAKE_WORKER_ID_INVALID = "snowflake.worker.id.invalid";

    /**
     * SnowFlake 数据中心ID非法提示：SnowFlake 数据中心ID非法，范围0~{0}，当前值：{1}
     */
    public static final String SNOWFLAKE_DATACENTER_ID_INVALID = "snowflake.datacenter.id.invalid";

    /**
     * SnowFlake 时钟回退提示：SnowFlake 系统时钟回退，禁止生成ID！上次时间戳：{0}，当前时间戳：{1}，差值：{2}ms
     */
    public static final String SNOWFLAKE_CLOCK_ROLLBACK = "snowflake.clock.rollback";

    /**
     * SnowFlake 时间戳溢出提示：SnowFlake 时间戳差值溢出！最大允许差值：{0}，当前差值：{1}
     */
    public static final String SNOWFLAKE_TIMESTAMP_OVERFLOW = "snowflake.timestamp.overflow";

    /**
     * SnowFlake 生成负数ID提示：SnowFlake 生成负数ID，时间戳溢出！tsDiff={0}, id={1}
     */
    public static final String SNOWFLAKE_NEGATIVE_ID = "snowflake.negative.id";
}
