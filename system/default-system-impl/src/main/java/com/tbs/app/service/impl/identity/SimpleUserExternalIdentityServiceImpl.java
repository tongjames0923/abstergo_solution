package com.tbs.app.service.impl.identity;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.app.service.identity.IExternalIdentityResolver;
import com.tbs.app.service.identity.IUserExternalIdentityService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.data.PageVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.mapper.SystemUserExternalIdentityMapper;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.shared.pojo.entity.SystemUserExternalIdentityEntity;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 外部身份绑定管理默认实现
 * <p>
 * 绑定规则单点收敛：查询走 {@code QueryEngine} 条件构造，写入走绑定 Mapper；
 * 自动关联依次执行策略链（按 {@link Ordered} 升序），首个命中即落库绑定。
 * 绑定语义：一个外部身份最多绑定一个系统用户（冲突报错）；
 * 一个系统用户在一个平台最多持有一个外部身份（重复绑定替换旧绑定）。
 * </p>
 *
 * @author abstergo
 * @see IExternalIdentityResolver 自动关联策略 SPI
 */
@Slf4j
public class SimpleUserExternalIdentityServiceImpl implements IUserExternalIdentityService {

    /**
     * 有效绑定标识（与实体 activeFlagValues 对齐）
     */
    private static final int FLAG_ACTIVE = 1;

    /**
     * 失效绑定标识（解绑即置为失效，查询自动过滤）
     */
    private static final int FLAG_INACTIVE = 0;

    /**
     * 分页每页数量上限
     */
    private static final int MAX_PAGE_SIZE = 100;

    private final List<IExternalIdentityResolver> resolvers;

    @Resource
    private SystemUserExternalIdentityMapper externalIdentityMapper;

    @Resource
    private ISystemUserService systemUserService;

