package com.tbs.app.service.user.model.impl;

import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
public class DefaultSystemUserDetails implements ISystemUserDetails {
    private List<SystemCompanyEntity> companies;
    private SystemCompanyEntity currentCompany;
    private String username;
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private List<? extends GrantedAuthority> authorities;

    public DefaultSystemUserDetails(List<SystemCompanyEntity> companies, SystemCompanyEntity currentCompany,
        String username, String password, boolean accountNonExpired, boolean accountNonLocked,
        boolean credentialsNonExpired, boolean enabled, List<? extends GrantedAuthority> authorities) {
        this.companies = companies;
        this.currentCompany = currentCompany;
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
    public List<SystemCompanyEntity> getCompanies() {
        return companies;
    }

    @Override
    public SystemCompanyEntity getCurrentCompany() {
        return currentCompany;
    }

    @Override
    public void setCurrentCompany(SystemCompanyEntity company) {
        this.currentCompany = company;
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
        private List<SystemCompanyEntity> companies;
        private SystemCompanyEntity currentCompany;
        private String username;
        private String password;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;
        private List<? extends GrantedAuthority> authorities;

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

        public Builder authorities(List<? extends GrantedAuthority> authorities) {
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

        public Builder companies(List<SystemCompanyEntity> companies) {
            this.companies = companies;
            return this;
        }

        public Builder currentCompany(SystemCompanyEntity currentCompany) {
            this.currentCompany = currentCompany;
            return this;
        }

        public DefaultSystemUserDetails build() {
            return new DefaultSystemUserDetails(companies, currentCompany, username, password, accountNonExpired,
                accountNonLocked, credentialsNonExpired, enabled, authorities);
        }
    }

    public static final Builder builder() {
        return new Builder();
    }

}
