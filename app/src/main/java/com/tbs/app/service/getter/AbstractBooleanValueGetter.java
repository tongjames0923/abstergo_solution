package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.result.ResultType;
import com.tbs.script.api.expressible.vo.result.ResultValue;

/**
 * 布尔结果取值器抽象基类。
 *
 * <p>统一声明 boolean 结果类型并提供布尔结果构造方法；结果值在协议层仍以字符串
 * true / false 传递，与现有取值器契约保持一致。</p>
 *
 * @author abstergo
 */
abstract class AbstractBooleanValueGetter extends AbstractStringValueGetter {

    /**
     * 声明布尔结果类型。
     *
     * @return 布尔结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("boolean").name(text("value.getter.bool.result.name"))
            .description(text("value.getter.bool.result.description")).sort(1).build();
    }

    /**
     * 构造布尔结果。
     *
     * @param value 布尔值
     * @return 结果值
     */
    protected ResultValue booleanResult(boolean value) {
        return ResultValue.builder().type(responseResultType()).value(String.valueOf(value)).build();
    }
}
