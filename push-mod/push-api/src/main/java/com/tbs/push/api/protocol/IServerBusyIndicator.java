package com.tbs.push.api.protocol;

/**
 * 服务端繁忙度指示器 SPI
 * <p>
 * 默认心跳推荐器据此决定 hello 下发给客户端的推荐心跳周期：繁忙度越高，推荐周期越长，
 * 以抑制海量终端在服务器承压时的上行心跳风暴。本接口把"如何度量繁忙"这一强部署相关的
 * 知识从推荐器中剥离——默认实现按在线会话数归一化，业务方可注册自定义 Bean 换用任意
 * 信号源（系统负载、队列深度、GC 压力等），而无需改动推荐器或协议。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IServerBusyIndicator {

    /**
     * 返回当前服务端的归一化繁忙度
     *
     * @return 取值 [0,1]，0 表示空闲、1 表示饱和；越界值由推荐器夹取到该区间
     */
    double busyScore();

}