    /**
     * 构建绑定管理服务
     *
     * @param resolvers 容器中全部自动关联策略，可为空（此时 autoBind 仅查既有绑定）
     */
    public SimpleUserExternalIdentityServiceImpl(List<IExternalIdentityResolver> resolvers) {
        this.resolvers = resolvers == null ? new ArrayList<>()
            : resolvers.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).collect(Collectors.toList());
    }

    @Override
    public Long getUserIdByIdentity(String identityType, String externalId) {
        SystemUserExternalIdentityEntity identity = fetchByIdentity(identityType, externalId);
        return identity == null ? null : identity.getUserId();
    }

    @Override
    public Long autoBind(ExternalUserProfile profile) {
        if (profile == null || StrUtil.isEmpty(profile.getIdentityType()) || StrUtil.isEmpty(profile.getExternalId())) {
            return null;
        }
        Long boundUserId = getUserIdByIdentity(profile.getIdentityType(), profile.getExternalId());
        if (boundUserId != null) {
            return boundUserId;
        }
        for (IExternalIdentityResolver resolver : resolvers) {
            if (!resolver.supports(profile.getIdentityType())) {
                continue;
            }
            Long resolvedUserId = resolver.resolve(profile);
            if (resolvedUserId != null) {
                bind(profile.getIdentityType(), profile.getExternalId(), resolvedUserId);
                return resolvedUserId;
            }
        }
        return null;
    }

    @Override
    public void bind(String identityType, String externalId, Long userId) {
        AssertUtil.isTrue(StrUtil.isEmpty(identityType) || StrUtil.isEmpty(externalId) || userId == null)
            .i18n(SystemI18nConstant.EXTERNAL_IDENTITY_PARAM_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        SystemUserExternalIdentityEntity existing = fetchByIdentity(identityType, externalId);
        if (existing != null) {
            if (Objects.equals(existing.getUserId(), userId)) {
                return;
            }
            AssertUtil.isTrue(true).i18n(SystemI18nConstant.EXTERNAL_IDENTITY_ALREADY_BOUND)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        Date now = new Date();
        SystemUserExternalIdentityEntity userBound = fetchByUserId(identityType, userId);
        if (userBound != null) {
            userBound.setExternalId(externalId);
            userBound.setUpdateTime(now);
            externalIdentityMapper.updateByPrimaryKeySelective(userBound);
            return;
        }
        SystemUserExternalIdentityEntity entity = new SystemUserExternalIdentityEntity();
        entity.setIdentityType(identityType);
        entity.setExternalId(externalId);
        entity.setUserId(userId);
        entity.setFlag(FLAG_ACTIVE);
        entity.setCreateTime(now);
        externalIdentityMapper.insert(entity);
    }

    @Override
    public void unbind(String identityType, Long userId) {
        AssertUtil.isTrue(StrUtil.isEmpty(identityType) || userId == null)
            .i18n(SystemI18nConstant.EXTERNAL_IDENTITY_PARAM_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        SystemUserExternalIdentityEntity existing = fetchByUserId(identityType, userId);
        if (existing == null) {
            return;
        }
        existing.setFlag(FLAG_INACTIVE);
        existing.setUpdateTime(new Date());
        externalIdentityMapper.updateByPrimaryKeySelective(existing);
    }

    @Override
    public List<SystemUserExternalIdentityEntity> queryByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return QueryEngine.getEntityQueryWrapper(SystemUserExternalIdentityEntity.class).query().where()
            .equal(SystemUserExternalIdentityEntity::getUserId, userId).pour().fetch();
    }

    @Override
    public PageVO<SystemUserExternalIdentityEntity> queryPage(String identityType, String userName, int pageNum,
        int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Long filterUserId = resolveUserIdByUserName(userName);
        long total = buildPageQuery(identityType, filterUserId).pour().count().longValue();
        var query = buildPageQuery(identityType, filterUserId).pour();
        query.getPageLimitSupport().ifPresentOrElse(pageLimit -> {
            pageLimit.pageOf((long) safePageNum, (long) safePageSize);
        }, () -> {
            log.warn("数据库未支持分页，将全局查询外部身份绑定");
        });
        List<SystemUserExternalIdentityEntity> data = query.fetch();
        return new PageVO<SystemUserExternalIdentityEntity>().setTotal((int) total).setData(data);
    }

    /**
     * 按登录用户名解析用户 ID（分页过滤用）
     *
     * @param userName 登录用户名，为空返回 {@code null}（不过滤）
     * @return 对应系统用户 ID；用户不存在或同名多用户时返回 {@code null}
     */
    private Long resolveUserIdByUserName(String userName) {
        if (StrUtil.isEmpty(userName)) {
            return null;
        }
        List<SystemUserEntity> users = systemUserService.queryByUserName(userName);
        SystemUserEntity user = CollUtil.getFirst(users);
        return user == null ? null : user.getId();
    }

    /**
     * 构造分页查询条件（每次调用生成独立构建器，计数与取数互不干扰）
     *
     * @param identityType 外部身份类型过滤，为空时不过滤
     * @param userId       系统用户 ID 过滤，为空时不过滤
     * @return 已附加条件的 where 构建器
     */
    private IWhereClause<SystemUserExternalIdentityEntity> buildPageQuery(String identityType, Long userId) {
        var where = QueryEngine.getEntityQueryWrapper(SystemUserExternalIdentityEntity.class).query().where();
        if (StrUtil.isNotEmpty(identityType)) {
            where.equal(SystemUserExternalIdentityEntity::getIdentityType, identityType);
        }
        if (userId != null) {
            where.equal(SystemUserExternalIdentityEntity::getUserId, userId);
        }
        return where;
    }

    /**
     * 按外部身份查询有效绑定
     *
     * @param identityType 外部身份类型
     * @param externalId   外部身份标识
     * @return 绑定实体；未绑定返回 {@code null}
     */
    private SystemUserExternalIdentityEntity fetchByIdentity(String identityType, String externalId) {
        if (StrUtil.isEmpty(identityType) || StrUtil.isEmpty(externalId)) {
            return null;
        }
        return QueryEngine.getEntityQueryWrapper(SystemUserExternalIdentityEntity.class).query().where()
            .equal(SystemUserExternalIdentityEntity::getIdentityType, identityType)
            .equal(SystemUserExternalIdentityEntity::getExternalId, externalId)
            .pour().fetchOne();
    }

    /**
     * 按用户与平台查询有效绑定
     *
     * @param identityType 外部身份类型
     * @param userId       系统用户 ID
     * @return 绑定实体；未绑定返回 {@code null}
     */
    private SystemUserExternalIdentityEntity fetchByUserId(String identityType, Long userId) {
        if (StrUtil.isEmpty(identityType) || userId == null) {
            return null;
        }
        return QueryEngine.getEntityQueryWrapper(SystemUserExternalIdentityEntity.class).query().where()
            .equal(SystemUserExternalIdentityEntity::getIdentityType, identityType)
            .equal(SystemUserExternalIdentityEntity::getUserId, userId)
            .pour().fetchOne();
    }
}
