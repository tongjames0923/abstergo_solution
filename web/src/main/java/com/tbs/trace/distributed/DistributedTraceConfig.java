package com.tbs.trace.distributed;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.trace.config.TraceProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 分布式链路追踪配置
 * <p>
 * 通过 {@link TraceProperty#traceStorage} 配置存储实现类，默认 ZooKeeper。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@Configuration
public class DistributedTraceConfig {

    /** trace数据最大存活时间（毫秒），默认30分钟 */
    private static final long TRACE_MAX_AGE_MS = 30 * 60 * 1000L;

    /** 清理间隔（毫秒），默认10分钟 */
    private static final long CLEANUP_INTERVAL_MS = 10 * 60 * 1000L;

    @Resource
    private TraceProperty traceProperty;

    /**
     * 分布式trace存储实现
     * <p>
     * 通过 tbs.trace.trace-storage 配置实现类，默认 ZkTraceStorage。
     * </p>
     */
    @Bean
    public ITraceStorage traceStorage() {
        return BeanUtil.buildBeanFromProperties(ZkTraceStorage::new, traceProperty.getTraceStorage(),
            BeanUtil::buildByEmptyArgs);
    }

    /**
     * 分布式trace聚合服务
     */
    @Bean
    public DistributedTraceAggregator distributedTraceAggregator(ITraceStorage traceStorage) {
        DistributedTraceAggregator aggregator = new DistributedTraceAggregator(traceStorage);
        aggregator.configure(TRACE_MAX_AGE_MS, CLEANUP_INTERVAL_MS);
        aggregator.start();
        log.info("分布式链路追踪已启用（maxAge={}ms, interval={}ms）", TRACE_MAX_AGE_MS, CLEANUP_INTERVAL_MS);
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
