package com.tbs.web.genric.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 泛化调用配置（新版链路的中立描述模型）
 * <p>
 * 一行 {@code sys_generic_api} 配置加上其参数子表登记的聚合视图。{@code web} 模块不依赖实体包，
 * 因此由接入侧（app 控制器）从实体映射而来；本模型只承载调用所需事实，不含任何校验规则——
 * 合法性在 Service 层断言，装配阶段只做形态转换。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericApiConfig {

    /**
     * API 唯一标识
     */
    private String apiId;

    /**
     * 目标接口全限定名（网关 classpath 上不需要存在）
     */
    private String interfaceName;

    /**
     * 目标方法名
     */
    private String methodName;

    /**
     * 业务分区坐标，映射 Dubbo 引用的 {@code group}；空表示不约束
     */
    private String business;

    /**
     * 登记版本，映射 Dubbo 引用的 {@code version}；{@code null} 表示不约束
     */
    private Integer version;

    /**
     * 是否需要登录态
     */
    private boolean needAuth;

    /**
     * 预认证处理器标识，逗号分隔，沿用既有 {@code IApiInstancePreAuth} 注册表
     */
    private String preAuthBeans;

    /**
     * 入参定义，按签名顺序
     */
    private List<GenericApiParamConfig> params = new ArrayList<>();
}
