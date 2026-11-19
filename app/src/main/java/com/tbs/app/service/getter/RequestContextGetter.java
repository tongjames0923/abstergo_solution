package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.*;
import com.tbs.web.context.RequestContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 请求上下文取值器。
 *
 * <p>读取当前请求上下文的 traceId 或自定义业务变量，作为请求链路类事件条件来源。
 * 无请求上下文、变量未命中、类型未知或取值异常时返回空字符串，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class RequestContextGetter extends AbstractStringValueGetter {

    /**
     * 信息类型枚举值：traceId
     */
    private static final String TYPE_TRACE_ID = "traceId";

    /**
     * 信息类型枚举值：自定义上下文变量
     */
    private static final String TYPE_CONTEXT_VALUE = "contextValue";

    /**
     * 信息类型文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.request-context.type.";

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.request-context.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "RequestContextGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.request-context.description");
    }

    /**
     * 声明参数定义：信息类型枚举与可选变量名。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> enumValues = List.of(enumValue(TYPE_TRACE_ID, "trace-id"), enumValue(TYPE_CONTEXT_VALUE,
            "context-value"));
        return List.of(ParamType.builder().isEnum(true).type("string")
            .name(text("value.getter.request-context.param.type.name"))
            .description(text("value.getter.request-context.param.type.description")).enumValues(enumValues)
            .required(true).sort(1)
            .ui(ParamUiMeta.builder().inputType("select")
                .placeholder(text("value.getter.request-context.param.type.placeholder")).build())
            .build(),
            ParamType.builder().type("string").name(text("value.getter.request-context.param.key.name"))
                .description(text("value.getter.request-context.param.key.description")).sort(2)
                .ui(ParamUiMeta.builder().inputType("text")
                    .placeholder(text("value.getter.request-context.param.key.placeholder")).build())
                .build());
    }

    /**
     * 按类型读取请求上下文信息。
     *
     * @param paramValues 参数值集合，首个参数为信息类型，第二个参数为可选变量名
     * @return 字符串结果；无上下文、变量未命中或异常时返回空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String type = paramString(paramValues, 0);
        if (type == null || type.isBlank()) {
            return emptyResult();
        }
        try {
            return stringResult(switch (type) {
                case TYPE_TRACE_ID -> RequestContextHolder.traceId().orElse("");
                case TYPE_CONTEXT_VALUE -> {
                    String key = paramString(paramValues, 1);
                    if (key == null || key.isBlank()) {
                        yield "";
                    }
                    yield RequestContextHolder.get(key).map(String::valueOf).orElse("");
                }
                default -> "";
            });
        } catch (Exception e) {
            return emptyResult();
        }
    }

    /**
     * 构造信息类型枚举项。
     *
     * @param value    枚举值
     * @param labelKey 文案 key 后缀
     * @return 枚举项
     */
    private EnumValue enumValue(String value, String labelKey) {
        return EnumValue.builder().value(value).label(text(I18N_PREFIX + labelKey)).build();
    }
}
