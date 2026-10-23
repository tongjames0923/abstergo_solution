package com.tbs.logging.config;

import com.tbs.app.service.log.ISystemLogService;
import com.tbs.common.constant.ProviderConstant;
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
import com.tbs.mq.v2.impl.InMemoryMqMessageMonopolizer;
import com.tbs.mq.v2.impl.InMemoryMqRecorder;
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
    // 说明：以下协作者均为进程内内存实现且刻意不加 @Component，集中由本 config 装配，避免与未来 DB/Redis 实现重复注册；
    // 记录层内存实现重启即清空，定位为链路验证/开发环境用，生产替换为 DB 记录器即可，消费上下文与各段无需改动。

    /**
     * 日志 v2 消息记录器（内存实现，可靠性真源）
     *
     * @return 记录器，作用域按 {@code LoggingRecordRabbitMqContext} 的上下文ID隔离
     */
    @Bean
    public IMqRecorder<Message, LoggingRecordForm> loggingRecordRecorder() {
        return new InMemoryMqRecorder<>();
    }

    /**
     * 日志 v2 消息单飞抢占器（内存实现）
     *
     * @return 抢占器，保证同一消息不被并行重处理
     */
    @Bean
    public IMqMessageMonopolizer loggingRecordMonopolizer() {
        return new InMemoryMqMessageMonopolizer();
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
     * 日志 v2 消息接收器（接收段），绑定内存记录器
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
