package com.tbs.web.result.impl;

import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IAjaxResultBuilder;

/**
 * 默认的 ajax 结果构造提供者
 * <p>
 * 直接返回 {@link SimpleAjaxResultBuilder} 实例，作为 {@link IAjaxBuilderProvider} 的默认实现。
 * </p>
 *
 * @author tongj
 */
public class SimpleBuilderProvider implements IAjaxBuilderProvider {
    @Override
    public IAjaxResultBuilder getAjaxResultBuilder() {
        return new SimpleAjaxResultBuilder();
    }
}
