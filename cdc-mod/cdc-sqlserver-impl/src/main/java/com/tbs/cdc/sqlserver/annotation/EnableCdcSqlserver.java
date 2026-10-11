package com.tbs.cdc.sqlserver.annotation;

import com.tbs.cdc.sqlserver.assemble.CdcSqlserverAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用CDC SQL Server捕获能力的装配注解
 * <p>
 * 标注在Spring Boot启动类上，通过{@link CdcSqlserverAutoRegister}一步装配SQL Server捕获实现
 * （{@code com.tbs.cdc.sqlserver}）与CDC通用运行时（{@code com.tbs.cdc}），
 * 参考{@code @EnableCdcMysql}同时装配厂商实现与抽象层的模式。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(CdcSqlserverAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
@Documented
public @interface EnableCdcSqlserver {
}
