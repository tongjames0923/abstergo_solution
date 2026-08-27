package com.tbs.schedule.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.schedule.NoopTaskStore;
import com.tbs.schedule.SchedulerCore;
import com.tbs.schedule.api.*;
import com.tbs.schedule.dispatcher.LocalTaskDispatcher;
import com.tbs.schedule.executor.DefaultExecutionPolicy;
import com.tbs.schedule.executor.ExecutionPolicyResolver;
import com.tbs.schedule.executor.LocalRunnableTaskExecutor;
import com.tbs.schedule.impl.LocalThreadScheduleTrigger;
import com.tbs.schedule.impl.SimpleLogErrorHandle;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

/**
 * 调度内核装配
 * <p>
 * 按 {@code tbs.schedule.enable} 总开关条件化装配调度内核与 SPI 链：
 * 时序策略 → 派发器 → 执行器 → 执行策略 → 监听器 → 存储 → 内核。
 * 所有可替换能力均经 {@code @ConditionalOnMissingBean} 注册，业务方可注册自定义实现覆盖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ScheduleProperty.class)
@ConditionalOnProperty(prefix = "tbs.schedule", name = "enable", havingValue = "true", matchIfMissing = true)
public class ScheduleConfig {

    /**
     * 时序策略（时间轮默认，无参 DelayQueue 兼容）
     *
     * @param property 配置属性
     * @return 时序策略
     */
    @Bean
    @ConditionalOnMissingBean(ScheduleStrategy.class)
    public ScheduleStrategy scheduleStrategy(ScheduleProperty property) {
        return buildStrategy(property);
    }

    /**
     * 默认执行策略
     *
     * @return 执行策略
     */
    @Bean
    @ConditionalOnMissingBean(ExecutionPolicy.class)
    public ExecutionPolicy executionPolicy() {
        return new DefaultExecutionPolicy();
    }

    /**
     * 执行策略解析器（三级覆盖）
     *
     * @param property 配置属性
     * @return 解析器
     */
    @Bean
    public ExecutionPolicyResolver executionPolicyResolver(ScheduleProperty property) {
        return ExecutionPolicyResolver.fromSpring();
    }

    /**
     * 本地 Runnable 执行器（默认兜底）
     *
     * @return 执行器
     */
    @Bean
    @ConditionalOnMissingBean(TaskExecutor.class)
    public TaskExecutor localRunnableTaskExecutor() {
        return new LocalRunnableTaskExecutor();
    }

    /**
     * 默认错误处理器
     *
     * @param property 配置属性
     * @return 错误处理器
     */
    @Bean
    @ConditionalOnMissingBean(TaskExecuteErrorHandle.class)
    public TaskExecuteErrorHandle scheduleExecuteErrorHandle(ScheduleProperty property) {
        return BeanUtil.buildBeanFromProperties(SimpleLogErrorHandle::new, property.getScheduleExecuteErrorHandle(),
            BeanUtil::buildByEmptyArgs);
    }

    /**
     * 内存兜底任务存储
     *
     * @return 任务存储
     */
    @Bean
    @ConditionalOnMissingBean(TaskStore.class)
    public TaskStore taskStore() {
        return new NoopTaskStore();
    }

    /**
     * 本地派发器（默认，执行外移）
     *
     * @param resolver  执行策略解析器
     * @param listeners 生命周期监听器
     * @return 派发器
     */
    @Bean
    @ConditionalOnMissingBean(TaskDispatcher.class)
    public TaskDispatcher localTaskDispatcher(ExecutionPolicyResolver resolver,
        ObjectProvider<LifecycleListener> listeners) {
        return new LocalTaskDispatcher(resolver, listeners.stream().collect(Collectors.toList()));
    }

    /**
     * 调度内核
     *
     * @param property   配置属性
     * @param strategy   时序策略
     * @param dispatcher 派发器
     * @param taskStore  任务存储
     * @param listeners  生命周期监听器
     * @return 调度内核
     */
    @Bean
    public SchedulerCore schedulerCore(ScheduleProperty property, ScheduleStrategy strategy, TaskDispatcher dispatcher,
        TaskStore taskStore, ScheduleTrigger trigger, ObjectProvider<LifecycleListener> listeners) {
        SchedulerCore core =
            new SchedulerCore(strategy, dispatcher, taskStore, listeners.stream().collect(Collectors.toList()),
                trigger);
        /* 将内核注入触发器，使其可驱动 tick；本地模式触发器作为 IStartup 在应用启动后自启循环 */
        trigger.start(core);
        return core;
    }

    /**
     * 调度触发器（内置线程触发器条件装配）
     * <p>
     * 仅当 {@code embeddedTrigger=true}（默认，未配置亦生效）且容器中无自定义 {@code ScheduleTrigger} Bean 时， 装配内置
     * {@code LocalThreadScheduleTrigger}（进程内常驻线程阻塞轮询，实现 IStartup 应用启动后自启循环）。 使用方需以其他调度器驱动内核时，将 {@code embeddedTrigger}
     * 置为 false 并在自身模块注册 {@code ScheduleTrigger} Bean（调用 {@code SchedulerCore.tick()}）即可覆盖，async 模块对具体调度器零假设。
     * </p>
     *
     * @param property 配置属性
     * @return 触发器
     */
    @Bean
    @ConditionalOnMissingBean(ScheduleTrigger.class)
    @ConditionalOnProperty(prefix = "tbs.schedule", name = "embedded-trigger", havingValue = "true",
        matchIfMissing = true)
    public ScheduleTrigger scheduleTrigger(ScheduleProperty property) {
        return new LocalThreadScheduleTrigger(property.getPollTime(), property.getPollTimeUnit());
    }

    /**
     * 构建时序策略实例（兼容带参时间轮与无参 DelayQueue）
     *
     * @param property 配置属性
     * @return 时序策略
     */
    private ScheduleStrategy buildStrategy(ScheduleProperty property) {
        Class<? extends ScheduleStrategy> clazz = property.getStrategy();
        try {
            return clazz.getConstructor(long.class, int.class, int.class)
                .newInstance(property.getTickDuration(), property.getWheelSize(), property.getMaxTicksPerPoll());
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("无法实例化调度策略: " + clazz.getName(), ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法实例化调度策略: " + clazz.getName(), e);
        }
    }
}
