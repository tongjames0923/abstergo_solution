package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.vo.InstanceMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.script.api.expressible.vo.result.ResultType;
import com.tbs.script.api.expressible.vo.result.ResultValue;

import java.util.Collection;

/**
 * 表达式值获取器接口
 */
public interface IValueGetter extends IRouteMeta, IRequestable,IResponseable {
    /**
     * 获取值获取器名称
     *
     * @return 返回值获取器名称
     */
    String getGetterName();

    /**
     * 获取值获取器ID
     *
     * @return 返回值获取器ID
     */
    String getGetterId();

    /**
     * 获取值获取器描述
     *
     * @return 返回值获取器描述
     */
    String getDescription();

    /**
     * 获取值
     *
     * @param paramValues 参数值
     * @return 返回值
     */
    ResultValue getValue(Collection<ParamValue> paramValues);

    @Override
    default InstanceMeta getMeta() {
        return IRouteMeta.super.getMeta().setBusiness(getGetterId());
    }
}
