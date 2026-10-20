package com.tbs.push.impl.transport;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.model.frame.FrameTypeEnum;
import com.tbs.push.model.frame.PushFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 推送连接优雅停机器（跨传输统一）
 * <p>
 * 以{@link SmartLifecycle}最高相位接入容器关闭流程：关闭时最先停止，
 * 早于Web服务器的优雅停机排水（其相位为{@code MAX_PHASE - 1024}）完成推送连接的
 * 通知与拆除——长连接（SSE无限期emitter/未来的WS）不会自己完成，若不主动拆除会
 * 把{@code server.shutdown=graceful}的等待拖满超时。停机时序：
 * <ol>
 *     <li>向全部在线会话广播SHUTDOWN通知帧（携带宽限期，提示客户端可收尾上行后重连）</li>
 *     <li>等待宽限期（{@code tbs.push.shutdown-grace-millis}，缺省5秒）——留出上行窗口，
 *     尤其兼容未来WS的上行能力，让客户端在连接被切断前完成最后一次发送</li>
 *     <li>主动关闭全部会话（物理拆除+清理链一次闭环），使在途长连接请求即时结束</li>
 * </ol>
 * 停止动作以会话注册表为唯一事实源，与具体传输无关，SSE/WS共用一条关停路径。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushGracefulShutdownLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(PushGracefulShutdownLifecycle.class);

    /**
     * 连接生命周期编排器（广播与全量关闭的唯一执行者）
     */
    private final PushConnectionOrchestrator orchestrator;

    /**
     * 停机宽限期（毫秒），非正数表示通知后立即关闭
     */
    private final long shutdownGraceMillis;

    /**
     * 运行状态标志（start后置真，stop后即时失效，避免重复关停）
     */
    private volatile boolean running = false;

    /**
     * 构造优雅停机器
     *
     * @param orchestrator         连接生命周期编排器
     * @param shutdownGraceMillis  停机宽限期（毫秒）
     */
    public PushGracefulShutdownLifecycle(PushConnectionOrchestrator orchestrator, long shutdownGraceMillis) {
        this.orchestrator = orchestrator;
        this.shutdownGraceMillis = shutdownGraceMillis;
    }

    /**
     * 容器就绪即进入可停机状态（本组件无启动动作，仅登记运行标志）
     */
    @Override
    public void start() {
        running = true;
    }

    /**
     * 优雅停机：广播SHUTDOWN通知帧→等待宽限期→全量关闭会话（幂等，重复停止无副作用）
     */
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        int notified = orchestrator.broadcast(shutdownNotice());
        log.info("已向{}个在线会话下发停机通知帧, 宽限期{}ms后关闭全部推送连接", notified, shutdownGraceMillis);
        awaitGrace();
        int closed = orchestrator.closeAll(PushFrameConstants.CLOSE_REASON_SERVER_SHUTDOWN);
        log.info("停机已关闭{}个推送连接", closed);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 声明最高相位：容器关闭时最先停止，确保在Web服务器优雅停机排水之前完成长连接拆除，
     * 避免永不自行完成的SSE/WS请求把优雅停机拖满超时。
     *
     * @return 生命周期相位（最高）
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * 构造停机通知帧：载荷携带关闭原因与宽限期，供客户端展示与维护重连节奏
     *
     * @return SHUTDOWN帧
     */
    private PushFrame shutdownNotice() {
        PushFrame frame = new PushFrame(FrameTypeEnum.SHUTDOWN);
        frame.setVersion(PushFrameConstants.PROTOCOL_VERSION);
        frame.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", PushFrameConstants.CLOSE_REASON_SERVER_SHUTDOWN);
        payload.put("graceMillis", Math.max(0L, shutdownGraceMillis));
        frame.setPayload(JsonUtils.toJSONString(payload));
        return frame;
    }

    /**
     * 等待宽限期：非正数不等待；线程中断则立即结束等待并转入关闭（不阻塞停机）
     */
    private void awaitGrace() {
        if (shutdownGraceMillis <= 0) {
            return;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(shutdownGraceMillis);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            log.warn("停机宽限期等待被中断, 立即关闭全部推送连接");
        }
    }

}
