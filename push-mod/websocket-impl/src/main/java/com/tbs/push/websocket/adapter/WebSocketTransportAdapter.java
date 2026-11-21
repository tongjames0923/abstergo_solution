package com.tbs.push.websocket.adapter;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.impl.transport.PushConnectionOrchestrator;
import com.tbs.push.model.auth.PushCredential;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import com.tbs.push.websocket.session.WebSocketSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket传输适配器
 * <p>
 * WebSocket传输对{@link ITransportAdapter}全链路契约的实现：容器握手后由
 * {@code PushWebSocketHandler}把容器会话交本适配器建立统一会话，容器文本事件
 * 默认转接{@link #acceptUpstreamText}契约口（与SSE上行同一路径）。
 * 建立/清理编排全部委托协议层{@link PushConnectionOrchestrator}，本类只负责
 * 容器会话包装（并登记进容器属性位供事件回调反查）与容器语境的静默容错
 * （上行会话解析失败仅记日志，不开HTTP错误通道）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class WebSocketTransportAdapter implements ITransportAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTransportAdapter.class);

    /**
     * 容器会话属性键：统一会话包装的反查锚点（免去簿记集合与O(n)遍历）
     */
    public static final String SESSION_ATTR_KEY = "tbs.push.session";

    /**
     * 协议编解码器（会话包装发送帧所需）
     */
    private final IPushCodec codec;

    /**
     * 连接生命周期编排器
     */
    private final PushConnectionOrchestrator orchestrator;

    /**
     * WebSocket端点路径（日志留痕）
     */
    private final String endpointPath;

    /**
     * 构造WebSocket传输适配器
     *
     * @param codec        协议编解码器
     * @param orchestrator 连接生命周期编排器
     * @param endpointPath WebSocket端点路径
     */
    public WebSocketTransportAdapter(IPushCodec codec, PushConnectionOrchestrator orchestrator, String endpointPath) {
        this.codec = codec;
        this.orchestrator = orchestrator;
        this.endpointPath = endpointPath;
    }

    /**
     * 建立统一会话：消费凭证→包装容器会话→登记属性位→统一建立编排
     *
     * @param ticket          一次性连接凭证（握手URL push-ticket）
     * @param physicalChannel 容器WebSocket会话
     * @return 建立完成的统一会话
     */
    @Override
    public IConnectionSession establish(String ticket, Object physicalChannel) {
        if (!(physicalChannel instanceof WebSocketSession)) {
            AssertUtil.throwError(() -> new IllegalStateException(I18nUtil.get(
                "push.websocket.transport.channel.required",
                physicalChannel == null ? "null" : physicalChannel.getClass().getName())));
        }
        WebSocketSession webSocketSession = (WebSocketSession) physicalChannel;
        PushCredential credential = orchestrator.consumeTicket(ticket);
        WebSocketSessionWrapper wrapper = new WebSocketSessionWrapper(credential.getSessionId(), webSocketSession,
            codec, credential.getSessionAttributes());
        webSocketSession.getAttributes().put(SESSION_ATTR_KEY, wrapper);
        orchestrator.establish(wrapper);
        return wrapper;
    }

    /**
     * 上行受理：结构化协议帧（容器场景保留契约能力，实际文本事件为主）
     *
     * @param sessionId 会话唯一标识
     * @param frame     结构化协议帧
     */
    @Override
    public void acceptUpstream(String sessionId, PushFrame frame) {
        IConnectionSession session = resolveUpstreamTarget(sessionId);
        if (session == null || frame == null) {
            return;
        }
        orchestrator.runUpstream(session, frame);
    }

    /**
     * 上行受理：传输文本（容器文本事件默认转接口）
     *
     * @param sessionId 会话唯一标识
     * @param text      上行帧JSON文本
     */
    @Override
    public void acceptUpstreamText(String sessionId, String text) {
        IConnectionSession session = resolveUpstreamTarget(sessionId);
        if (session == null || text == null) {
            return;
        }
        orchestrator.runUpstreamText(session, text);
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
     * 物理通道关闭事件回传（容器关闭回调入口，与主动关闭共享幂等门禁）
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
        log.info("WebSocket传输适配器已停止, 端点:{}, 关闭会话数:{}", endpointPath, closed);
    }

    /**
     * 容器语境的会话解析：不可解析或已关闭记warn静默丢弃（无HTTP错误通道）
     *
     * @param sessionId 会话唯一标识
     * @return 可受理上行的会话，不可解析时null
     */
    private IConnectionSession resolveUpstreamTarget(String sessionId) {
        IConnectionSession session = orchestrator.resolveSession(sessionId);
        if (session == null) {
            log.warn("WebSocket上行会话不可解析, 丢弃, sessionId:{}", sessionId);
            return null;
        }
        if (session.getState() == ConnectionStateEnum.CLOSED) {
            log.warn("WebSocket上行会话已关闭, 丢弃, sessionId:{}", sessionId);
            return null;
        }
        return session;
    }

}
