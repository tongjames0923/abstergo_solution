package com.tbs.app.service.getter;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.script.api.expressible.IValueGetter;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultType;
import com.tbs.script.api.expressible.vo.ResultValue;

import java.util.Collection;

/**
 * 字符串结果取值器抽象基类。
 *
 * <p>统一声明字符串返回类型元信息，并提供参数读取、空结果构造与文案解析的公共实现，
 * 子类只需关注自身的数据源与取值逻辑。</p>
 *
 * @author abstergo
 */
abstract class AbstractStringValueGetter implements IValueGetter {

    /**
     * 声明字符串返回类型。
     *
     * @return 字符串结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("string").name(text("value.getter.common.result.name"))
            .description(text("value.getter.common.result.description")).sort(1).build();
    }

    /**
     * 构造空字符串结果。
     *
     * @return 空字符串结果
     */
    protected ResultValue emptyResult() {
        return stringResult("");
    }

    /**
     * 构造字符串结果。
     *
     * @param value 结果值，为 null 时按空字符串处理
     * @return 字符串结果
     */
    protected ResultValue stringResult(String value) {
        return ResultValue.builder().type(responseResultType()).value(value == null ? "" : value).build();
    }

    /**
     * 读取指定下标的参数值。
     *
     * <p>按下标定位而非按内容过滤，与 {@code IValueGetterService} 的 null 占位对齐规则保持一致。</p>
     *
     * @param paramValues 参数值集合
     * @param index       参数下标，从 0 开始
     * @return 参数值字符串；参数不存在或为空时返回 null
     */
    protected String paramString(Collection<ParamValue> paramValues, int index) {
        ParamValue paramValue = nthParamValue(paramValues, index);
        return paramValue == null ? null : paramValue.getValue();
    }

    /**
     * 读取指定下标的参数对象。
     *
     * @param paramValues 参数值集合
     * @param index       参数下标，从 0 开始
     * @return 参数对象；参数不存在时返回 null
     */
    private ParamValue nthParamValue(Collection<ParamValue> paramValues, int index) {
        if (CollUtil.isEmpty(paramValues) || index < 0) {
            return null;
        }
        int i = 0;
        for (ParamValue paramValue : paramValues) {
            if (i == index) {
                return paramValue;
            }
            i++;
        }
        return null;
    }

    /**
     * 解析国际化文案。
     *
     * @param key 资源 key
     * @return 文案内容；无可用资源时回退为 key 本身
     */
    protected String text(String key) {
        String value = I18nUtil.get(key);
        return value == null || value.isBlank() ? key : value;
    }
}
