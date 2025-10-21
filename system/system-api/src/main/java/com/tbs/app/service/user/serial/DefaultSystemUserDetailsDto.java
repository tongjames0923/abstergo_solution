package com.tbs.app.service.user.serial;

import com.tbs.cache.serialize.annotation.CacheTransform;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CacheTransform(bridgeType = DefaultSystemUserDetailsBridge.class)
public class DefaultSystemUserDetailsDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<SystemCompanyEntity> companies;
    private SystemCompanyEntity currentCompany;
    private String username;
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private List<? extends String> authorities;
}
