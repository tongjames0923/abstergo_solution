package com.tbs.push.api.transport;

import com.tbs.push.model.TransportCapability;

/**
 * 传输适配器契约
 * <p>
 * 每种底层传输架构（WebSocket/SSE等）对应一个适配器实现，职责边界严格限定为：
 * <ul>
 *     <li>建立/关闭传输通道（端点注册、监听器挂载等传输细节）</li>
 *     <li>把物理连接包装为统一会话{@code IConnectionSession}并登记进会话注册表</li>
 *     <li>帧收发一律委托协议层的编解码器与帧处理器</li>
 * </ul>
 * 适配器之间互不依赖；上层经 tbs.push.transport 属性互斥选择装配，同一时刻仅一个适配器生效。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ITransportAdapter {

    /**
     * 获取传输类型标识
     *
     * @return 传输类型标识（见PushFrameConstants传输常量）
     */
    String getTransportType();

    /**
     * 获取传输能力声明
     *
     * @return 传输能力声明
     */
    TransportCapability capability();

    /**
     * 启动适配器
     * <p>
     * 传输端点注册等初始化动作；幂等，重复启动无副作用。
     * </p>
     */
    void start();

    /**
     * 停止适配器
     * <p>
     * 优雅关闭本适配器产出的全部会话；幂等，重复停止无副作用。
     * </p>
     */
    void stop();

}
