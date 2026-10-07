package com.tbs.app.service.impl.push;

import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushSessionAttrConstants;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 会话身份守卫拦截器（建连时点一致性与频道自动订阅）
 * <p>
 * 建连回调时点执行两件事：
 * <ul>
 *     <li>凭证绑定身份与当前请求登录主体一致性校验：ticket 在 TTL 窗口内被旁路
 *     获取时，他人携自己的令牌消费建连将在此被关闭（close 幂等，适配器后续的
 *     markConnected/send 对已关闭会话均为无害空转）；非HTTP线程或无绑定身份时跳过；</li>
 *     <li>按会话绑定 userId 自动订阅本人信令频道：路由不依赖客户端自报，
 *     与客户端既有订阅幂等并存（注册表重复订阅无害）。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class PushSessionIdentityInterceptor implements IConnectionInterceptor {

    /**
     * 订阅注册表延迟获取（推送未装配时跳过自动订阅）
     */
    @Autowired
    private ObjectProvider<ITopicRegistry> topicRegistryProvider;

    /**
     * 连接建立回调：身份一致性守卫 + 本人信令频道自动订阅
     *
     * @param session 新建立的会话
     */
    @Override
    public void onConnect(IConnectionSession session) {
        String boundUsername = session.getAttribute(PushSessionAttrConstants.USERNAME);
        String currentUsername = resolveCurrentUsername();
        if (boundUsername != null && currentUsername != null && !boundUsername.equals(currentUsername)) {
            log.warn("建连主体与凭证绑定身份不一致, sessionId:{}, bound:{}, current:{}",
                session.getSessionId(), boundUsername, currentUsername);
            session.close(PushFrameConstants.CLOSE_REASON_IDENTITY_MISMATCH);
            return;
        }
        String userId = session.getAttribute(PushSessionAttrConstants.USER_ID);
        ITopicRegistry registry = topicRegistryProvider.getIfAvailable();
        if (userId != null && registry != null) {
            registry.subscribe(session.getSessionId(),
                TopicPrefixNotifyReceiverResolver.RESERVED_SIGNAL_PREFIX + userId);
        }
    }

    /**
     * 连接断开回调：订阅清理由协议层统一负责，本拦截器无断开动作
     *
     * @param session 断开的会话
     * @param reason  关闭原因标识
     */
    @Override
    public void onDisconnect(IConnectionSession session, String reason) {
        // 无断开动作
    }

    /**
     * 解析当前请求登录主体用户名（非HTTP认证上下文线程返回null）
     *
     * @return 当前登录用户名，取不到时为null
     */
    private String resolveCurrentUsername() {
        try {
            return AuthUtil.getInstance().getCurrentUser().getUsername();
        } catch (Exception error) {
            return null;
        }
    }

}
