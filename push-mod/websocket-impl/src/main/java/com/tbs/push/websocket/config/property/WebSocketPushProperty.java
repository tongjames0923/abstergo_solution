package com.tbs.push.websocket.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 推送WebSocket传输配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定tbs.push.websocket前缀的配置属性，
 * 集中管理WebSocket端点路径与跨域来源配置。
 * 不使用@Component注册，改由WebSocketPushConfig经@EnableConfigurationProperties显式激活。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties("tbs.push.websocket")
public class WebSocketPushProperty {

    /**
     * 默认端点路径
     */
    public static final String DEFAULT_ENDPOINT_PATH = "/push/ws";

    /**
     * 默认允许的跨域来源
     */
    public static final String DEFAULT_ALLOWED_ORIGIN = "*";

    /**
     * WebSocket端点路径
     */
    private String path = DEFAULT_ENDPOINT_PATH;

    /**
     * 允许的跨域来源，多个以逗号分隔
     */
    private String allowedOrigins = DEFAULT_ALLOWED_ORIGIN;

}
