package com.tbs.push.api.session;

import java.util.Set;

/**
 * 订阅注册表契约
 * <p>
 * 维护"会话 ↔ topic"订阅关系的单一数据源，投递协调器据此解析topic匹配的投递目标。
 * 实现必须线程安全，且在会话关闭时由协议层调用{@link #clearSession(String)}清理，
 * 防止向死连接投递。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ITopicRegistry {

    /**
     * 建立订阅关系
     * <p>
     * topic不合法（空/超长）时抛出协议异常；重复订阅幂等。
     * </p>
     *
     * @param sessionId 会话标识
     * @param topic     主题
     */
    void subscribe(String sessionId, String topic);

    /**
     * 取消订阅关系
     *
     * @param sessionId 会话标识
     * @param topic     主题
     */
    void unsubscribe(String sessionId, String topic);

    /**
     * 查询订阅指定主题的全部会话标识
     * <p>
     * 支持通配符模糊匹配：topic可含{@code *}（匹配恰好一个分段）与
     * {@code #}（匹配零个或多个分段，约定尾段）。含通配符时匹配所有命中的已订阅会话；
     * 不含通配符时精确查找。订阅侧同样允许写入含通配符的topic（对称支持）。
     * </p>
     *
     * @param topic 主题（可含通配符）
     * @return 会话标识集合，不为null
     */
    Set<String> findSessionIdsByTopic(String topic);

    /**
     * 清理会话的全部订阅关系
     * <p>
     * 会话注销时必须调用，防止订阅关系泄漏。
     * </p>
     *
     * @param sessionId 会话标识
     */
    void clearSession(String sessionId);

}
