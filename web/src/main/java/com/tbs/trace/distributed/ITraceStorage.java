package com.tbs.trace.distributed;

import java.util.List;

/**
 * 分布式链路追踪存储策略接口
 * <p>
 * 定义trace片段的持久化和查询能力，支持多种存储后端（ZooKeeper、数据库等）。
 * </p>
 *
 * @author abstergo
 */
public interface ITraceStorage {

    /**
     * 存储trace片段
     *
     * @param fragment trace片段
     */
    void store(TraceTreeFragment fragment);

    /**
     * 加载指定traceId的所有片段
     *
     * @param rootTraceId 根traceId
     * @return 该链路的所有片段列表，不存在时返回null
     */
    List<TraceTreeFragment> load(String rootTraceId);

    /**
     * 清理过期的trace数据
     *
     * @param maxAgeMs 最大存活时间（毫秒）
     */
    void cleanup(long maxAgeMs);
}
