package com.tbs.push.config;

import com.tbs.push.api.auth.IPushAuthenticator;
import com.tbs.push.api.auth.IPushCredentialStore;
import com.tbs.push.api.auth.IPushTopicGranter;
import com.tbs.push.api.coordinator.IRouteCoordinator;
import com.tbs.push.api.dispatch.IPushDispatcher;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.intercept.IConnectionInterceptor;
import com.tbs.push.api.record.IDeliveryRecordStore;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.session.ISubscriptionAuthorizer;
import com.tbs.push.api.session.ITopicRegistry;
import com.tbs.push.api.session.IUploadGuard;
import com.tbs.push.api.store.IPushMessageStore;
import com.tbs.push.api.transport.IPushEndpoint;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.api.transport.IUpstreamListener;
import com.tbs.push.config.property.PushAuthProperty;
import com.tbs.push.config.property.PushDispatchProperty;
import com.tbs.push.config.property.PushProperty;
import com.tbs.push.impl.auth.NoopAuthenticator;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.impl.endpoint.*;
import com.tbs.push.impl.protocol.JsonPushCodec;
import com.tbs.push.impl.protocol.PushFrameProcessor;
import com.tbs.push.impl.session.DefaultSessionRegistry;
import com.tbs.push.impl.session.DefaultTopicRegistry;
import com.tbs.push.impl.session.HeartbeatMonitor;
import com.tbs.push.impl.transport.PushConnectionOrchestrator;
import com.tbs.push.impl.transport.PushGracefulShutdownLifecycle;
import com.tbs.push.impl.store.InMemoryCredentialStore;
import com.tbs.push.impl.store.InMemoryDeliveryRecordStore;
import com.tbs.push.impl.store.InMemoryPushMessageStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

