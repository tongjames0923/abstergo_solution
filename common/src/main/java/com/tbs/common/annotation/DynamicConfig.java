package com.tbs.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态配置注解——声明某个字段或单参 setter 的取值来源为配置项 key，
 * 由 common 的 {@code DynamicConfigBinder} 在启动时初绑、并在配置中心刷新时热更新。
 * <p>
 * 目标是「复用同一个 Bean 实例、只改可变位」，而非 {@code @RefreshScope} 重建对象，
 * 因此对有状态组件（如带并发计数的限流器）安全：刷新只替换阈值，不重置其余状态。
 * </p>
 *
 * <h3>可用位置</h3>
 * <ul>
 *   <li><b>字段</b>：{@code @Target(FIELD)}，字段须非 {@code final}；若会被并发热路径读取，声明为 {@code volatile}。
 *       注意：字段直接反射写入对 AOP 代理不生效，Binder 会先解包到目标对象再写入。</li>
 *   <li><b>Setter 方法</b>：{@code @Target(METHOD)}，方法须有且仅有一个参数，参数类型即目标类型。</li>
 * </ul>
 *
 * <h3>取值与转换</h3>
 * <p>按 key 从 {@code Environment}（含本地 yml 与 Nacos 等导入源）读取字符串，
 * 再按目标字段/参数类型经 {@code DefaultConversionService} 转换（支持 int/long/boolean/String 等标量）。
 * key 缺失或转换失败时保留原值不覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class Foo {
 *     @DynamicConfig("tbs.foo.threshold")
 *     private volatile int threshold;
 *
 *     @DynamicConfig("tbs.foo.mode")
 *     public void setMode(String mode) { this.mode = mode; }
 * }
 * }</pre>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DynamicConfig {

    /**
     * 配置项 key，与 {@code Environment} 中的属性名一致（如 {@code tbs.sql.conn-limit.max-waiters}）。
     *
     * @return 配置项 key
     */
    String value();
}
