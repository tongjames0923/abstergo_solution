package com.tbs.web.result;

/**
 * 结果创建者
 * <p>
 * 由 {@link Wrapped} 注解指定，定义控制器方法返回值如何被包装为 {@link AjaxResult}。
 * </p>
 *
 * @author jiangnan
 * @date 2020/01/09
 */
public interface IResultCreator {
    /**
     * 创建结果
     *
     * @param builder 构建器，不能为空
     * @param val     控制器方法的原始返回值，可以为 {@code null}
     * @return 进入构建第二步（整理/输出）的结果构建器
     */
    IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder,Object val);
}
