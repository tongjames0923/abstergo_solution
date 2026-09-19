package com.tbs.push.impl;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.api.transport.IUpstreamListener;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.ConnectionStateEnum;
import com.tbs.push.model.FrameTypeEnum;
import com.tbs.push.model.PushErrorCodeEnum;
import com.tbs.push.model.PushFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 推送协议帧处理器
 * <p>
 * 协议层控制帧的统一处理中枢，全部传输适配层共用本处理器，保证协议行为跨传输一致：
 * <ul>
 *     <li>HELLO — 版本协商：版本一致时回发握手确认帧（载荷含sessionId与版本）并置会话可用；
 *     不一致时回发ERROR帧后关闭连接</li>
 *     <li>PING/PONG — 心跳应答与活性刷新</li>
 *     <li>SUBSCRIBE/UNSUBSCRIBE — 订阅关系维护并回发确认帧</li>
 *     <li>DATA — 上行业务数据分发给全部上行监听器</li>
 *     <li>ERROR — 记录对端报告的错误</li>
 * </ul>
 * 帧解析失败视为协议违规：回发ERROR帧后关闭会话。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushFrameProcessor {

    private static final Logger log = LoggerFactory.getLogger(PushFrameProcessor.class);

    /**
     * 协议编解码器
     */
    private final IPushCodec codec;

    /**
     * 订阅注册表
     */
    private final ITopicRegistry topicRegistry;

    /**
     * 上行消息监听器列表
     */
    private final List<IUpstreamListener> upstreamListeners;

    /**
     * 构造协议帧处理器
     *
     * @param codec             协议编解码器，不能为空
     * @param topicRegistry     订阅注册表，不能为空
     * @param upstreamListeners 上行监听器列表，允许为null或空
     */
    public PushFrameProcessor(IPushCodec codec, ITopicRegistry topicRegistry,
        List<IUpstreamListener> upstreamListeners) {
        this.codec = codec;
        this.topicRegistry = topicRegistry;
        this.upstreamListeners = upstreamListeners;
    }

    /**
     * 处理一行传输文本
     * <p>
     * 解码失败即协议违规：回发ERROR帧后关闭会话。
     * </p>
     *
     * @param session 来源会话
     * @param text    传输文本
     */
    public void handleText(IConnectionSession session, String text) {
        PushFrame frame;
        try {
            frame = codec.decodeFrame(text);
        } catch (PushProtocolException error) {
            reject(session, PushErrorCodeEnum.FRAME_MALFORMED, error.getMessage());
            return;
        }
        handleFrame(session, frame);
    }

    /**
     * 处理协议帧
     *
     * @param session 来源会话
     * @param frame   协议帧
     */
    public void handleFrame(IConnectionSession session, PushFrame frame) {
        if (frame == null || frame.getType() == null) {
            reject(session, PushErrorCodeEnum.FRAME_MALFORMED, PushI18nConstant.FRAME_TYPE_REQUIRED);
            return;
        }
        session.touch();
        switch (frame.getType()) {
            case HELLO:
                handleHello(session, frame);
                break;
            case PING:
                reply(session, pong(frame));
                break;
            case PONG:
                session.touch();
                break;
            case SUBSCRIBE:
                handleSubscribe(session, frame, true);
                break;
            case UNSUBSCRIBE:
                handleSubscribe(session, frame, false);
                break;
            case DATA:
                handleData(session, frame);
                break;
            case ERROR:
                log.warn("对端报告协议错误, sessionId:{}, payload:{}", session.getSessionId(), frame.getPayload());
                break;
            default:
                reject(session, PushErrorCodeEnum.FRAME_TYPE_UNSUPPORTED, frame.getType().getValue());
                break;
        }
    }

    /**
     * 处理握手帧（可选版本确认）
     * <p>
     * 统一连接流程下会话已凭证认证进入可用态，本方法降级为可选的版本确认：
     * 版本一致时回发握手确认帧（重复发送无害）；不一致视为协议违规关闭连接。
     * </p>
     *
     * @param session 来源会话
     * @param frame   HELLO帧
     */
    private void handleHello(IConnectionSession session, PushFrame frame) {
        if (frame.getVersion() != PushFrameConstants.PROTOCOL_VERSION) {
            String detail = I18nUtil.get(PushI18nConstant.PROTOCOL_VERSION_MISMATCH, frame.getVersion(),
                PushFrameConstants.PROTOCOL_VERSION);
            PushFrame errorPush = errorFrame(PushErrorCodeEnum.PROTOCOL_VERSION_MISMATCH, detail);
            errorPush.setMessageId(frame.getMessageId());
            session.send(errorPush);
            session.close(PushFrameConstants.CLOSE_REASON_PROTOCOL_VIOLATION);
            return;
        }
        session.markConnected();
        sendHelloAck(session, frame.getMessageId());
    }

    /**
     * 下发统一握手确认帧
     * <p>
     * 凭证认证通过的建连入口统一调用（WebSocket/SSE同构），载荷含预分配sessionId与协议版本。
     * </p>
     *
     * @param session 目标会话
     */
    public void sendHelloAck(IConnectionSession session) {
        sendHelloAck(session, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 下发统一握手确认帧（保留原帧标识便于应答关联）
     *
     * @param session   目标会话
     * @param messageId 应答帧携带的帧标识
     */
    private void sendHelloAck(IConnectionSession session, String messageId) {
        PushFrame ack = new PushFrame(FrameTypeEnum.HELLO);
        ack.setVersion(PushFrameConstants.PROTOCOL_VERSION);
        ack.setMessageId(messageId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", session.getSessionId());
        payload.put("version", PushFrameConstants.PROTOCOL_VERSION);
        ack.setPayload(codec.serializePayload(payload));
        reply(session, ack);
    }

    /**
     * 处理订阅/退订帧
     *
     * @param session   来源会话
     * @param frame     订阅/退订帧
     * @param subscribe true表示订阅，false表示退订
     */
    private void handleSubscribe(IConnectionSession session, PushFrame frame, boolean subscribe) {
        try {
            if (subscribe) {
                topicRegistry.subscribe(session.getSessionId(), frame.getTopic().getValue());
            } else {
                topicRegistry.unsubscribe(session.getSessionId(), frame.getTopic().getValue());
            }
        } catch (PushProtocolException error) {
            reject(session, PushErrorCodeEnum.TOPIC_INVALID, error.getMessage());
            return;
        }
        PushFrame ack = new PushFrame(frame.getType());
        ack.setVersion(PushFrameConstants.PROTOCOL_VERSION);
        ack.setMessageId(frame.getMessageId());
        ack.setTopic(frame.getTopic());
        reply(session, ack);
    }

    /**
     * 处理上行业务数据帧
     *
     * @param session 来源会话
     * @param frame   DATA帧
     */
    private void handleData(IConnectionSession session, PushFrame frame) {
        if (session.getState() != ConnectionStateEnum.CONNECTED) {
            reject(session, PushErrorCodeEnum.NOT_CONNECTED, session.getSessionId());
            return;
        }
        if (CollUtil.isEmpty(upstreamListeners)) {
            log.debug("上行DATA帧无监听器处理, sessionId:{}, topic:{}", session.getSessionId(), frame.getTopic().getValue());
            return;
        }
        for (IUpstreamListener listener : upstreamListeners) {
            try {
                listener.onUpstream(session, frame);
            } catch (Throwable error) {
                log.warn("上行监听器处理异常, sessionId:{}, topic:{}", session.getSessionId(),
                    frame.getTopic().getValue(), error);
            }
        }
    }

    /**
     * 协议违规统一处理：回发ERROR帧，帧格式损坏场景同时关闭会话
     *
     * @param session 来源会话
     * @param code    协议错误码
     * @param detail  错误详情文本
     */
    private void reject(IConnectionSession session, PushErrorCodeEnum code, String detail) {
        session.send(errorFrame(code, detail));
        if (code == PushErrorCodeEnum.FRAME_MALFORMED) {
            session.close(PushFrameConstants.CLOSE_REASON_PROTOCOL_VIOLATION);
        }
    }

    /**
     * 构造ERROR帧
     *
     * @param code   协议错误码
     * @param detail 错误详情文本
     * @return ERROR帧
     */
    private PushFrame errorFrame(PushErrorCodeEnum code, String detail) {
        PushFrame frame = new PushFrame(FrameTypeEnum.ERROR);
        frame.setVersion(PushFrameConstants.PROTOCOL_VERSION);
        frame.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        frame.setPayload(detail);
        return frame;
    }

    /**
     * 构造心跳应答帧
     *
     * @param ping 收到的PING帧
     * @return PONG帧（保留原messageId便于应答关联）
     */
    private PushFrame pong(PushFrame ping) {
        PushFrame frame = new PushFrame(FrameTypeEnum.PONG);
        frame.setVersion(PushFrameConstants.PROTOCOL_VERSION);
        frame.setMessageId(ping.getMessageId());
        return frame;
    }

    /**
     * 异常安全地回发帧
     *
     * @param session 目标会话
     * @param frame   协议帧
     */
    private void reply(IConnectionSession session, PushFrame frame) {
        try {
            session.send(frame);
        } catch (Throwable error) {
            log.warn("协议应答帧发送失败, sessionId:{}", session.getSessionId(), error);
        }
    }

}
