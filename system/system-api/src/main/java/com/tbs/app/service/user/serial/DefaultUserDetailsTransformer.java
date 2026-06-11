package com.tbs.app.service.user.serial;

import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.user.IUserDetailsTransformer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

/**
 *
 */
public class DefaultUserDetailsTransformer implements IUserDetailsTransformer<DefaultSystemUserDetailsDto> {
    @Override
    public DefaultSystemUserDetailsDto transform(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        if (userDetails instanceof ISystemUserDetails) {
            return new DefaultSystemUserDetailsDto(((ISystemUserDetails)userDetails).getDepartments(),
                ((ISystemUserDetails)userDetails).getCurrentDepartment(), userDetails.getUsername(),
                userDetails.getPassword(), userDetails.isAccountNonExpired(), userDetails.isAccountNonLocked(),
                userDetails.isCredentialsNonExpired(), userDetails.isEnabled(),
                CollUtil.isEmpty(userDetails.getAuthorities()) ? null
                    : userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
        } else {
            return new DefaultSystemUserDetailsDto(null, null, userDetails.getUsername(), userDetails.getPassword(),
                userDetails.isAccountNonExpired(), userDetails.isAccountNonLocked(),
                userDetails.isCredentialsNonExpired(), userDetails.isEnabled(),
                CollUtil.isEmpty(userDetails.getAuthorities()) ? null
                    : userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
        }
    }

    @Override
    public UserDetails reverseTransform(DefaultSystemUserDetailsDto userDetails) {
        if (userDetails == null) {
            return null;
        }
        return new DefaultSystemUserDetails(userDetails.getDepartments(), userDetails.getCurrentDepartment(),
            userDetails.getUsername(), userDetails.getPassword(), userDetails.isAccountNonExpired(),
            userDetails.isAccountNonLocked(), userDetails.isCredentialsNonExpired(), userDetails.isEnabled(),
            CollUtil.isEmpty(userDetails.getAuthorities()) ? null
                : userDetails.getAuthorities().stream().map(DefaultGrantedAuthority::new).collect(Collectors.toList()));
    }
}
