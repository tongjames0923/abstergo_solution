package com.tbs.app.config;

import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.constraint.impl.SimpleUserValidator;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.impl.SimpleSystemUserServiceImpl;
import com.tbs.common.holder.context.AbstractNamedContext;
import com.tbs.common.holder.context.impl.LocalStoredMapImplContext;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author abstergo
 */
@ConfigurationProperties(prefix = "tbs.system")
@Data
public class SystemAppProperty {
    /**
     * 登录用户名密码校验器
     */
    private Class<? extends IUserNamePasswordValidator> systemUserNamePasswordValidatorImpl = SimpleUserValidator.class;

    /**
     * 系统用户服务实现类
     */
    private Class<? extends ISystemUserService> systemUserServiceImpl = SimpleSystemUserServiceImpl.class;

    /**
     * token上下文实现类
     */
    private Class<? extends AbstractNamedContext> tokenContextImpl = LocalStoredMapImplContext.class;
}
