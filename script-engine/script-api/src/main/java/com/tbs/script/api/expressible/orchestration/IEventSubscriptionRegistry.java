package com.tbs.script.api.expressible.orchestration;

import com.tbs.script.api.expressible.orchestration.vo.EventSubscription;

import java.util.Collection;
import java.util.List;

/**
 * 事件订阅仓库。
 *
 * <p>承载「事件 → 触发器」的绑定关系，并在绑定上记录条件规则编码。
 * 默认提供内存实现，业务方也可替换为持久化实现（实现本接口并声明为 Bean 即可）。</p>
 */
public interface IEventSubscriptionRegistry {

    /**
     * 绑定一个事件订阅。
     *
     * <p>同一 {@code (reporterId, eventCode, triggerId)} 已存在时覆盖原绑定。</p>
     *
     * @param subscription 订阅关系，reporterId、eventCode、triggerId 必填
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
     * @param subscriptions 新的订阅集合，为空时清空该事件的全部订阅
     */
    void replace(String reporterId, String eventCode, Collection<EventSubscription> subscriptions);

    /**
     * 查询指定事件下的全部订阅。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 按 order 升序的订阅列表，无订阅时返回空列表
     */
    List<EventSubscription> find(String reporterId, String eventCode);

    /**
     * 查询指定事件下的单个订阅，供条件触发器反查自身的条件规则编码。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     * @return 订阅关系；未绑定时返回 null
     */
    EventSubscription find(String reporterId, String eventCode, String triggerId);
}
