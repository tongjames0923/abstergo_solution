package com.tbs.shared.enums;

import com.tbs.common.BaseEnum;

/**
 * 应用版本兼容动作枚举
 * <p>
 * 定义兼容策略命中后对目标应用客户端触发的更新动作类型，
 * 编码值越大表示更新要求的强制程度越高。
 *
 * @author abstergo
 */
public enum AppVersionCompatibilityActionEnum implements BaseEnum<String> {
    /**
     * 强制更新，低于最低兼容版本时阻断使用直至升级
     */
    FORCE("force", 1),
    /**
     * 推荐更新，提示用户升级但不阻断使用
     */
    RECOMMEND("recommend", 2),
    /**
     * 仅提示，告知用户存在新版本不做进一步干预
     */
    NOTIFY("notify", 3);

    private String value;
    private int code;

    /**
     * 构造兼容动作枚举实例
     *
     * @param value 动作类型的英文标识，用于系统间交互
     * @param code  动作类型编码，值越大强制程度越高
     */
    AppVersionCompatibilityActionEnum(String value, int code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 获取动作类型标识
     *
     * @return 动作类型的英文标识字符串
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取动作类型编码
     *
     * @return 动作类型的整数编码
     */
    @Override
    public Integer getCode() {
        return code;
    }
}
