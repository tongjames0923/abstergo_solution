package com.tbs.app.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 网关配置属性类
 * <p>
 * 通过@ConfigurationProperties注解绑定app.gateway前缀的配置属性 用于配置网关版本信息和视图路由分组
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties("app.gateway")
public class GatewayProperty {

    /**
     * 测试控制器启用模式
     * <p>
     * 可选值：all（全部开启）、none（全部关闭）、pub（仅公开）、pri（仅私有），默认为none
     * </p>
     */
    private TestControllerMode enableTestController = TestControllerMode.NONE;

    /**
     * 网关版本标识
     * <p>
     * 优先取启动参数 APP_GATEWAY_VERSION，否则为 Maven 资源过滤注入的 pom 版本号 可用启动参数覆盖，如：-DAPP_GATEWAY_VERSION=1.2.3-hotfix
     * </p>
     */
    private String version = "";

    /**
     * 视图路由分组配置
     * <p>
     * 用于指定需要加载的视图路由分组，默认为default
     * </p>
     */
    private List<String> routeGroups = Arrays.asList("default");

}
