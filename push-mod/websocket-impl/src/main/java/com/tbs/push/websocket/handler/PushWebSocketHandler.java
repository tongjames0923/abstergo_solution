package com.tbs.push.websocket.handler;

import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.impl.PushCredentialService;
import com.tbs.push.impl.PushFrameProcessor;
import com.tbs.push.model.PushCredential;
import com.tbs.push.websocket.session.WebSocketSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推送WebSocket帧处理器
 * <p>
 * WebSocket连接生命周期的适配入口，与SSE共用协议层的统一连接建立流程：
 * <ul>
 *     <li>连接建立：从握手URL提取 push-ticket → 原子消费凭证（一次性防重放）→
 *     以预分配sessionId包装会话、注入认证属性、登记注册表 → 下发统一ACK帧进入可用态；
 *     凭证校验失败按策略关闭（1008）</li>
 *     <li>文本消息：刷新活性后委托协议层{@link PushFrameProcessor}处理（心跳/订阅/数据/可选版本确认）</li>
 *     <li>连接关闭：注销会话、触发断开拦截器（订阅清理由协议层注册表完成）</li>
 * </ul>
 * 协议校验逻辑全部在协议层完成，本类不感知帧语义。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PushWebSocketHandler.class);

    /**
     * 协议编解码器
     */
    private final IPushCodec codec;

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 连接拦截器链
     */
    private final IConnectionInterceptor interceptorChain;

    /**
     * 协议帧处理器
     */
    private final PushFrameProcessor frameProcessor;

    /**
     * 连接凭证服务
     */
    private final PushCredentialService credentialService;

    /**
     * 本处理器产出的全部会话包装（供适配器stop时统一关闭）
     */
    private final Set<WebSocketSessionWrapper> sessions = ConcurrentHashMap.newKeySet();

    /**
     * 构造推送WebSocket帧处理器
     *
     * @param codec             协议编解码器
     * @param sessionRegistry   会话注册表
     * @param interceptorChain  连接拦截器链
     * @param frameProcessor    协议帧处理器
     * @param credentialService 连接凭证服务
     */
    public PushWebSocketHandler(IPushCodec codec, ISessionRegistry sessionRegistry,
        IConnectionInterceptor interceptorChain, PushFrameProcessor frameProcessor,
        PushCredentialService credentialService) {
        this.codec = codec;
        this.sessionRegistry = sessionRegistry;
        this.interceptorChain = interceptorChain;
        this.frameProcessor = frameProcessor;
        this.credentialService = credentialService;
    }

    /**
     * 连接建立回调：凭证校验 → 预分配会话登记 → 统一ACK
     *
     * @param session 底层WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String ticket;
        PushCredential credential;
        try {
            ticket = extractTicket(session);
            credential = credentialService.consume(ticket);
        } catch (PushProtocolException error) {
            log.warn("WebSocket连接凭证校验失败, uri:{}", session.getUri(), error);
            closeQuietly(session);
            return;
        }
        WebSocketSessionWrapper wrapper =
            new WebSocketSessionWrapper(credential.getSessionId(), session, codec, credential.getAttributes());
        sessions.add(wrapper);
        sessionRegistry.register(wrapper);
        interceptorChain.onConnect(wrapper);
        wrapper.markConnected();
        frameProcessor.sendHelloAck(wrapper);
        log.info("WebSocket连接建立, sessionId:{}", wrapper.getSessionId());
    }

    /**
     * 文本消息回调：刷新活性并委托协议层处理
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
        wrapper.touch();
        frameProcessor.handleText(wrapper, message.getPayload());
    }

    /**
     * 传输异常回调：记录并关闭该会话（不影响其他连接）
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
     * 连接关闭回调：注销会话并触发断开拦截器
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
        sessions.remove(wrapper);
        sessionRegistry.unregister(wrapper.getSessionId());
        interceptorChain.onDisconnect(wrapper, String.valueOf(status.getCode()));
        log.info("WebSocket连接关闭, sessionId:{}, status:{}", wrapper.getSessionId(), status);
    }

    /**
     * 关闭本处理器管理的全部会话（服务端优雅停机）
     *
     * @param reason 关闭原因标识
     */
    public void closeAll(String reason) {
        for (WebSocketSessionWrapper wrapper : sessions) {
            wrapper.close(reason);
            sessionRegistry.unregister(wrapper.getSessionId());
        }
        sessions.clear();
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
     * 按底层会话id解析会话包装（预分配sessionId与容器id解耦后需遍历匹配）
     *
     * @param session 底层WebSocket会话
     * @return 会话包装，不存在时返回null
     */
    private WebSocketSessionWrapper resolveWrapper(WebSocketSession session) {
        for (WebSocketSessionWrapper wrapper : sessions) {
            if (wrapper.getWebSocketSession() == session) {
                return wrapper;
            }
        }
        return null;
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
