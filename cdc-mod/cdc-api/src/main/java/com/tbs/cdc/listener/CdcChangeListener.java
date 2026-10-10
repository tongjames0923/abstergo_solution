package com.tbs.cdc.listener;

import com.tbs.cdc.model.CdcEvent;

/**
 * 编程式变更监听契约
 * <p>
 * 与注解式{@code @CdcListener}并存的另一种监听风格。实现类注册为Spring Bean后，
 * 由运行时在启动期扫描并按{@link #getSubscription()}声明的关注范围路由事件。
 * 投递语义为至少一次，实现方应按{@code event.getEventId()}自行做幂等处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface CdcChangeListener {

    /**
     * 声明本监听器关注的库表、变更类型与执行方式
     *
     * @return 订阅描述，不得为null
     */
    CdcSubscription getSubscription();

    /**
     * 处理一条变更事件
     *
     * @param event 统一变更事件，非null
     */
    void onCdcEvent(CdcEvent event);
}
