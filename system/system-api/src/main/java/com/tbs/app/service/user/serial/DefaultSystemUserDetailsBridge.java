package com.tbs.app.service.user.serial;

import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.common.data.IDataBridge;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DefaultSystemUserDetailsBridge
    implements IDataBridge<DefaultSystemUserDetailsDto, DefaultSystemUserDetails> {
    @Override
    public DefaultSystemUserDetailsDto bridge(@NotNull DefaultSystemUserDetails obj) {
        return new DefaultSystemUserDetailsDto(obj.getCompanies(), obj.getCurrentCompany(), obj.getUsername(),
            obj.getPassword(), obj.isAccountNonExpired(), obj.isAccountNonLocked(), obj.isCredentialsNonExpired(),
            obj.isEnabled(),
            obj.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }

    @Override
    public DefaultSystemUserDetails unbridge(@NotNull DefaultSystemUserDetailsDto obj) {
        return new DefaultSystemUserDetails(obj.getCompanies(), obj.getCurrentCompany(), obj.getUsername(),
            obj.getPassword(), obj.isAccountNonExpired(), obj.isAccountNonLocked(), obj.isCredentialsNonExpired(),
            obj.isEnabled(), obj.getAuthorities().stream().map((s) -> {
            return new SimpleGrantedAuthority(s);
        }).collect(Collectors.toList()));
    }
}
