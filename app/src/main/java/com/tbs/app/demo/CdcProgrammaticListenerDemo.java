package com.tbs.app.demo;

import com.tbs.cdc.listener.CdcChangeListener;
import com.tbs.cdc.listener.CdcSubscription;
import com.tbs.cdc.model.CdcChangeType;
import com.tbs.cdc.model.CdcEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * 编程式CDC监听示例
 * <p>
 * 演示实现{@link CdcChangeListener}契约，通过{@link #getSubscription()}声明关注范围，
 * 与注解式监听并存的另一种风格。适合监听逻辑复杂、需复用实例字段或依赖注入的场景。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class CdcProgrammaticListenerDemo implements CdcChangeListener {

    /**
     * 声明订阅：关注sys_role表的变更类型集合与执行方式
     *
     * @return 订阅描述
     */
    @Override
    public CdcSubscription getSubscription() {
        Set<CdcChangeType> types = EnumSet.of(CdcChangeType.CREATE, CdcChangeType.UPDATE, CdcChangeType.DELETE);
        return CdcSubscription.builder()
            .tablePatterns(Set.of("sys_role"))
            .changeTypes(types)
            .async(true)
            .build();
    }

    /**
     * 处理命中的变更事件
     * <p>
     * 至少一次投递，生产消费须按{@code eventId}幂等；本示例仅打印事件概要作为使用范例。
     * </p>
     *
     * @param event 统一变更事件，非null
     */
    @Override
    public void onCdcEvent(CdcEvent event) {
        log.info("[CDC-编程式] 捕获变更 eventId:{}, table:{}, op:{}, afterData:{}",
            event.getEventId(), event.getTable(), event.getChangeType(), event.getAfterData());
    }
}
