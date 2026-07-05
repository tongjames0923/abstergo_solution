package com.tbs.trace.core;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 方法追踪对象
 *
 * @author abstergo
 */
@Getter
public class MethodTraceObject extends TraceObject {
    /**
     * 构造方法
     *
     * @param traceId
     * @param method
     * @param args
     * @param target 调用对象
     */
    public MethodTraceObject(String traceId, Method method, Object[] args, Object target) {
        super(traceId);
        this.method = method;
        this.args = args;
        this.target = target;
    }

    /**
     * 方法
     */
    private Method method;
    /**
     * 方法参数
     */
    private Object[] args;

    /**
     * 调用对象
     */
    private Object target;

    public MethodTraceObject setMethod(Method method) {
        this.method = method;
        return this;
    }

    public MethodTraceObject setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public MethodTraceObject setTarget(Object target) {
        this.target = target;
        return this;
    }

    @Override
    public String toString() {
        return "MethodTraceObject{" +
            "method=" +
            method +
            ", args=" +
            Arrays.toString(args) +
            ", target=" +
            target +
            ", traceObject=" +
            super.toString() +
            '}';
    }
}
