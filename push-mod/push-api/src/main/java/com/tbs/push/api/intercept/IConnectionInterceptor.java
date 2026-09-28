package com.tbs.push.api.intercept;

import com.tbs.push.api.session.IConnectionSession;

/**
 * 连接拦截器扩展点
 * <p>
 * 协议层默认不集成安全验证，仅预留本扩展点供业务方按需介入连接生命周期
 * （如接入鉴权、来源审计、限流等）：
 * <ul>
 *     <li>onConnect — 传输建立、会话登记之后触发；可在此做握手前置校验并拒绝连接</li>
 *     <li>onDisconnect — 会话关闭、订阅清理之后触发</li>
 * </ul>
 * 可注册多个实现Bean，协议层经拦截器链逐一调用，单个拦截器异常不影响连接本身。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IConnectionInterceptor {

    /**
     * 连接建立回调
     *
     * @param session 新建立的会话
     */
    void onConnect(IConnectionSession session);

    /**
     * 连接断开回调
     *
     * @param session 断开的会话
     * @param reason  关闭原因标识
     */
    void onDisconnect(IConnectionSession session, String reason);

}
