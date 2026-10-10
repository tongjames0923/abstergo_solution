package com.tbs.app.demo;

import com.tbs.cdc.annotation.CdcListener;
import com.tbs.cdc.model.CdcChangeType;
import com.tbs.cdc.model.CdcEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 注解式CDC监听示例
 * <p>
 * 演示以{@link CdcListener}声明关注表与变更类型，运行时按声明路由事件。
 * 监听方法须有且仅有一个{@link CdcEvent}入参。投递语义为至少一次，
 * 生产消费须按{@code eventId}做幂等，本示例仅打印事件概要作为使用范例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class CdcAnnotationListenerDemo {

    /**
     * 监听sys_user表的插入与更新变更（异步执行）
     *
     * @param event 变更事件
     */
    @CdcListener(tables = {"sys_user"}, types = {CdcChangeType.CREATE, CdcChangeType.UPDATE}, async = true)
    public void onSysUserChanged(CdcEvent event) {
        log.info("[CDC-注解] 捕获变更 eventId:{}, table:{}, op:{}, changedColumns:{}",
            event.getEventId(), event.getTable(), event.getChangeType(), event.changedColumns());
    }

    /**
     * 监听所有表的删除变更（同步执行，示例展示async=false）
     *
     * @param event 变更事件
     */
    @CdcListener(types = {CdcChangeType.DELETE}, async = false)
    public void onAnyDelete(CdcEvent event) {
        log.info("[CDC-注解] 捕获删除 eventId:{}, db:{}, table:{}, primaryKey:{}",
            event.getEventId(), event.getDatabase(), event.getTable(), event.getPrimaryKey());
    }
}
