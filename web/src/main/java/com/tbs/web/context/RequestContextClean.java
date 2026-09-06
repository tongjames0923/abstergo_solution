package com.tbs.web.context;

import org.springframework.stereotype.Component;

/**
 * 请求上下文清理器
 * <p>
 * 清理 {@link RequestContextHolder} 持有的请求上下文，防止容器线程
 * （线程池复用）残留上一请求数据导致的上下文串台（traceId、URI 错乱）
 * 与内存泄漏。
 * </p>
 *
 * @author abstergo
 * @see IContextClean
 * @see RequestContextHolder
 */
@Component
public class RequestContextClean implements IContextClean {

    @Override
    public void clean() {
        RequestContextHolder.clear();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
