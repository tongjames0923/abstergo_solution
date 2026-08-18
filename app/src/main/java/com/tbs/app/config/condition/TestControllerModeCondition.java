package com.tbs.app.config.condition;

import com.tbs.app.config.property.TestControllerMode;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Map;

/**
 * 测试控制器模式条件匹配器
 * <p>
 * 根据 app.gateway.enable-test-controller 配置值与注解指定的模式进行匹配，
 * 支持多值"或"语义（任意一个匹配即激活）
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TestControllerModeCondition implements Condition {

    private static final String PROPERTY_KEY = "app.gateway.enable-test-controller";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(
            ConditionalOnTestControllerMode.class.getName());
        if (attrs == null) {
            return false;
        }
        TestControllerMode[] allowedModes = (TestControllerMode[]) attrs.get("value");
        String property = context.getEnvironment().getProperty(PROPERTY_KEY, TestControllerMode.NONE.name());
        try {
            TestControllerMode currentMode = TestControllerMode.valueOf(property.toUpperCase());
            return Arrays.asList(allowedModes).contains(currentMode);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
