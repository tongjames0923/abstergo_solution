package com.tbs.sql.datasource.decoration;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcConnectionContext;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 拦截器链：持有排序后的 {@link JdbcExecutionInterceptor} 列表，
 * 负责连接级与执行级裁决，并在拒绝时统一经 AssertUtil 抛出 i18n 异常。
 *
 * <h3>执行流程</h3>
 * <ol>
 *   <li>执行级裁决前先走 {@link SqlExcludeRule} 短路（命中即透传，保护性能与框架自身语句）</li>
 *   <li>按 order 升序遍历拦截器，首个拒绝裁决立即抛出异常（短路语义）</li>
 *   <li>拦截器列表延迟解析（首次使用时从 Supplier 获取），避免装配期依赖用户 Bean 的实例化顺序</li>
 * </ol>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class InterceptorChain {

    /**
     * 拦截器列表供应器，延迟解析
     */
    private final Supplier<List<JdbcExecutionInterceptor>> interceptorSupplier;

    /**
     * SQL 排除规则
     */
    private final SqlExcludeRule excludeRule;

    /**
     * 解析并排序后的拦截器列表，懒加载
     */
    private volatile List<JdbcExecutionInterceptor> interceptors;

    /**
     * 构造拦截器链。
     *
     * @param interceptorSupplier 拦截器列表供应器，延迟解析以避开装配顺序问题
     * @param excludeRule         SQL 排除规则，不允许为 null
     */
    public InterceptorChain(Supplier<List<JdbcExecutionInterceptor>> interceptorSupplier,
                            SqlExcludeRule excludeRule) {
        this.interceptorSupplier = interceptorSupplier;
        this.excludeRule = excludeRule;
    }

    /**
     * 连接获取阶段裁决。
     *
     * @param context 连接获取上下文
     */
    public void checkConnection(JdbcConnectionContext context) {
        for (JdbcExecutionInterceptor interceptor : resolveInterceptors()) {
            InterceptVerdict verdict = interceptor.interceptConnection(context);
            if (verdict.isDenied()) {
                throwDenied(verdict);
            }
        }
    }

    /**
     * SQL 执行阶段裁决，排除规则命中的 SQL 直接透传。
     *
     * @param context 执行上下文
     */
    public void checkExecution(JdbcExecuteContext context) {
        if (excludeRule.excludes(context.getSql())) {
            return;
        }
        for (JdbcExecutionInterceptor interceptor : resolveInterceptors()) {
            InterceptVerdict verdict = interceptor.interceptExecution(context);
            if (verdict.isDenied()) {
                throwDenied(verdict);
            }
        }
    }

    /**
     * 判断链是否为空（无拦截器且无排除规则），空链下装饰无实际意义。
     *
     * @return true 表示空链
     */
    public boolean isEmpty() {
        return resolveInterceptors().isEmpty() && !excludeRule.hasRules();
    }

    /**
     * 延迟解析并排序拦截器列表（order 升序）。
     *
     * @return 排序后的拦截器列表，无拦截器时返回空列表
     */
    private List<JdbcExecutionInterceptor> resolveInterceptors() {
        if (interceptors == null) {
            synchronized (this) {
                if (interceptors == null) {
                    List<JdbcExecutionInterceptor> resolved =
                            new ArrayList<>(interceptorSupplier.get());
                    resolved.removeIf(Objects::isNull);
                    resolved.sort(Comparator.comparingInt(Ordered::getOrder));
                    interceptors = List.copyOf(resolved);
                }
            }
        }
        return interceptors;
    }

    /**
     * 拒绝裁决统一出口：经 AssertUtil 链式断言抛出 i18n 化异常（铁律 3/4）。
     *
     * @param verdict 拒绝裁决
     */
    private void throwDenied(InterceptVerdict verdict) {
        String i18nKey = StrUtil.isEmpty(verdict.getI18nKey())
                ? InterceptVerdict.DEFAULT_DENY_KEY : verdict.getI18nKey();
        Object[] params = verdict.getParams();
        AssertUtil.isTrue(true).i18n(i18nKey, params == null ? new Object[0] : params).throwIt();
    }
}
