package com.tbs.app.service.event;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;

import java.util.Collection;
import java.util.List;

/**
 * 事件动作绑定服务。
 *
 * <p>把「事件命中条件后执行哪些动作」持久化为按事件索引的配置：
 * 运行期由动作回调读取启用中的绑定并按顺序执行，管理端通过本服务维护配置。</p>
 *
 * @author abstergo
 */
public interface IEventActionService extends IServiceTemplate<SystemEventActionEntity, Long> {

    /**
     * 查询指定事件下的全部动作绑定（含停用），按执行顺序升序。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 动作绑定列表；无匹配时返回空列表
     */
    List<SystemEventActionEntity> listByEvent(String reporterId, String eventCode);

    /**
     * 查询指定事件下启用中的动作绑定，按执行顺序升序。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @return 启用中的动作绑定列表；无匹配时返回空列表
     */
    List<SystemEventActionEntity> listEnabled(String reporterId, String eventCode);

    /**
     * 整体替换指定事件下的全部动作绑定。
     *
     * <p>按动作ID做增量合并：已存在的绑定更新参数、顺序与启停状态，
     * 新增的绑定插入，未出现在入参中的绑定逻辑删除。</p>
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param actions    新的动作绑定集合，为空时清空该事件的全部动作
     * @return 保存后的动作绑定列表
     */
    List<SystemEventActionEntity> replaceByEvent(String reporterId, String eventCode,
        Collection<SystemEventActionEntity> actions);

    /**
     * 启用或停用动作绑定。
     *
     * @param id      绑定主键
     * @param enabled true 启用，false 停用
     */
    void setEnabled(Long id, boolean enabled);
}
