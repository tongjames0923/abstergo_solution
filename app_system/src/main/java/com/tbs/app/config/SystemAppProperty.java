package com.tbs.app.config;

import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.constraint.impl.SimpleUserValidator;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.impl.NoEncryptPassword;
import com.tbs.app.service.business.impl.SimpleSystemUserServiceImpl;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.data.impl.MapDataHolder;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.impl.SimpleDelegateInvoker;
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
    private Class<? extends IDataHolder> tokenHolderImpl = MapDataHolder.class;

    /**
     *
     */
    private Class<? extends IDataHolderExpireActuator> tokenExpireImpl = LocalDataExpireActuator.class;

    /**
     * 主代理实现类
     */
    private Class<? extends IDelegateInvoker> mainDelegateInvokerImpl = SimpleDelegateInvoker.class;

    /**
     * 密码加密实现类
     */
    private Class<? extends IPasswordEncrypt> passwordEncryptImpl = NoEncryptPassword.class;
}
