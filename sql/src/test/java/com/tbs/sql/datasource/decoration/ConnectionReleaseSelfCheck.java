package com.tbs.sql.datasource.decoration;

import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcConnectionContext;
import com.tbs.sql.datasource.model.JdbcExecuteContext;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接归还回调框架的可运行自检（零测试依赖，用 {@code main} + 断言）。
 *
 * <p>验证 {@link TbsDataSourceDecorator} + {@link InterceptorChain} +
 * {@link ConnectionInvocationHandler} 的「借出占用 / 归还释放」配对语义：</p>
 * <ol>
 *   <li>getConnection 放行后资源计数 +1</li>
 *   <li>close() 归还后资源计数 -1</li>
 *   <li>重复 close() 只触发一次释放</li>
 *   <li>取真实连接抛异常时归还已占用资源，避免泄漏</li>
 * </ol>
 *
 * <p>运行：{@code java -ea ...ConnectionReleaseSelfCheck}（打印 PASSED 即通过）。</p>
 */
public class ConnectionReleaseSelfCheck {

    public static void main(String[] args) throws Exception {
        AtomicInteger open = new AtomicInteger(0);

        JdbcExecutionInterceptor tracking = new JdbcExecutionInterceptor() {
            @Override
            public InterceptVerdict interceptConnection(JdbcConnectionContext context) {
                open.incrementAndGet();
                return InterceptVerdict.allow();
            }

            @Override
            public void onConnectionRelease(JdbcConnectionContext context) {
                open.decrementAndGet();
            }

            @Override
            public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
                return InterceptVerdict.allow();
            }
        };

        InterceptorChain chain = new InterceptorChain(() -> List.of(tracking), new SqlExcludeRule(null));
        DataSource decorator = new TbsDataSourceDecorator(dataSourceReturning(fakeConnection()), chain, "ds");

        Connection c1 = decorator.getConnection();
        assertEquals(1, open.get(), "借出后应 +1");
        c1.close();
        assertEquals(0, open.get(), "归还后应 -1");
        c1.close();
        assertEquals(0, open.get(), "重复 close 不应再减");

        Connection c2 = decorator.getConnection();
        assertEquals(1, open.get(), "第二次借出应 +1");
        c2.close();
        assertEquals(0, open.get(), "第二次归还应 -1");

        DataSource failing = new TbsDataSourceDecorator(dataSourceThrowing(), chain, "ds");
        try {
            failing.getConnection();
            throw new AssertionError("取连接失败时应抛出 SQLException");
        } catch (SQLException expected) {
            assertEquals(0, open.get(), "取连接失败时应归还已占用许可");
        }

        System.out.println("ConnectionReleaseSelfCheck PASSED");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + "：expected=" + expected + ", actual=" + actual);
        }
    }

    private static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, methodArgs) -> null);
    }

    private static DataSource dataSourceReturning(Connection connection) {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class}, (proxy, method, methodArgs) -> {
                    if (method.getName().equals("getConnection")) {
                        return connection;
                    }
                    return null;
                });
    }

    private static DataSource dataSourceThrowing() {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class}, (proxy, method, methodArgs) -> {
                    if (method.getName().equals("getConnection")) {
                        throw new SQLException("模拟取连接失败");
                    }
                    return null;
                });
    }
}
