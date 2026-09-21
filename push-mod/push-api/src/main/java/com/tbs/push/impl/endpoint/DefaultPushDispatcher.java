package com.tbs.push.impl.endpoint;

import com.tbs.push.api.coordinator.IRouteCoordinator;
import com.tbs.push.api.dispatch.AbstractPushDispatcher;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.store.IPushMessageStore;

/**
 * 默认推送消息分发器
 * <p>
 * 在分发器模板基础上绑定消息源，形成完整分发闭环： 取消息 → 协调器解析目标 → 逐会话投递 → 结果留痕 → 标记分发完成。 本类不持有调度线程，触发方式由调度驱动决定。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultPushDispatcher extends AbstractPushDispatcher {

    /**
     * 消息源
     */
    private final IPushMessageStore messageStore;

    /**
     * 构造默认分发器
     *
     * @param routeCoordinator 投递协调器，不能为空
     * @param codec            协议编解码器，不能为空
     * @param messageStore     消息源，不能为空
     */
    public DefaultPushDispatcher(IRouteCoordinator routeCoordinator, IPushCodec codec, IPushMessageStore messageStore) {
        super(routeCoordinator, codec);
        this.messageStore = messageStore;
    }

    /**
     * 获取消息源
     *
     * @return 消息源
     */
    @Override
    protected IPushMessageStore getMessageStore() {
        return messageStore;
    }

}
