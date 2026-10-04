package com.tbs.push.websocket.handler;

import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.websocket.adapter.WebSocketTransportAdapter;
import com.tbs.push.websocket.session.WebSocketSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 推送WebSocket帧处理器
 * <p>
 * WebSocket容器事件的薄壳转接层：本类只做容器语境的物理动作（URL凭证提取、
 * 容器会话属性位反查、物理通道关闭），全部链路语义转接
 * {@link ITransportAdapter}全链路契约——建立走establish、上行默认走
 * acceptUpstreamText契约口（与SSE同一路径）、被动关闭走onChannelClosed
 * （与主动关闭共享幂等门禁）。不持有注册表/拦截器/帧处理器等编排依赖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PushWebSocketHandler.class);

    /**
     * 传输适配器延迟获取（transport条件未命中或业务覆盖装配时缺失，容器动作静默拒绝）
     */
    private final ObjectProvider<ITransportAdapter> transportProvider;

    /**
     * 构造推送WebSocket帧处理器
     *
     * @param transportProvider 传输适配器提供者
     */
    public PushWebSocketHandler(ObjectProvider<ITransportAdapter> transportProvider) {
        this.transportProvider = transportProvider;
    }

    /**
     * 连接建立回调：提取握手凭证 → 适配器统一建立编排；凭证校验失败按策略关闭
     *
     * @param session 底层WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        ITransportAdapter adapter = transportProvider.getIfAvailable();
        if (adapter == null) {
            log.error("WebSocket传输适配器未装配, 拒绝连接, uri:{}", session.getUri());
            closeQuietly(session);
            return;
        }
        try {
            adapter.establish(extractTicket(session), session);
        } catch (PushProtocolException error) {
            log.warn("WebSocket连接凭证校验失败, uri:{}", session.getUri(), error);
            closeQuietly(session);
        }
    }

    /**
     * 文本消息回调：反查统一会话后转接上行契约口
     *
     * @param session 底层WebSocket会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        WebSocketSessionWrapper wrapper = resolveWrapper(session);
        if (wrapper == null) {
            log.warn("收到未注册会话的文本消息, 忽略, uri:{}", session.getUri());
            return;
        }
        ITransportAdapter adapter = transportProvider.getIfAvailable();
        if (adapter != null) {
            adapter.acceptUpstreamText(wrapper.getSessionId(), message.getPayload());
        }
    }

    /**
     * 传输异常回调：记录并关闭该会话（不影响其他连接，清理经关闭回调统一触发）
     *
     * @param session 底层WebSocket会话
     * @param error   传输异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) {
        log.warn("WebSocket传输异常, uri:{}", session.getUri(), error);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException closeError) {
            log.warn("异常会话关闭失败, uri:{}", session.getUri(), closeError);
        }
    }

    /**
     * 连接关闭回调：被动关闭事件回传契约入口（幂等门禁去重主动关闭已完成的清理）
     *
     * @param session 底层WebSocket会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketSessionWrapper wrapper = resolveWrapper(session);
        if (wrapper == null) {
            return;
        }
        ITransportAdapter adapter = transportProvider.getIfAvailable();
        if (adapter != null) {
            adapter.onChannelClosed(wrapper.getSessionId(), String.valueOf(status.getCode()));
        }
    }

    /**
     * 从握手URL提取连接凭证
     *
     * @param session 底层WebSocket会话
     * @return 凭证文本，缺失时抛协议异常
     * @throws PushProtocolException 凭证缺失时抛出
     */
    private String extractTicket(WebSocketSession session) {
        String query = session.getUri() == null ? null : session.getUri().getRawQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && PushFrameConstants.TICKET_PARAM.equals(kv[0])) {
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        }
        throw PushProtocolException.protocolI18n(PushI18nConstant.TICKET_REQUIRED);
    }

    /**
     * 从容器会话属性位反查统一会话包装（建立时由适配器登记）
     *
     * @param session 底层WebSocket会话
     * @return 会话包装，不存在时返回null
     */
    private WebSocketSessionWrapper resolveWrapper(WebSocketSession session) {
        Object wrapper = session.getAttributes().get(WebSocketTransportAdapter.SESSION_ATTR_KEY);
        return wrapper instanceof WebSocketSessionWrapper ? (WebSocketSessionWrapper) wrapper : null;
    }

    /**
     * 异常安全地关闭底层会话
     *
     * @param session 底层WebSocket会话
     */
    private void closeQuietly(WebSocketSession session) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException error) {
            log.warn("拒绝会话关闭失败, uri:{}", session.getUri(), error);
        }
    }

}
