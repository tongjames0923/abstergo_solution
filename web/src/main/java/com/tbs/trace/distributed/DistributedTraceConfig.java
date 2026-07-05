package com.tbs.trace.distributed;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式链路追踪配置
 * <p>
 * 基于ZooKeeper实现分布式trace数据存储和聚合。 每个服务在请求结束时将trace片段写入ZK节点，查询时读取并拼接为完整分布式树。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@Configuration
public class DistributedTraceConfig {

    /** trace数据在ZK中的最大存活时间（毫秒），默认30分钟 */
    private static final long TRACE_MAX_AGE_MS = 30 * 60 * 1000L;

    /** 清理间隔（毫秒），默认10分钟 */
    private static final long CLEANUP_INTERVAL_MS = 10 * 60 * 1000L;

    /**
     * 创建分布式trace专用的CuratorFramework客户端
     * <p>
     * 复用Dubbo的ZooKeeper地址，使用独立的连接与Dubbo自身的注册中心隔离。
     * </p>
     */
    @Bean
    public CuratorFramework traceCuratorFramework(
        @Value("${tbs.trace.zk-address:${ZOOKEEPER_ADDRESS:zookeeper://127.0.0.1:2181}}") String zkAddress) {
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkAddress).sessionTimeoutMs(30000)
            .connectionTimeoutMs(15000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
        log.info("分布式trace ZooKeeper客户端已启动: {}", zkAddress);
        return client;
    }

    /**
     * 分布式trace聚合服务（ZooKeeper实现）
     */
    @Bean
    public DistributedTraceAggregator distributedTraceAggregator(CuratorFramework traceCuratorFramework) {
        DistributedTraceAggregator aggregator = new DistributedTraceAggregator();
        aggregator.setCuratorFramework(traceCuratorFramework);
        aggregator.configure(TRACE_MAX_AGE_MS, CLEANUP_INTERVAL_MS);
        aggregator.start();
        log.info("分布式链路追踪已启用（基于ZooKeeper, maxAge={}ms, interval={}ms）", TRACE_MAX_AGE_MS,
            CLEANUP_INTERVAL_MS);
        return aggregator;
    }

    /**
     * 分布式trace监听器
     */
    @Bean
    public DistributedTraceListener distributedTraceListener() {
        return new DistributedTraceListener();
    }
}
