package com.tbs.redis.boradcast;

import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.impl.SpringManagedListenablePublishResult;

/**
 * 默认的Redis事件总线实现
 * @author tongj
 */
public class SimpleRedisEventBus extends AbstractRedisEventBus<SimpleRedisAcceptedPublishResult> {

    @Override
    protected SimpleRedisAcceptedPublishResult put(IPublishResult r) {
        return new SimpleRedisAcceptedPublishResult(r.getEvent(),r.getPublisher().getPushlisherId(),
            r.getHandleMode());
    }

    @Override
    public String getChannel() {
        return "tbs.event.channel";
    }

    @Override
    protected IPublishResult transform(SimpleRedisAcceptedPublishResult r) {
        return new SpringManagedListenablePublishResult(r.getEvent(), r.getPublisher(), r.getHandleMode());
    }

}
