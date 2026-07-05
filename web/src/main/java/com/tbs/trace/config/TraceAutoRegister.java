package com.tbs.trace.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

public class TraceAutoRegister extends AbstractAutoBeanRegister {
    public static final String PACK_NAME = "TRACE PACK";

    @Override
    protected String getPackName() {
        return "TRACE PACK";
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.trace"};
    }

    @Override
    protected Feature getFeature() {
        return Feature.TRACE;
    }
}
