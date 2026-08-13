package com.tbs.mq.constants;

/**
 * MQ消息头常量
 * <p>
 * 定义框架在消息头中读写的自定义属性名称。
 * 重试次数等状态不再依赖Broker的x-death头（requeue场景下不产生该头），
 * 改由消费端与重投递调度器通过以下自定义头显式维护。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public final class MqHeaderConstants {

    private MqHeaderConstants() {
    }

    /**
     * 重试次数头：记录消息已被重投递消费的次数，由重投递调度器递增写入
     */
    public static final String RETRY_COUNT = "x-retry-count";

    /**
     * 首次失败时间头：毫秒时间戳，首次消费失败入库时写入，用于全链路时延追溯
     */
    public static final String FIRST_FAIL_TIME = "x-first-fail-time";

}
