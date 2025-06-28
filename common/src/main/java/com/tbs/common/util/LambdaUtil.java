package com.tbs.common.util;

import com.tbs.common.SFunction;
import com.tbs.common.exception.FormattedRuntimeException;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lambda 工具类，用于从 Lambda 表达式中提取对应的 Field、Method 或字段名/方法名
 */
public class LambdaUtil {

    // 缓存字段
    private static final ConcurrentMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * 从 Lambda 表达式中提取对应的 Field 对象
     *
     * @param func 函数式接口，例如 UserEntity::getId
     * @return Field 对象
     */
    public static <T, R> Field getField(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String key = buildCacheKey(lambda);

            return FIELD_CACHE.computeIfAbsent(key, k -> {
                try {
                    String methodName = lambda.getImplMethodName();
                    String fieldName;
                    if (methodName.startsWith("get") || methodName.startsWith("set")) {
                        fieldName = Introspector.decapitalize(methodName.substring(3));
                    } else if (methodName.startsWith("is")) {
                        fieldName = Introspector.decapitalize(methodName.substring(2));
                    } else {
                        fieldName = Introspector.decapitalize(methodName);
                    }

                    Class<?> targetClass = Class.forName(lambda.getImplClass().replace('/', '.'));
                    return targetClass.getDeclaredField(fieldName);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to extract field from lambda", e);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract field from lambda", e);
        }
    }

    /**
     * 提取字段名字符串
     *
     * @param func 函数式接口，例如 UserEntity::getId
     * @return 字段名
     */
    public static <T, R> String extractFieldName(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String key = buildCacheKey(lambda);

            return FIELD_NAME_CACHE.computeIfAbsent(key, k -> {
                String methodName = lambda.getImplMethodName();
                if (methodName.startsWith("get") || methodName.startsWith("set")) {
                    return Introspector.decapitalize(methodName.substring(3));
                } else if (methodName.startsWith("is")) {
                    return Introspector.decapitalize(methodName.substring(2));
                }
                return methodName;
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract field name from lambda", e);
        }
    }

    /**
     * 从 Lambda 表达式中提取对应的 Method 对象
     *
     * @param func 函数式接口，例如 UserEntity::getId
     * @return Method 对象
     */
    public static <T, R> Method getMethod(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String key = buildCacheKey(lambda);

            return METHOD_CACHE.computeIfAbsent(key, k -> {
                try {
                    String methodName = lambda.getImplMethodName();
                    Class<?> targetClass = Class.forName(lambda.getImplClass().replace('/', '.'));

                    for (Method method : targetClass.getMethods()) {
                        if (method.getName().equals(methodName)) {
                            return method;
                        }
                    }
                    throw new FormattedRuntimeException("No method found with name: " + methodName);
                } catch (Exception e) {
                    throw new FormattedRuntimeException(e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract method from lambda", e);
        }
    }

    /**
     * 提取方法名字符串（去除 get/set/is 前缀）
     *
     * @param func 函数式接口，例如 UserEntity::getId
     * @return 方法名
     */
    public static <T, R> String extractMethodName(SFunction<T, R> func) {
        try {
            SerializedLambda lambda = extractSerializedLambda(func);
            String methodName = lambda.getImplMethodName();

            if (methodName.startsWith("get") || methodName.startsWith("set")) {
                return Introspector.decapitalize(methodName.substring(3));
            } else if (methodName.startsWith("is")) {
                return Introspector.decapitalize(methodName.substring(2));
            }

            return methodName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract method name from lambda", e);
        }
    }

    // === 私有辅助方法 ===

    private static SerializedLambda extractSerializedLambda(SFunction<?, ?> func) throws Exception {
        Method replaceMethod = func.getClass().getDeclaredMethod("writeReplace");
        replaceMethod.setAccessible(true);
        return (SerializedLambda)replaceMethod.invoke(func);
    }

    private static String buildCacheKey(SerializedLambda lambda) {
        return lambda.getImplClass() + "." + lambda.getImplMethodName();
    }
}
