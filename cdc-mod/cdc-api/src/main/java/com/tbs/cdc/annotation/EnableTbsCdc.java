package com.tbs.cdc.annotation;

import com.tbs.cdc.assemble.CdcAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用CDC通用运行时的装配注解
 * <p>
 * 标注在Spring Boot启动类上，通过{@link CdcAutoRegister}扫描{@code com.tbs.cdc}包，
 * 装配配置绑定、事件总线、监听器注册路由、过滤链、MQ桥接与生命周期编排等运行时组件。
 * 具体数据库捕获源需叠加对应实现注解（如MySQL的{@code @EnableCdcMysql}）。
 * 当{@code tbs.cdc.enable=false}时运行期零Bean生效、零线程占用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Import(CdcAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
@Documented
public @interface EnableTbsCdc {
}
