package com.tbs.app.config;

import com.tbs.web.security.ITokenHandler;
import com.tbs.web.security.model.TokenModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author tongj
 */
@Configuration
public class AppConfig {

    @Bean
    ITokenHandler tokenHandler() {
        return new ITokenHandler() {
            @Override
            public UserDetails handle(TokenModel tokenModel) {
                return org.springframework.security.core.userdetails.User.builder().username("JACK").password("JACK")
                    .roles(tokenModel.getToken()) // 将用户角色转换为字符串数组
                    .build();
            }
        };
    }
}
