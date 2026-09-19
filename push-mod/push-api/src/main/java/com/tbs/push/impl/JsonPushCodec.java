package com.tbs.push.impl;

import com.alibaba.fastjson2.JSONException;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.FrameTypeEnum;
import com.tbs.push.model.PushFrame;
import com.tbs.push.model.PushMessage;
import com.tbs.push.model.PushTopic;

/**
 * JSON协议编解码器（默认实现）
 * <p>
 * 基于fastjson2实现帧与载荷的JSON序列化（与项目工具栈一致，不新增依赖）：
 * 帧编码为单行JSON文本，适配WebSocket文本消息与SSE data载荷两种传输载体。
 * 业务可通过注册自定义IPushCodec Bean覆盖本实现以切换序列化格式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class JsonPushCodec implements IPushCodec {

    /**
     * 序列化业务载荷
     *
     * @param payload 业务载荷对象
     * @return JSON文本，载荷为null时返回null
     */
    @Override
    public String serializePayload(Object payload) {
        return payload == null ? null : JsonUtils.toJSONString(payload);
    }

    /**
     * 由业务消息构造DATA帧
     *
     * @param message 业务消息
     * @return DATA帧
     */
    @Override
    public PushFrame encodeMessage(PushMessage message) {
        PushFrame frame = new PushFrame(FrameTypeEnum.DATA);
        frame.setVersion(com.tbs.push.constants.PushFrameConstants.PROTOCOL_VERSION);
        frame.setMessageId(message.getId());
        frame.setTopic(PushTopic.of(message.getTopic()));
        frame.setPayload(message.getPayload());
        return frame;
    }

    /**
     * 编码帧为JSON文本
     *
     * @param frame 协议帧
     * @return JSON文本
     */
    @Override
    public String encodeFrame(PushFrame frame) {
        return JsonUtils.toJSONString(frame);
    }

    /**
     * 解码JSON文本为帧
     *
     * @param text JSON文本
     * @return 协议帧
     * @throws PushProtocolException 文本不合法或帧类型缺失时抛出
     */
    @Override
    public PushFrame decodeFrame(String text) {
        PushFrame frame;
        try {
            frame = JsonUtils.parseObject(text, PushFrame.class);
        } catch (JSONException error) {
            throw PushProtocolException.protocolI18n(PushI18nConstant.FRAME_DECODE_FAILED, text);
        }
        if (frame == null || frame.getType() == null) {
            throw PushProtocolException.protocolI18n(PushI18nConstant.FRAME_TYPE_REQUIRED);
        }
        return frame;
    }

}
