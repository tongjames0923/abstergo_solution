package com.tbs.web.result;

import com.tbs.web.BaseController;

import java.lang.reflect.Method;

/**
 * ajax结果构建器提供者
 * <p>
 * 为控制器方法提供对应的 {@link IAjaxResultBuilder} 实例，并声明该提供者适用的返回类型范围。
 * </p>
 *
 * @author tongj
 */
public interface IAjaxBuilderProvider {
    /**
     * 获取 ajax 结果构建器
     *
     * @return 构建器实例，不能为空
     */
    IAjaxResultBuilder getAjaxResultBuilder();

    /**
     * 判断该提供者是否适用于指定方法
     * <p>
     * 默认规则：方法声明类为 {@link BaseController} 或其子类时生效。
     * </p>
     *
     * @param method 待判定的控制器方法，不能为空
     * @return 支持返回 {@code true}，否则 {@code false}
     */
    default boolean support(Method method) {
        return method.getDeclaringClass().isAssignableFrom(BaseController.class);
    }
}
