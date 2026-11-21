package com.tbs.app.service.event;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;

import java.util.Collection;
import java.util.List;

/**
 * 事件订阅服务。
 *
 * <p>把「事件 → 触发器 → 条件规则」的绑定持久化为按事件索引的配置：
 * 运行期由事件编排层读取启用中的订阅完成分发，管理端通过本服务维护配置。</p>
 *
 * @author abstergo
 */
public interface IEventSubscriptionService extends IServiceTemplate<SystemEventSubscriptionEntity, Long> {

    /**
     * 查询指定事件下的全部订阅（含停用），按执行顺序升序。
     *
     * <p>供写操作前的增量合并使用：仅在启用订阅上做合并会误删同事件的停用绑定。</p>
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 订阅列表；无匹配时返回空列表
     */
    List<SystemEventSubscriptionEntity> listByEvent(String reporterId, String eventCode);

    /**
     * 查询指定事件下启用中的订阅，按执行顺序升序。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 启用中的订阅列表；无匹配时返回空列表
     */
    List<SystemEventSubscriptionEntity> listEnabled(String reporterId, String eventCode);

    /**
     * 查询指定事件下某个触发器的启用订阅。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param triggerId  触发器ID
     * @return 启用中的订阅；未绑定或已停用时返回 null
     */
    SystemEventSubscriptionEntity findEnabled(String reporterId, String eventCode, String triggerId);

    /**
     * 整体替换指定事件下的全部订阅。
     *
     * <p>按触发器ID做增量合并：已存在的绑定更新条件规则、顺序与启停状态，
     * 新增的绑定插入，未出现在入参中的绑定逻辑删除。</p>
     *
     * @param reporterId    事件上报器ID
     * @param eventCode     事件编码
     * @param subscriptions 新的订阅集合，为空时清空该事件的全部订阅
     * @return 保存后的订阅列表
     */
    List<SystemEventSubscriptionEntity> replaceByEvent(String reporterId, String eventCode,
        Collection<SystemEventSubscriptionEntity> subscriptions);

    /**
     * 启用或停用订阅。
     *
     * @param id      订阅主键
     * @param enabled true 启用，false 停用
     */
    void setEnabled(Long id, boolean enabled);
}
