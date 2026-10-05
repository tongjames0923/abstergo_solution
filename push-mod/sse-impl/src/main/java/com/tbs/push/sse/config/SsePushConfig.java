package com.tbs.push.sse.config;

import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.impl.transport.PushConnectionOrchestrator;
import com.tbs.push.sse.adapter.SseTransportAdapter;
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
     * SSE传输适配器（全链路契约薄壳，流程编排委托协议层编排器）
     *
     * @param codec        协议编解码器
     * @param orchestrator 连接生命周期编排器
     * @return SSE传输适配器
     */
    @Bean
    @ConditionalOnMissingBean(SseTransportAdapter.class)
    public SseTransportAdapter sseTransportAdapter(IPushCodec codec, PushConnectionOrchestrator orchestrator) {
        return new SseTransportAdapter(codec, orchestrator);
    }

}
