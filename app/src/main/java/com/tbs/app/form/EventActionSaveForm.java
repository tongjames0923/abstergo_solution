package com.tbs.app.form;

import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 事件动作绑定整体保存表单。
 *
 * <p>以事件为单位提交动作列表：服务端按动作ID做增量合并，
 * 未出现在列表中的绑定会被逻辑删除。</p>
 *
 * @author abstergo
 */
@Data
public class EventActionSaveForm implements Serializable {

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
     * 动作绑定列表
     */
    private List<SystemEventActionEntity> actions;
}
