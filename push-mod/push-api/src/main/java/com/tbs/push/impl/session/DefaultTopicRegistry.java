package com.tbs.push.impl.session;

import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.exception.PushProtocolException;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.util.TopicMatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认订阅注册表
 * <p>
 * 基于并发Map的"topic → 会话标识集合"倒排索引实现，按topic解析投递目标为O(1)。 订阅时校验topic合法性（非空且不超长），会话注销时整体清理其订阅关系。
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
     * 含通配符的订阅topic集合
     * <p>
     * 用于发布侧为具体topic时仍能命中"订阅侧通配符"（如订阅{@code global.broadcast.*}、 发布{@code global.broadcast.user}），避免无通配符查询退化为全量遍历。
     * </p>
     */
    private final Set<String> wildcardTopics = ConcurrentHashMap.newKeySet();

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
        if (TopicMatcher.containsWildcard(topic)) {
            wildcardTopics.add(topic);
        }
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
            if (sessionIds.isEmpty() && TopicMatcher.containsWildcard(topic)) {
                wildcardTopics.remove(topic);
            }
        }
    }

    /**
     * 查询订阅指定主题的全部会话标识
     * <p>
     * 通配符感知匹配：topic不含通配符时优先走倒排索引精确查找（O(1)）， 并叠加命中"订阅侧通配符"的会话；topic含通配符（{@code *}单级 / {@code #}多级）时
     * 遍历全部已存topic，借助{@link TopicMatcher}双向匹配（查询可为模式、已存key也可为模式） 合并去重会话集合，避免同一会话命中多个匹配topic时重复投递。
     * </p>
     *
     * @param topic 主题（可含通配符）
     * @return 会话标识集合，不为null
     */
    @Override
    public Set<String> findSessionIdsByTopic(String topic) {
        Set<String> matched = new HashSet<>();
        if (!TopicMatcher.containsWildcard(topic)) {
            Set<String> exact = topicIndex.get(topic);
            if (exact != null) {
                matched.addAll(exact);
            }
            if (!wildcardTopics.isEmpty()) {
                for (String wildcard : wildcardTopics) {
                    if (TopicMatcher.matches(wildcard, topic)) {
                        Set<String> sessionIds = topicIndex.get(wildcard);
                        if (sessionIds != null) {
                            matched.addAll(sessionIds);
                        }
                    }
                }
            }
            return Collections.unmodifiableSet(matched);
        }
        for (Map.Entry<String, Set<String>> entry : topicIndex.entrySet()) {
            String stored = entry.getKey();
            if (stored.equals(topic)) {
                matched.addAll(entry.getValue());
            } else if (TopicMatcher.matches(topic, stored)) {
                matched.addAll(entry.getValue());
            } else if (TopicMatcher.matches(stored, topic)) {
                matched.addAll(entry.getValue());
            }
        }
        return Collections.unmodifiableSet(matched);
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
        wildcardTopics.removeIf(key -> {
            Set<String> sessionIds = topicIndex.get(key);
            return sessionIds == null || sessionIds.isEmpty();
        });
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
