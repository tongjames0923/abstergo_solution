package com.tbs.common.config;

import com.tbs.common.annotation.DynamicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 动态配置绑定器：扫描容器内所有 Bean 的 {@link DynamicConfig} 字段/单参 setter，
 * 启动时按 key 从 {@link Environment} 初绑一次，运行期在配置刷新时对相应位热更新。
 * <p>
 * 设计要点：热更新只「改写可变位」、复用同一实例，因此对被绑对象自身的其它状态（字段、计数器等）零影响，
 * 这正是相对 {@code @RefreshScope} 重建方案对有状态组件更安全的原因。
 * </p>
 * <p>
 * 对 AOP 代理 Bean：先解包到目标对象再反射写入，规避「写代理字段而目标不变」的失效。
 * 值转换走 {@link DefaultConversionService}，支持标量（int/long/boolean/String 等）；key 缺失或转换失败保留原值。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DynamicConfigBinder implements BeanPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(DynamicConfigBinder.class);

    /** 配置项 key -> 该 key 对应的所有绑定位，一个 key 可被多个字段/多个 Bean 复用。 */
    private final Map<String, List<Target>> targetsByKey = new ConcurrentHashMap<>();

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object target = resolveTarget(bean);
        Class<?> targetType = target.getClass();
        // 字段绑定位
        ReflectionUtils.doWithFields(targetType, field -> {
            DynamicConfig ann = field.getAnnotation(DynamicConfig.class);
            if (ann == null) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            register(ann.value(), new FieldTarget(target, field));
        });
        // setter 绑定位（单参数方法）
        ReflectionUtils.doWithMethods(targetType, method -> {
            DynamicConfig ann = method.getAnnotation(DynamicConfig.class);
            if (ann == null) {
                return;
            }
            if (method.getParameterCount() != 1) {
                log.warn("[DynamicConfig] 忽略非法 setter（须恰好一个参数）: {}#{}", targetType.getName(), method.getName());
                return;
            }
            ReflectionUtils.makeAccessible(method);
            register(ann.value(), new MethodTarget(target, method));
        });
        return bean;
    }

    /**
     * 对变更的配置 key 重新取值并写回所有绑定位。
     *
     * @param changedKeys 本次变更的配置 key 集合（如 Nacos 刷新后的 diff）
     */
    public void rebind(Collection<String> changedKeys) {
        if (changedKeys == null) {
            return;
        }
        for (String key : changedKeys) {
            List<Target> targets = targetsByKey.get(key);
            if (targets == null) {
                continue;
            }
            for (Target target : targets) {
                applyOne(key, target);
            }
        }
    }

    /**
     * 注册一个绑定位并立即做启动初绑。
     *
     * @param key    配置项 key
     * @param target 绑定位（字段或 setter）
     */
    private void register(String key, Target target) {
        targetsByKey.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(target);
        applyOne(key, target);
    }

    /**
     * 读取 key 当前值并写入绑定位；缺失或转换失败则保留原值。
     *
     * @param key    配置项 key
     * @param target 绑定位
     */
    private void applyOne(String key, Target target) {
        String raw = environment.getProperty(key);
        if (raw == null) {
            return;
        }
        Object converted;
        try {
            converted = DefaultConversionService.getSharedInstance().convert(raw, wrapPrimitive(target.getValueType()));
        } catch (Exception e) {
            log.warn("[DynamicConfig] key={} 值[{}] 转换为 {} 失败，保留原值", key, raw, target.getValueType().getSimpleName(), e);
            return;
        }
        if (converted == null) {
            return;
        }
        try {
            target.accept(converted);
            log.info("[DynamicConfig] 热更新 {} = {}", key, converted);
        } catch (Exception e) {
            log.error("[DynamicConfig] 写入 key={} 失败", key, e);
        }
    }

    /**
     * 基本类型转对应包装类：ConversionService 对 {@code int.class} 等基本类型目标查找转换器不稳定，
     * 统一转成包装类转换，反射写入时再由 JDK 自动拆箱。
     *
     * @param type 目标类型
     * @return 包装类（非基本类型原样返回）
     */
    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        return type;
    }

    /**
     * 解包 AOP 代理到目标对象；非代理直接返回。字段反射写入必须作用到目标，否则代理字段改了目标不变。
     *
     * @param bean 可能被代理的 Bean
     * @return 目标对象
     */
    private Object resolveTarget(Object bean) {
        if (bean instanceof Advised advised) {
            try {
                Object target = advised.getTargetSource().getTarget();
                if (target != null) {
                    return target;
                }
            } catch (Exception e) {
                log.debug("[DynamicConfig] 解包代理失败，按原 Bean 处理: {}", bean.getClass().getName(), e);
            }
        }
        return bean;
    }

    /**
     * 绑定位抽象：一个可被动态配置写入的字段或 setter。
     */
    private interface Target {

        /**
         * @return 目标值类型（字段类型或 setter 唯一入参类型），用于类型转换
         */
        Class<?> getValueType();

        /**
         * 写入已转换后的值。
         *
         * @param value 转换后的值
         * @throws Exception 反射写入异常
         */
        void accept(Object value) throws Exception;
    }

    /**
     * 字段绑定位。
     */
    private static final class FieldTarget implements Target {

        private final Object bean;
        private final Field field;

        FieldTarget(Object bean, Field field) {
            this.bean = bean;
            this.field = field;
        }

        @Override
        public Class<?> getValueType() {
            return field.getType();
        }

        @Override
        public void accept(Object value) throws IllegalAccessException {
            field.set(bean, value);
        }
    }

    /**
     * setter 方法绑定位。
     */
    private static final class MethodTarget implements Target {

        private final Object bean;
        private final Method method;

        MethodTarget(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }

        @Override
        public Class<?> getValueType() {
            return method.getParameterTypes()[0];
        }

        @Override
        public void accept(Object value) throws Exception {
            method.invoke(bean, value);
        }
    }
}
