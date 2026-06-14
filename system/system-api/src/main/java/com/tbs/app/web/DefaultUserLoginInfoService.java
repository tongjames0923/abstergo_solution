package com.tbs.app.web;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.serial.DefaultSystemUserDetailsDto;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.cache.serialize.utils.ValueTransformUtils;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.DelegateUtil;
import com.tbs.shared.enums.LoginInfoFlagEnum;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.*;
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

    SystemLoginInfoEntity setUserDetails(SystemLoginInfoEntity loginInfoEntity, UserDetails userDetails) {
        AssertUtil.notNull(userDetails, () -> new FormattedRuntimeException("用户详情不能为空"));
        AssertUtil.notNull(loginInfoEntity, () -> new FormattedRuntimeException("用户登陆信息不能为空"));
        loginInfoEntity.setUserDetails(
            JSON.toJSONBytes(ValueTransformUtils.valueTransform(userDetails, ValueTransformUtils.TRANSFORM_WRITE)));
        return loginInfoEntity;
    }

    @Override
    protected UserDetails readUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token);
        return loginInfoEntity == null ? null : (UserDetails)ValueTransformUtils.valueTransform(
            JSON.parseObject(loginInfoEntity.getUserDetails(), DefaultSystemUserDetailsDto.class),
            ValueTransformUtils.TRANSFORM_READ);
    }

    @Override
    protected void writeUserDetails(ITokenModel token, UserDetails userDetails) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token.getToken());
        if (loginInfoEntity != null) {
            loginInfoEntity = setUserDetails(loginInfoEntity, userDetails);
            loginInfoEntity.setTokenExpireTime(token.getExpireTime());
            loginInfoEntity.setUpdateTime(new Date());
            mapper.updateByPrimaryKey(loginInfoEntity);
        } else {
            loginInfoEntity = new SystemLoginInfoEntity();
            loginInfoEntity = setUserDetails(loginInfoEntity, userDetails);
            loginInfoEntity.setToken(token.getToken());
            loginInfoEntity.setTokenExpireTime(token.getExpireTime());
            loginInfoEntity.setUserId(Optional.ofNullable(
                    CollUtil.of(systemUserService.queryByUserName(userDetails.getUsername())).findFirstOrNull())
                .map(SystemUserEntity::getId).orElseThrow(() -> {
                    return new FormattedRuntimeException("用户{}不存在无法生成用户登陆信息", userDetails.getUsername());
                }));
            loginInfoEntity.setSessionId(token.getSessionId());
            loginInfoEntity.setFlag(LoginInfoFlagEnum.ACTIVE.getCode());
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
        loginInfoEntity.setFlag(LoginInfoFlagEnum.CANCELLED.getCode());
        mapper.updateByPrimaryKey(loginInfoEntity);
    }

    private List<String> getUserAvaliableSessionIdsByUserName(String username) {
        SystemUserEntity user = CollUtil.of(systemUserService.queryByUserName(username)).findFirstOrNull();
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在。", username));
        var query = getAvailableTokensQuery();
        query.where().equal(SystemLoginInfoEntity::getUserId, user.getId())
            .isNotNull(SystemLoginInfoEntity::getSessionId);
        query.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        // 移除 GROUP BY，改为在 Java 中去重，避免 MySQL ONLY_FULL_GROUP_BY 错误
        return query.fetch().stream().map(SystemLoginInfoEntity::getSessionId).filter(StrUtil::isNotEmpty).distinct()
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "user_login_info_cache", key = "#token")
    @ExpireOn(cacheNames = "user_login_info_cache", value = 5, timeUnit = TimeUnit.MINUTES)
    public UserDetails getUserDetails(String token) {
        return super.getUserDetails(token);
    }

    @Override
    public List<String> getUserAvaliableSessionId(String username) {
        return getUserAvaliableSessionIdsByUserName(username);
    }

    @Override
    public List<LoginSessionVO> listUserSessions(String username) {
        SystemUserEntity user = CollUtil.of(systemUserService.queryByUserName(username)).findFirstOrNull();
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在。", username));
        var query = getAvailableTokensQuery();
        query.where().equal(SystemLoginInfoEntity::getUserId, user.getId())
            .isNotNull(SystemLoginInfoEntity::getSessionId);
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
            }).sorted((vo1, vo2) -> {
                //优先按创建时间排序，若创建时间在同一天，则按最后活动时间排序
                Date createTime1 = vo1.getCreateTime();
                Date createTime2 = vo2.getCreateTime();

                // 如果创建时间都存在
                if (createTime1 != null && createTime2 != null) {
                    // 比较创建日期（年月日）
                    Date date1 = DateUtil.beginOfDay(createTime1);
                    Date date2 = DateUtil.beginOfDay(createTime2);

                    int dateCompare = date2.compareTo(date1); // 按创建日期降序排序
                    if (dateCompare != 0) {
                        return dateCompare;
                    }

                    // 如果创建时间在同一天，按最后活动时间排序
                    Date lastActiveTime1 = vo1.getLastActiveTime();
                    Date lastActiveTime2 = vo2.getLastActiveTime();

                    if (lastActiveTime1 != null && lastActiveTime2 != null) {
                        return lastActiveTime2.compareTo(lastActiveTime1); // 按最后活动时间降序排序
                    } else if (lastActiveTime1 != null) {
                        return -1;
                    } else if (lastActiveTime2 != null) {
                        return 1;
                    }
                } else if (createTime1 != null) {
                    return -1;
                } else if (createTime2 != null) {
                    return 1;
                }

                return 0;
            }).collect(Collectors.toList());

    }

    @CacheEvict(cacheNames = "user_login_info_cache", key = "#token",condition =
    "#token!=null and #token.length()>0")
    @Override
    public void removeUserLoginInfo(String token) {
        super.removeUserLoginInfo(token);
    }

    @CacheEvict(cacheNames = "user_login_info_cache", key = "#token.token",condition =
    "#token!=null and #token.token!=null and #token.token.length()>0")
    @Override
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        super.replaceUserDetails(userDetails, token);
    }

}
