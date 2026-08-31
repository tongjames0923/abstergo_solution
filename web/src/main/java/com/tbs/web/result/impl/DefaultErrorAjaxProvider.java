package com.tbs.web.result.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.core.Ordered;

/**
 * 默认错误处理 ajax 结果提供者
 * <p>
 * 兜底处理所有未被更具体 {@link IOnErrorAjaxProvider} 匹配的异常：以异常本地化消息（为空时回退到通用错误消息）构造错误响应。
 * 由于 {@code getOrder} 为最低优先级，仅在其它专门 provider 不支持时生效。
 * </p>
 *
 * @author tongj
 */
public class DefaultErrorAjaxProvider implements IOnErrorAjaxProvider<Throwable> {
    @Override
    public AjaxResult onError(Throwable throwable) {
        return AjaxUtil.getInstance()
            .error(StrUtil.emptyToDefault(throwable.getLocalizedMessage(), AjaxResult.getDefaultErrorMessage()), null);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}