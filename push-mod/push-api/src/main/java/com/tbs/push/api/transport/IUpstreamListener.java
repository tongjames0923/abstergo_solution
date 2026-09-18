package com.tbs.push.api.transport;

import com.tbs.push.model.PushFrame;
import com.tbs.push.api.session.IConnectionSession;

/**
 * 上行消息监听器SPI
 * <p>
 * 客户端经长连接上行的DATA帧的处理出口：协议层解析出合法DATA帧后，
 * 逐一通知全部已注册的监听器（可注册多个Bean）。未注册任何监听器时，
 * 上行DATA帧仅记录debug日志后忽略。
 * 监听器实现内部抛出的异常由协议层捕获记录，不影响连接本身。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IUpstreamListener {

    /**
     * 处理上行DATA帧
     *
     * @param session 来源会话
     * @param frame   上行的DATA帧
     */
    void onUpstream(IConnectionSession session, PushFrame frame);

}
