package com.tbs.web.result;

/**
 * ajax结果构建器提供者
 * @author tongj
 */
public interface IAjaxBuilderProvider {
    /**
     * 获取ajax结果构建器
     *
     * @return
     */
    IAjaxResultBuilder getAjaxResultBuilder();
}
