package com.tbs.trace.distributed;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 链路追踪树片段
 * <p>
 * 表示单个服务内的完整trace调用树，用于跨服务传输和聚合
 * </p>
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
public class TraceTreeFragment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 根traceId（整个分布式链路的唯一标识）
     */
    private String rootTraceId;

    /**
     * 父级traceId（在分布式调用树中的挂载位置，来自RpcContext）
     */
    private String parentTraceId;

    /**
     * 服务名称/标识
     */
    private String serviceName;

    /**
     * 本地调用树的根节点
     */
    private TraceNode root;

    public TraceTreeFragment(String rootTraceId, String parentTraceId, String serviceName, TraceNode root) {
        this.rootTraceId = rootTraceId;
        this.parentTraceId = parentTraceId;
        this.serviceName = serviceName;
        this.root = root;
    }
}
