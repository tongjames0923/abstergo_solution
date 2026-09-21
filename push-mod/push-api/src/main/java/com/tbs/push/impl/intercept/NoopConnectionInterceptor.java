package com.tbs.push.impl.intercept;

import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.IConnectionSession;

/**
 * 空实现连接拦截器
 * <p>
 * 协议层默认不集成安全验证，本实现作为拦截器链的兜底：不做任何校验， 全部回调为无操作。业务需要接入鉴权等逻辑时注册自定义IConnectionInterceptor即可。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class NoopConnectionInterceptor implements IConnectionInterceptor {

    /**
     * 连接建立回调（空实现）
     *
     * @param session 新建立的会话
     */
    @Override
    public void onConnect(IConnectionSession session) {
        // 无操作：默认不做任何连接校验
    }

    /**
     * 连接断开回调（空实现）
     *
     * @param session 断开的会话
     * @param reason  关闭原因标识
     */
    @Override
    public void onDisconnect(IConnectionSession session, String reason) {
        // 无操作：默认不做任何断开处理
    }

}
