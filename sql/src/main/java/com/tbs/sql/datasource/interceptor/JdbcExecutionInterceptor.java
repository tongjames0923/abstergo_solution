package com.tbs.sql.datasource.interceptor;

import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcConnectionContext;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.springframework.core.Ordered;

/**
 * JDBC 执行拦截器 SPI，注册为 Spring Bean 后由 DataSource 装饰框架按
 * {@link #getOrder()} 升序链式调用。
 *
 * <h3>双钩子设计</h3>
 * <ul>
 *   <li>{@link #interceptConnection}：连接获取阶段，仅作拦截点，不承担数据源路由职责</li>
 *   <li>{@link #interceptExecution}：SQL 执行前，放行或拒绝</li>
 * </ul>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>拦截器只能裁决（放行/拒绝），不能改写 SQL，也拿不到参数值</li>
 *   <li>SQL 日志、耗时观测由 p6spy 与 Executor 层日志承担，本 SPI 不强制观测回调</li>
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
     * SQL 执行前拦截。
     *
     * @param context 执行上下文（含 SQL 文本与命令类型推断，不含参数值）
     * @return 裁决结果，拒绝时携带 i18n 资源 key 与参数
     */
    InterceptVerdict interceptExecution(JdbcExecuteContext context);

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