/**
 * 推送抽象层默认组件装配
 * <p>
 * 为推送协议层提供开箱即用的默认装配（总开关tbs.push.enable=true时生效），全部允许被使用方覆盖：
 * <ul>
 *     <li>{@link IPushCodec} — JSON编解码器</li>
 *     <li>{@link ISessionRegistry} / {@link ITopicRegistry} — 会话与订阅注册表</li>
 *     <li>{@link IPushMessageStore} / {@link IDeliveryRecordStore} — 懒委托存储：
 *     运行期优先透传真实实现，缺失时退化为内存兜底</li>
 *     <li>{@link IRouteCoordinator} — topic直配投递协调器</li>
 *     <li>{@link IConnectionInterceptor} — 拦截器链（默认无校验，安全验证由业务方按需扩展）</li>
 *     <li>{@link IPushDispatcher} / {@link IPushEndpoint} — 分发器与统一门面</li>
 *     <li>心跳监控与调度驱动（embedded/job互斥二选一）</li>
 * </ul>
 * 具体传输适配器由对应impl模块自行装配。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({PushProperty.class, PushDispatchProperty.class, PushAuthProperty.class})
public class PushSupportConfig {

    /**
     * 默认JSON协议编解码器
     *
     * @return JSON编解码器
     */
    @Bean
    @ConditionalOnMissingBean(IPushCodec.class)
    public IPushCodec pushCodec() {
        return new JsonPushCodec();
    }

    /**
     * 连接认证器（默认放行）
     * <p>
     * 协议层不内置具体鉴权：容器无认证器实现时放行并透传用户标识，
     * 业务方注册自定义IPushAuthenticator Bean即接入真实鉴权。
     * </p>
     *
     * @return 连接认证器
     */
    @Bean
    @ConditionalOnMissingBean(IPushAuthenticator.class)
    public IPushAuthenticator pushAuthenticator() {
        return new NoopAuthenticator();
    }

    /**
     * 连接凭证存储懒委托装配
     * <p>
     * 真实存储实现（如Redis）无论何时由哪个模块注册均在运行期优先生效，
     * 全部缺失时才表现为内存兜底行为。
     * </p>
     *
     * @param storeProvider 凭证存储实现提供者
     * @param authProperty  连接认证配置属性
     * @return 懒委托凭证存储
     */
    @Bean
    @ConditionalOnMissingBean(IPushCredentialStore.class)
    public IPushCredentialStore pushCredentialStore(ObjectProvider<IPushCredentialStore> storeProvider,
        PushAuthProperty authProperty) {
        return new DelegatingCredentialStore(storeProvider,
            new InMemoryCredentialStore(authProperty.getCredentialCapacity()));
    }

    /**
     * 连接凭证服务（统一连接建立流程的协议层核心）
     *
     * @param pushAuthenticator 连接认证器
     * @param credentialStore   连接凭证存储
     * @param granterProvider   主题授权器链提供者（无实现Bean时不计算授权集）
     * @param authProperty      连接认证配置属性
     * @return 连接凭证服务
     */
    @Bean
    @ConditionalOnMissingBean(PushCredentialService.class)
    public PushCredentialService pushCredentialService(IPushAuthenticator pushAuthenticator,
        IPushCredentialStore credentialStore, ObjectProvider<IPushTopicGranter> granterProvider,
        PushAuthProperty authProperty) {
        return new PushCredentialService(pushAuthenticator, credentialStore,
            granterProvider.stream().collect(Collectors.toList()), authProperty.getTicketTtlMillis());
    }

    /**
     * 默认会话注册表
     *
     * @return 会话注册表
     */
    @Bean
    @ConditionalOnMissingBean(ISessionRegistry.class)
    public ISessionRegistry sessionRegistry() {
        return new DefaultSessionRegistry();
    }

    /**
     * 默认订阅注册表
     *
     * @return 订阅注册表
     */
    @Bean
    @ConditionalOnMissingBean(ITopicRegistry.class)
    public ITopicRegistry topicRegistry() {
        return new DefaultTopicRegistry();
    }

    /**
     * 推送消息源懒委托装配
     * <p>
     * 不在装配期固化内存兜底：真实存储实现（无论何时由哪个模块注册）均在运行期优先生效，
     * 全部缺失时才表现为内存兜底行为。
     * </p>
     *
     * @param storeProvider 存储实现提供者
     * @param property      推送基础配置属性
     * @return 懒委托消息源
     */
    @Bean
    @ConditionalOnMissingBean(IPushMessageStore.class)
    public IPushMessageStore pushMessageStore(ObjectProvider<IPushMessageStore> storeProvider, PushProperty property) {
        return new DelegatingPushMessageStore(storeProvider,
            new InMemoryPushMessageStore(property.getMemoryMessageCapacity()));
    }

    /**
     * 投递记录存储懒委托装配
     *
     * @param storeProvider 记录存储实现提供者
     * @param property      推送基础配置属性
     * @return 懒委托投递记录存储
     */
    @Bean
    @ConditionalOnMissingBean(IDeliveryRecordStore.class)
    public IDeliveryRecordStore deliveryRecordStore(ObjectProvider<IDeliveryRecordStore> storeProvider,
        PushProperty property) {
        return new DelegatingDeliveryRecordStore(storeProvider,
            new InMemoryDeliveryRecordStore(property.getMemoryRecordCapacity()));
    }

    /**
     * 默认topic直配投递协调器
     *
     * @param sessionRegistry 会话注册表
     * @param topicRegistry   订阅注册表
     * @return 投递协调器
     */
    @Bean
    @ConditionalOnMissingBean(IRouteCoordinator.class)
    public IRouteCoordinator routeCoordinator(ISessionRegistry sessionRegistry, ITopicRegistry topicRegistry) {
        return new TopicRouteCoordinator(sessionRegistry, topicRegistry);
    }

    /**
     * 协议帧处理器
     * <p>
     * 协议层控制帧的统一处理中枢，全部传输适配层共用，保证协议行为跨传输一致。
     * 订阅授权校验器与上行守卫链由业务方按需注册，缺失时均全放行（历史行为）。
     * </p>
     *
     * @param codec                 协议编解码器
     * @param topicRegistry         订阅注册表
     * @param listenerProvider      上行监听器实现提供者
     * @param authorizerProvider    订阅授权校验器提供者（多实现时取唯一Bean，无实现时全放行）
     * @param guardProvider         上行守卫实现提供者（多实现链式叠加，无实现时全放行）
     * @return 协议帧处理器
     */
    @Bean
    @ConditionalOnMissingBean(PushFrameProcessor.class)
    public PushFrameProcessor pushFrameProcessor(IPushCodec codec, ITopicRegistry topicRegistry,
        ObjectProvider<IUpstreamListener> listenerProvider, ObjectProvider<ISubscriptionAuthorizer> authorizerProvider,
        ObjectProvider<IUploadGuard> guardProvider) {
        return new PushFrameProcessor(codec, topicRegistry, listenerProvider.stream().collect(Collectors.toList()),
            authorizerProvider.getIfAvailable(), guardProvider.stream().collect(Collectors.toList()));
    }

    /**
     * 推送消息分发器
     *
     * @param routeCoordinator    投递协调器
     * @param codec               协议编解码器
     * @param messageStore        消息源
     * @param deliveryRecordStore 投递记录存储
     * @param dispatchProperty    分发调度配置属性
     * @return 消息分发器
     */
    @Bean
    @ConditionalOnMissingBean(IPushDispatcher.class)
    public IPushDispatcher pushDispatcher(IRouteCoordinator routeCoordinator, IPushCodec codec,
        IPushMessageStore messageStore, IDeliveryRecordStore deliveryRecordStore,
        PushDispatchProperty dispatchProperty) {
        return new DefaultPushDispatcher(routeCoordinator, codec, messageStore)
            .deliveryRecordStore(deliveryRecordStore)
            .fetchBatchSize(dispatchProperty.getBatchSize());
    }

    /**
     * 推送统一门面
     *
     * @param messageStore    消息源
     * @param dispatcher      消息分发器
     * @param codec           协议编解码器
     * @param sessionRegistry 会话注册表
     * @return 推送统一门面
     */
    @Bean
    @ConditionalOnMissingBean(IPushEndpoint.class)
    public IPushEndpoint pushEndpoint(IPushMessageStore messageStore, IPushDispatcher dispatcher, IPushCodec codec,
        ISessionRegistry sessionRegistry) {
        return new DefaultPushEndpoint(messageStore, dispatcher, codec, sessionRegistry);
    }

    /**
     * 连接生命周期编排器（跨传输统一的建立/上行/清理流程中枢）
     * <p>
     * 拦截器链、注册表操作与清理链的唯一实现点，传输适配器内部共享；
     * 适配器只保留物理通道包装与契约转发，避免两份实现行为分叉。
     * </p>
     *
     * @param sessionRegistry   会话注册表
     * @param topicRegistry     订阅注册表
     * @param interceptorChain  连接拦截器链提供者
     * @param frameProcessor    协议帧处理器
     * @param credentialService 连接凭证服务
     * @return 连接生命周期编排器
     */
    @Bean
    @ConditionalOnMissingBean(PushConnectionOrchestrator.class)
    public PushConnectionOrchestrator pushConnectionOrchestrator(ISessionRegistry sessionRegistry,
        ITopicRegistry topicRegistry, ObjectProvider<IConnectionInterceptor> interceptorChain,
        PushFrameProcessor frameProcessor, PushCredentialService credentialService) {
        return new PushConnectionOrchestrator(sessionRegistry, topicRegistry, interceptorChain, frameProcessor,
            credentialService);
    }

    /**
     * 推送连接优雅停机器（跨传输统一）
     * <p>
     * 以最高相位接入容器关闭：停机时先广播SHUTDOWN通知帧、留宽限期后全量关闭，
     * 且早于Web服务器优雅停机排水，避免长连接把停机拖满超时。
     * </p>
     *
     * @param orchestrator 连接生命周期编排器
     * @param property     推送基础配置属性
     * @return 优雅停机器
     */
    @Bean
    @ConditionalOnMissingBean(PushGracefulShutdownLifecycle.class)
    public PushGracefulShutdownLifecycle pushGracefulShutdownLifecycle(PushConnectionOrchestrator orchestrator,
        PushProperty property) {
        return new PushGracefulShutdownLifecycle(orchestrator, property.getShutdownGraceMillis());
    }

    /**
     * 心跳空闲超时监控器
     * <p>
     * 超时配置为非正数时监控器启动后不做任何动作；超时会话优先经传输适配器
     * 主动关闭（清理链完整闭环），适配器缺失时退化为基线关闭与注销。
     * </p>
     *
     * @param sessionRegistry  会话注册表
     * @param property         推送基础配置属性
     * @param transportProvider 传输适配器提供者
     * @return 心跳监控器
     */
    @Bean
    @ConditionalOnMissingBean(HeartbeatMonitor.class)
    public HeartbeatMonitor heartbeatMonitor(ISessionRegistry sessionRegistry, PushProperty property,
        ObjectProvider<ITransportAdapter> transportProvider) {
        return new HeartbeatMonitor(sessionRegistry, property.getHeartbeatTimeoutMillis(), transportProvider);
    }

    /**
     * 内嵌式分发调度器（默认驱动）
     * <p>
     * 调度间隔取自PushDispatchProperty。互斥判定用@ConditionalOnProperty而非属性Bean，
     * 因条件在Bean定义阶段评估，早于配置属性绑定。
     * </p>
     *
     * @param dispatcher      消息分发器
     * @param dispatchProperty 分发调度配置属性
     * @return 内嵌式分发调度器
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push.dispatch", name = "scheduler",
        havingValue = PushDispatchProperty.SCHEDULER_EMBEDDED, matchIfMissing = true)
    @ConditionalOnMissingBean(EmbeddedPushScheduler.class)
    public EmbeddedPushScheduler embeddedPushScheduler(IPushDispatcher dispatcher,
        PushDispatchProperty dispatchProperty) {
        return new EmbeddedPushScheduler(dispatcher, dispatchProperty.getIntervalMillis());
    }

    /**
     * 分发后台任务桥接（外部调度驱动）
     * <p>
     * 配置tbs.push.dispatch.scheduler=job时启用，与内嵌调度器互斥，
     * 交由xxl-job等外部调度平台集中触发。
     * </p>
     *
     * @param dispatcher 消息分发器
     * @return 后台任务桥接
     */
    @Bean
    @ConditionalOnProperty(prefix = "tbs.push.dispatch", name = "scheduler",
        havingValue = PushDispatchProperty.SCHEDULER_JOB)
    @ConditionalOnMissingBean(PushDispatchBackgroundJob.class)
    public PushDispatchBackgroundJob pushDispatchBackgroundJob(IPushDispatcher dispatcher) {
        return new PushDispatchBackgroundJob(dispatcher);
    }

}
