package com.tbs.push.impl.protocol;

import com.tbs.push.api.protocol.IServerBusyIndicator;
import com.tbs.push.api.session.ISessionRegistry;

/**
 * 默认繁忙度指示器：以本节点在线会话数归一化
 * <p>
 * 繁忙度 = {@code min(1.0, 在线会话数 / busyCapacity)}。在线会话数直接反映本推送节点的上行
 * fan-in 压力，是最贴合"心跳风暴"这一威胁的信号；busyCapacity（视为 100% 饱和的会话量）
 * 取自配置，属部署方知识。会话数无法取得或容量非正时返回 0（视为空闲，走最短保温周期）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SessionCountBusyIndicator implements IServerBusyIndicator {

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 饱和会话数（在线会话达到该量视为 100% 繁忙）
     */
    private final int busyCapacity;

    /**
     * 构造默认繁忙度指示器
     *
     * @param sessionRegistry 会话注册表，不能为空
     * @param busyCapacity    饱和会话数，非正数表示禁用自适应（恒返回空闲）
     */
    public SessionCountBusyIndicator(ISessionRegistry sessionRegistry, int busyCapacity) {
        this.sessionRegistry = sessionRegistry;
        this.busyCapacity = busyCapacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double busyScore() {
        if (busyCapacity <= 0) {
            return 0D;
        }
        return Math.min(1.0D, (double) sessionRegistry.count() / busyCapacity);
    }

}
