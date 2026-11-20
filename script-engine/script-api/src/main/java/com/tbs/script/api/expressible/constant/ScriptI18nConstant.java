package com.tbs.script.api.expressible.constant;

/**
 * 表达式与事件编排模块国际化常量。
 *
 * <p>集中收拢 script 模块面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code src/main/resources/i18n/messages-script.properties}（中文）
 * 与 {@code messages-script_en_US.properties}（英文），新增 key 须中英文两份同步维护。</p>
 */
public final class ScriptI18nConstant {

    private ScriptI18nConstant() {
    }

    /* ===================== 事件编排 ===================== */

    /**
     * 事件信封为空校验提示：事件上报信封不能为空
     */
    public static final String ORCHESTRATION_ENVELOPE_NULL = "script.orchestration.envelope.null";

    /**
     * 上报器ID为空校验提示：事件上报器ID不能为空
     */
    public static final String ORCHESTRATION_REPORTER_ID_REQUIRED = "script.orchestration.reporter.id.required";

    /**
     * 事件编码为空校验提示：事件编码不能为空
     */
    public static final String ORCHESTRATION_EVENT_CODE_REQUIRED = "script.orchestration.event.code.required";

    /**
     * 上报器不存在提示：未找到事件上报器: {0}
     */
    public static final String ORCHESTRATION_REPORTER_NOT_FOUND = "script.orchestration.reporter.not.found";

    /**
     * 事件未声明提示：事件上报器 [{0}] 未声明事件: {1}
     */
    public static final String ORCHESTRATION_EVENT_NOT_DECLARED = "script.orchestration.event.not.declared";

    /**
     * 参数数量超限提示：事件 [{0}] 期望最多 {1} 个参数，实际传入 {2} 个
     */
    public static final String ORCHESTRATION_PARAM_OVERFLOW = "script.orchestration.param.overflow";

    /**
     * 触发器不存在提示：未找到触发器: {0}
     */
    public static final String ORCHESTRATION_TRIGGER_NOT_FOUND = "script.orchestration.trigger.not.found";

    /**
     * 订阅关系为空校验提示：订阅关系不能为空
     */
    public static final String ORCHESTRATION_SUBSCRIPTION_NULL = "script.orchestration.subscription.null";

    /**
     * 订阅触发器ID为空校验提示：订阅关系中的触发器ID不能为空
     */
    public static final String ORCHESTRATION_SUBSCRIPTION_TRIGGER_REQUIRED =
        "script.orchestration.subscription.trigger.required";

    /* ===================== 条件触发器 ===================== */

    /**
     * 条件规则触发器名称：条件规则触发器
     */
    public static final String TRIGGER_CONDITION_NAME = "script.trigger.condition.name";

    /**
     * 条件规则触发器描述：按订阅关系绑定的条件规则判断是否触发
     */
    public static final String TRIGGER_CONDITION_DESCRIPTION = "script.trigger.condition.description";

    /* ===================== 触发器动作 ===================== */

    /**
     * 动作不存在提示：未找到触发器动作: {0}
     */
    public static final String TRIGGER_ACTION_NOT_FOUND = "script.trigger.action.not.found";
}
