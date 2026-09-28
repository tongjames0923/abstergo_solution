package com.tbs.push.websocket.config;

import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.impl.protocol.PushFrameProcessor;
import com.tbs.push.websocket.adapter.WebSocketTransportAdapter;
import com.tbs.push.websocket.config.property.WebSocketPushProperty;
import com.tbs.push.websocket.controller.WsPushAuthController;
import com.tbs.push.websocket.handler.PushWebSocketHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

/**
 * 推送WebSocket传输装配
 * <p>
 * 总开关tbs.push.enable=true时启用本配置类；传输选择条件tbs.push.transport=websocket
 * 落在具体Bean上（未配置transport时默认选中websocket，与sse-impl互斥装配）：
 * 注册WebSocket端点并装配传输适配器，端点路径与跨域来源经{@link WebSocketPushProperty}配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.push", name = "enable", havingValue = "true")
@EnableWebSocket
@EnableConfigurationProperties(WebSocketPushProperty.class)
public class WebSocketPushConfig implements WebSocketConfigurer {

    /**
     * WebSocket端点路径属性
     */
    private final WebSocketPushProperty property;

    /**
     * 帧处理器提供者（transport条件未命中时为空，端点注册跳过）
     */
    private final ObjectProvider<PushWebSocketHandler> handlerProvider;

    /**
     * 构造WebSocket传输装配
     *
     * @param property       WebSocket传输配置属性
     * @param handlerProvider 帧处理器提供者
     */
    public WebSocketPushConfig(WebSocketPushProperty property, ObjectProvider<PushWebSocketHandler> handlerProvider) {
        this.property = property;
        this.handlerProvider = handlerProvider;
    }

    /**
     * 推送WebSocket帧处理器
     *
     * @param codec             协议编解码器
     * @param sessionRegistry   会话注册表
     * @param interceptorChain  连接拦截器链
     * @param frameProcessor    协议帧处理器
     * @param credentialService 连接凭证服务
     * @return 推送WebSocket帧处理器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "transport", havingValue = "websocket", matchIfMissing = true)
    @ConditionalOnMissingBean(PushWebSocketHandler.class)
    public PushWebSocketHandler pushWebSocketHandler(IPushCodec codec, ISessionRegistry sessionRegistry,
        IConnectionInterceptor interceptorChain, PushFrameProcessor frameProcessor,
        PushCredentialService credentialService) {
        return new PushWebSocketHandler(codec, sessionRegistry, interceptorChain, frameProcessor, credentialService);
    }

    /**
     * WebSocket传输适配器
     *
     * @param handler  推送WebSocket帧处理器
     * @return WebSocket传输适配器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "transport", havingValue = "websocket", matchIfMissing = true)
    @ConditionalOnMissingBean(WebSocketTransportAdapter.class)
    public ITransportAdapter websocketTransportAdapter(PushWebSocketHandler handler) {
        return new WebSocketTransportAdapter(handler, property.getPath());
    }

    /**
     * 连接认证薄壳控制器
     * <p>
     * 逻辑全部委托协议层PushCredentialService；与SSE侧同构薄壳经transport条件互斥。
     * </p>
     *
     * @param credentialService 连接凭证服务
     * @return 连接认证薄壳控制器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push", name = "transport", havingValue = "websocket", matchIfMissing = true)
    @ConditionalOnMissingBean(WsPushAuthController.class)
    public WsPushAuthController wsPushAuthController(PushCredentialService credentialService) {
        return new WsPushAuthController(credentialService);
    }

    /**
     * 注册WebSocket端点
     * <p>
     * transport条件未命中（如选择SSE传输）时处理器不存在，跳过端点注册。
     * </p>
     *
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        PushWebSocketHandler handler = handlerProvider.getIfAvailable();
        if (handler == null) {
            return;
        }
        registry.addHandler(handler, property.getPath())
            .setAllowedOrigins(splitOrigins(property.getAllowedOrigins()));
    }

    /**
     * 拆分跨域来源配置为注册表所需的数组
     *
     * @param allowedOrigins 逗号分隔的来源配置
     * @return 来源数组
     */
    private String[] splitOrigins(String allowedOrigins) {
        if (!StringUtils.hasText(allowedOrigins)) {
            return new String[] {"*"};
        }
        return Arrays.stream(allowedOrigins.split(",")).map(String::trim).toArray(String[]::new);
    }

}
