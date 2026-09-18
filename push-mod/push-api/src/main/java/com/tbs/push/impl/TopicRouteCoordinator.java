package com.tbs.push.impl;

import com.tbs.push.api.coordinator.IRouteCoordinator;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.model.PushMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * topic直配投递协调器（默认实现）
 * <p>
 * "投递给谁"的默认决策实现：按消息topic在订阅注册表中匹配会话标识，
 * 再从会话注册表解析出会话实例。业务需要按元数据/会话属性做定向投递时，
 * 注册自定义IRouteCoordinator Bean覆盖本实现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TopicRouteCoordinator implements IRouteCoordinator {

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 订阅注册表
     */
    private final ITopicRegistry topicRegistry;

    /**
     * 构造topic直配协调器
     *
     * @param sessionRegistry 会话注册表，不能为空
     * @param topicRegistry   订阅注册表，不能为空
     */
    public TopicRouteCoordinator(ISessionRegistry sessionRegistry, ITopicRegistry topicRegistry) {
        this.sessionRegistry = sessionRegistry;
        this.topicRegistry = topicRegistry;
    }

    /**
     * 按消息topic解析投递目标会话集合
     *
     * @param message 待投递的业务消息
     * @return 投递目标会话列表，不为null
     */
    @Override
    public List<IConnectionSession> resolveTargets(PushMessage message) {
        Set<String> sessionIds = topicRegistry.findSessionIdsByTopic(message.getTopic());
        List<IConnectionSession> targets = new ArrayList<>(sessionIds.size());
        for (String sessionId : sessionIds) {
            IConnectionSession session = sessionRegistry.get(sessionId);
            if (session != null) {
                targets.add(session);
            }
        }
        return targets;
    }

}
