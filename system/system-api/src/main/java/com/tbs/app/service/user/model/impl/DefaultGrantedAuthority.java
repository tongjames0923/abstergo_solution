package com.tbs.app.service.user.model.impl;

import com.tbs.app.vo.SystemRoleGroupVO;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * 默认权限
 * <p>
 * 两种构造方式：携带角色组时保留完整权限树（登录路径使用）；仅持有编码时只保留扁平字符串，
 * 后者适用于认证阶段按权限编码装配的请求级快照，避免把整棵权限树带进每次请求。
 * </p>
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

    public DefaultGrantedAuthority(String authority) {
        this.authority = authority;
    }

    public DefaultGrantedAuthority() {
    }

}
