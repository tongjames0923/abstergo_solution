package com.tbs.app.web.login;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.mapper.LoginInfoMapper;
import com.tbs.shared.pojo.entity.SystemLoginInfoEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    /**
     * 获取可用的token查询条件 创建28天内的且Token过期时间未到的
     *
     * @return
     */
    IQueryQueryBuilder<SystemLoginInfoEntity> getAvailableTokensQuery() {
        Date now = new Date();
        Date availableTime = DateUtil.offsetDay(now, -28);
        return QueryUtil.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
            .greaterThanOrEqual(SystemLoginInfoEntity::getCreateTime, availableTime)
            .greaterThanOrEqual(SystemLoginInfoEntity::getTokenExpireTime, now).pour();
    }

    /**
     * 根据token获取用户登陆信息
     *
     * @param token
     * @return
     */
    SystemLoginInfoEntity getEntityByToken(String token) {
        var queryWrapper = getAvailableTokensQuery();
        queryWrapper.where().equal(SystemLoginInfoEntity::getToken, token);
        queryWrapper.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        return queryWrapper.fetchOne();
    }

    @Override
    protected UserDetails readUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token);
        return loginInfoEntity == null ? null : loginInfoEntity.getUserDetailsObject();
    }

    @Override
    protected void writeUserDetails(ITokenModel token, UserDetails userDetails) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token.getToken());
        if (loginInfoEntity != null) {
            loginInfoEntity.setUserDetailsObject(userDetails,
                SpringUtil.getBean(appSettingProperty.getUserDetailsTransformerClass()));
            loginInfoEntity.setTokenExpireTime(token.getExpireTime());
            loginInfoEntity.setUpdateTime(new Date());
            mapper.updateByPrimaryKey(loginInfoEntity);
        } else {
            loginInfoEntity = new SystemLoginInfoEntity();
            loginInfoEntity.setUserDetailsObject(userDetails,
                SpringUtil.getBean(appSettingProperty.getUserDetailsTransformerClass()));
            loginInfoEntity.setToken(token.getToken());
            loginInfoEntity.setTokenExpireTime(token.getExpireTime());
            loginInfoEntity.setUserId(
                Optional.ofNullable(CollUtil.first(systemUserService.queryByUserName(userDetails.getUsername())))
                    .map(SystemUserEntity::getId).orElseThrow(() -> {
                        return new FormattedRuntimeException("用户{}不存在无法生成用户登陆信息", userDetails.getUsername());
                    }));
            loginInfoEntity.setFlag(CommonConstant.FLAG_ON);
            loginInfoEntity.setCreateTime(new Date());
            mapper.insert(loginInfoEntity);
        }
    }

    @Override
    protected void removeUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token);
        if (loginInfoEntity == null) {
            log.info("clean up expired login info");
            return;
        }
        loginInfoEntity.setFlag(CommonConstant.FLAG_OFF);
        mapper.updateByPrimaryKey(loginInfoEntity);
    }

    @Override
    protected List<String> getUserAvaliableTokensByUserName(String username) {
        SystemUserEntity user = CollUtil.first(systemUserService.queryByUserName(username));
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在。", username));
        var query = getAvailableTokensQuery();
        query.where().equal(SystemLoginInfoEntity::getUserId, user.getId());
        query.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        return query.fetch().stream().map(SystemLoginInfoEntity::getToken).collect(Collectors.toList());
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
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        super.replaceUserDetails(userDetails, token);
    }
}
