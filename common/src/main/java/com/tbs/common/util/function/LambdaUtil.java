package com.tbs.common.util.function;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.function.SFunction;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//FIXME 需要完善这些代码的逻辑，field和method部分功能似乎一样

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
                    AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
                }
                return null;
            });

        } catch (Exception e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
        }
        return null;
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
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
        }
        return null;
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
                    AssertUtil.throwError(() -> ExceptionUtil.newException("未找到对应的方法"));
                } catch (Exception e) {
                    AssertUtil.throwError(() -> ExceptionUtil.newException(e, "获取方法异常"));
                }
                return null;
            });
        } catch (Exception e) {
            AssertUtil.throwError(() -> ExceptionUtil.newException(e, "获取方法异常"));
        }
        return null;
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
            AssertUtil.throwError(
                () -> new FormattedRuntimeException(e, "Failed to extract method name from lambda").errorLevel(
                    ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }
        return null;
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
