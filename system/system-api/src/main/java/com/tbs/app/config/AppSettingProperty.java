package com.tbs.app.config;

import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.impl.SimpleDelegateInvoker;
import com.tbs.common.error.IErrorThrowEvent;
import com.tbs.common.error.impl.SimpleErrorThrowEvent;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.constraint.impl.SimpleUserValidator;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.api.persistence.impl.NoImplTreeStore;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.security.user.IUserDetailsTransformer;
import com.tbs.web.security.user.SimpleUserDetailsTransformer;
import com.tbs.web.security.user.impl.NoEncryptPassword;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * 主代理实现类
     */
    private Class<? extends IDelegateInvoker> mainDelegateInvokerImpl = SimpleDelegateInvoker.class;

    /**
     * 登录信息实现类
     */
    private Class<? extends IUserDetailsTransformer> userDetailsTransformerClass = SimpleUserDetailsTransformer.class;

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

    /**
     * 错误抛出事件实现类
     */
    Class<? extends IErrorThrowEvent> errorThrowEventImpl = SimpleErrorThrowEvent.class;


}
