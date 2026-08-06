package com.tbs.app.service.user.serial;

import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.common.data.IDataBridge;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultSystemUserDetailsBridge
    implements IDataBridge<DefaultSystemUserDetailsDto, DefaultSystemUserDetails> {
    @Override
    public DefaultSystemUserDetailsDto bridge( DefaultSystemUserDetails obj) {
        return new DefaultSystemUserDetailsDto(obj.getDepartments(), obj.getCurrentDepartment(), obj.getUsername(),
            obj.getPassword(), obj.isAccountNonExpired(), obj.isAccountNonLocked(), obj.isCredentialsNonExpired(),
            obj.isEnabled(), (List<DefaultGrantedAuthority>)obj.getAuthorities());
    }

    @Override
    public DefaultSystemUserDetails unbridge( DefaultSystemUserDetailsDto obj) {
        return new DefaultSystemUserDetails(obj.getDepartments(), obj.getCurrentDepartment(), obj.getUsername(),
            obj.getPassword(), obj.isAccountNonExpired(), obj.isAccountNonLocked(), obj.isCredentialsNonExpired(),
            obj.isEnabled(), obj.getAuthorities());
    }
}
