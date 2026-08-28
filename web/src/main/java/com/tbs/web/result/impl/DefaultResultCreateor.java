package com.tbs.web.result.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxResultBuilder;
import com.tbs.web.result.IResultCreator;

/**
 * 默认结果创建者
 * <p>
 * {@link IResultCreator} 的默认实现：将控制器返回值包装为成功响应（固定成功码 + 通用成功消息 + data 载荷）。
 * 由 {@code Wrapped} 注解默认引用。
 * </p>
 *
 * @author jiangnan
 * @date 2022/01/05
 */
public class DefaultResultCreateor implements IResultCreator {
    @Override
    public IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder, Object val) {
        return builder.create().appendField(AjaxResult.CODE_KEY, AjaxResult.DEFAULT_SUCCESS_CODE)
            .appendField(AjaxResult.MESSAGE_KEY, AjaxResult.getDefaultSuccessMessage())
            .appendField(AjaxResult.DATA_KEY, val).next();
    }
}