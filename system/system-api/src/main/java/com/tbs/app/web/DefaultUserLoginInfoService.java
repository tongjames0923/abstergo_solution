package com.tbs.app.web;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.DelegateUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.mapper.LoginInfoMapper;
import com.tbs.shared.pojo.entity.SystemLoginInfoEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.ef.IQueryQueryBuilder;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import com.tbs.web.security.user.LoginSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;

    IQueryQueryBuilder<SystemLoginInfoEntity> getAvailableTokensQuery() {
        Date now = new Date();
        Date availableTime = DateUtil.offsetDay(now, -28);
        return QueryUtil.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
            .greaterThanOrEqual(SystemLoginInfoEntity::getCreateTime, availableTime).pour();
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
            loginInfoEntity.setSessionId(token.getSessionId());
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

    private List<String> getUserAvaliableSessionIdsByUserName(String username) {
        SystemUserEntity user = CollUtil.first(systemUserService.queryByUserName(username));
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在。", username));
        var query = getAvailableTokensQuery();
        query.where().equal(SystemLoginInfoEntity::getUserId, user.getId());
        query.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        query.groupBy(SystemLoginInfoEntity::getSessionId);
        return query.fetch().stream().map(SystemLoginInfoEntity::getSessionId).collect(Collectors.toSet()).stream()
            .collect(Collectors.toList());
    }

    @Override
    public UserDetails getUserDetails(String token) {
        return super.getUserDetails(token);
    }

    @Override
    public List<String> getUserAvaliableSessionId(String username) {
        return getUserAvaliableSessionIdsByUserName(username);
    }

    @Override
    public List<LoginSessionVO> listUserSessions(String username) {
        SystemUserEntity user = CollUtil.first(systemUserService.queryByUserName(username));
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在。", username));
        var query = getAvailableTokensQuery();
        query.where().equal(SystemLoginInfoEntity::getUserId, user.getId());
        query.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        var loginInfo = query.fetch();
        if (CollUtil.isEmpty(loginInfo)) {
            return Collections.emptyList();
        }

        // 按sessionId分组
        return loginInfo.stream().collect(Collectors.groupingBy(SystemLoginInfoEntity::getSessionId)).entrySet()
            .stream().map(entry -> {
                String sessionId = entry.getKey();
                List<SystemLoginInfoEntity> sessionTokens = entry.getValue();

                LoginSessionVO vo = new LoginSessionVO();
                vo.setSessionId(sessionId);
                // 获取最早创建时间作为会话创建时间
                Date createTime =
                    sessionTokens.stream().filter(Objects::nonNull).map(SystemLoginInfoEntity::getCreateTime)
                        .filter(Objects::nonNull).min(Date::compareTo).orElse(null);
                vo.setCreateTime(createTime);

                // 获取该会话的所有令牌
                List<String> tokensStr =
                    sessionTokens.stream().filter(Objects::nonNull).map(SystemLoginInfoEntity::getToken)
                        .filter(StrUtil::isNotEmpty).collect(Collectors.toList());

                ITokenModel[] tokenModels = new ITokenModel[tokensStr.size()];
                var invoker =
                    DelegateUtil.getInstance().delegateWithTarget(tokenModels).build(AsyncDelegateInvoker.class);
                for (int i = 0; i < tokensStr.size(); i++) {
                    var finalI = i;
                    invoker.takeIn((obj, token) -> {
                        obj[finalI] = token;
                    }, () -> {
                        return tokenIssuanceCenter.getTokenByToken(tokensStr.get(finalI));
                    });
                }
                invoker.executeAll().forEach(iDelegateFlag -> {
                    iDelegateFlag.waitUntilFinished();
                });

                var tokens = Arrays.stream(tokenModels).filter(Objects::nonNull).collect(Collectors.toList());
                vo.setAccessTokens(tokens);

                // 设置额外信息
                vo.setTokenCount(tokens.size());

                // 计算有效的刷新令牌数量
                long activeRefreshTokens = tokenIssuanceCenter.getTokensBySessionId(sessionId).stream()
                    .filter(token -> token.getTokenType() == TokenTypeEnum.REFRESH_TOKEN)
                    .filter(token -> token.getStatus() == TokenStatusEnum.ACTIVE).count();
                vo.setActiveRefreshTokenCount((int)activeRefreshTokens);

                // 判断是否在线（至少有一个有效的刷新令牌）
                vo.setOnline(activeRefreshTokens > 0);

                // 获取最后活跃时间（使用最新的令牌创建时间）
                Date lastActiveTime =
                    tokens.stream().filter(Objects::nonNull).map(ITokenModel::getCreateTime).filter(Objects::nonNull)
                        .max(Date::compareTo).orElse(null);
                vo.setLastActiveTime(lastActiveTime);

                return vo;
            }).collect(Collectors.toList());

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
