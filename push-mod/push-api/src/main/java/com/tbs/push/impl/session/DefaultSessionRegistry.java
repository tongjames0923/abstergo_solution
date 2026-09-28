package com.tbs.push.impl.session;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认会话注册表
 * <p>
 * 基于并发Map的全部在线会话登记实现，注册/注销/查询均为O(1)。 注销不存在的会话标识静默跳过，保证与传输层关闭回调的竞态安全。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultSessionRegistry implements ISessionRegistry {

    /**
     * 在线会话表
     */
    private final Map<String, IConnectionSession> sessions = new ConcurrentHashMap<>();

    /**
     * 注册会话
     *
     * @param session 会话
     */
    @Override
    public void register(IConnectionSession session) {
        if (session == null) {
            return;
        }
        sessions.put(session.getSessionId(), session);
    }

    /**
     * 注销会话（不存在的id静默跳过）
     *
     * @param sessionId 会话标识
     */
    @Override
    public void unregister(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessions.remove(sessionId);
    }

    /**
     * 按标识查询会话
     *
     * @param sessionId 会话标识
     * @return 会话，不存在时返回null
     */
    @Override
    public IConnectionSession get(String sessionId) {
        return sessionId == null ? null : sessions.get(sessionId);
    }

    /**
     * 查询全部在线会话
     *
     * @return 在线会话集合，不为null
     */
    @Override
    public Collection<IConnectionSession> all() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * 统计在线会话数量
     *
     * @return 在线会话数量
     */
    @Override
    public int count() {
        return sessions.size();
    }

}
