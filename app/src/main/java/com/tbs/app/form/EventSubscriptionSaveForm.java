package com.tbs.app.form;

import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 事件订阅整体保存表单。
 *
 * <p>以事件为单位提交订阅列表：服务端按触发器ID做增量合并，
 * 未出现在列表中的绑定会被逻辑删除。</p>
 *
 * @author abstergo
 */
@Data
public class EventSubscriptionSaveForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件上报器ID
     */
    private String reporterId;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 订阅列表
     */
    private List<SystemEventSubscriptionEntity> subscriptions;
}
