package com.tbs.web.result;

import com.tbs.web.BaseController;

import java.lang.reflect.Method;

/**
 * ajax结果构建器提供者
 *
 * @author tongj
 */
public interface IAjaxBuilderProvider {
    /**
     * 获取ajax结果构建器
     *
     * @return
     */
    IAjaxResultBuilder getAjaxResultBuilder();

    /**
     * 是否支持此方法
     *
     * @param method
     * @return
     */
    default boolean support(Method method) {
        return method.getDeclaringClass().isAssignableFrom(BaseController.class);
    }
}
