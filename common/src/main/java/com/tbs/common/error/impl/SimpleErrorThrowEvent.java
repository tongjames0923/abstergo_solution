package com.tbs.common.error.impl;

import com.tbs.common.error.IErrorThrowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的错误抛出事件实现
 *
 * @author tongj
 */
public class SimpleErrorThrowEvent implements IErrorThrowEvent {
    private static final Logger logger = LoggerFactory.getLogger(SimpleErrorThrowEvent.class);

    @Override
    public boolean onAssertUtilThrow(Throwable throwable) {
        logger.error("AssertUtil error", throwable);
        return false;
    }
}
