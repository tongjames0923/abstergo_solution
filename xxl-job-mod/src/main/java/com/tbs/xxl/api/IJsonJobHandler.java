package com.tbs.xxl.api;

import com.tbs.common.util.base.JsonUtils;

import java.util.Map;

public interface IJsonJobHandler<T> {

    /**
     * 获取参数类型
     *
     * @return
     */
    Class<? extends T> classType();

    /**
     * 获取帮助信息
     *
     * @return
     */
    default String help() {
        return "";
    }

    /**
     * 参数转换
     *
     * @param mp
     * @return
     */
    default T paramConvert(final Map mp) {
        return JsonUtils.parseObject(JsonUtils.toJSONString(mp), this.classType());
    }

    /**
     * 处理
     *
     * @param params
     * @return
     * @throws Exception
     */
    String handle(T params) throws Exception;
}
