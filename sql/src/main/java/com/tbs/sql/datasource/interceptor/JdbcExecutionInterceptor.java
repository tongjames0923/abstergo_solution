package com.tbs.sql.datasource.interceptor;

import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcConnectionContext;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.springframework.core.Ordered;

/**
 * JDBC 执行拦截器 SPI，注册为 Spring Bean 后由 DataSource 装饰框架按
 * {@link #getOrder()} 升序链式调用。
 *
 * <h3>四钩子设计</h3>
 * <ul>
 *   <li>{@link #interceptConnection}：连接获取阶段，仅作拦截点，不承担数据源路由职责</li>
 *   <li>{@link #onConnectionRelease}：连接归还（{@code close()}）阶段，与 {@link #interceptConnection} 成对，
 *       用于归还该连接在准入阶段占用的资源（如并发许可）</li>
 *   <li>{@link #interceptExecution}：SQL 执行前，放行或拒绝</li>
 *   <li>{@link #onExecutionCompleted}：SQL 执行完成后，用于参数观测、耗时统计、SQL 日志与慢 SQL 告警</li>
 * </ul>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>拦截器只能裁决（放行/拒绝），不能改写 SQL；裁决阶段不提供参数值，
 *       参数值仅在 {@link #onExecutionCompleted} 观测阶段可获取</li>
 *   <li>SQL 日志、耗时与慢 SQL 观测统一经 {@link #onExecutionCompleted} 回调获取，
 *       不再依赖驱动层 p6spy 代理</li>
 *   <li>拒绝时返回 {@link InterceptVerdict#deny}，由框架统一走 AssertUtil 抛 i18n 异常</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class DangerousDdlInterceptor implements JdbcExecutionInterceptor {
 *
 *     @Override
 *     public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
 *         if (context.getCommandKind() == SqlCommandKind.DDL) {
 *             return InterceptVerdict.deny("datasource.intercept.ddl.forbidden");
 *         }
 *         return InterceptVerdict.allow();
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 0;
 *     }
 * }
 * }</pre>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.datasource.decoration.InterceptorChain
 */
public interface JdbcExecutionInterceptor extends Ordered {

    /**
     * 连接获取阶段拦截。
     *
     * <p>默认放行。该钩子仅用于在连接建立前做准入裁决，
     * 明确<b>不承担</b>多数据源路由职责。</p>
     *
     * @param context 连接获取上下文
     * @return 裁决结果，放行返回 {@link InterceptVerdict#allow()}
     */
    default InterceptVerdict interceptConnection(JdbcConnectionContext context) {
        return InterceptVerdict.allow();
    }

    /**
     * 连接归还阶段回调，与 {@link #interceptConnection} 成对出现。
     *
     * <p>当代理连接的 {@code close()} 被调用（池化语义即“归还连接”）时，
     * 由框架恰好一次地触发本回调，供拦截器释放在 {@link #interceptConnection}
     * 中为该连接占用的资源（如并发许可）。默认空实现。</p>
     *
     * @param context 与 {@link #interceptConnection} 相同的连接上下文
     */
    default void onConnectionRelease(JdbcConnectionContext context) {
    }

    /**
     * SQL 执行前拦截。
     *
     * @param context 执行上下文（含 SQL 文本与命令类型推断；参数分组为空，裁决不感知参数值）
     * @return 裁决结果，拒绝时携带 i18n 资源 key 与参数
     */
    InterceptVerdict interceptExecution(JdbcExecuteContext context);

    /**
     * SQL 执行完成回调，在语句真正执行结束后触发（成功与失败均触发）。
     *
     * <p>与 {@link #interceptExecution} 成对出现：仅当执行前裁决放行且存在可识别的 SQL 文本时回调，
     * 命中排除规则的 SQL 不触发。适用于参数观测、耗时统计、SQL 日志与慢 SQL 告警等场景。</p>
     *
     * <p>回调异常由框架吞掉，不影响业务执行，也不改变语句返回结果。</p>
     *
     * @param context       与执行前裁决相同的上下文，额外携带 PreparedStatement 参数分组
     * @param elapsedMillis 本次语句执行耗时（毫秒）
     * @param error         语句执行抛出的异常，成功时为 null
     */
    default void onExecutionCompleted(JdbcExecuteContext context, long elapsedMillis, Throwable error) {
    }

    /**
     * 获取拦截器执行顺序，值越小越先执行。
     *
     * @return 执行顺序值，默认为最低优先级
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
