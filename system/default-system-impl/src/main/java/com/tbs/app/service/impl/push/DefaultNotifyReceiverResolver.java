package com.tbs.app.service.impl.push;

import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.service.push.INotifyReceiverResolver;

import java.util.Collections;
import java.util.List;

/**
 * 接收人解析器默认实现（兜底）
 * <p>
 * 保守行为：支持全部消息但解析结果为空集——消息仅落主表与 SEND 日志， 不产生 fan-out 与信令（纯落库审计语义）。 经 BuisnessBeanConfig 以 @ConditionalOnMissingBean 装配，
 * 业务方注册自定义实现 Bean 即覆盖本实现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultNotifyReceiverResolver implements INotifyReceiverResolver {

    /**
     * 默认实现支持全部消息（兜底语义）
     *
     * @param form 发送表单
     * @return 恒为true
     */
    @Override
    public boolean supports(PushNotifySendForm form) {
        return true;
    }

    /**
     * 默认不解析任何接收人（空集=无 fan-out、无信令）
     *
     * @param form 发送表单
     * @return 空列表
     */
    @Override
    public List<Long> resolveReceivers(PushNotifySendForm form) {
        return Collections.emptyList();
    }

}
