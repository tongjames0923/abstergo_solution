package com.tbs.async.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * 异步注册
 *
 * @author tongj
 */
public class AsyncAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Async Package";
    }

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.async", "com.tbs.broadcast", "com.tbs.lock", "com.tbs.schedule",
            "com.tbs.thread"};
    }
}
