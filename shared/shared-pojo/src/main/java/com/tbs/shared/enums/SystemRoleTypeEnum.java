package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 系统角色类型枚举
 *
 * <p>定义系统中不同类型的权限分类，用于区分权限的作用域和应用场景。
 * 每种类型对应不同的权限检查逻辑和数据访问策略。</p>
 *
 * <ul>
 *   <li>{@link #MENU_ROLE} - 菜单权限，控制前端菜单和页面的可见性</li>
 *   <li>{@link #DATA_ROLE} - 数据权限，控制数据的访问范围（全部/部门/本人）</li>
 *   <li>{@link #API_ROLE} - API权限，控制接口的访问权限</li>
 * </ul>
 *
 * @author abstergo
 * @see BaseEnum
 */
public enum SystemRoleTypeEnum implements BaseEnum<String> {

    /**
     * 菜单权限，控制前端菜单和页面的可见性
     */
    MENU_ROLE("菜单权限", 1),

    /**
     * 数据权限，控制数据的访问范围
     */
    DATA_ROLE("数据权限", 2),

    /**
     * 角色权限，传统角色概念
     */
    CHARACTER_ROLE("API权限", 3);

    /**
     * 权限类型描述
     */
    private String val;

    /**
     * 权限类型编码
     */
    private Integer code;

    /**
     * 构造函数，初始化枚举实例
     *
     * @param val  权限类型描述
     * @param code 权限类型编码
     */
    SystemRoleTypeEnum(String val, Integer code) {
        this.val = val;
        this.code = code;
    }

    /**
     * 获取权限类型描述
     *
     * @return 权限类型描述字符串
     */
    @Override
    public String getValue() {
        return val;
    }

    /**
     * 获取权限类型编码
     *
     * @return 权限类型编码
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
