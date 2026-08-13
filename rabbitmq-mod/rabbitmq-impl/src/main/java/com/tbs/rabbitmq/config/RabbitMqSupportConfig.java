package com.tbs.rabbitmq.config;

import com.tbs.mq.api.consumer.IMessageRepublisher;
import com.tbs.mq.api.producer.IMqProducer;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.impl.SteppedRetryDelayPolicy;
import com.tbs.rabbitmq.producer.RabbitMqProducer;
import com.tbs.rabbitmq.retry.NoopFailedMessageStore;
import com.tbs.rabbitmq.retry.RabbitMqMessageRepublisher;
import com.tbs.rabbitmq.retry.RetryMessageDispatcher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消费框架默认组件装配
 * <p>
 * 为统一发送、失败重试、重投递链路提供开箱即用的默认实现，全部允许被使用方覆盖：
 * <ul>
 *     <li>{@link IMqProducer} — 基于RabbitTemplate的统一发送实现</li>
 *     <li>{@link IRetryDelayPolicy} — 档位式延迟策略（30秒→2分钟→10分钟）</li>
 *     <li>{@link IFailedMessageStore} — 无真实存储时的空实现兜底（如引入redis-mod后由Redis实现接管）</li>
 *     <li>{@link IMessageRepublisher} — 失败消息还原与重投递实现</li>
 *     <li>{@link RetryMessageDispatcher} — 到期消息重投递调度器</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
public class RabbitMqSupportConfig {

    /**
     * 统一消息发送实现
     *
     * @param rabbitTemplate   RabbitMQ发送模板
     * @param messageConverter 消息转换器
     * @return 默认消息发送实现
     */
    @Bean
    @ConditionalOnMissingBean(IMqProducer.class)
    public IMqProducer mqProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        return new RabbitMqProducer(rabbitTemplate, messageConverter);
    }

    /**
     * 默认重试延迟策略
     *
     * @return 档位式延迟策略
     */
    @Bean
    @ConditionalOnMissingBean(IRetryDelayPolicy.class)
    public IRetryDelayPolicy retryDelayPolicy() {
        return new SteppedRetryDelayPolicy();
    }

    /**
     * 失败消息存储兜底实现
     * <p>
     * 仅在容器尚未提供任何存储实现时生效；引入redis-mod等实现模块后由真实存储接管。
     * </p>
     *
     * @return 空实现的失败消息存储
     */
    @Bean
    @ConditionalOnMissingBean(IFailedMessageStore.class)
    public IFailedMessageStore failedMessageStore() {
        return new NoopFailedMessageStore();
    }

    /**
     * 失败消息重投递实现
     *
     * @param failedMessageStore 失败消息存储
     * @param retryDelayPolicy   重试延迟策略
     * @param rabbitTemplate     RabbitMQ发送模板
     * @return 默认重投递实现
     */
    @Bean
    @ConditionalOnMissingBean(IMessageRepublisher.class)
    public IMessageRepublisher messageRepublisher(IFailedMessageStore failedMessageStore,
                                                  IRetryDelayPolicy retryDelayPolicy,
                                                  RabbitTemplate rabbitTemplate) {
        return new RabbitMqMessageRepublisher(failedMessageStore, retryDelayPolicy, rabbitTemplate);
    }

    /**
     * 失败消息重投递调度器
     * <p>
     * 存储与重投递器均按可选依赖注入，缺失时调度器自动降级为不启动。
     * </p>
     *
     * @param failedMessageStores 失败消息存储提供者
     * @param republishers        重投递器提供者
     * @return 重投递调度器
     */
    @Bean
    @ConditionalOnMissingBean(RetryMessageDispatcher.class)
    public RetryMessageDispatcher retryMessageDispatcher(ObjectProvider<IFailedMessageStore> failedMessageStores,
                                                         ObjectProvider<IMessageRepublisher> republishers) {
        return new RetryMessageDispatcher(failedMessageStores.getIfAvailable(), republishers.getIfAvailable());
    }

}
