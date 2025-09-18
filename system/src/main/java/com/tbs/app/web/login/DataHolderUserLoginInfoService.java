package com.tbs.app.web.login;

import com.tbs.common.IStartup;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 默认用户登录信息服务实现
 *
 * @author abstergo
 */
public class DataHolderUserLoginInfoService extends AbstractUserLoginInfoService implements IStartup {
    private IDataHolder dataHolder;

    @Resource
    IDataHolderExpireActuator expireActuator;

    public static final String USER_LOGIN_INFO_HOLDER = "user_login_info";

    @Override
    protected UserDetails readUserDetails(String token) {
        return dataHolder.<UserDetails>get(token).orElse(null);
    }

    @Override
    protected void writeUserDetails(String token, UserDetails userDetails) {
        dataHolder.set(token, userDetails, true);
        expireActuator.expire(dataHolder, token, new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000));
    }

    @Override
    protected void removeUserDetails(String token) {
        dataHolder.remove(token);
    }

    @Override
    public void startUp() throws RuntimeException {
        this.dataHolder = SpringUtil.getBean(IDataHolderManager.class).getDataHolder(USER_LOGIN_INFO_HOLDER)
            .orElseThrow(() -> new RuntimeException("未找到用户登录信息数据源"));
    }
}
