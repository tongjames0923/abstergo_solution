package com.tbs.push.api.protocol;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.protocol.HeartbeatAdvice;

/**
 * 心跳推荐值 SPI
 * <p>
 * HELLO 握手帧下发给客户端的推荐心跳（周期与最迟期限）的生成决策点：协议层只把会话这一传输
 * 无关的事实交给实现，"推荐什么节奏"完全归属实现 Bean。默认实现的最迟期限直接取服务端心跳
 * 空闲回收点，推荐周期按 {@link IServerBusyIndicator} 报告的繁忙度自适应（越忙越长以抑制上行风暴）。
 * 部署方可注册自定义实现换用其它策略（如已知代理读超时的固定周期），返回 {@code null}
 * 表示不下发推荐，客户端沿用自身默认心跳（保持无 SPI 部署的历史行为）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IHeartbeatRecommender {

    /**
     * 为指定会话生成推荐心跳值
     *
     * @param session 正在握手的会话
     * @return 推荐心跳值；null 表示不下发
     */
    HeartbeatAdvice recommend(IConnectionSession session);

}
