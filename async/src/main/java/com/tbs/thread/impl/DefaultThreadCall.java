package com.tbs.thread.impl;

import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.IThreadRequest;
import org.slf4j.Logger;

/**
 * 默认线程调用
 *
 * @author tongj
 */
public class DefaultThreadCall implements IThreadRequest.IThreadCall {
    private Runnable runnable;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultThreadCall.class);

    public DefaultThreadCall(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void call(IThreadRequest request, IThreadPoolManager threadPoolManager) {
        log.trace("DefaultThreadCall call with {} at {}", request, threadPoolManager);
        runnable.run();
    }
}
