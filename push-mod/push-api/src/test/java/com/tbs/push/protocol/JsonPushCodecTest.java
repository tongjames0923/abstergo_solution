package com.tbs.push.protocol;

import com.tbs.push.impl.protocol.JsonPushCodec;
import com.tbs.push.model.frame.FrameTypeEnum;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.message.PushMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonPushCodec 编解码单元测试
 * <p>
 * 覆盖帧类型构帧规则：缺省frameType兜底DATA（向下兼容）、SIGNAL信令帧构造、 帧JSON编解码往返一致性。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class JsonPushCodecTest {

    /**
     * 被测编解码器
     */
    private final JsonPushCodec codec = new JsonPushCodec();

    /**
     * 构造测试消息
     *
     * @param frameType 帧类型，允许为null（验证兜底）
     * @return 测试消息
     */
    private PushMessage message(FrameTypeEnum frameType) {
        PushMessage message = new PushMessage();
        message.setId("m1");
        message.setTopic("tbs.push.channel.1001");
        message.setPayload(frameType == FrameTypeEnum.SIGNAL ? null : "{\"k\":\"v\"}");
        message.setFrameType(frameType);
        return message;
    }

    /**
     * 缺省帧类型应兜底为DATA（向下兼容既有调用方）
     */
    @Test
    void encodeMessageShouldFallbackToDataWhenFrameTypeAbsent() {
        PushMessage message = message(null);
        message.setFrameType(null);
        PushFrame frame = codec.encodeMessage(message);
        assertEquals(FrameTypeEnum.DATA, frame.getType());
        assertEquals("{\"k\":\"v\"}", frame.getPayload());
    }

    /**
     * 显式DATA类型应构造DATA帧
     */
    @Test
    void encodeMessageShouldBuildDataFrameWhenDataGiven() {
        PushFrame frame = codec.encodeMessage(message(FrameTypeEnum.DATA));
        assertEquals(FrameTypeEnum.DATA, frame.getType());
    }

    /**
     * SIGNAL信令帧应保留空payload（信令不承载业务内容）
     */
    @Test
    void encodeMessageShouldBuildSignalFrameWithNullPayload() {
        PushFrame frame = codec.encodeMessage(message(FrameTypeEnum.SIGNAL));
        assertEquals(FrameTypeEnum.SIGNAL, frame.getType());
        assertEquals("tbs.push.channel.1001", frame.getTopic().getValue());
        assertNull(frame.getPayload());
    }

    /**
     * 帧JSON编解码应往返一致（SIGNAL帧含空payload）
     */
    @Test
    void encodeDecodeRoundTripShouldPreserveSignalFrame() {
        PushFrame frame = codec.encodeMessage(message(FrameTypeEnum.SIGNAL));
        PushFrame decoded = codec.decodeFrame(codec.encodeFrame(frame));
        assertEquals(FrameTypeEnum.SIGNAL, decoded.getType());
        assertEquals(frame.getMessageId(), decoded.getMessageId());
        assertTrue(decoded.getPayload() == null || decoded.getPayload().isEmpty());
    }

}
