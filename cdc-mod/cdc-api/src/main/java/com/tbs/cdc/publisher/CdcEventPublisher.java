package com.tbs.cdc.publisher;

import com.tbs.cdc.model.CdcEvent;

/**
 * 变更事件发布契约
 * <p>
 * 抽象变更事件的入口，供捕获源以外的其他来源（如人工触发、补偿重放）复用同一分发管道。
 * 发布的事件经滤链与事件总线路由至匹配的监听器与可选MQ桥接。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface CdcEventPublisher {

    /**
     * 发布一条变更事件进入分发管道
     *
     * @param event 统一变更事件，不得为null
     */
    void publish(CdcEvent event);
}
