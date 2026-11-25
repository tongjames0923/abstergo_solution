package com.tbs.app.service.impl.genericapi;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.app.service.genericapi.ISystemGenericApiParamService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemGenericApiMapper;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import com.tbs.sql.service.QueryEngine;
import com.tbs.web.genric.IApiInstancePreAuth;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tbs.app.service.impl.CacheNamesConstants.GENERIC_API;
import static com.tbs.app.service.impl.CacheNamesConstants.GENERIC_API_PARAM;

/**
 * 泛化API配置服务实现类
 *
 * @author abstergo
 */
@Slf4j
public class GenericApiServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemGenericApiEntity, Long, SystemGenericApiMapper>
    implements IGenericApiService {

    private static final IAuditService<SystemGenericApiEntity, Long, Long> auditService = new DefaultAuditService<>();
    @Resource
    private SystemGenericApiMapper genericApiMapper;
    @Resource
    private AppSettingProperty property;
    /**
     * 入参定义服务（主表写路径整组替换参数、删除路径联动清理）
     */
    @Resource
    private ISystemGenericApiParamService genericApiParamService;

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

        AssertUtil.isTrue(StrUtil.isEmpty(entity.getApiId()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_ID_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getApiName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getAppName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_APP_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getInterfaceName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_INTERFACE_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getMethodName()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_METHOD_NAME_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        // 验证预认证Bean名称
        validatePreAuthBeanNames(entity.getPreAuthBeans());

        var i = ((IGenericApiService)getInstance()).getByApiId(entity.getApiId());
        AssertUtil.isTrue(i != null).i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_ID_DUPLICATE)
            .params(entity.getApiId()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

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
    protected SystemGenericApiEntity compareAndUpdateValue(SystemGenericApiEntity newOne,
        SystemGenericApiEntity oldOne) {
        // 验证预认证Bean名称
        validatePreAuthBeanNames(newOne.getPreAuthBeans());

        if (StrUtil.isNotEmpty(newOne.getApiId()) && !Objects.equals(newOne.getApiId(), oldOne.getApiId())) {
            var i = ((IGenericApiService)getInstance()).getByApiId(newOne.getApiId());
            AssertUtil.isTrue(i != null).i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_ID_DUPLICATE)
                .params(newOne.getApiId()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        // 更新API名称
        if (StrUtil.isNotEmpty(newOne.getApiName())) {
            oldOne.setApiName(newOne.getApiName());
        }
        // 更新应用名
        if (StrUtil.isNotEmpty(newOne.getAppName())) {
            oldOne.setAppName(newOne.getAppName());
        }
        // 更新业务分区坐标：传空串表示清除约束（回到不约束），不传则保持原值
        if (newOne.getBusiness() != null) {
            oldOne.setBusiness(StrUtil.isBlank(newOne.getBusiness()) ? null : newOne.getBusiness().trim());
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
        // 更新预认证Bean配置
        if (newOne.getPreAuthBeans() != null) {
            oldOne.setPreAuthBeans(newOne.getPreAuthBeans());
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

    @CacheEvict(cacheNames = GENERIC_API, allEntries = true)
    @Override
    public List<SystemGenericApiEntity> save(SystemGenericApiEntity entity) {
        return super.save(entity);
    }

    @CacheEvict(cacheNames = {GENERIC_API, GENERIC_API_PARAM}, allEntries = true)
    @Override
    public SystemGenericApiEntity deleteById(Long id) {
        SystemGenericApiEntity entity = super.queryById(id);
        SystemGenericApiEntity deleted = super.deleteById(id);
        // 主表行删除后联动清除其入参登记：否则重建同名 apiId 时会拿到上一代的参数定义
        if (entity != null) {
            removeParams(entity.getApiId());
        }
        return deleted;
    }

    @CacheEvict(cacheNames = {GENERIC_API, GENERIC_API_PARAM}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SystemGenericApiEntity saveWithParams(SystemGenericApiEntity apiConfig,
        List<SystemGenericApiParamEntity> params) {
        List<SystemGenericApiEntity> saved = super.save(apiConfig);
        SystemGenericApiEntity first = CollUtil.getFirst(saved);
        SystemGenericApiEntity result = first == null ? apiConfig : first;
        replaceParams(result.getApiId(), params);
        return result;
    }

    /**
     * 整组替换指定 API 的入参定义
     * <p>
     * 先清除现有登记再逐行新增：参数行的语义全在 {@code paramOrder} 上，整组替换不会漏掉被删行； 新行一律清空主键，避开前端回传的旧主键与已删行碰撞。
     * </p>
     *
     * @param apiId  API唯一标识
     * @param params 新的入参定义列表，可以为空
     */
    private void replaceParams(String apiId, List<SystemGenericApiParamEntity> params) {
        removeParams(apiId);
        if (CollUtil.isEmpty(params)) {
            return;
        }
        for (SystemGenericApiParamEntity param : params) {
            param.setId(null);
            param.setApiId(apiId);
            genericApiParamService.save(param);
        }
    }

    /**
     * 清除指定 API 的全部入参登记（逻辑删除）
     *
     * @param apiId API唯一标识
     */
    private void removeParams(String apiId) {
        List<SystemGenericApiParamEntity> existing = genericApiParamService.listByApiId(apiId);
        if (CollUtil.isEmpty(existing)) {
            return;
        }
        for (SystemGenericApiParamEntity param : existing) {
            genericApiParamService.deleteById(param.getId());
        }
    }

    @Override
    public SystemGenericApiEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Override
    public List<IApiInstancePreAuth> getAllAvaliablePreAuthList() {
        return SpringUtil.getBeansByTypeSafe(IApiInstancePreAuth.class).stream().collect(Collectors.toList());
    }

    @Cacheable(cacheNames = GENERIC_API, key = "#apiId", unless = "#result==null")
    @Override
    public SystemGenericApiEntity getByApiId(String apiId) {
        return QueryEngine.getEntityQueryWrapper(new SystemGenericApiEntity()).query().where()
            .equal(SystemGenericApiEntity::getApiId, apiId).pour().fetchOne();
    }

    @Override
    public SystemGenericApiEntity getInvokableApi(String apiId) {
        SystemGenericApiEntity apiConfig = ((IGenericApiService)getInstance()).getByApiId(apiId);
        AssertUtil.isTrue(apiConfig == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(SystemI18nConstant.GENERIC_API_NOT_EXISTS).params(apiId).throwIt();
        AssertUtil.isTrue(!Objects.equals(CommonFlagEnum.ACTIVE.getCode(), apiConfig.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n(SystemI18nConstant.GENERIC_API_DISABLED)
            .params(apiConfig.getApiName()).throwIt();
        return apiConfig;
    }

    @Override
    public SystemGenericApiEntity getPublicInvokableApi(String apiId) {
        SystemGenericApiEntity apiConfig = getInvokableApi(apiId);
        AssertUtil.isTrue(Boolean.TRUE.equals(apiConfig.getNeedAuth())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(SystemI18nConstant.GENERIC_API_NEED_AUTH).params(apiConfig.getApiName()).throwIt();
        return apiConfig;
    }

    @CacheEvict(cacheNames = GENERIC_API, allEntries = true)
    @Override
    public void enableApi(String apiId) {
        SystemGenericApiEntity entity = ((IGenericApiService)getInstance()).getByApiId(apiId);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_NOT_FOUND)
            .params(apiId).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(CommonFlagEnum.ACTIVE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    @CacheEvict(cacheNames = GENERIC_API, allEntries = true)
    @Override
    public void disableApi(String apiId) {
        SystemGenericApiEntity entity = ((IGenericApiService)getInstance()).getByApiId(apiId);
        AssertUtil.isTrue(entity == null).i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_NOT_FOUND)
            .params(apiId).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(CommonFlagEnum.INACTIVE.getCode());
        getMapper().updateByPrimaryKey(entity);
    }

    @Override
    protected Optional<IAuditService<SystemGenericApiEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }

    /**
     * 验证预认证Bean名称是否合法
     *
     * @param preAuthBeans 预认证Bean名称字符串，以逗号分隔
     */
    private void validatePreAuthBeanNames(String preAuthBeans) {
        if (StrUtil.isEmpty(preAuthBeans)) {
            return; // 如果没有配置预认证，则无需验证
        }

        String[] beanNames = preAuthBeans.split(",");

        // 获取所有可用的预认证实现
        List<IApiInstancePreAuth> availablePreAuths = getAllAvaliablePreAuthList();
        Map<String, IApiInstancePreAuth> availablePreAuthMap =
            availablePreAuths.stream().collect(Collectors.toMap(IApiInstancePreAuth::getPreAuthId, preAuth -> preAuth));

        for (String beanName : beanNames) {
            String trimmedBeanName = beanName.trim();
            if (StrUtil.isEmpty(trimmedBeanName)) {
                continue;
            }

            // 检查Bean名称是否存在于Spring容器中
            IApiInstancePreAuth preAuth = null;
            try {
                // 首先尝试按ID查找
                preAuth = availablePreAuthMap.get(trimmedBeanName);
                if (preAuth == null) {
                    // 如果按ID没找到，尝试按Bean名称查找
                    preAuth = SpringUtil.getBean(trimmedBeanName, IApiInstancePreAuth.class);
                }
            } catch (Exception e) {
                // Bean不存在
            }

            AssertUtil.isTrue(preAuth == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_PRE_AUTH_BEAN_NOT_EXISTS)
                .params(trimmedBeanName).throwIt();
        }
    }
}
