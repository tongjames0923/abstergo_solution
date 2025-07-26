package com.tbs.web.result;

/**
 * 结果创建者
 *
 * @author jiangnan
 * @date 2020/01/09
 */
public interface IResultCreator {
    /**
     * 创建结果
     *
     * @param builder
     * @return
     */
    IAjaxResultBuilder.IAjaxResult2Step create(IAjaxResultBuilder builder,Object val);
}
