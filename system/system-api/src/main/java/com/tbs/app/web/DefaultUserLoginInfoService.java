package com.tbs.app.web;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.IUserIdentityService;
import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.service.user.model.UserIdentity;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
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
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tbs.app.web.CacheNamesConstants.USER_SESSIONS_CACHE;

/**
 * 默认用户登录信息服务
 * <p>
 * 存储职责收敛为「会话」本身：一行只记录令牌、用户标识、会话标识、当前选中的部门与状态，
 * 不再保存用户详情、部门归属与权限。用户维度数据在认证时经 {@link IUserIdentityService}
 * 一次性装配为请求级快照，会话内不保留权限副本，因此权限与部门变更无需清理会话即可在下次认证时重新装配。
 * </p>
 * <p>
 * 「下次认证即生效」的前提是装配链路上的缓存已被及时失效：身份装配依赖按用户的权限组快照
 * （{@code ROLE_GROUP_USER_ID}）与部门列表（{@code USER_DEPARTMENTS}）。前者不靠各写入点手工清除，而是由
 * {@code PermissionUserSnapshotEvictLink} 向缓存框架声明「组权限缓存被清除时连带清除本缓存」；
 * 后者由部门写入点注解失效。给身份装配新增一层派生缓存时，应补一条关联失效声明而不是在写入点
 * 手写逐键 evict（03 分册 3.4.3 第 7 条），否则本类的实时性表述不成立。
 * </p>
 * <p>
 * 缓存设计说明：
 * <ul>
 *     <li>用户详情不做令牌级缓存：它已是请求级快照，再缓存只会让权限变更延后生效；</li>
 *     <li>缓存仅保留按用户名的会话列表（{@link #listUserSessions}），空列表不缓存，避免新会话产生后的脏读。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DefaultUserLoginInfoService extends AbstractUserLoginInfoService implements IStartup {

    @Resource
    private LoginInfoMapper mapper;

    @Resource
    private ISystemUserService systemUserService;

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;

    @Resource
    private IUserIdentityService userIdentityService;

    @Resource
    private CacheManager cacheManager;

    /**
     * 单例实例
     */
    @Getter
    private static DefaultUserLoginInfoService instance;

    private void postConstruct() {
        instance = SpringUtil.getBean(DefaultUserLoginInfoService.class);
    }





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
     * <p>
     * 存储层不建唯一约束，因此即使出现同令牌多行也只取最新一行，保证读路径与写入路径命中同一行。
     * 令牌唯一性由 {@link #existsLoginInfo(String)} 在写入前把关。
     * </p>
     *
     * @param token 登录令牌，不能为空
     * @return 会话行；不存在时返回 {@code null}
     */

    @Cacheable(cacheNames = USER_SESSIONS_CACHE, key = "#token")
    @ExpireOn(cacheNames = USER_SESSIONS_CACHE, value = 5, timeUnit = TimeUnit.MINUTES)
    public List<SystemLoginInfoEntity> getEntityByToken(String token) {
        var queryWrapper = getAvailableTokensQuery();
        queryWrapper.where().equal(SystemLoginInfoEntity::getToken, token);
        queryWrapper.getOrderSupport().ifPresent((order) -> {
            order.orderByDesc(SystemLoginInfoEntity::getCreateTime);
            order.orderByDesc(SystemLoginInfoEntity::getUpdateTime);
        });
        return CollUtil.singletonList(queryWrapper.fetchOne());
    }

    /**
     * 按令牌装配用户详情
     * <p>
     * 先定位会话行，再以会话行上的用户标识一次性拉取身份视图，最后叠加会话级的当前部门。
     * 每次调用都重新装配并返回新实例，不在令牌维度缓存，保证权限与部门变更在下次认证时即可见。
     * </p>
     *
     * @param token 登录令牌，不能为空
     * @return 用户详情；会话不存在或用户已不存在时返回 {@code null}
     */
    @Override
    protected UserDetails readUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = CollUtil.getFirst(getInstance().getEntityByToken(token));
        if (loginInfoEntity == null || loginInfoEntity.getUserId() == null) {
            return null;
        }
        UserIdentity identity = userIdentityService.loadUserIdentity(loginInfoEntity.getUserId());
        if (identity == null) {
            return null;
        }
        DefaultSystemUserDetails details = toUserDetails(identity);
        applyCurrentDepartment(loginInfoEntity, details);
        return details;
    }

    /**
     * 叠加会话级当前部门
     * <p>
     * 仅在会话选中的部门仍属于该用户时生效：用户被移出部门后会话仍可继续使用，
     * 只是当前部门置空、由前端引导重新选择，避免历史选择变成认证失败的硬阻断。
     * </p>
     *
     * @param loginInfoEntity 会话行，不能为空
     * @param details         待填充的用户详情，不能为空
     */
    private void applyCurrentDepartment(SystemLoginInfoEntity loginInfoEntity, DefaultSystemUserDetails details) {
        Long currentDepartmentId = loginInfoEntity.getCurrentDepartmentId();
        if (currentDepartmentId == null || CollUtil.isEmpty(details.getDepartments())) {
            return;
        }
        boolean joined = details.getDepartments().stream()
            .map(DepartmentInfoVO::getDepartmentId)
            .anyMatch(id -> Objects.equals(id, currentDepartmentId));
        if (!joined) {
            log.debug("令牌{}选中的部门{}已不属于用户，忽略当前部门设置", loginInfoEntity.getToken(), currentDepartmentId);
            return;
        }
        details.setCurrentDepartment(currentDepartmentId);
    }

    @Override
    protected boolean existsLoginInfo(String token) {
        return CollUtil.isNotEmpty(getInstance().getEntityByToken(token));
    }

    /**
     * 将身份视图转换为用户详情
     * <p>
     * 只搬运鉴权需要的字段：角色组名称展开为扁平 authority，权限编码直接透传；
     * 权限项名称、描述、数据范围等展示字段不进请求级快照。
     * </p>
     *
     * @param identity 身份视图，不能为空
     * @return 装配好的用户详情
     */
    private DefaultSystemUserDetails toUserDetails(UserIdentity identity) {
        List<DefaultGrantedAuthority> authorities = CollUtil.isEmpty(identity.getRoleGroupNames())
            ? new ArrayList<>()
            : identity.getRoleGroupNames().stream().map(DefaultGrantedAuthority::new).collect(Collectors.toList());
        return DefaultSystemUserDetails.builder()
            .userName(identity.getUsername())
            .accountNonExpired(identity.isAccountNonExpired())
            .accountNonLocked(identity.isAccountNonLocked())
            .credentialsNonExpired(identity.isCredentialsNonExpired())
            .enabled(identity.isEnabled())
            .departments(identity.getDepartments())
            .authorities(authorities)
            .apiPermissions(identity.getApiPermissions())
            .menuPermissions(identity.getMenuPermissions())
            .build();
    }

    /**
     * 写入会话行
     * <p>
     * 仅记录会话自身的数据，用户详情不进库。当前部门只在详情携带选中部门时更新，
     * 避免刷新等场景因详情未携带部门而把已有选择清空。
     * </p>
     *
     * @param token       登录令牌，不能为空
     * @param userDetails 用户详情，不能为空
     */
    @Override
    protected void writeUserDetails(ITokenModel token, UserDetails userDetails) {
        SystemLoginInfoEntity loginInfoEntity = CollUtil.getFirst(getInstance().getEntityByToken(token.getToken()));
        boolean isNew = loginInfoEntity == null;
        if (isNew) {
            loginInfoEntity = new SystemLoginInfoEntity();
            loginInfoEntity.setToken(token.getToken());
            loginInfoEntity.setSessionId(token.getSessionId());
            loginInfoEntity.setUserId(resolveUserId(userDetails.getUsername()));
            loginInfoEntity.setFlag(LoginInfoFlagEnum.ACTIVE.getCode());
            loginInfoEntity.setCreateTime(new Date());
        }
        loginInfoEntity.setTokenExpireTime(token.getExpireTime());
        loginInfoEntity.setUpdateTime(new Date());
        if (userDetails instanceof ISystemUserDetails) {
            DepartmentInfoVO current = ((ISystemUserDetails)userDetails).getCurrentDepartment();
            if (current != null) {
                loginInfoEntity.setCurrentDepartmentId(current.getDepartmentId());
            }
        }
        if (isNew) {
            mapper.insert(loginInfoEntity);
        } else {
            mapper.updateByPrimaryKey(loginInfoEntity);
        }
        // 写库后必须清除会话缓存：token 键对应 getEntityByToken；username 派生键对应
        // getUserAvaliableSessionId（sessionIds: 前缀）与 listUserSessions（sessions: 前缀）。
        // 签发/状态迁移的统一事件已由令牌颁发中心回调 ITokenCleanner 连带失效派生键，
        // 此处保留属主侧直清作为清理器未装配部署形态的兜底（重复清除幂等无害）；
        // 不清则 TTL 窗口内认证仍读旧行/空列表，且本地缓存返回同一实例会污染缓存内容
        evictSessionCache(token.getToken(),
            SESSION_IDS_KEY_PREFIX + userDetails.getUsername(), SESSION_LIST_KEY_PREFIX + userDetails.getUsername());
    }

    /**
     * 按用户名解析用户标识
     *
     * @param username 用户名，不能为空
     * @return 用户标识
     */
    private Long resolveUserId(String username) {
        SystemUserEntity user = CollUtil.of(systemUserService.queryByUserName(username)).findFirstOrNull();
        AssertUtil.notNull(user, () -> new FormattedRuntimeException("用户{}不存在无法生成用户登陆信息", username));
        return user.getId();
    }

    @Override
    protected void removeUserDetails(String token) {
        SystemLoginInfoEntity loginInfoEntity = CollUtil.getFirst(getInstance().getEntityByToken(token));
        if (loginInfoEntity == null) {
            log.info("clean up expired login info");
            return;
        }
        loginInfoEntity.setFlag(LoginInfoFlagEnum.CANCELLED.getCode());
        mapper.updateByPrimaryKey(loginInfoEntity);
        evictSessionCache(token);
    }

    /**
     * 清除指定令牌的会话缓存
     * <p>
     * {@link #writeUserDetails}/{@link #removeUserDetails} 由父类模板方法内部调用（同类调用不过代理）， 注解式 {@code @CacheEvict} 不会生效，故统一走
     * CacheManager 显式清除。
     * </p>
     *
     * @param keys 缓存键（令牌或用户名），逐个清除
     */
    private void evictSessionCache(Object... keys) {
        var cache = cacheManager.getCache(USER_SESSIONS_CACHE);
        if (cache == null) {
            return;
        }
        for (var key : keys) {
            if (key != null) {
                cache.evict(key);
            }
        }
    }

    /**
     * 失效会话快照类派生缓存：供令牌签发/失效链路的 ITokenCleanner 协调器回调，
     * 令牌模型自带 userName，无需反查库
     *
     * @param username 用户名，空则不处理
     */
    @Override
    public void evictUserSessionSnapshot(String username) {
        if (StrUtil.isEmpty(username)) {
            return;
        }
        evictSessionCache(SESSION_IDS_KEY_PREFIX + username, SESSION_LIST_KEY_PREFIX + username);
    }

    private List<String> getUserAvailableSessionIdsByUserName(String username) {
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
     * 切换当前会话的当前部门
     * <p>
     * 部门归属从聚合服务实时读取，校验通过后只更新会话行上的当前部门，不触碰任何用户维度数据。
     * 因此同一用户的其他会话不受影响，权限与部门的其他变更也不会被本次写入覆盖。
     * </p>
     *
     * @param token        令牌模型，不能为空
     * @param departmentId 目标部门标识，不能为空
     */
    @Override
    public void changeCurrentDepartment(ITokenModel token, Long departmentId) {
        AssertUtil.notNull(token, "登录令牌不能为空");
        AssertUtil.hasText(token.getToken(), "登录令牌内容不能为空");
        AssertUtil.notNull(departmentId, "目标部门标识不能为空");
        SystemLoginInfoEntity loginInfoEntity = CollUtil.getFirst(getInstance().getEntityByToken(token.getToken()));
        AssertUtil.notNull(loginInfoEntity, "令牌{}，登录用户信息不存在", token.getToken());
        AssertUtil.notNull(loginInfoEntity.getUserId(), "令牌{}，登录用户信息不完整", token.getToken());
        UserIdentity identity = userIdentityService.loadUserIdentity(loginInfoEntity.getUserId());
        AssertUtil.notNull(identity, "令牌{}，登录用户信息不存在", token.getToken());
        AssertUtil.isTrue(CollUtil.isEmpty(identity.getDepartments()), "用户{}未设置部门", identity.getUsername());
        boolean joined = identity.getDepartments().stream()
            .map(DepartmentInfoVO::getDepartmentId)
            .anyMatch(id -> Objects.equals(id, departmentId));
        AssertUtil.isTrue(!joined, "用户{}未加入部门{}", identity.getUsername(), departmentId);
        loginInfoEntity.setCurrentDepartmentId(departmentId);
        loginInfoEntity.setUpdateTime(new Date());
        mapper.updateByPrimaryKey(loginInfoEntity);
        evictSessionCache(token.getToken());
    }

    /**
     * 会话列表类缓存在 USER_SESSIONS_CACHE 中的键前缀：
     * getUserAvaliableSessionId（List<String>）与 listUserSessions（List<LoginSessionVO>）
     * 同为 username 键但结果类型不同，必须分键隔离，否则互相覆盖污染（注解里为 SpEL 字面量，修改须与本常量同步）
     */
    private static final String SESSION_IDS_KEY_PREFIX = "sessionIds:";
    private static final String SESSION_LIST_KEY_PREFIX = "sessions:";

    @Override
    @Cacheable(cacheNames = USER_SESSIONS_CACHE, key = "'sessionIds:' + #username")
    @ExpireOn(cacheNames = USER_SESSIONS_CACHE, value = 2, timeUnit = TimeUnit.MINUTES)
    public List<String> getUserAvaliableSessionId(String username) {
        return getUserAvailableSessionIdsByUserName(username);
    }

    /**
     * 按用户名列出会话列表。
     * <p>
     * 缓存策略：键加 sessions: 前缀与 getUserAvaliableSessionId 的 sessionIds: 键隔离；
     * 空用户名不缓存，null / 空会话列表不缓存（unless）—— 避免用户刚登录产生新会话时，
     * 2 分钟 TTL 内持续读到旧的空列表（脏读）。
     * </p>
     */
    @Override
    @Cacheable(cacheNames = USER_SESSIONS_CACHE, key = "'sessions:' + #username",
        condition = "#username != null and !#username.isEmpty()", unless = "#result == null || #result.isEmpty()")
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

            // 计算有效的刷新令牌数量。listSessions 返回存储原始状态（不做惰性过期修正），
            // 自然到期但从未被读取的令牌在库中仍为 ACTIVE，须叠加到期时刻判定，
            // 否则死令牌被计入活跃数、会话被误判为在线
            long now = System.currentTimeMillis();
            long activeRefreshTokens = sessionTokens.stream()
                .filter(token -> token.getTokenType() == TokenTypeEnum.REFRESH_TOKEN)
                .filter(token -> token.getStatus() == TokenStatusEnum.ACTIVE)
                .filter(token -> token.getExpireTime() == null || token.getExpireTime().getTime() >= now)
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
     * 启动执行方法
     * <p>
     * 应用启动后需要执行的具体任务逻辑
     * </p>
     *
     * @throws RuntimeException 执行过程中可能抛出的运行时异常
     */
    @Override
    public void startUp() throws RuntimeException {
        this.postConstruct();
    }
}
