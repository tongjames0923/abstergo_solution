package com.tbs.struct.tree.closure.impl.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 闭包表自动注册类
 *
 * @author tongj
 */


public class ClosureTableAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Closure Table Package";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return 闭包表树形结构功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.CLOSURE_TABLE;
    }

    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.struct.tree.closure"};
    }
}
