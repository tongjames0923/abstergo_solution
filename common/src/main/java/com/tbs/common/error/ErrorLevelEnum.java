package com.tbs.common.error;

import com.tbs.common.BaseEnum;
import lombok.Getter;

/**
 * 错误级别枚举类
 * <p>
 * 该枚举定义了系统中不同类型的错误级别，按照功能重要性和影响范围进行分类。 从用户输入错误到严重系统错误，级别数字越大表示严重程度越高。
 * </p>
 *
 * @author tongj
 */
@Getter
public enum ErrorLevelEnum implements BaseEnum<String> {
    /**
     * 用户输入错误 - 最低级别错误 由用户输入不正确或业务规则校验失败导致的错误
     */
    USER_INPUT_ERROR(1, "用户输入错误"),

    /**
     * 业务逻辑错误 - 次低级别错误 在业务逻辑执行过程中出现的常规运行时错误
     */
    BUSINESS_LOGIC_ERROR(2, "业务逻辑错误"),

    /**
     * 数据访问错误 - 中等级别错误 与数据库操作和数据访问相关的错误
     */
    DATA_ACCESS_ERROR(3, "数据访问错误"),

    /**
     * 系统错误 - 高级别错误 涉及系统核心功能和基础设施的错误
     */
    SYSTEM_ERROR(4, "系统错误"),

    /**
     * 严重系统错误 - 最高级别错误 导致系统关键功能无法正常运行的严重错误
     */
    CRITICAL_SYSTEM_ERROR(5, "严重系统错误");

    /**
     * 错误级别代码
     */
    private Integer code;

    /**
     * 错误级别描述
     */
    private String value;

    /**
     * 构造函数
     *
     * @param code  错误级别代码，数字越大表示严重程度越高
     * @param value 错误级别的文本描述
     */
    ErrorLevelEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}
