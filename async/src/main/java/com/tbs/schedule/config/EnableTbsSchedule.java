package com.tbs.schedule.config;

import com.tbs.common.annotation.EnableCommonPack;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用轻量调度包注解
 * <p>
 * 用于自动注册和启用 schedule 包中的延迟调度组件。通过 {@link Import} 导入 {@link ScheduleAutoRegister}
 * 实现包扫描与 Bean 注册，并由 {@link ScheduleConfig} 按 {@code tbs.schedule.enable} 开关条件化装配内核。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@EnableCommonPack
@Inherited
@Import(ScheduleAutoRegister.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableTbsSchedule {
}
