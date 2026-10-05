package com.tbs.push.api.transport;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.frame.PushFrame;

/**
 * 传输适配器契约（长连接全链路能力门面）
 * <p>
 * 每种底层传输（WebSocket/SSE等）对应一个适配器，定位是覆盖
 * 「建立连接 → 上下行传输 → 主动/被动关闭」的全链路能力入口：
 * <ul>
 *     <li>establish — 消费凭证并按统一编排建立会话（注册→拦截器链→置可用→ACK）</li>
 *     <li>acceptUpstream/acceptUpstreamText — 上行契约唯一受理口；事件驱动型传输
 *     （WebSocket容器回调）也默认汇入本口，保证跨传输上行语义一致</li>
 *     <li>closeSession — 服务端主动关闭（token过期、心跳超时、管理踢线等），
 *     物理拆除与清理链一次调用闭环，不依赖任何事件回传</li>
 *     <li>onChannelClosed — 物理通道关闭的事件回传入口，与主动关闭共享幂等门禁</li>
 *     <li>stop — 优雅关停，按注册表全扫描逐个主动关闭</li>
 * </ul>
 * 清理链幂等门禁为注册表注销的原子唯一性（{@code ISessionRegistry.unregister}
 * 首次移除成功方），主动关闭与事件回传双入口竞态时仅一方执行清理。
 * 帧语义与协议处理全部委托协议层，适配器不感知帧内容；
 * 适配器之间互不依赖，经 tbs.push.transport 属性互斥选择装配，同一时刻仅一个生效。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ITransportAdapter {

    /**
     * 建立统一会话（统一连接建立流程）
     * <p>
     * 原子消费一次性凭证（缺失/无效/过期抛协议异常），以预分配sessionId包装
     * 物理通道为统一会话并执行建立编排。
     * </p>
     *
     * @param ticket          一次性连接凭证
     * @param physicalChannel 传输物理通道对象（SSE：SseEmitter，可为null由实现自建；
     *                        WebSocket：容器WebSocketSession）
     * @return 建立完成的统一会话（已登记注册表；被拦截器拒绝时状态为CLOSED）
     */
    IConnectionSession establish(String ticket, Object physicalChannel);

    /**
     * 上行受理：结构化协议帧（跨传输统一契约口）
     * <p>
     * 会话解析、状态校验、活性刷新后交协议帧处理器按类型分发。
     * 会话不可解析或已关闭时的快速失败语义按传输上下文适配：SSE抛业务异常
     * 经全局异常处理转统一错误响应，WebSocket记warn静默丢弃。
     * </p>
     *
     * @param sessionId 会话唯一标识
     * @param frame     结构化协议帧
     */
    void acceptUpstream(String sessionId, PushFrame frame);

    /**
     * 上行受理：传输文本（帧解码由协议层完成，解码失败按协议违规处理）
     *
     * @param sessionId 会话唯一标识
     * @param text      上行帧JSON文本
     */
    void acceptUpstreamText(String sessionId, String text);

    /**
     * 服务端主动关闭会话
     * <p>
     * 物理通道拆除与清理链（注销→订阅清理→断开拦截器）一次调用闭环，
     * 不依赖物理通道的事件回传——半开连接等回传不可靠场景下语义依然完整。
     * </p>
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识（PushFrameConstants关闭常量）
     * @return true表示本次调用完成了关闭与清理，false表示会话不存在或已被清理（幂等静默）
     */
    boolean closeSession(String sessionId, String reason);

    /**
     * 被动关闭事件回传入口（物理通道已死时的清理登记）
     * <p>
     * 与{@link #closeSession}共享幂等门禁：清理链已执行过（含主动关闭先行）时静默跳过。
     * </p>
     *
     * @param sessionId 会话唯一标识
     * @param reason    关闭原因标识
     * @return true表示本次调用执行了清理链，false表示重复或会话不存在
     */
    boolean onChannelClosed(String sessionId, String reason);

    /**
     * 优雅关停：按注册表全扫描逐个主动关闭本传输产出的全部会话；幂等
     */
    void stop();

}
