package com.tbs.trace.distributed;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * 分布式链路追踪聚合服务
 * <p>
 * 采用"本地缓冲 + 异步刷盘"模式：
 * <ul>
 *   <li>业务线程调用 {@link #publish} 仅写入 ConcurrentHashMap，立即返回，零阻塞</li>
 *   <li>后台守护线程定时将缓冲数据批量刷入 {@link ITraceStorage}</li>
 *   <li>查询时优先读缓冲，其次读存储</li>
 * </ul>
 * 线程安全：ConcurrentHashMap 的 remove() 是原子操作，flush 线程和业务线程无竞争。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DistributedTraceAggregator {

    private static final int MAX_BUFFER_SIZE = 10000;

    private final ITraceStorage storage;

    /**
     * 本地缓冲区：rootTraceId -> 该链路收到的所有片段
     * <p>写入由业务线程完成（publish），移除由flush线程完成（flushToStorage）</p>
     */
    private final ConcurrentHashMap<String, List<TraceTreeFragment>> fragmentBuffer = new ConcurrentHashMap<>();

    /** 待刷盘队列：flush线程从fragmentBuffer remove后放入此队列批量写入 */
    private final LinkedBlockingQueue<TraceTreeFragment> pendingQueue = new LinkedBlockingQueue<>(MAX_BUFFER_SIZE);

    /** 异步刷盘调度器 */
    private final ScheduledExecutorService flushScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "trace-flush");
            t.setDaemon(true);
            return t;
        });

    /** trace数据最大存活时间（毫秒） */
    private long maxAgeMs = 30 * 60 * 1000L;

    /** flush间隔（毫秒） */
    private long flushIntervalMs = 5000L;

    /** 清理间隔（毫秒） */
    private long cleanupIntervalMs = 10 * 60 * 1000L;

    public DistributedTraceAggregator(ITraceStorage storage) {
        this.storage = storage;
    }

    /**
     * 启动异步flush和清理守护线程
     */
    public void start() {
        // 异步刷盘：每5秒将缓冲数据批量写入存储
        flushScheduler.scheduleWithFixedDelay(this::flushToStorage, flushIntervalMs, flushIntervalMs,
            TimeUnit.MILLISECONDS);
        // 定时清理过期数据
        flushScheduler.scheduleWithFixedDelay(this::doCleanup, cleanupIntervalMs, cleanupIntervalMs,
            TimeUnit.MILLISECONDS);
        log.info("trace异步flush已启动: flushInterval={}ms, cleanupInterval={}ms", flushIntervalMs,
            cleanupIntervalMs);
    }

    /**
     * 配置参数
     */
    public void configure(long maxAgeMs, long cleanupIntervalMs) {
        this.maxAgeMs = maxAgeMs;
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    /**
     * 发布trace片段（业务线程调用，零阻塞）
     * <p>
     * 仅将片段放入本地缓冲区和待刷盘队列，由后台线程异步写入存储。
     * </p>
     */
    public void publish(TraceTreeFragment fragment) {
        if (fragment == null || fragment.getRootTraceId() == null) {
            return;
        }

        // 1. 写入本地缓冲（用于查询时的快速读取）
        fragmentBuffer.computeIfAbsent(fragment.getRootTraceId(), k -> new CopyOnWriteArrayList<>()).add(fragment);

        // 2. 放入待刷盘队列（非满则放入，满了则丢弃，不阻塞业务线程）
        if (!pendingQueue.offer(fragment)) {
            log.debug("trace待刷盘队列已满，丢弃片段: rootTraceId={}", fragment.getRootTraceId());
        }
    }

    /**
     * 获取完整的分布式调用树
     */
    public Optional<TraceNode> getDistributedTrace(String rootTraceId) {
        // 先从本地缓冲获取
        List<TraceTreeFragment> fragments = fragmentBuffer.get(rootTraceId);
        if (fragments != null && !fragments.isEmpty()) {
            return Optional.ofNullable(stitchFragments(fragments));
        }

        // 从存储加载
        fragments = storage.load(rootTraceId);
        if (fragments != null && !fragments.isEmpty()) {
            fragmentBuffer.put(rootTraceId, new CopyOnWriteArrayList<>(fragments));
            return Optional.ofNullable(stitchFragments(fragments));
        }

        return Optional.empty();
    }

    /**
     * 获取所有rootTraceId列表（合并本地缓冲 + 存储）
     */
    public List<String> listTraceIds() {
        Set<String> ids = new LinkedHashSet<>(fragmentBuffer.keySet());
        ids.addAll(storage.listTraceIds());
        return new ArrayList<>(ids);
    }

    // ======================== 后台线程逻辑 ========================

    /**
     * 异步刷盘：批量取出队列中的片段，写入存储，同时清理已刷盘的buffer条目
     */
    private void flushToStorage() {
        if (pendingQueue.isEmpty()) {
            return;
        }

        // 批量取出（最多取100条，避免单次刷盘耗时过长）
        List<TraceTreeFragment> batch = new ArrayList<>(100);
        pendingQueue.drainTo(batch, 100);

        if (batch.isEmpty()) {
            return;
        }

        // 按rootTraceId分组后批量写入
        Map<String, List<TraceTreeFragment>> grouped = new HashMap<>();
        for (TraceTreeFragment fragment : batch) {
            grouped.computeIfAbsent(fragment.getRootTraceId(), k -> new ArrayList<>()).add(fragment);
        }

        for (Map.Entry<String, List<TraceTreeFragment>> entry : grouped.entrySet()) {
            for (TraceTreeFragment fragment : entry.getValue()) {
                try {
                    storage.store(fragment);
                } catch (Exception e) {
                    log.debug("trace异步flush写入失败: rootTraceId={}, error={}", fragment.getRootTraceId(),
                        e.getMessage());
                }
            }
        }

        log.debug("trace异步flush完成: 本次刷盘{}条", batch.size());
    }

    /**
     * 定时清理过期数据
     */
    private void doCleanup() {
        try {
            storage.cleanup(maxAgeMs);
        } catch (Exception e) {
            log.debug("trace数据清理异常", e);
        }
    }

    // ======================== 拼接逻辑 ========================

    private TraceNode stitchFragments(List<TraceTreeFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }

        // 按rootTraceId分组（理论上同一链路的片段共享rootTraceId）
        String rootTraceId = fragments.get(0).getRootTraceId();

        // 找到消费端片段（parentTraceId为空的是链路入口）
        TraceTreeFragment consumerFragment = fragments.stream()
            .filter(f -> f.getParentTraceId() == null || f.getParentTraceId().isEmpty())
            .findFirst()
            .orElse(fragments.get(0));

        // 以消费端的树为基础
        TraceNode baseTree = deepCopy(consumerFragment.getRoot());

        // 将所有Provider片段挂载到消费端树的对应节点上
        for (TraceTreeFragment fragment : fragments) {
            if (fragment == consumerFragment) {
                continue; // 跳过消费端自身
            }
            if (fragment.getParentTraceId() == null || fragment.getParentTraceId().isEmpty()) {
                continue; // 跳过没有挂载位置的片段
            }
            attachProviderTree(baseTree, fragment);
        }

        return baseTree;
    }

    /**
     * 将Provider片段的子树挂载到消费端树中traceId == parentTraceId的节点下
     */
    private void attachProviderTree(TraceNode consumerTree, TraceTreeFragment providerFragment) {
        String attachPointTraceId = providerFragment.getParentTraceId();
        TraceNode attachNode = findNodeByTraceId(consumerTree, attachPointTraceId);
        if (attachNode == null) {
            log.debug("消费端树中未找到挂载点: traceId={}, provider={}",
                attachPointTraceId, providerFragment.getServiceName());
            return;
        }

        // Provider的root节点是全局根的副本，跳过root，只挂载其子节点
        TraceNode providerRoot = providerFragment.getRoot();
        if (providerRoot != null && providerRoot.getChildren() != null) {
            for (TraceNode child : providerRoot.getChildren()) {
                attachNode.addChild(deepCopy(child));
            }
        }
    }

    /**
     * 递归查找指定traceId的节点
     */
    private TraceNode findNodeByTraceId(TraceNode node, String traceId) {
        if (node == null || traceId == null) {
            return null;
        }
        if (traceId.equals(node.getTraceId())) {
            return node;
        }
        if (node.getChildren() != null) {
            for (TraceNode child : node.getChildren()) {
                TraceNode found = findNodeByTraceId(child, traceId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private TraceNode deepCopy(TraceNode source) {
        if (source == null) {
            return null;
        }

        TraceNode copy = new TraceNode(source.getTraceId(), source.getParentTraceId());
        copy.setServiceName(source.getServiceName());
        copy.setInterfaceName(source.getInterfaceName());
        copy.setClassName(source.getClassName());
        copy.setMethodSignature(source.getMethodSignature());
        copy.setParamTypes(source.getParamTypes());
        copy.setArgsDescription(source.getArgsDescription());
        copy.setDubboCall(source.isDubboCall());
        copy.setThreadName(source.getThreadName());
        copy.setThreadId(source.getThreadId());
        copy.setContextId(source.getContextId());
        copy.setHttpMethod(source.getHttpMethod());
        copy.setHttpUrl(source.getHttpUrl());
        copy.setHttpStatus(source.getHttpStatus());
        copy.setRemoteAddr(source.getRemoteAddr());
        copy.setUserAgent(source.getUserAgent());
        copy.setContentType(source.getContentType());
        copy.setQueryString(source.getQueryString());
        copy.setExceptionMessage(source.getExceptionMessage());
        copy.setExceptionType(source.getExceptionType());
        copy.setAvailableTime(source.getAvailableTime());
        copy.setDisposeTime(source.getDisposeTime());

        if (source.getChildren() != null) {
            for (TraceNode child : source.getChildren()) {
                copy.addChild(deepCopy(child));
            }
        }

        return copy;
    }
}
