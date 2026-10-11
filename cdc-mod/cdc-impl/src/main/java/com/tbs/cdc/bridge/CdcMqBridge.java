package com.tbs.cdc.bridge;

import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.mq.api.producer.IMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * CDC事件MQ桥接
 * <p>
 * 可选能力：当{@code tbs.cdc.dispatch.mode}为mq或both时，将变更事件经{@link IMqProducer}
 * 契约发送至目标交换机，实现跨服务分发。事件{@code eventId}作为messageId传入，
 * 消费端天然获得幂等键。桥接仅在容器存在IMqProducer实现时生效，未引入MQ实现时静默跳过。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
public class CdcMqBridge {

    @Resource
    private CdcProperty cdcProperty;

    /**
     * MQ生产者提供者，未引入MQ实现时为空，故用ObjectProvider延迟获取
     */
    @Resource
    private ObjectProvider<IMqProducer> mqProducerProvider;

    /**
     * 判断当前配置是否需要桥接至MQ
     *
     * @return true表示dispatch.mode包含mq
     */
    public boolean isEnabled() {
        String mode = cdcProperty.getDispatch().getMode();
        boolean needMq = CdcProperty.DispatchMode.MQ.getValue().equalsIgnoreCase(mode)
            || CdcProperty.DispatchMode.BOTH.getValue().equalsIgnoreCase(mode);
        return needMq && mqProducerProvider.getIfAvailable() != null;
    }

    /**
     * 将事件桥接发送至MQ
     * <p>
     * 按routing键模板渲染路由键，以eventId为messageId投递。发送异常不外抛，
     * 避免MQ故障阻断进程内分发链路，故障仅记录ERROR日志。
     * </p>
     *
     * @param event 变更事件，非null
     */
    public void send(CdcEvent event) {
        if (!isEnabled()) {
            return;
        }
        IMqProducer producer = mqProducerProvider.getIfAvailable();
        if (producer == null) {
            return;
        }
        CdcProperty.Mq mq = cdcProperty.getDispatch().getMq();
        String routingKey = renderRoutingKey(mq.getRoutingPattern(), event);
        try {
            producer.send(mq.getExchange(), routingKey, event, event.getEventId());
        } catch (Exception e) {
            log.error("CDC事件桥接至MQ失败, eventId:{}, exchange:{}, routingKey:{}",
                event.getEventId(), mq.getExchange(), routingKey, e);
        }
    }

    /**
     * 渲染路由键模板，替换{database}/{table}/{changeType}占位符
     *
     * @param pattern 路由键模板
     * @param event   变更事件
     * @return 渲染后的路由键
     */
    private String renderRoutingKey(String pattern, CdcEvent event) {
        if (StrUtil.isBlank(pattern)) {
            return StrUtil.format("cdc.{}.{}.{}", event.getDatabase(), event.getTable(), event.getChangeType());
        }
        return pattern
            .replace("{database}", StrUtil.nullToEmpty(event.getDatabase()))
            .replace("{table}", StrUtil.nullToEmpty(event.getTable()))
            .replace("{changeType}", event.getChangeType() == null ? "" : event.getChangeType().name());
    }
}
