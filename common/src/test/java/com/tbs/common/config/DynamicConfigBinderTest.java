package com.tbs.common.config;

import com.tbs.common.annotation.DynamicConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DynamicConfigBinder} 自检：验证字段与单参 setter 的启动初绑 + 配置刷新热更新 + 类型转换。
 * <p>
 * 非 Spring 上下文级测试——直接驱动绑定器的核心方法，聚焦「复用同一实例、只改可变位」的关键行为。
 * </p>
 *
 * @author abstergo
 */
class DynamicConfigBinderTest {

    private static final String KEY = "demo.threshold";

    @Test
    void shouldBindFieldAndSetterOnStartupAndRebindOnChange() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(KEY, "50");

        DynamicConfigBinder binder = new DynamicConfigBinder();
        binder.setEnvironment(env);

        DemoBean bean = new DemoBean();
        binder.postProcessAfterInitialization(bean, "demo");

        // 启动初绑：字段与 setter 均按 int 目标类型从字符串转换
        assertEquals(50, bean.threshold);
        assertEquals(50, bean.setterValue);

        // 变更未在跟踪的 key —— 不应误伤
        binder.rebind(Set.of("other.key"));
        assertEquals(50, bean.threshold);

        // 热更新：改配置后按 key 重绑，写回同一实例的可变位
        env.setProperty(KEY, "88");
        binder.rebind(Set.of(KEY));
        assertEquals(88, bean.threshold);
        assertEquals(88, bean.setterValue);
    }

    /**
     * 被测目标：一个字段绑定位 + 一个 setter 绑定位，共用同一配置 key。
     */
    static class DemoBean {

        @DynamicConfig(KEY)
        volatile int threshold;

        int setterValue;

        @DynamicConfig(KEY)
        void setSetterValue(int value) {
            this.setterValue = value;
        }
    }
}
