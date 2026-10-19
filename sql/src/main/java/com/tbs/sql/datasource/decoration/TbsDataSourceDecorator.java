package com.tbs.sql.datasource.decoration;

import com.tbs.sql.datasource.model.JdbcConnectionContext;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource 装饰器：对容器内的目标数据源做透明包装，
 * 在 {@code getConnection} 系方法入口执行连接级拦截，并返回携带
 * 语句级拦截能力的 {@link Connection} 动态代理。
 *
 * <h3>兼容性红线</h3>
 * <ul>
 *   <li>{@code unwrap} / {@code isWrapperFor} / {@code getParentLogger} 等元数据方法严格透传，
 *       保证 Hikari 连接池健康检查与 Spring 事务同步器行为不被破坏</li>
 *   <li>{@code equals} / {@code hashCode} 委托目标对象，避免池化连接的身份判定异常</li>
 *   <li>装饰为幂等包装：重复包装由 {@link DataSourceDecorationPostProcessor} 防御</li>
 * </ul>
 *
 * <h3>能力边界</h3>
 * <p>仅做拦截裁决，不改写 SQL、不获取参数值、不做多数据源路由。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TbsDataSourceDecorator implements DataSource {

    /**
     * 被装饰的目标数据源
     */
    private final DataSource target;

    /**
     * 拦截器链
     */
    private final InterceptorChain chain;

    /**
     * 数据源标识（目标 DataSource 的 Bean 名称）
     */
    private final String dataSourceId;

    /**
     * 构造数据源装饰器。
     *
     * @param target       被装饰的目标数据源
     * @param chain        拦截器链
     * @param dataSourceId 数据源标识，透传给拦截上下文
     */
    public TbsDataSourceDecorator(DataSource target, InterceptorChain chain, String dataSourceId) {
        this.target = target;
        this.chain = chain;
        this.dataSourceId = dataSourceId;
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkConnectionAllowed(null);
        return wrapConnection(target.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        checkConnectionAllowed(username);
        return wrapConnection(target.getConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return target.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        target.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        target.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return target.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return target.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return target.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return target.isWrapperFor(iface);
    }

    @Override
    public boolean equals(Object obj) {
        return target.equals(obj);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return "TbsDataSourceDecorator[" + target + "]";
    }

    /**
     * 连接级裁决，拒绝时经拦截器链抛出 i18n 异常。
     *
     * @param userName 连接用户名，无参获取时为 null
     */
    private void checkConnectionAllowed(String userName) {
        chain.checkConnection(new JdbcConnectionContext(dataSourceId, userName,
                Thread.currentThread().getName(), System.currentTimeMillis()));
    }

    /**
     * 将真实连接包装为携带语句级拦截的动态代理。
     *
     * @param connection 真实连接
     * @return 代理连接
     */
    private Connection wrapConnection(Connection connection) {
        Class<?>[] interfaces = connection.getClass().getInterfaces();
        if (interfaces.length == 0) {
            interfaces = new Class<?>[]{Connection.class};
        }
        return (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(),
                interfaces, new ConnectionInvocationHandler(connection, chain, dataSourceId));
    }
}
