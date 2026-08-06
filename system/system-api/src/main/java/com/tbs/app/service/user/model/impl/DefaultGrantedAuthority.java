package com.tbs.app.service.user.model.impl;

import com.tbs.app.vo.SystemRoleGroupVO;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * 默认权限
 */
@Data
public class DefaultGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;
    private String authority;

    private SystemRoleGroupVO roleGroup;

    public DefaultGrantedAuthority(SystemRoleGroupVO roleGroup) {
        this.authority = roleGroup.getGroup().getRoleGroupName();
        this.roleGroup = roleGroup;
    }

    public DefaultGrantedAuthority() {
    }

}
