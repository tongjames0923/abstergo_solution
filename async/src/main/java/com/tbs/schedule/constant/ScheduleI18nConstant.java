package com.tbs.schedule.constant;

/**
 * 调度框架模块国际化常量
 * <p>
 * 集中收拢 schedule 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code async/src/main/resources/i18n/messages-schedule.properties}（中文）
 * 与 {@code messages-schedule_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class ScheduleI18nConstant {

    private ScheduleI18nConstant() {
    }

    /* ===================== 任务执行器 ===================== */

    /**
     * 本地执行器类型不匹配提示：本地执行器不支持的任务类型: {0}
     */
    public static final String EXECUTOR_LOCAL_TYPE_MISMATCH = "schedule.executor.local.type.mismatch";

    /* ===================== 任务提交描述转换 ===================== */

    /**
     * 待转换的任务提交描述为空校验提示：待转换的任务提交描述不能为空
     */
    public static final String TRANSFORM_SUBMISSION_NULL = "schedule.transform.submission.null";

    /**
     * 任务载荷内存态不可持久化提示：任务载荷为内存态，不可持久化: {0}
     */
    public static final String TRANSFORM_SUBMISSION_NOT_PERSISTABLE = "schedule.transform.submission.not.persistable";

    /**
     * 任务触发时刻为空校验提示：任务触发时刻不能为空: {0}
     */
    public static final String TRANSFORM_SUBMISSION_TRIGGER_AT_NULL = "schedule.transform.submission.trigger.at.null";

    /**
     * 任务持久化快照序列化失败提示：任务持久化快照序列化失败: {0}
     */
    public static final String TRANSFORM_SERIALIZE_FAILED = "schedule.transform.serialize.failed";

    /**
     * 任务持久化快照反序列化失败提示：任务持久化快照反序列化失败
     */
    public static final String TRANSFORM_DESERIALIZE_FAILED = "schedule.transform.deserialize.failed";

    /**
     * 任务载荷类型不可解析或不是 TaskPayload 实现提示：任务载荷类型不可解析或不是 TaskPayload 实现: {0}
     */
    public static final String TRANSFORM_PAYLOAD_TYPE_INVALID = "schedule.transform.payload.type.invalid";

    /**
     * 任务载荷快照还原失败提示：任务载荷快照还原失败: {0}
     */
    public static final String TRANSFORM_PAYLOAD_PARSE_FAILED = "schedule.transform.payload.parse.failed";

    /* ===================== 任务提交 ===================== */

    /**
     * 任务提交描述为空校验提示：任务提交描述不能为空
     */
    public static final String SUBMISSION_NULL = "schedule.submission.null";

    /**
     * 任务载荷为空校验提示：任务载荷不能为空
     */
    public static final String SUBMISSION_PAYLOAD_NULL = "schedule.submission.payload.null";
}
