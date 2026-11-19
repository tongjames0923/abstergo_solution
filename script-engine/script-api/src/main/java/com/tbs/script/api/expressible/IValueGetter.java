package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.vo.InstanceMeta;
import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultType;
import com.tbs.script.api.expressible.vo.ResultValue;

import java.util.Collection;
import java.util.List;

/**
 * 表达式值获取器接口
 */
public interface IValueGetter extends IRouteMeta {
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
     * 获取请求参数类型
     *
     * @return 返回请求参数类型
     */
    List<ParamType> requestParamTypes();

    /**
     * 获取返回值类型
     *
     * @return 返回值类型
     */
    ResultType responseResultType();

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
