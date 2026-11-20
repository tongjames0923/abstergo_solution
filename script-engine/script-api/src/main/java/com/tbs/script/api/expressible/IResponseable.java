package com.tbs.script.api.expressible;

import com.tbs.script.api.expressible.vo.result.ResultType;

public interface IResponseable {
    /**
     * 获取返回值类型
     *
     * @return 返回值类型
     */
    ResultType responseResultType();
}
