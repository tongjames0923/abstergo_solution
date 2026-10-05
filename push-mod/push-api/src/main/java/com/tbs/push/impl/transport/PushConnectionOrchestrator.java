package com.tbs.push.impl.transport;

import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.impl.protocol.PushFrameProcessor;
import com.tbs.push.model.auth.PushCredential;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接生命周期编排器（跨传输统一的建立/上行/清理流程中枢）
 * <p>
 * 传输适配器内部共享的协议层编排件，把原先散落在各适配器中的复制流程收拢为
 * 唯一实现，保证跨传输行为一致：
 * <ul>
 *     <li>establish — 注册→拦截器链onConnect（链式、单个异常仅记日志跳过）→
 *     拦截器已拒绝（会话被关闭）则登记清理并终止→置可用→下发hello ACK</li>
 *     <li>runUpstream/runUpstreamText — 活性刷新后委托协议帧处理器</li>
 *     <li>closeSession — 主动关闭：物理拆除+清理链一次闭环，不依赖事件回传</li>
 *     <li>cleanup — 清理链唯一实现：以注册表注销的原子唯一性为幂等门禁，
 *     首次注销方执行 订阅清理→拦截器链onDisconnect；双入口竞态仅一方生效</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushConnectionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PushConnectionOrchestrator.class);

    /**
     * 会话注册表（在线会话权威登记，注销原子性是清理幂等门禁）
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 订阅注册表
     */
    private final ITopicRegistry topicRegistry;

    /**
     * 连接拦截器链提供者（多实现链式执行，单个异常隔离不阻断主流程）
     */
    private final ObjectProvider<IConnectionInterceptor> interceptorChain;

    /**
     * 协议帧处理器
     */
    private final PushFrameProcessor frameProcessor;

    /**
     * 连接凭证服务
     */
    private final PushCredentialService credentialService;

    /**
     * 构造连接生命周期编排器
     *
     * @param sessionRegistry    会话注册表
     * @param topicRegistry      订阅注册表
     * @param interceptorChain   连接拦截器链提供者
     * @param frameProcessor     协议帧处理器
     * @param credentialService  连接凭证服务
     */
    public PushConnectionOrchestrator(ISessionRegistry sessionRegistry, ITopicRegistry topicRegistry,
        ObjectProvider<IConnectionInterceptor> interceptorChain, PushFrameProcessor frameProcessor,
        PushCredentialService credentialService) {
        this.sessionRegistry = sessionRegistry;
        this.topicRegistry = topicRegistry;
        this.interceptorChain = interceptorChain;
        this.frameProcessor = frameProcessor;
        this.credentialService = credentialService;
    }

    /**
     * 原子消费一次性连接凭证（缺失/无效/过期抛协议异常）
     *
     * @param ticket 连接凭证文本
     * @return 凭证载荷（含预分配sessionId与认证阶段绑定的属性）
     */
    public PushCredential consumeTicket(String ticket) {
        return credentialService.consume(ticket);
    }

    /**
     * 建立编排：注册→拦截器链→拒绝判定→置可用→ACK
     * <p>
     * 拦截器onConnect中关闭会话即为拒绝（如建连身份不一致）；被拒会话完成清理登记后
     * 返回，不置可用态也不下发ACK。
     * </p>
     *
     * @param session 已包装登记的候选会话（由适配器构造）
     */
    public void establish(IConnectionSession session) {
        sessionRegistry.register(session);
        for (IConnectionInterceptor interceptor : interceptorList()) {
            try {
                interceptor.onConnect(session);
            } catch (Exception error) {
                log.error("连接拦截器[{}]执行失败, sessionId:{}", interceptor.getClass().getSimpleName(),
                    session.getSessionId(), error);
            }
        }
        if (session.getState() == ConnectionStateEnum.CLOSED) {
            log.warn("建连被拦截器拒绝, sessionId:{}", session.getSessionId());
            cleanup(session, PushFrameConstants.CLOSE_REASON_CONNECT_REJECTED);
            return;
        }
        session.markConnected();
        frameProcessor.sendHelloAck(session);
        log.info("推送连接建立, sessionId:{}, transport:{}", session.getSessionId(), session.getTransportType());
    }

    /**
     * 上行编排：活性刷新并交协议帧处理器按类型分发
     *
     * @param session 已解析校验的统一会话
     * @param frame   结构化协议帧
     */
    public void runUpstream(IConnectionSession session, PushFrame frame) {
        session.touch();
        frameProcessor.handleFrame(session, frame);
    }

    /**
     * 上行编排（文本入口）：解码与协议违规处置由帧处理器负责
     *
     * @param session 已解析校验的统一会话
     * @param text    上行帧JSON文本
     */
    public void runUpstreamText(IConnectionSession session, String text) {
        session.touch();
        frameProcessor.handleText(session, text);
    }

    /**
     * 按标识解析已登记会话
     *
     * @param sessionId 会话唯一标识
     * @return 统一会话，不存在时返回null
     */
    public IConnectionSession resolveSession(String sessionId) {
        return sessionRegistry.get(sessionId);
    }

    /**
     * 服务端主动关闭：物理拆除与清理链一次闭环，不依赖事件回传
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识
     * @return true表示本次调用完成关闭与清理，false表示会话不存在或已被清理
     */
    public boolean closeSession(String sessionId, String reason) {
        IConnectionSession session = sessionRegistry.get(sessionId);
        if (session == null) {
            return false;
        }
        session.close(reason);
        return cleanup(session, reason);
    }

    /**
     * 被动关闭事件回传：物理通道已死，仅执行清理链（幂等门禁去重）
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识
     * @return true表示本次调用执行了清理链，false表示重复或会话不存在
     */
    public boolean onChannelClosed(String sessionId, String reason) {
        IConnectionSession session = sessionRegistry.get(sessionId);
        if (session == null) {
            return false;
        }
        return cleanup(session, reason);
    }

    /**
     * 全量关闭（优雅停机）：按注册表快照逐个主动关闭
     *
     * @param reason 关闭原因标识
     * @return 实际完成关闭的会话数
     */
    public int closeAll(String reason) {
        List<IConnectionSession> snapshot = new ArrayList<>(sessionRegistry.all());
        int closed = 0;
        for (IConnectionSession session : snapshot) {
            if (closeSession(session.getSessionId(), reason)) {
                closed++;
            }
        }
        return closed;
    }

    /**
     * 清理链唯一实现：注册表原子注销为幂等门禁，首次注销方执行订阅清理与断开拦截器链
     *
     * @param session 待清理会话
     * @param reason  关闭原因标识（日志留痕）
     * @return true表示本次调用执行了清理链
     */
    private boolean cleanup(IConnectionSession session, String reason) {
        if (!sessionRegistry.unregister(session.getSessionId())) {
            return false;
        }
        topicRegistry.clearSession(session.getSessionId());
        for (IConnectionInterceptor interceptor : interceptorList()) {
            try {
                interceptor.onDisconnect(session, reason);
            } catch (Exception error) {
                log.error("断开拦截器[{}]执行失败, sessionId:{}", interceptor.getClass().getSimpleName(),
                    session.getSessionId(), error);
            }
        }
        log.info("推送连接关闭, sessionId:{}, transport:{}, reason:{}", session.getSessionId(),
            session.getTransportType(), reason);
        return true;
    }

    /**
     * 快照拦截器链（ObjectProvider按注册顺序聚合，无实现时空链）
     *
     * @return 拦截器列表
     */
    private List<IConnectionInterceptor> interceptorList() {
        List<IConnectionInterceptor> interceptors = new ArrayList<>();
        interceptorChain.orderedStream().forEach(interceptors::add);
        return interceptors;
    }

}
