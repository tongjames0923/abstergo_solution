package com.tbs.web.result.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxResultBuilder;
import com.tbs.web.result.IResultCreator;

/**
 * 默认结果创建者
 *
 * @author jiangnan
 * @date 2022/01/05
 **/
public class DefaultResultCreateor implements IResultCreator {
    @Override
    public IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder, Object val) {
        return builder.create().appendField(AjaxResult.CODE_KEY, AjaxResult.DEFAULT_SUCCESS_CODE)
            .appendField(AjaxResult.MESSAGE_KEY, AjaxResult.DEFAULT_SUCCESS_MESSAGE)
            .appendField(AjaxResult.DATA_KEY, val).next();
    }
}
