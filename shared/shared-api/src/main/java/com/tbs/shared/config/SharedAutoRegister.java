package com.tbs.shared.config;

import com.tbs.common.AbstractAutoBeanRegister;

/**
 * @author tongj
 */
public class SharedAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Shared Lib Package";
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.shared"};
    }
}
