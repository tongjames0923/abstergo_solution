package com.tbs.app.service.getter;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.script.api.expressible.impl.IValueGetterService;
import com.tbs.script.api.expressible.vo.EnumValue;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.script.api.expressible.vo.result.ResultType;
import com.tbs.script.api.expressible.vo.result.ResultValue;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 * 布尔条件取值器。
 *
 * <p>对两个操作数执行比较并输出 true/false。每个操作数既可以是字面量，也可以是另一次 getter 调用，
 * 因此可以组合出「DateTimeGetter(dayOfWeek) &gt;= 5」「CurrentUserGetter(userName) eq admin」这类条件，
 * 作为动态事件触发系统的布尔条件来源。</p>
 *
 * <p>操作数为 getter 调用时使用 JSON 对象描述：</p>
 * <pre>{"getterId":"DateTimeGetter","paramValues":[{"value":"dayOfWeek"}]}</pre>
 *
 * <p>该 getter 可嵌套自身以构建复合条件，但通过调用栈检测循环引用；任何异常或非法输入均返回 false。</p>
 *
 * @author abstergo
 */
@Slf4j
@Component
public class ConditionGetter extends AbstractStringValueGetter {

    /**
     * 运算符枚举值：等于
     */
    private static final String OP_EQ = "eq";

    /**
     * 运算符枚举值：不等于
     */
    private static final String OP_NE = "ne";

    /**
     * 运算符枚举值：大于
     */
    private static final String OP_GT = "gt";

    /**
     * 运算符枚举值：大于等于
     */
    private static final String OP_GE = "ge";

    /**
     * 运算符枚举值：小于
     */
    private static final String OP_LT = "lt";

    /**
     * 运算符枚举值：小于等于
     */
    private static final String OP_LE = "le";

    /**
     * 运算符枚举值：包含
     */
    private static final String OP_CONTAINS = "contains";

    /**
     * 运算符枚举值：不包含
     */
    private static final String OP_NOT_CONTAINS = "notContains";

    /**
     * 运算符枚举值：以…开头
     */
    private static final String OP_STARTS_WITH = "startsWith";

    /**
     * 运算符枚举值：以…结尾
     */
    private static final String OP_ENDS_WITH = "endsWith";

    /**
     * 运算符枚举值：为 true
     */
    private static final String OP_IS_TRUE = "isTrue";

    /**
     * 运算符枚举值：为 false 或空
     */
    private static final String OP_IS_FALSE = "isFalse";

    /**
     * 运算符枚举值：为空
     */
    private static final String OP_IS_EMPTY = "isEmpty";

    /**
     * 运算符枚举值：非空
     */
    private static final String OP_IS_NOT_EMPTY = "isNotEmpty";

    /**
     * 嵌套调用最大深度
     */
    private static final int MAX_NEST_DEPTH = 8;

    /**
     * 条件 getter 调用签名栈，用于检测参数相同的循环引用（参数不同的同名 getter 嵌套仍允许）
     */
    private static final ThreadLocal<Deque<String>> CALL_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 运算符文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.condition.op.";

