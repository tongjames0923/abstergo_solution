package com.tbs.app.service.user.model.impl;

import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.common.error.AssertUtil;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认用户详情
 * <p>
 * 请求级用户视图：由认证阶段按会话行装配，装配后不再回源，本次请求内权限、部门等读取均为同一快照。 不参与持久化与缓存序列化，因此不需要形态转换桥接。
 * </p>
 */
@Data
public class DefaultSystemUserDetails implements ISystemUserDetails {
    private List<DepartmentInfoVO> departments;
    private DepartmentInfoVO currentDepartment;
    private String username;
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private List<DefaultGrantedAuthority> authorities;
    private Set<String> apiPermissions = new HashSet<>();
    private Set<String> menuPermissions = new HashSet<>();

    public DefaultSystemUserDetails(List<DepartmentInfoVO> departments, DepartmentInfoVO currentDepartment,
        String username, String password, boolean accountNonExpired, boolean accountNonLocked,
        boolean credentialsNonExpired, boolean enabled, List<DefaultGrantedAuthority> authorities) {
        this.departments = departments;
        this.currentDepartment = currentDepartment;
        this.username = username;
        this.password = password;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public DefaultSystemUserDetails() {
    }

    @Override
    public List<DepartmentInfoVO> getDepartments() {
        return departments;
    }

    @Override
    public DepartmentInfoVO getCurrentDepartment() {
        return currentDepartment;
    }

    @Override
    public void setCurrentDepartment(long department) {
        AssertUtil.notEmpty(departments, "用户未绑定任何部门");
        for (var d : departments) {
            if (d.getDepartmentId() == department) {
                this.currentDepartment = d;
                return;
            }
        }
        AssertUtil.throwError(() -> new IllegalArgumentException("部门编号不存在"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static class Builder {
        private List<DepartmentInfoVO> departments;
        private DepartmentInfoVO currentDepartment;
        private String username;
        private String password;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;
        private List<DefaultGrantedAuthority> authorities;
        private Set<String> apiPermissions;
        private Set<String> menuPermissions;

        public Builder() {
        }

        public Builder userName(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder authorities(List<DefaultGrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder departments(List<DepartmentInfoVO> departments) {
            this.departments = departments;
            return this;
        }

        public Builder currentDepartment(DepartmentInfoVO currentDepartment) {
            this.currentDepartment = currentDepartment;
            return this;
        }

        public Builder apiPermissions(Set<String> apiPermissions) {
            this.apiPermissions = apiPermissions;
            return this;
        }

        public Builder menuPermissions(Set<String> menuPermissions) {
            this.menuPermissions = menuPermissions;
            return this;
        }

        public DefaultSystemUserDetails build() {
            DefaultSystemUserDetails details =
                new DefaultSystemUserDetails(departments, currentDepartment, username, password, accountNonExpired,
                    accountNonLocked, credentialsNonExpired, enabled, authorities);
            if (apiPermissions != null) {
                details.setApiPermissions(apiPermissions);
            }
            if (menuPermissions != null) {
                details.setMenuPermissions(menuPermissions);
            }
            return details;
        }
    }

    public static final Builder builder() {
        return new Builder();
    }

}
