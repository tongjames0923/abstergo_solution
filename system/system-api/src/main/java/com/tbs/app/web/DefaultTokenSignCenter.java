package com.tbs.app.web;

import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.app.web.model.SimpleTokenModel;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.pojo.criteria.CommonTokenQueryCriteria;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.impl.OrderedModel;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.AbstractTokenIssuanceCenter;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.NetUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import static com.tbs.app.web.CacheNamesConstants.TOKEN_CACHE;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 默认令牌签发中心实现
 * <p>
 * 继承自AbstractTokenIssuanceCenter，提供了基于数据库的令牌管理实现。 该类负责令牌的创建、保存、查询等操作，并与SystemTokenEntity实体进行转换。
 * </p>
 *
 * @author abstergo
 */
public class DefaultTokenSignCenter extends AbstractTokenIssuanceCenter {

    /**
     * 系统令牌服务，用于与数据库交互进行令牌的增删改查操作
     */
    @Resource
    ISystemTokenService tokenService;

    /**
     * 将系统令牌实体转换为令牌模型
     *
     * @param entity 系统令牌实体
     * @return 对应的令牌模型
     */
    private ITokenModel toModel(SystemTokenEntity entity) {
        return new SimpleTokenModel(entity.getToken(), entity.getUserName(),
            EnumUtil.valueOf(TokenStatusEnum.class, entity.getFlag()),
            EnumUtil.valueOf(TokenTypeEnum.class, entity.getTokenType()), entity.getExpireTime(),
            entity.getCreateTime(), EnumUtil.valueOf(TokenCreateTypeEnum.class, entity.getCreateType()),
            entity.getSessionId(), entity.getMacAddress(), entity.getIpAddress());
    }

    /**
     * 将令牌模型转换为系统令牌实体
     *
     * @param model 令牌模型
     * @return 对应的系统令牌实体
     */
    private SystemTokenEntity toEntity(ITokenModel model) {
        var entity = new SystemTokenEntity();
        entity.setToken(model.getToken());
        entity.setUserName(model.getUserName());
        entity.setFlag(model.getStatus().getCode());
        entity.setTokenType(model.getTokenType().getCode());
        entity.setExpireTime(model.getExpireTime());
        entity.setCreateTime(model.getCreateTime());
        entity.setCreateType(model.getCreateType().getCode());
        if (model instanceof SimpleTokenModel) {
            var m = (SimpleTokenModel)model;
            entity.setMacAddress(m.getMacAddr());
            entity.setIpAddress(m.getIpAddr());
        }
        entity.setSessionId(model.getSessionId());
        return entity;
    }

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 通过系统令牌服务查询数据库获取令牌信息，并转换为令牌模型。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型，如果不存在则返回null
     */
    @Override
    protected ITokenModel getModel( String token) {
        var vals = tokenService.queryByToken(token);
        if (CollUtil.isEmpty(vals)) {
            return null;
        }
        var val = CollUtil.getFirst(vals);
        return toModel(val);
    }

    /**
     * 保存令牌模型
     * <p>
     * 将令牌模型转换为系统令牌实体并保存到数据库中。 如果令牌已存在则更新，否则新增。
     * </p>
     *
     * @param model 令牌模型
     */
    @Override
    protected void saveToken(ITokenModel model) {
        var ls = tokenService.queryByToken(model.getToken());
        if (CollUtil.isEmpty(ls)) {
            tokenService.save(toEntity(model));
        } else {
            SystemTokenEntity entity = toEntity(model);
            entity.setId(CollUtil.getFirst(ls).getId());
            tokenService.save(entity);
        }
    }

    /**
     * 保存令牌管理信息
     * <p>
     * 在此实现中为空操作，因为所有信息都保存在SystemTokenEntity中。
     * </p>
     *
     * @param userName  用户名
     * @param tokenType 令牌类型
     * @param token     令牌字符串
     */
    @Override
    protected void saveManageInfo(String userName, int tokenType, String token) {

    }

    /**
     * 获取用户名、token类型对应的token列表,仅28天内有效
     * <p>
     * 查询指定用户和令牌类型的所有令牌，限定创建时间在28天内，并按创建时间和过期时间降序排列。
     * </p>
     *
     * @param userName  用户名，不能为空
     * @param tokenType 令牌类型
     * @return 用户名和令牌类型对应的令牌模型集合
     */
    @Override
    protected Collection<ITokenModel> getUserNameAndTokenType( String userName, int tokenType) {
        return tokenService.query(
                new CommonTokenQueryCriteria().setUserNameEquals(userName).setTokenTypeEquals(tokenType)
                    .setCreateTimeAfter(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(28)))
                    .setCreateTimeOrdered(new OrderedModel(OrderEnum.DESC))
                    .setExpireTimeOrdered(new OrderedModel(OrderEnum.DESC)), null).stream().map(this::toModel)
            .collect(Collectors.toList());
    }

    @Override
    protected Collection<ITokenModel> getTokensWithSessionId(String sessionId) {
        // 直接查询并转换，避免N+1查询
        return tokenService.query(new CommonTokenQueryCriteria().setSessionIdEquals(sessionId)
            .setCreateTimeAfter(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(28)))
            .setCreateTimeOrdered(new OrderedModel(OrderEnum.DESC))
            .setExpireTimeOrdered(new OrderedModel(OrderEnum.DESC)), null).stream()
            .map(this::toModel)
            .collect(Collectors.toList());
    }

    @Override
    protected Collection<ITokenModel> listSessionsByUserName(String userName, Date beg, Date end) {
        return tokenService.query(
                new CommonTokenQueryCriteria().setUserNameEquals(userName).setCreateTimeAfter(beg).setCreateTimeBefore(end)
                    .setCreateTimeOrdered(new OrderedModel(OrderEnum.DESC))
                    .setExpireTimeOrdered(new OrderedModel(OrderEnum.DESC)), null).stream().map(this::toModel)
            .collect(Collectors.toList());
    }

    @Override
    protected ITokenModel createModel( TokenRequestForm form) {
        Date now = new Date();
        return new SimpleTokenModel(UuidUtil.getInstance().getUuidString(), form.getUserDetails().getUsername(),
            TokenStatusEnum.ACTIVE, EnumUtil.valueOf(TokenTypeEnum.class, form.getTokenType()),
            recommendExpireTime(now, form.getTokenType()), now,
            EnumUtil.valueOf(TokenCreateTypeEnum.class, form.getTokenCreateType()), form.getSessionId(),
            NetUtil.getMac(request), NetUtil.getIp(request));
    }

    /**
     * HTTP请求对象，用于获取客户端网络信息
     */
    @Resource
    HttpServletRequest request;

    @Cacheable(cacheNames = TOKEN_CACHE, key = "#token", condition = "#token!=null && #token.length()>0")
    @ExpireOn(cacheNames = TOKEN_CACHE, value = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public ITokenModel getTokenByToken( String token) {
        return super.getTokenByToken(token);
    }

    @CacheEvict(cacheNames = TOKEN_CACHE, key = "#token", condition = "#token!=null && #token.length()>0")
    @Override
    public void setTokenStatus( String token,  TokenStatusEnum status) {
        super.setTokenStatus(token, status);
    }
}
