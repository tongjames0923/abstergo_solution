package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.result.ResultType;
import com.tbs.script.api.expressible.vo.result.ResultValue;
import org.springframework.stereotype.Component;

/**
 * 文本强制转换取值器。
 *
 * <p>把数字、布尔等取值器的结果显式转换为文本结果，使条件构建器可以使用 contains、
 * startsWith 等文本匹配运算符，同时支持源值为空时回退到默认值。</p>
 *
 * @author abstergo
 */
@Component
public class ToTextGetter extends AbstractCastValueGetter {

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.cast.text.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ToTextGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.cast.text.description");
    }

    /**
     * 声明文本结果类型。
     *
     * @return 文本结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("string").name(text("value.getter.cast.text.result.name"))
            .description(text("value.getter.cast.text.result.description")).sort(1).build();
    }

    /**
     * 将源值作为文本输出。
     *
     * @param source   源值字符串
     * @param fallback 源值为空时使用的默认值
     * @return 文本结果
     */
    @Override
    protected ResultValue cast(String source, String fallback) {
        if (source == null || source.isBlank()) {
            return result(fallback == null ? "" : fallback);
        }
        return result(source);
    }
}
