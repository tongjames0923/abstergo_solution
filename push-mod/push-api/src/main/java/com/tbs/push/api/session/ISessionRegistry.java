package com.tbs.push.api.session;

import java.util.Collection;

/**
 * 会话注册表契约
 * <p>
 * 全部在线会话的单点登记与查询入口，是连接状态同步的权威数据源：
 * 传输适配层在连接建立/断开时调用register/unregister，注册表只做登记，
 * 状态机转换由会话实现与协议层维护。
 * 实现必须线程安全。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISessionRegistry {

    /**
     * 注册会话
     *
     * @param session 会话
     */
    void register(IConnectionSession session);

    /**
     * 注销会话
     * <p>
     * 注销动作的原子唯一性（首次移除成功）是会话清理链的幂等门禁：
     * 主动关闭与物理通道事件回传双入口时，仅成功注销的一方执行后续清理。
     * </p>
     *
     * @param sessionId 会话标识，空或不存在的id静默跳过
     * @return true表示本次调用首次移除登记（清理链唯一执行者），false表示登记项已不存在（重复清理跳过）
     */
    boolean unregister(String sessionId);

    /**
     * 按标识查询会话
     *
     * @param sessionId 会话标识
     * @return 会话，不存在时返回null
     */
    IConnectionSession get(String sessionId);

    /**
     * 查询全部在线会话
     *
     * @return 在线会话集合，不为null
     */
    Collection<IConnectionSession> all();

    /**
     * 统计在线会话数量
     *
     * @return 在线会话数量
     */
    int count();

}
