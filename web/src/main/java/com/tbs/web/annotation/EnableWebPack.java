package com.tbs.web.annotation;

import com.tbs.common.annotation.EnableCommonPack;
import com.tbs.web.config.WebAutoRegister;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

/**
 * 启用 Web 模块自动装配
 * <p>
 * 在启动类标注后，会导入 {@link WebAutoRegister} 完成 Web 组件注册，并开启 Spring Security 的
 * Web 安全与基于注解的方法级安全（{@code @PreAuthorize} 等）。同时复用通用包的 {@code @EnableCommonPack}。
 * </p>
 *
 * @author tongj
 * @see WebAutoRegister Web 组件自动注册器
 */
@Import(WebAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableCommonPack
@Target(ElementType.TYPE)
public @interface EnableWebPack {
}
