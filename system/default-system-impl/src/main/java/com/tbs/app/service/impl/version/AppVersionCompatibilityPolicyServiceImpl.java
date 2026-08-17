package com.tbs.app.service.impl.version;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.version.IAppVersionCompatibilityPolicyService;
import com.tbs.app.service.version.IAppVersionService;
import com.tbs.app.vo.AppVersionUpdateCheckVO;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.AppVersionCompatibilityActionEnum;
import com.tbs.shared.enums.AppVersionFlagEnum;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.version.AppVersionComptMapper;
import com.tbs.shared.pojo.entity.version.AppVersionCompatibilityPolicyEntity;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;
import com.tbs.sql.service.QueryEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 应用版本兼容策略服务实现
 * <p>
 * 提供跨应用版本兼容策略的维护与评估能力：
 * 源应用发布新版本后可通过策略对目标应用提出最低版本要求，
 * 目标应用客户端通过升级检查获得更新要求（强制更新/推荐更新/仅提示）
 *
 * @author abstergo
 */
@Slf4j
public class AppVersionCompatibilityPolicyServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<AppVersionCompatibilityPolicyEntity, Long, AppVersionComptMapper>
    implements IAppVersionCompatibilityPolicyService {

    @Resource
    private AppVersionComptMapper appVersionComptMapper;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected AppVersionComptMapper getMapper() {
        return appVersionComptMapper;
    }

    @Override
    protected AppVersionCompatibilityPolicyEntity newEntity() {
        return new AppVersionCompatibilityPolicyEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected AppVersionCompatibilityPolicyEntity insertNew(AppVersionCompatibilityPolicyEntity entity) {
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getSourceAppKey())).i18n("policy.source.app.key.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getTargetAppKey())).i18n("policy.target.app.key.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getMinCompatVersion() == null).i18n("policy.min.compat.version.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getAction() == null).i18n("policy.action.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!EnumUtil.isValidCode(AppVersionCompatibilityActionEnum.class, entity.getAction()))
            .i18n("policy.action.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        validateAppRegistered(entity.getSourceAppKey(), "policy.source.app.not.exists");
        validateAppRegistered(entity.getTargetAppKey(), "policy.target.app.not.exists");
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<AppVersionCompatibilityPolicyEntity> updateBy(AppVersionCompatibilityPolicyEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected AppVersionCompatibilityPolicyEntity compareAndUpdateValue(AppVersionCompatibilityPolicyEntity newOne,
        AppVersionCompatibilityPolicyEntity oldOne) {
        if (StrUtil.isNotEmpty(newOne.getSourceAppKey())) {
            validateAppRegistered(newOne.getSourceAppKey(), "policy.source.app.not.exists");
            oldOne.setSourceAppKey(newOne.getSourceAppKey());
        }
        if (newOne.getSourceVersionCode() != null) {
            oldOne.setSourceVersionCode(newOne.getSourceVersionCode());
        }
        if (StrUtil.isNotEmpty(newOne.getSourcePlatform())) {
            oldOne.setSourcePlatform(newOne.getSourcePlatform());
        }
        if (StrUtil.isNotEmpty(newOne.getTargetAppKey())) {
            validateAppRegistered(newOne.getTargetAppKey(), "policy.target.app.not.exists");
            oldOne.setTargetAppKey(newOne.getTargetAppKey());
        }
        if (StrUtil.isNotEmpty(newOne.getTargetPlatform())) {
            oldOne.setTargetPlatform(newOne.getTargetPlatform());
        }
        if (newOne.getMinCompatVersion() != null) {
            oldOne.setMinCompatVersion(newOne.getMinCompatVersion());
        }
        if (newOne.getAction() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(AppVersionCompatibilityActionEnum.class, newOne.getAction()))
                .i18n("policy.action.invalid").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            oldOne.setAction(newOne.getAction());
        }
        return oldOne;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "app_version_compatibility", allEntries = true)
    })
    @Override
    public void enablePolicy(Long id) {
        AppVersionCompatibilityPolicyEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n("policy.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "app_version_compatibility", allEntries = true)
    })
    @Override
    public void disablePolicy(Long id) {
        AppVersionCompatibilityPolicyEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n("policy.not.exists")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(CommonFlagEnum.INACTIVE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    @Override
    public AppVersionUpdateCheckVO evaluateCompatibility(String appKey, String platform, Long currentVersionCode) {
        IAppVersionService versionService = SpringUtil.getBean(IAppVersionService.class);
        AppVersionEntity latest = versionService.queryLatestReleased(appKey, platform);

        boolean needUpdate = latest != null && currentVersionCode < latest.getVersionCode();
        Integer strongestAction = null;

        if (needUpdate) {
            List<AppVersionCompatibilityPolicyEntity> policies =
                QueryEngine.getEntityQueryWrapper(new AppVersionCompatibilityPolicyEntity()).query().where()
                    .equal(AppVersionCompatibilityPolicyEntity::getTargetAppKey, appKey)
                    .equal(AppVersionCompatibilityPolicyEntity::getFlag, CommonFlagEnum.ACTIVE.getCode())
                    .pour().fetch();

            for (AppVersionCompatibilityPolicyEntity policy : policies) {
                if (StrUtil.isNotEmpty(policy.getTargetPlatform())
                    && !policy.getTargetPlatform().equals(platform)) {
                    continue;
                }
                if (policy.getSourceVersionCode() != null) {
                    AppVersionEntity sourceLatest =
                        versionService.queryLatestReleased(policy.getSourceAppKey(), policy.getSourcePlatform());
                    if (sourceLatest == null || sourceLatest.getVersionCode() < policy.getSourceVersionCode()) {
                        continue;
                    }
                }
                if (currentVersionCode < policy.getMinCompatVersion()) {
                    if (strongestAction == null || policy.getAction() < strongestAction) {
                        strongestAction = policy.getAction();
                    }
                }
            }
        }

        AppVersionUpdateCheckVO vo = new AppVersionUpdateCheckVO();
        vo.setNeedUpdate(needUpdate);
        vo.setAction(strongestAction);
        if (latest != null) {
            vo.setLatestVersionCode(latest.getVersionCode());
            vo.setLatestVersionName(latest.getVersionName());
            vo.setReleaseNote(latest.getReleaseNote());
        }
        return vo;
    }

    /**
     * 校验应用标识已有登记版本
     * <p>
     * 兼容限制只能作用于已登记版本的应用，防止绕过前端下拉选择提交不存在的应用
     *
     * @param appKey  应用标识
     * @param i18nKey 校验失败时的国际化消息 key
     */
    private void validateAppRegistered(String appKey, String i18nKey) {
        IAppVersionService versionService = SpringUtil.getBean(IAppVersionService.class);
        AssertUtil.isTrue(!versionService.listAppKeys().contains(appKey)).i18n(i18nKey)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    private static final IAuditService<AppVersionCompatibilityPolicyEntity, Long, Long> auditService =
        new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<AppVersionCompatibilityPolicyEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
