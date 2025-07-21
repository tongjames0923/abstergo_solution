package com.tbs.web.result;

/**
 * 错误处理ajax结果提供者
 *
 * @author tongj
 */
public interface IOnErrorAjaxProvider {
    /**
     * 错误处理ajax结果提供者
     *
     * @param throwable
     * @return
     */
    AjaxResult onError(Throwable throwable);
}
