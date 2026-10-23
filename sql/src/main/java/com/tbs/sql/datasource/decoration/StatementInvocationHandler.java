package com.tbs.sql.datasource.decoration;

import com.tbs.sql.datasource.model.JdbcExecuteContext;
import com.tbs.sql.datasource.model.SqlCommandKind;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Statement 动态代理调用处理器：在 execute 系方法调用前执行 SQL 级拦截裁决，
 * 并在执行结束后回调观测钩子（SQL、耗时、异常），同时维护批量语句
 * （addBatch / executeBatch）的 SQL 记录。
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
            return invokeTarget(method, args);
        }
        if ("clearBatch".equals(methodName)) {
            batchSqls.clear();
            return invokeTarget(method, args);
        }
        if ("executeBatch".equals(methodName) || "executeLargeBatch".equals(methodName)) {
            checkBatchExecution();
            Object result = executeWithTiming(method, args, batchSql(), true);
            // JDBC 规范：executeBatch 成功后批次被清空；异常时保留影子记录，与实际批次状态保持一致
            batchSqls.clear();
            return result;
        }
        if (isExecuteMethod(methodName)) {
            String sql = extractSql(args);
            boolean batch = isBatchContext();
            checkExecution(sql, batch);
            return executeWithTiming(method, args, sql, batch);
        }
        return invokeTarget(method, args);
    }

    /**
     * 执行语句并记录耗时，执行结束后回调观测钩子。
     *
     * @param method 被调用的语句方法
     * @param args   方法入参
     * @param sql    本次执行的 SQL 文本，未知时为 null
     * @param batch  是否批量上下文
     * @return 方法返回值
     * @throws Throwable 语句执行抛出的真实异常
     */
    private Object executeWithTiming(Method method, Object[] args, String sql, boolean batch) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            Object result = invokeTarget(method, args);
            completeExecution(sql, batch, startNanos, null);
            return result;
        } catch (Throwable error) {
            completeExecution(sql, batch, startNanos, error);
            throw error;
        }
    }

    /**
     * 反射调用真实语句方法，并解开 {@link InvocationTargetException} 包装，
     * 保持 JDBC 原生异常类型不被动态代理再次包裹。
     *
     * @param method 被调用的语句方法
     * @param args   方法入参
     * @return 方法返回值
     * @throws Throwable 真实异常
     */
    private Object invokeTarget(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 回调执行完成钩子，SQL 未知时跳过。
     *
     * @param sql        本次执行的 SQL 文本
     * @param batch      是否批量上下文
     * @param startNanos 执行起始时间（纳秒）
     * @param error      执行异常，成功时为 null
     */
    private void completeExecution(String sql, boolean batch, long startNanos, Throwable error) {
        if (sql == null) {
            return;
        }
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        chain.completeExecution(new JdbcExecuteContext(sql, SqlCommandKind.of(sql),
                dataSourceId, Thread.currentThread().getName(), batch), elapsedMillis, error);
    }

    /**
     * 构造批量执行的 SQL 描述：优先使用 addBatch 记录的语句，无记录时回退预编译 SQL。
     *
     * @return 批量 SQL 描述，无可用 SQL 时返回 null
     */
    private String batchSql() {
        if (batchSqls.isEmpty()) {
            return preparedSql;
        }
        return String.join("; ", batchSqls);
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
