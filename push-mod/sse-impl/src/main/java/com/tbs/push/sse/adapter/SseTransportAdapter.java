package com.tbs.push.sse.adapter;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.impl.transport.PushConnectionOrchestrator;
import com.tbs.push.model.auth.PushCredential;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import com.tbs.push.sse.session.SseSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE组合通道传输适配器
 * <p>
 * SSE传输对{@link ITransportAdapter}全链路契约的实现：SseEmitter下行流 +
 * HTTP POST帧上行的组合双向通道。建立/上行/关闭的流程编排全部委托协议层
 * {@link PushConnectionOrchestrator}（跨传输行为一致），本类只负责：
 * <ul>
 *     <li>物理通道构造与包装（emitter→统一会话，挂载关闭回调）</li>
 *     <li>HTTP端点语境的快速失败断言（会话缺失/已关闭转统一错误响应）</li>
 * </ul>
 * 清理链幂等门禁由编排器以注册表注销原子性保证，emitter回调与主动关闭
 * 双入口竞态时仅一方执行清理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SseTransportAdapter implements ITransportAdapter {

    private static final Logger log = LoggerFactory.getLogger(SseTransportAdapter.class);

    /**
     * 协议编解码器（会话包装发送帧所需）
     */
    private final IPushCodec codec;

    /**
     * 连接生命周期编排器
     */
    private final PushConnectionOrchestrator orchestrator;

    /**
     * 构造SSE传输适配器
     *
     * @param codec        协议编解码器
     * @param orchestrator 连接生命周期编排器
     */
    public SseTransportAdapter(IPushCodec codec, PushConnectionOrchestrator orchestrator) {
        this.codec = codec;
        this.orchestrator = orchestrator;
    }

    /**
     * 建立SSE下行通道（控制器便捷入口）
     *
     * @param ticket 一次性连接凭证（query param push-ticket）
     * @return 下行流emitter（已随建立编排完成会话登记）
     */
    public SseEmitter connect(String ticket) {
        SseSessionWrapper wrapper = (SseSessionWrapper)establish(ticket, null);
        return wrapper.getEmitter();
    }

    /**
     * 建立统一会话：消费凭证→包装emitter→挂载关闭回调→统一建立编排
     *
     * @param ticket          一次性连接凭证
     * @param physicalChannel 物理通道（SseEmitter，null时本实现自建无限期emitter）
     * @return 建立完成的统一会话
     */
    @Override
    public IConnectionSession establish(String ticket, Object physicalChannel) {
        PushCredential credential = orchestrator.consumeTicket(ticket);
        SseEmitter emitter = physicalChannel instanceof SseEmitter ? (SseEmitter)physicalChannel : new SseEmitter(0L);
        SseSessionWrapper wrapper =
            new SseSessionWrapper(credential.getSessionId(), emitter, codec, credential.getAttributes());
        emitter.onCompletion(
            () -> onChannelClosed(wrapper.getSessionId(), PushFrameConstants.CLOSE_REASON_CHANNEL_CLOSED));
        emitter.onTimeout(
            () -> onChannelClosed(wrapper.getSessionId(), PushFrameConstants.CLOSE_REASON_CHANNEL_CLOSED));
        emitter.onError(
            error -> onChannelClosed(wrapper.getSessionId(), PushFrameConstants.CLOSE_REASON_CHANNEL_CLOSED));
        orchestrator.establish(wrapper);
        return wrapper;
    }

    /**
     * 上行受理：结构化协议帧（HTTP POST端点语境，解析失败抛业务异常转统一错误响应）
     *
     * @param sessionId 会话唯一标识
     * @param frame     结构化协议帧
     */
    @Override
    public void acceptUpstream(String sessionId, PushFrame frame) {
        SseSessionWrapper wrapper = resolveActiveSession(sessionId);
        AssertUtil.isTrue(frame == null).i18n(PushI18nConstant.FRAME_REQUIRED).throwIt();
        orchestrator.runUpstream(wrapper, frame);
    }

    /**
     * 上行受理：传输文本（历史文本协议兼容入口）
     *
     * @param sessionId 会话唯一标识
     * @param text      上行帧JSON文本
     */
    @Override
    public void acceptUpstreamText(String sessionId, String text) {
        SseSessionWrapper wrapper = resolveActiveSession(sessionId);
        AssertUtil.isTrue(StrUtil.isEmpty(text)).i18n(PushI18nConstant.FRAME_REQUIRED).throwIt();
        orchestrator.runUpstreamText(wrapper, text);
    }

    /**
     * 服务端主动关闭会话（委托编排器，物理拆除+清理链一次闭环）
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识
     * @return true表示本次调用完成关闭与清理
     */
    @Override
    public boolean closeSession(String sessionId, String reason) {
        return orchestrator.closeSession(sessionId, reason);
    }

    /**
     * 物理通道关闭事件回传（emitter回调入口，与主动关闭共享幂等门禁）
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识
     * @return true表示本次调用执行了清理链
     */
    @Override
    public boolean onChannelClosed(String sessionId, String reason) {
        return orchestrator.onChannelClosed(sessionId, reason);
    }

    /**
     * 优雅关停：注册表全扫描逐个主动关闭（幂等，重复停止无副作用）
     */
    @Override
    public void stop() {
        int closed = orchestrator.closeAll(PushFrameConstants.CLOSE_REASON_SERVER_SHUTDOWN);
        log.info("SSE传输适配器已停止, 关闭会话数:{}", closed);
    }

    /**
     * HTTP上行语境的会话快速失败断言：参数缺失与会话不可解析/已关闭均抛业务异常
     *
     * @param sessionId 会话唯一标识
     * @return 可受理上行的SSE会话包装
     */
    private SseSessionWrapper resolveActiveSession(String sessionId) {
        AssertUtil.isTrue(StrUtil.isEmpty(sessionId)).i18n(PushI18nConstant.SESSION_ID_REQUIRED).throwIt();
        IConnectionSession session = orchestrator.resolveSession(sessionId);
        AssertUtil.isTrue(session == null).i18n(PushI18nConstant.SESSION_NOT_FOUND).params(sessionId).throwIt();
        AssertUtil.isTrue(session.getState() == ConnectionStateEnum.CLOSED).i18n(PushI18nConstant.SESSION_NOT_CONNECTED)
            .params(sessionId).throwIt();
        AssertUtil.isTrue(!(session instanceof SseSessionWrapper)).i18n(PushI18nConstant.SESSION_NOT_FOUND)
            .params(sessionId).throwIt();
        return (SseSessionWrapper)session;
    }

}
