package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.ResultType;
import com.tbs.script.api.expressible.vo.ResultValue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数字强制转换取值器。
 *
 * <p>把源值的字符串结果解析为数字，并把结果类型声明为 number，便于布尔条件按数值大小比较。</p>
 *
 * @author abstergo
 */
@Component
public class ToNumberGetter extends AbstractCastValueGetter {

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.cast.number.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ToNumberGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.cast.number.description");
    }

    /**
     * 声明数字结果类型。
     *
     * @return 数字结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("number").name(text("value.getter.cast.number.result.name"))
            .description(text("value.getter.cast.number.result.description")).sort(1).build();
    }

    /**
     * 将源值转换为数字字符串。
     *
     * @param source   源值字符串
     * @param fallback 转换失败时的默认值
     * @return 数字结果；解析失败时返回默认值，默认值为 0
     */
    @Override
    protected ResultValue cast(String source, String fallback) {
        try {
            BigDecimal number = new BigDecimal(source.trim());
            return result(number.stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            return result(fallback == null ? "0" : fallback);
        }
    }
}
