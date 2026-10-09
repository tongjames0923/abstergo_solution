package com.tbs.app.service.impl.version;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.version.IAppVersionService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.AppVersionFlagEnum;
import com.tbs.shared.enums.AppVersionTypeEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.version.AppVersionMapper;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;
import com.tbs.sql.service.QueryEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.tbs.app.constant.SystemI18nConstant;

/**
 * 应用版本服务实现
 * <p>
 * 提供应用版本的基础 CRUD 与生命周期状态流转能力， 状态流转路径：DRAFT(0) -> PENDING(2) -> RELEASE(1) -> OFFLINE(-1) -> ARCHIVED(-3)， 其中 RELEASE
 * 可直接流转至 ROLLED_BACK(-2)。 逻辑删除以 ARCHIVED(-3) 作为终态。
 *
 * @author abstergo
 */
@Slf4j
public class AppVersionServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<AppVersionEntity, Long, AppVersionMapper>
    implements IAppVersionService {

    @Resource
    private AppVersionMapper appVersionMapper;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected AppVersionMapper getMapper() {
        return appVersionMapper;
    }

    @Override
    protected AppVersionEntity newEntity() {
        return new AppVersionEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return AppVersionFlagEnum.ARCHIVED.getCode();
    }

    @Override
    protected AppVersionEntity insertNew(AppVersionEntity entity) {
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getAppKey())).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_APP_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getVersionCode() == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_CODE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getVersionName())).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getVersionType() == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!EnumUtil.isValidCode(AppVersionTypeEnum.class, entity.getVersionType()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_INVALID).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getPlatform())).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_PLATFORM_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        validateDuplicateVersionCode(entity.getAppKey(), entity.getVersionCode(), entity.getPlatform(), null);

        entity.setFlag(AppVersionFlagEnum.DRAFT.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<AppVersionEntity> updateBy(AppVersionEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected AppVersionEntity compareAndUpdateValue(AppVersionEntity newOne, AppVersionEntity oldOne) {
        AssertUtil.isTrue(!AppVersionFlagEnum.DRAFT.getCode().equals(oldOne.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_ONLY_DRAFT_CAN_EDIT).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();

        if (StrUtil.isNotEmpty(newOne.getAppKey())) {
            oldOne.setAppKey(newOne.getAppKey());
        }
        if (newOne.getVersionCode() != null && !newOne.getVersionCode().equals(oldOne.getVersionCode())) {
            validateDuplicateVersionCode(oldOne.getAppKey(), newOne.getVersionCode(), oldOne.getPlatform(),
                oldOne.getId());
            oldOne.setVersionCode(newOne.getVersionCode());
        }
        if (StrUtil.isNotEmpty(newOne.getVersionName())) {
            oldOne.setVersionName(newOne.getVersionName());
        }
        if (newOne.getVersionType() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(AppVersionTypeEnum.class, newOne.getVersionType()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_INVALID).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            oldOne.setVersionType(newOne.getVersionType());
        }
        if (StrUtil.isNotEmpty(newOne.getPlatform())) {
            oldOne.setPlatform(newOne.getPlatform());
        }
        if (newOne.getReleaseNote() != null) {
            oldOne.setReleaseNote(newOne.getReleaseNote());
        }
        if (newOne.getBuildInfo() != null) {
            oldOne.setBuildInfo(newOne.getBuildInfo());
        }
        return oldOne;
    }

    @Override
    protected void deletePending(AppVersionEntity entity) {
        AssertUtil.isTrue(!AppVersionFlagEnum.DRAFT.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_ONLY_DRAFT_CAN_DELETE).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity submit(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.DRAFT.getCode().equals(entity.getFlag())).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_DRAFT)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.PENDING.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity release(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.PENDING.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_PENDING).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.RELEASE.getCode());
        entity.setReleaseTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity offline(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.RELEASE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_RELEASE).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.OFFLINE.getCode());
        entity.setOfflineTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity rollback(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.RELEASE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_RELEASE).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.ROLLED_BACK.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity archive(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.OFFLINE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_OFFLINE).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.ARCHIVED.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Override
    public AppVersionEntity queryLatestReleased(String appKey, String platform) {
        var qb = QueryEngine.getEntityQueryWrapper(new AppVersionEntity()).query();
        qb.where().equal(AppVersionEntity::getAppKey, appKey).equal(AppVersionEntity::getPlatform, platform)
            .equal(AppVersionEntity::getFlag, AppVersionFlagEnum.RELEASE.getCode()).pour();
        qb.getOrderSupport().ifPresent(order -> order.orderByDesc(AppVersionEntity::getVersionCode));
        return qb.fetchOne();
    }

    @Override
    public List<String> listAppKeys() {
        List<AppVersionEntity> versions = QueryEngine.getEntityQueryWrapper(new AppVersionEntity()).query().where()
            .notEqual(AppVersionEntity::getFlag, AppVersionFlagEnum.ARCHIVED.getCode()).pour().fetch();
        return versions.stream().map(AppVersionEntity::getAppKey).filter(StrUtil::isNotEmpty).distinct().sorted()
            .collect(Collectors.toList());
    }

    /**
     * 校验同一应用+平台下版本号是否重复
     *
     * @param appKey      应用标识
     * @param versionCode 内部版本号
     * @param platform    平台标识
     * @param excludeId   排除的版本ID（编辑场景排除自身）
     */
    private void validateDuplicateVersionCode(String appKey, Long versionCode, String platform, Long excludeId) {
        var existing = QueryEngine.getEntityQueryWrapper(new AppVersionEntity()).query().where()
            .equal(AppVersionEntity::getAppKey, appKey).equal(AppVersionEntity::getVersionCode, versionCode)
            .equal(AppVersionEntity::getPlatform, platform)
            .notEqual(AppVersionEntity::getFlag, AppVersionFlagEnum.ARCHIVED.getCode()).pour().fetchOne();
        AssertUtil.isTrue(existing != null && !existing.getId().equals(excludeId)).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_CODE_DUPLICATE)
            .params(appKey, versionCode, platform).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    private static final IAuditService<AppVersionEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<AppVersionEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
