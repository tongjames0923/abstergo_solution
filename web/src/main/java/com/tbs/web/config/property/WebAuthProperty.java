package com.tbs.web.config.property;

import com.tbs.web.enums.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tbs.web.auth")
public class WebAuthProperty {

    public static final String DEFAULT_TOKEN_PICKER = "default";

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
     * token获取器实现类,若为default则使用默认实现，其他的使用Spring Bean名
     */
    private String tokenPickerImpl = DEFAULT_TOKEN_PICKER;

}
