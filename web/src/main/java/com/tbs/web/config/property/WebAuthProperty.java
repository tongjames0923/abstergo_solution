package com.tbs.web.config.property;

import com.tbs.web.enums.TokenSource;
import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.security.authentication.IAuthManager;
import com.tbs.web.security.authentication.center.IAuthenticationCenter;
import com.tbs.web.security.authentication.center.impl.SimpleAuthenticationCenter;
import com.tbs.web.security.authentication.impl.SpringSecurityContextHolderAuthManager;
import com.tbs.web.security.impl.DefaultLoginTokenPicker;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.issuance.impl.NoImplTokenIssuanceCenter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证相关配置属性
 * <p>
 * 绑定前缀 {@code tbs.web.auth}，集中管控令牌来源/字段/有效期、各认证组件的实现类，
 * 以及令牌数据源名称。实现类字段均带默认内置实现，可在配置中心覆盖。
 * </p>
 *
 * @author abstergo
 * @see WebProperty Web 通用配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "tbs.web.auth")
public class WebAuthProperty {

    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * token来源
     */
    private TokenSource tokenSource = TokenSource.HEADER;

    /**
     * token字段名称
     */
    private String tokenFiledName = "Authorization";

    /**
     * token有效期,单位：秒
     */
    private int tokenExpireTime = 3600;

    /**
     * 刷新token有效期,单位：小时
     */
    private int refreshTokenExpireTime = 7 * 24;

    /**
     * 应用token有效期,单位：小时
     */
    private int applicationTokenExpireTime = 3 * 24;

    /**
     * 第三方token有效期,单位：小时
     */
    private int thirdPartyTokenExpireTime = 3;


    /**
     * token获取器实现类
     */
    private Class<? extends ITokenPicker> tokenPickerImpl = DefaultLoginTokenPicker.class;

    /**
     * 认证中心实现类
     */
    private Class<? extends IAuthenticationCenter> authenticationCenterImpl = SimpleAuthenticationCenter.class;

    /**
     * token颁发中心实现类
     */
    private Class<? extends ITokenIssuanceCenter> tokenIssuanceCenterImpl = NoImplTokenIssuanceCenter.class;

    /**
     * token数据源名称
     */
    private String tokenDataSourceName = "TOKEN";

    /**
     * 认证实现类
     */
    private Class<? extends IAuthManager> authManagerImpl = SpringSecurityContextHolderAuthManager.class;

}
