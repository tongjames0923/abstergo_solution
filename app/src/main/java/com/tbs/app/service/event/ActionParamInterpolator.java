package com.tbs.app.service.event;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动作参数占位符解析器。
 *
 * <p>动作参数中可使用 {@code ${参数名}} 引用本次事件参数，例如标题配置为
 * {@code 新用户 ${userName} 已创建} 时，运行期会用事件参数 {@code userName} 的值替换占位符。
 * 未匹配到的事件参数保留原占位符，便于在调试区直接看出配置缺失。</p>
 *
 * @author abstergo
 */
public final class ActionParamInterpolator {

    /**
     * 占位符格式：${参数名}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    private ActionParamInterpolator() {
    }

    /**
     * 按事件参数替换动作参数中的占位符。
     *
     * @param actionParams 动作参数值
     * @param eventParams  事件参数值
     * @return 替换后的动作参数值列表
     */
    public static List<ParamValue> interpolate(Collection<ParamValue> actionParams,
        Collection<ParamValue> eventParams) {
        if (CollUtil.isEmpty(actionParams)) {
            return List.of();
        }
        Map<String, String> values = toValueMap(eventParams);
        List<ParamValue> result = new ArrayList<>(actionParams.size());
        for (ParamValue actionParam : actionParams) {
            if (actionParam == null) {
                continue;
            }
            if (values.isEmpty() || StrUtil.isEmpty(actionParam.getValue())
                || !actionParam.getValue().contains("${")) {
                result.add(actionParam);
                continue;
            }
            result.add(ParamValue.builder().type(actionParam.getType()).name(actionParam.getName())
                .sort(actionParam.getSort()).value(replace(actionParam.getValue(), values)).build());
        }
        return result;
    }

    /**
     * 按事件参数替换模板中的占位符。
     *
     * @param template    模板文本
     * @param eventParams 事件参数值
     * @return 替换后的文本；模板为空时原样返回
     */
    public static String interpolate(String template, Collection<ParamValue> eventParams) {
        if (StrUtil.isEmpty(template) || !template.contains("${")) {
            return template;
        }
        return replace(template, toValueMap(eventParams));
    }

    /**
     * 执行占位符替换。
     *
     * @param template 模板文本
     * @param values   参数名到参数值的映射
     * @return 替换后的文本；参数名缺失时保留原占位符
     */
    private static String replace(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = values.get(name);
            String replacement = value == null ? matcher.group() : value;
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    /**
     * 事件参数转为参数名到参数值的映射。
     *
     * @param eventParams 事件参数值
     * @return 参数名到参数值的映射，同名参数后者覆盖前者
     */
    private static Map<String, String> toValueMap(Collection<ParamValue> eventParams) {
        Map<String, String> values = new LinkedHashMap<>();
        if (CollUtil.isEmpty(eventParams)) {
            return values;
        }
        for (ParamValue paramValue : eventParams) {
            if (paramValue == null || StrUtil.isBlank(paramValue.getName())) {
                continue;
            }
            values.put(paramValue.getName(), paramValue.getValue());
        }
        return values;
    }
}
