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
     * 业务分区坐标，映射 Dubbo 引用的 {@code group}；空=只匹配未声明 group 的提供者，{@code *} 才是不限
     */
    private String business;

    /**
     * 业务配置版本：用于区分同一 apiId 的不同登记，<b>不映射</b> Dubbo 服务版本
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
