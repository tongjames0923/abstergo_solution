package com.tbs.cdc.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.cdc.listener.CdcChangeListener;
import com.tbs.cdc.listener.CdcSubscription;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.service.i18n.CdcServiceI18nConstant;
import com.tbs.cdc.service.property.CdcMqProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.api.producer.IMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;

/**
 * CDC变更事件MQ发出监听器
 * <p>
 * 订阅全部库表与全部变更类型，将{@code CdcEvent}按路由键模板渲染后， 以JSON载荷经{@code IMqProducer}发出，{@code eventId}作为messageId幂等键。
 * 发送失败仅记录日志，不影响CDC总线上的其他监听器与捕获链路。 启动阶段按{@code autoDeclare}配置自动声明交换机与可选的队列绑定。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class CdcMqPublisher implements CdcChangeListener, InitializingBean {

    /**
     * 路由键模板占位符：数据库名
     */
    private static final String PLACEHOLDER_DATABASE = "{database}";

    /**
     * 路由键模板占位符：表名
     */
    private static final String PLACEHOLDER_TABLE = "{table}";

    /**
     * 路由键模板占位符：变更类型
     */
    private static final String PLACEHOLDER_CHANGE_TYPE = "{changeType}";

    /**
     * MQ消息发送契约
     */
    private final IMqProducer mqProducer;

    /**
     * MQ发出配置
     */
    private final CdcMqProperty property;

    /**
     * JSON序列化器
     */
    private final ObjectMapper objectMapper;

    /**
     * AMQP管理器提供者，用于按配置自动声明资源；未装配时跳过声明
     */
    private final ObjectProvider<AmqpAdmin> amqpAdminProvider;

    /**
     * 构造CDC MQ发出监听器
     *
     * @param mqProducer        MQ消息发送契约
     * @param property          MQ发出配置
     * @param objectMapper      JSON序列化器
     * @param amqpAdminProvider AMQP管理器提供者
     */
    public CdcMqPublisher(IMqProducer mqProducer, CdcMqProperty property, ObjectMapper objectMapper,
        ObjectProvider<AmqpAdmin> amqpAdminProvider) {
        this.mqProducer = mqProducer;
        this.property = property;
        this.objectMapper = objectMapper;
        this.amqpAdminProvider = amqpAdminProvider;
    }

    /**
     * 声明本监听器订阅全部变更事件
     *
     * @return 不限制库表与类型的全量订阅描述
     */
    @Override
    public CdcSubscription getSubscription() {
        return CdcSubscription.all();
    }

    /**
     * 处理一条变更事件：渲染路由键并发出到MQ
     *
     * @param event 统一变更事件
     */
    @Override
    public void onCdcEvent(CdcEvent event) {
        if (event == null) {
            log.error("CDC事件为空");
            return;
        }
        try {
            String routingKey = renderRoutingKey(event);
            String payload = objectMapper.writeValueAsString(event);
            log.info("CDC事件MQ发送中,{} eventId:{}, routingKey:{}, payload:{}", property.getExchange(),
                event.getEventId(), routingKey, payload);
            mqProducer.send(property.getExchange(), routingKey, payload, event.getEventId());
        } catch (Exception e) {
            log.error("CDC事件MQ发送失败, eventId:{}, table:{}", event.getEventId(), event.getTable(), e);
        }
    }

    /**
     * 启动校验与资源声明
     */
    @Override
    public void afterPropertiesSet() {
        AssertUtil.isTrue(StrUtil.isBlank(property.getExchange())).i18n(CdcServiceI18nConstant.MQ_EXCHANGE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isBlank(property.getRoutingPattern()))
            .i18n(CdcServiceI18nConstant.MQ_ROUTING_PATTERN_REQUIRED).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        log.info("CDC MQ发出监听器已启动, exchange:{}, routingPattern:{}", property.getExchange(),
            property.getRoutingPattern());
    }

    /**
     * 按模板渲染事件的路由键
     *
     * @param event 统一变更事件
     * @return 渲染后的路由键
     */
    private String renderRoutingKey(CdcEvent event) {
        return property.getRoutingPattern().replace(PLACEHOLDER_DATABASE, nullSafe(event.getDatabase()))
            .replace(PLACEHOLDER_TABLE, nullSafe(event.getTable()))
            .replace(PLACEHOLDER_CHANGE_TYPE, event.getChangeType() == null ? "" : event.getChangeType().name());
    }

    /**
     * 空值兜底：null渲染为空串
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

}
