package com.tbs.struct.tree.closure.impl.config;

import com.tbs.common.AbstractAutoBeanRegister;
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

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.struct.tree.closure"};
    }
}
