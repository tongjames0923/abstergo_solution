package com.tbs.web.result.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.core.Ordered;

/**
 * 默认错误处理
 *
 * @author tongj
 */
public class DefaultErrorAjaxProvider implements IOnErrorAjaxProvider<Throwable> {
    @Override
    public AjaxResult onError(Throwable throwable) {
        return AjaxUtil.getInstance()
            .error(StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.DEFAULT_ERROR_MESSAGE), null);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
