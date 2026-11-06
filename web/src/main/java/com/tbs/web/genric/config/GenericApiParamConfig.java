package com.tbs.web.genric.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 泛化调用入参定义
 * <p>
 * 对应 {@code sys_generic_api_param} 的一行，是新版链路装配 Dubbo 泛化调用的唯一依据：
 * 顺序决定实参位置，类型决定 {@code paramTypes}，名称决定命名式入参如何匹配。
 * 同一模型也直接作为元数据接口的出参，供前端渲染表单与预填示例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericApiParamConfig {

    /**
     * 参数在方法签名中的位置，从 0 开始
     */
    private Integer order;

    /**
     * 参数名；留空表示该参数只接受按位置传入
     */
    private String name;

    /**
     * 擦除后的 JVM 声明类型名（如 {@code java.lang.String}、{@code java.util.List}）或基本类型关键字（如 {@code long}）
     */
    private String type;

    /**
     * 是否必填；请求缺省且本项为真时报错
     */
    private boolean required;

    /**
     * 参数语义说明
     */
    private String description;

    /**
     * 示例值（JSON 字面量文本）
     */
    private String example;
}
