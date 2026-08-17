package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 应用版本标志枚举
 * <p>
 * 定义应用版本从创建到终止的完整生命周期状态，各状态编码规则如下：
 * <ul>
 *     <li>正数编码（0, 1, 2）：版本活跃阶段状态</li>
 *     <li>负数编码（-1, -2, -3）：版本终止阶段状态</li>
 * </ul>
 * 生命周期流转路径：DRAFT(0) -> PENDING(2) -> RELEASE(1) -> OFFLINE(-1)，
 * 其中 RELEASE 可直接流转至 ROLLED_BACK(-2)，OFFLINE 可流转至 ARCHIVED(-3)。
 *
 * @author abstergo
 */
public enum AppVersionFlagEnum implements BaseEnum<String> {
    /**
     * 版本生命周期状态枚举
     * <p>
     * 编码为正数表示活跃阶段状态，编码为负数表示终止阶段状态
     */
    DRAFT("草稿", 0), PENDING("待审核", 2), RELEASE("发布", 1), OFFLINE("下线", -1), ROLLED_BACK("回滚", -2),
    ARCHIVED("归档", -3);

    private String val;
    private Integer code;

    /**
     * 构造版本标志枚举实例
     *
     * @param val  版本状态的中文描述，用于界面展示
     * @param code 版本状态编码，正数为活跃阶段，负数为终止阶段
     */
    AppVersionFlagEnum(String val, Integer code) {
        this.val = val;
        this.code = code;
    }

    /**
     * 获取枚举详情值
     * <p>
     * 返回枚举项的详细描述信息，通常是一个具有业务含义的字符串， 用于在界面展示或日志记录中提供更友好的信息。
     *
     * @return 枚举详情值
     */
    @Override
    public String getValue() {
        return val;
    }

    /**
     * 获取枚举编码
     * <p>
     * 返回枚举项的整数编码，通常用于数据库存储或系统内部处理， 编码值应该是唯一的，便于程序识别和处理不同的枚举项。
     *
     * @return 枚举编码值
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
