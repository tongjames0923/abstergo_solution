package com.tbs.web.result.impl;

import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IAjaxResultBuilder;
import org.springframework.stereotype.Component;

/**
 * 默认的ajax结果构造提供者
 *
 * @author tongj
 */
public class SimpleBuilderProvider implements IAjaxBuilderProvider {
    @Override
    public IAjaxResultBuilder getAjaxResultBuilder() {
        return new SimpleAjaxResultBuilder();
    }
}
