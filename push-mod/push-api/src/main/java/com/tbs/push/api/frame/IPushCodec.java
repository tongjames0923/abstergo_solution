package com.tbs.push.api.frame;

import com.tbs.push.model.PushFrame;
import com.tbs.push.model.PushMessage;

/**
 * 推送协议编解码器SPI
 * <p>
 * 协议层与传输层之间的序列化边界，职责固定为三件事：
 * <ul>
 *     <li>业务载荷序列化：业务对象 → 载荷文本（写入{@code PushMessage}）</li>
 *     <li>帧编码：{@link PushFrame} → 传输文本（WebSocket文本消息/SSE data）</li>
 *     <li>帧解码：传输文本 → {@link PushFrame}，失败时抛出协议异常</li>
 * </ul>
 * 默认实现为JSON编解码，业务可通过注册自定义Bean覆盖以切换序列化格式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushCodec {

    /**
     * 序列化业务载荷
     *
     * @param payload 业务载荷对象
     * @return 序列化后的载荷文本
     */
    String serializePayload(Object payload);

    /**
     * 由业务消息构造DATA帧
     * <p>
     * 帧的messageId取消息id，topic与payload原样映射，供分发器直接下发。
     * </p>
     *
     * @param message 业务消息
     * @return DATA帧
     */
    PushFrame encodeMessage(PushMessage message);

    /**
     * 编码帧为传输文本
     *
     * @param frame 协议帧
     * @return 传输文本
     */
    String encodeFrame(PushFrame frame);

    /**
     * 解码传输文本为帧
     *
     * @param text 传输文本
     * @return 协议帧
     * @throws com.tbs.push.api.exception.PushProtocolException 文本不是合法帧时抛出
     */
    PushFrame decodeFrame(String text);

}
