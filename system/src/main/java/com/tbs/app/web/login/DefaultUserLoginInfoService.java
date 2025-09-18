package com.tbs.app.web.login;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.mapper.LoginInfoMapper;
import com.tbs.shared.pojo.entity.SystemLoginInfoEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 默认用户登录信息服务
 *
 * @author abstergo
 */
@Slf4j
public class DefaultUserLoginInfoService extends AbstractUserLoginInfoService {

    @Resource
    private AppSettingProperty appSettingProperty;
    @Resource
    private LoginInfoMapper mapper;

    @Resource
    private ISystemUserService systemUserService;

    @Override
    protected UserDetails readUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity =
            QueryUtil.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
                .equal(SystemLoginInfoEntity::getToken).greaterThanOrEqual(SystemLoginInfoEntity::getCreateTime,
                    new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 28)).pour().fetchOne();
        return loginInfoEntity == null ? null : loginInfoEntity.getUserDetailsObject();
    }

    @Override
    protected void writeUserDetails(String token, UserDetails userDetails) {
        SystemLoginInfoEntity loginInfoEntity =
            QueryUtil.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
                .equal(SystemLoginInfoEntity::getToken).greaterThanOrEqual(SystemLoginInfoEntity::getCreateTime,
                    new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 28)).pour().fetchOne();
        if (loginInfoEntity != null) {
            loginInfoEntity.setUserDetailsObject(userDetails,
                SpringUtil.getBean(appSettingProperty.getUserDetailsTransformerClass()));
            loginInfoEntity.setUpdateTime(new Date());
            mapper.updateByPrimaryKey(loginInfoEntity);
        } else {
            loginInfoEntity = new SystemLoginInfoEntity();
            loginInfoEntity.setUserDetailsObject(userDetails,
                SpringUtil.getBean(appSettingProperty.getUserDetailsTransformerClass()));
            loginInfoEntity.setToken(token);
            loginInfoEntity.setCreateTime(new Date());
            loginInfoEntity.setUserId(
                Optional.ofNullable(CollUtil.first(systemUserService.queryByUserName(userDetails.getUsername())))
                    .map(SystemUserEntity::getId).orElseThrow(() -> {
                        return new FormattedRuntimeException("用户{}不存在无法生成用户登陆信息", userDetails.getUsername());
                    }));
            loginInfoEntity.setFlag(CommonConstant.FLAG_ON);
            mapper.insert(loginInfoEntity);
        }
    }

    @Override
    protected void removeUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity =
            QueryUtil.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
                .equal(SystemLoginInfoEntity::getToken).greaterThanOrEqual(SystemLoginInfoEntity::getCreateTime,
                    new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 28)).pour().fetchOne();
        if (loginInfoEntity == null) {
            log.info("clean up expired login info");
            return;
        }
        loginInfoEntity.setFlag(CommonConstant.FLAG_OFF);
        mapper.updateByPrimaryKey(loginInfoEntity);
    }

    @Cacheable(cacheNames = "user_login_info_cache", key = "#token")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public UserDetails getUserDetails(String token) {
        return super.getUserDetails(token);
    }

    @CacheEvict(cacheNames = "user_login_info_cache", key = "#token")
    @Override
    public void removeUserLoginInfo(String token) {
        super.removeUserLoginInfo(token);
    }

    @CacheEvict(cacheNames = "user_login_info_cache", key = "#token")
    @Override
    public void replaceUserDetails(UserDetails userDetails, String token) {
        super.replaceUserDetails(userDetails, token);
    }
}
