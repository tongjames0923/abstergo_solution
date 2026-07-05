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
 * 自管理CuratorFramework连接，通过 {@link #create(String)} 工厂方法创建。
 * ZNode结构：
 * <pre>
 * /tbs/trace/
 *   {rootTraceId}/
 *     {serviceName}_{timestamp}  →  TraceTreeFragment JSON
 * </pre>
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class ZkTraceStorage implements ITraceStorage {

    private static final String TRACE_BASE_PATH = "/tbs/trace";

    private CuratorFramework curatorFramework;

    @javax.annotation.Resource
    private TraceProperty traceProperty;

    public ZkTraceStorage() {
    }

    @PostConstruct
    public void init() {
        String zkAddress = normalizeAddress(traceProperty.getZkAddress());
        this.curatorFramework = CuratorFrameworkFactory.builder()
            .connectString(zkAddress)
            .sessionTimeoutMs(30000)
            .connectionTimeoutMs(15000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();
        curatorFramework.start();
        log.info("分布式trace ZooKeeper客户端已启动: {}", zkAddress);
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "127.0.0.1:2181";
        }
        // Curator connectString只接受 host:port，不接受 zookeeper:// 前缀
        if (address.startsWith("zookeeper://")) {
            return address.substring("zookeeper://".length());
        }
        if (address.startsWith("zk://")) {
            return address.substring("zk://".length());
        }
        return address;
    }

    @Override
    public void store(TraceTreeFragment fragment) {
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
            log.warn("trace片段写入ZK失败: rootTraceId={}, error={}", rootTraceId, e.getMessage());
            throw new RuntimeException("trace存储失败", e);
        }
    }

    @Override
    public List<TraceTreeFragment> load(String rootTraceId) {
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
}
