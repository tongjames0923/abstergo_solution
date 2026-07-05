package com.tbs.trace.distributed;

import com.alibaba.fastjson2.JSON;
import com.tbs.trace.config.TraceProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于ZooKeeper的trace存储实现
 * <p>
 * 所有ZK操作均有超时保护，不会阻塞业务线程。初始化失败仅记录日志，不影响应用启动。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class ZkTraceStorage implements ITraceStorage {

    private static final String TRACE_BASE_PATH = "/tbs/trace";

    /** 单次ZK操作超时（毫秒） */
    private static final int OPERATION_TIMEOUT_MS = 3000;

    private volatile CuratorFramework curatorFramework;

    @javax.annotation.Resource
    private TraceProperty traceProperty;

    public ZkTraceStorage() {
    }

    @PostConstruct
    public void init() {
        try {
            String zkAddress = normalizeAddress(traceProperty.getZkAddress());
            this.curatorFramework = CuratorFrameworkFactory.builder().connectString(zkAddress).sessionTimeoutMs(10000)
                .connectionTimeoutMs(3000).retryPolicy(new ExponentialBackoffRetry(1000, 1)).build();
            curatorFramework.start();
            log.info("分布式trace ZooKeeper客户端已启动: {}", zkAddress);
        } catch (Exception e) {
            // ZK不可达时不阻塞应用启动，仅记录警告
            log.warn("分布式trace ZooKeeper客户端启动失败，trace存储功能降级: {}", e.getMessage());
            this.curatorFramework = null;
        }
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "127.0.0.1:2181";
        }
        if (address.startsWith("zookeeper://")) {
            return address.substring("zookeeper://".length());
        }
        if (address.startsWith("zk://")) {
            return address.substring("zk://".length());
        }
        return address;
    }

    /**
     * 检查ZK客户端是否可用
     */
    private boolean isAvailable() {
        return curatorFramework != null && curatorFramework.getZookeeperClient().isConnected();
    }

    @Override
    public void store(TraceTreeFragment fragment) {
        if (!isAvailable()) {
            return;
        }

        String rootTraceId = fragment.getRootTraceId();
        String nodePath = buildNodePath(rootTraceId, fragment.getServiceName());

        try {
            String parentPath = buildTracePath(rootTraceId);
            if (curatorFramework.checkExists().forPath(parentPath) == null) {
                curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(parentPath, new byte[0]);
            }

            String json = JSON.toJSONString(fragment);
            if (curatorFramework.checkExists().forPath(nodePath) != null) {
                curatorFramework.setData().forPath(nodePath, json.getBytes(StandardCharsets.UTF_8));
            } else {
                curatorFramework.create()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(nodePath, json.getBytes(StandardCharsets.UTF_8));
            }

            log.debug("trace片段已写入ZK: rootTraceId={}, service={}", rootTraceId, fragment.getServiceName());
        } catch (Exception e) {
            log.debug("trace片段写入ZK失败: rootTraceId={}, error={}", rootTraceId, e.getMessage());
        }
    }

    @Override
    public List<TraceTreeFragment> load(String rootTraceId) {
        if (!isAvailable()) {
            return null;
        }

        try {
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
                        TraceTreeFragment fragment = JSON.parseObject(
                            new String(data, StandardCharsets.UTF_8), TraceTreeFragment.class);
                        if (fragment != null) {
                            fragments.add(fragment);
                        }
                    }
                } catch (Exception e) {
                    log.debug("读取trace片段失败: path={}", childPath, e);
                }
            }

            return fragments.isEmpty() ? null : fragments;
        } catch (Exception e) {
            log.debug("从ZK加载trace失败: rootTraceId={}", rootTraceId, e);
            return null;
        }
    }

    @Override
    public void cleanup(long maxAgeMs) {
        if (!isAvailable()) {
            return;
        }

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
                        log.debug("已清理过期trace: rootTraceId={}", traceId);
                    }
                } catch (KeeperException.NoNodeException ignored) {
                }
            }
        } catch (Exception e) {
            log.debug("trace数据清理异常", e);
        }
    }

    private String buildTracePath(String rootTraceId) {
        return TRACE_BASE_PATH + "/" + rootTraceId;
    }

    private String buildNodePath(String rootTraceId, String serviceName) {
        return TRACE_BASE_PATH + "/" + rootTraceId + "/" + serviceName + "_" + System.currentTimeMillis();
    }

    @Override
    public List<String> listTraceIds() {
        if (!isAvailable()) {
            return java.util.Collections.emptyList();
        }
        try {
            if (curatorFramework.checkExists().forPath(TRACE_BASE_PATH) == null) {
                return java.util.Collections.emptyList();
            }
            List<String> children = curatorFramework.getChildren().forPath(TRACE_BASE_PATH);
            return children != null ? children : java.util.Collections.emptyList();
        } catch (Exception e) {
            log.debug("从ZK列出traceId失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
