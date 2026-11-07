package com.tbs.web.genric;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.genric.config.GenericApiConfig;
import com.tbs.web.genric.config.GenericApiParamConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 泛化调用实参装配器（新版链路）
 * <p>
 * 把 HTTP 请求体与登记好的参数定义装配成 Dubbo 泛化调用所需的 {@code paramTypes/args} 两件套，
 * 支持两种请求体形态：
 * </p>
 * <ol>
 *     <li>JSON 对象（命名式）：按登记参数名取值，顺序仍取登记的 {@code order}；</li>
 *     <li>JSON 数组（位置式）：按下标对齐登记顺序，适合临时试机时不起参数名的场景。</li>
 * </ol>
 * <p>
 * 装配阶段一律<b>不猜类型</b>：类型只认登记值，缺失或多余都直接报错。因为泛化调用没有接口 jar，
 * 任何猜测都只会在远端以更难懂的方式失败；把错误留在入口才有可诊断性。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class GenericArgumentAssembler {

    private GenericArgumentAssembler() {
    }

    /**
     * 装配泛化调用实参
     *
     * @param config 泛化调用配置（含参数定义），不能为空
     * @param body   请求体 JSON 节点，可以为 {@code null}（等价于无入参内容）
     * @return 泛化调用所需的参数类型数组与实参数组
     */
    public static AssembledArguments assemble(GenericApiConfig config, JsonNode body) {
        List<GenericApiParamConfig> params = sortedParams(config);
        boolean bodyEmpty = body == null || body.isNull() || (body.isObject() && body.isEmpty()) || (body.isArray()
            && body.isEmpty());
        if (params.isEmpty()) {
            AssertUtil.isTrue(!bodyEmpty).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_PARAM_DEF_MISSING).throwIt();
            return new AssembledArguments(new String[0], new Object[0]);
        }
        String[] parameterTypes = new String[params.size()];
        Object[] arguments = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            parameterTypes[i] = registeredType(params.get(i), i);
        }
        if (bodyEmpty) {
            fillWithoutBody(params, arguments);
            return new AssembledArguments(parameterTypes, arguments);
        }
        if (body.isArray()) {
            assemblePositional(params, arguments, body);
        } else {
            assembleNamed(params, arguments, body);
        }
        return new AssembledArguments(parameterTypes, arguments);
    }

    /**
     * 命名式装配：逐个参数名取值，并检查请求体是否存在未登记的键
     *
     * @param params    参数定义，按顺序排列
     * @param arguments 实参结果载体
     * @param body      JSON 对象形态请求体
     */
    private static void assembleNamed(List<GenericApiParamConfig> params, Object[] arguments, JsonNode body) {
        AssertUtil.isTrue(!body.isObject()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(WebI18nConstant.GENERIC_API_BODY_FORMAT_UNSUPPORTED).throwIt();
        Set<String> registeredNames = new HashSet<>();
        for (int i = 0; i < params.size(); i++) {
            GenericApiParamConfig param = params.get(i);
            String name = param.getName();
            AssertUtil.isTrue(StrUtil.isBlank(name)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_PARAM_NAME_UNREGISTERED).params(paramLabel(param, i)).throwIt();
            registeredNames.add(name);
            arguments[i] = valueOf(body.get(name), param, i);
        }
        body.fieldNames().forEachRemaining(field -> AssertUtil.isTrue(!registeredNames.contains(field))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(WebI18nConstant.GENERIC_API_PARAM_UNKNOWN)
            .params(field).throwIt());
    }

    /**
     * 位置式装配：数组长度必须与登记条数一致
     *
     * @param params    参数定义，按顺序排列
     * @param arguments 实参结果载体
     * @param body      JSON 数组形态请求体
     */
    private static void assemblePositional(List<GenericApiParamConfig> params, Object[] arguments, JsonNode body) {
        AssertUtil.isTrue(body.size() != params.size()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(WebI18nConstant.GENERIC_API_PARAM_COUNT_MISMATCH).params(params.size(), body.size()).throwIt();
        for (int i = 0; i < params.size(); i++) {
            GenericApiParamConfig param = params.get(i);
            arguments[i] = valueOf(body.get(i), param, i);
        }
    }

    /**
     * 请求体无内容时的必填判定：只要存在必填参数即报错，全非必填时统一传 {@code null}
     *
     * @param params    参数定义
     * @param arguments 实参结果载体
     */
    private static void fillWithoutBody(List<GenericApiParamConfig> params, Object[] arguments) {
        for (int i = 0; i < params.size(); i++) {
            GenericApiParamConfig param = params.get(i);
            AssertUtil.isTrue(param.isRequired()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_PARAM_REQUIRED).params(paramLabel(param, i)).throwIt();
            arguments[i] = null;
        }
    }

    /**
     * 取单个参数的请求值并做必填校验
     *
     * @param node  请求体中对应的节点，可以为 {@code null}（表示未传）
     * @param param 参数定义
     * @param index 参数位置
     * @return 可直接交给泛化调用的普通 Java 值（Map/List/标量/数组）
     */
    private static Object valueOf(JsonNode node, GenericApiParamConfig param, int index) {
        AssertUtil.isTrue((node == null || node.isNull()) && param.isRequired())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(WebI18nConstant.GENERIC_API_PARAM_REQUIRED)
            .params(paramLabel(param, index)).throwIt();
        return toPlainValue(node);
    }

    /**
     * 把 JSON 节点还原为普通 Java 值
     * <p>
     * 泛化调用侧要见到 Map/List/标量才能按声明类型还原成 DTO；直接传 {@link JsonNode} 会让远端拿到无法序列化的树节点。
     * </p>
     *
     * @param node JSON 节点，可以为 {@code null}
     * @return 普通 Java 值，入参为 {@code null} 或缺失时返回 {@code null}
     */
    private static Object toPlainValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return JsonUtils.getMapperInstance().convertValue(node, Object.class);
    }

    /**
     * 取登记的参数类型
     *
     * @param param 参数定义
     * @param index 参数位置
     * @return 擦除后的声明类型名
     */
    private static String registeredType(GenericApiParamConfig param, int index) {
        AssertUtil.isTrue(StrUtil.isBlank(param.getType())).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(WebI18nConstant.GENERIC_API_PARAM_TYPE_UNREGISTERED).params(paramLabel(param, index)).throwIt();
        return param.getType();
    }

    /**
     * 取按顺序排列的参数定义
     *
     * @param config 泛化调用配置，不能为空
     * @return 参数定义列表，无登记时为空列表
     */
    private static List<GenericApiParamConfig> sortedParams(GenericApiConfig config) {
        List<GenericApiParamConfig> params = config.getParams();
        if (CollUtil.isEmpty(params)) {
            return new ArrayList<>();
        }
        return params.stream().filter(Objects::nonNull).sorted(
            Comparator.comparing(GenericApiParamConfig::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    /**
     * 面向报错信息的参数标识：有登记名用名，无登记名用位置
     *
     * @param param 参数定义
     * @param index 参数位置
     * @return 可读参数标识
     */
    private static String paramLabel(GenericApiParamConfig param, int index) {
        return StrUtil.isBlank(param.getName()) ? "#" + index : param.getName();
    }

    /**
     * 装配结果：泛化调用所需的参数类型与实参
     */
    @Getter
    @RequiredArgsConstructor
    public static final class AssembledArguments {
        /**
         * 参数类型数组，擦除后的 JVM 声明类型名
         */
        private final String[] parameterTypes;
        /**
         * 实参数组，元素为 Map/List/标量等普通 Java 值
         */
        private final Object[] arguments;
    }
}
