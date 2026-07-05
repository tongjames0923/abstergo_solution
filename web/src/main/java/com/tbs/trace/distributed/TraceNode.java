package com.tbs.trace.distributed;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 可序列化的链路追踪节点
 * <p>
 * 用于跨服务传输和持久化存储，替代含有不可序列化字段的TraceObject
 * </p>
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
public class TraceNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点traceId
     */
    private String traceId;

    /**
     * 父级traceId
     */
    private String parentTraceId;

    /**
     * 方法签名（类名.方法名）
     */
    private String methodSignature;

    /**
     * 调用的接口类名
     */
    private String interfaceName;

    /**
     * 节点生效时间
     */
    private Date availableTime;

    /**
     * 节点失效时间
     */
    private Date disposeTime;

    /**
     * 是否为Dubbo远程调用节点
     */
    private boolean dubboCall;

    /**
     * 子节点列表
     */
    private List<TraceNode> children = new ArrayList<>();

    public TraceNode(String traceId, String parentTraceId) {
        this.traceId = traceId;
        this.parentTraceId = parentTraceId;
    }

    /**
     * 添加子节点
     */
    public void addChild(TraceNode child) {
        this.children.add(child);
    }

    /**
     * 获取方法的完整签名（类名.方法名）
     */
    public String getFullSignature() {
        if (interfaceName != null && methodSignature != null) {
            return interfaceName + "." + methodSignature;
        }
        return methodSignature;
    }
}
