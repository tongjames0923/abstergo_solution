package com.tbs.app.service.impl.version;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.version.IAppPackageService;
import com.tbs.app.service.version.IAppVersionService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.oss.core.OssClient;
import com.tbs.oss.model.CopyObjectRequest;
import com.tbs.shared.enums.AppVersionFlagEnum;
import com.tbs.shared.enums.AppVersionTypeEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.version.AppVersionMapper;
import com.tbs.shared.pojo.entity.version.AppPackageEntity;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final IAuditService<AppVersionEntity, Long, Long> auditService = new DefaultAuditService<>();
    @Resource
    private AppVersionMapper appVersionMapper;
    @Resource
    private AppSettingProperty property;
    @Resource
    private IAppPackageService appPackageService;
    @Resource
    private OssClient ossClient;

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
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getAppKey()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_APP_KEY_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getVersionCode() == null)
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_CODE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getVersionName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getVersionType() == null)
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!EnumUtil.isValidCode(AppVersionTypeEnum.class, entity.getVersionType()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_INVALID)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getPlatform()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_PLATFORM_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        validateDuplicateVersionCode(entity.getAppKey(), entity.getVersionCode(), entity.getPlatform(), null);

        entity.setFlag(AppVersionFlagEnum.DRAFT.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected AppVersionEntity compareAndUpdateValue(AppVersionEntity newOne, AppVersionEntity oldOne) {
        AssertUtil.isTrue(!AppVersionFlagEnum.DRAFT.getCode().equals(oldOne.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_ONLY_DRAFT_CAN_EDIT)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();

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
                .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_TYPE_INVALID)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
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
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_ONLY_DRAFT_CAN_DELETE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity submit(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.DRAFT.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_DRAFT)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.PENDING.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity release(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.PENDING.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_PENDING)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.RELEASE.getCode());
        entity.setReleaseTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity offline(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.RELEASE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_RELEASE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.OFFLINE.getCode());
        entity.setOfflineTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity rollback(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.RELEASE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_RELEASE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.ROLLED_BACK.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity archive(Long id) {
        AppVersionEntity entity = getInstance().queryById(id);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(!AppVersionFlagEnum.OFFLINE.getCode().equals(entity.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_STATUS_NOT_OFFLINE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        entity.setFlag(AppVersionFlagEnum.ARCHIVED.getCode());
        getMapper().updateByPrimaryKey(entity);
        return entity;
    }

    @Override
    public AppVersionEntity queryLatestReleased(String appKey, String platform) {
        var qb = QueryEngine.getEntityQueryWrapper(new AppVersionEntity()).query();
        qb.where().equal(AppVersionEntity::getAppKey, appKey).equal(AppVersionEntity::getPlatform, platform)
            .equal(AppVersionEntity::getFlag, AppVersionFlagEnum.RELEASE.getCode())
            .andInclude(sub -> sub
                .equal(AppVersionEntity::getVersionType, AppVersionTypeEnum.RELEASE.getCode())
                .or().isNull(AppVersionEntity::getVersionType)).pour();
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AppVersionEntity cloneVersion(Long sourceId, AppVersionEntity override, boolean copyPackages) {
        AppVersionEntity source = getInstance().queryById(sourceId);
        AssertUtil.isTrue(source == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(override == null).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        override.setId(null);
        override.setFlag(AppVersionFlagEnum.DRAFT.getCode());
        List<AppVersionEntity> saved = save(override);
        AssertUtil.isTrue(saved.isEmpty()).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_NOT_EXISTS)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        AppVersionEntity target = saved.get(0);
        if (!copyPackages) {
            return target;
        }
        Map<String, String> copiedObjects = new LinkedHashMap<>();
        try {
            for (AppPackageEntity pkg : appPackageService.listByVersionId(sourceId)) {
                AppPackageEntity copy = new AppPackageEntity();
                copy.setVersionId(target.getId());
                copy.setBucketName(pkg.getBucketName());
                copy.setMd5(pkg.getMd5());
                copy.setDownloadUrl(pkg.getDownloadUrl());
                if (StrUtil.isNotEmpty(pkg.getObjectName())) {
                    String targetKey = buildCopiedObjectKey(target.getId(), pkg.getObjectName());
                    CopyObjectRequest.Builder builder = CopyObjectRequest.builder(pkg.getObjectName(), targetKey);
                    if (StrUtil.isNotEmpty(pkg.getBucketName())) {
                        builder.sourceBucketName(pkg.getBucketName()).targetBucketName(pkg.getBucketName());
                    }
                    ossClient.copyObject(builder.build());
                    copiedObjects.put(targetKey, pkg.getBucketName());
                    copy.setObjectName(targetKey);
                }
                appPackageService.save(copy);
            }
        } catch (RuntimeException e) {
            log.error("复制安装包文件失败，sourceId={}, targetId={}", sourceId, target.getId(), e);
            copiedObjects.forEach((key, bucket) -> {
                try {
                    ossClient.deleteObject(bucket, key);
                } catch (RuntimeException cleanupError) {
                    log.warn("清理复制失败的安装包对象失败，key={}", key, cleanupError);
                }
            });
            AssertUtil.isTrue(true).i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_CLONE_OSS_COPY_FAILED)
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
            return null;
        }
        return target;
    }

    /**
     * 生成复制后的对象 key，保证新版本与源版本文件互不共享
     *
     * @param versionId 新版本ID
     * @param sourceKey 源对象 key
     * @return 新对象 key
     */
    private String buildCopiedObjectKey(Long versionId, String sourceKey) {
        String fileName = sourceKey.contains("/") ? sourceKey.substring(sourceKey.lastIndexOf('/') + 1) : sourceKey;
        return "app-package/" + versionId + "/" + UUID.randomUUID().toString().replace("-", "") + "_" + fileName;
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
        AssertUtil.isTrue(existing != null && !existing.getId().equals(excludeId))
            .i18n(com.tbs.app.constant.SystemI18nConstant.VERSION_CODE_DUPLICATE).params(appKey, versionCode, platform)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    @Override
    protected Optional<IAuditService<AppVersionEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
