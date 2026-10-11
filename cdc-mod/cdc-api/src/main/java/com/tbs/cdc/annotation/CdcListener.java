package com.tbs.cdc.annotation;

import com.tbs.cdc.model.CdcChangeType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 变更事件监听注解
 * <p>
 * 标注在Spring Bean的方法上，声明该方法关注的表与变更类型，运行时按声明路由事件。
 * 方法须有且仅有一个{@link com.tbs.cdc.model.CdcEvent}类型的入参。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CdcListener {

    /**
     * 关注的表名或表名正则集合
     * <p>
     * 空数组表示不限制表。支持精确表名与正则表达式两种写法，命中其一即视为匹配。
     * </p>
     *
     * @return 表名/正则集合
     */
    String[] tables() default {};

    /**
     * 关注的变更类型集合
     * <p>
     * 空数组表示订阅全部变更类型。
     * </p>
     *
     * @return 变更类型集合
     */
    CdcChangeType[] types() default {};

    /**
     * 是否异步执行监听方法
     * <p>
     * true表示提交到独立线程池执行，慢监听不阻塞捕获链路；false表示在分发线程同步执行。
     * </p>
     *
     * @return true异步，false同步
     */
    boolean async() default true;
}
