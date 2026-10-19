package com.tbs.sql.datasource.decoration;

import com.tbs.sql.datasource.model.JdbcExecuteContext;
import com.tbs.sql.datasource.model.SqlCommandKind;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Statement 动态代理调用处理器：在 execute 系方法调用前执行 SQL 级拦截裁决，
 * 并维护批量语句（addBatch / executeBatch）的 SQL 记录。
 *
 * <h3>拦截点</h3>
 * <ul>
 *   <li>{@code execute / executeQuery / executeUpdate / executeLargeUpdate}：直接裁决</li>
 *   <li>{@code executeBatch / executeLargeBatch}：按记录的批量 SQL 逐条裁决</li>
 *   <li>{@code addBatch(String)} / {@code addBatch()}：仅记录 SQL，不裁决</li>
 * </ul>
 *
 * <h3>说明</h3>
 * <p>普通 {@code createStatement().execute()}（无 SQL 入参）无法在执行前获知 SQL 文本，
 * 此时仅在有预编译 SQL 可裁决，否则透传执行。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class StatementInvocationHandler implements InvocationHandler {

    /**
     * 真实语句对象
     */
    private final Statement target;

    /**
     * 拦截器链
     */
    private final InterceptorChain chain;

    /**
     * 数据源标识
     */
    private final String dataSourceId;

    /**
     * 预编译时已知的 SQL 文本（PreparedStatement / CallableStatement），普通 Statement 为 null
     */
    private final String preparedSql;

    /**
     * 批量语句 SQL 记录
     */
    private final List<String> batchSqls = new ArrayList<>();

    /**
     * 构造语句代理处理器。
     *
     * @param target       真实语句对象
     * @param chain        拦截器链
     * @param dataSourceId 数据源标识
     * @param preparedSql  预编译时已知的 SQL 文本，无则传 null
     */
    public StatementInvocationHandler(Statement target, InterceptorChain chain,
                                      String dataSourceId, String preparedSql) {
        this.target = target;
        this.chain = chain;
        this.dataSourceId = dataSourceId;
        this.preparedSql = preparedSql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("equals".equals(methodName)) {
            return target.equals(args[0]);
        }
        if ("hashCode".equals(methodName)) {
            return target.hashCode();
        }
        if ("addBatch".equals(methodName)) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                batchSqls.add((String) args[0]);
            } else if (preparedSql != null) {
                batchSqls.add(preparedSql);
            }
            return method.invoke(target, args);
        }
        if ("clearBatch".equals(methodName)) {
            batchSqls.clear();
            return method.invoke(target, args);
        }
        if ("executeBatch".equals(methodName) || "executeLargeBatch".equals(methodName)) {
            checkBatchExecution();
            return method.invoke(target, args);
        }
        if (isExecuteMethod(methodName)) {
            String sql = extractSql(args);
            checkExecution(sql, isBatchContext());
            return method.invoke(target, args);
        }
        return method.invoke(target, args);
    }

    /**
     * 判断是否为 execute 系执行方法（含 Java 8 引入的 Large 系列）。
     *
     * @param methodName 方法名
     * @return true 表示执行方法
     */
    private boolean isExecuteMethod(String methodName) {
        return "execute".equals(methodName)
                || "executeQuery".equals(methodName)
                || "executeUpdate".equals(methodName)
                || "executeLargeUpdate".equals(methodName);
    }

    /**
     * 提取待执行的 SQL 文本：优先取方法入参中的 String，其次取预编译 SQL。
     *
     * @param args 方法入参
     * @return SQL 文本，无可用 SQL 时返回 null
     */
    private String extractSql(Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        return preparedSql;
    }

    /**
     * 判断当前执行是否处于批量上下文（executeBatch 经批量记录路径处理，
     * 普通 execute 方法在批量累积未清空时同样标记为批量）。
     *
     * @return true 表示批量上下文
     */
    private boolean isBatchContext() {
        return !batchSqls.isEmpty();
    }

    /**
     * 执行单条 SQL 裁决，SQL 文本不可知时透传。
     *
     * @param sql     待执行 SQL
     * @param isBatch 是否批量上下文
     */
    private void checkExecution(String sql, boolean isBatch) {
        if (sql == null) {
            return;
        }
        chain.checkExecution(new JdbcExecuteContext(sql, SqlCommandKind.of(sql),
                dataSourceId, Thread.currentThread().getName(), isBatch));
    }

    /**
     * 批量执行裁决：按记录的批量 SQL 逐条检查；无记录且有预编译 SQL 时按预编译 SQL 检查。
     */
    private void checkBatchExecution() {
        if (batchSqls.isEmpty()) {
            checkExecution(preparedSql, true);
            return;
        }
        for (String sql : batchSqls) {
            checkExecution(sql, true);
        }
    }
}
