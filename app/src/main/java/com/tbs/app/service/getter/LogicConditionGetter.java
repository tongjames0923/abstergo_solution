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
import java.util.*;

/**
 * 逻辑条件取值器。
 *
 * <p>对一组条件执行「且（and）」或「或（or）」组合，输出 true/false。每个条件既可以是字面量，
 * 也可以是另一次 getter 调用，因此可以组合出：</p>
 * <pre>
 * and( ConditionGetter("DateTimeGetter(dayOfWeek) ge 5"),
 *      ConditionGetter("CurrentUserGetter(userName) eq admin") )
 * </pre>
 *
 * <p>条件为 getter 调用时使用 JSON 对象描述：</p>
 * <pre>{"getterId":"ConditionGetter","paramValues":[{"value":"..."},{"value":"eq"},{"value":"..."}]}</pre>
 *
 * <p>求值采用短路语义：</p>
 * <ul>
 *     <li>{@code and}：任一条件为 false 立即返回 false；全部为 true 才返回 true</li>
 *     <li>{@code or}：任一条件为 true 立即返回 true；全部为 false 才返回 false</li>
 * </ul>
 *
 * <p>参数约定：第 1 个参数为逻辑运算符（and / or），其后最多预留 8 个条件参数；
 * 未使用的条件位由服务层补 null，求值时跳过。条件按传入顺序参与短路求值。
 * 该 getter 可嵌套自身以构建复合条件，但通过调用栈检测循环引用；任何异常或非法输入均返回 false。</p>
 *
 * @author abstergo
 */
@Slf4j
@Component
public class LogicConditionGetter extends AbstractStringValueGetter {

    /**
     * 运算符枚举值：且
     */
    private static final String OP_AND = "and";

    /**
     * 运算符枚举值：或
     */
    private static final String OP_OR = "or";

    /**
     * 嵌套调用最大深度
     */
    private static final int MAX_NEST_DEPTH = 8;

    /**
     * 声明的条件参数数量，与前端逻辑条件构建器保持一致
     */
    private static final int MAX_CONDITIONS = 8;

    /**
     * 逻辑条件 getter 调用签名栈，用于检测参数相同的循环引用
     */
    private static final ThreadLocal<Deque<String>> CALL_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 运算符文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.logic.condition.op.";

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
        return text("value.getter.logic.condition.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "LogicConditionGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.logic.condition.description");
    }

    /**
     * 声明布尔结果类型。
     *
     * @return 布尔结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("boolean").name(text("value.getter.logic.condition.result.name"))
            .description(text("value.getter.logic.condition.result.description")).sort(1).build();
    }

    /**
     * 声明参数定义：逻辑运算符、条件（可重复传入多个）。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> operators = List.of(op(OP_AND, "and"), op(OP_OR, "or"));
        List<ParamType> params = new ArrayList<>();
        params.add(ParamType.builder().isEnum(true).type("string")
            .name(text("value.getter.logic.condition.param.op.name"))
            .description(text("value.getter.logic.condition.param.op.description")).enumValues(operators)
            .required(true).sort(1).ui(ParamUiMeta.builder().inputType("select").build()).build());
        for (int i = 1; i <= MAX_CONDITIONS; i++) {
            params.add(ParamType.builder().type("operand")
                .name(text("value.getter.logic.condition.param.conditions.name"))
                .description(text("value.getter.logic.condition.param.conditions.description")).sort(i + 1)
                .ui(ParamUiMeta.builder().inputType("operand").build()).build());
        }
        return params;
    }

    /**
     * 计算逻辑组合结果。
     *
     * @param paramValues 参数值集合：逻辑运算符 + 一个或多个条件
     * @return true 或 false 字符串；非法输入、嵌套失败或异常时返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        Deque<String> stack = CALL_STACK.get();
        String currentSignature = signature(getGetterId(), paramValues);
        if (stack.contains(currentSignature)) {
            log.warn("逻辑条件取值器检测到循环引用, signature={}, stack={}", currentSignature, stack);
            return falseResult();
        }
        stack.push(currentSignature);
        try {
            String operator = paramString(paramValues, 0);
            if (operator == null || operator.isBlank()) {
                return falseResult();
            }
            String op = operator.trim();
            boolean isAnd = OP_AND.equals(op);
            boolean isOr = OP_OR.equals(op);
            if (!isAnd && !isOr) {
                log.warn("逻辑条件取值器不支持的运算符, operator={}", op);
                return falseResult();
            }
            List<String> conditions = collectConditions(paramValues);
            if (conditions.isEmpty()) {
                return falseResult();
            }
            for (String condition : conditions) {
                boolean value = resolveCondition(condition);
                if (isAnd && !value) {
                    return stringResult("false");
                }
                if (isOr && value) {
                    return stringResult("true");
                }
            }
            return stringResult(isAnd ? "true" : "false");
        } catch (Exception e) {
            log.warn("逻辑条件取值器计算失败, operator={}", operatorOf(paramValues), e);
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
     * 收集第 2 个参数及其之后的所有非空条件。
     *
     * @param paramValues 参数值集合
     * @return 条件原始字符串列表
     */
    private List<String> collectConditions(Collection<ParamValue> paramValues) {
        List<String> conditions = new ArrayList<>();
        if (paramValues == null || paramValues.isEmpty()) {
            return conditions;
        }
        List<ParamValue> list = new ArrayList<>(paramValues);
        for (int i = 1; i < list.size(); i++) {
            String raw = rawValue(list.get(i));
            if (raw == null || raw.isBlank()) {
                continue;
            }
            conditions.add(raw.trim());
        }
        return conditions;
    }

    /**
     * 解析单个条件：JSON 对象视为 getter 调用，其余视为字面量。
     *
     * @param raw 条件原始字符串
     * @return 条件布尔值
     */
    private boolean resolveCondition(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String text = raw.trim();
        if (!text.startsWith("{")) {
            return Boolean.parseBoolean(text);
        }
        Operand operand = JsonUtils.parseObject(text, Operand.class);
        if (operand == null || operand.getGetterId() == null || operand.getGetterId().isBlank()) {
            return false;
        }
        String value = invokeNested(operand.getGetterId(), operand.getParamValues());
        return Boolean.parseBoolean(value);
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
            log.warn("逻辑条件取值器拒绝嵌套调用, signature={}, stack={}", targetSignature, stack);
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
            log.warn("逻辑条件取值器嵌套调用失败, getterId={}", getterId, e);
            return "";
        }
    }

    /**
     * 读取 ParamValue 的字符串值。
     *
     * @param paramValue 参数值
     * @return 字符串值；缺失时返回 null
     */
    private String rawValue(ParamValue paramValue) {
        if (paramValue == null) {
            return null;
        }
        Object value = paramValue.getValue();
        return value == null ? null : String.valueOf(value);
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
        String operator = paramString(paramValues, 0);
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
     * getter 调用条件。
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
