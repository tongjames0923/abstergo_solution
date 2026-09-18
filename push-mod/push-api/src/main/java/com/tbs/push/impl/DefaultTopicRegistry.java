package com.tbs.push.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认订阅注册表
 * <p>
 * 基于并发Map的"topic → 会话标识集合"倒排索引实现，按topic解析投递目标为O(1)。
 * 订阅时校验topic合法性（非空且不超长），会话注销时整体清理其订阅关系。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultTopicRegistry implements ITopicRegistry {

    /**
     * topic倒排索引
     */
    private final Map<String, Set<String>> topicIndex = new ConcurrentHashMap<>();

    /**
     * 建立订阅关系（topic合法性校验，重复订阅幂等）
     *
     * @param sessionId 会话标识
     * @param topic     主题
     */
    @Override
    public void subscribe(String sessionId, String topic) {
        validateTopic(topic);
        topicIndex.computeIfAbsent(topic, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    /**
     * 取消订阅关系
     *
     * @param sessionId 会话标识
     * @param topic     主题
     */
    @Override
    public void unsubscribe(String sessionId, String topic) {
        Set<String> sessionIds = topicIndex.get(topic);
        if (sessionIds != null) {
            sessionIds.remove(sessionId);
        }
    }

    /**
     * 查询订阅指定主题的全部会话标识
     *
     * @param topic 主题
     * @return 会话标识集合，不为null
     */
    @Override
    public Set<String> findSessionIdsByTopic(String topic) {
        Set<String> sessionIds = topicIndex.get(topic);
        return sessionIds == null ? Collections.emptySet() : Collections.unmodifiableSet(sessionIds);
    }

    /**
     * 清理会话的全部订阅关系
     *
     * @param sessionId 会话标识
     */
    @Override
    public void clearSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        topicIndex.values().forEach(sessionIds -> sessionIds.remove(sessionId));
    }

    /**
     * 校验topic合法性
     *
     * @param topic 主题
     */
    private void validateTopic(String topic) {
        if (StrUtil.isEmpty(topic) || topic.length() > PushFrameConstants.TOPIC_MAX_LENGTH) {
            throw PushProtocolException.protocolI18n(PushI18nConstant.TOPIC_INVALID, topic);
        }
    }

}
