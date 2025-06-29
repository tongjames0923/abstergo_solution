package com.tbs.web.enums;

import com.tbs.common.BaseEnum;

/**
 * Token来源枚举类
 * <p>
 * 该枚举定义了Token可以从哪些位置获取，每个枚举值都对应一个具体的来源方式。 实现了BaseEnum接口，提供了标准的getValue和getCode方法以获取枚举值的字符串表示和整型代码。
 * </p>
 * @author tongj
 */
public enum TokenSource implements BaseEnum<String> {
    /**
     * 来自Cookie的Token
     * <p>
     * 表示Token是从HTTP请求的Cookie中提取的。
     * </p>
     */
    COOKIE("cookie", 1),

    /**
     * 来自Header的Token
     * <p>
     * 表示Token是从HTTP请求头（如Authorization头）中提取的。
     * </p>
     */
    HEADER("header", 2),

    /**
     * 来自URL参数的Token
     * <p>
     * 表示Token是从URL查询参数中提取的。
     * </p>
     */
    PARAM("param", 3),

    /**
     * 来自请求体的Token
     * <p>
     * 表示Token是从HTTP请求体（如JSON或表单数据）中提取的。
     * </p>
     */
    BODY("body", 4);

    /**
     * 枚举值的字符串表示
     */
    private final String value;

    /**
     * 枚举值的整型代码
     */
    private final int code;

    /**
     * 构造函数
     *
     * @param value 枚举值的字符串表示
     * @param code  枚举值的整型代码
     */
    TokenSource(String value, int code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 获取枚举值的字符串表示
     *
     * @return 枚举值的字符串表示
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举值的整型代码
     *
     * @return 枚举值的整型代码
     */
    @Override
    public int getCode() {
        return code;
    }
}
// ... existing code ...
