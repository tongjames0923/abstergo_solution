package com.tbs.app.config.condition;

import com.tbs.app.config.property.OssDownloadCodeStorageType;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Map;

/**
 * 下载码存储类型条件匹配器
 * <p>
 * 根据 tbs.oss.download-code.storage 配置值与注解指定的存储类型进行匹配，
 * 支持多值"或"语义（任意一个匹配即激活）；
 * 配置缺省时按REDIS处理，配置值非法时不满足任何条件（启动期装配失败即暴露配置错误）
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DownloadCodeStorageCondition implements Condition {

    /**
     * 存储类型配置键
     */
    private static final String PROPERTY_KEY = "tbs.oss.download-code.storage";

    /**
     * 缺省存储类型
     */
    private static final OssDownloadCodeStorageType DEFAULT_STORAGE = OssDownloadCodeStorageType.REDIS;

    /**
     * 判断当前配置的存储类型是否在注解允许的范围内
     *
     * @param context 条件上下文
     * @param metadata 注解元数据
     * @return 匹配返回true
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(
            ConditionalOnDownloadCodeStorage.class.getName());
        if (attrs == null) {
            return false;
        }
        OssDownloadCodeStorageType[] allowedTypes = (OssDownloadCodeStorageType[]) attrs.get("value");
        String property = context.getEnvironment().getProperty(PROPERTY_KEY, DEFAULT_STORAGE.name());
        try {
            OssDownloadCodeStorageType currentType = OssDownloadCodeStorageType.valueOf(property.toUpperCase());
            return Arrays.asList(allowedTypes).contains(currentType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
