package com.tbs.mq.v2.producer;

import com.tbs.mq.v2.vo.MqMessageOutcomeVO;

/**
 * MQ 消息发送契约（v2 记录驱动模型 · 生产段）
 * <p>
 * v2 原生、厂商无关的消息发送出口：业务组装好 {@link MqMessageOutcomeVO} 信封（含 messageId、businessId、
 * payloadFormat、payloadType、payload、topic、routeKey、headers），由具体厂商实现负责把信封序列化为
 * 传输层消息并投递。实现必须保证：
 * <ul>
 *     <li>messageId 写入厂商消息属性，作为消费端建档去重的记录主键（消费端 {@code genMessageId} 复用该值）；</li>
 *     <li>businessId / payloadFormat / payloadType 及自定义 headers 写入消息头，供消费端
 *         {@code resolveBusinessId} 与负载转换器按格式回查、还原目标类型；</li>
 *     <li>payload 按 payloadFormat 选定的 {@code IMqMessagePayloadConverter} 序列化为字节，
 *         与消费端反序列化路径严格对称。</li>
 * </ul>
 * 本接口不依赖任何 MQ 厂商类型，与消费段共同构成 v2 自建的可移植收发闭环。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
public interface IMqMessageProducer {

    /**
     * 发送消息
     * <p>
     * 按信封的 topic/routeKey 投递，payload 依据 payloadFormat 序列化，messageId/businessId/payloadType
     * 随消息头一并写出。信封为空由实现按断言拒绝。
     * </p>
     *
     * @param message 待发送的消息信封，不为空
     */
    void send(MqMessageOutcomeVO message);

}
