package com.tbs.logging.demo;

import lombok.Getter;

/**
 * 泛化调用演示状态枚举
 * <p>
 * 用于验证枚举入参的还原路径：登记类型为枚举自身的 FQCN，请求体里传枚举名字符串，
 * 由服务端按声明类型还原成枚举实例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Getter
public enum StandaloneDemoStatus {

    /**
     * 待处理
     */
    PENDING("待处理"),

    /**
     * 处理中
     */
    RUNNING("处理中"),

    /**
     * 已完成
     */
    DONE("已完成");

    /**
     * 展示文案
     */
    private final String label;

    StandaloneDemoStatus(String label) {
        this.label = label;
    }
}
