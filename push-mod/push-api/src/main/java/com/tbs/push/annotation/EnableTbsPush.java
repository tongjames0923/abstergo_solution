package com.tbs.push.annotation;

import com.tbs.push.config.PushPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用推送抽象层默认装配的注解
 * <p>
 * 在Spring Boot应用中使用此注解即可快速启动推送协议层的厂商无关默认组件
 * （编解码器、会话与订阅注册表、消息源与投递记录兜底存储、投递协调器、
 * 分发器与双驱动调度、心跳监控）。
 * 需配合总开关 tbs.push.enable=true 生效；具体传输适配
 * 由对应impl模块（如@EnableTbsPushWebSocket、@EnableTbsPushSse）提供，
 * 其注解已包含本注解的装配范围。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(PushPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsPush {
}
