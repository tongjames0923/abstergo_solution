package com.tbs.trace.distributed;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于ZooKeeper的分布式链路追踪聚合服务
 * <p>
 * 将各服务的TraceTreeFragment存储到ZooKeeper节点，通过ZNode层级结构聚合分布式调用树。
 * <p>
 * ZNode结构：
 * <pre>
 * /tbs/trace/
 *   {rootTraceId}/
 *     {serviceName}_{timestamp}  →  TraceTreeFragment JSON
 * </pre>
 * <p>
 * 每个服务在请求结束时将本地trace树写入ZK，查询时读取所有子节点并拼接为完整分布式树。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DistributedTraceAggregator {

    private static final String TRACE_BASE_PATH = "/tbs/trace";

    /**
     * 本地缓冲区：rootTraceId -> 该链路收到的所有片段（避免每次查询都读ZK）
     */
    private final ConcurrentHashMap<String, List<TraceTreeFragment>> fragmentBuffer = new ConcurrentHashMap<>();

    /**
     * 清理任务调度器，守护线程，随JVM退出
     */
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

    private CuratorFramework curatorFramework;

    /**
     * 设置CuratorFramework客户端
     *
     * @param curatorFramework ZooKeeper客户端
     */
    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    /**
     * 启动自动清理
     */
    public void start() {
        cleanupScheduler.scheduleWithFixedDelay(this::doCleanup, cleanupIntervalMs, cleanupIntervalMs,
            TimeUnit.MILLISECONDS);
        log.info("trace数据自动清理已启动: interval={}ms, maxAge={}ms", cleanupIntervalMs, maxAgeMs);
    }

    /**
     * 设置最大存活时间和清理间隔
     *
     * @param maxAgeMs       trace数据最大存活时间（毫秒）
     * @param cleanupIntervalMs 清理执行间隔（毫秒）
     */
    public void configure(long maxAgeMs, long cleanupIntervalMs) {
        this.maxAgeMs = maxAgeMs;
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    /**
     * 将trace片段写入ZooKeeper
     *
     * @param fragment 服务端trace片段
     */
    public void publish(TraceTreeFragment fragment) {
        if (fragment == null || fragment.getRootTraceId() == null) {
            return;
        }

        String rootTraceId = fragment.getRootTraceId();
        String nodePath = buildNodePath(rootTraceId, fragment.getServiceName());

        try {
            // 确保父节点存在
            String parentPath = buildTracePath(rootTraceId);
            if (curatorFramework.checkExists().forPath(parentPath) == null) {
                curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(parentPath, new byte[0]);
            }

            // 写入片段数据
            String json = JSON.toJSONString(fragment);
            if (curatorFramework.checkExists().forPath(nodePath) != null) {
                curatorFramework.setData().forPath(nodePath, json.getBytes(StandardCharsets.UTF_8));
            } else {
                curatorFramework.create()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(nodePath, json.getBytes(StandardCharsets.UTF_8));
            }

            // 同时更新本地缓冲
            fragmentBuffer.computeIfAbsent(rootTraceId, k -> new ArrayList<>()).add(fragment);

            log.debug("trace片段已写入ZK: rootTraceId={}, service={}", rootTraceId, fragment.getServiceName());
        } catch (Exception e) {
            log.warn("trace片段写入ZK失败: rootTraceId={}, error={}", rootTraceId, e.getMessage());
            // 降级到本地缓冲
            fragmentBuffer.computeIfAbsent(rootTraceId, k -> new ArrayList<>()).add(fragment);
        }
    }

    /**
     * 获取完整的分布式调用树
     *
     * @param rootTraceId 根traceId
     * @return 完整的TraceNode树，如果不存在则返回空
     */
    public Optional<TraceNode> getDistributedTrace(String rootTraceId) {
        // 先从本地缓冲获取
        List<TraceTreeFragment> fragments = fragmentBuffer.get(rootTraceId);
        if (fragments != null && !fragments.isEmpty()) {
            return Optional.ofNullable(stitchFragments(fragments));
        }

        // 从ZooKeeper获取
        try {
            fragments = loadFragmentsFromZk(rootTraceId);
            if (fragments != null && !fragments.isEmpty()) {
                fragmentBuffer.put(rootTraceId, fragments);
                return Optional.ofNullable(stitchFragments(fragments));
            }
        } catch (Exception e) {
            log.debug("从ZK获取trace失败: rootTraceId={}", rootTraceId, e);
        }

        return Optional.empty();
    }

    /**
     * 执行清理（内部调用）
     */
    private void doCleanup() {
        try {
            cleanup(maxAgeMs);
        } catch (Exception e) {
            log.debug("trace数据自动清理异常", e);
        }
    }

    /**
     * 清理过期的trace数据
     *
     * @param maxAgeMs 最大存活时间（毫秒）
     */
    public void cleanup(long maxAgeMs) {
        try {
            if (curatorFramework.checkExists().forPath(TRACE_BASE_PATH) == null) {
                return;
            }

            List<String> traceIds = curatorFramework.getChildren().forPath(TRACE_BASE_PATH);
            long now = System.currentTimeMillis();

            for (String traceId : traceIds) {
                String tracePath = TRACE_BASE_PATH + "/" + traceId;
                try {
                    List<String> children = curatorFramework.getChildren().forPath(tracePath);
                    if (children == null || children.isEmpty()) {
                        curatorFramework.delete().forPath(tracePath);
                        continue;
                    }

                    // 检查子节点是否都过期
                    boolean allExpired = true;
                    for (String child : children) {
                        String childPath = tracePath + "/" + child;
                        var stat = curatorFramework.checkExists().forPath(childPath);
                        if (stat != null && (now - stat.getMtime()) < maxAgeMs) {
                            allExpired = false;
                            break;
                        }
                    }

                    if (allExpired) {
                        curatorFramework.delete().deletingChildrenIfNeeded().forPath(tracePath);
                        fragmentBuffer.remove(traceId);
                        log.debug("已清理过期trace: rootTraceId={}", traceId);
                    }
                } catch (KeeperException.NoNodeException ignored) {
                    // 节点已被其他进程删除
                }
            }
        } catch (Exception e) {
            log.debug("trace数据清理异常", e);
        }
    }

    /**
     * 从ZooKeeper加载trace片段
     */
    private List<TraceTreeFragment> loadFragmentsFromZk(String rootTraceId) throws Exception {
        String tracePath = buildTracePath(rootTraceId);
        if (curatorFramework.checkExists().forPath(tracePath) == null) {
            return null;
        }

        List<String> children = curatorFramework.getChildren().forPath(tracePath);
        if (children == null || children.isEmpty()) {
            return null;
        }

        List<TraceTreeFragment> fragments = new ArrayList<>();
        for (String child : children) {
            String childPath = tracePath + "/" + child;
            try {
                byte[] data = curatorFramework.getData().forPath(childPath);
                if (data != null && data.length > 0) {
                    TraceTreeFragment fragment = JSON.parseObject(new String(data, StandardCharsets.UTF_8),
                        TraceTreeFragment.class);
                    if (fragment != null) {
                        fragments.add(fragment);
                    }
                }
            } catch (Exception e) {
                log.debug("读取trace片段失败: path={}", childPath, e);
            }
        }

        return fragments.isEmpty() ? null : fragments;
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

        // 建立 rootTraceId -> fragment 的映射
        Map<String, TraceTreeFragment> fragmentMap = new HashMap<>();
        for (TraceTreeFragment fragment : fragments) {
            fragmentMap.put(fragment.getRootTraceId(), fragment);
        }

        // 找到Consumer端的片段（parentTraceId为null或空）作为基础树
        TraceTreeFragment consumerFragment = fragments.stream()
            .filter(f -> f.getParentTraceId() == null || f.getParentTraceId().isEmpty())
            .findFirst()
            .orElse(fragments.get(0));

        // 复制Consumer树作为基础（避免修改原始数据）
        TraceNode baseTree = deepCopy(consumerFragment.getRoot());

        // 递归拼接Provider子树
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
            // 如果子节点是Dubbo调用节点，尝试查找对应的Provider片段
            if (child.isDubboCall()) {
                TraceTreeFragment providerFragment = fragmentMap.get(child.getTraceId());
                if (providerFragment != null) {
                    // 将Provider树挂载为该Dubbo节点的子节点
                    TraceNode providerRoot = deepCopy(providerFragment.getRoot());
                    child.addChild(providerRoot);
                }
            }

            // 递归处理子节点
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

    private String buildTracePath(String rootTraceId) {
        return TRACE_BASE_PATH + "/" + rootTraceId;
    }

    private String buildNodePath(String rootTraceId, String serviceName) {
        return TRACE_BASE_PATH + "/" + rootTraceId + "/" + serviceName + "_" + System.currentTimeMillis();
    }
}
