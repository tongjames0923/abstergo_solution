package com.tbs.push.websocket.annotation;

import com.tbs.push.annotation.EnableTbsPush;
import com.tbs.push.websocket.config.WebSocketPushAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用推送WebSocket传输适配的注解
 * <p>
 * 已包含推送抽象层（@EnableTbsPush）的装配范围，引入后同时获得协议层默认组件
 * 与WebSocket传输适配能力。需配合总开关 tbs.push.enable=true 与
 * tbs.push.transport=websocket 生效。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import({EnableTbsPushRegistrar.class, WebSocketPushAutoRegister.class})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsPushWebSocket {
}