    /**
     * 值获取器服务，用于递归调用被嵌套的 getter
     */
    @Resource
    private IValueGetterService valueGetterService;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.condition.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ConditionGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.condition.description");
    }

    /**
     * 声明布尔结果类型。
     *
     * @return 布尔结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("boolean").name(text("value.getter.condition.result.name"))
            .description(text("value.getter.condition.result.description")).sort(1).build();
    }

    /**
     * 声明参数定义：左操作数、运算符、右操作数。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> operators = List.of(op(OP_EQ, "eq"), op(OP_NE, "ne"), op(OP_GT, "gt"), op(OP_GE, "ge"),
            op(OP_LT, "lt"), op(OP_LE, "le"), op(OP_CONTAINS, "contains"), op(OP_NOT_CONTAINS, "not-contains"),
            op(OP_STARTS_WITH, "starts-with"), op(OP_ENDS_WITH, "ends-with"), op(OP_IS_TRUE, "is-true"),
            op(OP_IS_FALSE, "is-false"), op(OP_IS_EMPTY, "is-empty"), op(OP_IS_NOT_EMPTY, "is-not-empty"));
        return List.of(ParamType.builder().type("operand").name(text("value.getter.condition.param.left.name"))
            .description(text("value.getter.condition.param.left.description")).sort(1)
            .ui(ParamUiMeta.builder().inputType("operand").build()).build(),
            ParamType.builder().isEnum(true).type("string").name(text("value.getter.condition.param.op.name"))
                .description(text("value.getter.condition.param.op.description")).enumValues(operators).required(true)
                .sort(2).ui(ParamUiMeta.builder().inputType("select").build()).build(),
            ParamType.builder().type("operand").name(text("value.getter.condition.param.right.name"))
                .description(text("value.getter.condition.param.right.description")).sort(3)
                .ui(ParamUiMeta.builder().inputType("operand").build()).build());
    }

    /**
     * 计算布尔条件结果。
     *
     * @param paramValues 参数值集合：左操作数、运算符、右操作数
     * @return true 或 false 字符串；非法输入、嵌套失败或异常时返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        Deque<String> stack = CALL_STACK.get();
        stack.push(signature(getGetterId(), paramValues));
        try {
            String operator = paramString(paramValues, 1);
            if (operator == null || operator.isBlank()) {
                return falseResult();
            }
            String left = resolveOperand(paramString(paramValues, 0));
            String right = resolveOperand(paramString(paramValues, 2));
            return stringResult(String.valueOf(compare(left, operator, right)));
        } catch (Exception e) {
            log.warn("条件取值器计算失败, operator={}", operatorOf(paramValues), e);
            return falseResult();
        } finally {
            stack.pop();
            if (stack.isEmpty()) {
                CALL_STACK.remove();
            }
        }
    }

    /**
     * 构造 false 结果。
     *
     * @return false 结果
     */
    private ResultValue falseResult() {
        return stringResult("false");
    }

    /**
     * 解析操作数：JSON 对象视为 getter 调用，其余视为字面量。
     *
     * @param raw 操作数原始字符串
     * @return 解析后的字符串值
     */
    private String resolveOperand(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw.trim();
        if (!text.startsWith("{")) {
            return text;
        }
        Operand operand = JsonUtils.parseObject(text, Operand.class);
        if (operand == null || operand.getGetterId() == null || operand.getGetterId().isBlank()) {
            return text;
        }
        return invokeNested(operand.getGetterId(), operand.getParamValues());
    }

    /**
     * 递归调用被嵌套的 getter。
     *
     * @param getterId    被嵌套 getter 的 ID
     * @param paramValues 被嵌套 getter 的参数
     * @return getter 结果的字符串形式；循环引用、超深或异常时返回空字符串
     */
    private String invokeNested(String getterId, List<ParamValue> paramValues) {
        Deque<String> stack = CALL_STACK.get();
        String targetSignature = signature(getterId, paramValues);
        if (stack.size() >= MAX_NEST_DEPTH || stack.contains(targetSignature)) {
            log.warn("条件取值器拒绝嵌套调用, signature={}, stack={}", targetSignature, stack);
            return "";
        }
        try {
            ResultValue result = valueGetterService.invokeValueGetter(getterId, paramValues);
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
            log.warn("条件取值器嵌套调用失败, getterId={}", getterId, e);
            return "";
        }
    }

    /**
     * 执行比较运算。
     *
     * @param left     左操作数字符串
     * @param operator 运算符
     * @param right    右操作数字符串
     * @return 比较结果为 true 或 false
     */
    private boolean compare(String left, String operator, String right) {
        return switch (operator) {
            case OP_EQ -> left.equals(right);
            case OP_NE -> !left.equals(right);
            case OP_GT -> compareOrdered(left, right) > 0;
            case OP_GE -> compareOrdered(left, right) >= 0;
            case OP_LT -> compareOrdered(left, right) < 0;
            case OP_LE -> compareOrdered(left, right) <= 0;
            case OP_CONTAINS -> left.contains(right);
            case OP_NOT_CONTAINS -> !left.contains(right);
            case OP_STARTS_WITH -> left.startsWith(right);
            case OP_ENDS_WITH -> left.endsWith(right);
            case OP_IS_TRUE -> Boolean.parseBoolean(left);
            case OP_IS_FALSE -> !Boolean.parseBoolean(left);
            case OP_IS_EMPTY -> left.isEmpty();
            case OP_IS_NOT_EMPTY -> !left.isEmpty();
            default -> false;
        };
    }

    /**
     * 大小比较：可解析为数值时按数值比较，否则按字符串字典序比较。
     *
     * @param left  左值
     * @param right 右值
     * @return 负数表示左小于右，0 表示相等，正数表示左大于右
     */
    private int compareOrdered(String left, String right) {
        try {
            return new BigDecimal(left).compareTo(new BigDecimal(right));
        } catch (NumberFormatException e) {
            return left.compareTo(right);
        }
    }

    /**
     * 计算调用签名，由 getter ID 与参数值拼接而成。
     *
     * @param getterId    获取器 ID
     * @param paramValues 参数值集合
     * @return 调用签名
     */
    private String signature(String getterId, Collection<ParamValue> paramValues) {
        StringBuilder sb = new StringBuilder(getterId);
        if (paramValues != null) {
            for (ParamValue paramValue : paramValues) {
                sb.append('|').append(paramValue == null ? "" : paramValue.getValue());
            }
        }
        return sb.toString();
    }

    /**
     * 读取运算符用于日志。
     *
     * @param paramValues 参数值集合
     * @return 运算符字符串；缺失时返回空字符串
     */
    private String operatorOf(Collection<ParamValue> paramValues) {
        String operator = paramString(paramValues, 1);
        return operator == null ? "" : operator;
    }

    /**
     * 构造运算符枚举项。
     *
     * @param value    枚举值
     * @param labelKey 文案 key 后缀
     * @return 枚举项
     */
    private EnumValue op(String value, String labelKey) {
        return EnumValue.builder().value(value).label(text(I18N_PREFIX + labelKey)).build();
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
