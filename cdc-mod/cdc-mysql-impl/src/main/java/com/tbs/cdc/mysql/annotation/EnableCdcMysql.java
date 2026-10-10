package com.tbs.cdc.mysql.annotation;

import com.tbs.cdc.mysql.assemble.CdcMysqlAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用CDC MySQL捕获能力的装配注解
 * <p>
 * 标注在Spring Boot启动类上，通过{@link CdcMysqlAutoRegister}一步装配MySQL捕获实现
 * （{@code com.tbs.cdc.mysql}）与CDC通用运行时（{@code com.tbs.cdc}），
 * 参考{@code @EnableRabbitMq}同时装配厂商实现与抽象层的模式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(CdcMysqlAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
@Documented
public @interface EnableCdcMysql {
}
