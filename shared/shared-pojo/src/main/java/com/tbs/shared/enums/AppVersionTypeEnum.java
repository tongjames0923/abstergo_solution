package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 应用版本类型枚举
 * <p>
 * 定义软件版本的发布阶段类型，编码值递增表示版本成熟度逐步提升。
 * 流转路径：ALPHA(1) -> BETA(2) -> RC(3) -> RELEASE(4)。
 *
 * @author abstergo
 */
public enum AppVersionTypeEnum implements BaseEnum<String> {
    /**
     * 版本发布阶段类型
     * <p>
     * 编码值越大表示版本成熟度越高，RELEASE(4) 为最终正式发布版本
     */
    ALPHA("alpha", 1), BETA("beta", 2), RC("rc", 3), RELEASE("release", 4);

    private String value;
    private int code;

    /**
     * 构造版本类型枚举实例
     *
     * @param value 版本类型的英文标识，用于系统间交互
     * @param code  版本类型编码，递增表示成熟度提升
     */
    AppVersionTypeEnum(String value, int code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 获取版本类型标识
     *
     * @return 版本类型的英文标识字符串
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取版本类型编码
     *
     * @return 版本类型的整数编码，值越大表示成熟度越高
     */
    @Override
    public Integer getCode() {
        return code;
    }

}
