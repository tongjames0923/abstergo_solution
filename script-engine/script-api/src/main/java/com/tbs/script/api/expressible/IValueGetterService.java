package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.vo.EnumValue;
import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 值获取器服务
 */
@DubboService
@Slf4j
@Service
public class IValueGetterService {

    @Resource
    IRegistryCenter registryCenter;

    /**
     * 参数摘要：仅打印名字与值，避免大对象 toString 过长；参数过多时截断。
     */
    private static String summarizeParams(Collection<ParamValue> paramValues) {
        if (CollUtil.isEmpty(paramValues)) {
            return "[]";
        }
        return paramValues.stream().filter(Objects::nonNull)
            .map(pv -> safeToString(pv.getName()) + "=" + safeToString(pv.getValue()))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 声明参数摘要：打印 name(type) 列表，便于排查数量/顺序问题。
     */
    private static String summarizeParamTypes(List<ParamType> paramTypes) {
        if (CollUtil.isEmpty(paramTypes)) {
            return "[]";
        }
        return paramTypes.stream().filter(Objects::nonNull)
            .map(pt -> safeToString(pt.getName()) + "(" + safeToString(pt.getType()) + ")")
            .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 返回值摘要：value 为 Serializable，不直接 length；values 集合存在时只打印前几个。
     */
    private static String summarizeResult(ResultValue resultValue) {
        if (resultValue == null) {
            return "null";
        }
        Serializable value = resultValue.getValue();
        if (value != null) {
            return safeToString(value);
        }
        Collection<Serializable> values = resultValue.getValues();
        if (CollUtil.isEmpty(values)) {
            return "null";
        }
        String preview = values.stream().limit(10).map(IValueGetterService::safeToString)
            .collect(Collectors.joining(", ", "[", values.size() > 10 ? ", ...]" : "]"));
        return "values(size=" + values.size() + ")=" + preview;
    }

    /* ------------------------------ 日志辅助方法 ------------------------------ */

    private static String safeGetterName(IValueGetter getter) {
        try {
            return getter.getGetterName();
        } catch (Exception e) {
            // 打日志阶段抛异常会掩盖原始异常，这里吞掉
            return "unknown";
        }
    }

    private static String safeToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            String s = String.valueOf(obj);
            return s.length() > 100 ? s.substring(0, 100) + "..." : s;
        } catch (Exception e) {
            return "<toString异常:" + obj.getClass().getName() + ">";
        }
    }

    /**
     * 获取所有值获取器
     */
    public List<IValueGetter> getValueGetters(String nameLike, String idLike) {
        if (log.isDebugEnabled()) {
            log.debug("查询值获取器列表, nameLike={}, idLike={}", nameLike, idLike);
        }
        var contributors = registryCenter.getAllContributors().stream().flatMap(
            contributor -> contributor.getAllInstanceByTypeAndBusinessAndVersion(IValueGetter.class, null, null)
                .stream());
        if (StrUtil.isNotBlank(nameLike)) {
            contributors = contributors.filter(
                instance -> ((IValueGetter)instance.getInstance()).getGetterName().contains(nameLike));
        }
        if (StrUtil.isNotBlank(idLike)) {
            contributors =
                contributors.filter(instance -> ((IValueGetter)instance.getInstance()).getGetterId().contains(idLike));
        }
        List<IValueGetter> result =
            contributors.map(instance -> (IValueGetter)instance.getInstance()).collect(Collectors.toList());
        log.debug("查询值获取器列表完成, nameLike={}, idLike={}, 命中数量={}", nameLike, idLike, result.size());
        return result;
    }

