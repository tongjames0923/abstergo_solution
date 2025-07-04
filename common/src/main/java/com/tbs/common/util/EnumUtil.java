package com.tbs.common.util;

import com.tbs.common.BaseEnum;

/**
 * 枚举工具类
 *
 * @author abstergo
 */
public class EnumUtil {
    /**
     * 根据 code 查找匹配的枚举值
     */
    public static <E extends Enum<E> & BaseEnum> E valueOf(Class<E> enumClass, int code) {
        for (E en : enumClass.getEnumConstants()) {
            if (en.getCode() == code) {
                return en;
            }
        }
        return null;
    }

    /**
     * 根据 value 获取枚举值
     */
    public static <E extends Enum<E> & BaseEnum> E valueOf(Class<E> enumClass, String value) {
        for (E en : enumClass.getEnumConstants()) {
            if (en.getValue().equals(value)) {
                return en;
            }
        }
        return null;
    }

    /**
     * 判断 code 是否合法
     */
    public static <E extends Enum<E> & BaseEnum> boolean isValidCode(Class<E> enumClass, int code) {
        return valueOf(enumClass, code) != null;
    }
}
