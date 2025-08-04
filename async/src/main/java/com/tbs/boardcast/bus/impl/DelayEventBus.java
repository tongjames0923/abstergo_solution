//package com.tbs.boardcast.bus.impl;
//
//import com.tbs.boardcast.bus.AbstractEventBus;
//import com.tbs.boardcast.bus.AbstractSemaphoreEventBus;
//import com.tbs.boardcast.publisher.IPublishResult;
//import com.tbs.schedule.IScheduleTask;
//
//import java.util.Queue;
//import java.util.concurrent.DelayQueue;
//import java.util.concurrent.TimeUnit;
//
///**
// * 延迟事件总线
// *
// * @author tongj
// */
//public class DelayEventBus extends AbstractSemaphoreEventBus<DelayEventPublishResult> {
//
//    private final DelayQueue<DelayEventPublishResult> delayQueue = new DelayQueue<>();
//
//    @Override
//    protected Queue<DelayEventPublishResult> getQueue() {
//        return delayQueue;
//    }
//
//    @Override
//    protected DelayEventPublishResult put(IPublishResult r) {
//        if (r instanceof DelayEventPublishResult) {
//            return (DelayEventPublishResult)r;
//        }
//        if (r.getEvent().getEventData() == null) {
//            return new DelayEventPublishResult<>(null, r);
//        }
//        if (r.getEvent().getEventDataClass()) {
//            return new DelayEventPublishResult<>((IScheduleTask)r.getEvent().getEventData(), r);
//        } else {
//            return new DelayEventPublishResult<>(null, r);
//        }
//    }
//
//}
