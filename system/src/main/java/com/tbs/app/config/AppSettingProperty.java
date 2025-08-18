package com.tbs.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.impl.NoEncryptPassword;
import com.tbs.app.service.business.impl.SimpleSystemUserServiceImpl;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.impl.LocalDataExpireActuator;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.impl.SimpleDelegateInvoker;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;

import lombok.Data;

/**
 * @author abstergo
 */
@ConfigurationProperties(prefix = "tbs.system")
@Data
@Component
public class AppSettingProperty {

    /**
     * 登录用户名密码校验器
     */
    private Class<? extends IUserNamePasswordValidator> systemUserNamePasswordValidatorImpl = SimpleUserValidator.class;

    /**
     * 系统用户服务实现类
     */
    private Class<? extends ISystemUserService> systemUserServiceImpl = SimpleSystemUserServiceImpl.class;
    /**
     * 登录token过期实现类
     */
    private Class<? extends IDataHolderExpireActuator> expireActuatorClass = LocalDataExpireActuator.class;

    /**
     * 主代理实现类
     */
    private Class<? extends IDelegateInvoker> mainDelegateInvokerImpl = SimpleDelegateInvoker.class;

    /**
     * 密码加密实现类
     */
    private Class<? extends IPasswordEncrypt> passwordEncryptImpl = NoEncryptPassword.class;

    /**
     * 是否启用内置的系统实现
     */
    private boolean enable = true;

    /**
     * 是否启用操作日志
     */
    private boolean enableOperateLog = false;
}
