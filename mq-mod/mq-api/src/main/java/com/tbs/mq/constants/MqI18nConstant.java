package com.tbs.mq.constants;

/**
 * MQ 模块国际化常量
 * <p>
 * 集中收拢 mq-api 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。 key 对应资源文件见
 * {@code mq-api/src/main/resources/i18n/messages-mq.properties}（中文） 与 {@code messages-mq_en_US.properties}（英文），新增 key
 * 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class MqI18nConstant {

    private MqI18nConstant() {
    }

    /* ===================== 失败消息存储 ===================== */

    /**
     * 失败消息记录为空校验提示：失败消息记录不能为空
     */
    public static final String FAILED_MESSAGE_REQUIRED = "mq.failed.message.required";

    /**
     * 失败消息唯一标识为空校验提示：失败消息记录缺少唯一标识
     */
    public static final String FAILED_MESSAGE_ID_REQUIRED = "mq.failed.message.id.required";

    /**
     * 失败消息下次重试时间为空校验提示：失败消息记录缺少下次重试时间, id:{0}
     */
    public static final String FAILED_MESSAGE_NEXT_RETRY_TIME_REQUIRED = "mq.failed.message.next.retry.time.required";

    /* ===================== 重试延迟策略 ===================== */

    /**
     * 重试延迟档位为空校验提示：重试延迟档位不能为空
     */
    public static final String RETRY_DELAY_STEPS_REQUIRED = "mq.retry.delay.steps.required";

    /* ===================== 失败消息重投递 ===================== */

    /**
     * 重投递缺少失败消息存储提示：重投递需要失败消息存储
     */
    public static final String REPUBLISHER_FAILED_STORE_REQUIRED = "mq.republisher.failed.store.required";

    /**
     * 重投递缺少重试延迟策略提示：重投递需要重试延迟策略
     */
    public static final String REPUBLISHER_DELAY_POLICY_REQUIRED = "mq.republisher.delay.policy.required";
}
