package com.tbs.web.config.property;

import com.tbs.common.data.IDataHolder;
import com.tbs.web.enums.TokenSource;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.impl.DefaultTokenPicker;
import com.tbs.web.security.impl.NotImplUserAuthService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author abstergo
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
     * token获取器实现类
     */
    private Class<? extends ITokenPicker> tokenPickerImpl = DefaultTokenPicker.class;

    /**
     * 用户认证服务实现类
     */
    private Class<? extends IUserAuthService> userAuthServiceImpl = NotImplUserAuthService.class;

    /**
     * 忽略token验证的url
     */
    private List<String> tokenIgnoreUrls;

}
