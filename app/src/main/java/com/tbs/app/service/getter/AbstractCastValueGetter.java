package com.tbs.app.service.getter;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.script.api.expressible.impl.IValueGetterService;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.script.api.expressible.vo.result.ResultValue;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 强制转换取值器抽象基类。
 *
 * <p>统一声明「源值 + 默认值」两个参数：源值既可以是字面量，也可以是与 ConditionGetter 相同格式的
 * getter 调用 JSON 对象；转换类取值器递归执行被嵌套的 getter 后，把结果字符串转换为目标类型并声明
 * 对应的 ResultType，使布尔条件构建器能按真实类型推断操作数与运算符。转换失败时返回默认值，不中断脚本。</p>
 *
 * @author abstergo
 */
@Slf4j
abstract class AbstractCastValueGetter extends AbstractStringValueGetter {

    /**
     * 嵌套调用最大深度
     */
    private static final int MAX_NEST_DEPTH = 8;

    /**
     * 嵌套调用深度，防止操作数组合成循环引用
     */
    private static final ThreadLocal<Integer> NEST_DEPTH = new ThreadLocal<>();

    /**
     * 值获取器服务，用于递归执行源 getter
     */
    @Resource
    protected IValueGetterService valueGetterService;

    /**
     * 声明参数定义：源值与转换失败时的默认值。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(
            ParamType.builder().type("operand").name(text("value.getter.cast.param.source.name"))
                .description(text("value.getter.cast.param.source.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("operand")
                    .placeholder(text("value.getter.cast.param.source.placeholder")).build()).build(),
            ParamType.builder().type("string").name(text("value.getter.cast.param.fallback.name"))
                .description(text("value.getter.cast.param.fallback.description")).sort(2)
                .ui(ParamUiMeta.builder().inputType("text")
                    .placeholder(text("value.getter.cast.param.fallback.placeholder")).build()).build());
    }

    /**
     * 解析源值并执行转换。
     *
     * @param paramValues 参数值集合，首个参数为源值，第二个参数为默认值
     * @return 目标类型的结果值
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String source = resolveOperand(paramString(paramValues, 0));
        String fallback = paramString(paramValues, 1);
        return cast(source, fallback);
    }

    /**
     * 把源值转换为目标类型。
     *
     * @param source   源值字符串
     * @param fallback 转换失败时的默认值，可为空
     * @return 目标类型的结果值
     */
    protected abstract ResultValue cast(String source, String fallback);

    /**
     * 按当前取值器声明的结果类型构造结果。
     *
     * @param value 结果字符串
     * @return 结果值
     */
    protected ResultValue result(String value) {
        return ResultValue.builder().type(responseResultType()).value(value == null ? "" : value).build();
    }

    /**
     * 解析操作数：JSON 对象视为 getter 调用并递归执行，其余视为字面量。
     *
     * @param raw 操作数原始字符串
     * @return 解析后的字符串值
     */
    private String resolveOperand(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String content = raw.trim();
        if (!content.startsWith("{")) {
            return content;
        }
        Operand operand = JsonUtils.parseObject(content, Operand.class);
        if (operand == null || operand.getGetterId() == null || operand.getGetterId().isBlank()) {
            return content;
        }
        int depth = NEST_DEPTH.get() == null ? 0 : NEST_DEPTH.get();
        if (depth >= MAX_NEST_DEPTH) {
            log.warn("强制转换取值器拒绝超深嵌套调用, getterId={}, depth={}", operand.getGetterId(), depth);
            return "";
        }
        NEST_DEPTH.set(depth + 1);
        try {
            ResultValue result = valueGetterService.invokeValueGetter(operand.getGetterId(), operand.getParamValues());
            if (result == null) {
                return "";
            }
            Serializable value = result.getValue();
            if (value != null) {
                return String.valueOf(value);
            }
            Collection<Serializable> values = result.getValues();
            return values == null || values.isEmpty() ? "" : String.valueOf(values.iterator().next());
        } catch (Exception e) {
            log.warn("强制转换取值器解析源 getter 失败, getterId={}", operand.getGetterId(), e);
            return "";
        } finally {
            if (depth == 0) {
                NEST_DEPTH.remove();
            } else {
                NEST_DEPTH.set(depth);
            }
        }
    }

    /**
     * getter 调用操作数。
     */
    @Data
    public static class Operand {

        /**
         * 被嵌套调用 getter 的 ID
         */
        private String getterId;

        /**
         * 被嵌套调用 getter 的参数列表
         */
        private List<ParamValue> paramValues;
    }
}
