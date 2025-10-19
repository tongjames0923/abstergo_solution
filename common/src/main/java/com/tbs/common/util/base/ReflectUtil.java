package com.tbs.common.util.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 反射工具类
 *
 * @author abstergo
 */
public class ReflectUtil {
    /**
     * 判断字段是否是静态字段或者final字段
     *
     * @param field
     * @return
     */
    public static boolean isStaticOrFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers);
    }
}
