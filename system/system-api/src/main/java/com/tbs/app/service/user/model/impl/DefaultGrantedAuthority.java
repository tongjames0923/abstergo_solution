package com.tbs.app.service.user.model.impl;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * 默认权限
 */
@Data
public class DefaultGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;
    private String authority;

    public DefaultGrantedAuthority(String authority) {
        this.authority = authority;
    }

    public DefaultGrantedAuthority() {
    }

}
