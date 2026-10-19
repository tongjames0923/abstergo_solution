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

    /**
     * 业务标识头：生产者发送时显式写入的业务主键（如订单号），
     * 供消费端解析为记录业务标识，作为幂等去重与人工排查的锚点；缺失时由消费端回落队列名
     */
    public static final String BUSINESS_ID = "businessId";

    /**
     * 负载格式头：标识消息体 bytes 的序列化格式（如 jdk），消费端据此选择对应的负载转换器；缺失时回落默认格式
     */
    public static final String PAYLOAD_FORMAT = "payloadFormat";

    /**
     * 负载类型头：业务负载的全限定类名，供消费端将 bytes 反序列化回目标类型（自建序列化链路的目标类来源）
     */
    public static final String PAYLOAD_TYPE = "payloadType";

}
