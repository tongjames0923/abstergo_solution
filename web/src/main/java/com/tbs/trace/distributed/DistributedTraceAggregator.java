package com.tbs.trace.distributed;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分布式链路追踪聚合服务
 * <p>
 * 负责trace片段的本地缓冲、拼接和清理调度。
 * 存储能力委托给 {@link ITraceStorage} 实现（ZooKeeper、数据库等）。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DistributedTraceAggregator {

    private final ITraceStorage storage;

    /** 本地缓冲区：rootTraceId -> 该链路收到的所有片段（避免频繁读存储） */
    private final ConcurrentHashMap<String, List<TraceTreeFragment>> fragmentBuffer = new ConcurrentHashMap<>();

    /** 清理任务调度器，守护线程，随JVM退出 */
    private final ScheduledExecutorService cleanupScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "trace-cleanup");
            t.setDaemon(true);
            return t;
        });

    /** trace数据最大存活时间（毫秒） */
    private long maxAgeMs = 30 * 60 * 1000L;

    /** 清理间隔（毫秒） */
    private long cleanupIntervalMs = 10 * 60 * 1000L;

    public DistributedTraceAggregator(ITraceStorage storage) {
        this.storage = storage;
    }

    /**
     * 启动自动清理守护线程
     */
    public void start() {
        cleanupScheduler.scheduleWithFixedDelay(this::doCleanup, cleanupIntervalMs, cleanupIntervalMs,
            TimeUnit.MILLISECONDS);
        log.info("trace数据自动清理已启动: interval={}ms, maxAge={}ms", cleanupIntervalMs, maxAgeMs);
    }

    /**
     * 设置最大存活时间和清理间隔
     */
    public void configure(long maxAgeMs, long cleanupIntervalMs) {
        this.maxAgeMs = maxAgeMs;
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    /**
     * 发布trace片段到存储
     */
    public void publish(TraceTreeFragment fragment) {
        if (fragment == null || fragment.getRootTraceId() == null) {
            return;
        }

        // 更新本地缓冲
        fragmentBuffer.computeIfAbsent(fragment.getRootTraceId(), k -> new ArrayList<>()).add(fragment);

        // 持久化到存储
        try {
            storage.store(fragment);
        } catch (Exception e) {
            log.warn("trace片段持久化失败，已降级到本地缓冲: rootTraceId={}", fragment.getRootTraceId());
        }
    }

    /**
     * 获取完整的分布式调用树
     *
     * @param rootTraceId 根traceId
     * @return 完整的TraceNode树，不存在时返回空
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
            fragmentBuffer.put(rootTraceId, fragments);
            return Optional.ofNullable(stitchFragments(fragments));
        }

        return Optional.empty();
    }

    /**
     * 执行清理（内部调用）
     */
    private void doCleanup() {
        try {
            storage.cleanup(maxAgeMs);
        } catch (Exception e) {
            log.debug("trace数据自动清理异常", e);
        }
    }

    /**
     * 将所有片段拼接成完整的分布式调用树
     *
     * <p>拼接算法：
     * 1. 找到Consumer端的片段（parentTraceId为null）作为基础树
     * 2. 遍历基础树中的每个节点
     * 3. 如果节点标记为Dubbo调用且在fragmentMap中有对应的Provider片段，
     *    将Provider片段的根节点插入为该Dubbo节点的子节点
     * 4. 递归处理插入的Provider子树中的Dubbo节点
     * </p>
     */
    private TraceNode stitchFragments(List<TraceTreeFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }

        Map<String, TraceTreeFragment> fragmentMap = new HashMap<>();
        for (TraceTreeFragment fragment : fragments) {
            fragmentMap.put(fragment.getRootTraceId(), fragment);
        }

        // 找到Consumer端的片段作为基础树
        TraceTreeFragment consumerFragment = fragments.stream()
            .filter(f -> f.getParentTraceId() == null || f.getParentTraceId().isEmpty())
            .findFirst()
            .orElse(fragments.get(0));

        TraceNode baseTree = deepCopy(consumerFragment.getRoot());
        stitchSubTrees(baseTree, fragmentMap);
        return baseTree;
    }

    /**
     * 递归将Provider子树挂载到Consumer的Dubbo调用节点上
     */
    private void stitchSubTrees(TraceNode node, Map<String, TraceTreeFragment> fragmentMap) {
        if (node == null || node.getChildren() == null) {
            return;
        }

        for (TraceNode child : node.getChildren()) {
            if (child.isDubboCall()) {
                TraceTreeFragment providerFragment = fragmentMap.get(child.getTraceId());
                if (providerFragment != null) {
                    child.addChild(deepCopy(providerFragment.getRoot()));
                }
            }
            stitchSubTrees(child, fragmentMap);
        }
    }

    /**
     * 深拷贝TraceNode树（避免修改原始数据）
     */
    private TraceNode deepCopy(TraceNode source) {
        if (source == null) {
            return null;
        }

        TraceNode copy = new TraceNode(source.getTraceId(), source.getParentTraceId());
        copy.setMethodSignature(source.getMethodSignature());
        copy.setInterfaceName(source.getInterfaceName());
        copy.setAvailableTime(source.getAvailableTime());
        copy.setDisposeTime(source.getDisposeTime());
        copy.setDubboCall(source.isDubboCall());

        if (source.getChildren() != null) {
            for (TraceNode child : source.getChildren()) {
                copy.addChild(deepCopy(child));
            }
        }

        return copy;
    }
}
