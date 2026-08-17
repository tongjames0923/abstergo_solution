package com.tbs.oss.annotation;

import com.tbs.oss.config.OssPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用TBS OSS功能的注解
 * <p>
 * 在Spring Boot应用中使用此注解可以自动启用和配置TBS OSS功能
 * 通过@Import引入OssPackAutoRegister类，实现OSS相关组件的自动注册
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(OssPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsOss {
}
