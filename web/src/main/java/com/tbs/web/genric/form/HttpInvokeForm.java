package com.tbs.web.genric.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 泛化 API 调用表单
 * <p>
 * 作为 {@code POST /invoke} 的请求体：携带目标 apiId/version 与参数列表，由
 * {@link com.tbs.web.genric.AbstractGenricApiController} 解析并路由到对应实例。
 * </p>
 * <p>
 * 仅旧链路使用：新入口 {@code POST /call/{apiId}} 不接收本结构（apiId 走路径变量，参数走命名/位置 JSON）。
 * 尚未标 @Deprecated 是因为 {@link com.tbs.web.genric.IApiInstancePreAuth} 的活签名仍在引用它（新链路传 {@code null}），
 * 需先改造 SPI 参数才谈得上删除；旧入口弃用范围见 {@link com.tbs.web.genric.AbstractGenricApiController}。
 * </p>
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpInvokeForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 调用的接口id
     */
    private String id;
    /**
     * 调用的版本
     */
    private Integer version;
    /**
     * 调用的参数列表
     */
    List<GenricParameterForm> parameters;
}
