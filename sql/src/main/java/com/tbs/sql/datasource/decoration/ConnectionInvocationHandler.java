package com.tbs.sql.datasource.decoration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Connection 动态代理调用处理器：拦截语句创建方法，将返回的
 * {@link Statement} / {@link java.sql.PreparedStatement} /
 * {@link java.sql.CallableStatement} 包装为语句级代理；
 * 其余方法（含 {@code close}、{@code setAutoCommit}、{@code unwrap} 等）
 * 一律原样透传，保证连接池与事务语义不受影响。
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ConnectionInvocationHandler implements InvocationHandler {

    /**
     * 真实连接对象
     */
    private final Connection target;

    /**
     * 拦截器链
     */
    private final InterceptorChain chain;

    /**
     * 数据源标识
     */
    private final String dataSourceId;

    /**
     * 构造连接代理处理器。
     *
     * @param target       真实连接
     * @param chain        拦截器链
     * @param dataSourceId 数据源标识
     */
    public ConnectionInvocationHandler(Connection target, InterceptorChain chain, String dataSourceId) {
        this.target = target;
        this.chain = chain;
        this.dataSourceId = dataSourceId;
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
        if (isStatementFactory(methodName) && args != null && args.length > 0 && args[0] instanceof String) {
            Object statement = method.invoke(target, args);
            if (statement instanceof Statement) {
                return wrapStatement((Statement) statement, (String) args[0]);
            }
            return statement;
        }
        if ("createStatement".equals(methodName)) {
            Object statement = method.invoke(target, args);
            if (statement instanceof Statement) {
                return wrapStatement((Statement) statement, null);
            }
            return statement;
        }
        return method.invoke(target, args);
    }

    /**
     * 判断是否为携带 SQL 文本的语句工厂方法。
     *
     * @param methodName 方法名
     * @return true 表示 prepareStatement / prepareCall
     */
    private boolean isStatementFactory(String methodName) {
        return "prepareStatement".equals(methodName) || "prepareCall".equals(methodName);
    }

    /**
     * 包装语句对象为动态代理。
     *
     * @param statement    真实语句对象
     * @param preparedSql 预编译时已知的 SQL 文本，普通 Statement 传 null
     * @return 语句代理
     */
    private Object wrapStatement(Statement statement, String preparedSql) {
        Class<?>[] interfaces = statement.getClass().getInterfaces();
        if (interfaces.length == 0) {
            interfaces = new Class<?>[]{Statement.class};
        }
        return Proxy.newProxyInstance(statement.getClass().getClassLoader(), interfaces,
                new StatementInvocationHandler(statement, chain, dataSourceId, preparedSql));
    }
}
