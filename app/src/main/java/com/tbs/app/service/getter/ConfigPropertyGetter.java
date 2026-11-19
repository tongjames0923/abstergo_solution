package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 配置属性取值器。
 *
 * <p>按 key 读取 Spring 配置值，作为开关或阈值类事件条件来源。仅允许白名单前缀且非敏感的 key，
 * 被拦截或配置不存在时返回调用方给定的默认值（未提供则为空字符串），不抛异常中断脚本执行。</p>
 *
 * @author abstergo
 */
@Slf4j
@Component
public class ConfigPropertyGetter extends AbstractStringValueGetter {

    /**
     * 允许读取的配置前缀白名单
     */
    private static final List<String> ALLOWED_PREFIXES = List.of("tbs.", "app.", "server.");

    /**
     * 允许读取的精确配置 key
     */
    private static final Set<String> ALLOWED_EXACT_KEYS = Set.of("spring.application.name");

    /**
     * 敏感配置关键词，命中即拒绝读取
     */
    private static final List<String> SENSITIVE_KEYWORDS = List.of("password", "secret", "token", "credential",
        "accesskey", "privatekey", "apikey");

    /**
     * Spring 环境
     */
    @Resource
    private Environment environment;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.config-property.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "ConfigPropertyGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.config-property.description");
    }

    /**
     * 声明参数定义：配置 key 与可选默认值。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(ParamType.builder().type("string").name(text("value.getter.config-property.param.key.name"))
            .description(text("value.getter.config-property.param.key.description")).sort(1).build(),
            ParamType.builder().type("string").name(text("value.getter.config-property.param.default.name"))
                .description(text("value.getter.config-property.param.default.description")).sort(2).build());
    }

    /**
     * 读取配置属性值。
     *
     * @param paramValues 参数值集合，首个参数为配置 key，第二个参数为可选默认值
     * @return 字符串结果；配置不存在、key 被拦截或异常时返回默认值或空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String key = paramString(paramValues, 0);
        String defaultValue = paramString(paramValues, 1);
        String fallback = defaultValue == null ? "" : defaultValue;
        if (key == null || key.isBlank()) {
            return stringResult(fallback);
        }
        if (!isAllowed(key)) {
            log.debug("配置属性取值器拒绝读取敏感或不在白名单内的配置, key={}", key);
            return stringResult(fallback);
        }
        try {
            String value = environment.getProperty(key, fallback);
            return stringResult(value == null ? fallback : value);
        } catch (Exception e) {
            return stringResult(fallback);
        }
    }

    /**
     * 校验配置 key 是否在允许范围内。
     *
     * @param key 配置 key
     * @return 允许读取时返回 true
     */
    private boolean isAllowed(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_KEYWORDS.stream().anyMatch(lower::contains)) {
            return false;
        }
        return ALLOWED_EXACT_KEYS.contains(lower) || ALLOWED_PREFIXES.stream().anyMatch(lower::startsWith);
    }
}
