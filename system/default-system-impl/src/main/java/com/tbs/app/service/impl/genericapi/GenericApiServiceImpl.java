package com.tbs.app.service.impl.genericapi;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemGenericApiMapper;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.sql.service.QueryEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 泛化API配置服务实现类
 *
 * @author abstergo
 */
@Slf4j
public class GenericApiServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemGenericApiEntity, Long, SystemGenericApiMapper>
    implements IGenericApiService {

    @Resource
    private SystemGenericApiMapper genericApiMapper;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected SystemGenericApiMapper getMapper() {
        return genericApiMapper;
    }

    @Override
    protected SystemGenericApiEntity newEntity() {
        return new SystemGenericApiEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return DataDicFlagEnum.DELETE.getCode();
    }

    @Override
    protected SystemGenericApiEntity insertNew(SystemGenericApiEntity entity) {
        // 设置默认值
        if (entity.getFlag() == null) {
            entity.setFlag(DataDicFlagEnum.ENABLE.getCode());
        }
        if (entity.getNeedAuth() == null) {
            entity.setNeedAuth(false);
        }
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<SystemGenericApiEntity> updateBy(SystemGenericApiEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemGenericApiEntity compareAndUpdateValue(SystemGenericApiEntity newOne,
        SystemGenericApiEntity oldOne) {
        // 更新API名称
        if (StrUtil.isNotEmpty(newOne.getApiName())) {
            oldOne.setApiName(newOne.getApiName());
        }
        // 更新应用名
        if (StrUtil.isNotEmpty(newOne.getAppName())) {
            oldOne.setAppName(newOne.getAppName());
        }
        // 更新接口名
        if (StrUtil.isNotEmpty(newOne.getInterfaceName())) {
            oldOne.setInterfaceName(newOne.getInterfaceName());
        }
        // 更新方法名
        if (StrUtil.isNotEmpty(newOne.getMethodName())) {
            oldOne.setMethodName(newOne.getMethodName());
        }
        // 更新是否需要认证
        if (newOne.getNeedAuth() != null) {
            oldOne.setNeedAuth(newOne.getNeedAuth());
        }
        // 更新版本
        if (newOne.getVersion() != null) {
            oldOne.setVersion(newOne.getVersion());
        }
        // 更新描述
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        // 更新状态
        if (newOne.getFlag() != null) {
            oldOne.setFlag(newOne.getFlag());
        }
        return oldOne;
    }

    @Override
    protected void deletePending(SystemGenericApiEntity entity) throws FormattedRuntimeException {
        // 删除前检查，可以在这里添加业务逻辑检查
    }

    @CacheEvict(cacheNames = "generic_api", key = "#entity.id", condition = "#entity!=null&&#entity.id!=null")
    @Override
    public List<SystemGenericApiEntity> save(SystemGenericApiEntity entity) {
        return super.save(entity);
    }

    @CacheEvict(cacheNames = "generic_api", key = "#id")
    @Override
    public SystemGenericApiEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    @Cacheable(cacheNames = "generic_api", key = "#id", unless = "#result==null")
    @Override
    public SystemGenericApiEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Cacheable(cacheNames = "generic_api", key = "#apiId", unless = "#result==null")
    @Override
    public SystemGenericApiEntity getByApiId(String apiId) {
        return QueryEngine.getEntityQueryWrapper(new SystemGenericApiEntity()).query().where()
            .equal(SystemGenericApiEntity::getApiId, apiId).pour().fetchOne();
    }

    @CacheEvict(cacheNames = "generic_api", key = "#apiId")
    @Override
    public void enableApi(String apiId) {
        SystemGenericApiEntity entity = ((IGenericApiService)getInstance()).getByApiId(apiId);
        AssertUtil.isTrue(entity == null).i18n("generic.api.not.found").params(apiId)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(DataDicFlagEnum.ENABLE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    @CacheEvict(cacheNames = "generic_api", key = "#apiId")
    @Override
    public void disableApi(String apiId) {
        SystemGenericApiEntity entity = ((IGenericApiService)getInstance()).getByApiId(apiId);
        AssertUtil.isTrue(entity == null).i18n("generic.api.not.found").params(apiId)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(DataDicFlagEnum.DISABLE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    private static final IAuditService<SystemGenericApiEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<SystemGenericApiEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
