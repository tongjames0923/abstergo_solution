package com.tbs.sql.interceptor;

import com.tbs.sql.builder.SqlBuildContext;
import com.tbs.sql.model.where.WhereCompose;
import org.springframework.core.Ordered;

/**
 * SQL WHERE条件组合拦截器接口，用于在SQL构建过程中拦截和修改WHERE条件。
 *
 * <p>该接口定义了SQL WHERE条件拦截的标准方法，允许在SQL构建过程中动态添加、
 * 修改或删除WHERE条件。支持多拦截器链式执行，按优先级顺序处理。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>拦截并修改WHERE条件 {@link #intercept(WhereCompose, SqlBuildContext)}</li>
 *   <li>控制拦截器执行顺序 {@link #getOrder()}</li>
 *   <li>支持多拦截器链式执行</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>添加数据权限控制条件</li>
 *   <li>添加租户隔离条件</li>
 *   <li>动态修改查询条件</li>
 *   <li>添加审计日志条件</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class TenantInterceptor implements SqlComposeInterceptor {
 *     @Override
 *     public WhereCompose intercept(WhereCompose compose, Class<?> entityClass) {
 *         // 添加租户条件
 *         compose.addWhere("tenant_id = ?", getCurrentTenantId());
 *         return compose;
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 0;  // 最高优先级
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>拦截器需要注册为Spring Bean</li>
 *   <li>通过 {@link #getOrder()} 方法控制执行顺序</li>
 *   <li>确保拦截逻辑不会破坏原有的查询条件</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see WhereCompose
 * @see com.tbs.sql.service.QueryEngine
 */
public interface SqlComposeInterceptor extends Ordered {
    /**
     * 拦截并修改WHERE条件组合。
     *
     * @param compose 原始的WHERE条件组合对象
     * @param context 实体类类型
     * @return 修改后的WHERE条件组合对象
     */
    WhereCompose intercept(WhereCompose compose, SqlBuildContext context);

    /**
     * 获取拦截器执行顺序，值越小优先级越高。
     *
     * @return 执行顺序值，默认为最低优先级
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
