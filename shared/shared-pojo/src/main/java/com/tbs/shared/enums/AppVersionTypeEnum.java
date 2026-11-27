package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 应用版本类型枚举
 * <p>
 * 定义软件版本的发布阶段类型。编码沿用前端历史约定，与成熟度递增无关。
 * 历史约定：RELEASE(1) -> BETA(2) -> ALPHA(3) -> RC(4)。
 *
 * @author abstergo
 */
public enum AppVersionTypeEnum implements BaseEnum<String> {
    /**
     * 版本发布阶段类型
     * <p>
     * 历史编码约定：RELEASE(1)、BETA(2)、ALPHA(3)、RC(4)
     */
    ALPHA("alpha", 3), BETA("beta", 2), RC("rc", 4), RELEASE("release", 1);

    private String value;
    private int code;

    /**
     * 构造版本类型枚举实例
     *
     * @param value 版本类型的英文标识，用于系统间交互
     * @param code  版本类型编码，沿用前端历史约定
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
     * @return 版本类型的整数编码，沿用前端历史约定
     */
    @Override
    public Integer getCode() {
        return code;
    }

}
