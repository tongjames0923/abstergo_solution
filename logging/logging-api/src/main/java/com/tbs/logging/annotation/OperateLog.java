package com.tbs.logging.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级操作日志注解
 * <p>
 * 标注于需要记录方法级操作日志的方法上，配合方法级日志切面（OperateLogAspect）工作。
 * 与接口级审计（默认记录全部 Controller 方法，{@code @NoLog} 跳过）的采集方向相反，
 * 方法级日志遵循<b>"默认不记录、标注才记录"</b>原则——避免对全部 Service 方法
 * 无差别采集导致日志爆炸，仅对业务认定的关键方法做精细审计。
 * </p>
 * <p>
 * 记录内容：方法全名（类名.方法名）、注解描述、入参（可选）、返回结果（可选）、
 * 异常信息、耗时、操作用户、链路追踪ID。环境维度信息（链路追踪ID、操作用户、
 * IP、请求URL与方法）经日志上下文链路（创建器与拦截器管道）统一获取并回填。
 * 记录经 {@code ILogRecorder} 异步落库，与所属 HTTP 请求的接口级记录
 * 通过 traceId 关联。
 * </p>
 * <p>
 * <b>与接口级审计的关系</b>：若本注解标注在 Controller 方法上，该方法将同时产生
 * 接口级记录（1 条）与方法级记录（1 条），两者职责不同、互不替代。
 * </p>
 * <p>
 * <b>启用前提</b>：需开启 {@code tbs.system.enable-method-log=true}
 * （默认关闭，见 {@code AppSettingProperty}）。
 * </p>
 *
 * @author abstergo
 * @see com.tbs.app.intercept.OperateLogAspect
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperateLog {

    /**
     * 操作描述
     * <p>
     * 落库到日志的操作内容字段，建议填写业务语义描述（如"导出用户报表"）。
     * 为空时落库内容为方法全名。
     * </p>
     *
     * @return 操作描述
     */
    String value() default "";

    /**
     * 是否记录方法入参
     *
     * @return true 记录入参 JSON（默认），false 跳过
     */
    boolean recordParams() default true;

    /**
     * 是否记录方法返回结果
     * <p>
     * 默认不记录——返回结果（如分页列表）体量通常远大于入参，
     * 需要审计结果时再显式开启。
     * </p>
     *
     * @return true 记录结果 JSON，false 跳过（默认）
     */
    boolean recordResult() default false;
}
