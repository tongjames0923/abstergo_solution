package com.tbs.thread.impl;

import com.tbs.thread.IOnThreadError;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的线程错误处理
 *
 * @author abstergo
 */
@Slf4j
public class DefaultOnThreadError implements IOnThreadError {
    @Override
    public void onThreadError(Throwable throwable, Thread at) {
        log.error(String.format("线程 %s 异常。Msg:%s", at.getName(), throwable.getLocalizedMessage()), throwable);
    }
}
