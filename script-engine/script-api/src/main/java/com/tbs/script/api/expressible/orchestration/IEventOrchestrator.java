package com.tbs.script.api.expressible.orchestration;

import com.tbs.script.api.expressible.orchestration.vo.EventDispatchResult;
import com.tbs.script.api.expressible.orchestration.vo.EventEnvelope;
import com.tbs.script.api.expressible.orchestration.vo.EventReportResult;
import com.tbs.script.api.expressible.orchestration.vo.EventSubscription;

import java.util.Collection;
import java.util.List;

/**
 * 事件编排门面。
 *
 * <p>把「上报器上报事件」与「触发器消费事件」串成一条链路：</p>
 * <ol>
 *     <li>{@link #report(EventEnvelope)}：校验并上报事件，随后按订阅关系分发；</li>
 *     <li>{@link #dispatch(EventEnvelope)}：事件已上报的场景，仅按订阅关系分发；</li>
 *     <li>{@link #dispatch(EventEnvelope, Collection)}：按指定触发器列表分发，便于调试与定向预览。</li>
 * </ol>
 *
 * <p>分发时按订阅顺序逐个调用触发器，单个触发器异常被隔离，不中断后续触发器。</p>
 */
public interface IEventOrchestrator {

    /**
     * 上报事件并按订阅关系分发。
     *
     * @param envelope 事件上报信封
     * @return 上报与分发汇总结果；上报失败时不分发
     */
    EventReportResult report(EventEnvelope envelope);

    /**
     * 按订阅关系分发事件，不调用上报器。
     *
     * @param envelope 事件信封
     * @return 分发汇总结果
     */
    EventDispatchResult dispatch(EventEnvelope envelope);

    /**
     * 按指定触发器列表分发事件，不依赖订阅关系。
     *
     * <p>注意：条件触发器需要从订阅关系上取条件编码，未绑定订阅时一律视为未命中；
     * 该重载主要用于已绑定订阅后的定向预览与调试。</p>
     *
     * @param envelope   事件信封
     * @param triggerIds 触发器ID集合，为空时不调用任何触发器
     * @return 分发汇总结果
     */
    EventDispatchResult dispatch(EventEnvelope envelope, Collection<String> triggerIds);

    /**
     * 绑定一个事件订阅。
     *
     * @param subscription 订阅关系
     */
    void bind(EventSubscription subscription);

    /**
     * 解绑指定事件下的一个触发器。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     */
    void unbind(String reporterId, String eventCode, String triggerId);

    /**
     * 整体替换指定事件下的全部订阅。
     *
     * @param reporterId    事件上报器ID
     * @param eventCode     事件编码
     * @param subscriptions 新的订阅集合
     */
    void replace(String reporterId, String eventCode, Collection<EventSubscription> subscriptions);

    /**
     * 查询指定事件下的订阅列表。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 按 order 升序的订阅列表
     */
    List<EventSubscription> listSubscriptions(String reporterId, String eventCode);
}
