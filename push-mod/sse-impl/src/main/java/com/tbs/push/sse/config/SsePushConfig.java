package com.tbs.push.sse.config;

import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.impl.protocol.PushFrameProcessor;
import com.tbs.push.sse.adapter.SseTransportAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 推送SSE传输装配
 * <p>
 * 由@EnableTbsPushSse注解扫包驱动装配（与websocket-impl按引入的注解互斥）：
 * 装配SSE组合通道适配器，端点路径固定为 /private/push/sse/connect（下行）与
 * /private/push/sse/upload（上行）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
public class SsePushConfig {

    /**
     * SSE传输适配器
     *
     * @param codec             协议编解码器
     * @param sessionRegistry   会话注册表
     * @param topicRegistry     订阅注册表
     * @param interceptorChain  连接拦截器链
     * @param frameProcessor    协议帧处理器
     * @param credentialService 连接凭证服务
     * @return SSE传输适配器
     */
    @Bean
    @ConditionalOnMissingBean(SseTransportAdapter.class)
    public SseTransportAdapter sseTransportAdapter(IPushCodec codec, ISessionRegistry sessionRegistry,
        ITopicRegistry topicRegistry, ObjectProvider<IConnectionInterceptor> interceptorChain,
        PushFrameProcessor frameProcessor,
        PushCredentialService credentialService) {
        return new SseTransportAdapter(codec, sessionRegistry, topicRegistry, interceptorChain, frameProcessor,
            credentialService);
    }

}
