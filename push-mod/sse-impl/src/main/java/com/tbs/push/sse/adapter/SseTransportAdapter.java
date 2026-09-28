package com.tbs.push.sse.adapter;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.impl.protocol.PushFrameProcessor;
import com.tbs.push.model.auth.PushCredential;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.push.model.session.ConnectionStateEnum;
import com.tbs.push.model.session.TransportCapability;
import com.tbs.push.sse.session.SseSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE组合通道传输适配器
 * <p>
 * 将SSE的单向下行流组合为协议层的统一双向会话：
 * <ul>
 *     <li>下行：{@code connect()}创建SseEmitter并包装登记为统一会话，
 *     随即下发握手确认帧（载荷含sessionId与协议版本）</li>
 *     <li>上行：客户端携sessionId调用HTTP POST上行端点（由控制器承接），
 *     本适配器校验会话有效后委托协议层帧处理器处理</li>
 * </ul>
 * emitter的完成/超时/异常回调统一收敛到cleanup：注销会话、清理订阅关系、触发断开拦截器。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SseTransportAdapter implements ITransportAdapter {

    private static final Logger log = LoggerFactory.getLogger(SseTransportAdapter.class);

    /**
     * 协议编解码器
     */
    private final IPushCodec codec;

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 订阅注册表
     */
    private final ITopicRegistry topicRegistry;

    /**
     * 连接拦截器链
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
     * 本适配器产出的全部会话包装（供stop时统一关闭）
     */
    private final Set<SseSessionWrapper> sessions = ConcurrentHashMap.newKeySet();

    /**
     * 构造SSE传输适配器
     *
     * @param codec             协议编解码器
     * @param sessionRegistry   会话注册表
     * @param topicRegistry     订阅注册表
     * @param interceptorChain  连接拦截器链
     * @param frameProcessor    协议帧处理器
     * @param credentialService 连接凭证服务
     */
    public SseTransportAdapter(IPushCodec codec, ISessionRegistry sessionRegistry, ITopicRegistry topicRegistry,
        ObjectProvider<IConnectionInterceptor> interceptorChain, PushFrameProcessor frameProcessor,
        PushCredentialService credentialService) {
        this.codec = codec;
        this.sessionRegistry = sessionRegistry;
        this.topicRegistry = topicRegistry;
        this.interceptorChain = interceptorChain;
        this.frameProcessor = frameProcessor;
        this.credentialService = credentialService;
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
     * 获取传输能力声明（HTTP POST上行+SSE下行组合为双向文本通道）
     *
     * @return 传输能力声明
     */
    @Override
    public TransportCapability capability() {
        return TransportCapability.bidirectionalText();
    }

    /**
     * 建立SSE下行通道（统一连接建立流程）
     * <p>
     * 原子消费一次性连接凭证（缺失/无效/过期抛协议异常，经全局异常处理返回错误响应），
     * 成功后以预分配sessionId包装登记为统一会话、注入认证属性、挂载清理回调、
     * 下发统一ACK帧并进入可用态。
     * </p>
     *
     * @param ticket 客户端携带的连接凭证（query param push-ticket）
     * @return 返回给控制器的SseEmitter
     * @throws PushProtocolException 凭证校验失败时抛出
     */
    public SseEmitter connect(String ticket) {
        PushCredential credential = credentialService.consume(ticket);
        SseEmitter emitter = new SseEmitter(0L);
        SseSessionWrapper wrapper =
            new SseSessionWrapper(credential.getSessionId(), emitter, codec, credential.getAttributes());
        sessions.add(wrapper);
        emitter.onCompletion(() -> cleanup(wrapper, "sse-completed"));
        emitter.onTimeout(() -> cleanup(wrapper, "sse-timeout"));
        emitter.onError(error -> cleanup(wrapper, "sse-error"));
        sessionRegistry.register(wrapper);
        for (IConnectionInterceptor interceptor : interceptorChain) {
            try {
                interceptor.onConnect(wrapper);
            } catch (Exception e) {
                log.error("连接拦截器[{}]执行失败", interceptor.getClass().getSimpleName(), e);
            }
        }

        wrapper.markConnected();
        frameProcessor.sendHelloAck(wrapper);
        log.info("SSE连接建立, sessionId:{}", wrapper.getSessionId());
        return emitter;
    }

    /**
     * 处理HTTP POST上行的结构化协议帧
     * <p>
     * sessionId未注册、会话已关闭或帧缺失时快速失败（i18n协议异常，
     * 经全局异常处理转为统一响应）。帧内容直接交由协议层帧处理器按类型分发处理。
     * </p>
     *
     * @param sessionId 会话唯一标识
     * @param frame     结构化协议帧
     */
    public void uploadFrame(String sessionId, PushFrame frame) {
        AssertUtil.isTrue(StrUtil.isEmpty(sessionId))
            .i18n(PushI18nConstant.SESSION_ID_REQUIRED).throwIt();
        AssertUtil.isTrue(frame == null)
            .i18n(PushI18nConstant.FRAME_REQUIRED).throwIt();
        SseSessionWrapper wrapper = resolveSession(sessionId);
        AssertUtil.isTrue(wrapper == null)
            .i18n(PushI18nConstant.SESSION_NOT_FOUND).params(sessionId).throwIt();
        AssertUtil.isTrue(wrapper.getState() == ConnectionStateEnum.CLOSED)
            .i18n(PushI18nConstant.SESSION_NOT_CONNECTED).params(sessionId).throwIt();
        wrapper.touch();
        frameProcessor.handleFrame(wrapper, frame);
    }

    /**
     * 处理HTTP POST上行的传输文本（兼容入口）
     * <p>
     * 历史文本协议入口：解析失败由协议层按协议违规回发ERROR帧并关闭会话。
     * </p>
     *
     * @param sessionId 会话唯一标识
     * @param text      上行的帧JSON文本
     */
    public void uploadText(String sessionId, String text) {
        AssertUtil.isTrue(StrUtil.isEmpty(text))
            .i18n(PushI18nConstant.FRAME_REQUIRED).throwIt();
        SseSessionWrapper wrapper = resolveSession(sessionId);
        AssertUtil.isTrue(wrapper == null)
            .i18n(PushI18nConstant.SESSION_NOT_FOUND).params(sessionId).throwIt();
        AssertUtil.isTrue(wrapper.getState() == ConnectionStateEnum.CLOSED)
            .i18n(PushI18nConstant.SESSION_NOT_CONNECTED).params(sessionId).throwIt();
        wrapper.touch();
        frameProcessor.handleText(wrapper, text);
    }

    /**
     * 解析上行目标会话
     *
     * @param sessionId 会话唯一标识
     * @return 会话包装，未注册时返回null
     */
    private SseSessionWrapper resolveSession(String sessionId) {
        return sessionRegistry.get(sessionId) instanceof SseSessionWrapper
            ? (SseSessionWrapper) sessionRegistry.get(sessionId) : null;
    }

    /**
     * 启动适配器（幂等）
     */
    @Override
    public void start() {
        // SSE通道由HTTP端点按需建立，无全局启动动作
    }

    /**
     * 停止适配器（幂等）：优雅关闭全部SSE会话
     */
    @Override
    public void stop() {
        for (SseSessionWrapper wrapper : sessions) {
            wrapper.close(PushFrameConstants.CLOSE_REASON_SERVER_SHUTDOWN);
        }
        sessions.clear();
        log.info("SSE传输适配器已停止");
    }

    /**
     * 会话清理（幂等）：注销会话、清理订阅关系、触发断开拦截器
     *
     * @param wrapper 会话包装
     * @param reason  关闭原因标识
     */
    private void cleanup(SseSessionWrapper wrapper, String reason) {
        if (wrapper.getState() == ConnectionStateEnum.CLOSED) {
            return;
        }
        wrapper.close(reason);
        sessions.remove(wrapper);
        sessionRegistry.unregister(wrapper.getSessionId());
        topicRegistry.clearSession(wrapper.getSessionId());

        try {
            for (IConnectionInterceptor interceptor : interceptorChain) {
                try {
                    interceptor.onDisconnect(wrapper, reason);
                } catch (Exception e) {
                    log.error("连接拦截器[{}]执行失败", interceptor.getClass().getSimpleName(), e);
                }
            }
        } catch (Exception e) {
            log.error("连接拦截器执行失败", e);
        }


        log.info("SSE连接关闭, sessionId:{}, reason:{}", wrapper.getSessionId(), reason);
    }

}
