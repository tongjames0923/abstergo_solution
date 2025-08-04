package com.tbs.boardcast.bus.impl;

import com.tbs.boardcast.bus.AbstractSemaphoreEventBus;
import com.tbs.boardcast.publisher.IPublishResult;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 默认事件总线
 *
 * @author abstergo
 */
public class SimpleEventBus extends AbstractSemaphoreEventBus {

    private static PriorityQueue<IPublishResult> queue = new PriorityQueue<>(new Comparator<IPublishResult>() {
        @Override
        public int compare(IPublishResult o1, IPublishResult o2) {
            return o1.getEvent().getEventPriority() - o2.getEvent().getEventPriority();
        }
    });

    @Override
    protected Queue<IPublishResult> getQueue() {
        return queue;
    }

}
