package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * OSS下载码标志枚举
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum OssDownloadCodeFlagEnum implements BaseEnum<String> {

    /**
     * 有效状态，下载码可正常使用
     */
    ACTIVE("有效", 1),

    /**
     * 已删除状态，逻辑删除标记，下载码立即失效
     */
    DELETED("已删除", -1);

    private final String value;

    private final Integer code;

    OssDownloadCodeFlagEnum(String value, Integer code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 获取枚举描述
     *
     * @return 枚举描述
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举编码
     *
     * @return 枚举编码
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
