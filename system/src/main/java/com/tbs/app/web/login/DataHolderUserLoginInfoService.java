package com.tbs.app.web.login;

import cn.hutool.core.date.DateUtil;
import com.tbs.common.IStartup;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderExpireActuator;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static final String getUserTokenCache(String userName) {
        return "user_token_" + userName;
    }

    @Override
    protected UserDetails readUserDetails(String token) {
        return dataHolder.<UserDetails>get(token).orElse(null);
    }

    @Override
    protected void writeUserDetails(ITokenModel token, UserDetails userDetails) {
        dataHolder.set(token.getToken(), userDetails, true);
        Set<String> tokens =
            dataHolder.<Set<String>>get(getUserTokenCache(userDetails.getUsername())).orElse(new HashSet<>(1));
        tokens.add(token.getToken());
        dataHolder.set(getUserTokenCache(userDetails.getUsername()), tokens, true);
        expireActuator.expire(dataHolder, getUserTokenCache(userDetails.getUsername()),
            DateUtil.offsetDay(new Date(), 1));
        expireActuator.expire(dataHolder, token, new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000));
    }

    @Override
    protected void removeUserDetails(String token) {
        UserDetails userDetails = readUserDetails(token);
        if (userDetails == null) {
            return;
        }
        Set<String> tokens =
            dataHolder.<Set<String>>get(getUserTokenCache(userDetails.getUsername())).orElse(new HashSet<>(1));
        tokens.remove(token);
        dataHolder.set(getUserTokenCache(userDetails.getUsername()), tokens, true);
        expireActuator.expire(dataHolder, getUserTokenCache(userDetails.getUsername()),
            DateUtil.offsetDay(new Date(), 1));
        dataHolder.remove(token);
    }

    @Override
    protected List<String> getUserAvaliableTokensByUserName(String username) {
        return dataHolder.<Set<String>>get(getUserTokenCache(username)).orElse(new HashSet<>(1)).stream()
            .collect(Collectors.toList());
    }

    @Override
    public void startUp() throws RuntimeException {
        this.dataHolder = SpringUtil.getBean(IDataHolderManager.class).getDataHolder(USER_LOGIN_INFO_HOLDER)
            .orElseThrow(() -> new RuntimeException("未找到用户登录信息数据源"));
    }
}
