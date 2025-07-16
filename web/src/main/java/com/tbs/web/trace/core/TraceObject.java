package com.tbs.web.trace.core;

import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * 链路追踪ID
 *
 * @author abstergo
 */
@Getter
public class TraceObject implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 链路追踪ID当前是否有效
     */
    private boolean valid = true;

    /**
     * 链路追踪ID对应的上下文ID,可能是线程ID也可能是sessionId之类的
     */
    private String contextId;

    /**
     * 链路追踪ID对应的上下文ID对应的生效时间
     */
    private Date avaliableTime;

    /**
     * 链路追踪ID对应的上下文ID对应的失效时间
     */
    private Date disposeTime;

    /**
     * 父级调用Id
     */
    private String parentTraceId;

    public TraceObject(String traceId) {
        this.traceId = traceId;
    }

    public TraceObject setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public TraceObject setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public TraceObject setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public TraceObject setAvaliableTime(Date avaliableTime) {
        this.avaliableTime = avaliableTime;
        return this;
    }

    public TraceObject setDisposeTime(Date disposeTime) {
        this.disposeTime = disposeTime;
        return this;
    }

    public TraceObject setParentTraceId(String parentTraceId) {
        this.parentTraceId = parentTraceId;
        return this;
    }

    @Override
    public String toString() {
        return "TraceObject{" +
            "traceId='" +
            traceId +
            '\'' +
            ", valid=" +
            valid +
            ", contextId='" +
            contextId +
            '\'' +
            ", avaliableTime=" +
            avaliableTime +
            ", disposeTime=" +
            disposeTime +
            ", parentTraceId='" +
            parentTraceId +
            '\'' +
            '}';
    }
}
