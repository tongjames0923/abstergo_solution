package com.tbs.push.websocket.adapter;

import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.model.session.TransportCapability;
import com.tbs.push.websocket.handler.PushWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket传输适配器
 * <p>
 * WebSocket传输的适配入口：端点注册由装配配置完成（WebSocketConfigurer），
 * 本适配器持有帧处理器引用，负责start/stop生命周期与能力声明。
 * stop时优雅关闭本传输产出的全部会话。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class WebSocketTransportAdapter implements ITransportAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTransportAdapter.class);

    /**
     * 推送WebSocket帧处理器
     */
    private final PushWebSocketHandler handler;

    /**
     * WebSocket端点路径
     */
    private final String endpointPath;

    /**
     * 启动标志，保证start/stop幂等
     */
    private volatile boolean started = false;

    /**
     * 构造WebSocket传输适配器
     *
     * @param handler      推送WebSocket帧处理器
     * @param endpointPath WebSocket端点路径
     */
    public WebSocketTransportAdapter(PushWebSocketHandler handler, String endpointPath) {
        this.handler = handler;
        this.endpointPath = endpointPath;
    }

    /**
     * 获取传输类型标识
     *
     * @return 传输类型标识
     */
    @Override
    public String getTransportType() {
        return PushFrameConstants.TRANSPORT_WEBSOCKET;
    }

    /**
     * 获取传输能力声明（原生双向文本通道，不支持二进制）
     *
     * @return 传输能力声明
     */
    @Override
    public TransportCapability capability() {
        return TransportCapability.bidirectionalText();
    }

    /**
     * 启动适配器（幂等）
     */
    @Override
    public void start() {
        if (started) {
            return;
        }
        started = true;
        log.info("WebSocket传输适配器已启动, 端点:{}", endpointPath);
    }

    /**
     * 停止适配器（幂等）：优雅关闭全部WebSocket会话
     */
    @Override
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        handler.closeAll(PushFrameConstants.CLOSE_REASON_SERVER_SHUTDOWN);
        log.info("WebSocket传输适配器已停止, 端点:{}", endpointPath);
    }

}
