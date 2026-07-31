package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 系统数据权限范围枚举，实现BaseEnum接口。 用于定义系统中不同的数据访问权限范围。
 *
 * @author abstergo
 */
public enum SystemDataScopeEnum implements BaseEnum<String> {
    /** 全部数据权限 */
    ALL("全部数据", 1),
    /** 部门数据权限 */
    DEPT("部门数据", 2),

    /** 仅本人数据权限 */
    SELF("仅本人数据", 3),
    /** 自定义权限 */
    CUSTOM("自定义权限", 4);

    private String val;
    private Integer code;

    /**
     * 构造函数，初始化枚举实例。
     *
     * @param val  枚举值的描述
     * @param code 枚举值的编码
     */
    SystemDataScopeEnum(String val, Integer code) {
        this.val = val;
        this.code = code;
    }

    /**
     * 获取枚举值的描述。
     *
     * @return 枚举值的描述
     */
    @Override
    public String getValue() {
        return val;
    }

    /**
     * 获取枚举值的编码。
     *
     * @return 枚举值的编码
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
