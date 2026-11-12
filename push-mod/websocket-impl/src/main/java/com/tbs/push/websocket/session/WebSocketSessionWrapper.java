package com.tbs.push.websocket.session;

import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket统一会话包装
 * <p>
 * 将Spring WebSocketSession包装为推送协议的统一会话{@link IConnectionSession}：
 * 发送统一经编解码器转文本帧，底层IOException被吞掉并以false返回（投递结果如实留痕）；
 * 状态机与活性时间戳由本类单点维护，send/close均同步串行化以规避底层并发写限制。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class WebSocketSessionWrapper implements IConnectionSession {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionWrapper.class);

    /**
     * 底层WebSocket会话
     */
    private final WebSocketSession webSocketSession;

    /**
     * 协议编解码器
     */
    private final IPushCodec codec;

    /**
     * 会话唯一标识（认证阶段预分配，与凭证/注册表/投递记录同源）
     */
    private final String sessionId;

    /**
     * 会话属性表
     */
    private final Map<String, String> attributes = new ConcurrentHashMap<>();

    /**
     * 连接状态（volatile保证跨线程可见）
     */
    private volatile ConnectionStateEnum state = ConnectionStateEnum.CONNECTING;

    /**
     * 最近活性时间戳（毫秒）
     */
    private volatile long lastActiveTime = System.currentTimeMillis();

    /**
     * 关闭标志，保证close幂等
     */
    private volatile boolean closed = false;

    /**
     * 构造WebSocket会话包装
     *
     * @param sessionId  认证阶段预分配的会话唯一标识
     * @param webSocketSession 底层WebSocket会话
     * @param codec      协议编解码器
     * @param attributes 认证通过后注入的会话属性（userId等），允许为null
     */
    public WebSocketSessionWrapper(String sessionId, WebSocketSession webSocketSession, IPushCodec codec,
        Map<String, String> attributes) {
        this.sessionId = sessionId;
        this.webSocketSession = webSocketSession;
        this.codec = codec;
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    /**
     * 获取会话唯一标识（认证阶段预分配，不取底层容器会话id）
     *
     * @return 会话唯一标识
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取传输类型标识
     *
     * @return 传输类型标识
     */
    @Override
    public String getTransportType() {
        return com.tbs.push.constants.PushFrameConstants.TRANSPORT_WEBSOCKET;
    }

    /**
     * 获取当前连接状态
     *
     * @return 当前连接状态
     */
    @Override
    public ConnectionStateEnum getState() {
        return state;
    }

    /**
     * 标记会话握手完成进入可用态
     */
    @Override
    public void markConnected() {
        if (state == ConnectionStateEnum.CONNECTING) {
            state = ConnectionStateEnum.CONNECTED;
        }
    }

    /**
     * 发送协议帧
     * <p>
     * 同步串行化发送，规避Spring标准WebSocket会话的并发写限制；
     * 底层异常吞掉并返回false，由分发器记录投递失败。
     * </p>
     *
     * @param frame 协议帧
     * @return true表示帧已写入传输通道
     */
    @Override
    public boolean send(PushFrame frame) {
        if (closed || !webSocketSession.isOpen()) {
            return false;
        }
        boolean sent;
        synchronized (webSocketSession) {
            try {
                webSocketSession.sendMessage(new TextMessage(codec.encodeFrame(frame)));
                touch();
                sent = true;
            } catch (Exception error) {
                sent = false;
                // 与 SSE 同口径：只记摘要，关闭后本会话不再写入，日志不会随消息量放大
                log.warn("WebSocket帧发送失败, 会话将关闭, sessionId:{}, cause:{}", getSessionId(), error.toString());
            }
        }
        if (!sent) {
            close("send-failed");
        }
        return sent;
    }

    /**
     * 关闭会话（幂等）
     *
     * @param reason 关闭原因标识
     */
    @Override
    public void close(String reason) {
        if (closed) {
            return;
        }
        closed = true;
        state = ConnectionStateEnum.CLOSED;
        try {
            webSocketSession.close(CloseStatus.NORMAL);
        } catch (Exception error) {
            log.warn("WebSocket会话关闭失败, sessionId:{}, reason:{}", getSessionId(), reason, error);
        }
    }

    /**
     * 触碰会话活性时间戳
     */
    @Override
    public void touch() {
        lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 获取最近一次活性时间戳
     *
     * @return 最近一次活性时间戳（毫秒）
     */
    @Override
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 设置会话属性
     *
     * @param key   属性键
     * @param value 属性值
     */
    @Override
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * 获取会话属性
     *
     * @param key 属性键
     * @return 属性值，不存在时返回null
     */
    @Override
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 获取底层WebSocket会话
     *
     * @return 底层WebSocket会话
     */
    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

}
