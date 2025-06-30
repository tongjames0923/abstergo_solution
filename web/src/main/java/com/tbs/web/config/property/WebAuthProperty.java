package com.tbs.web.config.property;

import com.tbs.web.enums.TokenSource;
import com.tbs.web.security.ITokenHandler;
import com.tbs.web.security.ITokenPicker;
import com.tbs.web.security.impl.DefaultTokenPicker;
import com.tbs.web.security.impl.NoImplTokenHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * token处理器实现类,默认是无实现的，即不处理token，需要自定义后才能使用
     */
    private Class<? extends ITokenHandler> tokenHandlerImpl = NoImplTokenHandler.class;

}
