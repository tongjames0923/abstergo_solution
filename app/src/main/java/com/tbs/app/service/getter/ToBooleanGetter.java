package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.ResultType;
import com.tbs.script.api.expressible.vo.ResultValue;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 布尔强制转换取值器。
 *
 * <p>把源值按 true/false、1/0、yes/no、on/off 等常见写法解析为布尔值，并把结果类型声明为 boolean，
 * 便于布尔条件使用 isTrue/isFalse 等判定运算符。</p>
 *
 * @author abstergo
 */
@Component
public class ToBooleanGetter extends AbstractCastValueGetter {

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.cast.boolean.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ToBooleanGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.cast.boolean.description");
    }

    /**
     * 声明布尔结果类型。
     *
     * @return 布尔结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("boolean").name(text("value.getter.cast.boolean.result.name"))
            .description(text("value.getter.cast.boolean.result.description")).sort(1).build();
    }

    /**
     * 将源值转换为布尔字符串。
     *
     * @param source   源值字符串
     * @param fallback 无法识别时的默认值
     * @return 布尔结果；无法识别时返回默认值，默认值为 false
     */
    @Override
    protected ResultValue cast(String source, String fallback) {
        String normalized = source.trim().toLowerCase(Locale.ROOT);
        Boolean parsed = switch (normalized) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> null;
        };
        if (parsed == null) {
            return result(fallback == null ? "false" : fallback);
        }
        return result(String.valueOf(parsed));
    }
}
