package com.tbs.app.service.impl.identity;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.identity.IThirdPartyAppSecretBuilder;
import com.tbs.app.service.identity.IThirdPartyAppService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.ThirdPartyAppMapper;
import com.tbs.shared.pojo.criteria.CommonThirdPartyAppCriteria;
import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 第三方应用接出能力的默认实现。
 * <p>
 * 基于逻辑删除全功能服务模板 {@link AbstractLogicDeleteFullFeatureServiceTemplate} 实现，
 * 负责第三方应用的登记、代理用户（执行人账号）校验、登录授权类型校验等业务规则。
 * 第三方应用一经创建后不允许直接修改核心字段，因此更新操作统一抛出业务异常。
 * </p>
 *
 * @author abstergo
 */
public class DefaultThirdPartyServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<ThirdPartyAppEntity, Long, ThirdPartyAppMapper>
    implements IThirdPartyAppService {

    @Resource
    ObjectProvider<ILoginGrantProvider> loginGrantProviders;

    @Resource
    private ISystemUserService userService;

    @Resource
    private ThirdPartyAppMapper thirdPartyAppMapper;

    @Resource
    private IThirdPartyAppSecretBuilder secretBuilder;

    /**
     * 获取逻辑删除时写入的标志值（{@link CommonFlagEnum#DELETED}）。
     *
     * @return 逻辑删除标志位的整型编码
     */
    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    /**
     * 指定分页查询返回 {@link com.tbs.common.data.PageVO} 时使用的构建器类型。
     * <p>这里使用异步代理构建器 {@link AsyncDelegateInvoker}，以避免在分页查询时阻塞主流程。</p>
     *
     * @return PageVO 构建器对应的 {@link IDelegateInvoker} 实现类
     */
    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    /**
     * 新增第三方应用前的业务校验与落库处理。
     * <p>
     * 处理流程：
     * <ol>
     *     <li>校验代理用户 ID 不为空；</li>
     *     <li>校验代理用户存在、且未处于删除/未激活状态；</li>
     *     <li>校验系统中至少配置了一个登录授权提供方；</li>
     *     <li>校验 {@code appId} 在未被删除的记录中唯一；</li>
     *     <li>根据 {@code grantType} 匹配到具体的登录授权提供方；</li>
     *     <li>未传入 {@code appId} 时自动生成 UUID；</li>
     *     <li>写入 {@code ACTIVE} 状态的记录，并对 {@code appSecret} 进行哈希后回写。</li>
     * </ol>
     * </p>
     *
     * @param entity 待新增的第三方应用实体（含代理用户 ID、授权类型、应用标识与密钥）
     * @return 已落库并补全密钥哈希的第三方应用实体
     */
    @Override
    protected ThirdPartyAppEntity insertNew(ThirdPartyAppEntity entity) {
        AssertUtil.isTrueBusinessError(entity.getProxyId() == null).i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_ID_EMPTY).throwIt();
        var proxyUser = userService.queryById(entity.getProxyId());
        AssertUtil.isTrueBusinessError(proxyUser == null)
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_ID_NOT_FOUND, entity.getProxyId()).throwIt();
        AssertUtil.isTrueBusinessError(Objects.equals(proxyUser.getFlag(), CommonFlagEnum.DELETED.getCode()))
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_ID_DELETED, entity.getProxyId()).throwIt();
        AssertUtil.isTrueBusinessError(Objects.equals(proxyUser.getFlag(), CommonFlagEnum.INACTIVE.getCode()))
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_PROXY_ID_INACTIVE, entity.getProxyId()).throwIt();

        AssertUtil.isTrueBusinessError(CollUtil.isEmpty(loginGrantProviders))
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_LOGIN_GRANT_PROVIDER_EMPTY).throwIt();

        var e = getQueryBuilder().where().equal(ThirdPartyAppEntity::getAppId, entity.getAppId())
            .notEqual(ThirdPartyAppEntity::getFlag, CommonFlagEnum.DELETED.getCode()).pour().fetchOne();
        AssertUtil.isTrueBusinessError(e != null).i18n(SystemI18nConstant.THIRD_PARTY_APP_APP_ID_EXISTED, entity.getAppId()).throwIt();

        ILoginGrantProvider grantProvider = null;
        for (ILoginGrantProvider provider : loginGrantProviders) {
            if (Objects.equals(provider.getGrantType(), entity.getGrantType())) {
                grantProvider = provider;
                break;
            }
        }

        AssertUtil.isTrueBusinessError(grantProvider == null)
            .i18n(SystemI18nConstant.THIRD_PARTY_APP_LOGIN_GRANT_PROVIDER_NOT_FOUND, entity.getGrantType()).throwIt();

        entity.setGrantType(grantProvider.getGrantType());
        /*
         * 未显式传入应用标识时，使用 UUID 生成全局唯一的应用标识。
         */
        if (StrUtil.isEmpty(entity.getAppId())) {
            entity.setAppId(UuidUtil.getInstance().getUuidString());
        }
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        var appSecret = secretBuilder.build(entity);
        entity.setAppSecret(appSecret);
        return entity;
    }

    /**
     * 第三方应用不支持直接按主键更新，统一抛出业务异常。
     *
     * @param entity 待更新的实体
     * @return 永远不会返回，始终抛出异常
     */
    @Override
    protected List<ThirdPartyAppEntity> updateBy(ThirdPartyAppEntity entity) {
        AssertUtil.isTrue(true).i18n(SystemI18nConstant.THIRD_PARTY_APP_UPDATE_NOT_SUPPORTED).throwIt();
        return null;
    }

    /**
     * 第三方应用不支持字段级增量更新，统一抛出业务异常。
     *
     * @param newOne 新值实体
     * @param oldOne 旧值实体
     * @return 永远不会返回，始终抛出异常
     */
    @Override
    protected ThirdPartyAppEntity compareAndUpdateValue(ThirdPartyAppEntity newOne, ThirdPartyAppEntity oldOne) {
        AssertUtil.isTrue(true).i18n(SystemI18nConstant.THIRD_PARTY_APP_UPDATE_NOT_SUPPORTED).throwIt();
        return null;
    }

    /**
     * 获取当前服务使用的 {@link ThirdPartyAppMapper} 实例。
     *
     * @return 第三方应用 Mapper
     */
    @Override
    protected ThirdPartyAppMapper getMapper() {
        return thirdPartyAppMapper;
    }

    private static final ThirdPartyAppEntity EMPTY = new ThirdPartyAppEntity();

    /**
     * 返回一个用于获取实体类型的空实例。
     * <p>该实例仅用于推导 {@link ThirdPartyAppEntity} 的运行时类型，不参与任何持久化操作。</p>
     *
     * @return 复用的空实体实例
     */
    @Override
    protected ThirdPartyAppEntity newEntity() {
        return EMPTY;
    }

    private static final DefaultAuditService<ThirdPartyAppEntity, Long> auditService = new DefaultAuditService<>();

    /**
     * 获取审计服务支持
     *
     * @return
     */
    @Override
    protected Optional<IAuditService<ThirdPartyAppEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }

    /**
     * 根据应用ID查询应用信息。
     *
     * @param appId 应用ID
     * @return 应用信息
     */
    @Cacheable(value = "thirdPartyApp", key = "#appId")
    @ExpireOn(value = 12, timeUnit = TimeUnit.HOURS)
    @Override
    public ThirdPartyAppEntity getThirdPartyAppEntity(String appId) {
        return CollUtil.getFirst(query(CommonThirdPartyAppCriteria.builder().EqAppId(appId).build(), null));
    }
}
