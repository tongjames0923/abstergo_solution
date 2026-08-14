package com.tbs.mq.config;

import com.tbs.mq.api.consumer.IMessageRepublisher;
import com.tbs.mq.api.retry.IFailedMessageStore;
import com.tbs.mq.api.retry.IRetryDelayPolicy;
import com.tbs.mq.config.property.MqRetryProperty;
import com.tbs.mq.impl.EmbeddedRetryScheduler;
import com.tbs.mq.impl.RetryDispatchBackgroundJob;
import com.tbs.mq.impl.RetryMessageDispatcher;
import com.tbs.mq.impl.SteppedRetryDelayPolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQ抽象层默认组件装配
 * <p>
 * 为失败重试链路提供厂商无关的开箱即用默认装配，全部允许被使用方覆盖：
 * <ul>
 *     <li>{@link IRetryDelayPolicy} — 档位式延迟策略（30秒→2分钟→10分钟）</li>
 *     <li>{@link IFailedMessageStore} — 懒委托存储：运行期优先透传真实实现（如redis-mod的Redis存储），无实现时退化为Noop兜底</li>
 *     <li>{@link RetryMessageDispatcher} — 纯分发器，本身不含调度线程</li>
 * </ul>
 * 调度驱动二选一互斥装配（属性{@link MqRetryProperty#getScheduler()}）：
 * <ul>
 *     <li>embedded（默认）— {@link EmbeddedRetryScheduler}内嵌守护线程，零配置开箱即用</li>
 *     <li>job — {@link RetryDispatchBackgroundJob}经IBackgroundJob交由外部调度平台（如xxl-job）集中触发</li>
 * </ul>
 * 具体MQ厂商的发送与重投递实现（IMqProducer、IMessageRepublisher）由对应impl模块（如rabbitmq-impl）自行装配。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MqRetryProperty.class)
public class MqSupportConfig {

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
     * 失败消息存储懒委托装配
     * <p>
     * 不在装配期固化Noop兜底：真实存储实现（无论何时由哪个模块注册）均在运行期优先生效，
     * 全部缺失时才表现为Noop兜底行为。
     * </p>
     *
     * @param stores 存储实现提供者
     * @return 懒委托失败消息存储
     */
    @Bean
    @ConditionalOnMissingBean(IFailedMessageStore.class)
    public IFailedMessageStore failedMessageStore(ObjectProvider<IFailedMessageStore> stores) {
        return new DelegatingFailedMessageStore(stores);
    }

    /**
     * 失败消息重投递分发器
     * <p>
     * 纯分发职责，存储与重投递器均按可选依赖注入，缺失时分发静默跳过。
     * </p>
     *
     * @param failedMessageStores 失败消息存储提供者
     * @param republishers        重投递器提供者
     * @return 重投递分发器
     */
    @Bean
    @ConditionalOnMissingBean(RetryMessageDispatcher.class)
    public RetryMessageDispatcher retryMessageDispatcher(ObjectProvider<IFailedMessageStore> failedMessageStores,
                                                         ObjectProvider<IMessageRepublisher> republishers) {
        return new RetryMessageDispatcher(failedMessageStores.getIfAvailable(), republishers.getIfAvailable());
    }

    /**
     * 内嵌式重试调度器（默认驱动）
     * <p>
     * 调度间隔取自{@link MqRetryProperty#getIntervalMillis()}。
     * 注：互斥判定用@ConditionalOnProperty而非属性Bean，因条件在Bean定义阶段评估，
     * 早于配置属性绑定，二者读自同一Environment不冲突。
     * </p>
     *
     * @param dispatcher 重投递分发器
     * @param property   重试配置属性
     * @return 内嵌式重试调度器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.mq.retry", name = "scheduler", havingValue = MqRetryProperty.SCHEDULER_EMBEDDED, matchIfMissing = true)
    @ConditionalOnMissingBean(EmbeddedRetryScheduler.class)
    public EmbeddedRetryScheduler embeddedRetryScheduler(RetryMessageDispatcher dispatcher, MqRetryProperty property) {
        return new EmbeddedRetryScheduler(dispatcher, property.getIntervalMillis());
    }

    /**
     * 失败消息重投递后台任务桥接（外部调度驱动）
     * <p>
     * 配置{@code tbs.mq.retry.scheduler=job}时启用，与内嵌调度器互斥，
     * 交由xxl-job等外部调度平台集中触发。
     * </p>
     *
     * @param dispatcher 重投递分发器
     * @return 后台任务桥接
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.mq.retry", name = "scheduler", havingValue = MqRetryProperty.SCHEDULER_JOB)
    @ConditionalOnMissingBean(RetryDispatchBackgroundJob.class)
    public RetryDispatchBackgroundJob retryDispatchBackgroundJob(RetryMessageDispatcher dispatcher) {
        return new RetryDispatchBackgroundJob(dispatcher);
    }

}
