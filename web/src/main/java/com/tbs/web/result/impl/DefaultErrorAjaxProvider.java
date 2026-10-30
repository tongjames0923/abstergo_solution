package com.tbs.web.result.impl;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IOnErrorAjaxProvider;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.core.Ordered;

/**
 * 默认错误处理 ajax 结果提供者
 * <p>
 * 兜底处理所有未被更具体 {@link IOnErrorAjaxProvider} 匹配的异常，经 {@link AjaxUtil#resolveErrorMessage(Throwable)}
 * 统一取文案：自建异常（AssertUtil 构建）的消息即对外文案直接回传，其余异常回退通用提示，
 * 避免 SQL、连接串、内部类名等实现细节经响应体泄露。
 * 由于 {@code getOrder} 为最低优先级，仅在其它专门 provider 不支持时生效。
 * </p>
 *
 * @author tongj
 */
public class DefaultErrorAjaxProvider implements IOnErrorAjaxProvider<Throwable> {
    @Override
    public AjaxResult onError(Throwable throwable) {
        return AjaxUtil.getInstance().error(AjaxUtil.resolveErrorMessage(throwable), null);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}