    /**
     * 调用值获取器
     *
     * @param getterId    值获取器ID
     * @param paramValues 参数值
     * @return 结果值
     */
    public ResultValue invokeValueGetter(String getterId, Collection<ParamValue> paramValues) {
        if (StrUtil.isBlank(getterId)) {
            log.warn("调用值获取器失败, getterId 为空");
            throw new IllegalArgumentException("getterId is empty");
        }
        log.debug("调用值获取器开始, getterId={}, 参数数量={}", getterId, paramValues == null ? 0 : paramValues.size());

        var contributors = registryCenter.getAllContributors().stream().flatMap(
                contributor -> contributor.getAllInstanceByTypeAndBusinessAndVersion(IValueGetter.class, null, null)
                    .stream()).filter(instance -> getterId.equals(instance.getBusiness()))
            .map(instance -> (IValueGetter)instance.getInstance()).collect(Collectors.toList());
        IValueGetter valueGetter = CollUtil.getFirst(contributors);
        if (valueGetter == null) {
            log.warn("调用值获取器失败, 未找到值获取器, getterId={}", getterId);
            throw new IllegalArgumentException("未找到值获取器: " + getterId);
        }

        Collection<ParamValue> normalizedValues = paramValues;
        if (CollUtil.isNotEmpty(paramValues)) {
            try {
                normalizedValues = validateAndNormalizeParams(valueGetter, paramValues);
            } catch (IllegalArgumentException e) {
                log.warn("值获取器参数校验失败, getterId={}, 参数摘要={}, 原因={}", getterId,
                    summarizeParams(paramValues), e.getMessage());
                throw e;
            }
        }

        long start = System.currentTimeMillis();
        ResultValue resultValue;
        try {
            resultValue = valueGetter.getValue(normalizedValues);
        } catch (Exception e) {
            log.error("调用值获取器异常, getterId={}, getterName={}, 参数摘要={}, 耗时={}ms", getterId,
                safeGetterName(valueGetter), summarizeParams(normalizedValues), System.currentTimeMillis() - start, e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("调用值获取器完成, getterId={}, 参数摘要={}, 返回值={}, 耗时={}ms", getterId,
                summarizeParams(normalizedValues), summarizeResult(resultValue), System.currentTimeMillis() - start);
        }
        return resultValue;
    }

    /**
     * 根据值获取器声明的参数定义，对传入参数做校验并按声明顺序对齐。
     * <p>
     * 校验规则：
     * <ul>
     *     <li>传入参数数量不能超过声明参数数量；</li>
     *     <li>输入参数若都带 sort，先按 sort 升序重排，保证与声明参数的 sort 顺序对齐；</li>
     *     <li>声明为枚举的参数，传入值必须命中某个枚举 value；</li>
     *     <li>声明了参数但调用方少传时，以 null 占位，保证 getter 内部按位置读取不错位。</li>
     * </ul>
     */
    private List<ParamValue> validateAndNormalizeParams(IValueGetter valueGetter, Collection<ParamValue> paramValues) {
        List<ParamType> paramTypes = valueGetter.requestParamTypes();
        if (CollUtil.isEmpty(paramTypes)) {
            if (CollUtil.isNotEmpty(paramValues)) {
                log.debug("值获取器未声明参数, 忽略传入的 {} 个参数, getterId={}", paramValues.size(),
                    valueGetter.getGetterId());
            }
            return List.of();
        }
        // 按 sort 升序排列声明参数（sort 为空排到最后）
        List<ParamType> sortedTypes = paramTypes.stream()
            .sorted(Comparator.comparing(ParamType::getSort, Comparator.nullsLast(Integer::compareTo)))
            .collect(Collectors.toList());

        // 过滤掉 null 元素
        List<ParamValue> input = paramValues.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // 输入若都带 sort，按 sort 升序重排，避免调用方乱序传入导致位置错位
        if (!input.isEmpty() && input.stream().allMatch(pv -> pv.getSort() != null)) {
            input = input.stream()
                .sorted(Comparator.comparing(ParamValue::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        } else if (input.size() > 1) {
            log.debug("输入参数未全部携带 sort, 将按传入顺序与声明参数对齐, getterId={}", valueGetter.getGetterId());
        }

        if (input.size() > sortedTypes.size()) {
            String msg = "值获取器 [" +
                valueGetter.getGetterId() +
                "] 期望最多 " +
                sortedTypes.size() +
                " 个参数，实际传入 " +
                input.size() +
                " 个";
            log.warn("值获取器参数数量超限, getterId={}, 声明数量={}, 实际数量={}, 声明参数={}",
                valueGetter.getGetterId(), sortedTypes.size(), input.size(), summarizeParamTypes(sortedTypes));
            throw new IllegalArgumentException(msg);
        }
        if (input.size() < sortedTypes.size()) {
            log.debug("值获取器参数少于声明数量, 将以 null 占位对齐, getterId={}, 声明数量={}, 实际数量={}",
                valueGetter.getGetterId(), sortedTypes.size(), input.size());
        }

        List<ParamValue> result = new ArrayList<>(sortedTypes.size());
        for (int i = 0; i < sortedTypes.size(); i++) {
            ParamType expected = sortedTypes.get(i);
            ParamValue actual = i < input.size() ? input.get(i) : null;
            // 枚举参数校验
            if (actual != null &&
                Boolean.TRUE.equals(expected.getIsEnum()) &&
                CollUtil.isNotEmpty(expected.getEnumValues())) {
                boolean matched =
                    expected.getEnumValues().stream().anyMatch(ev -> Objects.equals(ev.getValue(), actual.getValue()));
                if (!matched) {
                    String allowed =
                        expected.getEnumValues().stream().map(EnumValue::getValue).collect(Collectors.joining(", "));
                    log.warn("值获取器枚举参数非法, getterId={}, 参数名={}, 传入值={}, 允许值={}",
                        valueGetter.getGetterId(), expected.getName(), actual.getValue(), allowed);
                    throw new IllegalArgumentException(
                        "参数 [" + expected.getName() + "] 的值 [" + actual.getValue() + "] 非法，允许的值: " + allowed);
                }
            }
            result.add(actual);
        }
        return result;
    }
}
