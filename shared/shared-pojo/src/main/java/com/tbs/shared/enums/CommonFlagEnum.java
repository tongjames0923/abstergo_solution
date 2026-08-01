package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 通用标识枚举，实现BaseEnum接口。
 *
 * <p>用于定义系统中通用的状态标识，如启用、禁用、删除等。
 * 该枚举广泛应用于实体的逻辑删除和状态管理。</p>
 *
 * <ul>
 *   <li>{@link #ACTIVE} - 启用状态，表示记录有效可用</li>
 *   <li>{@link #INACTIVE} - 禁用状态，表示记录暂时不可用</li>
 *   <li>{@link #DELETED} - 删除状态，表示记录已被逻辑删除</li>
 * </ul>
 *
 * @author abstergo
 * @see BaseEnum
 */
public enum CommonFlagEnum implements BaseEnum<String> {

    /**
     * 启用状态，表示记录有效可用
     */
    ACTIVE("启用", 1),

    /**
     * 禁用状态，表示记录暂时不可用
     */
    INACTIVE("禁用", -1),

    /**
     * 删除状态，表示记录已被逻辑删除
     */
    DELETED("删除", 0);

    /**
     * 状态描述
     */
    private String value;

    /**
     * 状态编码
     */
    private Integer code;

    /**
     * 构造函数，初始化枚举实例
     *
     * @param val  状态描述
     * @param code 状态编码
     */
    CommonFlagEnum(String val, Integer code) {
        this.value = val;
        this.code = code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述字符串
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取状态编码
     *
     * @return 状态编码
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
