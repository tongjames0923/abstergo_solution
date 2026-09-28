package com.tbs.push.sse.annotation;

import com.tbs.push.config.PushPackAutoRegister;
import com.tbs.push.sse.config.SsePushAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用推送SSE传输适配的注解
 * <p>
 * 纯注解扫包驱动：同时导入推送抽象层（PushPackAutoRegister，扫描com.tbs.push，
 * 装配协议层默认组件如PushCredentialService）与SSE适配层（SsePushAutoRegister，
 * 扫描com.tbs.push.sse），引入后推送能力即完整可用，不依赖任何配置文件开关。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import({PushPackAutoRegister.class, SsePushAutoRegister.class})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsPushSse {
}
