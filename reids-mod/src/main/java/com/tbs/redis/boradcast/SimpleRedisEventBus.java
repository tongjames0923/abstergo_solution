package com.tbs.redis.boradcast;

import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.impl.SpringManagedListenablePublishResult;
import com.tbs.common.data.IDataBridge;
import org.jetbrains.annotations.NotNull;

/**
 * 默认的Redis事件总线实现
 * @author tongj
 */
public class SimpleRedisEventBus extends AbstractRedisEventBus<SimpleRedisAcceptedPublishResult> {

    @Override
    public String getChannel() {
        return "tbs.event.channel";
    }

    private static class SimpleDataBridge implements IDataBridge<SimpleRedisAcceptedPublishResult, IPublishResult> {

        @Override
        public SimpleRedisAcceptedPublishResult bridge(@NotNull IPublishResult obj) {
            return new SimpleRedisAcceptedPublishResult(obj.getEvent(), obj.getPublisher().getPushlisherId(),
                obj.getHandleMode());
        }

        @Override
        public IPublishResult unbridge(@NotNull SimpleRedisAcceptedPublishResult obj) {
            return new SpringManagedListenablePublishResult(obj.getEvent(), obj.getPublisher(), obj.getHandleMode());
        }
    }

    private static final SimpleDataBridge simpleDataBridge = new SimpleDataBridge();

    @Override
    protected IDataBridge<SimpleRedisAcceptedPublishResult, IPublishResult> getDataBridge() {
        return simpleDataBridge;
    }

}
