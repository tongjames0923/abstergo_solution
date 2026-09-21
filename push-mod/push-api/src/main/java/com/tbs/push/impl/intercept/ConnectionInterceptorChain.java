package com.tbs.push.impl.intercept;

import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.IConnectionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 连接拦截器链
 * <p>
 * 聚合容器中全部IConnectionInterceptor实现并按序调用，单拦截器异常仅记warn不中断， 保证业务拦截器的故障不传播到连接本身。容器无任何拦截器实现时退化为无操作， 与协议层"默认不集成安全验证"的定位一致。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ConnectionInterceptorChain implements IConnectionInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ConnectionInterceptorChain.class);

    /**
     * 拦截器实现提供者
     */
    private final ObjectProvider<IConnectionInterceptor> provider;

    /**
     * 构造拦截器链
     *
     * @param provider 拦截器实现提供者
     */
    public ConnectionInterceptorChain(ObjectProvider<IConnectionInterceptor> provider) {
        this.provider = provider;
    }

    /**
     * 连接建立回调（逐一调用全部拦截器）
     *
     * @param session 新建立的会话
     */
    @Override
    public void onConnect(IConnectionSession session) {
        for (IConnectionInterceptor interceptor : provider) {
            try {
                interceptor.onConnect(session);
            } catch (Throwable error) {
                log.warn("连接拦截器onConnect执行异常, interceptor:{}, sessionId:{}", interceptor.getClass().getName(),
                    session.getSessionId(), error);
            }
        }
    }

    /**
     * 连接断开回调（逐一调用全部拦截器）
     *
     * @param session 断开的会话
     * @param reason  关闭原因标识
     */
    @Override
    public void onDisconnect(IConnectionSession session, String reason) {
        for (IConnectionInterceptor interceptor : provider) {
            try {
                interceptor.onDisconnect(session, reason);
            } catch (Throwable error) {
                log.warn("连接拦截器onDisconnect执行异常, interceptor:{}, sessionId:{}",
                    interceptor.getClass().getName(), session.getSessionId(), error);
            }
        }
    }

}
