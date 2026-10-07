package com.tbs.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tbs.common.provider.impl.RoutingServiceRegistrar;

import org.springframework.context.annotation.Import;

/**
 * 启用多来源路由服务扫描注册的注解
 * <p>
 * 导入 RoutingServiceRegistrar 扫描标注 @RoutingService 的接口并注册路由代理 Bean。
 * 经 EnableCommonPack 启用 common 模块时已随 ObjectProviderConfig 生效，
 * 未启用 common 包自动装配的场景可单独使用本注解按需开启。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RoutingServiceRegistrar.class)
public @interface EnableTbsRoutingService {

    /**
     * 扫描 @RoutingService 接口的基础包，为空时扫描默认包 com.tbs
     * <p>
     * 建议业务侧显式声明本属性，避免大型仓库下的全 classpath 扫描开销。
     * </p>
     *
     * @return 扫描基础包列表
     */
    String[] basePackages() default {};
}
