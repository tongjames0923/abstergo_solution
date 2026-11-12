package com.tbs.push.sse.session;

import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE统一会话包装
 * <p>
 * 将SseEmitter下行流包装为推送协议的统一会话{@link IConnectionSession}：
 * 上行由HTTP POST端点承接（组合成双向通道），下行经emitter发送（事件名为帧类型小写值）。
 * 会话标识由服务端生成，经握手确认帧下发，客户端上行时回传以定位会话。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SseSessionWrapper implements IConnectionSession {

    private static final Logger log = LoggerFactory.getLogger(SseSessionWrapper.class);

    /**
     * 底层SseEmitter
     */
    private final SseEmitter emitter;

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
     * 关闭标志，保证close幂等并避免完成回调递归清理
     */
    private volatile boolean closed = false;

    /**
     * 构造SSE会话包装
     *
     * @param sessionId  认证阶段预分配的会话唯一标识
     * @param emitter    底层SseEmitter
     * @param codec      协议编解码器
     * @param attributes 认证通过后注入的会话属性（userId等），允许为null
     */
    public SseSessionWrapper(String sessionId, SseEmitter emitter, IPushCodec codec, Map<String, String> attributes) {
        this.sessionId = sessionId;
        this.emitter = emitter;
        this.codec = codec;
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    /**
     * 获取会话唯一标识（认证阶段预分配）
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
        return PushFrameConstants.TRANSPORT_SSE;
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
     * 经SseEmitter下行发送：事件名为帧类型小写值，data为帧JSON文本；
     * 底层异常吞掉并返回false，由分发器记录投递失败。
     * </p>
     *
     * @param frame 协议帧
     * @return true表示帧已写入传输通道
     */
    @Override
    public boolean send(PushFrame frame) {
        if (closed) {
            return false;
        }
        boolean sent;
        synchronized (emitter) {
            try {
                emitter.send(SseEmitter.event().name(frame.getType().getValue())
                    .data(codec.encodeFrame(frame), MediaType.APPLICATION_JSON));
                touch();
                sent = true;
            } catch (Exception error) {
                sent = false;
                // 客户端断链：只记一行摘要，不打印整段异常堆栈；close 后本会话不会再被写入，日志天然限流到一次
                log.warn("SSE帧发送失败, 会话将关闭, sessionId:{}, cause:{}", sessionId, error.toString());
            }
        }
        if (!sent) {
            // 立即置失效并走完整清理链（emitter 完成回调注销会话与订阅），避免后续消息反复向断链写
            close(PushFrameConstants.CLOSE_REASON_CHANNEL_CLOSED);
        }
        return sent;
    }

    /**
     * 关闭会话（幂等）：完成下行流，后续由完成回调统一清理
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
            emitter.complete();
        } catch (Exception error) {
            log.warn("SSE会话关闭失败, sessionId:{}, reason:{}", sessionId, reason, error);
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
     * 获取底层SseEmitter
     *
     * @return 底层SseEmitter
     */
    public SseEmitter getEmitter() {
        return emitter;
    }

}
