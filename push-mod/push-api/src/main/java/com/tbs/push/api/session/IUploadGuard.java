package com.tbs.push.api.session;

import com.tbs.push.model.frame.PushFrame;

/**
 * 上行帧守卫扩展点
 * <p>
 * 协议层统一的上行入口校验：全部传输（SSE HTTP upload / WebSocket 帧上行）进入
 * {@code PushFrameProcessor} 分发帧类型前依次执行守卫链，任一守卫拒绝则回发
 * ERROR帧（SESSION_NOT_OWNER）丢弃该帧，不关闭连接。
 * 守卫基于会话属性（认证阶段绑定的身份）判定，不感知具体传输；
 * 未注册任何守卫实现时全部上行放行，保持历史行为。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IUploadGuard {

    /**
     * 判定会话可否上行指定协议帧
     *
     * @param session 来源会话（携带认证阶段绑定的身份属性）
     * @param frame   待处理的协议帧
     * @return true表示放行，false表示拒绝该帧
     */
    boolean permit(IConnectionSession session, PushFrame frame);

}
