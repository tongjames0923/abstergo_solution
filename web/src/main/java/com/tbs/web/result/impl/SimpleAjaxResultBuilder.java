package com.tbs.web.result.impl;

import com.tbs.common.util.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxResultBuilder;

/**
 * 默认的ajax结果构造器
 * @author tongj
 */
public class SimpleAjaxResultBuilder implements IAjaxResultBuilder {
    private AjaxResult result;

    @Override
    public IAjaxResultBuilder create() {
        result = new AjaxResult();
        return this;
    }

    @Override
    public IAjaxResultBuilder appendField(String feild, Object value) {
        if (StrUtil.isEmpty(feild)) {
            return this;
        }
        result.getMap().put(feild, value);
        return this;
    }

    @Override
    public AjaxResult build() {
        var r = result;
        result = null;
        return r;
    }
}
