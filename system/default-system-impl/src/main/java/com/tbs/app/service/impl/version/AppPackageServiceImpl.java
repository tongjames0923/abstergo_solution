package com.tbs.app.service.impl.version;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.version.IAppPackageService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.version.AppVersionPackageMapper;
import com.tbs.shared.pojo.entity.version.AppPackageEntity;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 应用安装包服务实现
 * <p>
 * 提供应用版本下安装包产物的登记与查询能力， 安装包通过 versionId 挂接到应用版本，支持多架构/多渠道包共存； 文件实体的上传与下载由统一 OSS 门面承担
 *
 * @author abstergo
 */
@Slf4j
public class AppPackageServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<AppPackageEntity, Long, AppVersionPackageMapper>
    implements IAppPackageService {

    private static final IAuditService<AppPackageEntity, Long, Long> auditService = new DefaultAuditService<>();
    @Resource
    private AppVersionPackageMapper appVersionPackageMapper;
    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected AppVersionPackageMapper getMapper() {
        return appVersionPackageMapper;
    }

    @Override
    protected AppPackageEntity newEntity() {
        return new AppPackageEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return CommonFlagEnum.DELETED.getCode();
    }

    @Override
    protected AppPackageEntity insertNew(AppPackageEntity entity) {
        AssertUtil.isTrue(entity.getVersionId() == null)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PACKAGE_VERSION_ID_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getDownloadUrl()) &&
                (StrUtil.isEmpty(entity.getBucketName()) || StrUtil.isEmpty(entity.getObjectName())))
            .i18n(com.tbs.app.constant.SystemI18nConstant.PACKAGE_DOWNLOAD_INFO_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected AppPackageEntity compareAndUpdateValue(AppPackageEntity newOne, AppPackageEntity oldOne) {
        if (newOne.getVersionId() != null) {
            oldOne.setVersionId(newOne.getVersionId());
        }
        if (StrUtil.isNotEmpty(newOne.getBucketName())) {
            oldOne.setBucketName(newOne.getBucketName());
        }
        if (StrUtil.isNotEmpty(newOne.getObjectName())) {
            oldOne.setObjectName(newOne.getObjectName());
        }
        if (StrUtil.isNotEmpty(newOne.getMd5())) {
            oldOne.setMd5(newOne.getMd5());
        }
        if (StrUtil.isNotEmpty(newOne.getDownloadUrl())) {
            oldOne.setDownloadUrl(newOne.getDownloadUrl());
        }
        return oldOne;
    }

    @Override
    public List<AppPackageEntity> listByVersionId(Long versionId) {
        return QueryEngine.getEntityQueryWrapper(new AppPackageEntity()).query().where()
            .equal(AppPackageEntity::getVersionId, versionId)
            .equal(AppPackageEntity::getFlag, CommonFlagEnum.ACTIVE.getCode()).pour().fetch();
    }

    @Override
    protected Optional<IAuditService<AppPackageEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
