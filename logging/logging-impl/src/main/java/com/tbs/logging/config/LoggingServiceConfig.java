package com.tbs.logging.config;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.common.data.ITableDataHandler;
import com.tbs.logging.core.ILogExtractor;
import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.logging.impl.DbLogExtractor;
import com.tbs.logging.impl.RemoteTableDataOperator;
import com.tbs.logging.impl.SimpleSystemLogServiceImpl;
import com.tbs.logging.v2.LoggingRecordConsumer;
import com.tbs.logging.v2.LoggingRecordMessageProcessor;
import com.tbs.logging.v2.LoggingRecordMessageReceiver;
import com.tbs.logging.v2.LoggingRecordRabbitMqContext;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.core.IMqMessageMonopolizer;
import com.tbs.mq.v2.core.IMqMessageProcessor;
import com.tbs.mq.v2.core.IMqMessageReceiver;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.impl.InMemoryConsumerRegistry;
import com.tbs.redis.mq.RedisMqMessageMonopolizer;
import com.tbs.redis.mq.RedisMqRecorder;
import com.tbs.shared.mapper.SystemOperateMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.amqp.core.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class LoggingServiceConfig {
    @Bean
    public ILogExtractor logExtractor() {
        return new DbLogExtractor();
    }

    @Bean
    @DubboService
    public ISystemLogService logService() {
        return new SimpleSystemLogServiceImpl();
    }

    /**
     * 远程表格数据操作器
     * <p>
     * 导出 {@link ITableDataHandler} 供主应用经 IObjectProvider 多来源枚举发现，
     * 验证「本地 Bean + 远程 Dubbo 实例」统一处理链路。
     * </p>
     *
     * @return 表格数据处理器远程实现
     */
    @Bean
    @DubboService
    public ITableDataHandler remoteTableDataOperator() {
        return new RemoteTableDataOperator();
    }

    // ==================== MQ v2 记录驱动模型 · 日志消费链路（v1 DbLogWriter 已下线，v2 全面接管） ====================
    // 说明：以下协作者刻意不加 @Component，集中由本 config 装配，避免与框架侧扫描注册重复；
    // 记录器与单飞抢占器选用 Redis 实现（主记录/台账跨实例共享、单飞跨实例互斥），重启不丢消息；
    // 消费者注册表仍为进程内实现（注册的是宿主消费者对象，无法跨实例序列化）。

    /**
     * 日志 v2 消息记录器（Redis 实现，可靠性真源）
     *
     * @return 记录器，主记录与台账落 Redis，作用域按 {@code LoggingRecordRabbitMqContext} 的上下文ID隔离
     */
    @Bean
    public IMqRecorder<Message, LoggingRecordForm> loggingRecordRecorder() {
        return new RedisMqRecorder<>();
    }

    /**
     * 日志 v2 消息单飞抢占器（Redis 实现，保证同一消息在集群内不被并行重处理）
     *
     * @return 抢占器，按上下文ID + messageId 加分布式锁
     */
    @Bean
    public IMqMessageMonopolizer loggingRecordMonopolizer() {
        return new RedisMqMessageMonopolizer();
    }

    /**
     * 日志 v2 消费者注册表（内存实现，按负载类型路由消费者）
     *
     * @return 注册表
     */
    @Bean
    public IConsumerRegistry loggingRecordConsumerRegistry() {
        return new InMemoryConsumerRegistry();
    }

    /**
     * 日志 v2 消息接收器（接收段），绑定 Redis 记录器
     *
     * @param loggingRecordRecorder 日志记录器
     * @return 接收器
     */
    @Bean
    public IMqMessageReceiver<Message, LoggingRecordForm> loggingRecordReceiver(
        IMqRecorder<Message, LoggingRecordForm> loggingRecordRecorder) {
        return new LoggingRecordMessageReceiver(loggingRecordRecorder);
    }

    /**
     * 日志 v2 消息分发器（分发段），消费者注册表由父类字段按类型注入
     *
     * @return 分发器
     */
    @Bean
    public IMqMessageProcessor<Message, LoggingRecordForm> loggingRecordProcessor() {
        return new LoggingRecordMessageProcessor();
    }

    /**
     * 日志 v2 消费者（承载缓冲+批量入库），创建即注册进注册表，供分发段按负载类型回查
     *
     * @param mapper                  日志数据访问
     * @param loggingRecordConsumerRegistry 消费者注册表
     * @return 日志消费者
     */
    @Bean
    public IMqConsumer<LoggingRecordForm> loggingRecordConsumer(SystemOperateMapper mapper,
        IConsumerRegistry loggingRecordConsumerRegistry) {
        LoggingRecordConsumer consumer = new LoggingRecordConsumer(mapper);
        loggingRecordConsumerRegistry.register(consumer, LoggingRecordForm.class);
        return consumer;
    }

    /**
     * 日志 v2 消费上下文：自装配监听容器 + 建档确认全链路，接管原队列 {@code systemLog}
     *
     * @return 消费上下文
     */
    @Bean
    @DependsOn("loggingRecordConsumer")
    public LoggingRecordRabbitMqContext loggingRecordContext() {
        return new LoggingRecordRabbitMqContext();
    }
}
