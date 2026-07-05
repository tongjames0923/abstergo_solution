package com.tbs.trace.dubbo;

/**
 * Dubbo Trace上下文持有者
 * <p>
 * 用于在Dubbo RPC调用中传递traceId相关的常量和工具方法
 * </p>
 *
 * @author abstergo
 */
public class DubboTraceContextHolder {

    /**
     * 根traceId的Attachment键
     */
    public static final String ROOT_TRACE_ID_KEY = "tbs.trace.rootId";

    /**
     * 当前traceId的Attachment键
     */
    public static final String CURRENT_TRACE_ID_KEY = "tbs.trace.currentId";

    /**
     * 私有构造函数，防止实例化
     */
    private DubboTraceContextHolder() {
    }
}