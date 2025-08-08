package com.tbs.web.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author abstergo
 */
@Component
@ConfigurationProperties(prefix = "tbs.web")
@Data
public class WebProperty {
    public static final String DEFAULT_RESULT_BUILDER_PROVIDER = "default";

    /**
     * 是否启用跨域
     */
    private boolean enableCors = true;

    /**
     * 是否启用CSRF
     */
    private boolean enableCsrf = false;

    /**
     * 是否启用匿名访问
     */
    private boolean enableAnonymous = true;

    /**
     * 是否启用表单登录
     */
    private boolean enableFormLogin = false;

    /**
     * 是否启用HTTP Basic认证
     */
    private boolean enableHttpBasic = false;

    /**
     * 是否启用注销功能
     */
    private boolean enableLogout = false;

    /**
     * 需要认证才能访问的路径
     */
    private List<String> securedPaths = List.of("/api/**");

    /**
     * 允许所有用户访问的路径
     */
    private List<String> permitAllPaths = List.of("/", "/login", "/public/**");

}
