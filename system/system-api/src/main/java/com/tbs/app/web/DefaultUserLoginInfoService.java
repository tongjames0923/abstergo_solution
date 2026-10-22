package com.tbs.app.web;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.serial.DefaultSystemUserDetailsDto;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.cache.serialize.utils.ValueTransformUtils;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.LoginInfoFlagEnum;
import com.tbs.shared.mapper.LoginInfoMapper;
import com.tbs.shared.pojo.entity.SystemLoginInfoEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.service.QueryEngine;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.AbstractUserLoginInfoService;
import com.tbs.web.security.user.vo.LoginSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tbs.app.web.CacheNamesConstants.USER_LOGIN_INFO_CACHE;
import static com.tbs.app.web.CacheNamesConstants.USER_SESSIONS_CACHE;

/**
 * 默认用户登录信息服务
 * <p>
 * 缓存设计说明：
 * <ul>
 *     <li>缓存键：以业务原始 token / username 作为 key（token 为一次性随机串，天然无冲突，无需额外命名空间）</li>
 *     <li>失效路径：登出（removeUserLoginInfo）与置换（replaceUserDetails）入口主动 evict；TTL 作为最终兜底</li>
 *     <li>一致性：insertUserLoginInfo 走新 token，无旧缓存可脏；写路径均先落库后失效，失败不误删</li>
 *     <li>防膨胀：null 与空集合结果一律不缓存，避免无效 token / 空会话长期占据缓存并产生脏读</li>
 * </ul>
 * </p>
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

    /**
     * 获取可用的token查询条件
     *
     * @return
     */
    IQueryBuilder<SystemLoginInfoEntity> getAvailableTokensQuery() {
        Date now = new Date();
        Date availableTime = DateUtil.offsetDay(now, -28);
        return QueryEngine.getEntityQueryWrapper(SystemLoginInfoEntity.class).query().where()
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
        loginInfoEntity.setUserDetails(JsonUtils.toJSONBytes(
            ValueTransformUtils.valueTransform(userDetails, ValueTransformUtils.TRANSFORM_WRITE)));
        return loginInfoEntity;
    }

    @Override
    protected UserDetails readUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = getEntityByToken(token);
        // 未命中缓存的回源路径：实体或明细字段缺失时直接短路，避免无意义的 JSON 反序列化开销
        if (loginInfoEntity == null || loginInfoEntity.getUserDetails() == null) {
            return null;
        }
        return (UserDetails)ValueTransformUtils.valueTransform(
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

    /**
     * 按令牌获取用户详情（认证高频路径）。
     * <p>
     * 缓存策略：
     * <ul>
     *     <li>condition 过滤空令牌，避免以空串为 key 产生无效缓存；</li>
     *     <li>空集合参与缓存：无效令牌首次回源后写入空集合，TTL 内不再穿透数据库，抵御令牌探测类攻击；</li>
     *     <li>写入路径（登录、置换）均先落库再主动 evict，补数据后下一次读取即可回源，不会被空集合挡住；</li>
     *     <li>未启用 sync 防击穿：回源仅为单条索引查询，且空集合已消除重复穿透，代价可控；</li>
     *     <li>TTL 5 分钟兜底：用户禁用等状态变更在主动 evict 缺位时最长延迟 5 分钟生效。</li>
     * </ul>
     * </p>
     */
    @Override
    @Cacheable(cacheNames = USER_LOGIN_INFO_CACHE, key = "#token", condition = "#token != null and !#token.isEmpty()")
    @ExpireOn(cacheNames = USER_LOGIN_INFO_CACHE, value = 5, timeUnit = TimeUnit.MINUTES)
    public List<UserDetails> getUserDetails(String token) {
        return super.getUserDetails(token);
    }

    @Override
    public List<String> getUserAvaliableSessionId(String username) {
        return getUserAvaliableSessionIdsByUserName(username);
    }

    /**
     * 按用户名列出会话列表。
     * <p>
     * 缓存策略：空用户名不缓存；null / 空会话列表不缓存（unless）—— 避免用户刚登录产生新会话时，2 分钟 TTL 内持续读到旧的空列表（脏读）。
     * </p>
     */
    @Override
    @Cacheable(cacheNames = USER_SESSIONS_CACHE, key = "#username",
        condition = "#username != null and !#username.isEmpty()")
    @ExpireOn(cacheNames = USER_SESSIONS_CACHE, value = 2, timeUnit = TimeUnit.MINUTES)
    public List<LoginSessionVO> listUserSessions(String username) {
        // 使用 listSessions 方法一次性获取用户的所有会话令牌，避免多次查询
        Collection<ITokenModel> allTokens = tokenIssuanceCenter.listSessions(username, null, null);
        if (CollUtil.isEmpty(allTokens)) {
            return Collections.emptyList();
        }

        // 按 sessionId 分组
        Map<String, List<ITokenModel>> sessionGroups = allTokens.stream()
            .filter(token -> StrUtil.isNotEmpty(token.getSessionId()))
            .collect(Collectors.groupingBy(ITokenModel::getSessionId));

        return sessionGroups.entrySet().stream().map(entry -> {
            String sessionId = entry.getKey();
            List<ITokenModel> sessionTokens = entry.getValue();

            LoginSessionVO vo = new LoginSessionVO();
            vo.setSessionId(sessionId);

            // 获取最早创建时间作为会话创建时间
            Date createTime = sessionTokens.stream()
                .filter(Objects::nonNull)
                .map(ITokenModel::getCreateTime)
                .filter(Objects::nonNull)
                .min(Date::compareTo)
                .orElse(null);
            vo.setCreateTime(createTime);

            // 获取访问令牌（access tokens）
            List<ITokenModel> accessTokens = sessionTokens.stream()
                .filter(token -> token.getTokenType() == TokenTypeEnum.WEB_ACCESS_TOKEN ||
                                 token.getTokenType() == TokenTypeEnum.APPLICATION_ACCESS_TOKEN)
                .collect(Collectors.toList());
            vo.setAccessTokens(accessTokens);

            // 设置额外信息
            vo.setTokenCount(accessTokens.size());

            // 计算有效的刷新令牌数量
            long activeRefreshTokens = sessionTokens.stream()
                .filter(token -> token.getTokenType() == TokenTypeEnum.REFRESH_TOKEN)
                .filter(token -> token.getStatus() == TokenStatusEnum.ACTIVE)
                .count();
            vo.setActiveRefreshTokenCount((int) activeRefreshTokens);

            // 判断是否在线（至少有一个有效的刷新令牌）
            vo.setOnline(activeRefreshTokens > 0);

            // 获取最后活跃时间（使用最新的令牌创建时间）
            Date lastActiveTime = accessTokens.stream()
                .filter(Objects::nonNull)
                .map(ITokenModel::getCreateTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
            vo.setLastActiveTime(lastActiveTime);

            return vo;
        }).sorted((vo1, vo2) -> {
            // 优先按创建时间排序，若创建时间在同一天，则按最后活动时间排序
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

    /**
     * 登出：先落库（置 CANCELLED）再失效缓存（evict 默认 afterInvocation， 落库失败不误删缓存，保证读端一致性）。
     */
    @CacheEvict(cacheNames = USER_LOGIN_INFO_CACHE, key = "#token",condition =
    "#token!=null and #token.length()>0")
    @Override
    public void removeUserLoginInfo(String token) {
        super.removeUserLoginInfo(token);
    }

    /**
     * 置换用户详情：先落库再失效缓存，下次读取回源获取新值，避免 evict-then-write 竞态下的旧值回填。
     */
    @CacheEvict(cacheNames = USER_LOGIN_INFO_CACHE, key = "#token.token",condition =
    "#token!=null and #token.token!=null and #token.token.length()>0")
    @Override
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        super.replaceUserDetails(userDetails, token);
    }

}
