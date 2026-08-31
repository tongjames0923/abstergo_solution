package com.tbs.schedule.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * 调度包自动注册
 * <p>
 * 扫描 {@code com.tbs.schedule} 包下标注 {@code @Component}/{@code @Configuration} 的 Bean，
 * 并关联 ASYNC 功能模块（调度包隶属 async 模块）。
 * 内核等需装配参数的 Bean 由 {@link ScheduleConfig} 统一提供。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ScheduleAutoRegister extends AbstractAutoBeanRegister {

    @Override
    protected String getPackName() {
        return "Schedule Package";
    }

    @Override
    protected Feature getFeature() {
        return Feature.ASYNC;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.schedule"};
    }
}
